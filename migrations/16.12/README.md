# UPGRADING from 15.12 to 16.12

## Database

An SQL migration script is provided in the [migrations/16.12](.) folder. It creates two new schemas, and several tables in the `extractorapp` schema.
It is a required addition before the `extractorapp` and `atlas` modules are started.

The LDAPadmin webapp should also create these new tables automatically during the first startup:
 * ldapadmin.admin_attachments
 * ldapadmin.admin_emails
 * ldapadmin.admin_emails_attachments
 * ldapadmin.admin_log
 * ldapadmin.email_template

## LDAP

There are several important changes in the LDAP:
 * What we used to call `groups` is now called `roles`. As a result, in the LDAP tree, eg `ou=groups,dc=georchestra,dc=org` should be renamed into `ou=roles,dc=georchestra,dc=org`.
 * A new `ou=orgs,dc=georchestra,dc=org` has been created to host the Organization objects. An org is made of an `organization` linked with a `groupOfMembers`-typed object.
 * **Roles are now expected to have parent roles**, eg role `EL` is the parent of role `EL_DSI`, which is the parent of role `EL_DSI_SERVICE`. This allows for an easy browsing between roles in the new Console application, but there's [more to come](https://github.com/georchestra/georchestra/issues/1559).
 * Several static roles have been renamed:
   * `SV_USER` -> `USER` is the basic role **expected for all geOrchestra users**, including administrators.
   * `SV_ADMIN` -> `GN_ADMIN` is for the GeoNetwork administrators.
   * `SV_REVIEWER` -> `GN_REVIEWER` is for the GeoNetwork metadata reviewers.
   * `SV_EDITOR` -> `GN_EDITOR` is for the GeoNetwork metadata editors.
 * All users (`inetOrgPerson`) should also have the `shadowAccount` objectClass. This allows to set an account expiration date.
 * We now require the `memberof` overlay to be configured with `olcMemberOfRefInt: TRUE` (it was previously set to `FALSE`). It enables integrity check between the `member` and `memberOf` values. Without this parameter, the `member` and `memberOf` can get out of sync. For more information, see [this commit](https://github.com/georchestra/georchestra/commit/534606e97186988ba2a672b729b2031e55256cfc).

Since we introduced orgs, the `o` field which can be found in every `inetOrgPerson` instance is not used anymore. Instead, the org title should be used.
In the [migrations/16.12](migrations/16.12) folder, we added a python script which helps creating the new Organizations in your LDAP, based on your user's current organization titles (from the `o` attribute).

The recommended way to upgrade your LDAP database is the following:
 * dump all LDAP objects in a LDIF file
 * edit the LDIF file
 * reimport it

Here's the [full LDAP diff](https://gist.github.com/fvanderbiest/7c8ae5656e29325cc0372eb2dc0519d8) for the curious ones.

## Configurations

**The geOrchestra datadir is now mandatory**. It is indeed possible to fully configure a standard geOrchestra instance without having to build it.  
Please read the notes from the [georchestra/datadir](https://github.com/georchestra/datadir/blob/master/README.md) repository to upgrade.

For the mapfishapp viewer, we restored the ability to choose which context is shown by default through the `GEOR.custom.DEFAULT_WMC` config option.
Read [#1534](https://github.com/georchestra/georchestra/pull/1354) for more.

GeoServer datadir:
 * the logging configuration has been improved
 * the rest configuration should be modified to take into account roles renaming
 * the `role` and `usergroup` services have been plugged onto the LDAP, which provides **automatic role creation in GeoServer**.
Please refer to this [geoserver datadir full diff](https://gist.github.com/fvanderbiest/2ae1bac7e4dd3023e1060b8deab6683b) for upgrade guidance.

Rewrite rules which add an extra trailing slash on webapps are no longer needed on the frontend webserver, since it is now managed security-proxy side.
Please refer to [#1502](https://github.com/georchestra/georchestra/pull/1502/files) for more information.

If you had JSP files customised in your own profile, you should synchronise them with upstream changes. Namely, with [#1480](https://github.com/georchestra/georchestra/issues/1480) we removed the need for a `header.js` file, and the header iframe's calling `onload="_headerOnLoad(this)"`. This also improves the header response times.


## Others

`ROLE_ANONYMOUS` does not exist anymore.  
Applications should not use it. Instead, they should rely on the `sec-username` & `sec-roles` values.

The security-proxy-generated `sec-org` header now maps to the user's Org `cn`, rather than the inetOrgPerson's `o` field. In addition, the `sec-orgname` header is now added to proxied requests, which maps to the user's organism title (ie the `o` field from the Org's groupOfMembers instance). This means that applications which were relying on the `sec-org` header should now use `sec-orgname` instead.
