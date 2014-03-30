package org.cloudgraph.examples.corpus.nlp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.cloudgraph.examples.corpus.wiki.query.QPage;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.plasma.query.Query;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLDOMParser;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class WikiPageImporterMapper extends
		Mapper<LongWritable, Text, NullWritable, Text> implements PageCallbackHandler {
    private static Log log = LogFactory.getLog(WikiPageImporterMapper.class);
	
	private GraphServiceDelegate service;
	private Context context;
	private StanfordCoreNLP pipeline;
	private WikiAnnotator annotator;
	private static int SENTENCE_MAX = 300;
	private static int SENTENCE_MIN = 12;

	public WikiPageImporterMapper() {
		this.service = new GraphServiceDelegate();
		Properties props = new Properties();
		props.put("annotators",
				"tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		this.pipeline = new StanfordCoreNLP(props);
		this.annotator = new WikiAnnotator();
	}
	
   	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_INPUT,
		PAGES_SUCCESS,
		PAGES_FAILED,
		PAGES_PARSE_SUCCESS,
		PAGES_PARSE_FAILED,
		PAGES_IGNORED,
		PAGES_ALREADY_EXIST,
		SENTENCES_PARSE_IGNORED,
		SENTENCES_PARSE_SUCCESS,
		SENTENCES_PARSE_FAILED,
		REDIRECT_PAGES_IGNORED,
		FILE_PAGES_IGNORED,
	}

	@Override
	public void map(LongWritable key, Text value1, Context context)
	    throws IOException, InterruptedException 
	{
		
        try {
    		this.context = context;
    		//log.info(value1.toString());
			ByteArrayInputStream is = new ByteArrayInputStream(value1.getBytes());
			
			WikiXMLDOMParser parser = new WikiXMLDOMParser(is);
			parser.setPageCallback(this);
			parser.parse();        		
		
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	@Override
	public void process(WikiPage wikiPage) {
		context.getCounter(Counters.PAGES_INPUT).increment(1);
		try {
			
			String redirectPage = wikiPage.getRedirectPage();			

			// skip parsing
			if (redirectPage != null && redirectPage.length() > 0)
			{
				context.getCounter(Counters.REDIRECT_PAGES_IGNORED).increment(1);
				return; // have no text
			}					
			if (wikiPage.getTitle().startsWith("File:")) {
				context.getCounter(Counters.FILE_PAGES_IGNORED).increment(1);
				return; // have no text
			}			
			
			byte[] bytes = wikiPage.getText().getBytes(Charset.forName("UTF-8"));
			String plainText = new String(bytes, "UTF-8");
			if (plainText == null || plainText.trim().length() == 0) {
				context.getCounter(Counters.PAGES_IGNORED).increment(1);
				return;
			}
			
			if (annotator.alphaCharCount(plainText) < 20) {
				context.getCounter(Counters.PAGES_IGNORED).increment(1);
				return;
			}
			
			int pageId = Integer.valueOf(wikiPage.getID()).intValue();
			Page page = null;
			
			DataGraph[] graphs = this.service.find(createPageQuery(pageId), context);
			if (graphs == null || graphs.length == 0) {
				// now create it
				DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
				dataGraph.getChangeSummary().beginLogging();
				Type rootType = PlasmaTypeHelper.INSTANCE.getType(Page.class);
				log.info("creating: " + wikiPage.getTitle() + "("+wikiPage.getID()+")");
				page = (Page) dataGraph.createRootObject(rootType);
				page.setPageTitle(wikiPage.getTitle());
				page.setPageId(Integer.valueOf(wikiPage.getID()));
			}
			else {
				context.getCounter(Counters.PAGES_ALREADY_EXIST).increment(1);
				return;
			}						
	
			//for (String lnk : wikiPage.getLinks()) {
			//	Pagelinks link = page.createPagelinks();
			//	link.setPlTitle(lnk);
			//}
			//for (String cat : wikiPage.getCategories()) {
			//	Categorylinks link = page.createCategorylinks();
			//	link.setClTo(cat);
			//}
			
			Revision revision = page.createRevision();
			revision.setRevId(revision.getRevId());
			
			org.cloudgraph.examples.corpus.wiki.Text text = revision.createPlainText();
			text.setOldText(bytes);

			Document parseDocument = page.createDocument();
			
			try {
				parse(plainText, parseDocument);
			    context.getCounter(Counters.PAGES_PARSE_SUCCESS).increment(1);
			}
			catch (Throwable t) {
				context.getCounter(Counters.PAGES_PARSE_FAILED).increment(1);
				log.error(t.getMessage(), t);
			}
		    String xml = serialize(parseDocument);
		    parseDocument.setBody(xml);
			
			this.service.commit(page.getDataGraph(), this.context);
			
			context.getCounter(Counters.PAGES_SUCCESS).increment(1);
		} catch (Exception ex) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.error(ex.getMessage(), ex);
		}
	}
	
	private Query createPageQuery(int id) {
		QPage query = QPage.newQuery();
		query.select(query.wildcard());
		query.where(query.pageId().eq(id));
		return query;		
	}
			
	private void parse(String plainText, Document parseDocument) throws IOException
	{
		Sentence previousSentence = null;
		Sentence currentSentence = null;
		Sentence nextSentence = null;
		SentenceBreak[] breaks = SentenceUtil.getBreaks(plainText);
				
		for (int i = 0; i < breaks.length; i++) {
			SentenceBreak brk = breaks[i];

			previousSentence = currentSentence;
			currentSentence = nextSentence;
			
            String nextSentenceText = plainText.substring(brk.firstIndex, brk.lastIndex);			
            nextSentence = parseDocument.createSentence();			
            //currentSentence.setText(currentSentenceText); // another mapper pass
			/*
            if (currentSentence != null) {
			    if (previousSentence != null)
				    currentSentence.setPrevious(previousSentence);
			    if (nextSentence != null)
				    currentSentence.setNext(nextSentence);
            }
            */
			
			// decide if we're going to actually parse it
            if (nextSentenceText.length() > SENTENCE_MAX) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence exceeded max length ("+SENTENCE_MAX+") - ignoring");
    			context.getCounter(Counters.SENTENCES_PARSE_IGNORED).increment(1);
            	continue;
            }
            if (nextSentenceText.length() < SENTENCE_MIN) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence under min length ("+SENTENCE_MIN+") - ignoring");
    			context.getCounter(Counters.SENTENCES_PARSE_IGNORED).increment(1);
            	continue;
            }
            if (!this.annotator.onlyAlphaAndPunctuation(nextSentenceText)) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence contains non punctuation chars - ignoring");
    			context.getCounter(Counters.SENTENCES_PARSE_IGNORED).increment(1);
            	continue;
            }
            
            // ok parse it
			long before = System.currentTimeMillis();
		    Annotation document = new Annotation(nextSentenceText);
		    this.pipeline.annotate(document);
		    
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);	
		    int parseSentCount = 0;
		    for (CoreMap sentenceAnnotation : sentences) {
		    	parseSentCount++;
		    	if (parseSentCount > 1) {
		    		log.warn("parsed multiple sentences from sentence break - ignoring");
		    		continue;
		    	}
		    	this.annotator.buildSentence(this.pipeline,				    		
			    	sentenceAnnotation, nextSentence, 
			    	brk.firstIndex, brk.lastIndex, parseDocument);		    
		    }
			
			long after = System.currentTimeMillis();
			if (log.isDebugEnabled())
        	    log.debug("parse: " + String.valueOf(after - before) + ": " + nextSentenceText);
			context.getCounter(Counters.SENTENCES_PARSE_SUCCESS).increment(1);
         }
	}

	private String serialize(Document parseDocument) throws IOException {
		DefaultOptions options = new DefaultOptions(parseDocument.getType().getURI());
		options.setRootNamespacePrefix("ns1");
		XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(
				parseDocument,
				parseDocument.getType().getURI(), null);
		options.setPrettyPrint(false);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PlasmaXMLHelper.INSTANCE.save(doc, os, options);
		os.flush();
		return new String(os.toByteArray(), "UTF-8");
	}
}
