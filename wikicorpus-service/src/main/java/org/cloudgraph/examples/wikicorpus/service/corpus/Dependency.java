package org.cloudgraph.examples.wikicorpus.service.corpus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudgraph.examples.corpus.search.WordDependency;

public class Dependency {
    //private WordDependency delegate;
	private Dependency parent;
	private List<Dependency> dependencies;
	
	private String depTypeName;
	private String lemma;
	private int governorCount;
	private int dependentCount;

	
	private static Map<String, String> names = new HashMap<String, String>();
	private static Map<String, String> definitions = new HashMap<String, String>();
	private static Map<String, String> parents = new HashMap<String, String>();
	{
		names.put("dep", "Dependency");

		//aux
		names.put("aux", "Auxiliary");
		names.put("auxpass", "Passive auxiliary");
		parents.put("auxpass", "aux");
		names.put("cop", "Copula");
		parents.put("cop", "aux");
		
		//arg
		names.put("arg", "Argument");
		//arg-agent
		names.put("agent", "Agent");
		parents.put("agent", "arg");
		
		//arg-comp
		names.put("comp", "Complement");
		parents.put("comp", "arg");		
		names.put("acomp", "Adjectival Complement");
		parents.put("acomp", "arg");
		names.put("ccomp", "Clausal complement with internal subject");
		parents.put("ccomp", "arg");
		names.put("xcomp", "Clausal complement with external subject");
		parents.put("xcomp", "arg");
		names.put("obj", "Object");
		parents.put("obj", "arg");		
		names.put("dobj", "Direct Object");
		parents.put("dobj", "arg");
		names.put("iobj", "Indirect Object");
		parents.put("iobj", "arg");
		names.put("pobj", "Object of Preposition");
		parents.put("pobj", "arg");
		names.put("pcomp", "Prepositional Complement");
		parents.put("pcomp", "arg");
		
		//arg-subj
		names.put("subj", "Subject");
		parents.put("subj", "arg");		
		names.put("nsubj", "Nominal Subject");
		parents.put("nsubj", "arg");		
		names.put("nsubjpass", "Passive Nominal Subject");
		parents.put("nsubjpass", "arg");		
		names.put("csubj", "Clausal Subject");
		parents.put("csubj", "arg");		
		names.put("csubjpass", "Passive Clausal Subject");
		parents.put("csubjpass", "arg");		
		
		
		names.put("cc", "Coordination");
		parents.put("cc", "dep");
		names.put("conj", "Conjunct");
		parents.put("conj", "dep");
		names.put("expl", "Expletive");
		parents.put("expl", "dep");
		
		//acomp
		names.put("acomp", "Adjectival Complement");		
		definitions.put("acomp", "An adjectival complement of a verb is an adjectival phrase which functions as the complement (like an object of the verb)");		
		parents.put("acomp", "dep");
		
		
		
		//mod
		names.put("mod", "Modifier");
		
		names.put("advcl", "Adverbial Clause Modifier");		
		definitions.put("advcl", "An adverbial clause modifier of a VP or S is a clause modifying the verb (temporal clause, consequence, conditional clause, purpose clause, etc.)");		 
		parents.put("advcl", "mod");
		
		names.put("advmod", "Adverbial Modifier");		
		definitions.put("advmod", "An adverbial modifier of a word is a (non-clausal) adverb or adverbial phrase (ADVP) that serves to modify the meaning of the word.");		 
		parents.put("advmod", "mod");		
		names.put("neg", "negation modifier");
		parents.put("neg", "mod");

		names.put("amod", "Adjectival Modifier");
		definitions.put("amod", "An adjectival modifier of an NP is any adjectival phrase that serves to modify the meaning of the NP.");
		parents.put("amod", "mod");
		
		names.put("appos", "Appositional Modifier");
		definitions.put("appos", "An appositional modifier of an NP is an NP immediately to the right of the first NP that serves to define or modify that NP");
		parents.put("appos", "mod");
		
		names.put("det", "Determiner");
		parents.put("det", "mod");
		names.put("predet", "Predeterminer");
		parents.put("predet", "mod");
		names.put("preconj", "Preconjunct");
		parents.put("preconj", "mod");
		names.put("vmod", "Reduced, Non-finite Verbal Modifier");
		parents.put("vmod", "mod");
		names.put("mwe", "Multi-Word Expression Modifier");
		parents.put("mwe", "mod");		
		names.put("mark", "Marker");
		parents.put("mark", "mod");
	    names.put("rcmod", "Relative Clause Modifier");
		parents.put("rcmod", "mod");		
	    names.put("quantmod", "Quantifier Modifier");
		parents.put("quantmod", "mod");		
	    names.put("nn", "Noun Compound Modifier");
		parents.put("nn", "mod");		
	    names.put("npadvmod", "Noun Phrase Adverbial Modifier");
		parents.put("npadvmod", "mod");		
	    names.put("tmod", "Temporal Modifier");
		parents.put("tmod", "mod");		
	    names.put("num", "Numeric Modier");
		parents.put("num", "mod");		
	    names.put("number", "Element of Compound Number");
		parents.put("number", "mod");		
	    names.put("prep", "Prepositional Modifier");
		parents.put("prep", "mod");			
		names.put("prepc", "Prepositional Clausal Modifier");
		parents.put("prepc", "mod");	
	    names.put("poss", "Possession Modifier");
		parents.put("poss", "mod");		
	    names.put("possessive", "Possessive Modifier ('s)");
		parents.put("possessive", "mod");		
	    names.put("prt", "Phrasal Verb Particle");				
		parents.put("prt", "mod");		
		names.put("infmod", "Info Modifier");
		parents.put("infmod", "mod");	
		names.put("partmod", "Partial Modifier");
		parents.put("partmod", "mod");	
		
		
		//conj
		names.put("conj", "Conjunct");
		definitions.put("conj", "The relation between two elements connected by a coordinating conjunction");
	
		names.put("conj_and", "Conjunctive And");
		parents.put("conj_and", "conj");
		names.put("conj_but", "Conjunctive But");
		parents.put("conj_but", "conj");
		names.put("conj_nor", "Conjunctive Nor");
		parents.put("conj_nor", "conj");
		names.put("conj_or", "Conjunctive Or");
		parents.put("conj_or", "conj");				
		
		
		names.put("parataxis", "Parataxis");
		parents.put("parataxis", "dep");	
		names.put("punct", "Punctuation");
		parents.put("punct", "dep");	
		names.put("ref", "Referent");
		parents.put("ref", "dep");	
		names.put("sdep", "Semantic Dependent");
		parents.put("sdep", "dep");	
		names.put("xsubj", "Controlling Subject");
		parents.put("xsubj", "dep");	
		names.put("attr", "Attribute");
		parents.put("attr", "dep");	
		
	}
	
	public Dependency(WordDependency delegate, Dependency parent) {
		super();
		this.depTypeName = delegate.getDependencyType();
		this.lemma = delegate.getLemma();
		this.governorCount = delegate.getGovernorCount();
		this.dependentCount = delegate.getDependentCount();
		this.parent = parent;
		if (this.parent != null)
		    if (!this.parent.getDependencies().contains(this))
			    this.parent.getDependencies().add(this);
	}	
	
	public Dependency(String depTypeName, String lemma, int governorCount,
			int dependentCount, Dependency parent) {
		super();
		this.depTypeName = depTypeName;
		this.lemma = lemma;
		this.governorCount = governorCount;
		this.dependentCount = dependentCount;
		this.parent = parent;
		if (this.parent != null)
		    if (!this.parent.getDependencies().contains(this))
			    this.parent.getDependencies().add(this);
	}

	public String getDepTypeName() {
		return depTypeName;
	}

	public void setDepTypeName(String depTypeName) {
		this.depTypeName = depTypeName;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public void setGovernorCount(int governorCount) {
		this.governorCount = governorCount;
	}

	public void setDependentCount(int dependentCount) {
		this.dependentCount = dependentCount;
	}

	public static String getDisplayableType(String type) {
		String result = null;
		if (type == null)
			return null;
		int idx = type.indexOf("_");
		if (idx < 0) {
			result = names.get(type);
		}
		else {
			String root = type.substring(0, idx);
			String displayRoot = names.get(root);
			String suffix = type.substring(idx+1);
			result = displayRoot + " (" + suffix + ")";
		}
		if (result != null)
			return result;
		else
			return type;
	}
	
	public String getDisplayType() {
		return getDisplayableType(this.depTypeName);
	}
	
	public static String getDefinition(WordDependency dep) {
		return definitions.get(dep.getDependencyType());
	}

	public static String getParentTypeName(WordDependency dep) {
		int idx = dep.getDependencyType().indexOf("_");
		if (idx < 0) {
		     return parents.get(dep.getDependencyType());
		}
		else {
			String root = dep.getDependencyType().substring(0, idx);
			return  parents.get(root);
		}
	}	
	
	public Dependency getParent() {
		return parent;
	}
	
	public void setParent(Dependency parent) {
		this.parent = parent;
	}
	
	public List<Dependency> getDependencies() {
		if (this.dependencies == null)
			this.dependencies = new ArrayList<Dependency>();
		return dependencies;
	}
	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public String getType() {
		return this.depTypeName;
	}
	
	public String getLemma() {
		return this.lemma;
	}
		
	public int getDependentCount() {
		return this.dependentCount;
	}
	
	public int getDependentCountDeep() {
		int total = this.dependentCount;
		if (this.dependencies != null)
		    for (Dependency child : this.dependencies)
		    	total += child.getDependentCountDeep();
		return total;
	}	
	
	public int getGovernorCount() {
		return this.governorCount;
	}
	
	public int getGovernorCountDeep() {
		int total = this.governorCount;
		if (this.dependencies != null)
		    for (Dependency child : this.dependencies)
		    	total += child.getGovernorCountDeep();
		return total;
	}	
	
	public static Comparator<Dependency> createComparator() {
		
		return new Comparator<Dependency>() {
			@Override
			public int compare(Dependency o1, Dependency o2) {
				return o2.getDisplayType().compareTo(o1.getDisplayType());
			}
			
		};
	}
	
	public String toString() {
		if (this.parent == null)
		    return this.lemma + ":" + this.depTypeName + "(" 
				+ this.governorCount + "/" + this.dependentCount + ")";
		else
		    return this.lemma + ":" + this.parent.getDepTypeName() + "/" + this.depTypeName + "(" 
				+ this.governorCount + "/" + this.dependentCount + ")";
	}
	
}
