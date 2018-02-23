package org.humancellatlas.ingest.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;

/**
 * Created by rolando on 22/02/2018.
 */
@Getter
@RequiredArgsConstructor
public class SubmissionEnvelopeReference {
    private final String id;
    private final UUID uuid;
    private final String callbackLocation;
}