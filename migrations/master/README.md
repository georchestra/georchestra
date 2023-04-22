# From 23.0 to master

## cas
From 23.0 to 23.1
To activate password rotation policy for ldap, user need to run these commands:

```
ldapmodify -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret
dn: olcOverlay={0}ppolicy,olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcPPolicyDefault
olcPPolicyDefault: cn=default,ou=pwpolicy,dc=georchestra,dc=org

```
```
ldapadd -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret
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

```
To activate rotation password policy for all users, please run script 'set_rotation_policy_for_all_users.sh' located at 'georchestra/migrations/23.0' :
```
sh set_rotation_policy_for_all_users.sh 
```
To desactivate rotation password policy for non humain users, please run :
```
ldapmodify -H ldap://localhost:389  -D cn=admin,dc=georchestra,dc=org -w secret
dn: uid=geoserver_privileged_user,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org

dn: uid=idatafeeder,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org
```




