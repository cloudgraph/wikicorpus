package org.cloudgraph.examples.wikicorpus.service;

import java.io.File;

import edu.jhu.nlp.wikipedia.WikiPage;

public interface WikiService {
    public void load(File file, int batchSize, boolean commit);
}
