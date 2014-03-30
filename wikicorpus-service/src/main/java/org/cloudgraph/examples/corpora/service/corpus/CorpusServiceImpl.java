package org.cloudgraph.examples.wikicorpus.service.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.cloudgraph.examples.corpus.parse.Dependent;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.parse.query.QDependent;
import org.cloudgraph.examples.corpus.parse.query.QGovernor;
import org.cloudgraph.examples.corpus.search.WordDependency;
import org.cloudgraph.examples.corpus.search.query.QWordDependency;
import org.plasma.config.DataAccessProviderName;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.DataAccessClient;
import org.plasma.sdo.access.client.PojoDataAccessClient;

import commonj.sdo.DataGraph;

public class CorpusServiceImpl implements CorpusService {
	private static Log log = LogFactory.getLog(CorpusServiceImpl.class);
    private DataAccessClient service; 	
    public CorpusServiceImpl() {
    	this.service = new PojoDataAccessClient(DataAccessProviderName.HBASE);
     }

	@Override
	public List<Dependency> findDependencies(String word) {
		Query query = createDependencyQuery(word); 
		DataGraph[] graphs = this.service.find(query);
		
		HashSet<WordDependency> allDeps = new HashSet<WordDependency>();
		for (DataGraph graph : graphs) {
			WordDependency dep = (WordDependency)graph.getRootObject();
			allDeps.add(dep);
		}
		
		Dependency root = new Dependency("root", word, 0, 0, null);
		
		WordDependency[] a = new WordDependency[allDeps.size()];
		allDeps.toArray(a);
		Map<String, Dependency> level1 = new HashMap<String, Dependency>();
		for (WordDependency dep : a) {
			String parentType = Dependency.getParentTypeName(dep);
			if (parentType == null) {
				level1.put(dep.getDependencyType(), new Dependency(dep, root));
				if (!allDeps.remove(dep))
					log.warn("cant remove dep");
			}
		}
		
		if (level1.get("mod") == null) {
			Dependency syntheticRoot = new Dependency("mod", word, 0, 0, root);
			level1.put("mod", syntheticRoot);
		}
		if (level1.get("arg") == null) {
			Dependency syntheticRoot = new Dependency("arg", word, 0, 0, root);
			level1.put("arg", syntheticRoot);
		}
		if (level1.get("aux") == null) {
			Dependency syntheticRoot = new Dependency("aux", word, 0, 0, root);
			level1.put("aux", syntheticRoot);
		}		
		
		Map<String, Dependency> level2 = new HashMap<String, Dependency>();
		a = new WordDependency[allDeps.size()];
		allDeps.toArray(a);
		for (WordDependency dep : a) {
			String parentType = Dependency.getParentTypeName(dep);
			Dependency parent = level1.get(parentType);
			if (parent != null) {
				Dependency child = new Dependency(dep, parent);
				if (!allDeps.remove(dep))
					log.warn("cant remove dep");
				level2.put(dep.getDependencyType(), child);
			}
		}
		
		a = new WordDependency[allDeps.size()];
		allDeps.toArray(a);
		for (WordDependency dep : a) {
			String parentType = Dependency.getParentTypeName(dep);
			Dependency parent = level2.get(parentType);
			if (parent != null) {
				Dependency child = new Dependency(dep, parent);
				if (!allDeps.remove(dep))
					log.warn("cant remove dep");
			}
		}
		
		if (allDeps.size() > 0)
			log.warn("failed to process " + allDeps.size() + " deps");
		
		List<Dependency> result = new ArrayList<Dependency>();
		result.addAll(level1.values());
		
		return result;
	}
		
	private Query createDependencyQuery(String word) {
		QWordDependency query = QWordDependency.newQuery();
		query.select(query.wildcard());
		query.where(query.lemma().eq(word));
		//query.where(query.dependencyTypeCount().gt(1000));
		query.orderBy(query.dependencyType());
		return query;		
	}
	
	private Query createDependencyWildcardQuery(String wildcard) {
		QWordDependency query = QWordDependency.newQuery();
		query.select(query.wildcard());
		query.where(query.lemma().like(wildcard));
		query.orderBy(query.dependencyType());
		return query;		
	}

	@Override
	public List<QueueAdapter> findGovernors(String word, String dependencyType,
			Integer startRange, Integer endRange, 
			boolean asc) {
		List<QueueAdapter> result = new ArrayList<QueueAdapter>();
		Query query = createGovernorQuery(word, dependencyType); 
		if (startRange != null && endRange != null) {
			query.setStartRange(startRange);
			query.setEndRange(endRange);
		}
		DataGraph[] graphs = this.service.find(query);
		int i = 0;
		for (DataGraph graph : graphs) {
			Sentence sent = new GovernorSentence((Governor)graph.getRootObject());
			result.add(sent);
			sent.setIndex(i);
			i++;
		}
		return result;
	}

	@Override
	public List<QueueAdapter> findDependents(String word, String dependencyType, 
    		Integer startRange, Integer endRange, 
			boolean asc) {
		List<QueueAdapter> result = new ArrayList<QueueAdapter>();
		Query query = createDependentQuery(word, dependencyType); 
		if (startRange != null && endRange != null) {
			query.setStartRange(startRange);
			query.setEndRange(endRange);
		}
		DataGraph[] graphs = this.service.find(query);
		int i = 0;
		for (DataGraph graph : graphs) {
			Sentence sent = new DependentSentence((Dependent)graph.getRootObject());
			result.add(sent);
			sent.setIndex(i);
			i++;
		}
		return result;
	}
 
	private Query createGovernorQuery(String lemma, String type) {
		QGovernor query = QGovernor.newQuery();
		query.select(query.wildcard())
		     .select(query.dependency().dependencySet().sentence().wildcard())
		     .select(query.dependency().dependencySet().sentence().document().page().pageTitle())
		     .select(query.node().wildcard())
		;
		if (type != null)
			query.where(query.node().lemma().eq(lemma).and(query.dependency().type().eq(type)));
		else
			query.where(query.node().lemma().eq(lemma));
		return query;		
	}

	private Query createDependentQuery(String lemma, String type) {
		QDependent query = QDependent.newQuery();
		query.select(query.wildcard())
		     .select(query.dependency().dependencySet().sentence().wildcard())
		     .select(query.dependency().dependencySet().sentence().document().page().pageTitle())
		     .select(query.node().wildcard())
		;
		if (type != null)
			query.where(query.node().lemma().eq(lemma).and(query.dependency().type().eq(type)));
		else
			query.where(query.node().lemma().eq(lemma));
		return query;		
	}
}
