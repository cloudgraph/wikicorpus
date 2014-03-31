package org.cloudgraph.examples.wikicorpus.mapreduce;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.examples.corpus.search.CategoryTitle;
import org.cloudgraph.examples.corpus.search.query.QCategoryTitle;
import org.cloudgraph.examples.corpus.search.query.QPageTitle;
import org.cloudgraph.examples.corpus.wiki.Category;
import org.cloudgraph.examples.corpus.wiki.Categorylinks;
import org.cloudgraph.examples.corpus.wiki.Page;
import org.cloudgraph.examples.corpus.wiki.query.QPage;
import org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphReducer;
import org.cloudgraph.hbase.mapreduce.GraphWritable;
import org.cloudgraph.hbase.mapreduce.GraphXmlInputFormat;
import org.cloudgraph.hbase.mapreduce.GraphXmlMapper;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.cloudgraph.test.datatypes.StringNode;
import org.plasma.config.DataAccessProviderName;
import org.plasma.query.model.Query;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.access.DataAccessService;
import org.plasma.sdo.access.client.DataAccessClient;
import org.plasma.sdo.access.client.PojoDataAccessClient;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * hadoop jar /home/lib/wikicorpus-mapreduce-0.5.1.jar org.cloudgraph.examples.corpus.mapreduce.CategoryImporter -libjars ${LIBJARS} -Dmapred.input.dir=/tmp/wiki
 */
public class CategoryImporter {
    private static Log log = LogFactory.getLog(CategoryImporter.class);

	/** Name of this 'program'. */
	static final String NAME = CategoryImporter.class.getSimpleName();

	/**
	 * Mapper  
	 */
	static class CategoryImporterMapper extends
	    GraphXmlMapper<Text, IntWritable> {

		private final IntWritable ONE = new IntWritable(1);

	   	/** Counter enumeration to count the actual rows. */
		public static enum Counters {
			CATS,
		}

		@Override
		public void map(LongWritable row, GraphWritable graph,
				Context context) throws IOException {
			//try {
				 // track changes
				 //graph.getDataGraph().getChangeSummary().beginLogging();
				
		         Category cat = (Category)graph.getDataGraph().getRootObject();
		         log.info("commit: " + graph.toXMLString());
		         try {
		                //super.commit(graph.getDataGraph(), context);
					   context.getCounter(Counters.CATS).increment(1);
		         }
			     catch (Throwable t) {
			    	 log.info("commit failed");
					log.info(t);
			     }
		         
			//}
			//catch (Throwable t) {
			//	log.info(t);
			//}
		}
	}
	
	
	/**
	 * Sets up the actual job.
	 * 
	 * @param conf
	 *            The current configuration.
	 * @param args
	 *            The command line parameters.
	 * @return The newly created job.
	 * @throws IOException
	 *             When setting up the job fails.
	 */
	public static Job createSubmittableJob(Configuration conf, String[] args)
			throws IOException {

		Job job = new Job(conf, NAME);
		job.setJarByClass(CategoryImporter.class);	
		
		//DistributedCache.addFileToClassPath(new Path("/myapp/mylib.jar"), 
		//		job.getConfiguration());
				
		job.setNumReduceTasks(0);
		job.getConfiguration().set(GraphXmlInputFormat.ROOT_ELEM_NAMESPACE_URI, "http://cloudgraph.org/examples/corpus/wiki");
		job.getConfiguration().set(GraphXmlInputFormat.ROOT_ELEM_NAMESPACE_PREFIX, "c");
		
		job.setInputFormatClass(GraphXmlInputFormat.class);
		job.setOutputFormatClass(NullOutputFormat.class);
		
		job.setMapperClass(CategoryImporterMapper.class);
		
		return job;
	}
	
		
	/*
	 * @param errorMessage Can attach a message when error occurs.
	 */
	private static void printUsage(String errorMessage) {
		System.err.println("ERROR: " + errorMessage);
		printUsage();
	}

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + CategoryImporter.class.getSimpleName());
	}

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *            The command line parameters.
	 * @throws Exception
	 *             When running the job fails.
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = CloudGraphContext.instance().getConfig();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 0) {
			printUsage("Wrong number of parameters: " + args.length);
			System.exit(-1);
		}
		Job job = createSubmittableJob(conf, otherArgs);
		if (job == null) {
			System.exit(-1);
		}
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
