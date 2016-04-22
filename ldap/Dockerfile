#
# Dockerfile for the geOrchestra openldap service
#

FROM dinkel/openldap

RUN apt-get update && \
     apt-get install -y ldap-utils && \
     rm -rf /var/lib/apt/lists/*

USER openldap
ADD groupofmembers.ldif /etc/ldap.dist/modules/
ADD docker/memberof.ldif /tmp/
ADD georchestra.ldif /tmp/

USER root
RUN mkdir /docker-entrypoint.d
ADD docker/docker-entrypoint.d/* /docker-entrypoint.d/
ADD docker/docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.d/*

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "slapd", "-d", "32768", "-u", "openldap", "-g", "openldap" ]
