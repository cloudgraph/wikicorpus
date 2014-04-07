package org.cloudgraph.examples.wikicorpus.web.model.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusService;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusServiceImpl;
import org.cloudgraph.examples.wikicorpus.service.corpus.Dependency;
import org.cloudgraph.examples.wikicorpus.web.model.ModelBean;
import org.cloudgraph.examples.wikicorpus.web.model.cache.ReferenceDataCache;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.tagcloud.TagCloudItem;

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
	private int seriesIndex = -1;
	private int depIndex = -1;
	private long grandTotalGovernorCount;
	private long grandTotalDependentCount;
	private double maxCount;
	private CartesianChartModel categoryModel = new CartesianChartModel();  
	private List<Dependency> dependencies = null;
	private Dependency currentRoot = null;
	private Dependency selectedRoot = null;
    
    public DependencyBean() {
	  	this.hbase = new CorpusServiceImpl();
	  	//this.rdbms = new WordnetServiceImpl(DataAccessProviderName.JDBC);
	  	this.cache = this.beanFinder.findReferenceDataCache();
    }
        
 	public String getWord() {
		return this.word;
	}
	
	public void setWord(String value) {		 
	    this.word = value.toLowerCase();
	    this.cache.incrementWord(this.word);
		clearAll();
		refreshAll();
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
        	clearAll();
        	refreshAll();
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
 	        FacesMessage msg = new FacesMessage("Internal Error");  	       
	        FacesContext.getCurrentInstance().addMessage(null, msg);          	
        } finally {
        }       		
	}
	
    public void handleTagCloudWordSelect(SelectEvent event) {  
        TagCloudItem item = (TagCloudItem) event.getObject();  
    	setWord(item.getLabel());
    }  
	
	private void clearAll() {
		this.dependencies = null;
		this.categoryModel = null;
		this.level = 0;
		this.depIndex = -1;
		this.seriesIndex = -1;
		this.selectedRoot = null;
	}
	
	private void refreshAll() {
		try {
			refreshCategoryModel(); 
			setupSentences();
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
	
	public List<Dependency> getDependencies() {
		return getDependencies(this.hbase);
	}
		
	private List<Dependency> getDependencies(CorpusService service) {
				
		if (this.word == null)
			return EMPTY_DEP_LIST;
		
		
		try  {
			if (this.dependencies == null || this.dependencies.size() == 0)			
				this.dependencies = service.findDependencies(this.word);
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

	public String getSelectedRootDisplayName() {
		if (this.selectedRoot != null) {
			if (this.selectedRoot.getParent() != null && !"root".equals(this.selectedRoot.getParent().getDepTypeName())) {
				return this.selectedRoot.getParent().getDisplayType() + " / " + this.selectedRoot.getDisplayType();
			}
			else {
				return this.selectedRoot.getDisplayType();
			}
		}
		return "";
	}

	
	public void handleDependencySelect(ItemSelectEvent event) {  		
		log.info("series: " + event.getSeriesIndex());
		log.info("item: " + event.getItemIndex());
		if (level == 0) // only have 2 series at level 0
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
	
	private void setupSentences()
	{
		try {
		SentenceQueueBean sentences = this.beanFinder.findSentenceQueueBean();
		sentences.setWord(this.word);
		if (this.selectedRoot == null) {
			sentences.setDepTypes(null);
			 
			if (this.tabIndex == 0) {			    
				sentences.setCount((int)(this.grandTotalGovernorCount + this.grandTotalDependentCount)); // FIXME
			}
			else {
				if ("Y".equals(this.displayGovernors)) {
					sentences.setCount((int)this.grandTotalGovernorCount);  
					sentences.setNodeType("governor");
				}
				else {
					sentences.setCount((int)this.grandTotalDependentCount);  
					sentences.setNodeType("dependent");
				}				
		    }
		     
		}
		else { 	
			List<String> depTypes = new ArrayList<String>();
			if (this.selectedRoot.getDependencies().size() > 0) {
				for (Dependency dep : this.selectedRoot.getDependencies())
					depTypes.add(dep.getDepTypeName());
		    }
		    else
		    	depTypes.add(this.selectedRoot.getDepTypeName());
			sentences.setDepTypes(depTypes);
		    	
			if (seriesIndex == -1) {
				if ("Y".equals(this.displayGovernors)) {
					sentences.setCount(this.selectedRoot.getGovernorCountDeep());  
					sentences.setNodeType("governor");
				}
				else {
					sentences.setCount(this.selectedRoot.getDependentCountDeep());  
					sentences.setNodeType("dependent");
				}
			}
			else if (seriesIndex == 0) {
				sentences.setCount(this.selectedRoot.getGovernorCountDeep());  
				sentences.setNodeType("governor");
	 		}
	 		else if (seriesIndex == 1) {
				sentences.setCount(this.selectedRoot.getDependentCountDeep());  
				sentences.setNodeType("dependent");
	 		}
		}
		
		sentences.clear();
		
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		
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
		this.grandTotalGovernorCount = 0;
		this.grandTotalDependentCount = 0;
		this.maxCount = 0;
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
	
	
	public boolean getHasSelectedRoot() {
		return selectedRoot != null;
	}
	
	public Dependency getSelectedRoot() {
		return selectedRoot;
	}
	
	public void setSelectedRoot(Dependency selectedRoot) {
		this.selectedRoot = selectedRoot;
 		refreshCategoryModel(); 
 		setupSentences();
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

	private String displayGovernors = "Y";
	
	public String getDisplayGovernors() {
		return displayGovernors;
	}

	public void setDisplayGovernors(String displayGovernors) {
		this.displayGovernors = displayGovernors;
		this.refreshCategoryModel();
		this.setupSentences();
	}

	private String logarithmicDisplay = "Y";
			
	public String getLogarithmicDisplay() {
		return logarithmicDisplay;
	}

	public void setLogarithmicDisplay(String logarithmicDisplay) {
		this.logarithmicDisplay = logarithmicDisplay;
		refreshCategoryModel();
	}

	private ChartSeries createGovernorSeries(List<Dependency> deps) {
     	ChartSeries governor = new ChartSeries();  
        governor.setLabel("Governor");  
    	for (Dependency dep : deps) {
    		long count = dep.getGovernorCountDeep();
    		this.grandTotalGovernorCount += count;    		
    		if ("Y".equals(this.logarithmicDisplay)) {
	    		double logValue = 0;
	    		if (count > 0) 
			        logValue = Math.log((double)count);
			    governor.set(dep.getDisplayType(), logValue);
			    if (logValue > maxCount)
				    maxCount = logValue;    
    		}
    		else {
			    governor.set(dep.getDisplayType(), count);
			    if (count > maxCount)
				    maxCount = count;    
    		}
    	}
    	return governor;
	}
 	
	private ChartSeries createDependentSeries(List<Dependency> deps) {
        ChartSeries dependent = new ChartSeries();        
        dependent.setLabel("Dependent");  
    	for (Dependency dep : deps) {
    		long count = dep.getDependentCountDeep();
    		this.grandTotalDependentCount += count;
    		if ("Y".equals(this.logarithmicDisplay)) {
	    		double logValue = 0;
	    		if (count > 0) 
	    		    logValue = Math.log((double)count);
			    dependent.set(dep.getDisplayType(), logValue);
			    if (logValue > maxCount)
				    maxCount = logValue;
    		}
    		else {
    			dependent.set(dep.getDisplayType(), count);
			    if (count > maxCount)
				    maxCount = count;    
    		}
    	}
		return dependent;
	}
	
	
	private int tabIndex = 0;
    public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}	
	
    public void onTabChange(TabChangeEvent event) {  
    	if ("Chart".equalsIgnoreCase(event.getTab().getTitle()))
    		this.tabIndex = 0;
    	else if ("Menu".equalsIgnoreCase(event.getTab().getTitle()))
    		this.tabIndex = 1;
    	else
    		log.warn("WTF? " + event.getTab().getTitle());
    	
    	clearAll();
    	refreshAll();
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
