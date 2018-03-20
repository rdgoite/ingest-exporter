import os
import logging
from ingestbroker.broker.ingestexportservice import IngestExporter

DEFAULT_RABBIT_URL=os.environ.get('RABBIT_URL', 'amqp://localhost:5672')
DEFAULT_QUEUE_NAME=os.environ.get('SUBMISSION_QUEUE_NAME', 'ingest.envelope.submitted.queue')
LOG_FORMAT = ('%(levelname) -10s %(asctime)s %(name) -30s %(funcName) '
              '-35s %(lineno) -5d: %(message)s')
LOGGER = logging.getLogger(__name__)

class IngestReceiver:

    def __init__(self):
        self.logger = LOGGER

    def run(self, submittedObject):
        if "documentId" in submittedObject:
            try:
                ingestExporter = IngestExporter()
                ingestExporter.processSubmission(submittedObject["documentId"])
                ingestExporter.generateBundles(submittedObject["documentId"])
                ingestExporter.completeSubmission(submittedObject["documentId"])
            except Exception, e:
                self.logger.exception("Failed to export to dss: "+submittedObject["documentId"]+ ", error:"+str(e))