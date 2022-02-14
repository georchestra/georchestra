#
# Dockerfile for the geOrchestra openldap service
#

FROM debian:bullseye

ENV OPENLDAP_VERSION 2.4.57
ENV RUN_AS_UID 101
ENV RUN_AS_GID 101

ENV SLAPD_DOMAIN georchestra.org
ENV SLAPD_PASSWORD secret
ENV SLAPD_ADDITIONAL_MODULES groupofmembers,openssh

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y \
        slapd=${OPENLDAP_VERSION}* ldap-utils procps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mv /etc/ldap /etc/ldap.dist
COPY docker-root /
RUN chown openldap /*.ldif /etc/ldap.dist/modules/*

EXPOSE 389

VOLUME ["/etc/ldap", "/var/lib/ldap"]

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "sh", "-c", "exec slapd -d ${SLAPD_LOG_LEVEL:-32768} -u ${RUN_AS_UID} -g ${RUN_AS_GID}" ]

HEALTHCHECK --interval=30s --timeout=10s \
  CMD ldapsearch -xLLL uid=geoserver_privileged_user || exit 1
