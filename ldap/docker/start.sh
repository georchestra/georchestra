#!/bin/bash
set -e

/populate_ldap.sh &
sleep 1
exec /entrypoint.sh slapd -d 32768 -u openldap -g openldap




