package org.cloudgraph.examples.wikicorpus.service.corpus;

import java.util.List;

import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;


public interface CorpusService {
    public List<Dependency> findDependencies(String word);

    public List<QueueAdapter> findRelations(String word, List<String> dependencyTypes, 
    		Integer startRange, Integer endRange, 
			boolean asc);
    public List<QueueAdapter> findGovernors(String word, List<String> dependencyTypes, 
    		Integer startRange, Integer endRange, 
			boolean asc);
    public List<QueueAdapter> findDependents(String word, List<String> dependencyTypes, 
    		Integer startRange, Integer endRange, 
			boolean asc);
	 
}
