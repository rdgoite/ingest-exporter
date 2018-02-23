package org.humancellatlas.ingest.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * Created by rolando on 22/02/2018.
 */
@Data
public class ProcessJson implements EntityJson {
    private JsonNode json;
}
