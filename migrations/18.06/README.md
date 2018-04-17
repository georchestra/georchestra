# UPGRADING from 17.12 to 18.06

The `MOD_ANALYTICS` is not used anymore. You should remove it, and instead grant the `SUPERUSER` role to these users.
The `MOD_EXTRACTORAPP` role was renamed into `EXTRACTORAPP`.

## Security-proxy rewrite

After [PR #1902](https://github.com/georchestra/georchestra/pull/1902); the
security-proxy has been refactored and update to new dependencies. This
involves the main following points:

* Switch to Java8 mandatory, at least for the servlet container running the Security-proxy

* In case you have a customization on the header webapp, the new logout
  entrypoint is now `/logout`, and no more `j_spring_security_logout`,

* the login entrypoint is no longer `j_spring_cas_security_check`, but `/login/cas` instead.

If you are using the default configuration profile, you should be good to go,
but please pay a particular attention in case of spring-security customizations
in your profile, you would probably have to update your configuration, as the
spring version bump will require some changes on the XML files especially.


## Datadir changes

With [#1919](https://github.com/georchestra/georchestra/issues/1919), `orgsSearchBaseDN` was renamed into `orgSearchBaseDN`. This impacts the console and security-proxy webapps. Please update your datadir files.

With #1968 all connections to the DB are performed with role `georchestra` instead of `www-data`.


## URL pattern changes

First of all, ldapadmin webapp is renamed into console.
Also, with [#1924](https://github.com/georchestra/georchestra/issues/1924), the admin console moves from `/ldapadmin/console/` to `/console/manager/`

## ReCaptcha v2

With [#1938](https://github.com/georchestra/georchestra/pull/1938) we upgraded ReCaptcha to v2, a mandatory step since v1 has been deprecated by Google. This requires that you generate a new public/private keypair on https://www.google.com/recaptcha/ and update your geOrchestra datadir according to [georchestra/datadir@0ff17b]( https://github.com/georchestra/datadir/commit/0ff17b3d2e7fb265dbf64fc0c65f7ee01ef39dea)
