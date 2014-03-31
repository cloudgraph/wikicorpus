package org.cloudgraph.examples.wikicorpus.web;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import commonj.sdo.Property;
import commonj.sdo.Type;

public class ResourceManager
{
    private static Log log =LogFactory.getLog(
        ResourceManager.class); 

    // this delim char makes Faces EL expressions 
    // happy. Pretty much every other char doesn't
    public static final String DELIM = "_";  

    static private ResourceManager instance = null; 

    private ResourceBundle facesBundle;    

    private ResourceManager()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession)context.getExternalContext().getSession(false);   	
    	
        // Note: assumes no locale switches
        Locale locale = getLocale(context);
        ClassLoader loader = getClassLoader();
        facesBundle = ResourceBundle.getBundle("javax.faces.Messages", locale, loader);   
        if (facesBundle == null)
            throw new RuntimeException("could not get bundle 'javax.faces.Messages'");   
           
    }

    public static ResourceManager instance()
        throws RuntimeException
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }  

    private static synchronized void initializeInstance()
        throws RuntimeException
    {
        if (instance == null)            
            instance = new ResourceManager();
    }
    
    public ResourceBundle getBundle()
    {
        return AppMessageUtils.getMessageBundle();
    }

    public String getString(String key)
    {
        try {
            return AppMessageUtils.getMessageBundle().getString(key);
        }
        catch (MissingResourceException e) {
            return facesBundle.getString(key);
        }
    }

    // We KNOW resources with these custom keys are not in the
    // faces bundle, so don't try
    public String getString(String id, ResourceType type)
    {
        return AppMessageUtils.getMessageBundle().getString(getResourceKey(id, type));
    }

    public String getString(Type type, ResourceType resourceType)
    {
        return AppMessageUtils.getMessageBundle().getString(getResourceKey(type, resourceType));
    }

    public String getString(Type type, Property property, ResourceType resourceType)
    {
        return AppMessageUtils.getMessageBundle().getString(getResourceKey(type, property, resourceType));
    }    

    private String getResourceKey(Type type, ResourceType resourceType)
    {
        return DELIM + type.getName() + DELIM + resourceType.toString();
    }

    private String getResourceKey(Type type, Property property, ResourceType resourceType)
    {
        return DELIM + type.getName() + DELIM + property.getName() + DELIM + resourceType.toString();
    }

    public String getResourceKey(Type type, Property property, String value, ResourceType resourceType)
    {
        return DELIM + type.getName() + DELIM + property.getName() 
           + DELIM + value + DELIM + resourceType.toString();
    }

    private String getResourceKey(String id, ResourceType type)
    {
        return id + DELIM + type.toString();
    }

    private String getResourceKey(String viewId, String subviewId, ResourceType type)
    {
        return viewId + DELIM +subviewId 
           + DELIM + type.toString();
    }

    private Locale getLocale(FacesContext context) {                
        Locale locale = null;                                                 
        UIViewRoot viewRoot = context.getViewRoot();                      
        if (viewRoot != null) locale = viewRoot.getLocale();              
        if (locale == null) locale = Locale.getDefault();                 
        return locale;                                                    
    }                                                                     
                                                                          
    private ClassLoader getClassLoader() {                          
        ClassLoader loader = Thread.currentThread().getContextClassLoader();  
        if (loader == null) loader = ClassLoader.getSystemClassLoader();     
        return loader;                                                       
    }                                                                     
}