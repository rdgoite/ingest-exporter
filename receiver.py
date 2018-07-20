import json
import logging
import os
import pika

from ingest.exporter.ingestexportservice import IngestExporter

DEFAULT_RABBIT_URL=os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
DEFAULT_QUEUE_NAME=os.environ.get('SUBMISSION_QUEUE_NAME', 'ingest.envelope.submitted.queue')
LOG_FORMAT = ('%(levelname) -10s %(asctime)s %(name) -30s %(funcName) '
              '-35s %(lineno) -5d: %(message)s')
LOGGER = logging.getLogger(__name__)

EXCHANGE = 'ingest.bundle.exchange'
ASSAY_COMPLETED_ROUTING_KEY = 'ingest.bundle.assay.completed'


class IngestReceiver:

    def __init__(self):
        self.logger = LOGGER

    def run(self, message):
        self.logger.info('process received ' + message["callbackLink"])
        self.logger.info('process index: ' + str(message["index"]) + ', total processes: ' + str(message["total"]))

        ingest_exporter = IngestExporter()
        ingest_exporter.export_bundle(message["envelopeUuid"], message["documentUuid"])

        self.complete_bundle(message)

    def complete_bundle(self, message):
        self.logger.info("Sending a completed message for process " + message["callbackLink"])

        completed_message = dict()

        completed_message["documentId"] = message["documentId"]
        completed_message["envelopeUuid"] = message["envelopeUuid"]
        completed_message["index"] = message["index"]
        completed_message["total"] = message["total"]

        connection = pika.BlockingConnection(pika.URLParameters(DEFAULT_RABBIT_URL))
        channel = connection.channel()
        channel.basic_publish(exchange=EXCHANGE,
                              routing_key=ASSAY_COMPLETED_ROUTING_KEY,
                              body=json.dumps(completed_message))

        connection.close()
