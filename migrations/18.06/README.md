# UPGRADING from 17.12 to 18.06

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