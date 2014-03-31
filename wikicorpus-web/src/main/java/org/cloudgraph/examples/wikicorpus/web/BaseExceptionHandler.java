package org.cloudgraph.examples.wikicorpus.web;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseExceptionHandler 
{
	private static Log log =LogFactory.getLog(
			   BaseExceptionHandler.class);
    private static String instanceName = "Server Instance";
    
    static {
        try {
            //InitialContext ctx = new InitialContext();
            //MBeanServer server = (MBeanServer)ctx.lookup("java:comp/env/jmx/runtime"); 
            //ObjectName serverRT = (ObjectName)server.getAttribute(new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean"), "ServerRuntime");
            //instanceName = (String)server.getAttribute(serverRT, "Name");
        }
        catch (Exception e) {
        	log.error("Could not get server instance name", e);        	
        }
    }
    
    protected static String getInstanceName() {
    	return instanceName;
    }
    protected String errorId;
    
    protected abstract Throwable getException();
    protected abstract Log getLogInstance();
    
    public BaseExceptionHandler()
    {
        this.errorId = createUniqueID();
    }
    
    protected void logStackTrace() 
    {
        getLogInstance().error(getStrackTrace());
    }
    
    public String getStrackTrace()
    {
        Throwable ex = getException();

        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        pw.println("\n================ " + errorId + " ================ ");
        populateTrace(ex, pw);
        pw.println("================ " + errorId + " ================ ");

        return writer.toString();
    }
    
    protected void populateTrace(Throwable ex, PrintWriter pw)
    {
        fillStackTrace(ex, pw);
    }
    
    private void fillStackTrace(Throwable ex, PrintWriter pw) 
    {
        if (null == ex) { return; }

        ex.printStackTrace(pw);

        if (ex instanceof ServletException) 
        {
            Throwable cause = ((ServletException) ex).getRootCause();
            if (null != cause) 
            {
                pw.println("Root Cause:");
                fillStackTrace(cause, pw);
            }
        } 
        else 
        {
            Throwable cause = ex.getCause();
            if (null != cause) 
            {
                pw.println("Cause:");
                fillStackTrace(cause, pw);
            }
        }
    }
    
    public static String createUniqueID ()
    {
        return getInstanceName() + "." + System.currentTimeMillis();
    }
    
    public String getErrorId() {
        return errorId;
    }
}
