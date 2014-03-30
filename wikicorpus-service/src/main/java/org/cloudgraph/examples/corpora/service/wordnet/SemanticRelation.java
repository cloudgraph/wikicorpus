package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.List;

import org.cloudgraph.examples.corpus.wordnet.Samples;
import org.cloudgraph.examples.corpus.wordnet.Semlinks;
import org.cloudgraph.examples.corpus.wordnet.Senses;
import org.cloudgraph.examples.corpus.wordnet.Synsets;

public class SemanticRelation {
	private Semlinks link;
	private Synsets synset;
	private List<Senses> senses;
	public SemanticRelation(Semlinks link, Synsets synset, List<Senses> senses) {
		super();
		this.link = link;
		this.synset = synset;
		this.senses = senses;
	}
	public Semlinks getLink() {
		return link;
	}
	
	public List<Senses> getSenses() {
		return senses;
	}
	
	public Synsets getSynset() {
		return synset;
	}
	
	public Samples[] getSamples()
	{
		return this.synset.getSamples();
	}
	
	public boolean getHasSamples() {
		return this.synset.getSamplesCount() > 0;
	}
	
	public String getPos() {
		String pos = this.synset.getPos();
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
		return this.synset.getDefinition();
	}
	
	
}
