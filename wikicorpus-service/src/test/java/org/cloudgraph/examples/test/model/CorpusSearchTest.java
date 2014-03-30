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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusService;
import org.cloudgraph.examples.wikicorpus.service.corpus.CorpusServiceImpl;
import org.cloudgraph.examples.wikicorpus.service.corpus.Dependency;
import org.cloudgraph.examples.wikicorpus.service.corpus.Sentence;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.search.WordDependency;

/**
 */
public class CorpusSearchTest extends CommonTest {
	private static Log log = LogFactory.getLog(CorpusSearchTest.class);
	private CorpusService service;

	public void setUp() throws Exception {
		super.setUp();
		this.service = new CorpusServiceImpl();
	}
	
	 
	public void testFindDependencies() throws Exception {
		List<Dependency> list = this.service.findDependencies("have");
		int govtotal = 0;
		int deptotal = 0;
		for (Dependency dep : list) {
			log.info(dep.getLemma() + "/" + dep.getType() + "/" + dep.getDisplayType() + "/" 
		        + dep.getGovernorCountDeep() + "/" + dep.getDependentCountDeep());
			for (Dependency dep2 : dep.getDependencies()) {
				log.info("\t" + dep2.getLemma() + "/" + dep2.getType() + "/" + dep2.getDisplayType() + "/" 
				        + dep2.getGovernorCountDeep() + "/" + dep2.getDependentCountDeep());
			}
			
			govtotal += dep.getGovernorCount();
			deptotal += dep.getDependentCount();
		}
		log.info("govtotal: " + govtotal);
		log.info("deptotal: " + deptotal);
	}
	 	
	 
   /*
	public void testFindGovernorSentences() throws Exception {
		List<Sentence> list = this.service.findGovernors("have", "parataxis");
		for (Sentence sent : list) {
			log.info(sent.getLemma() + "/" + sent.getPOS() + "/" + sent.getTextWithMarkup());
		}
	}
	*/
	 
}