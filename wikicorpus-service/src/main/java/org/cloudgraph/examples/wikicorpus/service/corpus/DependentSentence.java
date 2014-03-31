package org.cloudgraph.examples.wikicorpus.service.corpus;

import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.Dependent;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.parse.Node;

public class DependentSentence extends Sentence {
	private Dependent dependent;
	protected DependentSentence(Dependent dependent) {
		super(dependent.getDependency(), dependent.getNode());
		this.dependent = dependent;
	}

}
