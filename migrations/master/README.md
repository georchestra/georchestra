# From 23.0 to master

## LDAP

### Password rotation policy

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
To disable rotation policy for non-human users, run the following :
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
### oAuth2ProviderId attribute on the georchestraUser schema

The `oAuth2ProviderId` user attribute was added to the `georchestraUser` ldap schema.

This attribute will contain the OAuth2 user identifier when said user is connected using an external identity provider.


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

### mail attribute on the organizations

The organisations from the LDAP have been modified so that we can now define a `mail` attribute on them via the Console.
a `objectClass: extensibleObject` has to be added to the existing organisations so that they remain compatible with geOrchestra.

See the provided [add_extensible_object_orgs.sh](add_extensible_object_orgs.sh) shell script for an example to migrate the
organizations.