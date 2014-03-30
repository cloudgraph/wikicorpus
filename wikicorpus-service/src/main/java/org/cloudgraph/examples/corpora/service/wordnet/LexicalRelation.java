package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.List;

import org.cloudgraph.examples.corpus.wordnet.Lexlinks;
import org.cloudgraph.examples.corpus.wordnet.Samples;
import org.cloudgraph.examples.corpus.wordnet.Senses;
import org.cloudgraph.examples.corpus.wordnet.Synsets;

public class LexicalRelation {
	private Lexlinks link;
	private Synsets synset;
	private List<Senses> senses;
	public LexicalRelation(Lexlinks link, Synsets synset, List<Senses> senses) {
		super();
		this.link = link;
		this.synset = synset;
		this.senses = senses;
	}
	public Lexlinks getLink() {
		return link;
	}
	public void setLink(Lexlinks link) {
		this.link = link;
	}
	public List<Senses> getSenses() {
		return senses;
	}
	public void setSenses(List<Senses> senses) {
		this.senses = senses;
	}
	
	public Samples[] getSamples()
	{
		return this.synset.getSamples();
	}
	
	public boolean getHasSamples() {
		return this.synset.getSamplesCount() > 0;
	}
	public Synsets getSynset() {
		return synset;
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
