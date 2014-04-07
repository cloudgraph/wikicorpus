package org.cloudgraph.examples.wikicorpus.web.model.cache;

import org.cloudgraph.examples.corpus.search.ParsedWordAggregate;


public class ParsedWordAdapter {
    private ParsedWordAggregate word;
    private int hitCount = 0;

	public ParsedWordAdapter(ParsedWordAggregate word) {
		super();
		this.word = word;
	}

	public ParsedWordAggregate getWord() {
		return word;
	}

	public void setWord(ParsedWordAggregate word) {
		this.word = word;
	}
	    
	public String getLemma() {
		return word.getLemma();
	}

	public String toString() {
		return this.word.getLemma();
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}
	
	
}
