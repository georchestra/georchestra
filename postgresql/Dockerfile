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
# This script is used to skip loading of sample data if IGNORE_DATA var is set to yes
ADD 000-skip_data.sh /docker-entrypoint-initdb.d/
ADD license.txt /docker-entrypoint-initdb.d/
ADD logo.png /docker-entrypoint-initdb.d/
RUN chown -R postgres /docker-entrypoint-initdb.d/
