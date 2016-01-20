#
# Dockerfile for the geOrchestra openldap service
#
# Use with caution: this is work in progress
#

FROM debian:jessie

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y slapd ldap-utils ca-certificates

# FIXME: find a way to customize the "secret" password
# Hint: environment variables

# Add ldif files from current directory into /tmp
ADD . /tmp

RUN /etc/init.d/slapd start && \
    ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/bootstrap.ldif && \
    ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/groupofmembers.ldif && \
    ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/memberof.ldif && \
    ldapadd -D"cn=admin,dc=georchestra,dc=org" -w secret -f /tmp/root.ldif && \
    ldapadd -D"cn=admin,dc=georchestra,dc=org" -w secret -f /tmp/georchestra.ldif

# Expose the ldap port
EXPOSE 389

# Add VOLUMEs to allow backup of databases
#~ VOLUME  ["/etc/ldap/slapd.d"]

# Set the default command to run when starting the container
CMD /usr/sbin/slapd -h "ldap:/// ldapi:///" -g openldap -u openldap -F /etc/ldap/slapd.d -d 256
