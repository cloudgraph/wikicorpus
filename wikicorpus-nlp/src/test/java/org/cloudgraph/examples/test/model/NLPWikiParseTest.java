/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.examples.test.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.corpus.nlp.SentenceBreak;
import org.cloudgraph.examples.corpus.nlp.SentenceUtil;
import org.cloudgraph.examples.corpus.nlp.WikiAnnotator;
import org.cloudgraph.examples.corpus.nlp.WikiPageImporterMapper.Counters;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 */
public class NLPWikiParseTest extends CommonTest {
	private static Log log = LogFactory.getLog(NLPWikiParseTest.class);
	private static int SENTENCE_MAX = 300;
	private static int SENTENCE_MIN = 12;
	private StanfordCoreNLP pipeline;
	private WikiAnnotator annotator;

	public void setUp() throws Exception {
		super.setUp();
		if (annotator == null) {
		    annotator = new WikiAnnotator();
		    Properties props = new Properties();
		    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		    //props.put("annotators", "tokenize, ssplit, pos, lemma");
		    pipeline = new StanfordCoreNLP(props);
		}
	}

	public void testParseLocal() throws Exception {
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser("../../data/enwiki-20130805-pages-articles1.xml-p000000010p000010000");
		wxsp.setPageCallback(new MyHandler());
        wxsp.parse();        
	}	
	
	class MyHandler implements PageCallbackHandler {
		
		private StanfordCoreNLP pipeline;
		{
			Properties props = new Properties();
			//props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			pipeline = new StanfordCoreNLP(props);
			
		}

		@Override
		public void process(WikiPage wikiPage) {
			
			String redirectPage = wikiPage.getRedirectPage();
			// skip parsing
			if (redirectPage != null && redirectPage.length() > 0)
			{
				return; // have no text
			}					
			if (wikiPage.getTitle().startsWith("File:"))			{
				return; // have no text
			}	
			byte[] bytes = wikiPage.getText().getBytes(Charset.forName("UTF-8"));
			String plainText = new String(bytes, Charset.forName("UTF-8"));
			if (plainText == null || plainText.trim().length() == 0) {
				log.info("ignoring: " + wikiPage.getTitle());
				return;
			}
			
			if (annotator.alphaCharCount(plainText) < 40) {
				log.info("ignoring: " + wikiPage.getTitle());
				return;
			}

			//log.info("TITLE: " + page.getTitle());
			//StringBuilder buf = new StringBuilder();
			//for (String cat : page.getCategories()) {
			//	buf.append("\t" + cat);
			//}
			//log.info("CATEGORIES: " + buf.toString());
			//if (page.getInfoBox() != null)
			//    log.info("INFOBOX: " + page.getInfoBox().dumpRaw());
			//buf = new StringBuilder();
			//for (String link : page.getLinks()) {
			//	buf.append("\t" + link);
			//}
			//log.info("LINKS: " + buf.toString());
			//log.info("TEXT: " + page.getText());
			writePlanTextFile(wikiPage, plainText);
			
			
			DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
			dataGraph.getChangeSummary().beginLogging();
			Type rootType = PlasmaTypeHelper.INSTANCE.getType(Page.class);
			log.info("creating: " + wikiPage.getTitle() + "("+wikiPage.getID()+")");
			Page page = (Page) dataGraph.createRootObject(rootType);
			page.setPageTitle(wikiPage.getTitle());
			page.setPageId(Integer.valueOf(wikiPage.getID()));
			Revision revision = page.createRevision();
			revision.setRevId(revision.getRevId());			
			org.cloudgraph.examples.corpus.wiki.Text text = revision.createPlainText();
			text.setOldText(bytes);
			
			Document parseDocument = page.createDocument();
			
			parse(plainText, parseDocument);

		    String xml;
			try {
				xml = serialize(parseDocument);
				log.info(xml);
			    parseDocument.setBody(xml);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			for (Sentence sent : parseDocument.getSentence()) {
				Sentence previous = sent.getPrevious();
				Sentence next = sent.getNext();
				int i = 0;
				i++;
			}

			
			
		}
		
	}
	
	private void parse(String plainText, Document parseDocument)
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
			
            if (currentSentence != null) {
			    if (previousSentence != null)
				    currentSentence.setPrevious(previousSentence);
			    if (nextSentence != null)
				    currentSentence.setNext(nextSentence);
            }
			
			// decide if we're going to actually parse it
            if (nextSentenceText.length() > SENTENCE_MAX) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence exceeded max length ("+SENTENCE_MAX+") - ignoring");
            	continue;
            }
            if (nextSentenceText.length() < SENTENCE_MIN) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence under min length ("+SENTENCE_MIN+") - ignoring");
             	continue;
            }
            if (!this.annotator.onlyAlphaAndPunctuation(nextSentenceText)) {
            	if (log.isDebugEnabled())
            	    log.debug("sentence contains non punctuation chars - ignoring");
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
         }
	}
		
	
	private void writePlanTextFile(WikiPage page, String text) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(new File("./target/", page.getTitle().trim().replace("/", "_") + ".txt"));
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(page.getText());
		} catch (IOException e) {
			log.error(e);
		} finally {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}
	
	private void parse(StringBuilder buf) throws IOException {
		BreakIterator iterator =
                BreakIterator.getSentenceInstance(Locale.US);

		String text = buf.toString();
		int counter = 0;
		iterator.setText(text);

        int lastIndex = iterator.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = iterator.next();

            if (lastIndex != BreakIterator.DONE) {
                String sentence = text.substring(firstIndex, lastIndex);
    			long before = System.currentTimeMillis();
    			//parse(sentence);
    			long after = System.currentTimeMillis();
    			log.info("time4: " + String.valueOf(after - before) + ": " + sentence);
                counter++;
            }
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