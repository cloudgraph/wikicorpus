package org.cloudgraph.examples.wikicorpus.web;                                                                    
                                                                                                                                       
import java.text.MessageFormat;
import java.util.MissingResourceException;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

                                                                       
public class ErrorHandlerBean extends BaseExceptionHandler implements ErrorConstants 
{                                    
    private static Log log =LogFactory.getLog(
        ErrorHandlerBean.class); 
            
    private Throwable throwable;
    private String redirectView;
    private String previousView;
    private boolean recoverable = false;
                             
    public ErrorHandlerBean() 
    {
        super();
        if (log.isDebugEnabled()) {
            log.debug("ErrorHandlerBean() - " + String.valueOf(this));
        }
        this.errorId = null;
    }
    
    protected Log getLogInstance(){ return ErrorHandlerBean.log; }
    
    public static ErrorHandlerBean getReferenceFromFaces() {
        FacesContext context = FacesContext.getCurrentInstance();
        ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application app = appFactory.getApplication();

        // pull the ErrorHandlerBean from the faces context
        String beanName = ErrorHandlerBean.class.getName().substring(ErrorHandlerBean.class.getName().lastIndexOf(".") + 1);
        ErrorHandlerBean bean = (ErrorHandlerBean) app.createValueBinding("#{" + beanName + "}").getValue(context);
        return bean;
    }    
    
    protected void setErrorId()
    {
        this.errorId = BaseExceptionHandler.createUniqueID();
    }
    
    /**
     * This weird situation occurs if the user attempst to refresh the ErrorHandler jsp
     * or visits the ErrorHandler JSP without an exception.
     * If this is the case, we'll dummy up an exception so that this event can be properly logged.
     * @return
     */
    public String getErrorId()
    { 
        if(this.errorId == null)
        {
            setError(new Exception("Attempt to Obtain Error ID without Exception"));
        }
        
        return this.errorId;
    }
    
    public void setError(Throwable t)
    {
        this.throwable = t;
        setErrorId();
    }    

    public void setError(Throwable t, String redirectView)
    {
        setError(t);
        this.redirectView = redirectView;
    }    

    public Throwable getError()
    {
        return this.throwable;
    }    
    
    protected Throwable getException() { return this.throwable; }

    public boolean getHasError()
    {
        return this.throwable != null;
    }    
    
    public void setNavigation(String redirectView)
    {
        this.redirectView = redirectView;
    }

    public String getNavigation()
    {
        return redirectView;
    }
    
    public boolean getHasNavigation()
    {
        return redirectView != null;
    }
    
    public boolean getIsRecoverable()
    {
        return recoverable;
    }
    
    public void setRecoverable(boolean recoverable)
    {
        this.recoverable = recoverable; 
    }
    
    public void logError(Throwable t)
    {
        setError(t);
        logError();    
    }

    public void logError()
    {
        if (this.throwable != null)
        {
            logStackTrace();
        }    
    }
        
    public String getErrorType()
    {
    	try {
	        if (throwable != null)
	        {
	            if (throwable instanceof UserException)
	            {
	                UserException userException = (UserException)throwable;
	                return ResourceManager.instance().getString(userException.getTypeId(), ResourceType.LABEL);
	            }
	        }
	        return ResourceManager.instance().getString(ERROR_TYPE_INTERNAL, ResourceType.LABEL);
    	}
    	catch (MissingResourceException t) {
    		return ERROR_TYPE_INTERNAL;
    	}
    }

    public String getErrorSeverity()
    {
    	try {
            return ResourceManager.instance().getString(ERROR_SEVERITY_FATAL, ResourceType.LABEL);
    	}
    	catch (MissingResourceException t) {
    		return ERROR_SEVERITY_FATAL;
    	}    
    }

    public String getErrorMessage()
    {
    	try {
	        if (throwable != null)
	        {
	            if (throwable instanceof UserException)
	            {
	            	UserException userException = (UserException)throwable;
	                String unformatted = ResourceManager.instance().getString(userException.getMessageId(), 
	                    ResourceType.LABEL);
	                if (!userException.hasParams())
	                    return unformatted;
	                else
	                    return MessageFormat.format(unformatted, userException.getParams());
	            }
	        }
	        return ResourceManager.instance().getString(ERROR_MESSAGE_INTERNAL, 
	            ResourceType.LABEL);
    	}
    	catch (MissingResourceException t) {
    		return ERROR_MESSAGE_INTERNAL;
    	}
    }

    public String getErrorHelp()
    {
    	try {
	        if (throwable != null)
	        {
	            if (throwable instanceof UserException)
	            {
	            	UserException userException = (UserException)throwable;
	                String unformatted = ResourceManager.instance().getString(userException.getMessageId(), 
	                    ResourceType.HELP);
	                if (!userException.hasParams())
	                    return unformatted;
	                else
	                    return MessageFormat.format(unformatted, userException.getParams());
	            }
	        }
	        return ResourceManager.instance().getString(ERROR_MESSAGE_INTERNAL, 
	            ResourceType.HELP);
    	}
    	catch (MissingResourceException t) {
    		return ERROR_MESSAGE_INTERNAL;
    	}
    }
    
    public String navigateBack(){
    	return previousView != null? previousView:""; 
    }

	public String getPreviousView() {
		return previousView;
	}

	public void setPreviousView(String previousView) {
		this.previousView = previousView;
	}
}                                                                                      