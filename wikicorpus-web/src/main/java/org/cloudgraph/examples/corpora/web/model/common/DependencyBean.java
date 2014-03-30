package org.cloudgraph.examples.wikicorpus.web.model.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusService;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusServiceImpl;
import org.cloudgraph.examples.wikicorpus.service.corpus.Dependency;
import org.cloudgraph.examples.wikicorpus.service.wordnet.WordRelations;
import org.cloudgraph.examples.wikicorpus.service.wordnet.Wordnet;
import org.cloudgraph.examples.wikicorpus.service.wordnet.WordnetService;
import org.cloudgraph.examples.wikicorpus.service.wordnet.WordnetServiceImpl;
import org.cloudgraph.examples.wikicorpus.web.model.ModelBean;
import org.cloudgraph.examples.wikicorpus.web.model.cache.ReferenceDataCache;
import org.cloudgraph.examples.wikicorpus.web.model.cache.WordAdapter;
import org.cloudgraph.examples.corpus.search.WordDependency;
import org.cloudgraph.examples.corpus.wordnet.Words;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import com.google.common.math.IntMath;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

@ManagedBean(name="DependencyBean")
@SessionScoped
public class DependencyBean extends ModelBean {
 	private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(DependencyBean.class);
    private String word;
    protected CorpusService hbase;
    private ReferenceDataCache cache;
    private static List<Dependency> EMPTY_DEP_LIST = new ArrayList<Dependency>();
    private int tabIndex;
    
    public DependencyBean() {
	  	this.hbase = new CorpusServiceImpl();
	  	//this.rdbms = new WordnetServiceImpl(DataAccessProviderName.JDBC);
	  	this.cache = this.beanFinder.findReferenceDataCache();
    }
        
    public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	public String getWord() {
		return this.word;
	}
	
	public void setWord(String value) {
		 
		if (this.word != null && value != null /*&& !this.word.equals(value)*/) 
			handleWordChange();
	    this.word = value.toLowerCase();
	}
    
	public boolean getHasWord() {
		return this.word != null;
	}
	
    public String getDisplayWord() {
    	StringBuilder buf = new StringBuilder();
    	String[] tokens = this.word.split("\\s+");
    	for (String token : tokens) {
    		buf.append(token.substring(0,1).toUpperCase());
    		buf.append(token.substring(1));
    		buf.append(" ");
    	}
		return buf.toString();
	}
    
	public void handleWordChange(SelectEvent event) {  
        try {   
        	handleWordChange();
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
 	        FacesMessage msg = new FacesMessage("Internal Error");  	       
	        FacesContext.getCurrentInstance().addMessage(null, msg);          	
        } finally {
        }       		
	}
	
	private void handleWordChange() {
		try {
			this.dependencies = null;
			this.categoryModel = null;
			this.level = 0;
			this.depIndex = -1;
			this.seriesIndex = -1;
			refreshCategoryModel(); 
			setupSentences();
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
	
	private void setupSentences()
	{
		SentenceQueueBean sentences = this.beanFinder.findSentenceQueueBean();
		sentences.setWord(this.word);
		sentences.setCount((int)this.grandTotalCount); // FIXME
		if (this.selectedRoot != null)
			sentences.setDepType(this.selectedRoot.getDepTypeName());
		if (level == 0) {
		}
		else { 		     					
			if (seriesIndex == 0) {
				sentences.setNodeType("governor");
	 		}
	 		else if (seriesIndex == 1) {
				sentences.setNodeType("dependent");
	 		}
		}
		
		sentences.clear();
		
	}
	
	public List<Dependency> getDependencies() {
		return getDependencies(this.hbase);
	}
		
	private List<Dependency> getDependencies(CorpusService service) {
				
		if (this.word == null)
			return EMPTY_DEP_LIST;
		
		
		try  {
			//if (dependencies.size() == 0)			
			    dependencies = service.findDependencies(this.word);
        	/*
            Words wd = null;//this.cache.getWord(this.word);
            if (wd != null) {            	
            	result = service.findDependencies(wd.getLemma());
            }
            else {
            	result = service.findDependencies(this.word);
            }
            if (result == null || result.size() == 0) {
    	        FacesMessage msg = new FacesMessage("No results found for '" + this.word + "'");  	       
    	        FacesContext.getCurrentInstance().addMessage(null, msg);  
    	        return EMPTY_RELATION_LIST;
            }
            else {
            	return result;
            }
            */
			
			return dependencies;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return EMPTY_DEP_LIST;
	}

	
	private int level = 0;
	
	public int getLevel() {
		return level;
	}


	private int seriesIndex = -1;
	private int depIndex = -1;
	
	public void handleDependencySelect(ItemSelectEvent event) {  		
		log.info("series: " + event.getSeriesIndex());
		log.info("item: " + event.getItemIndex());
		this.seriesIndex = event.getSeriesIndex();
		this.depIndex = event.getItemIndex();
 	    Dependency nextRoot = dependencies.get(depIndex);
 	    if (nextRoot.getDependencies().size() > 0) {
 	        this.currentRoot = nextRoot; 	    
 	        this.dependencies = this.currentRoot.getDependencies();
 		    Collections.sort(this.dependencies, Dependency.createComparator());
 			this.level++;
 			this.categoryModel = null;
 			refreshCategoryModel();
	    }
 	    this.selectedRoot = nextRoot;
 	    setupSentences();
	}  
	
	public void back(ActionEvent event) {
		this.level--;
		if (level == 0) {
			this.seriesIndex = -1;
			this.depIndex = -1;
		}
		this.currentRoot = this.currentRoot.getParent();
		this.selectedRoot = null;
		this.dependencies = this.currentRoot.getDependencies();
 		Collections.sort(this.dependencies, Dependency.createComparator());
		this.categoryModel = null;
 		refreshCategoryModel(); 
 		setupSentences();
	}
	 	
	private long grandTotalCount;
	private double maxCount;
	public int getMaxAggregate() {
		return Double.valueOf(this.maxCount).intValue();
	}
	
	public int getDependencyCount() {
		if (this.dependencies != null)
		   return this.dependencies.size();
		return 0;			
	}
	
	private void refreshCategoryModel() {
		this.categoryModel = new CartesianChartModel();
		this.grandTotalCount = 0;
    	try {
    		if (dependencies == null) {
	     	    dependencies = getDependencies();		     	   
	     	    Collections.sort(dependencies, Dependency.createComparator());	
     		    if (dependencies.size() > 0)
     		        this.currentRoot = dependencies.get(0).getParent();
    		}
	     	
	     	if (level == 0) {
		     	log.info("creating " + dependencies.size() + " deps");
		     	ChartSeries governor = createGovernorSeries(dependencies);  
		        categoryModel.addSeries(governor);  
		    	
		        ChartSeries dependent = createDependentSeries(dependencies); 
		        categoryModel.addSeries(dependent); 
	     	}
	     	else {
	     		if (seriesIndex == 0) {
	     			ChartSeries governor = createGovernorSeries(dependencies);    
			        categoryModel.addSeries(governor);  
	     		}
	     		else if (seriesIndex == 1) {
			        ChartSeries dependent = createDependentSeries(dependencies); 
			        categoryModel.addSeries(dependent); 		     			
	     		}
	     	}
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
	}
	
	private CartesianChartModel categoryModel = new CartesianChartModel();  
	private List<Dependency> dependencies = null;
	private Dependency currentRoot = null;
	private Dependency selectedRoot = null;
	
	public boolean getHasSelectedRoot() {
		return selectedRoot != null;
	}
	
	public Dependency getSelectedRoot() {
		return selectedRoot;
	}

	public CartesianChartModel getDependencyCategoryModel() {  
        return categoryModel;
    }
	
	public int getSeriesCount() {
		if (categoryModel != null)
			return categoryModel.getSeries().size();
		else
			return 0;
	}
	
	public String getSeriesColors() {
		if (level == 0) {
			return "e9a32c, 49b3c4";
		}
		else { 		     		
			if (seriesIndex == 0) {
				return "e9a32c";
	 		}
	 		else if (seriesIndex == 1) {
				return "49b3c4";
	 		}
		}
		return null;
	}
	
	private ChartSeries createGovernorSeries(List<Dependency> deps) {
     	ChartSeries governor = new ChartSeries();  
        governor.setLabel("Governor");  
    	for (Dependency dep : deps) {
    		this.grandTotalCount += dep.getGovernorCountDeep();
    		//log.info(dep.getDisplayType() + ": " + dep.getGovernorCountDeep() + "/" + dep.getDependentCountDeep());
    		double logValue = 0;
    		if (dep.getGovernorCountDeep() > 0) 
		        logValue = Math.log((double)dep.getGovernorCountDeep());
		    governor.set(dep.getDisplayType(), logValue);
		    if (logValue > maxCount)
			    maxCount = logValue;    		 
    	}
    	return governor;
	}
 	
	private ChartSeries createDependentSeries(List<Dependency> deps) {
        ChartSeries dependent = new ChartSeries();        
        dependent.setLabel("Dependent");  
    	for (Dependency dep : deps) {
    		this.grandTotalCount += dep.getDependentCountDeep();
    		double logValue = 0;
    		if (dep.getDependentCountDeep() > 0) 
    		    logValue = Math.log((double)dep.getDependentCountDeep());
		    dependent.set(dep.getDisplayType(), logValue);
		    if (logValue > maxCount)
			    maxCount = logValue;
    	}
		return dependent;
	}
	
	protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("test");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
}
