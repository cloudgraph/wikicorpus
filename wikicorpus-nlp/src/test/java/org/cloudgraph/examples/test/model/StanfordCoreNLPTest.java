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
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.xml.sax.SAXException;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.TypesafeMap;

/**
 */
public class StanfordCoreNLPTest extends CommonTest {
	private static Log log = LogFactory.getLog(StanfordCoreNLPTest.class);

	static StanfordCoreNLP pipeline; 
			
	public void setUp() throws Exception {
		super.setUp();
		if (pipeline == null) {
		    // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		    // NER, parsing, and coreference resolution
		    Properties props = new Properties();
		    props.put("annotators",
				"tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		    pipeline = new StanfordCoreNLP(props);
		}
	}

	public void testParse1() throws IOException, JAXBException, SAXException {
		String text = "Stanford University is located in California. It is a great university.";
		long before = System.currentTimeMillis();
		parse(text);
		long after = System.currentTimeMillis();
		log.info("time1: " + String.valueOf(after - before));
	}
	
	public void testParse2() throws IOException, JAXBException, SAXException {
		String text = "My Bonny lies over the ocean.";
		long before = System.currentTimeMillis();
		parse(text);
		long after = System.currentTimeMillis();
		log.info("time2: " + String.valueOf(after - before));
	}
	
	public void testParse3() throws IOException, JAXBException, SAXException {
		StringBuilder buf = new StringBuilder();
		
		buf.append("Four score and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal.");
		buf.append("Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this.");
		buf.append("But, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us -- that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion -- that we here highly resolve that these dead shall not have died in vain -- that this nation, under God, shall have a new birth of freedom -- and that government of the people, by the people, for the people, shall not perish from the earth.");		
		parse(buf);
	}
	
	public void testParse4() throws IOException, JAXBException, SAXException {
		StringBuilder buf = new StringBuilder();
		buf.append("Autism is a disorder of neural development characterized by impaired social interaction and verbal and non-verbal communication, and by restricted, repetitive or stereotyped behavior. The diagnostic criteria require that symptoms become apparent before a child is three years old.[2] Autism affects information processing in the brain by altering how nerve cells and their synapses connect and organize; how this occurs is not well understood.[3] It is one of three recognized disorders in the autism spectrum (ASDs), the other two being Asperger syndrome, which lacks delays in cognitive development and language, and pervasive developmental disorder, not otherwise specified (commonly abbreviated as PDD-NOS), which is diagnosed when the full set of criteria for autism or Asperger syndrome are not met.");
		buf.append("Autism has a strong genetic basis, although the genetics of autism are complex and it is unclear whether ASD is explained more by rare mutations, or by rare combinations of common genetic variants.[5] In rare cases, autism is strongly associated with agents that cause birth defects.[6] Controversies surround other proposed environmental causes, such as heavy metals, pesticides or childhood vaccines;[7] the vaccine hypotheses are biologically implausible and lack convincing scientific evidence.[8] The prevalence of autism is about 1–2 per 1,000 people worldwide, and it occurs about four times more often in boys than girls.[9] The Centers for Disease Control and Prevention (CDC) report 20 per 1,000 children in the United States are diagnosed with ASD as of 2012, up from 11 per 1,000 in 2008. The number of people diagnosed with autism has been increasing dramatically since the 1980s, partly due to changes in diagnostic practice and government-subsidized financial incentives for named diagnoses;[11] the question of whether actual prevalence has increased is unresolved.");
		buf.append("Parents usually notice signs in the first two years of their child's life.[12] The signs usually develop gradually, but some autistic children first develop more normally and then regress.[13] Early behavioral, cognitive, or speech interventions can help autistic children gain self-care, social, and communication skills.[12] Although there is no known cure,[12] there have been reported cases of children who recovered.[14] Not many children with autism live independently after reaching adulthood, though some become successful.[15] An autistic culture has developed, with some individuals seeking a cure and others believing autism should be accepted as a difference and not treated as a disorder.");
		parse(buf);
	}
	
	private void parse(StringBuilder buf) throws IOException {
		BreakIterator iterator =
                BreakIterator.getSentenceInstance(Locale.US);

		String text = buf.toString();
		int counter = 0;
		iterator.setText(text);

        int lastIndex = iterator.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = iterator.next();

            if (lastIndex != BreakIterator.DONE) {
                String sentence = text.substring(firstIndex, lastIndex);
    			long before = System.currentTimeMillis();
    			parse(sentence);
    			long after = System.currentTimeMillis();
    			log.info("time4: " + String.valueOf(after - before) + ": " + sentence);
                counter++;
            }
        }
		
	}

	private void parse(String text) throws IOException {
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			//String id = sentence.get(IDAnnotation.class);
			//assertTrue(id != null);
			Set<Class<?>> keys = sentence.keySet();
			for (Class<?> classKey : keys) {
				//log.info("key: " + classKey.getName());
			}
			int intAnot = sentence.get(CharacterOffsetBeginAnnotation.class);
			assertTrue(intAnot >= 0);
			//log.info("begin: " + CharacterOffsetBeginAnnotation.class.getSimpleName() + ": " + intAnot);
			int intAnot2 = sentence.get(CharacterOffsetEndAnnotation.class);
			//log.info("end: " + CharacterOffsetEndAnnotation.class.getSimpleName() + ": " + intAnot2);
			assertTrue(intAnot2 > 0);
			
			int index = sentence.get(SentenceIndexAnnotation.class);
			assertTrue(index >= 0);
			
			// this is the parse tree of the current sentence
			// FIXME: may consider modeling trees 
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Tree tree = sentence.get(TreeAnnotation.class);
			PrintWriter xmlOut = new PrintWriter(os);
			TreePrint print = pipeline.getConstituentTreePrinter();
			print.printTree(tree, xmlOut);
			//log.info(new String(os.toByteArray()));
			
			Map<Integer, CoreLabel> tokenMap = new HashMap<Integer, CoreLabel>();
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				if (word.equals(pos)) {
					//log.info("skip: " +word);
					continue; // punctuation, other garb, skip it
				}
				if (Pattern.matches("\\p{Punct}", word)) {
					//log.info("skip: " +word);
					continue; // punctuation
				}

				int ti = token.get(IndexAnnotation.class);
				assertTrue(ti >= 0);
				tokenMap.put(ti, token);
				
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
			}

			SemanticGraph basic = sentence.get(BasicDependenciesAnnotation.class);
			Collection<TypedDependency> deps = basic.typedDependencies();
			for (TypedDependency tdep : deps) {
				GrammaticalRelation reln = tdep.reln();
				String type = reln.toString();
				//log.info("type: " + type);
				CoreLabel gov = tdep.gov().label();
				int govIndex = gov.get(IndexAnnotation.class);
				CoreLabel label  = tokenMap.get(govIndex);
				assertTrue(label != null);
				
				CoreLabel dep = tdep.dep().label();
				int depIndex = dep.get(IndexAnnotation.class);
				label  = tokenMap.get(depIndex);
				assertTrue(label != null);
			}

			SemanticGraph ccProcessed = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			
			
		}

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		Map<Integer, CorefChain> graph = document
				.get(CorefChainAnnotation.class);

		//ByteArrayOutputStream os = new ByteArrayOutputStream();
		//pipeline.prettyPrint(document, os);
		//log.info(new String(os.toByteArray()));
		
		//pipeline.xmlPrint(document, os);
		//log.info(new String(os.toByteArray()));
	}
	
	private String getContent(File source) throws IOException {
		long size = source.length();
		byte[] buf = new byte[4000];
		ByteArrayOutputStream os = new ByteArrayOutputStream((int) size); // bad!
		FileInputStream is = new FileInputStream(source);
		int len = -1;
		while ((len = is.read(buf)) != -1)
			os.write(buf, 0, len);
		is.close();
		os.flush();
		return new String(os.toByteArray());
	}
}