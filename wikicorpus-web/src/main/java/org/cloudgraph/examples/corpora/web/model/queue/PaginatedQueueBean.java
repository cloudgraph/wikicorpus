package org.cloudgraph.examples.wikicorpus.web.model.queue;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.cloudgraph.examples.wikicorpus.service.Queueable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;


/**
 * Common bean for lazy paginated tables and grids under PrimeFaces data model
 * extensions. 
 */
public abstract class PaginatedQueueBean extends LazyDataModel<QueueAdapter> {
    private static Log log =LogFactory.getLog(PaginatedQueueBean.class);

    private static final long serialVersionUID = 1L;
    private Map<Integer, QueueAdapter> rowMap = new HashMap<Integer, QueueAdapter>();
    protected String currentSortField;
    protected SortOrder currentSortOrder;
    protected Integer rowCount; // better to buffer row count locally
    
    public PaginatedQueueBean() {
    	super.setPageSize(15);
    }
    
    public LazyDataModel<QueueAdapter> getModel() {
    	return this;
    }    
		
	public void clear() {
		@SuppressWarnings("unchecked")
		List<Queueable> data = (List<Queueable>)this.getWrappedData();
		if (data != null)
    	    data.clear();
    	this.setWrappedData(null);
    	this.rowMap.clear();
    	this.setRowIndex(-1);
    	this.rowCount = null;
    }
    
    public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }
    
    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }

    public int getMaxRows() {
		return super.getPageSize();
	}
    
    public abstract List<QueueAdapter> findResults(int startRow, int endRow, 
    		String sortField, SortOrder sortOrder,
    		Map<String, String> filters);
    public abstract int countResults();
	
	@Override
	public QueueAdapter getRowData(String rowKey) {
		Integer rowIndex = Integer.parseInt(rowKey);
		@SuppressWarnings("unchecked")
		List<QueueAdapter> data = (List<QueueAdapter>)this.getWrappedData();
		//FIXME: use row index
		for (QueueAdapter row : data) {
			if (row.getIndex().equals(rowIndex))
				return row;
		}

		return null;
	}

	@Override
	public Object getRowKey(QueueAdapter row) {
		return row.getIndex();
	}

	@Override
	public List<QueueAdapter> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
		
		
		if (this.currentSortField == null) {
			if (sortField != null) {
				this.currentSortField = sortField;
				this.clear(); // detected sort field change
			}
		}
		else {
			if (sortField != null) {
				if (!sortField.equals(this.currentSortField)) {
				    this.currentSortField = sortField;
				    this.clear(); // detected sort field change
				}
				//else no change
			}
		    else {
			    this.currentSortField = null;
			    this.clear(); // detected sort field change
		    }
		}
		
		if (this.currentSortOrder == null) {
			if (sortOrder != null && SortOrder.UNSORTED.ordinal() != sortOrder.ordinal()) {
				 this.currentSortOrder = sortOrder;
				 this.clear(); // detected sort order change
			}
		}
		else {
			if (sortOrder != null && SortOrder.UNSORTED.ordinal() != sortOrder.ordinal()) {
				if (!sortOrder.equals(this.currentSortOrder)) {
				    this.currentSortOrder = sortOrder;
				    this.clear(); // detected sort field change
				}
				//else no change
			}
		    else {
			    this.currentSortOrder = null;
			    this.clear(); // detected sort field change
		    }			
		}
		
		List<QueueAdapter> results = new ArrayList<QueueAdapter>();;
		boolean alreadyRead = true;
		QueueAdapter row = null;
		for (int i = first; i < first + pageSize; i++)
		{
			row = this.rowMap.get(new Integer(i));
			if (row == null)
			{
				alreadyRead = false;
				break;
			}
			else
				results.add(row);
		}
		
		if (alreadyRead)
		{
			log.debug("rows " + first + " thru " + first + pageSize + " found in cache");
		}
		else {		
			try {
				results = findResults(
		    		first + 1, first + pageSize,
		    		this.currentSortField, sortOrder, filters);
				
		    	for (int i = 0; i < results.size(); i++) {
		    		QueueAdapter adapter = results.get(i);
					adapter.setIndex(i+first);
					this.rowMap.put(adapter.getIndex(), adapter);
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }	
		}
    	super.setWrappedData(results);
    	super.setRowCount(getRowCount());
    	super.setRowIndex(-1);
        return results;
	}
  
	
	@Override
    public int getRowCount() {
        if (rowCount==null) {
	    	this.rowCount = countResults();	    	
        	log.debug("getRowCount DB Read: " + rowCount.toString());
            return rowCount.intValue();
        } else {
            return rowCount.intValue();
        }
    }
     
    
	
}
