package org.cloudgraph.examples.wikicorpus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.wiki.query.QCategory;
import org.cloudgraph.examples.corpus.wiki.query.QCategorylinks;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.JDBCPojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class CategoryExport {
	private static Log log = LogFactory.getLog(CategoryExport.class);
	protected String username = "export";
	private static String ARG_BATCH_FILE = "-file";
	private static String ARG_BATCH_SIZE = "-size";
	private static String ARG_BATCH_TYPE = "-type";
	private File file;
	private boolean commit = false;
	private int incr = 1000;
	private ExportType exportType = ExportType.categories;
	protected SDODataAccessClient rdbService;
	
	private enum ExportType {
		categories,
		links
	}

	private CategoryExport(Map<String, String> args) {
		String value = args.get(ARG_BATCH_FILE);
		if (value != null)
			file = new File(value);
		else {
			printUsage();
			System.exit(-1);
		}
			
		value = args.get(ARG_BATCH_SIZE);
		if (value != null)
			incr = Integer.valueOf(value).intValue();
		value = args.get(ARG_BATCH_TYPE);
		if (value != null)
			exportType = ExportType.valueOf(value);
		
		this.rdbService = new SDODataAccessClient(
				new JDBCPojoDataAccessClient());
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			//fos.write("<?xml version=\"1.0\"?>\n".getBytes());
			
			switch (exportType) {
			case categories:
			    export(createCategoryQuery(), fos);
			    break;
			case links:
			    export(createCategoryLinksQuery(), fos);
			    break;
			}
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
				}
			}			    
		}
	}

    private Query createCategoryQuery() {
    	QCategory query = QCategory.newQuery();
    	query.select(query.wildcard());
    	
    	return query;
    }
    
    private Query createCategoryLinksQuery() {
    	QCategorylinks query = QCategorylinks.newQuery();
    	query.select(query.wildcard())
    	    .select(query.clTo().catTitle())
    	    .select(query.clTo().catId());
    	
    	return query;
    }
	
	private void export(Query query, FileOutputStream fos) throws IOException {
		
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr - 1)));
			query.setStartRange(start);
			query.setEndRange(start + (incr - 1));
			DataGraph[] graphs = this.rdbService.find(query, 100000);
			log.info("found " + graphs.length + " results");
			if (graphs.length > 0) {
				for (DataGraph graph : graphs) {
					byte[] xmlbytes  = serializeGraph(graph);
					fos.write(xmlbytes);
					fos.write("\n".getBytes());
				}
			} else
				break;
		}
		log.info("done.");
	}

	protected byte[] serializeGraph(DataGraph graph) throws IOException {
		DefaultOptions options = new DefaultOptions(graph.getRootObject()
				.getType().getURI());
		options.setRootNamespacePrefix("c");
		options.setPrettyPrint(false);

		XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(
				graph.getRootObject(),
				graph.getRootObject().getType().getURI(), null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		doc.setXMLDeclaration(false);
		PlasmaXMLHelper.INSTANCE.save(doc, os, options);
		os.flush();
		os.close();
		return os.toByteArray();
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			for (int i = 0; i < args.length; i += 2) {
				map.put(args[i], args[i + 1]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			printUsage();
			return;
		}
        new CategoryExport(map);
	}

	private static void printUsage() {
		System.out
				.println("--------------------------------------------------------------------------");
		System.out
				.println("java -jar wikicorpus-export-0.5.1.jar [-file wiki-xml-dump-file] [-size record-chunk-size] [-commit true|false]");
		System.out
				.println("--------------------------------------------------------------------------");
		System.out.println("examples:");
		System.out
				.println("java -jar wikicorpus-export-0.5.1.jar -file ./enwiki-20130805-pages-articles19.xml -size 100 -commit false");
		System.out
				.println("java -Dfuml.configuration=fuml-config.xml -jar wikicorpus-export-0.5.1.jar -file ./enwiki-20130805-pages-articles19.xml -size 10000 -commit true");
		System.out
				.println("--------------------------------------------------------------------------");
	}

}
