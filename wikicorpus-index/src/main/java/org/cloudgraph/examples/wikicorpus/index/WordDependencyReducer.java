package org.cloudgraph.examples.wikicorpus.index;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.search.WordDependency;
import org.cloudgraph.examples.corpus.search.query.QWordDependency;
import org.cloudgraph.hbase.mapreduce.GraphReducer;
import org.plasma.query.Query;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class WordDependencyReducer extends
    GraphReducer<Text, IntWritable, Text> {
	private static Log log = LogFactory.getLog(WordDependencyReducer.class);

	/** Counter enumeration. */
	public static enum Counters {
		KEYS_STARTED, KEYS_PROCESSED, KEYS_FAILED,
	}

	public void reduce(Text key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {

		
		try {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
			context.getCounter(Counters.KEYS_STARTED).increment(1);
			
			HashSet<String> seen = new HashSet<String>();
			if (seen.contains(key.toString())) {
				log.warn("duplicate key '"+String.valueOf(key)+"' in reducer");
			}
			else
			    seen.add(key.toString());
			
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
	
	private void process(Text key, int sum, Context context) throws IOException {
		String[] tokens = key.toString().split(":");
		String prefix = tokens[0];
		String word = tokens[1];
		String depType = tokens[2];
		DataGraph dataGraph = null;
		Query query = createQuery(word, depType);
		DataGraph[] graphs = this.find(query, context);
		if (graphs == null || graphs.length == 0) {
			dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
			dataGraph.getChangeSummary().beginLogging();
			Type rootType = PlasmaTypeHelper.INSTANCE.getType(WordDependency.class);
			WordDependency wordDependency = (WordDependency) dataGraph.createRootObject(rootType);
			wordDependency.setLemma(word);
			wordDependency.setDependencyType(depType);
			this.updateAggregates(wordDependency, prefix, depType, sum);
		}
		else {
			if (graphs.length > 1)
				throw new RuntimeException("expected governor single row");
			dataGraph = graphs[0];
			dataGraph.getChangeSummary().beginLogging();
			WordDependency wordDependency = (WordDependency) dataGraph.getRootObject();
			this.updateAggregates(wordDependency, prefix, depType, sum);
		}
		
		this.commit(dataGraph, context);
	}
	
	private void updateAggregates(WordDependency wordDependency, String prefix,
			String depType, int sum) {
		if ("g".equals(prefix)) {
			wordDependency.setGovernorCount(sum);
		}
		else if ("d".equals(prefix)) {
			wordDependency.setDependentCount(sum);
		}
		else
			throw new RuntimeException("expected prefix, [g, d]");
	}
	
	private Query createQuery(String word, String type) {
		QWordDependency query = QWordDependency.newQuery();
		query.select(query.wildcard());
		query.where(query.lemma().eq(word).and(query.dependencyType().eq(type)));
		return query;		
	}

}
