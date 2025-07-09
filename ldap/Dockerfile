#
# Dockerfile for the geOrchestra openldap service
#

FROM debian:12

ARG PM_POLICY=default
ENV OPENLDAP_VERSION 2.5.13
ENV RUN_AS_UID 100
ENV RUN_AS_GID 101

ENV SLAPD_DOMAIN georchestra.org
ENV SLAPD_PASSWORD secret
ENV SLAPD_ADDITIONAL_MODULES groupofmembers,openssh,ppolicy
ENV SLAPD_PASSWORD_MGT_POLICY ${PM_POLICY}

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y \
        slapd=${OPENLDAP_VERSION}* ldap-utils procps sasl2-bin && \
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
  CMD ldapsearch -xLLL -s one ou=users | grep  '^dn:' || exit 1
