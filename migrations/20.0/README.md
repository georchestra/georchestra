# From 19.04 to 20.0.x

## Database migration

For the console, related to the GDPR compliance API [#2613](https://github.com/georchestra/georchestra/pull/2613), you should:
```
GRANT geonetwork TO georchestra;
```

For Geonetwork, since we now sync LDAP Organizations with GeoNetwork groups:
```
ALTER TABLE geonetwork.groups ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE geonetwork.groupsdes ALTER COLUMN label TYPE VARCHAR(255);
```

No other manual changes on the model are required to upgrade to the new version, since hibernate will take care of it.

## LDAP upgrade

With this release, custom new attributes are added to geOrchestra users and organisations in the LDAP, leveraging a custom, [dedicated schema](https://github.com/georchestra/georchestra/blob/master/ldap/docker-root/georchestraSchema.ldif).
As a result, the LDAP DIT should be upgraded with the provided [script](upgrade_ldap_from_19.04_to_20.0.ldif), which creates a new geOrchestra schema with the required objectClasses and attributes.

First of all, we recommend that you backup all entries:
```
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=users,dc=georchestra,dc=org" > users.backup.ldif
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=orgs,dc=georchestra,dc=org" > orgs.backup.ldif
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=roles,dc=georchestra,dc=org" > roles.backup.ldif
```

Once this is done, you can start adding the new schema:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/migrations/20.0.x/upgrade_ldap_from_19.04_to_20.0.ldif -O /tmp/upgrade_ldap_from_19.04_to_20.0.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/upgrade_ldap_from_19.04_to_20.0.ldif
```

Let's prepare a migration LDIF, for users:
```
ldapsearch -x -H ldap://localhost:389 -o ldif-wrap=no -b "ou=users,dc=georchestra,dc=org" dn |grep "^dn: uid=" | while read f ; do
    printf "$f\nchangetype:modify\nadd:objectClass\nobjectClass:georchestraUser\n\n" >> /tmp/modify.ldif
done
```
Then for orgs:
```
ldapsearch -x -H ldap://localhost:389 -o ldif-wrap=no -b "ou=orgs,dc=georchestra,dc=org" '(objectClass=organization)' dn |grep "^dn: o=" | while read f ; do
    printf "$f\nchangetype:modify\nadd:objectClass\nobjectClass:georchestraOrg\n\n" >> /tmp/modify.ldif
done
```
Check the generated `modify.ldif` file is correct. It should look like this:
```
dn: uid=aaaaaa,ou=users,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraUser

dn: uid=bbbbbb,ou=users,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraUser
.
.
.
dn: o=yyyyyy,ou=orgs,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraOrg

dn: o=zzzzzz,ou=orgs,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraOrg
```

Finally upgrade all entries:
```
ldapmodify -H ldap://localhost:389 -D "cn=admin,dc=georchestra,dc=org" -W -f /tmp/modify.ldif
```

If anything goes wrong during the upgrade process, you can rollback thanks to the above backup (always inserting users first, or the `memberOf` overlay won't work !).

## MAPSTORE_ADMIN role

The `20.0.6` release introduces the `MAPSTORE_ADMIN` role in the LDAP, granting administrative access to its members, on the new viewer based on Mapstore2. It can be inserted in
an existing OpenLDAP tree considering the following LDIF specification:

```
# cat mapstore-admin-role.ldif
# MAPSTORE_ADMIN, roles, georchestra.org
dn: cn=MAPSTORE_ADMIN,ou=roles,dc=georchestra,dc=org
objectClass: top
objectClass: groupOfMembers
cn: MAPSTORE_ADMIN
description: This role grants administrative access to MapStore2
member: uid=testadmin,ou=users,dc=georchestra,dc=org
```

Using the following command:

```
$ ldapadd -H ldap://localhost:389 -D "cn=admin,dc=georchestra,dc=org" -W -f mapstore-admin-role.ldif
```

## Datadir migration

The `security proxy`, `console` and `geonetwork` applications have had several important changes in their configuration files. We provide a [diff](https://gist.github.com/fvanderbiest/e3afb00cd47a406cddaa2991d7171d01) to make your mind between starting from a fresh one, or upgrading yours.

## Frontend

The following RewriteRule can be safely removed since [georchestra/georchestra#2872](https://github.com/georchestra/georchestra/pull/2872):
```
RewriteCond %{REQUEST_URI} !^/console/manager/public/.*$
RewriteCond %{REQUEST_URI} ^/console/manager/([home|users|org|orgs|role|roles|logs|analytics|delegations].*)$
RewriteRule .* /console/manager/#!/%1 [NE,R,L]
```
It is now managed by the console application itself.

## Tomcat for GeoServer

Locate tomcat's `conf/context.xml` and update the `<Context>` tag in order to set `useRelativeRedirects` to `false`, eg:
```xml
<Context useRelativeRedirects="false">
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
    <WatchedResource>${catalina.base}/conf/web.xml</WatchedResource>
</Context>
```
See [#1857](https://github.com/georchestra/georchestra/pull/1847) for the motivations.
