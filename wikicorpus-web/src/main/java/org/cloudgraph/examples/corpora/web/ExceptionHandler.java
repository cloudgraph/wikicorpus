package org.cloudgraph.examples.wikicorpus.web;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ExceptionHandler extends BaseExceptionHandler 
{
    private static Log log =LogFactory.getLog(
    		ExceptionHandler.class);

    protected Throwable sourceException;
    
    public ExceptionHandler(Throwable source)
    {
        this.sourceException = source;
    }
    
    protected Throwable getException() { return sourceException;}
    protected Log getLogInstance() { return ExceptionHandler.log;}
    
}
