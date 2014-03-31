package org.cloudgraph.examples.wikicorpus.mapreduce;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.examples.corpus.wordnet.Words;
import org.cloudgraph.examples.corpus.wordnet.query.QWords;
import org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphWritable;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.plasma.query.model.Query;

import commonj.sdo.DataGraph;

/**
 */
public class WordGraphReader {
    private static Log log = LogFactory.getLog(WordGraphReader.class);

	/** Name of this 'program'. */
	static final String NAME = "graphcounter";

	/**
	 * Mapper that runs the count.
	 */
	static class WordGraphReaderMapper extends
	    GraphMapper<ImmutableBytesWritable, DataGraph> {

		private final IntWritable ONE = new IntWritable(1);
	   	private Text text = new Text();

	   	/** Counter enumeration to count the actual rows. */
		public static enum Counters {
			WORD_GRAPHS,
		}

		/**
		 * Maps the data.
		 * 
		 * @param row
		 *            The current table row key.
		 * @param values
		 *            The columns.
		 * @param context
		 *            The current context.
		 * @throws IOException
		 *             When something is broken with the data.
		 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,
		 *      org.apache.hadoop.mapreduce.Mapper.Context)
		 */
		@Override
		public void map(ImmutableBytesWritable row, GraphWritable graph,
				Context context) throws IOException {
			if (graph != null) {
				try {
					 Words word = (Words)graph.getDataGraph().getRootObject();
			         log.info(graph.toXMLString());
				}
				catch (Throwable t) {
					log.info(t);
				}
			}
			else {
				log.info("null graph !");
			}
    		
			context.getCounter(Counters.WORD_GRAPHS).increment(1);
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
		//String queryFileName = args[0];
		Query query = createQuery();        

		Job job = new Job(conf, NAME + "_" + "WORD_GRAPH_READER");
		job.setJarByClass(WordGraphReader.class);	
		
		//DistributedCache.addFileToClassPath(new Path("/myapp/mylib.jar"), 
		//		job.getConfiguration());
		
		job.setOutputFormatClass(NullOutputFormat.class);
	    GraphMapReduceSetup.setupGraphMapperJob(query,
				WordGraphReaderMapper.class, ImmutableBytesWritable.class,
				GraphWritable.class, job);
		job.setNumReduceTasks(0);
		return job;
	}
	
	private static Query createQuery() {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		    .select(query.casedwords().wildcard())
		    .select(query.senses().wildcard())
		    .select(query.senses().synsets().wildcard())
		    .select(query.senses().synsets().samples().wildcard())
		    .select(query.senses().synsets().adjpositions().wildcard())		    
		    // semantic relations
		    .select(query.senses().synsets().semlinks().wildcard())  
		    .select(query.senses().synsets().semlinks().linktypes().wildcard())  		    
		    .select(query.senses().synsets().semlinks().synsets().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().words().wildcard())  
		    // lexical relations
		    .select(query.senses().synsets().lexlinks().wildcard())  
		    .select(query.senses().synsets().lexlinks().linktypes().wildcard())
		    .select(query.senses().synsets().lexlinks().synsets().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().words().wildcard())  
		;
		//query.where(query.lemma().eq("absorb"));
		//query.where(query.lemma().eq("giraffe"));
		/*
		if (wordid > 0)
		    query.where(query.lemma().eq(lemma).and(query.wordid().eq(wordid)));
		else
			query.where(query.lemma().eq(lemma));
		*/
		return query.getModel();
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
		System.err.println("Usage: PageParser");
		System.err.println("For performance consider the following options:\n"
				+ "-Dhbase.client.scanner.caching=100\n"
				+ "-Dmapred.map.tasks.speculative.execution=false");
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
