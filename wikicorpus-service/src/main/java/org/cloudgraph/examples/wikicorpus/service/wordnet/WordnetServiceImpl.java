package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.wordnet.Lexlinks;
import org.cloudgraph.examples.corpus.wordnet.Semlinks;
import org.cloudgraph.examples.corpus.wordnet.Senses;
import org.cloudgraph.examples.corpus.wordnet.Synsets;
import org.cloudgraph.examples.corpus.wordnet.Words;
import org.cloudgraph.examples.corpus.wordnet.query.QLexlinks;
import org.cloudgraph.examples.corpus.wordnet.query.QSemlinks;
import org.cloudgraph.examples.corpus.wordnet.query.QSenses;
import org.cloudgraph.examples.corpus.wordnet.query.QWords;
import org.plasma.config.DataAccessProviderName;
import org.plasma.query.Expression;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.PojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class WordnetServiceImpl implements WordnetService {
	private static Log log = LogFactory.getLog(WordnetServiceImpl.class);
	protected SDODataAccessClient service;
	protected DataAccessProviderName provider;

	public WordnetServiceImpl() {
		this.provider = DataAccessProviderName.HBASE;
		this.service = new SDODataAccessClient(new PojoDataAccessClient(this.provider));
	}
	
	public WordnetServiceImpl(DataAccessProviderName provider) {
		this.provider = provider;
		this.service = new SDODataAccessClient(new PojoDataAccessClient(this.provider));
	}
	
	@Override
	public DataAccessProviderName getProvider() {
		return this.provider;
	}

	@Override
	public Wordnet getAllRelations(String lemma) {
		return getAllRelations(lemma, 0);
	}
		
	@Override
	public Wordnet getAllRelations(String lemma, long wordid) {		
		List<WordRelations> relations = new ArrayList<WordRelations>();		
		Query query = createRelationQuery(lemma, wordid);
		DataGraph[] graphs = this.service.find(query);
		Words word = this.getWord(graphs, lemma, wordid);
		if (word == null)
			return null;
		
		int index = 1;
		for (Senses sense : word.getSenses()) {
			WordRelations wordRelations = new WordRelations(sense, index);
			
			Map<String, List<SemanticRelation>> semanticRelations = new HashMap<String, List<SemanticRelation>>();
			if (sense.getSynsets().getSemlinksCount() > 0)
				addSemanticLinks(word, sense, sense.getSynsets().getSemlinks(), semanticRelations);
			if (sense.getSynsets().getSemlinks1Count() > 0)
				addSemanticLinks(word, sense, sense.getSynsets().getSemlinks1(), semanticRelations);
			
			wordRelations.setSemanticRelations(semanticRelations);
			
			Map<String, List<LexicalRelation>> lexicalRelations = new HashMap<String, List<LexicalRelation>>();
			if (sense.getSynsets().getLexlinksCount() > 0)
				addLexicalLinks(word, sense, sense.getSynsets().getLexlinks(), lexicalRelations);
			if (sense.getSynsets().getLexlinks1Count() > 0)
				addLexicalLinks(word, sense, sense.getSynsets().getLexlinks1(), lexicalRelations);
			wordRelations.setLexicalRelations(lexicalRelations);
			
			relations.add(wordRelations);
			index++;
		}
		
		return new Wordnet(word, relations);
	}
	
	@Override
	public Wordnet getSynonyms(String lemma) {
		return getSynonyms(lemma, 0);
	}
	
	@Override
	public Wordnet getSynonyms(String lemma, long wordid) {
		List<WordRelations> relations = new ArrayList<WordRelations>();		
		Query query = createSynonymQuery(lemma, wordid);
		DataGraph[] graphs = this.service.find(query);
		Words word = this.getWord(graphs, lemma, 0);
		if (word == null)
			return null;
		
		int index = 1;
		for (Senses sense : word.getSenses()) {
			WordRelations wordRelations = new WordRelations(sense, index);
			
			Map<String, List<SemanticRelation>> semanticRelations = new HashMap<String, List<SemanticRelation>>();
			if (sense.getSynsets().getSemlinksCount() > 0)
				addSemanticLinks(word, sense, sense.getSynsets().getSemlinks(), semanticRelations);
			if (sense.getSynsets().getSemlinks1Count() > 0)
				addSemanticLinks(word, sense, sense.getSynsets().getSemlinks1(), semanticRelations);
			
			wordRelations.setSemanticRelations(semanticRelations);
			
			Map<String, List<LexicalRelation>> lexicalRelations = new HashMap<String, List<LexicalRelation>>();
			if (sense.getSynsets().getLexlinksCount() > 0)
				addLexicalLinks(word, sense, sense.getSynsets().getLexlinks(), lexicalRelations);
			if (sense.getSynsets().getLexlinks1Count() > 0)
				addLexicalLinks(word, sense, sense.getSynsets().getLexlinks1(), lexicalRelations);
			wordRelations.setLexicalRelations(lexicalRelations);
			
			relations.add(wordRelations);
			index++;
		}
		
		return new Wordnet(word, relations);
		
	}
	
	private Words getWord(DataGraph[] graphs, String lemma, long wordid) {
		for (DataGraph graph : graphs) {
			if (log.isDebugEnabled())
			try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			Words word = (Words) graph.getRootObject();
			if (!word.getLemma().equals(lemma)) {
				log.debug("ignoring lemma '" + word.getLemma() + "' for query lemma '" + lemma + "'");
				continue;
			}
			return word;
		}
		return null;
	}
	
	private void addSemanticLinks(Words word, Senses sense, Semlinks[] links, Map<String, List<SemanticRelation>> semanticRelations)
	{
		for (Semlinks link : links) {
			String linkType = link.getLinktypes().getLink();
			if (linkType == null)
				linkType = "unknown";
			List<SemanticRelation> list = semanticRelations.get(linkType);
			if (list == null) {
				list = new ArrayList<SemanticRelation>();
				semanticRelations.put(linkType, list);
			}
			if (link.getSynsets() != null && link.getSynsets().getSenses() != null) {
				List<Senses> rightSenses = new ArrayList<Senses>();
				for (Senses right : link.getSynsets().getSenses()) {
					if (log.isDebugEnabled())
					    log.debug("SemanticLink: " + right.getWords().getLemma());
					if (!right.getWords().getLemma().equals(word.getLemma()) && !rightSenses.contains(right))
					    rightSenses.add(right);
					else if (log.isDebugEnabled())
						log.debug("ignoring semantic sense target as root sence " + right.toString());
				}
				if (rightSenses.size() > 0) {
				    SemanticRelation relation = new SemanticRelation(link, link.getSynsets(), rightSenses);
				    list.add(relation);
				}
			}
			if (link.getSynsets1() != null && link.getSynsets1().getSenses() != null) {
				List<Senses> rightSenses = new ArrayList<Senses>();
				for (Senses right : link.getSynsets1().getSenses()) {
					if (log.isDebugEnabled())
					    log.debug("SemanticLink1: " + right.getWords().getLemma());
					if (!right.getWords().getLemma().equals(word.getLemma()) && !rightSenses.contains(right))
					    rightSenses.add(right);
					else if (log.isDebugEnabled())
						log.debug("ignoring semantic sense target as root sence " + right.toString());
				}
				if (rightSenses.size() > 0) {
				    SemanticRelation relation = new SemanticRelation(link, link.getSynsets1(), rightSenses);
				    list.add(relation);
				}
			}
		}
		
		List<String> toRemove = new ArrayList<String>();
		Iterator<String> iter = semanticRelations.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			List<SemanticRelation> list = semanticRelations.get(key);
			if (list.size() == 0)
				toRemove.add(key);
		}
		for (String key: toRemove)
			semanticRelations.remove(key);
	}
	
	private void addLexicalLinks(Words word, Senses sense, Lexlinks[] links, Map<String, List<LexicalRelation>> lexicalRelations)
	{
		for (Lexlinks link : links) {
			String linkType = link.getLinktypes().getLink();
			if (linkType == null)
				linkType = "unknown";
			List<LexicalRelation> list = lexicalRelations.get(linkType);
			if (list == null) {
				list = new ArrayList<LexicalRelation>();
				lexicalRelations.put(linkType, list);
			}
			if (link.getSynsets() != null && link.getSynsets().getSenses() != null) {
				List<Senses> rightSenses = new ArrayList<Senses>();
				for (Senses right : link.getSynsets().getSenses()) {
					if (log.isDebugEnabled())
					    log.debug("LexicalLink: " + right.getWords().getLemma());
					if (!right.getWords().getLemma().equals(word.getLemma()) && !rightSenses.contains(right))
					    rightSenses.add(right);
					else if (log.isDebugEnabled())
						log.debug("ignoring lexical sense target as root sence " + right.toString());
				}
				if (rightSenses.size() > 0) {
					LexicalRelation relation = new LexicalRelation(link, link.getSynsets(), rightSenses);
				    list.add(relation);
				}
			}
			if (link.getSynsets1() != null && link.getSynsets1().getSenses() != null) {
				List<Senses> rightSenses = new ArrayList<Senses>();
				for (Senses right : link.getSynsets1().getSenses()) {
					if (log.isDebugEnabled())
					    log.debug("LexicalLink2: " + right.getWords().getLemma());
					if (!right.getWords().getLemma().equals(word.getLemma()) && !rightSenses.contains(right))
					    rightSenses.add(right);
					else
						log.debug("ignoring lexical sense target as root sence " + right.toString());
				}
				if (rightSenses.size() > 0) {
					LexicalRelation relation = new LexicalRelation(link, link.getSynsets1(), rightSenses);
				    list.add(relation);
				}
			}
		}
		
		List<String> toRemove = new ArrayList<String>();
		Iterator<String> iter = lexicalRelations.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			List<LexicalRelation> list = lexicalRelations.get(key);
			if (list.size() == 0)
				toRemove.add(key);
		}
		for (String key: toRemove)
			lexicalRelations.remove(key);
	}
	
	private Query createSynonymQuery(String lemma, long wordid) {
		QWords query = QWords.newQuery();
		QSemlinks semlink = QSemlinks.newQuery();
		Expression predicate = semlink.linktypes().link().eq("similar");
		
		query.select(query.wildcard())
		    .select(query.senses().wildcard())
		    .select(query.senses().synsets().wildcard())
		    .select(query.senses().synsets().samples().wildcard())
		    .select(query.senses().synsets().adjpositions().wildcard())		    
		    // semantic relations
		    .select(query.senses().synsets().semlinks(predicate).wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).linktypes().wildcard())  		    
		    .select(query.senses().synsets().semlinks(predicate).synsets().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets().samples().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets().senses().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets1().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets1().samples().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets1().senses().wildcard())  
		    .select(query.senses().synsets().semlinks(predicate).synsets1().senses().words().wildcard())  
		;
		if (wordid > 0)
		    query.where(query.lemma().eq(lemma).and(query.wordid().eq(wordid)));
		else
			query.where(query.lemma().eq(lemma));
		
		return query;
	}
	
	private Query createRelationQuery(String lemma, long wordid) {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		    .select(query.casedwords().wildcard())
		    .select(query.senses().wildcard())
		    .select(query.senses().synsets().wildcard())
		    .select(query.senses().synsets().samples().wildcard())
		    .select(query.senses().synsets().adjpositions().wildcard())		    
		    // semantic relations
		    .select(query.senses().synsets().semlinks().wildcard())  
		    .select(query.senses().synsets().semlinks().linktypes().wildcard())  		    
		    .select(query.senses().synsets().semlinks().synsets().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().words().wildcard())  
		    // lexical relations
		    .select(query.senses().synsets().lexlinks().wildcard())  
		    .select(query.senses().synsets().lexlinks().linktypes().wildcard())
		    .select(query.senses().synsets().lexlinks().synsets().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().words().wildcard())  
		;
		if (wordid > 0)
		    query.where(query.lemma().eq(lemma).and(query.wordid().eq(wordid)));
		else
			query.where(query.lemma().eq(lemma));
		
		return query;
	}

	private List<Senses> getLeftSenses(String lemma, long wordid) {
		QWords query = QWords.newQuery();
		query.select(query.wildcard()).select(query.casedwords().wildcard())
				.select(query.senses().wildcard())
				.select(query.senses().synsets().wildcard())
				.select(query.senses().synsets().lexdomains().wildcard())
				.select(query.senses().synsets().adjpositions().wildcard());
		
		if (wordid > 0)
		    query.where(query.lemma().eq(lemma).and(query.wordid().eq(wordid)));
		else
			query.where(query.lemma().eq(lemma));

		List<Senses> result = new ArrayList<Senses>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			if (log.isDebugEnabled())
			try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			Words word = (Words) graph.getRootObject();
			if (word.getSensesCount() > 0)
				for (Senses sence : word.getSenses()) {
					result.add(sence);
				}
		}
		return result;
	}

	private Map<String, List<Semlinks>> getSemanticLinks(Synsets synset) {
		QSemlinks query = QSemlinks.newQuery();
		query.select(query.wildcard()).select(query.linktypes().wildcard())
				.select(query.synsets().wildcard())
				.select(query.synsets().samples().wildcard())
				.select(query.synsets().senses().wildcard()) // link is broken
																// between
																// synsets->senses
																// to get back
																// to Words
		// .select(query.synsets1().wildcard())
		;
		// FIXME: Where properties getting into query
		query.where(query.synsets1().synsetid().eq(synset.getSynsetid()));

		Map<String, List<Semlinks>> result = new HashMap<String, List<Semlinks>>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			if (log.isDebugEnabled())
			try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			Semlinks link = (Semlinks) graph.getRootObject();
			List<Semlinks> list = result.get(link.getLinktypes().getLink());
			if (list == null) {
				list = new ArrayList<Semlinks>();
				result.put(link.getLinktypes().getLink(), list);
			}
			list.add(link);
		}
		return result;
	}

	private Map<String, List<Lexlinks>> getLexicalLinks(Synsets synset) {
		QLexlinks query = QLexlinks.newQuery();
		query.select(query.wildcard()).select(query.linktypes().wildcard())
				.select(query.synsets().wildcard())
				.select(query.synsets().samples().wildcard())
				.select(query.synsets().senses().wildcard()) // link is broken
																// between
																// synsets->senses
																// to get back
																// to Words
		// .select(query.synsets1().wildcard())
		;
		// FIXME: Where properties getting into query
		query.where(query.synsets1().synsetid().eq(synset.getSynsetid()));

		Map<String, List<Lexlinks>> result = new HashMap<String, List<Lexlinks>>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			if (log.isDebugEnabled())
			try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			Lexlinks link = (Lexlinks) graph.getRootObject();
			List<Lexlinks> list = result.get(link.getLinktypes().getLink());
			if (list == null) {
				list = new ArrayList<Lexlinks>();
				result.put(link.getLinktypes().getLink(), list);
			}
			list.add(link);
		}
		return result;
	}

	private List<Senses> getSenses(Synsets synset) {
		QSenses query = QSenses.newQuery();
		query.select(query.wildcard()).select(query.words().wildcard())
				.select(query.synsets().wildcard());
		// FIXME: Where properties getting into query
		query.where(query.synsets().synsetid().eq(synset.getSynsetid()));

		List<Senses> result = new ArrayList<Senses>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			if (log.isDebugEnabled())
			try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			Senses senses = (Senses) graph.getRootObject();
			result.add(senses);
		}
		return result;
	}

	private String serializeGraph(DataGraph graph) throws IOException {
		DefaultOptions options = new DefaultOptions(graph.getRootObject()
				.getType().getURI());
		options.setRootNamespacePrefix("wordnet");

		XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(
				graph.getRootObject(),
				graph.getRootObject().getType().getURI(), null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PlasmaXMLHelper.INSTANCE.save(doc, os, options);
		os.flush();
		os.close();
		String xml = new String(os.toByteArray());
		return xml;
	}

}
