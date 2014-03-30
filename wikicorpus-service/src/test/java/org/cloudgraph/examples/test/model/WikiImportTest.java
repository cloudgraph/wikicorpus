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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wikicorpus.service.WikiService;
import org.cloudgraph.examples.wikicorpus.service.WikiServiceImpl;

/**
 */
public class WikiImportTest extends CommonTest {
	private static Log log = LogFactory.getLog(WikiImportTest.class);
	private WikiService service;

	public void setUp() throws Exception {
		super.setUp();
		this.service = new WikiServiceImpl();
	}

	
	public void testImport() throws Exception {
		this.service.load(new File("./src/test/resources/wiki-autism.xml"), 1000, false);
	}	

}