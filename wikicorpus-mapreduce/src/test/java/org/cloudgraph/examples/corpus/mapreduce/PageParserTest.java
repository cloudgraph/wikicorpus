package org.cloudgraph.examples.corpus.mapreduce;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wikicorpus.mapreduce.PageCounter;
import org.plasma.config.DataAccessProviderName;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.DataAccessClient;
import org.plasma.sdo.access.client.PojoDataAccessClient;

import commonj.sdo.DataGraph;

public class PageParserTest extends CommonTest {
    private DataAccessClient service; 	

	public void setUp() throws Exception {
		super.setUp();
    	this.service = new PojoDataAccessClient(DataAccessProviderName.HBASE);
	}
	  
	  
	public void testQuery() throws Exception {
		 Query query = PageCounter.createInputQuery();
		 DataGraph[] graphs = this.service.find(query);
		 assertTrue(graphs != null);
	}	
       	 
	
	public void testMatch1() {
		String patternStr = "Lan*";
		String value = "Lan's Lantern";
		
		Pattern pattern = Pattern.compile(wildcardToRegex(patternStr));
	    Matcher matcher = pattern.matcher(value);
	    boolean result = matcher.matches();
	    log.info("PAT: " + pattern.toString() + " value: " + value + " - " + result);
		assertTrue(result);
	}
	
	//L'Alfαs del Pi
	
	   public static String wildcardToRegex(String wildcard){
	        StringBuffer s = new StringBuffer(wildcard.length());
	        s.append('^');
	        for (int i = 0, is = wildcard.length(); i < is; i++) {
	            char c = wildcard.charAt(i);
	            switch(c) {
	                case '*':
	                    s.append(".*");
	                    break;
	                case '?':
	                    s.append(".");
	                    break;
	                    // escape special regexp-characters
	                case '(': case ')': case '[': case ']': case '$':
	                case '^': case '.': case '{': case '}': case '|':
	                case '\\':
	                    s.append("\\");
	                    s.append(c);
	                    break;
	                default:
	                    s.append(c);
	                    break;
	            }
	        }
	        s.append('$');
	        return(s.toString());
	    }
	
	
}
