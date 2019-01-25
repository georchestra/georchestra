# From 18.06 to 18.12

## LDAP upgrade

With [#2108](https://github.com/georchestra/georchestra/issues/2108), pending orgs and users belong to newly introduced Organizational Units.
It was indeed estimated that it would be simpler, since external services might auth users on the ou=users or synchronize data from the ou=orgs branch, and they would have to be modified to specialcase the PENDING ones.

As a result, the LDAP DIT should be upgraded with the provided [upgrade from 18.06 to 18.12 LDIF](upgrade_ldap_from_18.06_to_18.12.ldif), which creates two new Organizational Units for pending users and orgs.


Please note that running a 18.12 console on a 18.06 LDAP will make previously pending users and orgs considered as registered, valid objects (except that pending users will not have the required roles to log in).
It is thus recommended to discard them before doing the transition to 18.12.
