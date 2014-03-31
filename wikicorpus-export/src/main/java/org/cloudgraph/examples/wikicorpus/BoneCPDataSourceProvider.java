package org.cloudgraph.examples.wikicorpus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.rdb.connect.DataSourceProvder;
import org.plasma.config.DataAccessProviderName;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.Property;
import org.plasma.config.ConfigurationConstants;
import org.plasma.sdo.access.DataAccessException;

import com.jolbox.bonecp.BoneCPDataSource;

public class BoneCPDataSourceProvider implements DataSourceProvder {
	private static final Log log = LogFactory.getLog(BoneCPDataSourceProvider.class);
	private BoneCPDataSource dataSource;

	public BoneCPDataSourceProvider()
	{
		HashMap<String, Object> vendorPropMap = new HashMap<String, Object>();
		Properties props = new Properties();
	    for (Property property : PlasmaConfig.getInstance().getDataAccessProvider(DataAccessProviderName.JDBC).getProperties()) {
	    	props.put(property.getName(), property.getValue());
	    	if (!property.getName().startsWith(ConfigurationConstants.JDBC_PROVIDER_PROPERTY_PREFIX))
	    	   vendorPropMap.put(property.getName(), property.getValue());
	    }
	    
	    String providerName = props.getProperty(ConfigurationConstants.JDBC_PROVIDER_NAME);
	    String driverName = props.getProperty(ConfigurationConstants.JDBC_DRIVER_NAME);
	    String url = props.getProperty(ConfigurationConstants.JDBC_URL);
	    String user = props.getProperty(ConfigurationConstants.JDBC_USER);
	    String password = props.getProperty(ConfigurationConstants.JDBC_PASSWORD);
	    
		try {
			java.lang.Class.forName(driverName).newInstance();
		} catch (Exception e) {
			log.error(
				"Error when attempting to obtain DB Driver: "
							+ driverName, e);
		}
		dataSource = new BoneCPDataSource();   
		dataSource.setJdbcUrl(url);		 
		dataSource.setUsername(user);				 
		dataSource.setPassword(password);	
		try {
			dataSource.setProperties(props);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

}
