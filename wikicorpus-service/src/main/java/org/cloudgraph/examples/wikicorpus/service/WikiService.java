package org.cloudgraph.examples.wikicorpus.service;

import java.io.File;

public interface WikiService {
    public void load(File file, int batchSize, boolean commit);
}
