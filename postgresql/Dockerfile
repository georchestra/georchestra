#
# Dockerfile for the geOrchestra database service
#

FROM postgres:15

LABEL maintainer="psc@georchestra.org"

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_USER=georchestra \
    POSTGRES_PASSWORD=georchestra

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        postgresql-15-postgis-3 \
        postgresql-15-postgis-3-scripts && \
    rm -rf /var/lib/apt/lists/*

COPY --chown=postgres . /

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U $POSTGRES_USER || exit 1
