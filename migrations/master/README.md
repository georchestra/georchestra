# From 23.0 to master

## cas
From 23.0 to 23.1
To activate password rotation policy for ldap, user need to run these commands:

```
ldapmodify -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret  << EOF 
dn: olcOverlay={0}ppolicy,olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcPPolicyDefault
olcPPolicyDefault: cn=default,ou=pwpolicy,dc=georchestra,dc=org


EOF 

```
```
ldapadd -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret   << EOF 
dn: ou=pwpolicy,dc=georchestra,dc=org
objectClass: organizationalUnit
objectClass: top
ou: pwpolicy

dn: cn=default,ou=pwpolicy,dc=georchestra,dc=org
objectClass: person
objectClass: pwdPolicyChecker
objectClass: pwdPolicy
cn: pwpolicy
sn: pwpolicy
pwdAttribute: userPassword
pwdMinAge: 0
pwdMaxAge: 31536000
pwdGraceAuthnLimit: 0
pwdExpireWarning: 2592000

dn: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org
objectClass: person
objectClass: pwdPolicyChecker
objectClass: pwdPolicy
cn: pwpolicy
cn: pwd-no-expire
sn: pwpolicy
pwdAttribute: userPassword
pwdMinAge: 0
pwdMaxAge: 0


EOF

```
To activate rotation password policy for all users, please run script 'set_rotation_policy_for_all_users.sh' located in this very same folder :
```
sh set_rotation_policy_for_all_users.sh 
```
To desactivate rotation password policy for non humain users, please run :
```
ldapmodify -H ldap://localhost:389  -D cn=admin,dc=georchestra,dc=org -w secret  << EOF 
dn: uid=geoserver_privileged_user,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org

dn: uid=idatafeeder,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org


EOF 
```
## LDAP

The `oAuth2ProviderId` user attribute was added to the ldap schema.

This attribute will contain OAuth2 user id when user is connected using external identity provider.


To upgrade the ldap, you need first to find the georchestra schema definition using the following command : 

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b cn=schema,cn=config '(cn=*georchestra)' dn
```

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required dn is :

dn: cn={7}georchestra,cn=schema,cn=config

If you find a different dn please update Commands provided in [ldap_migration.ldif](ldap_migration.ldif) file

Also, required olcObjectClasses "georchestraUser" to be changed should be localized using the following command : 

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b "cn=schema,cn=config" | grep 1.3.6.1.4.1.53611.1.1.1
```
Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required dn is :

olcObjectClasses: {0}

If you find a different olcObjectClasses number please update commands provided in [ldap_migration.ldif](ldap_migration.ldif) file

Please follow previous steps with precaution before running final script.

Finally, use the following command with the [ldap_migration.ldif](ldap_migration.ldif) file:

```
ldapmodify -H "ldap://ldap:389" -D "cn=admin,dc=georchestra,dc=org" -w "secret" -f ldap_migration.ldif 
```



