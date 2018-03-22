#!/usr/bin/env python
"""
This script listens on a ingest submission queue and as submission are completed will
call the ingest export service to generate the bundles and submit bundles to datastore
"""
__author__ = "jupp"
__license__ = "Apache 2.0"


from optparse import OptionParser
import os, sys
import logging
import threading
from listener import Listener


def initReceivers(options):
    # start a listener for new assay messages and attempt to create a bundle
    listener = Listener(options)
    t = threading.Thread(target=listener.run)
    t.start()

if __name__ == '__main__':
    format = ' %(asctime)s  - %(name)s - %(levelname)s in %(filename)s:%(lineno)s %(funcName)s(): %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format=format)

    parser = OptionParser()
    parser.add_option("-q", "--queue", help="name of the ingest queues to listen for submission")
    parser.add_option("-r", "--rabbit", help="the URL to the Rabbit MQ messaging server")
    parser.add_option("-l", "--log", help="the logging level", default='INFO')

    (options, args) = parser.parse_args()
    initReceivers(options)

