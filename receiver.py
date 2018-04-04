import os, pika, json
import logging

from kombu.mixins import ConsumerProducerMixin

from ingestbroker.broker.ingestexportservice import IngestExporter

DEFAULT_RABBIT_URL = os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))

LOG_FORMAT = ('%(levelname) -10s %(asctime)s %(name) -30s %(funcName) '
              '-35s %(lineno) -5d: %(message)s')
LOGGER = logging.getLogger(__name__)

EXCHANGE = 'ingest.assays.exchange'
BUNDLE_COMPLETED_ROUTING_KEY = 'ingest.bundles.completed'
ROUTING_KEY = 'ingest.assays.submitted'
ASSAY_COMPLETED_ROUTING_KEY = 'ingest.assays.completed'


class IngestReceiver:
    def __init__(self):
        self.logger = LOGGER

    def run(self, newAssayMessage):
        ingestExporter = IngestExporter()
        bundleUuid = ingestExporter.generateAssayBundle(newAssayMessage)

        newAssayMessage["bundleUuid"] = bundleUuid

        self.verify_bundle(newAssayMessage)

    def verify_bundle(self, assayMessage):
        self.logger.info("Sending a message to verify bundle " + assayMessage["bundleUuid"])

        connection = pika.BlockingConnection(pika.URLParameters(DEFAULT_RABBIT_URL))
        channel = connection.channel()
        channel.basic_publish(exchange=EXCHANGE,
                              routing_key=BUNDLE_COMPLETED_ROUTING_KEY,
                              body=json.dumps(assayMessage))
        connection.close()


class AssayWorker(ConsumerProducerMixin):
    def __init__(self, connection, queues):
        self.connection = connection
        self.queues = queues
        self.logger = logging.getLogger(__name__)
        self.receiver = IngestReceiver()

    def get_consumers(self, Consumer, channel):
        return [Consumer(queues=self.queues,
                         callbacks=[self.on_message])]

    def on_message(self, body, message):
        message.ack()
        success = False
        try:
            self.receiver.run(json.loads(body))
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
            routing_key=ROUTING_KEY,
            retry=True,
        )
