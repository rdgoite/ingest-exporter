package org.humancellatlas.ingest.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by rolando on 22/02/2018.
 */
public interface EntityJson {
    JsonNode getJson();
    void setJson(JsonNode json);
}
