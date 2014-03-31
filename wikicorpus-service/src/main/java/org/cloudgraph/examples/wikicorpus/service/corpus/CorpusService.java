package org.cloudgraph.examples.wikicorpus.service.corpus;

import java.util.Collection;
import java.util.List;

import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.cloudgraph.examples.corpus.parse.Dependent;
import org.cloudgraph.examples.corpus.parse.Governor;
import org.cloudgraph.examples.corpus.search.WordDependency;


public interface CorpusService {
    public List<Dependency> findDependencies(String word);

    public List<QueueAdapter> findGovernors(String word, String dependencyType, 
    		Integer startRange, Integer endRange, 
			boolean asc);
    public List<QueueAdapter> findDependents(String word, String dependencyType, 
    		Integer startRange, Integer endRange, 
			boolean asc);
}
