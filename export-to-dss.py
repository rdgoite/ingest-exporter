#!/usr/bin/env python
"""
This script listens on a ingest submission queue and as submission are completed will
call the ingest export service to generate the bundles and submit bundles to datastore
"""
__author__ = "jupp"
__license__ = "Apache 2.0"


from optparse import OptionParser
import os, sys, json
import time
import logging
from kombu import Connection, Exchange, Queue, Producer
from receiver import IngestReceiver


DEFAULT_RABBIT_URL=os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
EXCHANGE = 'ingest.assays.exchange'
EXCHANGE_TYPE = 'topic'
QUEUE = 'ingest.assays.bundle.create'
ROUTING_KEY = 'ingest.assays.submitted'



def initReceivers(options):
    logger = logging.getLogger(__name__)

    receiver = IngestReceiver()

    def requeue(body):
        with Connection(DEFAULT_RABBIT_URL) as publish_conn:
            with publish_conn.channel() as channel:
                producer = Producer(channel)
                producer.publish(
                    body,
                    retry=True,
                    exchange=EXCHANGE,
                    routing_key=ROUTING_KEY
                )

    def callback(body, message):
        success = False
        try:
            receiver.run(json.loads(body))
            success = True
        except Exception, e:
            logger.exception(str(e))
            requeue(body)
            logger.info('Requeueing due to error! ' + str(message.delivery_tag))

        if success:
            logger.info('Finished! ' + str(message.delivery_tag))



    assayExchange = Exchange(EXCHANGE, EXCHANGE_TYPE, passive=True, durable=False)
    assayCreatedQueue = Queue(QUEUE, exchange=assayExchange, routing_key=ROUTING_KEY, durable=False, no_ack=True)


    with Connection(DEFAULT_RABBIT_URL, connect_timeout=1000, heartbeat=1000) as conn:
        # consume
        with conn.Consumer(assayCreatedQueue, callbacks=[callback]) as consumer:
            # Process messages and handle events on all channels
            while True:
                conn.drain_events()
                conn.heartbeat_check()


if __name__ == '__main__':
    format = ' %(asctime)s  - %(name)s - %(levelname)s in %(filename)s:%(lineno)s %(funcName)s(): %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format=format)

    parser = OptionParser()
    parser.add_option("-q", "--queue", help="name of the ingest queues to listen for submission")
    parser.add_option("-r", "--rabbit", help="the URL to the Rabbit MQ messaging server")
    parser.add_option("-l", "--log", help="the logging level", default='INFO')

    (options, args) = parser.parse_args()
    initReceivers(options)