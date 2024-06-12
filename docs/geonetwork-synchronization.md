# Synchronization between GeoNetwork and the LDAP

GeoNetwork requires to create some objects in its database to function
correctly, this mainly to ensure referential integrity. As a result, a
[process](https://github.com/georchestra/datadir/blob/master/geonetwork/config/config-security-georchestra.xml#L108-L124)
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

There is a current known limitation in the way the synchronization is working in geOrchestra's Geonetwork, when configured to synchronize against the roles. 
Every members of each roles in the LDAP will only grant you RegisteredUser privileges to the said roles. 
As an example, being member of the GN_EDITOR role will grant Geonetwork's Editor role ([see roles mapping](https://github.com/georchestra/datadir/blob/master/geonetwork/geonetwork.properties#L42-L47)), 
and even if it will grant you the editor privilege globally, user won't  have any role in Geonetwork's group. 
E.g : User John Doe has the `GN_EDITOR` and the `EL_group1` roles in the LDAP/console, John Doe will have the Geonetwork's Editor role but won't have any role in the `EL_group1` group in Geonetwork.

Issues behind this:
- If `syncRolesFilter` is set ot `.*` but we don't want to synchronize all geOrchestra's roles, we end up with a complex regex : see [#4202](https://github.com/georchestra/georchestra/issues/4202)

- LDAP side, we have no means of translating the geonetwork user's profile along with the role membership, so we are using RegisteredUser by default when affecting the user to a GN group. 
It would be more relevant to use the GN_* membership (e.g. giving GN_EDITOR to each group the user belongs to, if the user is member of the GN_EDITOR role).
