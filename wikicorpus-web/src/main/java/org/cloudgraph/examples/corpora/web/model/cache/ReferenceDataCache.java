package org.cloudgraph.examples.wikicorpus.web.model.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.search.ParsedWordAggregate;
import org.cloudgraph.examples.corpus.search.query.QParsedWordAggregate;
import org.cloudgraph.examples.corpus.wordnet.Words;
import org.cloudgraph.examples.corpus.wordnet.query.QWords;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import commonj.sdo.DataGraph;


/**
 * An application level cache which just stores
 * reference/lookup data using JSF application level
 * managed bean. 
 */
@ManagedBean(name="ReferenceDataCache")
@ApplicationScoped
public class ReferenceDataCache 
    implements Serializable 
{
    private static Log log = LogFactory.getLog(ReferenceDataCache.class);
	private static final long serialVersionUID = 1L;
    protected SDODataAccessClient service;
	private Map<String, ParsedWordAggregate> wordMap = new WeakHashMap<String, ParsedWordAggregate>();
	private static int[] ranges = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 5000};
  

    /**
     * Only to support managed bean facility and test harnesses. NOT
     * for client code in general. 
     * Start a cache monitor Thread for expiration and eviction purposes.
     */
	public ReferenceDataCache() {
	  	this.service = new SDODataAccessClient(new HBasePojoDataAccessClient());
	}
	
	public List<String> words(String wildcard) {
		String wildcardStr = wildcard.toLowerCase() + "*";
		
		QParsedWordAggregate query = QParsedWordAggregate.newQuery();
		query.select(query.lemma());
		query.where(query.lemma().like(wildcardStr));
		query.setStartRange(0);
		query.setEndRange(24);
		
		List<String> result = new ArrayList<String>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			ParsedWordAggregate word = (ParsedWordAggregate)graph.getRootObject();
	    	result.add(word.getLemma());
	    	wordMap.put(word.getLemma(), word);
		}		
		
		return result;
	}
	
	public ParsedWordAggregate getWord(String lemma) {
		return this.wordMap.get(lemma);
	}
	
	Map<Integer, List<Long>> hbaseAssemblyTime = new HashMap<Integer, List<Long>>();
	public synchronized void addHBaseAssemblyTime(long nodeCount, long milliseconds) {
		for (int r : ranges) {
			if (nodeCount < r) {
				List<Long> values = hbaseAssemblyTime.get(r);
				if (values == null) {
					Integer range = new Integer(r);
					values = new ArrayList<Long>();
					hbaseAssemblyTime.put(range, values);
				}
				values.add(milliseconds);
				break;
			}
		}
	}
	
	Map<Integer, List<Long>> rdbmsAssemblyTime = new HashMap<Integer, List<Long>>();
	public synchronized void addRdbmsAssemblyTime(long nodeCount, long milliseconds) {
		for (int r : ranges) {
			if (nodeCount < r) {
				List<Long> values = rdbmsAssemblyTime.get(r);
				if (values == null) {
					Integer range = new Integer(r);
					values = new ArrayList<Long>();
					rdbmsAssemblyTime.put(range, values);
				}
				values.add(milliseconds);
				break;
			}
		}
	}
	public synchronized  CartesianChartModel getHbaseCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries hbase = new ChartSeries();  
        hbase.setLabel("HBase");  
		for (int r : ranges) {
			List<Long> values = hbaseAssemblyTime.get(r);
			if (values == null)
				continue;
			long total = 0;
			for (Long value : values) {
				total += value.longValue();
			}
			 
			long average = total / values.size();
			hbase.set(String.valueOf(r), average);
		}  
        categoryModel.addSeries(hbase);  
        
        return categoryModel;
    }	
	
	public synchronized  CartesianChartModel getRdbmsCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries mysql = new ChartSeries();  
        mysql.setLabel("MySql");  
		for (int r : ranges) {
			List<Long> values = rdbmsAssemblyTime.get(r);
			if (values == null)
				continue;
			long total = 0;
			for (Long value : values) {
				total += value.longValue();
			}
			 
			long average = total / values.size();
			mysql.set(String.valueOf(r), average);
		}  
  
  
        categoryModel.addSeries(mysql);  
        
        return categoryModel;
    }	

	public synchronized  CartesianChartModel getCombinedCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries hbase = new ChartSeries();  
        hbase.setLabel("HBase");  
		for (int r : ranges) {
			List<Long> values = hbaseAssemblyTime.get(r);
			if (values == null)
				continue;
			long total = 0;
			for (Long value : values) {
				total += value.longValue();
			}
			 
			long average = total / values.size();
			hbase.set(String.valueOf(r), average);
		}  
  
        ChartSeries mysql = new ChartSeries();  
        mysql.setLabel("MySql");  
		for (int r : ranges) {
			List<Long> values = rdbmsAssemblyTime.get(r);
			if (values == null)
				continue;
			long total = 0;
			for (Long value : values) {
				total += value.longValue();
			}
			 
			long average = total / values.size();
			mysql.set(String.valueOf(r), average);
		}  
  
  
        categoryModel.addSeries(hbase);  
        categoryModel.addSeries(mysql);  
        
        return categoryModel;
    }	
}
