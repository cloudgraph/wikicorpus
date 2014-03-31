package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudgraph.examples.corpus.wordnet.Senses;
import org.cloudgraph.examples.corpus.wordnet.Synsets;

public class WordRelations {
    private Senses sense;
    private int senseIndex;
    private Map<String, List<SemanticRelation>> semanticRelations;
    private Map<String, List<LexicalRelation>> lexicalRelations;
	public WordRelations(Senses sense, int senseIndex) {
		super();
		this.sense = sense;
		this.senseIndex = senseIndex;
	}
	public WordRelations(Senses sense, int senseIndex,
			Map<String, List<SemanticRelation>> semanticRelations,
			Map<String, List<LexicalRelation>> lexicalRelations) {
		super();
		this.sense = sense;
		this.senseIndex = senseIndex;
		this.semanticRelations = semanticRelations;
		this.lexicalRelations = lexicalRelations;
	}
	public int getSenseIndex() {
		return senseIndex;
	}
	public Senses getSense() {
		return sense;
	}
	
	public Synsets getSynset() {
		return this.sense.getSynsets();
	}
	
	public Map<String, List<SemanticRelation>> getSemanticRelations() {
		return semanticRelations;
	}
	public void setSemanticRelations(
			Map<String, List<SemanticRelation>> semanticRelations) {
		this.semanticRelations = semanticRelations;
	}
	public Map<String, List<LexicalRelation>> getLexicalRelations() {
		return lexicalRelations;
	}
	public void setLexicalRelations(
			Map<String, List<LexicalRelation>> lexicalRelations) {
		this.lexicalRelations = lexicalRelations;
	}
	
	public List<SemanticRelationSet> getSemanticRelationSets() {
		List<SemanticRelationSet> result = new ArrayList<SemanticRelationSet>();
		Iterator<String> iter = this.semanticRelations.keySet().iterator();
		while (iter.hasNext()) {
			String type = iter.next();
			List<SemanticRelation> list = this.semanticRelations.get(type);
			result.add(new SemanticRelationSet(list, type));
		}
		return result;
	}	

	public List<LexicalRelationSet> getLexicalRelationSets() {
		List<LexicalRelationSet> result = new ArrayList<LexicalRelationSet>();
		Iterator<String> iter = this.lexicalRelations.keySet().iterator();
		while (iter.hasNext()) {
			String type = iter.next();
			List<LexicalRelation> list = this.lexicalRelations.get(type);
			result.add(new LexicalRelationSet(list, type));
		}
		return result;
	}	
	
	public String getPos() {
		String pos = this.sense.getSynsets().getPos();
		if (pos.equals("n"))
			return "Noun";
		else if (pos.equals("v"))
			return "Verb";
		else if (pos.equals("a"))
			return "Adjective";
		else if (pos.equals("r"))
			return "Adverb";
		else if (pos.equals("s"))
			return "Adjective Satellite";
		else
			return "unknown";
	}
	
	public String getDefinition() {
		return this.sense.getSynsets().getDefinition();
	}
}
