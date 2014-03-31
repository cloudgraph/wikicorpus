package org.cloudgraph.examples.wikicorpus.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphWritable;

public class WordMapper extends GraphMapper<Text, LongWritable> {
	private static Log log = LogFactory.getLog(WordMapper.class);


	public WordMapper() {
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_STARTED, PAGES_PROCESSED, PAGES_FAILED,
	}
	
	@Override
	public void map(ImmutableBytesWritable row, GraphWritable graph,
			Context context) throws IOException {
		
		context.getCounter(Counters.PAGES_STARTED).increment(1);
		
		try {
			Page page = (Page) graph.getDataGraph().getRootObject();
			//log.info("GRAPH: " + graph.toXMLString());
			//log.info(page.getPageTitle());
			Revision revision = page.getRevision(0);
			byte[] textBytes = revision.getPlainText().getOldText();
			String plainText = new String(textBytes, "UTF-8");
			
			Map<String, Long> map = collect(plainText, context);
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
	
	private Map<String, Long> collect(String text, Context context) throws IOException
	{
		Map<String, Long> result = new HashMap<String, Long>();
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);     
		StringReader in = new StringReader(text);
		TokenStream ts = analyzer.tokenStream("body", in);
		OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		while (ts.incrementToken()) {
		    int startOffset = offsetAttribute.startOffset();
		    int endOffset = offsetAttribute.endOffset();
		    String term = charTermAttribute.toString();
		    Long count = result.get(term);
			if (count == null) 
				count = new Long(1);
			else 
				count = new Long(count.longValue() + 1);	
			result.put(term, count);
		}
		analyzer.close();
		
		return result;
	}
	 
	
}
