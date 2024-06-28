#!/bin/bash

LDAP_URL=ldap://localhost:389/
LDAP_ADMIN_DN=cn=admin,dc=georchestra,dc=org
LDAP_ADMIN_PASSWORD=secret
LDAP_ORGS_BASE_DN=ou=orgs,dc=georchestra,dc=org

ldapsearch -H ${LDAP_URL}  -D${LDAP_ADMIN_DN} objectClass=georchestraOrg -b ${LDAP_ORGS_BASE_DN} -x -w ${LDAP_ADMIN_PASSWORD} dn -LLL | grep '^dn:' | while read -r line;
do
cat <<EOF  | ldapmodify -H ${LDAP_URL} -D${LDAP_ADMIN_DN} -w ${LDAP_ADMIN_PASSWORD} -x
$line
changetype: modify
add: objectClass
objectClass: extensibleObject

EOF

done