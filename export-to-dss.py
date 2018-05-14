#!/usr/bin/env python
"""
This script listens on a ingest submission queue and as submission are completed will
call the ingest export service to generate the bundles and submit bundles to datastore
"""
__author__ = "jupp"
__license__ = "Apache 2.0"

from optparse import OptionParser
import os, sys, json
import logging
from kombu import Connection, Exchange, Queue
from kombu.mixins import ConsumerProducerMixin

from receiver import IngestReceiver

DEFAULT_RABBIT_URL = os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
EXCHANGE = 'ingest.bundle.exchange'
EXCHANGE_TYPE = 'topic'
ASSAY_QUEUE = 'ingest.bundle.assay.create'
ANALYSIS_QUEUE = 'ingest.bundle.analysis.create'

ASSAY_ROUTING_KEY = 'ingest.bundle.assay.submitted'
ANALYSIS_ROUTING_KEY = 'ingest.bundle.analysis.submitted'

logger = logging.getLogger(__name__)
receiver = IngestReceiver()


class Worker(ConsumerProducerMixin):
    def __init__(self, connection, queues):
        self.connection = connection
        self.queues = queues

    def get_consumers(self, Consumer, channel):
        return [Consumer(queues=self.queues,
                         callbacks=[self.on_message])]

    def on_message(self, body, message):
        message.ack()
        success = False
        try:
            receiver.run(json.loads(body))
            success = True
        except Exception, e1:
            logger.exception(str(e1))
            logger.error("Failed to process the exporter message:" + body)

        if success:
            logger.info('Finished! ' + str(message.delivery_tag))


if __name__ == '__main__':
    format = ' %(asctime)s  - %(name)s - %(levelname)s in %(filename)s:%(lineno)s %(funcName)s(): %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format=format)

    parser = OptionParser()
    parser.add_option("-q", "--queue", help="name of the ingest queues to listen for submission")
    parser.add_option("-r", "--rabbit", help="the URL to the Rabbit MQ messaging server")
    parser.add_option("-l", "--log", help="the logging level", default='INFO')

    (options, args) = parser.parse_args()

    bundle_exchange = Exchange(EXCHANGE, type=EXCHANGE_TYPE)
    bundle_queues = [Queue(ASSAY_QUEUE, bundle_exchange, routing_key=ASSAY_ROUTING_KEY),
                     Queue(ANALYSIS_QUEUE, bundle_exchange, routing_key=ANALYSIS_ROUTING_KEY)]

    with Connection(DEFAULT_RABBIT_URL, heartbeat=1200) as conn:
        worker = Worker(conn, bundle_queues)
        worker.run()
