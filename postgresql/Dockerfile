#
# Dockerfile for the geOrchestra database service
#

FROM postgres:18

LABEL maintainer="psc@georchestra.org"

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_USER=georchestra \
    POSTGRES_PASSWORD=georchestra

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        postgresql-18-postgis-3 \
        postgresql-18-postgis-3-scripts && \
    rm -rf /var/lib/apt/lists/*

COPY --chown=postgres . /

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U $POSTGRES_USER || exit 1

# OCI image labels (https://github.com/opencontainers/image-spec/blob/main/annotations.md)
LABEL org.opencontainers.image.title="geOrchestra Database"
LABEL org.opencontainers.image.description="geOrchestra PostgreSQL/PostGIS database service"
LABEL org.opencontainers.image.authors="PSC <psc@georchestra.org>"
LABEL org.opencontainers.image.vendor="geOrchestra"
LABEL org.opencontainers.image.url="https://www.georchestra.org"
LABEL org.opencontainers.image.documentation="https://docs.georchestra.org/"
LABEL org.opencontainers.image.source="https://github.com/georchestra/georchestra"
LABEL org.opencontainers.image.licenses="GPL-3.0-only"
