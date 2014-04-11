package org.cloudgraph.examples.wikicorpus.index;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
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
 * hadoop jar /home/lib/wikicorpus-index-0.5.1.jar org.cloudgraph.examples.wikicorpus.index.ParsedWordDriver -libjars ${LIBJARS} 
 */
public class ParsedWordDriver {
	private static Log log = LogFactory.getLog(ParsedWordDriver.class);

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + "hadoop jar /home/lib/wikicorpus-index-0.5.1.jar " + 
	        ParsedWordDriver.class.getName() + " -libjars ${LIBJARS}");
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
		Job job = new Job(conf, ParsedWordDriver.class.getSimpleName());
		job.setJarByClass(ParsedWordDriver.class);

		Query queryIn = createInputQuery();
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
	    		ParsedWordMapper.class, Text.class,
				LongWritable.class, job);
		job.setNumReduceTasks(1);
		job.setReducerClass(ParsedWordReducer.class);
		
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

	/**
	 * Returns minimal page info with the document body which stores XML serialized
	 * dependencies. 
	 * @return the query
	 */
	public static Query createInputQuery() {
		QPageParse query = QPageParse.newQuery();
		query.select(query.pageId())
		     .select(query.xml())
		;
		
		return query.getModel();
	}
}
