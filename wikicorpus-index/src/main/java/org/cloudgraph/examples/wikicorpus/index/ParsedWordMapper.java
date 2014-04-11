package org.cloudgraph.examples.wikicorpus.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Node;
import org.cloudgraph.examples.corpus.parse.PageParse;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphWritable;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class ParsedWordMapper extends GraphMapper<Text, LongWritable> {
	private static Log log = LogFactory.getLog(ParsedWordMapper.class);


	public ParsedWordMapper() {
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_STARTED, PAGES_PROCESSED, PAGES_FAILED,
		TEST_GOV_LEMMA_1, TEST_GOV_LEMMA_2,
		TEST_DEP_LEMMA_1, TEST_DEP_LEMMA_2,
	}
	
	String testLemma1 = "society";
	String testLemma2 = "have";

	@Override
	public void map(ImmutableBytesWritable row, GraphWritable graph,
			Context context) throws IOException {
		
		context.getCounter(Counters.PAGES_STARTED).increment(1);
		
		try {
			// track changes
			graph.getDataGraph().getChangeSummary().beginLogging();

			PageParse pageParse = (PageParse) graph.getDataGraph().getRootObject();
			//log.info("GRAPH: " + graph.toXMLString());
			log.info(pageParse.getPageId());
			
			String bodyXml = pageParse.getXml();
			Document parseDoc = deserialize(bodyXml, Document.NAMESPACE_URI);
			

			Map<String, Long> map = collect(parseDoc, context);
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Long count = map.get(key);
				Text text = new Text(key);
				context.write(text, new LongWritable(count));
			}
			
			context.getCounter(Counters.PAGES_PROCESSED).increment(1);

		} catch (Throwable t) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.info(t.getMessage(), t);
		}
	}
	
	private Map<String, Long> collect(Document parseDoc, Context context) {
		Map<String, Long> result = new HashMap<String, Long>();
		if (parseDoc.getSentence() != null)
			for (Sentence sent : parseDoc.getSentence()) {
				
				for (Node node : sent.getNode()) {
					Long count = result.get(node.getLemma());
					if (count == null) {
						count = new Long(1);
					}
					else {
						count = new Long(count.longValue() + 1);	
					}
					result.put(node.getLemma(), count);
				}
			}

		return result;
	}
	
	private Document deserialize(String xml, String uri) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));		
		DefaultOptions options = new DefaultOptions(uri);
		options.setRootNamespacePrefix("ns1");
		options.setValidate(false); // no XML schema for the doc necessary or present		
		XMLDocument doc = PlasmaXMLHelper.INSTANCE.load(is, uri, options);
		return (Document)doc.getRootObject();		
	}
	
}
