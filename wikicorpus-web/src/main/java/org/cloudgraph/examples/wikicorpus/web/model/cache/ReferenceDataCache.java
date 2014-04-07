package org.cloudgraph.examples.wikicorpus.web.model.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.search.ParsedWordAggregate;
import org.cloudgraph.examples.corpus.search.query.QParsedWordAggregate;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

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
	private Map<String, ParsedWordAdapter> wordMap = new WeakHashMap<String, ParsedWordAdapter>();
  

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
		query.orderBy(query.lemma());
		query.setStartRange(0);
		query.setEndRange(40);
		
		List<String> result = new ArrayList<String>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			ParsedWordAggregate word = (ParsedWordAggregate)graph.getRootObject();
	    	result.add(word.getLemma());
	    	
	    	ParsedWordAdapter existing = this.wordMap.get(word.getLemma());
	    	if (existing == null) {
	    		this.wordMap.put(word.getLemma(), new ParsedWordAdapter(word));
	    	}
	    	else
	    		existing.setHitCount(existing.getHitCount() + 1);
		}		
		
		return result;
	}
	
	public ParsedWordAdapter getWord(String lemma) {
		ParsedWordAdapter existing = this.wordMap.get(lemma);
		if (existing != null)
			existing.setHitCount(existing.getHitCount() + 1);
		return existing;
	}
	
	public void incrementWord(String lemma) {
		ParsedWordAdapter existing = this.wordMap.get(lemma);
		if (existing != null)
			existing.setHitCount(existing.getHitCount() + 1);
	}
	
    private TagCloudModel model;  
    public TagCloudModel getWordTagCloudModel() {  
    	model = new DefaultTagCloudModel();
    	List<ParsedWordAdapter> words = new ArrayList<ParsedWordAdapter>();
    	words.addAll(this.wordMap.values());
    	Collections.sort(words, new Comparator<ParsedWordAdapter>(){
			@Override
			public int compare(ParsedWordAdapter o1, ParsedWordAdapter o2) {
				// TODO Auto-generated method stub
				return Integer.valueOf(o2.getHitCount()).compareTo(Integer.valueOf(o1.getHitCount()));
			}});
    	
    	int max = words.size()-1;
    	if (max > 20)
    		max = 20;
    	for (int i = 0; i < max; i++) {
    		ParsedWordAdapter adapter = words.get(i);
   	        model.addTag(new DefaultTagCloudItem(adapter.getLemma(), 
 	    		adapter.getHitCount()));  
    	} 
    	
        return model;  
    }  

	
}
