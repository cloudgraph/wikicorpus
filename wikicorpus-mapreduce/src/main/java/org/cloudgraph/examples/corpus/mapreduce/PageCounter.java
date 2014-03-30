package org.cloudgraph.examples.corpus.mapreduce;

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
 */
public class PageCounter {
    private static Log log = LogFactory.getLog(PageCounter.class);

	/** Name of this 'program'. */
	static final String NAME = PageCounter.class.getSimpleName();

	/**
	 * Mapper that runs the count.
	 */
	static class CategoryCounterMapper extends
	    GraphMapper<Text, IntWritable> {

		private final IntWritable ONE = new IntWritable(1);

	   	/** Counter enumeration to count the actual rows. */
		public static enum Counters {
			PAGES,
		}

		@Override
		public void map(ImmutableBytesWritable row, GraphWritable graph,
				Context context) throws IOException {
			try {
				 // track changes
				 //graph.getDataGraph().getChangeSummary().beginLogging();
				
		         Page page = (Page)graph.getDataGraph().getRootObject();
		         log.info("GRAPH: " + graph.toXMLString());
		         log.info(page.getPageTitle());
		         log.info(page.getCategorylinksCount());
		         for (Categorylinks link : page.getCategorylinks()) {
		        	 //Text text = new Text(link.getClTo());
		        	 //context.write(text, ONE);
		         }
		         
		         //Categorylinks link = page.createCategorylinks();
		         //link.setClTo("DUMMY TEST LINK3");
		         //link.setClTimestamp((new Date()).toString());

		         //commit(graph, context);
		         
			}
			catch (Throwable t) {
				log.info(t);
			}
			context.getCounter(Counters.PAGES).increment(1);
		}
	}
	
	/**
	 * Mapper that runs the count.
	 */
	static class CategoryCounterReducer extends GraphReducer<Text, IntWritable, Text> {
	 	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			try {
				log.info("Reducer: HERE1");
				
    		int count = 0;
    		for (IntWritable val : values) {
    			count += val.get();
    		}
    		//Put put = new Put(Bytes.toBytes(key.toString()));
    		//put.add(CF, COUNT, Bytes.toBytes(i));

    		//context.write(null, put);
    		
    		String title = key.toString();
    		
    		CategoryTitle cat = findOrCreateCat(title, context);
    		cat.getCategory().setCatPages(count);
    		log.info("comitting: " + title + " ("+count+")");
    		this.commit(cat.getDataGraph(), context);
			}
			catch (Throwable t) {
				log.info("Reducer: "+ t.getMessage());
				log.info(t.getMessage(), t);
			}    		
   	    }
	 	
	 	private static Random rand = new Random();
		private CategoryTitle findOrCreateCat(String title, Context context) throws IOException {
			CategoryTitle result;
			
			Query queryOut = createOutputQuery(title);
			DataGraph[] results = this.find(queryOut, context);
			if (results != null && results.length > 0) {
				log.info("found existing graph for: " + title);
				result = (CategoryTitle)results[0].getRootObject();
				result.getDataGraph().getChangeSummary().beginLogging();
			}
			else {
				log.info("creating new graph for: " + title);
				PlasmaDataGraph graph = PlasmaDataFactory.INSTANCE.createDataGraph();
				graph.getChangeSummary().beginLogging();
				Type rootType = PlasmaTypeHelper.INSTANCE.getType(CategoryTitle.class);
				result = (CategoryTitle)graph.createRootObject(rootType);
				Category cat = result.createCategory();
				cat.setCatTitle(title);
				cat.setCatId(Math.abs(rand.nextInt()));
			}	
			return result;
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
		Query queryIn = createInputQuery();        

		Job job = new Job(conf, NAME + "_" + "WIKI_PAGE_AGGERGATOR");
		job.setJarByClass(PageCounter.class);	
		
		//DistributedCache.addFileToClassPath(new Path("/myapp/mylib.jar"), 
		//		job.getConfiguration());
		
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
				CategoryCounterMapper.class, Text.class,
				IntWritable.class, job);
		job.setNumReduceTasks(1);
		
		Query queryOut = createOutputQuery(null);
		
		job.setOutputFormatClass(NullOutputFormat.class);
		//GraphMapReduceSetup.setupGraphReducerJob(queryOut, 
		//		CategoryCounterReducer.class, job);
		
		job.setMapperClass(CategoryCounterMapper.class);
		job.setReducerClass(CategoryCounterReducer.class);
		
		return job;
	}
	
	public static Query createOutputQuery(String title) {
		QCategoryTitle query = QCategoryTitle.newQuery();
		query.select(query.wildcard())
		     .select(query.category().wildcard())
		;
		if (title != null)
		    query.where(query.category().catTitle().eq("#MYVAR"));
		return query.getModel();
	}	
	
	public static Query createInputQuery() {
		QPage query = QPage.newQuery();
		query.select(query.pageTitle())
		     .select(query.pageId())
		     .select(query.categorylinks().wildcard());
		
		
		//query.where(query.categorylinks().clTo().like("*Japan*")); // breaks
		//query.where(query.pageTitle().like("*") 
		//		.and(query.categorylinks().clTo().like("*Japan*")));
		query.where(query.pageTitle().like("L*")); 
				//.and(query.categorylinks().clTo().like("*Japan*")));
		
		//query.where(query.pageTitle().like("L*") 
		///		.and(query.categorylinks().clTo().eq("DUMMY TEST LINK2")));
		
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
