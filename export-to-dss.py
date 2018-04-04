#!/usr/bin/env python
"""
This script listens on a ingest submission queue and as submission are completed will
call the ingest export service to generate the bundles and submit bundles to datastore
"""
__author__ = "jupp"
__license__ = "Apache 2.0"

from optparse import OptionParser
from kombu import Connection, Exchange, Queue

from receiver import AssayWorker
from bundlereceiver import BundleWorker

import os, sys, json
import logging
import threading

DEFAULT_RABBIT_URL = os.path.expandvars(os.environ.get('RABBIT_URL', 'amqp://localhost:5672'))
EXCHANGE = 'ingest.assays.exchange'
EXCHANGE_TYPE = 'topic'
QUEUE = 'ingest.assays.bundle.create'

BUNDLE_VERIFY_QUEUE = 'ingest.assays.bundle.verify'
ROUTING_KEY = 'ingest.assays.submitted'
BUNDLE_COMPLETED_ROUTING_KEY = 'ingest.bundles.completed'

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

    bundle_queues = [Queue(BUNDLE_VERIFY_QUEUE, assay_exchange, routing_key=ROUTING_KEY)]

    with Connection(DEFAULT_RABBIT_URL, heartbeat=1200) as conn:
        worker = AssayWorker(conn, assay_queues)
        bundle_worker = BundleWorker(conn, assay_queues)

        t = threading.Thread(target=worker.run)
        t.start()
