/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.examples.test.model;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wikicorpus.model.xces.ana.XCESAnaDataBinding;
import org.plasma.common.bind.BindingValidationEventHandler;
import org.plasma.common.xslt.XSLTUtils;
import org.xces.ana.CesAna;
import org.xces.ana.StructType;
import org.xml.sax.SAXException;

/**
 */
public class XCESTest extends CommonTest {
    private static Log log = LogFactory.getLog(XCESTest.class);

    public void setUp() throws Exception {
        super.setUp();
    }        
   
    public void testXCESAnaUnmarshal() throws IOException, JAXBException, SAXException {
    	
    	
    	String text = getContent(new File("./src/test/resources/fiction/eggan/TheStory.txt"));
    	char[] chars = text.toCharArray();
    	
    	
    	XCESAnaDataBinding binding = new XCESAnaDataBinding(
    			new BindingValidationEventHandler(){

					@Override
					public boolean handleEvent(ValidationEvent event) {
						log.error(event.getMessage());
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public int getErrorCount() {
						// TODO Auto-generated method stub
						return 0;
					}
    				
    			});
    	//File file = new File("./src/test/resources/fiction/eggan/TheStory-s.xml");
    	File file = new File("./src/test/resources/fiction/eggan/TheStory-np.xml");
    	
    	
    	CesAna root = (CesAna)binding.unmarshal(new FileInputStream(file));
    	assertTrue(root != null);
    	log.info(root.getClass().getName());
    	//String xml = binding.marshal(root);
    	//log.info(xml);
    	
    	//multibyte characters in the text throw of the from/to character index numbers
    	// probably need to read text using proper encoding
    	int max = 25, count = 0;    	
    	for (StructType struct : root.getStructs()) {
    		int len = struct.getTo() - struct.getFrom();
    		char[] buf = new char[len];
    		System.arraycopy(chars, struct.getFrom(), buf, 0, len);
    		String chunk = new String(buf);
    		//String chunk = text.substring(struct.getFrom(), struct.getTo());
    		log.info(chunk);
    		count++;
    		if (count > max)
    			break;
    	}
    	
    	
    }
        
    private String getContent(File source)
            throws IOException
        {
            long size = source.length();
            byte[] buf = new byte[4000];
            ByteArrayOutputStream os = new ByteArrayOutputStream((int)size); // bad!
            FileInputStream is = new FileInputStream(source);              
            int len = -1;                                                
            while ((len = is.read(buf)) != -1)                           
                os.write(buf, 0, len);                                                           
            is.close();
            os.flush();                                                           
            return new String(os.toByteArray());            
        }
}