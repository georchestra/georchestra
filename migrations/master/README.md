# From 24.0 to master

### orgUniqueId attribute on the georchestraOrg schema

The `orgUniqueId` organization attribute was added to the `georchestraOrg` ldap schema.


This attribute will contain a unique organization identifier. The difference with existing identifiers is that this is not a system identifier.

This attribute is filled in when an organization is created, or via the console form as others organization attributes.
This attribute will also contain the OAuth2 organization identifier when user is connected using an external identity provider.

* Adapt georchestra schema definition

To upgrade the ldap, you need first to find the georchestra schema definition using the following command :

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b cn=schema,cn=config '(cn=*georchestra)' dn
```

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required `dn` is :

`dn: cn={5}georchestra,cn=schema,cn=config`

If you find a different `dn` (please update Commands provided in [ldap_migration.ldif](ldap_migration.ldif) file.

* Verify georchestraOrg `olcObjectClasses`

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that georchestraOrg `olcObjectClasses` schema is :

```
olcObjectClasses: ( 1.3.6.1.4.1.53611.1.1.2
    NAME 'georchestraOrg'
    DESC 'georchestra org'
    SUP top
    AUXILIARY
    MAY (jpegphoto $ labeledURI $ knowledgeInformation $ georchestraObjectIdentifier))
```
Please, verify this schema with the following commands (adapt `cn={5}georchestra,cn=schema,cn=config`): 

```
ldapsearch -Y EXTERNAL -H ldapi:/// -b cn={5}georchestra,cn=schema,cn=config '(objectClass=olcSchemaConfig)' olcObjectClasses
```

If you find a different olcObjectClasses, please update commands provided in [ldap_migration.ldif](ldap_migration.ldif) file with the correct schema.
 

* Apply changes

Please follow previous steps with precaution before running final script.

Finally, use the following command with the [ldap_migration.ldif](ldap_migration.ldif) file:

```
ldapmodify -H "ldap://ldap:389" -D "cn=admin,dc=georchestra,dc=org" -w "secret" -f ldap_migration.ldif
```

