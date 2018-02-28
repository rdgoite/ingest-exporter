package org.humancellatlas.ingest.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.humancellatlas.ingest.client.IngestClient;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.model.EntityType;
import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.humancellatlas.ingest.util.IngestClientUtil.*;

/**
 * Created by rolando on 22/02/2018.
 */
@Component
@DependsOn("configuration")
public class IngestClientImpl implements IngestClient {
    private ConfigurationService config;

    @Autowired
    public IngestClientImpl(@Autowired ConfigurationService config){
        this.config = config;
    }

    @Override
    public Collection<JsonNode> getAllEntitiesForSubmissionEnvelope(SubmissionEnvelopeReference envelopeReference,
                                                                    EntityType entityType) {
        String entitiesUri = traverseOn(config.INGEST_API_URI + envelopeReference.getCallbackLocation())
                .follow(entityType.toString().toLowerCase())
                .asLink()
                .getHref();

        return getAllResources(entitiesUri);
    }

    @Override
    public Collection<JsonNode> getAllEntitiesForSubmissionEnvelopeWithProjection(SubmissionEnvelopeReference envelopeReference,
                                                                                  EntityType entityType,
                                                                                  String projection) {
        MultiValueMap<String, String> projectionTemplateParam = new LinkedMultiValueMap<>();
        projectionTemplateParam.put("projection", Collections.singletonList(projection));

        String entitiesUri = traverseOn(config.INGEST_API_URI + envelopeReference.getCallbackLocation())
                .follow(entityType.toString().toLowerCase())
                .asLink()
                .getHref();

        return getAllResourcesWithTemplateParams(entitiesUri, projectionTemplateParam);
    }

}
