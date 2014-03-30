package org.cloudgraph.examples.wikicorpus.web;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ErrorDisplayBean extends BaseExceptionHandler
{
    
    private static final String REQ_ATTRIB_EXCP_KEY = "javax.servlet.error.exception";
    private static final String REQ_ATTRIB_STATUS_KEY = "javax.servlet.error.status_code";
    private static final String REQ_ATTRIB_URI_KEY = "javax.servlet.error.request_uri";
    private static final String LABEL_SERVER_NAME = "Server Name: ";
    private static final String LABEL_URL = "URI :";
    private static final String LABEL_ERR_CODE = "Error Code :";
    private static final String LABEL_SEP = ":";
    private static final String FACES_VIEWS_KEY = "com.sun.faces.VIEW_LIST";
    
	private static Log log =LogFactory.getLog(
			ErrorDisplayBean.class);
    
    protected String serverId;
    protected String port;
    protected Integer errorCode;
    protected String errorUri;
    
    protected FacesContext facesContext;
    
    protected Log getLogInstance(){ return ErrorDisplayBean.log; }
    
    protected Throwable getException()
    {
        Map requestMap = facesContext.getExternalContext().getRequestMap();
        return (Throwable) requestMap.get(REQ_ATTRIB_EXCP_KEY);
    }
    
    public ErrorDisplayBean()
    {
        super();
        init();
        logStackTrace();
        destroyView();
    }
    
    protected void init()
    {
        facesContext = FacesContext.getCurrentInstance();
        
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        if(request != null)
        {
            this.serverId  = request.getServerName();
            this.port = String.valueOf(request.getServerPort());
            this.errorUri = (String) request.getAttribute(REQ_ATTRIB_URI_KEY);
            this.errorCode = (Integer) request.getAttribute(REQ_ATTRIB_STATUS_KEY);
        }
    }
    
    /**
     * Any child class may specify what views should not be destroyed due to this process.
     * @return
     */
    protected Set getProtectedViews(){ return null; }
    
    protected void destroyView()
    {
        HttpSession session = getSessionObject();
        List views = null;
        Iterator viewsKeyIt = null;
        Set protectedViews = getProtectedViews();
        
        if(session != null)
            { views = (List)getSessionMap().get(FACES_VIEWS_KEY); }
        
        if(views != null)
            { viewsKeyIt = views.iterator(); }
        
        if(viewsKeyIt != null)
        {
            while (viewsKeyIt.hasNext())
            {
              String key = (String)viewsKeyIt.next();
              if (protectedViews != null && protectedViews.size() > 0 && !protectedViews.contains(key)) 
              {
                  session.removeAttribute(key);
              }
            }
        }

    }
    
    protected HttpSession getSessionObject() 
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpSession session = null;
        
        if (externalContext != null) 
        {
          session = (HttpSession) externalContext.getSession(false);
        }
        
        return session;
    }
    
    protected Map getSessionMap() 
    {
        Map sessionMap = null;
        ExternalContext externalContext = facesContext.getExternalContext();

        if (externalContext != null)
        {
            sessionMap = externalContext.getSessionMap();
        }
        
        return sessionMap;
      }
    
    public String getInfoMessage() 
    {
        return this.errorId;
    }

    protected void populateTrace(Throwable ex, PrintWriter pw)
    {
        fillRequestInfo(pw);
        super.populateTrace(ex, pw);
    }


    /**
     * Method to fill request information in the string write to be shown in
     * the UI page
     *
     * @param request   servlet request
     * @param writer    string writer reference
     */
    private void fillRequestInfo(PrintWriter pw)
    {
        StringBuffer sbData = new StringBuffer();
        // server name
        sbData.append(LABEL_SERVER_NAME);
        sbData.append(serverId);
        sbData.append(LABEL_SEP);
        sbData.append(port);
        pw.println(sbData.toString());
        
        // URL
        sbData = new StringBuffer();
        sbData.append(LABEL_URL);
        sbData.append(errorUri);
        pw.println(sbData.toString());

        // Error Code
        sbData = new StringBuffer();
        sbData.append(LABEL_ERR_CODE);
        sbData.append(errorCode);
        pw.println(sbData.toString());
    }
    
    
    
    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorId() {
        return errorId;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public String getPort() {
        return port;
    }

    public String getServerId() {
        return serverId;
    }   
}
