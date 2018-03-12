package org.humancellatlas.ingest.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.humancellatlas.ingest.exception.NoOutputFilesException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 28/02/2018.
 */
@Data
public class BundleableProcess extends ProcessJson {
    private List<FileJson> outputFiles;

    public BundleableProcess(JsonNode json) {
        super(json);
    }

    public BundleableProcess(JsonNode json, List<FileJson> outputFiles) {
        this(json);
        this.outputFiles = outputFiles;
    }

    public static BundleableProcess fromProcess(ProcessJson processJson) {
        JsonNode outputFiles = processJson.getJson().get("outputFiles");
        if(! outputFiles.isArray()) {
            throw new NoOutputFilesException(
                    String.format("Process with uuid %s has no output files",
                    processJson.getJson().get("uuid").get("uuid").asText()));
        } else {
            List<FileJson> outputFileEntites = outputFiles.iterator();
            return new BundleableProcess(processJson.getJson(), (ArrayNode) outputFiles);
        }
    }
}
