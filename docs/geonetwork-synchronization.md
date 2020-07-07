# Synchronization between GeoNetwork and the LDAP

GeoNetwork requires to create some objects in its database to function
correctly, this mainly to ensure referential integrity. As a result, a
[process](https://github.com/georchestra/datadir/blob/master/geonetwork/config/config-security-georchestra.xml#L108-L124)
is configured to run at a regular pace, which ensures groups are created from
some LDAP objects.

By default, this process is configured to create groups from the organizations
registered in the LDAP tree (the `ou=orgs` branch).

Historically though, geOrchestra only had a `ou=groups` branch, and to
distinguish between roles and groups, a prefix was used (`SV_` for roles, `EL_`
for groups). Some organizations still want to stick with the previous
  synchronization behaviour. This is still possible: by modifying the datadir
  consequently:

In the
[config-security-georchestra.xml](https://github.com/georchestra/datadir/blob/master/geonetwork/config/config-security-georchestra.xml#L117)
file, one have to change the filter to get the groups prefixed by `EL_`:


```diff
-    <entry key="ldapGroupSearchFilter" value="(objectClass=groupOfMembers)"/>
+    <entry key="ldapGroupSearchFilter" value="(&amp;(objectClass=groupOfMembers)(cn=EL_*))"/>

```

Also, to remove the `EL_` prefix, one have to adapt the default pattern, and
map the label of the group onto the `description` attribute from the ldap:

```diff
-      <entry key="ldapGroupSearchPattern" value="(.*)"/>
-      <entry key="ldapGroupLabelAttribute" value="o"/>
+     <entry key="ldapGroupSearchPattern" value="EL_(.*)"/>
+     <entry key="ldapGroupLabelAttribute" value="description"/>
```

Finally, we need to edit the
[geonetwork.properties](https://github.com/georchestra/datadir/blob/master/geonetwork/geonetwork.properties#L60)
to tell the synchronization process to use the `ou=roles` branch of the LDAP:

```diff
-ldap.groups.search.base=ou=orgs
+ldap.groups.search.base=ou=roles
```

# Automatic creation of groups

GeoNetwork in geOrchestra is also configured to create automatically
non-existing groups when the user connects onto the catalogue. With the
previous configuration, mapping the geonetwork groups onto the `ou=roles` LDAP
branch will create groups like `ADMINISTRATOR`, `REVIEWER` and so on at first
connection to the GeoNetwork UI. To disable this behaviour, we will need to set
the `createNonExistingLdapGroup` option to false in the
[config-security-georchestra.xml](https://github.com/georchestra/datadir/blob/master/geonetwork/config/config-security-georchestra.xml#L91):

```diff
-    <property name="createNonExistingLdapGroup" value="true"/>
+    <property name="createNonExistingLdapGroup" value="false"/>
```
