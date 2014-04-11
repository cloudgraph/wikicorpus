package org.cloudgraph.examples.wikicorpus.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.cloudgraph.examples.corpus.wiki.Text;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.access.client.DataAccessClient;
import org.plasma.sdo.access.client.PojoDataAccessClient;
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

public class WikiServiceImpl implements WikiService, PageCallbackHandler {
	private static Log log = LogFactory.getLog(WikiServiceImpl.class);
    private DataAccessClient service; 	
    private int batchSize = 1;
    private boolean commit;
    private int current = 0;
    private List<DataGraph> commitList = new ArrayList<DataGraph>();
    
    public WikiServiceImpl() {
    	this.service = new PojoDataAccessClient(DataAccessProviderName.HBASE);
     }

	@Override
	public void load(File file, int batchSize, boolean commit) {
		this.batchSize = batchSize;
		this.commit = commit;
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(file.getAbsolutePath());
        try {
			wxsp.setPageCallback(this);
	        wxsp.parse();        
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void process(WikiPage wikiPage) {
		DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		dataGraph.getChangeSummary().beginLogging();
		Type rootType = PlasmaTypeHelper.INSTANCE.getType(Page.class);
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
		revision.setRevId(page.getPageId());
		
		Text text = revision.createPlainText();
		byte[] bytes = wikiPage.getText().getBytes(Charset.forName("UTF-8"));
		//text.setOldText(bytes);
		current++;		
		commitList.add(dataGraph);
		
		String xml = "";
		//if (log.isDebugEnabled())
			try {
				xml = serializeGraph(dataGraph);
				log.info(xml);
			} catch (IOException e) {
			}		
	    DefaultOptions options = new DefaultOptions(dataGraph.getRootObject().getType().getURI());
		options.setRootNamespacePrefix("wiki");
		options.setValidate(false);
			
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
		XMLDocument doc = null;
		try {
			doc = PlasmaXMLHelper.INSTANCE.load(is, dataGraph.getRootObject().getType().getURI(), options);
		} catch (IOException e) {
			log.error(e);
		}
	    DataGraph dataGraph2 = doc.getRootObject().getDataGraph();
	    try {
			log.info(serializeGraph(dataGraph2));
		} catch (IOException e) {
			log.error(e);
		}
		
		if (commit) {
			if (current == this.batchSize) {
				log.info("comitting " +commitList.size()+ " page batch");
		        service.commit(commitList.toArray(new DataGraph[commitList.size()]), 
		    	    this.getClass().getSimpleName());
		        commitList.clear();
		        current = 0;
			}
		}
	}

	protected String serializeGraph(DataGraph graph) throws IOException {
		DefaultOptions options = new DefaultOptions(graph.getRootObject()
				.getType().getURI());
		options.setRootNamespacePrefix("wiki");

		XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(
				graph.getRootObject(),
				graph.getRootObject().getType().getURI(), null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PlasmaXMLHelper.INSTANCE.save(doc, os, options);
		os.flush();
		os.close();
		String xml = new String(os.toByteArray());
		return xml;
	}
}
