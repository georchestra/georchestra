#
# Dockerfile for the geOrchestra database service
#

FROM postgres:11

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_USER=georchestra

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        postgresql-11-postgis-2.5 \
        postgresql-11-postgis-2.5-scripts && \
    rm -rf /var/lib/apt/lists/*

COPY --chown=postgres [0-9][0-9]* fix-owner.sql license.txt logo.png /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U $POSTGRES_USER
