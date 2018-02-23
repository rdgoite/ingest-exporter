package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;

/**
 * Created by rolando on 22/02/2018.
 */
public interface ExportService {
    void exportSubmission(SubmissionEnvelopeReference submissionEnvelopeReference);
}
