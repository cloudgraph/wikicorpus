package org.cloudgraph.examples.wikicorpus.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.Revision;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphWritable;

public class SentenceProcessorMapper extends GraphMapper<Text, IntWritable> {
	private static Log log = LogFactory.getLog(SentenceProcessorMapper.class);


	public SentenceProcessorMapper() {
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_STARTED, PAGES_PROCESSED, PAGES_FAILED,
		SENTENCES_STARTED, SENTENCES_PROCESSED, SENTENCES_FAILED,
	}
	
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
			
			Revision revision = page.getRevision(0);
			byte[] textBytes = revision.getPlainText().getOldText();
			String plainTExt = new String(textBytes, "UTF-8");
			
			process(page.getDocument(), plainTExt, context);
			
			commit(page.getDataGraph(), context);
			
			context.getCounter(Counters.PAGES_PROCESSED).increment(1);

		} catch (Throwable t) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.info(t.getMessage(), t);
		}
	}
	
	private void process(Document parseDoc, String plainText, Context context) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		if (parseDoc.getSentenceCount() > 0) {
			Sentence[] sentences = parseDoc.getSentence();
			Sentence previous = null;
			Sentence next = null;
			for (int i = 0; i < sentences.length; i++) {
				Sentence current = sentences[i];
				if (i > 0)
					previous = sentences[i - 1];
				if (i < (sentences.length-1))
					next = sentences[i + 1];
		        String nextSentenceText = plainText.substring(
		        		current.getCharacterOffsetBegin(), current.getCharacterOffsetEnd());	
		        current.setText(nextSentenceText);	
		        if (previous != null)
		        	current.setPrevious(previous);
		        if (next != null)
		        	current.setNext(next);
				context.getCounter(Counters.SENTENCES_PROCESSED).increment(1);
			}
		}
	}
	
	
}
