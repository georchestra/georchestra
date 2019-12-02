# From 19.04 to 19.12

## LDAP upgrade

With this release, custom new attributes are added to geOrchestra users and organisations in the LDAP, leveraging a custom, [dedicated schema](https://github.com/georchestra/georchestra/blob/master/ldap/docker-root/georchestraSchema.ldif).

As a result, the LDAP DIT should be upgraded with the provided [upgrade from 19.04 to 19.12 LDIF script](upgrade_ldap_from_19.04_to_19.12.ldif), which creates a new geOrchestra schema with the required objectClasses and attributes:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/migrations/19.12/upgrade_ldap_from_19.04_to_19.12.ldif -O /tmp/upgrade_ldap_from_19.04_to_19.12.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/upgrade_ldap_from_19.04_to_19.12.ldif
```

Once done, we recommend that you export all user and organisation objects:
```
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -w your_ldap_password -b "ou=users,dc=georchestra,dc=org" > /tmp/users.ldif
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -w your_ldap_password -b "ou=orgs,dc=georchestra,dc=org" > /tmp/orgs.ldif
```

... and migrate them so they use the newly introduced object classes:
```
sed -i 's/objectClass: inetOrgPerson/objectClass: inetOrgPerson\nobjectClass: georchestraUser/g' /tmp/users.ldif
sed -i 's/objectClass: organization/objectClass: organization\nobjectClass: georchestraOrg/g' /tmp/orgs.ldif
```

Finally reimport them:
```
ldapdelete -H ldap://localhost:389 -x -r -D "cn=admin,dc=georchestra,dc=org" -W "ou=users,dc=georchestra,dc=org"
ldapadd -D "cn=admin,dc=georchestra,dc=org" -W -f /tmp/users.ldif
ldapdelete -H ldap://localhost:389 -x -r -D "cn=admin,dc=georchestra,dc=org" -W "ou=orgs,dc=georchestra,dc=org"
ldapadd -D "cn=admin,dc=georchestra,dc=org" -W -f /tmp/orgs.ldif
```
