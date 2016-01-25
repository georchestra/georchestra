#
# Dockerfile for the geOrchestra database service
#
# Use with caution: this is work in progress

FROM postgres:9.4

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninterative

RUN apt-get update && \
    apt-get install -y postgresql-9.4-postgis-2.1 && \
    rm -rf /var/lib/apt/lists/*

ADD *.sql /docker-entrypoint-initdb.d/

# Better query performance with:
# RUN sed -i 's/shared_buffers = 128MB/shared_buffers = 1024MB/' /etc/postgresql/9.4/main/postgresql.conf
