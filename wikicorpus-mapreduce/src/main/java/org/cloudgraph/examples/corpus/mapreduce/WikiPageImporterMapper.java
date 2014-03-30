package org.cloudgraph.examples.corpus.mapreduce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLDOMParser;
import edu.jhu.nlp.wikipedia.WikiXMLSAXParser;

public class WikiPageImporterMapper extends
		Mapper<LongWritable, Text, NullWritable, Text> implements PageCallbackHandler {
    private static Log log = LogFactory.getLog(WikiPageImporterMapper.class);
	
	private GraphServiceDelegate service;
	private Context context;

	public WikiPageImporterMapper() {
		this.service = new GraphServiceDelegate();
	}
	
   	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_SUCCESS,
		PAGES_FAILED,
		REDIRECT_PAGES_SKIPPED,
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
		try {
			
			String redirectPage = wikiPage.getRedirectPage();
			if (redirectPage != null && redirectPage.length() > 0)
			{
				context.getCounter(Counters.REDIRECT_PAGES_SKIPPED).increment(1);
				return; // have no text
			}					
			
			DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
			dataGraph.getChangeSummary().beginLogging();
			Type rootType = PlasmaTypeHelper.INSTANCE.getType(Page.class);
			log.info("creating: " + wikiPage.getTitle() + "("+wikiPage.getID()+")");
			Page page = (Page) dataGraph.createRootObject(rootType);
			page.setPageTitle(wikiPage.getTitle());
			page.setPageId(Integer.valueOf(wikiPage.getID()));
	
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
			byte[] bytes = wikiPage.getText().getBytes(Charset.forName("UTF-8"));
			text.setOldText(bytes);
			
			this.service.commit(dataGraph, this.context);
			context.getCounter(Counters.PAGES_SUCCESS).increment(1);
		} catch (Exception ex) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.error(ex);
		}
	}

}
