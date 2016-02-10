#!/bin/bash

if [ ! -d /etc/ldap/slapd.d ]; then

    echo "waiting LDAP to start"
    sleep 10
    echo "Loading georchestra.ldif"
    ldapadd -D"cn=admin,dc=georchestra,dc=org" -w "secret" -f /tmp/georchestra.ldif

fi