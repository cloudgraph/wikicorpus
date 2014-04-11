package org.cloudgraph.examples.wikicorpus.index;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.examples.corpus.parse.query.QPageParse;
import org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.plasma.query.model.Query;

/**
 * hadoop jar /home/lib/wikicorpus-index-0.5.1.jar org.cloudgraph.examples.wikicorpus.index.SentenceProcessorDriver -libjars ${LIBJARS} 
*/
public class SentenceProcessorDriver {
	private static Log log = LogFactory.getLog(SentenceProcessorDriver.class);

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + SentenceProcessorDriver.class.getSimpleName());
	}
	
	/*
	 * @param errorMessage Can attach a message when error occurs.
	 */
	private static void printUsage(String errorMessage) {
		System.err.println("ERROR: " + errorMessage);
		printUsage();
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Configuration conf = CloudGraphContext.instance().getConfig();
		
		conf.set("mapred.child.java.opts", "-Xms256m -Xmx2g -XX:+UseSerialGC");
		conf.set("mapred.job.map.memory.mb", "4096");
		conf.set("mapred.job.reduce.memory.mb", "1024");
		
		//long milliSeconds = 1000*60*20; // 60 minutes rather than default 10 min
		//conf.setLong("mapred.task.timeout", milliSeconds);
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 0) {
			printUsage("Wrong number of parameters: " + args.length);
			System.exit(-1);
		}
		try {
			runJob(conf);

		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public static void runJob(Configuration conf) throws IOException {


		Job job = new Job(conf, SentenceProcessorDriver.class.getSimpleName());
		job.setJarByClass(SentenceProcessorDriver.class);

		Query queryIn = createInputQuery();
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
	    		SentenceProcessorMapper.class, Text.class,
				IntWritable.class, job);
		job.setNumReduceTasks(0);
		
		job.setOutputFormatClass(NullOutputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);

		try {
			job.waitForCompletion(true);

		} catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
		} catch (ClassNotFoundException ex) {
			log.error(ex.getMessage(), ex);
		}

	}

	public static Query createInputQuery() {
		QPageParse query = QPageParse.newQuery();
		query.select(query.pageTitle())
		     .select(query.pageId())
		     .select(query.document().wildcard())
		     .select(query.document().sentence().wildcard()) // more...
		;
		return query.getModel();
	}
}
