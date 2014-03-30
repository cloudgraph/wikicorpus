package org.cloudgraph.examples.wikicorpus.service;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.Type;

public class Adapter implements Serializable {
	private static final long serialVersionUID = 1L;

    protected void validateTextFieldLength(FacesContext facesContext,
            UIComponent component, Object value, long maxLength,
            String label) {
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	if (text.length() > maxLength) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(maxLength) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
	}	
    
    protected long getMaxLength(Class clss, String propertyName)
    {
		Type type = PlasmaTypeHelper.INSTANCE.getType(clss);
		PlasmaProperty prop = 
			(PlasmaProperty)type.getProperty(propertyName);
		return prop.getMaxLength();    	
    }
}
