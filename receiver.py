import os
import logging
import pika
import json
from ingestbroker.broker.ingestexportservice import IngestExporter

DEFAULT_RABBIT_URL=os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
DEFAULT_QUEUE_NAME=os.environ.get('SUBMISSION_QUEUE_NAME', 'ingest.envelope.submitted.queue')
LOG_FORMAT = ('%(levelname) -10s %(asctime)s %(name) -30s %(funcName) '
              '-35s %(lineno) -5d: %(message)s')
LOGGER = logging.getLogger(__name__)

EXCHANGE = 'ingest.assays.exchange'
ASSAY_COMPLETED_ROUTING_KEY = 'ingest.assays.completed'
class IngestReceiver:

    def __init__(self):
        self.logger = LOGGER

    def run(self, newAssayMessage):
        try:
            ingestExporter = IngestExporter()
            ingestExporter.generateAssayBundle(newAssayMessage)
            self.completeBundle(newAssayMessage)
        except Exception, e:
            self.logger.exception("Failed to export to dss: "+newAssayMessage["callbackLink"]+ ", error:"+str(e))

    def completeBundle(self, assayMessage):
        self.logger.info("Sending a completed message for assay "+assayMessage["callbackLink"])

        assayCompletedMessage = dict()

        assayCompletedMessage["documentId"] = assayMessage["documentId"]
        assayCompletedMessage["envelopeUuid"] = assayMessage["envelopeUuid"]
        assayCompletedMessage["assayIndex"] = assayMessage["assayIndex"]
        assayCompletedMessage["totalAssays"] = assayMessage["totalAssays"]

        connection = pika.BlockingConnection(pika.ConnectionParameters(DEFAULT_RABBIT_URL))
        channel = connection.channel()
        channel.basic_publish(exchange=EXCHANGE,
                              routing_key=ASSAY_COMPLETED_ROUTING_KEY,
                              body=json.dumps(assayCompletedMessage))
        connection.close()
