#
# Dockerfile for the geOrchestra database service
#

FROM postgres:13

LABEL maintainer="psc@georchestra.org"

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_USER=georchestra \
    POSTGRES_PASSWORD=georchestra

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        postgresql-13-postgis-3 \
        postgresql-13-postgis-3-scripts && \
    rm -rf /var/lib/apt/lists/*

COPY --chown=postgres [0-9][0-9]* fix-owner.sql license.txt logo.png /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U $POSTGRES_USER || exit 1
