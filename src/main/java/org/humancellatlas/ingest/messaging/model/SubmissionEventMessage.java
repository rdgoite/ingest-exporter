package org.humancellatlas.ingest.messaging.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by rolando on 22/02/2018.
 */
@Getter
@AllArgsConstructor
public class SubmissionEventMessage {
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
}
