package org.cloudgraph.examples.wikicorpus.web.model.cache;

import org.cloudgraph.examples.corpus.wordnet.Words;


public class WordAdapter {
    private Words word;

	public WordAdapter(Words word) {
		super();
		this.word = word;
	}

	public Words getWord() {
		return word;
	}

	public void setWord(Words word) {
		this.word = word;
	}
	    
	public String getLemma() {
		return word.getLemma();
	}

	public int getWordid() {
		return word.getWordid();
	}

	public String toString() {
		return this.word.getLemma();
	}
}
