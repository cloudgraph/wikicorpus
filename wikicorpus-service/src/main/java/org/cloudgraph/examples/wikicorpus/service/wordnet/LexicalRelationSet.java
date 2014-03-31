package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.List;

public class LexicalRelationSet extends RelationSet {
    private List<LexicalRelation> links;
	public LexicalRelationSet(List<LexicalRelation> links, String type) {
		super(type);
		this.links = links;
	}
	public List<LexicalRelation> getLinks() {
		return links;
	}
	public void setLinks(List<LexicalRelation> links) {
		this.links = links;
	}
   
}
