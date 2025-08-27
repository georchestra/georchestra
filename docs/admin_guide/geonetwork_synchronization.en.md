# Synchronization between GeoNetwork and the LDAP

GeoNetwork requires to create some objects in its database to function
correctly, this mainly to ensure referential integrity. As a result, a
[process](https://github.com/georchestra/datadir/blob/master/geonetwork/geonetwork.properties#L25-L57)
is configured to run at a regular pace, which ensures groups are created from
some LDAP objects.

By default, this process is configured to create groups from the organizations
registered in the LDAP tree.

## Little bit of history

*Historically though, geOrchestra only had a `ou=groups` branch, and to
distinguish between roles and groups, a prefix was used (`SV_` for roles, `EL_`
for groups). Some organizations still want to stick with the previous
synchronization behaviour.*

*One reason to rollback to the previous behaviour is that one geOrchestra user belongs to one
and only one organization. Keeping the `EL_*` role logic allows a user to be
a member of several groups in GeoNetwork.*

## How to set synchronization to use roles

Using roles as GeoNetwork groups is still possible by modifying
the datadir consequently, see below:

First, we need to edit in the datadir the
[geonetwork.properties](https://github.com/georchestra/datadir/blob/master/geonetwork/geonetwork.properties#L25-L37)
to tell the synchronization process to use the `roles` of the LDAP and set the `geonetwork.syncRolesFilter` to match actual needs. 

```diff
-geonetwork.syncMode=orgs
+geonetwork.syncMode=roles

+geonetwork.syncRolesFilter=EL_(.*)
```

## If you use datafeeder too

Datafeeder is a tool to ingest data into GeoNetwork and Geoserver. 
In order to be compliant with the new synchronization mode, you need to set the `datafeeder.publishing.geonetwork.syncMode` to `roles` in the [`datafeeder.properties`](https://github.com/georchestra/datadir/blob/master/datafeeder/datafeeder.properties#L80-L83) file.
