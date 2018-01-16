#
# Dockerfile for the geOrchestra openldap service
#

FROM dinkel/openldap

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

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "slapd", "-d", "32768", "-u", "openldap", "-g", "openldap" ]

HEALTHCHECK --interval=30s --timeout=10s \
  CMD ldapsearch \
      -D "cn=admin,dc=georchestra,dc=org" \
      -w "${SLAPD_PASSWORD}" \
      -b "dc=georchestra,dc=org" \
      "cn=geoserver_privileged_user,ou=users,dc=georchestra,dc=org"
