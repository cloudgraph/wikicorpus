package org.cloudgraph.examples.corpus.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.DependencySet;
import org.cloudgraph.examples.corpus.parse.Dependent;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.parse.Node;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphWritable;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class WordDependencyMapper extends GraphMapper<Text, IntWritable> {
	private static Log log = LogFactory.getLog(WordDependencyMapper.class);


	public WordDependencyMapper() {
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

			Page page = (Page) graph.getDataGraph().getRootObject();
			//log.info("GRAPH: " + graph.toXMLString());
			log.info(page.getPageTitle());
			
			String bodyXml = page.getDocument().getBody();
			Document parseDoc = deserialize(bodyXml, Document.NAMESPACE_URI);
			

			Map<String, Integer> map = collect(parseDoc, context);
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Integer count = map.get(key);
				Text text = new Text(key);
				context.write(text, new IntWritable(count));
			}
			
			context.getCounter(Counters.PAGES_PROCESSED).increment(1);

		} catch (Throwable t) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.info(t.getMessage(), t);
		}
	}
	
	private Map<String, Integer> collect(Document parseDoc, Context context) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		if (parseDoc.getSentence() != null)
			for (Sentence sent : parseDoc.getSentence()) {
				DependencySet[] dSets = sent.getDependencySet();
				if (dSets == null)
					continue;
				for (DependencySet dset : dSets) {
					for (Dependency dependency : dset.getDependency()) {
						Governor governor = dependency.getGovernor();
						if (governor == null || governor.getNode() == null) {
							if (log.isDebugEnabled())
							    log.debug("no governor for dependency " + dset.getRepresentation() + "/" + dependency.getType_() + " when processing sentence: "
									+ sent.getCharacterOffsetBegin() + "/" + sent.getCharacterOffsetEnd());
							continue;
						}
						Node govNode = governor.getNode();
						if (govNode.getWord() == null) {
							if (log.isDebugEnabled())
							    log.debug("no governor word for dependency " + dset.getRepresentation() + "/" + dependency.getType_() + " when processing sentence: "
									+ sent.getCharacterOffsetBegin() + "/" + sent.getCharacterOffsetEnd());
							continue;
						}
						String key = getGovernorKey(govNode, dependency);
						increment(key, result);
						if (testLemma1.equals(govNode.getLemma()))
						    context.getCounter(Counters.TEST_GOV_LEMMA_1).increment(1);
						if (testLemma2.equals(govNode.getLemma()))
						    context.getCounter(Counters.TEST_GOV_LEMMA_2).increment(1);
						
						
						Dependent dependent = dependency.getDependent();
						if (dependent == null || dependent.getNode() == null) {
							if (log.isDebugEnabled())
							    log.debug("no dependent for dependency " + dset.getRepresentation() + "/" + dependency.getType_() + " when processing sentence: "
									+ sent.getCharacterOffsetBegin() + "/" + sent.getCharacterOffsetEnd());
							continue;
						}
						Node depNode = dependent.getNode();
						if (depNode.getWord() == null) {
							if (log.isDebugEnabled())
							    log.debug("no dependent word for dependency " + dset.getRepresentation() + "/" + dependency.getType_() + " when processing sentence: "
									+ sent.getCharacterOffsetBegin() + "/" + sent.getCharacterOffsetEnd());
							continue;
						}						
						
						key = getDependentKey(depNode, dependency);
						increment(key, result);
						
						if (testLemma1.equals(depNode.getLemma()))
						    context.getCounter(Counters.TEST_DEP_LEMMA_1).increment(1);
						if (testLemma2.equals(depNode.getLemma()))
						    context.getCounter(Counters.TEST_DEP_LEMMA_2).increment(1);
					}
				}
			}

		return result;
	}
	
	private void increment(String key, Map<String, Integer> result) {
		Integer count = result.get(key);
		if (count == null) {
			count = new Integer(1);
		}
		else {
			count = new Integer(count.intValue() + 1);	
		}
		result.put(key, count);
	}
	
	private String getGovernorKey(Node node, Dependency dependency) {
		StringBuilder buf = new StringBuilder();
		buf.append("g");
		buf.append(":");
		buf.append(node.getLemma().toLowerCase());
		buf.append(":");
		buf.append(dependency.getType_());
		return buf.toString();
	}

	private String getDependentKey(Node node, Dependency dependency) {
		StringBuilder buf = new StringBuilder();
		buf.append("d");
		buf.append(":");
		buf.append(node.getLemma().toLowerCase());
		buf.append(":");
		buf.append(dependency.getType_());
		return buf.toString();
	}
	
	private Document deserialize(String xml, String uri) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));		
		DefaultOptions options = new DefaultOptions(uri);
		options.setRootNamespacePrefix("ns1");
		options.setValidate(false); // no XML schema for the doc necessary or present		
		XMLDocument doc = PlasmaXMLHelper.INSTANCE.load(is, uri, options);
		return (Document)doc.getRootObject();		
	}

	/*
	{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);     
		StringReader in = new StringReader("text to magically vectorize");
		TokenStream ts = analyzer.tokenStream("body", in);
		TermAttribute termAtt = ts.addAttribute(TermAttribute.class);

		Vector v1 = new RandomAccessSparseVector(100);                   
		while (ts.incrementToken()) {
		  char[] termBuffer = termAtt.termBuffer();
		  int termLen = termAtt.termLength();
		  String w = new String(termBuffer, 0, termLen);                 
		  encoder.addToVector(w, 1, v1);                                 
		}
		
	}
	*/
	
}
