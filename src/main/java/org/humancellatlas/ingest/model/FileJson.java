package org.humancellatlas.ingest.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by rolando on 28/02/2018.
 */
@Data
@AllArgsConstructor
public class FileJson implements EntityJson {
   private JsonNode json;

}
