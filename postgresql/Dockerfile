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

COPY [0-9][0-9]* fix-owner.sql license.txt logo.png /docker-entrypoint-initdb.d/
RUN chown -R postgres /docker-entrypoint-initdb.d/
