package org.cloudgraph.examples.wikicorpus.nlp;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.DependencyRepresentation;
import org.cloudgraph.examples.corpus.parse.DependencySet;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.Node;
import org.cloudgraph.examples.corpus.parse.POS;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.parse.WordRelation;
import org.cloudgraph.examples.corpus.parse.WordRelationType;

import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class WikiAnnotator {
	private static Log log = LogFactory.getLog(WikiAnnotator.class);

	public Sentence buildSentence(StanfordCoreNLP pipeline, 
			CoreMap sentenceAnnotation, Sentence pageSentence,
			int beginOffset, int endOffset, Document document) {
		int index = sentenceAnnotation.get(SentenceIndexAnnotation.class);
		pageSentence.setId(index);
		
		//int beginOffset = sentenceAnnotation.get(CharacterOffsetBeginAnnotation.class);
		//int endOffset = sentenceAnnotation.get(CharacterOffsetEndAnnotation.class);
		pageSentence.setCharacterOffsetBegin(beginOffset); 
		pageSentence.setCharacterOffsetEnd(endOffset); 
				
		// the sentence parse tree
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Tree tree = sentenceAnnotation.get(TreeAnnotation.class);
		PrintWriter xmlOut = new PrintWriter(os);
		TreePrint print = pipeline.getConstituentTreePrinter();
		print.printTree(tree, xmlOut);
		pageSentence.setParse(new String(os.toByteArray()));
		
		Map<Integer, Node> pageTokenMap = buildNodes(sentenceAnnotation,  pageSentence);
		
		SemanticGraph basic = sentenceAnnotation.get(BasicDependenciesAnnotation.class);
		if (basic != null) {
		    Collection<TypedDependency> deps = basic.typedDependencies();
		    DependencySet dset = pageSentence.createDependencySet();
		    dset.setRepresentation(DependencyRepresentation.BASIC.getInstanceName());
		    addDependencies(dset, deps, pageTokenMap);
		}

		SemanticGraph colapsed = sentenceAnnotation
				.get(CollapsedDependenciesAnnotation.class);
		if (colapsed != null) {
		    Collection<TypedDependency> deps = colapsed.typedDependencies();
		    DependencySet dset = pageSentence.createDependencySet();
		    dset.setRepresentation(DependencyRepresentation.COLLAPSED.getInstanceName());
		    addDependencies(dset, deps, pageTokenMap);
		}

		SemanticGraph ccProcessed = sentenceAnnotation
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		if (ccProcessed != null) {
		    Collection<TypedDependency> deps = ccProcessed.typedDependencies();
		    DependencySet dset = pageSentence.createDependencySet();
		    dset.setRepresentation(DependencyRepresentation.CC_PROCESSED.getInstanceName());
		    addDependencies(dset, deps, pageTokenMap);
		}
		
		return pageSentence;
	}
	
	public Map<Integer, Node> buildNodes(CoreMap sentence, Sentence pageSentence) {
		Map<Integer, Node> pageTokenMap = new HashMap<Integer, Node>();
		List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
		for (CoreLabel token : labels) {
			int tokenIndex = 0;
			try {		
				tokenIndex = token.get(IndexAnnotation.class);
			}
			catch (Throwable t) {
				throw new RuntimeException(t);
			}
			
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			if (word.equals(pos) || pos == null) {
				continue; // punctuation, other garb, skip it
			}
			if (Pattern.matches("\\p{Punct}", word)) 
				continue; // punctuation
			//if (word.length() == 1 && 
			POS posValue = null;
			try {
			    posValue = POS.valueOf(pos);
			}
			catch (IllegalArgumentException e) {
				continue; // punctuation
			}			

			Node pageToken = pageSentence.createNode();
			pageToken.setId(tokenIndex);
			pageTokenMap.put(tokenIndex, pageToken);
			
			pageToken.setWord(word);
			
			pageToken.setPos(posValue.name());
			
			String lemma = token.get(LemmaAnnotation.class);
			if (lemma != null)
			    pageToken.setLemma(lemma);

			// this is the NER label of the token
			String ne = token.get(NamedEntityTagAnnotation.class);
			if (ne != null)
				pageToken.setNamedEntity(ne);

			int charOffsetBegin = token.beginPosition();
			int charOffsetEnd = token.endPosition();
			pageToken.setCharacterOffsetBegin(charOffsetBegin); 
			pageToken.setCharacterOffsetEnd(charOffsetEnd); 

		}	
		
		return pageTokenMap;
	}
	
	public void addDependencies(DependencySet dset, Collection<TypedDependency> deps, Map<Integer, Node> pageTokenMap)
	{
		for (TypedDependency typedDep : deps) {
			
			GrammaticalRelation reln = typedDep.reln();
			String type = reln.toString();
			
			/*
			DependencyType depType = null;
			try {
				depType = DependencyType.valueOf(type.toUpperCase());
			}
			catch (IllegalArgumentException e) {
				log.warn("skipping dependency type: " + type);
				continue;  
			}
			*/						
			
			Dependency pageDep = dset.createDependency();
			//pageDep.setType_(depType.name().toLowerCase());
			pageDep.setType_(type);
			
			//log.info("type: " + type);
			CoreLabel gov = typedDep.gov().label();
			int govIndex = gov.get(IndexAnnotation.class);
			Node label  = pageTokenMap.get(govIndex);
			if (label != null) { // token not mapped as useless punctuation, etc...
				WordRelation governor = pageDep.createRelation();
				governor.setRelationType(WordRelationType.GOVERNOR.getInstanceName());
				governor.setNode(label);
			}
			
			CoreLabel dep = typedDep.dep().label();
			int depIndex = dep.get(IndexAnnotation.class);
			label  = pageTokenMap.get(depIndex);
			if (label != null) {// token not mapped as useless punctuation, etc..
				WordRelation dependent = pageDep.createRelation();
				dependent.setRelationType(WordRelationType.DEPENDENT.getInstanceName());
				dependent.setNode(label);
			}
		}
		
	}

	public boolean onlyAlphaAndPunctuation(String s) {
		char[] array = s.toCharArray();
		for (char ch : array)
			if (!(Character.isLetter(ch) || isPunctuation(ch) || Character.isSpace(ch)))
				return false;		
		return true;
	}
	
	public static boolean isPunctuation(char c) {
        return c == ','
            || c == '.'
            || c == '!'
            || c == '?'
            || c == ':'
            || c == ';'
            ;
    }
			
	public int alphaCharCount(String s) {
		int result = 0;
		char[] array = s.toCharArray();
		for (char ch : array)
			if (Character.isLetter(ch))
				result++;		
		return result;
	}
}
