import os, pika, json
import logging

from kombu.mixins import ConsumerProducerMixin

from ingestbroker.broker.dssapi import DssApi

DEFAULT_RABBIT_URL=os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
DEFAULT_QUEUE_NAME=os.environ.get('SUBMISSION_QUEUE_NAME', 'ingest.envelope.submitted.queue')
LOG_FORMAT = ('%(levelname) -10s %(asctime)s %(name) -30s %(funcName) '
              '-35s %(lineno) -5d: %(message)s')
LOGGER = logging.getLogger(__name__)

EXCHANGE = 'ingest.assays.exchange'
ASSAY_COMPLETED_ROUTING_KEY = 'ingest.assays.completed'
BUNDLE_COMPLETED_ROUTING_KEY = 'ingest.bundles.completed'


class BundleReceiver:

    def __init__(self):
        self.logger = LOGGER
        self.dss_api = DssApi()

    def verify_bundle(self, assay_message):
        bundle_uuid = assay_message['bundleUuid']
        bundle = self.dss_api.retrieveBundle(bundle_uuid)

        if "bundle" in bundle and "files" in bundle["bundle"]:
            files = bundle["bundle"]["files"]
            for bundle_file in files:
                if "s3_etag" not in bundle_file or not bundle_file["s3_etag"]:
                    raise ValueError("File has no s3 etag")

        else:
            raise ValueError("Couldn't find bundle files")

        self.logger.info("Completing bundle" + str(assay_message))
        self.complete_bundle(assay_message)

    def complete_bundle(self, assay_message):

        self.logger.info("Sending a completed message for process " + str(assay_message["callbackLink"]))

        assay_completed_message = dict()

        assay_completed_message["documentId"] = assay_message["documentId"]
        assay_completed_message["envelopeUuid"] = assay_message["envelopeUuid"]
        assay_completed_message["assayIndex"] = assay_message["assayIndex"]
        assay_completed_message["totalAssays"] = assay_message["totalAssays"]

        connection = pika.BlockingConnection(pika.URLParameters(DEFAULT_RABBIT_URL))
        channel = connection.channel()
        channel.basic_publish(exchange=EXCHANGE,
                              routing_key=ASSAY_COMPLETED_ROUTING_KEY,
                              body=json.dumps(assay_completed_message))
        connection.close()


class BundleWorker(ConsumerProducerMixin):
    def __init__(self, connection, queues):
        self.connection = connection
        self.queues = queues
        self.receiver = BundleReceiver()
        self.logger = logging.getLogger(__name__)

    def get_consumers(self, Consumer, channel):
        return [Consumer(queues=self.queues,
                         callbacks=[self.on_message])]

    def on_message(self, body, message):
        message.ack()
        success = False
        try:
            self.receiver.verify_bundle(json.loads(body))
            success = True
        except Exception, e1:
            try:
                self.logger.info('Requeueing' + body)
                self.requeue_on_error(body)
            except Exception, e2:
                self.logger.exception("Critical error: could not requeue message:" + body)

            self.logger.exception(str(e1))

        if success:
            self.logger.info('Finished! ' + str(message.delivery_tag))

    def requeue_on_error(self, body):
        self.producer.publish(
            body,
            exchange=EXCHANGE,
            routing_key=BUNDLE_COMPLETED_ROUTING_KEY,
            retry=True,
        )
