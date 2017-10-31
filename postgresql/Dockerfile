#
# Dockerfile for the geOrchestra database service
#
# Use with caution: this is work in progress

FROM postgres:9.4

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y postgresql-9.4-postgis-2.1 postgresql-9.4-postgis-2.3 && \
    rm -rf /var/lib/apt/lists/*

ADD *.sql /docker-entrypoint-initdb.d/
ADD license.txt /docker-entrypoint-initdb.d/
ADD logo.png /docker-entrypoint-initdb.d/

# tweak entrypoint to ignore some SQL file based on environement var IGNORE_DATA
# If IGNORE_DATA contain 'yes' then *-data.sql will be ignored
RUN apt-get update && \
    apt-get install -y patch && \
    rm -rf /var/lib/apt/lists/*
ADD docker-entrypoint.sh.patch /
RUN cd /usr/local/bin && patch -p 0 < /docker-entrypoint.sh.patch
