package org.humancellatlas.ingest.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.humancellatlas.ingest.model.EntityJson;
import org.humancellatlas.ingest.model.EntityType;
import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;
import java.util.Collection;


/**
 * Created by rolando on 22/02/2018.
 */
public interface IngestClient {

    Collection<JsonNode> getAllEntitiesForSubmissionEnvelope(SubmissionEnvelopeReference envelopeReference, EntityType entityType);

    Collection<JsonNode> getAllEntitiesForSubmissionEnvelopeWithProjection(SubmissionEnvelopeReference envelopeReference, EntityType entityType, String projection);

}
