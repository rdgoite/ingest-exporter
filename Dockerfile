FROM python:3-alpine
MAINTAINER Simon Jupp "jupp@ebi.ac.uk"

RUN apk update && \
    apk add build-base && \
    apk add openssl-dev && \
    apk add libffi-dev

RUN mkdir /app
WORKDIR /app

COPY export-to-dss.py receiver.py  requirements.txt ./

RUN pip install -r requirements.txt

ENV INGEST_API=http://localhost:8080
ENV RABBIT_URL=amqp://localhost:5672
ENV SUBMISSION_QUEUE_NAME=ingest.envelope.submitted.queue
ENV STAGING_API=https://staging.staging.data.humancellatlas.org
ENV STAGING_API_VERSION=v1
ENV DSS_API=http://dss.dev.data.humancellatlas.org
ENV INGEST_API_KEY=key_not_set

ENTRYPOINT ["python"]
CMD ["export-to-dss.py"]
