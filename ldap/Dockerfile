#
# Dockerfile for the geOrchestra openldap service
#

FROM debian:stretch

ENV OPENLDAP_VERSION 2.4.44
ENV RUN_AS_UID 101
ENV RUN_AS_GID 101

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y \
        slapd=${OPENLDAP_VERSION}* && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mv /etc/ldap /etc/ldap.dist

COPY docker/modules/ /etc/ldap.dist/modules

RUN apt-get update && \
    apt-get install -y ldap-utils procps && \
    rm -rf /var/lib/apt/lists/*

USER openldap
ADD groupofmembers.ldif /etc/ldap.dist/modules/
ADD docker/indexes.ldif /tmp/
ADD memberof.ldif /tmp/
ADD georchestra.ldif /tmp/

USER root
RUN mkdir /docker-entrypoint.d
ADD docker/docker-entrypoint.d/* /docker-entrypoint.d/
ADD docker/docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.d/*

EXPOSE 389

VOLUME ["/etc/ldap", "/var/lib/ldap"]

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "sh", "-c", "exec slapd -d ${SLAPD_LOG_LEVEL:-32768} -u ${RUN_AS_UID} -g ${RUN_AS_GID}" ]

HEALTHCHECK --interval=30s --timeout=10s \
  CMD ldapsearch \
      -D "cn=admin,dc=georchestra,dc=org" \
      -w "${SLAPD_PASSWORD}" \
      -b "dc=georchestra,dc=org" \
      "cn=geoserver_privileged_user,ou=users,dc=georchestra,dc=org"
