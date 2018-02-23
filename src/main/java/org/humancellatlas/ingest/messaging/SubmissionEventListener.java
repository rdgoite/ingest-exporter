package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.export.ExportService;
import org.humancellatlas.ingest.messaging.model.SubmissionEventMessage;
import org.humancellatlas.ingest.util.ExportUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Created by rolando on 22/02/2018.
 */
@Component
@DependsOn("configuration")
public class SubmissionEventListener {
    private ExportService exportService;
    private ConfigurationService config;

    @Autowired
    public SubmissionEventListener(@Autowired ExportService exportService, @Autowired ConfigurationService config) {
        this.exportService = exportService;
        this.config = config;
    }

    @RabbitHandler()
    public void envelopeSubmittedMessage(SubmissionEventMessage submissionEventMessage) {
        this.exportService.exportSubmission(ExportUtil.submissionEventMessageToSubmissionEnvelopeReference(submissionEventMessage));
    }
}
