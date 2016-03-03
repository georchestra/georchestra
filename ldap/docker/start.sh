#!/bin/bash
set -e

# test if already initialized
sleep 10
if [ ! -d /etc/ldap/slapd.d ]; then

    # init ldap config
    /entrypoint.sh true

    # start slapd in background
    slapd -u openldap -g openldap -h "ldapi:/// ldap:///"

    echo -n "waiting for ldap deamon to start..."
    while true; do
        sleep 10
        ldapsearch -x >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            break
        fi
    done;
    echo "OK"

    echo "Populating ldap tree"
    ldapadd -D"cn=admin,dc=georchestra,dc=org" -w "secret" -f /tmp/georchestra.ldif
    echo "OK"

    pkill slapd

    # wait for ldap to stop
    echo -n "wait for ldap to stop..."
    while true; do
        sleep 10
        pgrep slapd >/dev/null 2>&1
        if [ $? -ne 0 ]; then
            break
        fi
    done;
    echo "OK"

else
    echo "Ldap already initialised"

fi

exec "slapd -d 32768 -u openldap -g openldap"




