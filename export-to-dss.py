#!/usr/bin/env python
"""
This script listens on a ingest submission queue and as submission are completed will
call the ingest export service to generate the bundles and submit bundles to datastore
"""
__author__ = "jupp"
__license__ = "Apache 2.0"


from optparse import OptionParser
import os, sys, pika, json
import logging
from receiver import IngestReceiver


DEFAULT_RABBIT_URL=os.environ.get('RABBIT_URL', 'amqp://localhost:5672')
EXCHANGE = 'ingest.assays.exchange'
EXCHANGE_TYPE = 'topic'
QUEUE = 'ingest.assays.bundle.create'
ROUTING_KEY = 'ingest.assays.submitted'

def initReceivers(options):
    receiver = IngestReceiver()
    connection = pika.BlockingConnection(pika.URLParameters(DEFAULT_RABBIT_URL))
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE)
    channel.queue_bind(queue=QUEUE, exchange=EXCHANGE, routing_key=ROUTING_KEY)

    def callback(ch, method, properties, body):
        receiver.run(json.loads(body))
        ch.basic_ack(method.delivery_tag)

    channel.basic_consume(callback, queue=QUEUE)

    # start consuming (blocks)
    channel.start_consuming()
    connection.close()

if __name__ == '__main__':
    format = ' %(asctime)s  - %(name)s - %(levelname)s in %(filename)s:%(lineno)s %(funcName)s(): %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format=format)

    parser = OptionParser()
    parser.add_option("-q", "--queue", help="name of the ingest queues to listen for submission")
    parser.add_option("-r", "--rabbit", help="the URL to the Rabbit MQ messaging server")
    parser.add_option("-l", "--log", help="the logging level", default='INFO')

    (options, args) = parser.parse_args()
    initReceivers(options)

