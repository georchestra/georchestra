#
# Dockerfile for the geOrchestra database service
#

FROM postgres:11

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_USER=georchestra

RUN apt-get update && \
    apt-get install -y postgresql-11-postgis-2.5 && \
    rm -rf /var/lib/apt/lists/*

COPY [0-9][0-9]* fix-owner.sql license.txt logo.png /docker-entrypoint-initdb.d/
RUN chown -R postgres /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U $POSTGRES_USER
