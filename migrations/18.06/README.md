# From 17.12 to 18.06

With [#1902](https://github.com/georchestra/georchestra/pull/1902), the security-proxy dependencies have been updated. As a consequence, **Java 8 is now mandatory** (at least for the servlet container running the security-proxy).

## URL pattern changes

First of all, you should note that the `ldapadmin` webapp was renamed into `console`.
Also, with [#1924](https://github.com/georchestra/georchestra/issues/1924), the admin console path changed from `/ldapadmin/console/` to `/console/manager/`.

The extractorapp UI has been removed and replaced with a viewer addon (which makes sense to not duplicate viewing efforts).
Users may still requests extractions, provided the extractorapp webapp is installed and the extractor addon activated. Forcing this addon to load on viewer startup may be achieved using eg `/mapfishapp/?addons=extractor_0`.

With [#1902](https://github.com/georchestra/georchestra/pull/1902), the security-proxy has been refactored. As a consequence:
* the login entrypoint is now `/login/cas` instead of `/j_spring_cas_security_check`,
* the logout entrypoint is now `/logout`, instead of `/j_spring_security_logout`.

## ROLE changes

 * `MOD_LDAPADMIN` becomes `SUPERUSER`, to make it clear that users with this role are allmighty.
 * `MOD_ANALYTICS` does not exist anymore. You should remove it, eventually granting the `SUPERUSER` role to these users.
 * The `MOD_EXTRACTORAPP` role was renamed into `EXTRACTORAPP`.
 * The `MOD_EMAILPROXY` role was renamed into `EMAILPROXY`.

The `ORGADMIN` role is transparently added by the console to delegated admins. Users having this role are allowed to access the console webapp.

 ## Upgrading to ReCaptcha v2

 With [#1938](https://github.com/georchestra/georchestra/pull/1938) we upgraded ReCaptcha to v2, a mandatory step since v1 has been deprecated by Google. This requires that you generate a new public/private keypair on https://www.google.com/recaptcha/ and update your geOrchestra datadir according to [georchestra/datadir@0ff17b]( https://github.com/georchestra/datadir/commit/0ff17b3d2e7fb265dbf64fc0c65f7ee01ef39dea)

## Upgrading your datadir

Lots of changes happened in the datadir for this release... If you did not customize it deeply, you might be better off starting with a fresh clone of branch 18.06 and apply your changes on top of it.

If you plan to upgrade your existing datadir (matching version 17.12), here's a [full diff](https://gist.github.com/fvanderbiest/1049126fc13a921f2d9a1adb6f5dc5a1) between 17.12 and 18.06. Remember: we do not recommend this, since chances are high that you miss some of the changes, like:
 * ldapadmin renamed into console everywhere in variable names
 * group renamed role in variable names
 * `orgsSearchBaseDN` renamed into `orgSearchBaseDN`
 * all connections to the DB are performed with role `georchestra` instead of `www-data`
 * public SDI url & header height factorized into default.properties
 * ...

As you can see, this release introduces a [default.properties](https://github.com/georchestra/datadir/blob/master/default.properties) file at the datadir root, which factorizes variables across apps. It can be omitted, but then, its variables should be replicated in webapp specific properties files.

Also, the `moderatorEmail` config option from the ldapadmin/console module was removed.
With [#2124](https://github.com/georchestra/georchestra/pull/2124), moderation emails are now dynamically sent to users having the `SUPERUSER` role (plus Org-scoped admins, if any).

## Upgrading your databases

Please run the provided [upgrade from 17.12 to 18.06 SQL script](db_migration.sql), which renames the `ldapadmin` schema and creates the `delegation` table.
