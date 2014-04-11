package org.cloudgraph.examples.wikicorpus.service.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.parse.WordRelation;
import org.cloudgraph.examples.corpus.parse.WordRelationType;
import org.cloudgraph.examples.corpus.parse.query.QWordRelation;
import org.cloudgraph.examples.corpus.search.WordDependency;
import org.cloudgraph.examples.corpus.search.query.QWordDependency;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.plasma.config.DataAccessProviderName;
import org.plasma.query.Expression;
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
		
		Map<String, Dependency> level2 = new HashMap<String, Dependency>();
		a = new WordDependency[allDeps.size()];
		allDeps.toArray(a);
		for (WordDependency dep : a) {
			String parentType = Dependency.getParentTypeName(dep);
			Dependency parent = level1.get(parentType);
			if (parent == null && parentType != null) {
				this.mapSyntheticParent(parentType, word, root, level1);
				parent = level1.get(parentType);
			}
			
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
			else {
				parent = level1.get("dep");
				if (parent == null) {
					mapSyntheticParent("dep", word, root, level1);
					parent = level1.get("dep");
				}
				Dependency child = new Dependency(dep, parent);
				if (!allDeps.remove(dep))
					log.warn("cant remove dep");
				level2.put(dep.getDependencyType(), child);
			}
		}
		
		if (allDeps.size() > 0)
			log.warn("failed to process " + allDeps.size() + " deps");
		
		List<Dependency> result = new ArrayList<Dependency>();
		result.addAll(level1.values());
		
		return result;
	}
	
	private void mapSyntheticParent(String name, String word, Dependency root, Map<String, Dependency> map)
	{
		if (map.get(name) == null) {
			Dependency syntheticRoot = new Dependency(name, word, 0, 0, root);
			map.put(name, syntheticRoot);
		}		
	}
		
	private Query createDependencyQuery(String word) {
		QWordDependency query = QWordDependency.newQuery();
		query.select(query.wildcard());
		query.where(query.lemma().eq(word));
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
    public List<QueueAdapter> findRelations(String word, List<String> dependencyTypes, 
    		Integer startRange, Integer endRange, 
			boolean asc) {
		
		List<QueueAdapter> result = new ArrayList<QueueAdapter>();
		Query query = createRelationQuery(word, null, dependencyTypes); 
		if (startRange != null && endRange != null) {
			query.setStartRange(startRange);
			query.setEndRange(endRange);
		}
		DataGraph[] graphs = this.service.find(query);
		int i = 0;
		for (DataGraph graph : graphs) {
			Sentence sent = new Sentence((WordRelation)graph.getRootObject());
			result.add(sent);
			sent.setIndex(i);
			i++;
		}
		return result;
	}

	@Override
	public List<QueueAdapter> findGovernors(String word, List<String> dependencyTypes,
			Integer startRange, Integer endRange, 
			boolean asc) {
		List<QueueAdapter> result = new ArrayList<QueueAdapter>();
		Query query = createGovernorQuery(word, dependencyTypes); 
		if (startRange != null && endRange != null) {
			query.setStartRange(startRange);
			query.setEndRange(endRange);
		}
		DataGraph[] graphs = this.service.find(query);
		int i = 0;
		for (DataGraph graph : graphs) {
			Sentence sent = new Sentence((WordRelation)graph.getRootObject());
			result.add(sent);
			sent.setIndex(i);
			i++;
		}
		return result;
	}
	
	@Override
	public List<QueueAdapter> findDependents(String word, List<String> dependencyTypes, 
    		Integer startRange, Integer endRange, 
			boolean asc) {
		List<QueueAdapter> result = new ArrayList<QueueAdapter>();
		Query query = createDependentQuery(word, dependencyTypes); 
		if (startRange != null && endRange != null) {
			query.setStartRange(startRange);
			query.setEndRange(endRange);
		}
		DataGraph[] graphs = this.service.find(query);
		int i = 0;
		for (DataGraph graph : graphs) {
			Sentence sent = new Sentence((WordRelation)graph.getRootObject());
			result.add(sent);
			sent.setIndex(i);
			i++;
		}
		return result;
	}
		
	private Query createGovernorQuery(String lemma, List<String> dependencyTypes) {
		return createRelationQuery(lemma, WordRelationType.GOVERNOR, dependencyTypes);
	}

	private Query createDependentQuery(String lemma, List<String> dependencyTypes) {
		return createRelationQuery(lemma, WordRelationType.DEPENDENT, dependencyTypes);
	}
	
	private Query createRelationQuery(String lemma, WordRelationType relationType, 
			List<String> dependencyTypes) {
		QWordRelation query = QWordRelation.newQuery();
		query.select(query.wildcard())
		     .select(query.dependency().wildcard())
		     .select(query.dependency().dependencySet().sentence().wildcard())
		     .select(query.dependency().dependencySet().sentence().document().page().pageTitle())
		     .select(query.node().wildcard())
		;
		Expression typesExpr = null;
		if (dependencyTypes != null && dependencyTypes.size() > 0) {
			typesExpr = query.dependency().type().eq(dependencyTypes.get(0));
			for (int i = 1; i < dependencyTypes.size(); i++) {
				typesExpr.or(
					query.dependency().type().eq(dependencyTypes.get(i)));
			}
		}	
		if (typesExpr != null)
		    typesExpr = query.group(typesExpr); 
		
		if (typesExpr != null) {
			if (relationType != null) {
			    query.where(query.node().lemma().eq(lemma)
				     .and(query.relationType().eq(relationType.getInstanceName())
				     .and(typesExpr)));
			}
			else {
			    query.where(query.node().lemma().eq(lemma)
				     .and(typesExpr));
			}
		}
		else {
			if (relationType != null) {
			    query.where(query.node().lemma().eq(lemma)
				     .and(query.relationType().eq(relationType.getInstanceName())));
			}
			else {
			    query.where(query.node().lemma().eq(lemma));
			}
		}
		return query;		
	}
	
}
