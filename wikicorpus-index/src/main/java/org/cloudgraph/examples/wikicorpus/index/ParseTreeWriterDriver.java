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
 * hadoop jar /home/lib/wikicorpus-index-0.5.1.jar org.cloudgraph.examples.wikicorpus.index.ParseTreeWriterDriver -Dunique.checks=false -libjars ${LIBJARS} 
 */
public class ParseTreeWriterDriver {
	private static Log log = LogFactory.getLog(ParseTreeWriterDriver.class);

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + "hadoop jar /home/lib/wikicorpus-index-0.5.1.jar " + 
	        ParseTreeWriterDriver.class.getName() + " -libjars ${LIBJARS}");
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
		
		// unique.checks does a Get before each row Put to determine if the row exists, such that
		// it won't be mistakenly overwritten
		conf.set("mapred.child.java.opts", "-Dunique.checks=false -Xms256m -Xmx2g -XX:+UseSerialGC -verbose:gc -Xloggc:/tmp/@taskid@-gc.log");
		conf.set("mapred.job.map.memory.mb", "4096");
		//conf.set("mapred.job.reduce.memory.mb", "1024");
		
		long milliSeconds = 1000*240*20; // 240 minutes rather than default 10 min
		conf.setLong("mapred.task.timeout", milliSeconds);		
		// The maximum number of attempts per map task. In other words, framework will try to execute a map task these many number of times before giving up on it.
		// Otherwise another task_attempt is kicked off after 10 min
		conf.setInt("mapred.map.max.attempts", 1); 
		
		
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
		
		Job job = new Job(conf, ParseTreeWriterDriver.class.getSimpleName());
		job.setJarByClass(ParseTreeWriterDriver.class);

		Query queryIn = createInputQuery();
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
	    		ParseTreeWriterMapper.class, Text.class,
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

	/**
	 * Returns minimal page info with the document body which stores XML serialized
	 * dependencies. 
	 * @return the query
	 */
	public static Query createInputQuery() {
		QPageParse query = QPageParse.newQuery();
		query.select(query.pageTitle())
		     .select(query.pageId())
		     //.select(query.xml())
		;
		
		return query.getModel();
	}
}
