package org.cloudgraph.examples.wikicorpus.web.util;

import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.web.ResourceManager;
import org.cloudgraph.examples.wikicorpus.web.ResourceType;






public class ResourceFinder 
    implements WebConstants
{
    private static Log log =LogFactory.getLog(ResourceFinder.class);
    
    public String getStatusLabel(String status) {
        String result = status;
        try {
        	result = ResourceManager.instance().getString(RESOURCE_DASHBOARD_SATUS_PREFIX + status,
                    ResourceType.LABEL);        	
        } catch (MissingResourceException e) {
            log.warn(e.getMessage(), e);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        
        return result;
    }

	public String getLabel(String resourceId) {
		return getStatusLabel(resourceId);
	}    
}
