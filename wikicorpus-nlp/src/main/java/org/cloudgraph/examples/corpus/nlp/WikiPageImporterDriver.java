package org.cloudgraph.examples.corpus.nlp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.examples.corpus.common.XmlInputFormat;
import org.cloudgraph.hbase.service.CloudGraphContext;

/**
 * hadoop jar /home/lib/wikicorpus-nlp-0.5.1.jar -libjars ${LIBJARS} /tmp/wikixml /usr/root/tmp
*/
public class WikiPageImporterDriver {

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + WikiPageImporterDriver.class.getSimpleName());
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
		
		long milliSeconds = 1000*60*60; // 60 minutes rather than default 10 min
		conf.setLong("mapred.task.timeout", milliSeconds);

		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			printUsage("Wrong number of parameters: " + args.length);
			System.exit(-1);
		}
		try {
			runJob(conf, otherArgs[0], otherArgs[1]);

		} catch (IOException ex) {
			Logger.getLogger(WikiPageImporterDriver.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public static void runJob(Configuration conf, String input, String output) throws IOException {

		conf.set(XmlInputFormat.START_TAG_KEY, "<page>");
		conf.set(XmlInputFormat.END_TAG_KEY, "</page>");
		conf.set(
				"io.serializations",
				"org.apache.hadoop.io.serializer.JavaSerialization,org.apache.hadoop.io.serializer.WritableSerialization");

		Job job = new Job(conf, WikiPageImporterDriver.class.getSimpleName());

		FileInputFormat.setInputPaths(job, input);
		job.setJarByClass(WikiPageImporterDriver.class);
		job.setMapperClass(WikiPageImporterMapper.class);
		job.getConfiguration().setLong("mapred.max.split.size", 1000000); // bytes rather than default 64M
		job.setNumReduceTasks(0);
		job.setInputFormatClass(XmlInputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		Path outPath = new Path(output);
		FileOutputFormat.setOutputPath(job, outPath);
		FileSystem dfs = FileSystem.get(outPath.toUri(), conf);
		if (dfs.exists(outPath)) {
			dfs.delete(outPath, true); // watch it this works
		}

		try {

			job.waitForCompletion(true);

		} catch (InterruptedException ex) {
			Logger.getLogger(WikiPageImporterDriver.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(WikiPageImporterDriver.class.getName()).log(
					Level.SEVERE, null, ex);
		}

	}

}
