package org.cloudgraph.examples.wikicorpus.web.model.profile;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.web.util.WebConstants;


@ManagedBean(name="UserBean")
@SessionScoped
public class UserBean implements Serializable {
	 
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(UserBean.class);
	
	private String name = "sbear"; // fallback user for Tomcat or other non-auth testing
	private String roleName = "ANONYMOUS";	
	
	public UserBean() {
		
	    
        try {
        	FacesContext context = FacesContext.getCurrentInstance();
        	Principal principal = context.getExternalContext().getUserPrincipal();
        	if (principal != null && 
        		principal.getName() != null && 
        		principal.getName().length() > 0)
        	    name = principal.getName();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
	        //throw new RuntimeException("could not create principal");
        }  
        
        
	}
	
	public String save() {
		return null;
	}
	
	public String getRoleName() {
		return this.roleName;
	}	
		
    public String getBundleName() {
    	return WebConstants.BUNDLE_BASENAME;
    }

	public String getName() {
		return name;
	}

	int countReload;
	public void reload() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ctx.getExternalContext().redirect("index.xhtml");
            /*
            countReload++;
            if (countReload < 5) {
                FacesContext ctx = FacesContext.getCurrentInstance();
                ctx.getExternalContext().redirect("index.xhtml");
            } else {
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpSession session = request.getSession(false);
                session.invalidate();
                FacesContext temp = FacesContext.getCurrentInstance();
                temp.getExternalContext().redirect("login.xhtml");
            }
            */
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
