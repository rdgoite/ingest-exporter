package org.humancellatlas.ingest.util;

import org.humancellatlas.ingest.messaging.model.SubmissionEventMessage;
import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;

import java.util.UUID;

/**
 * Created by rolando on 22/02/2018.
 *
 * Utility methods for exporting assay/analysis type datasets
 *
 */
public class ExportUtil {
    public static SubmissionEnvelopeReference submissionEventMessageToSubmissionEnvelopeReference(SubmissionEventMessage submissionEventMessage) {
        return new SubmissionEnvelopeReference(
                submissionEventMessage.getDocumentId(),
                UUID.fromString(submissionEventMessage.getDocumentUuid()),
                submissionEventMessage.getCallbackLink());
    }
}
