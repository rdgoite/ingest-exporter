package org.humancellatlas.ingest.model;

/**
 * Created by rolando on 28/02/2018.
 */
public interface TransformationChain {
    void insert(TransformationLink link, EntityJson src, EntityJson target);
}
