package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.List;

public class SemanticRelationSet extends RelationSet {
	private List<SemanticRelation> links;

	public SemanticRelationSet(List<SemanticRelation> links, String type) {
		super(type);
		this.links = links;
	}

	public List<SemanticRelation> getLinks() {
		return links;
	}

	public void setLinks(List<SemanticRelation> links) {
		this.links = links;
	}
	

}
