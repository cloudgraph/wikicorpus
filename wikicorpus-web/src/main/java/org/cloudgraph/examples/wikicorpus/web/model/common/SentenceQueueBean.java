package org.cloudgraph.examples.wikicorpus.web.model.common;

// java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.parse.Node;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusService;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusServiceImpl;
import org.cloudgraph.examples.wikicorpus.web.model.queue.PaginatedQueueBean;
import org.primefaces.model.SortOrder;



/**
 */
@ManagedBean(name="SentenceQueueBean")
@SessionScoped
public class SentenceQueueBean extends PaginatedQueueBean {
    private static Log log =LogFactory.getLog(SentenceQueueBean.class);

    private static final long serialVersionUID = 1L;
    private transient CorpusService service;
    private boolean personContact = false;
    private String word;    
    private List<String> depTypes;    
    private String nodeType = "governor";    
    private int count;    
    protected Map<String, Node.PROPERTY> orderingMap = new HashMap<String, Node.PROPERTY>();
    
    public SentenceQueueBean() {
    	super.setPageSize(15);
    	this.service = new CorpusServiceImpl();
       	
    	for (Node.PROPERTY prop : Node.PROPERTY.values()) {
    		this.orderingMap.put(prop.name(), prop);
    	}
    }


	public String getWord() {
		return word;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public void setWord(String word) {
		this.word = word;
	}

	
	public List<String> getDepTypes() {
		return depTypes;
	}


	public void setDepTypes(List<String> depTypes) {
		this.depTypes = depTypes;
	}


	public void handleDepTypeChange() {
    	this.clear();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }
    
    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }

	@Override
	public List<QueueAdapter> findResults(int startRow, int endRow,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {

		Node.PROPERTY ordering = null;
		if (this.currentSortField != null)
		{
			ordering = this.orderingMap.get(this.currentSortField);
			if (ordering == null)
				log.error("no ordering foeld found for '"
					+ this.currentSortField + "' - ignoring");
		}
		boolean asc = sortOrder != null && sortOrder.ordinal() != sortOrder.DESCENDING.ordinal();
		if ("governor".equals(this.nodeType)) {
			log.info("findGovernors: ("+this.count+")" + " word: " + this.word + " type: " + this.depTypes
					+ " (" + startRow + "/" + endRow + ")");
	        return this.service.findGovernors(this.word, this.depTypes,
	    		startRow, endRow, asc);
		}
		else {
			log.info("findDependents: ("+this.count+")" + " word: " + this.word + " type: " + this.depTypes
					+ " (" + startRow + "/" + endRow + ")");
	        return this.service.findDependents(this.word, this.depTypes,
	    		startRow, endRow, asc);
		}
	}

	@Override
	public int countResults() {
		return this.count;
	}
     
    
	
}
