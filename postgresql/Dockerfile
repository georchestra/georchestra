#
# Dockerfile for the geOrchestra database service
#

FROM postgres:10

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y postgresql-10-postgis-2.4 && \
    rm -rf /var/lib/apt/lists/*

COPY [0-9][0-9]* fix-owner.sql license.txt logo.png /docker-entrypoint-initdb.d/
RUN chown -R postgres /docker-entrypoint-initdb.d/
