package org.cloudgraph.examples.wikicorpus.web.util;

import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ResourceCache implements WebConstants
{
    private static Log log =LogFactory.getLog(
        ResourceCache.class); 


    static private ResourceCache instance = null; 
    
    private ResourceCache()
    {
        // pull common resources out once only
        try {


        } catch (MissingResourceException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static ResourceCache instance()
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }  

    private static synchronized void initializeInstance()
    {
        if (instance == null)            
            instance = new ResourceCache();
    }
    
}