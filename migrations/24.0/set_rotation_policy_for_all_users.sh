#!/bin/bash

echo "updating password management policy for all users"
sleep 2

for i in `ldapsearch -x -b ou=users,dc=georchestra,dc=org -H ldap://localhost:389| grep uid: | cut -d: -f2 | sed 's/^\ //g'`
do
if [ $i != "geoserver_privileged_user" ] && [ $i != "idatafeeder" ]; then
echo updating user $i
ldapmodify -x -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret  << EOF
dn: uid=$i,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=default,ou=pwpolicy,dc=georchestra,dc=org


EOF
fi
done
