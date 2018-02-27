package org.humancellatlas.ingest.export.impl;

import org.humancellatlas.ingest.client.IngestClient;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.export.ExportService;
import org.humancellatlas.ingest.model.EntityType;
import org.humancellatlas.ingest.model.ProcessJson;
import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rolando on 22/02/2018.
 */
@Service
@DependsOn("configuration")
public class ExportServiceImpl implements ExportService {
    private IngestClient ingestClient;
    private ConfigurationService config;

    @Autowired
    public ExportServiceImpl(@Autowired ConfigurationService config,
                             @Autowired IngestClient ingestClient) {
        this.config = config;
        this.ingestClient = ingestClient;
    }

    @Override
    public void exportSubmission(SubmissionEnvelopeReference submissionEnvelopeReference) {

    }

    public List<ProcessJson> getProcessesForSubmission(SubmissionEnvelopeReference submissionEnvelopeReference) {
        return ingestClient
                .getAllEntitiesForSubmissionEnvelope(submissionEnvelopeReference, EntityType.PROCESSES)
                .stream()
                .map(ProcessJson::new)
                .collect(Collectors.toList());
    }
}
