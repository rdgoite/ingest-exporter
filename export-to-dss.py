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
from kombu.mixins import ConsumerMixin

from receiver import IngestReceiver

DEFAULT_RABBIT_URL = os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
EXCHANGE = 'ingest.assays.exchange'
EXCHANGE_TYPE = 'topic'
QUEUE = 'ingest.assays.bundle.create'
ROUTING_KEY = 'ingest.assays.submitted'

logger = logging.getLogger(__name__)
receiver = IngestReceiver()


class Worker(ConsumerMixin):
    def __init__(self, connection, queues):
        self.connection = connection
        self.queues = queues

    def get_consumers(self, Consumer, channel):
        return [Consumer(queues=self.queues,
                         callbacks=[self.on_message])]

    def on_message(self, body, message):
        success = False
        try:
            receiver.run(json.loads(body))
            success = True
        except Exception, e:
            logger.exception(str(e))

        if success:
            message.ack()
            logger.info('Finished! ' + str(message.delivery_tag))


if __name__ == '__main__':
    format = ' %(asctime)s  - %(name)s - %(levelname)s in %(filename)s:%(lineno)s %(funcName)s(): %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format=format)

    parser = OptionParser()
    parser.add_option("-q", "--queue", help="name of the ingest queues to listen for submission")
    parser.add_option("-r", "--rabbit", help="the URL to the Rabbit MQ messaging server")
    parser.add_option("-l", "--log", help="the logging level", default='INFO')

    (options, args) = parser.parse_args()

    assay_exchange = Exchange(EXCHANGE, type=EXCHANGE_TYPE)
    assay_queues = [Queue(QUEUE, assay_exchange, routing_key=ROUTING_KEY)]

    with Connection(DEFAULT_RABBIT_URL, heartbeat=4) as conn:
        worker = Worker(conn, assay_queues)
        worker.run()
