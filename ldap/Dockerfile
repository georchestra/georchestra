#
# Dockerfile for the geOrchestra openldap service
#
# Use with caution: this is work in progress
#

FROM dinkel/openldap

RUN apt-get update && \
     apt-get install -y ldap-utils && \
     rm -rf /var/lib/apt/lists/*

USER openldap
ADD groupofmembers.ldif /etc/ldap.dist/modules/
ADD docker/memberof.ldif /etc/ldap.dist/modules/
ADD georchestra.ldif /tmp/

USER root
ADD docker/populate_ldap.sh /
ADD docker/start.sh /
RUN chmod +x /populate_ldap.sh /start.sh

ENTRYPOINT /start.sh
