package org.cloudgraph.examples.wikicorpus.service.corpus;

import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.parse.Node;

public class GovernorSentence extends Sentence {
	private Governor governor;
	protected GovernorSentence(Governor governor) {
		super(governor.getDependency(), governor.getNode());
		this.governor = governor;
	}

}
