# From 19.04 to 19.06

## LDAP upgrade

With [#2149](https://github.com/georchestra/georchestra/issues/2149), a new attribute (privacyPolicyAgreementDate) is added to the geOrchestra users in LDAP, to keep track of the date when the user agreed to the terms and conditions (or privacy policy) of the platform.

As a result, the LDAP DIT should be upgraded with the provided [upgrade from 19.04 to 19.06 LDIF](upgrade_ldap_from_19.04_to_19.06.ldif), which creates a new geOrchestra schema, and the required objectClasses and attributes.

All the existing users will have an empty privacyPolicyAgreementDate, since they have never been asked to agree to the conditions. New users will have that attribute filled with the account creation date.
