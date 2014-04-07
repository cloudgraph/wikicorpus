package org.cloudgraph.examples.wikicorpus.service;

import java.io.Serializable;


import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.Type;

public class Adapter implements Serializable {
	private static final long serialVersionUID = 1L;
    
    protected long getMaxLength(Class clss, String propertyName)
    {
		Type type = PlasmaTypeHelper.INSTANCE.getType(clss);
		PlasmaProperty prop = 
			(PlasmaProperty)type.getProperty(propertyName);
		return prop.getMaxLength();    	
    }
}
