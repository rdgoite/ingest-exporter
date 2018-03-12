package org.humancellatlas.ingest.model;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import lombok.Getter;


/**
 * Created by rolando on 28/02/2018.
 */
public class TransformGraph implements TransformationChain {
    @Getter private MutableValueGraph<EntityJson, TransformationLink> graph;

    public TransformGraph() {
        this.graph = ValueGraphBuilder.directed().build();
    }


    @Override
    public void insert(TransformationLink link, EntityJson src, EntityJson target) {
        getGraph().putEdgeValue(src, target, link);
    }
}
