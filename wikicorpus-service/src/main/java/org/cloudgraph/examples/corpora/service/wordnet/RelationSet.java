package org.cloudgraph.examples.wikicorpus.service.wordnet;

import java.util.HashMap;
import java.util.Map;

public abstract class RelationSet {
	protected static Map<String, String> typeNames;
	protected String type;

	public RelationSet(String type) {
		super();
		this.type = type;
		init();
	}
	public String getType() {
		return type;
	}
	
	public String getLinkTypeName() {
		return typeNames.get(this.type);
	}
	
	private void init()
	{
		if (typeNames != null)
			return;
		typeNames = new HashMap<String, String>();
		typeNames.put("hypernym", "Hypernyms");
		typeNames.put("hyponym", "Hyponyms");
		typeNames.put("instance hypernym", "Instance Hypernyms");
		typeNames.put("instance hyponym", "Instance Hyponyms");
		typeNames.put("part holonym", "Part Holonyms");
		typeNames.put("part meronym", "Part Meronyms");
		typeNames.put("member holonym", "Member Holonyms");
		typeNames.put("member meronym", "Member Meronyms");
		typeNames.put("substance holonym", "Substance Holonyms");
		typeNames.put("substance meronym", "Substance Heronyms");
		typeNames.put("entail", "Entails");
		typeNames.put("cause", "Causes");
		typeNames.put("antonym", "Antonyms");
		typeNames.put("similar", "Synonyms");
		typeNames.put("also", "See Also");
		typeNames.put("attribute", "Attributes");
		typeNames.put("verb group", "Verb Groups");
		typeNames.put("participle", "Participles");
		typeNames.put("pertainym", "Pertainyms");
		typeNames.put("derivation", "Derivations");
		typeNames.put("domain category", "Domain Categories");
		typeNames.put("domain member category", "Domain Member Categoryies");
		typeNames.put("domain region", "Domain Regions");
		typeNames.put("domain member region", "Domain Member Regions");
		typeNames.put("domain usage", "Domain Usages");
		typeNames.put("domain member usage", "Domain Member Usages");
		typeNames.put("domain", "Domains");
		typeNames.put("member", "Members");

	}

}
