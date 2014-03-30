package org.cloudgraph.examples.corpus.index;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.search.WordAggregate;
import org.cloudgraph.examples.corpus.search.WordDependency;
import org.cloudgraph.examples.corpus.search.query.QWordAggregate;
import org.cloudgraph.examples.corpus.search.query.QWordDependency;
import org.cloudgraph.hbase.mapreduce.GraphReducer;
import org.plasma.query.Query;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class WordReducer extends
    GraphReducer<Text, LongWritable, Text> {
	private static Log log = LogFactory.getLog(WordReducer.class);

	/** Counter enumeration */
	public static enum Counters {
		KEYS_STARTED, KEYS_PROCESSED, KEYS_FAILED,
	}

	public void reduce(Text key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException {

		
		try {
			long sum = 0;
			for (LongWritable val : values) {
				sum += val.get();
			}
			context.write(key, new LongWritable(sum));
			context.getCounter(Counters.KEYS_STARTED).increment(1);
			
			//if (log.isDebugEnabled())
			    log.info(String.valueOf(key) + ": " + sum);
			
			
			process(key, sum, context);
			context.getCounter(Counters.KEYS_PROCESSED).increment(1);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			context.getCounter(Counters.KEYS_FAILED).increment(1);
		}
	}
	
	private void process(Text key, long sum, Context context) throws IOException {
		String word = key.toString();
		DataGraph dataGraph = null;
		Query query = createQuery(word);
		DataGraph[] graphs = this.find(query, context);
		if (graphs == null || graphs.length == 0) {
			dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
			dataGraph.getChangeSummary().beginLogging();
			Type rootType = PlasmaTypeHelper.INSTANCE.getType(WordAggregate.class);
			WordAggregate wordAggregate = (WordAggregate) dataGraph.createRootObject(rootType);
			wordAggregate.setLemma(word);
			wordAggregate.setWordCount(sum);
		}
		else {
			if (graphs.length > 1)
				throw new RuntimeException("expected word aggregate single row");
			dataGraph = graphs[0];
			dataGraph.getChangeSummary().beginLogging();
			WordAggregate wordAggregate = (WordAggregate) dataGraph.getRootObject();
			wordAggregate.setWordCount(sum);
		}
		
		this.commit(dataGraph, context);
	}
	
	private Query createQuery(String word) {
		QWordAggregate query = QWordAggregate.newQuery();
		query.select(query.wildcard());
		query.where(query.lemma().eq(word));
		return query;		
	}

}
