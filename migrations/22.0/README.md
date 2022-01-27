# From 20.X to 22.x

## GeoNetwork 4

### Link to external viewer

To use MapStore as GeoNetwork's default viewer:
 * open the `Admin console`->`User Interface` at /geonetwork/srv/fre/admin.console#/settings/ui
 * search for the "viewer" section
 * check the "Use an external viewer" box
 * update the "Viewer URL template" field to the following:
```
/mapstore/#?actions=[{"type":"CATALOG:ADD_LAYERS_FROM_CATALOGS","layers":[${service.name}],"sources":[${service.url}]}]

```

### Redundant authentication panel

_geOrchestra_ already handles the authentication of the user for the underlying _GeoNetwork 4_. As a result, new installs of _geOrchestra_ should remove the authentication panel from the embedded _GeoNetwork 4_ (see [#187](https://github.com/georchestra/geonetwork/pull/187)).

For existing _GeoNetwork 4_ installations, this can be done by unticking the "Authentication" checkbox from `Admin console`->`User Interface`:
```
☐ Authentication
```

### authtype

When migrating from GeoNetwork `3.8.x` to `4.0.x`, there is one specific database migration to be performed by hand:
previously when users were connecting to the catalogue for the first time, they were created into the GeoNetwork database
with an `authtype` set to 'LDAP'. This column of the `geonetwork.users` table has to be set to '' for these ones:

```sql
UPDATE geonetwork.users SET authtype = '';
```

## OpenLDAP

The main modification made in this release was to:

* introduce a new object class `georchestraRole` on the roles (branch `ou=roles`)
* introduce on each object class from the georchestra custom schema a new
  attribute `georchestraObjectIdentifier`.

This new attribute should take a unique identifier in the form of a [uuid](https://en.wikipedia.org/wiki/Universally_unique_identifier).

This change was motivated to be able to synchronize objects on GeoNetwork, and rely
 on a stable identifier between the LDAP and the database in a more reliable way.

Below is a suggested modification procedure:


```bash
# dumps / backup the current LDAP tree before proceeding:

for i in orgs pendingorgs pendingusers roles users ;
  do ldapsearch -Dcn=admin,dc=georchestra,dc=org -w secret -H ldap://localhost:389 -bou=${i},dc=georchestra,dc=org > ${i}.ldif ;
done
```

Then the georchestra openLDAP schema should be patched. To do so, we first need
to figure out which identifier the schema has, using the following command:

```bash
ldapsearch -Y EXTERNAL -H ldapi:/// -bcn=schema,cn=config 'cn=*georchestra' 'cn'
```

The output will be likely the following:

```bash
[...]
# {5}georchestra, schema, config
dn: cn={5}georchestra,cn=schema,cn=config
cn: {5}georchestra
[...]
```
In the following, we are supposing the schema is on the identifier `5` in the internal LDAP configuration tree.
the `heredocs` below should be adapted consequently.

### Adding the unique identifier attribute

```bash
cat << EOF >  add-uuidattr.ldif
dn: cn={5}georchestra, cn=schema, cn=config
changetype: modify
add: olcAttributeTypes
olcAttributeTypes: ( 1.3.6.1.4.1.53611.1.2.2
    NAME 'georchestraObjectIdentifier'
    DESC 'A UUID identifying the geOrchestra object'
    SYNTAX 1.3.6.1.1.16.1
    SINGLE-VALUE  )
EOF


ldapmodify -Y EXTERNAL -H ldapi:/// -f add-uuidattr.ldif
rm add-uuidattr.ldif
```

### Adding the "georchestraRole" objectClass to the schema

```bash
cat <<EOF > add-geor-role-class.ldif
dn: cn={5}georchestra, cn=schema, cn=config
changetype: modify
add: olcObjectClasses
olcObjectClasses: ( 1.3.6.1.4.1.53611.1.1.3
   NAME 'georchestraRole'
   DESC 'Uniquely identifiable georchestra role'
   SUP top
   AUXILIARY
   MAY (georchestraObjectIdentifier))
EOF

ldapmodify -Y EXTERNAL -H ldapi:/// -f add-geor-role-class.ldif
rm add-geor-role-class.ldif
```

# Adding the identifier to the 2 existing object classes

```bash
cat <<EOF > modify-geor-classes.ldif
dn: cn={5}georchestra, cn=schema, cn=config
changetype: modify
replace: olcObjectClasses
olcObjectClasses: ( 1.3.6.1.4.1.53611.1.1.1
    NAME 'georchestraUser'
    DESC 'georchestra user'
    SUP top
    AUXILIARY
    MAY ( privacyPolicyAgreementDate $ knowledgeInformation $ georchestraObjectIdentifier ))
olcObjectClasses: ( 1.3.6.1.4.1.53611.1.1.2
    NAME 'georchestraOrg'
    DESC 'georchestra org'
    SUP top
    AUXILIARY
    MAY (jpegphoto $ labeledURI $ knowledgeInformation $ georchestraObjectIdentifier))
EOF

ldapmodify -Y EXTERNAL -H ldapi:/// -f modify-geor-classes.ldif
rm modify-geor-classes.ldif
```

### Modifying existing objects from the LDAP tree

The following commands will require the `uuid-runtime` debian package,
which provides the `uuidgen` command to generate UUIDs.

```bash
# Modifies the roles and introduce a unique identifier
ldapsearch -x -H ldap://localhost:389 -w secret -Dcn=admin,dc=georchestra,dc=org -o ldif-wrap=no -b "ou=roles,dc=georchestra,dc=org" dn |grep "^dn: cn=" | while read f ; do
    printf "$f\nchangetype: modify\nadd: objectClass\nobjectClass: georchestraRole\n-\nadd: georchestraObjectIdentifier\ngeorchestraObjectIdentifier: $(uuidgen)\n\n" >> modify-roles.ldif
done

# pendingorgs
ldapsearch -x -H ldap://localhost:389 -w secret -Dcn=admin,dc=georchestra,dc=org -o ldif-wrap=no -b "ou=pendingorgs,dc=georchestra,dc=org" ObjectClass=georchestraOrg o |grep "^dn: o=" | while read f ; do
    printf "$f\nchangetype:modify\nadd: georchestraObjectIdentifier\ngeorchestraObjectIdentifier: $(uuidgen)\n\n" >> modify-pendingorgs.ldif
done

# orgs
ldapsearch -x -H ldap://localhost:389 -w secret -Dcn=admin,dc=georchestra,dc=org -o ldif-wrap=no -b "ou=orgs,dc=georchestra,dc=org" ObjectClass=georchestraOrg o |grep "^dn: o=" | while read f ; do
    printf "$f\nchangetype: modify\nadd: georchestraObjectIdentifier\ngeorchestraObjectIdentifier: $(uuidgen)\n\n" >> modify-orgs.ldif
done

# pendingusers
ldapsearch -x -H ldap://localhost:389 -w secret -Dcn=admin,dc=georchestra,dc=org -o ldif-wrap=no -b "ou=pendingusers,dc=georchestra,dc=org" ObjectClass=georchestraUser uid |grep "^dn: uid=" | while read f ; do
    printf "$f\nchangetype: modify\nadd: georchestraObjectIdentifier\ngeorchestraObjectIdentifier: $(uuidgen)\n\n" >> modify-pendingusers.ldif
done

# users
ldapsearch -x -H ldap://localhost:389 -w secret -Dcn=admin,dc=georchestra,dc=org -o ldif-wrap=no -b "ou=users,dc=georchestra,dc=org" ObjectClass=georchestraUser uid |grep "^dn: uid=" | while read f ; do
    printf "$f\nchangetype: modify\nadd: georchestraObjectIdentifier\ngeorchestraObjectIdentifier: $(uuidgen)\n\n" >> modify-users.ldif
done

# then apply the modifications (the order does not matter)

for i in modify-*.ldif ; do ldapmodify -H ldap://localhost -x -w secret -Dcn=admin,dc=georchestra,dc=org -f ${i} ; done

rm modify-*.ldif
```

## CAS 6

Existing CAS webapp has been removed. The new CAS authentication for georchestra lives in its [own repository](https://github.com/georchestra/georchestra-cas-server).

For more information on the upgrade from CAS 4 to CAS 6, please refer to [issue 2799](https://github.com/georchestra/georchestra/issues/2799).
