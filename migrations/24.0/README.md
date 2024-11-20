# From 23.x to 24.0.x

## Header

By default, geOrchestra uses the new header https://github.com/georchestra/header.

Some variables must be set in datadir's `default.properties` file.

```properties
# Variable use to keep the old iframe header.
# Set headerUrl accordingly
# Default false
useLegacyHeader=false

# Header script for web component header
# https://github.com/georchestra/header
headerScript=https://cdn.jsdelivr.net/gh/georchestra/header@dist/header.js

# Logo URL
# Used to set header's logo.
logoUrl=https://www.georchestra.org/public/georchestra-logo.svg

# Stylesheet used to override default colors of header
# More design can be set by overriding default classes & styles
# Default empty string
# georchestraStylesheet=http://my-domain-name/stylesheet.css
```

To edit colors and some other CSS properties, you can override the default stylesheet by setting the `georchestraStylesheet` variable.

```css
/* Example of custom stylesheet */
header {
    --georchestra-header-primary: #e20714;
    --georchestra-header-secondary: white;
    --georchestra-header-primary-light: white;
    --georchestra-header-secondary-light: #eee;
}
.admin-dropdown>li.active {
    background-color: red;
    color: white;
}
```
This header can be totally customized by creating a fork of the header repository and setting the `headerScript` variable accordingly.


## GeoNetwork 4.2.4 to 4.2.8 migration notes

After the upgrade :
- Delete index and reindex .
- JS and CSS cache must be cleared.

using the url : `/geonetwork/srv/eng/admin.console?debug#/tools`

⚠️ Important info about Harvesters :
- Simple URL Harvester must now have their `Element for the UUID of each record` prefixed with a slash.
- XSL transformations must be updated, e.g. :
    - `iso19115-3.2018:convert/fromJsonLdEsri` becomes `schema:iso19115-3.2018:convert/fromJsonLdEsri`

## Elasticsearch

Elasticsearch has been upgraded to 7.17.15.

## LDAP

The `IMPORT` role was added to the ldap schema.

This role allows user to have access to the import tool (datafeeder).

By default, users can't use datafeeder application.

```
ldapadd -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret   << EOF
dn: cn=IMPORT,ou=roles,dc=georchestra,dc=org
objectClass: georchestraRole
objectClass: top
objectClass: groupOfMembers
cn: IMPORT
description:  This role grants access to import functionnality through datafeeder application.
EOF
```

### Migrating to Debian Bookworm

The `openldap` version has been upgraded, and with this new version, this is no
longer needed to have custom schemas with extensions loaded under `/etc/ldap/slapd.d`,
as they are contained into the plugin (.so) file.

If you keep your previous `/etc/ldap/slapd.d` content and try to launch
`slapd` on it, you may encounter the following error, preventing it to start:

```
0x7f84fc502200 config error processing cn={7}ppolicy,cn=schema,cn=config: olcAttributeTypes: Duplicate attributeType: "1.3.6.1.4.1.42.2.27.8.1.1"
0x7f84fc502200 slapd stopped.
0x7f84fc502200 connections_destroy: nothing to destroy
```

In order to fix this, you just need to browse your `/etc/ldap/slapd.d/cn=schema`,
and remove the `cn={7}ppolicy.ldif` file (Note: file naming may vary).

### Password rotation policy

To activate password rotation policy for ldap, user need to run these commands:

```
ldapmodify -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret  << EOF
dn: olcOverlay={0}ppolicy,olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcPPolicyDefault
olcPPolicyDefault: cn=default,ou=pwpolicy,dc=georchestra,dc=org


EOF

```
```
ldapadd -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret   << EOF
dn: ou=pwpolicy,dc=georchestra,dc=org
objectClass: organizationalUnit
objectClass: top
ou: pwpolicy

dn: cn=default,ou=pwpolicy,dc=georchestra,dc=org
objectClass: person
objectClass: pwdPolicyChecker
objectClass: pwdPolicy
cn: pwpolicy
sn: pwpolicy
pwdAttribute: userPassword
pwdMinAge: 0
pwdMaxAge: 31536000
pwdGraceAuthnLimit: 0
pwdExpireWarning: 2592000

dn: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org
objectClass: person
objectClass: pwdPolicyChecker
objectClass: pwdPolicy
cn: pwpolicy
cn: pwd-no-expire
sn: pwpolicy
pwdAttribute: userPassword
pwdMinAge: 0
pwdMaxAge: 0


EOF

```
To activate rotation password policy for all users, please run script 'set_rotation_policy_for_all_users.sh' located in this very same folder :
```
sh set_rotation_policy_for_all_users.sh
```
To disable rotation policy for non-human users, run the following :
```
ldapmodify -H ldap://localhost:389  -D cn=admin,dc=georchestra,dc=org -w secret  << EOF
dn: uid=geoserver_privileged_user,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org

dn: uid=idatafeeder,ou=users,dc=georchestra,dc=org
changetype: modify
add: pwdPolicySubentry
pwdPolicySubentry: cn=pwd-no-expire,ou=pwpolicy,dc=georchestra,dc=org


EOF
```
### oAuth2ProviderId attribute on the georchestraUser schema

The `oAuth2ProviderId` user attribute was added to the `georchestraUser` ldap schema.

This attribute will contain the OAuth2 user identifier when said user is connected using an external identity provider.


To upgrade the ldap, you need first to find the georchestra schema definition using the following command :

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b cn=schema,cn=config '(cn=*georchestra)' dn
```

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required dn is :

dn: cn={7}georchestra,cn=schema,cn=config

If you find a different dn please update Commands provided in [ldap_migration.ldif](ldap_migration.ldif) file

Also, required olcObjectClasses "georchestraUser" to be changed should be localized using the following command :

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b "cn=schema,cn=config" | grep 1.3.6.1.4.1.53611.1.1.1
```
Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required dn is :

olcObjectClasses: {0}

If you find a different olcObjectClasses number please update commands provided in [ldap_migration.ldif](ldap_migration.ldif) file

Please follow previous steps with precaution before running final script.

Finally, use the following command with the [ldap_migration.ldif](ldap_migration.ldif) file:

```
ldapmodify -H "ldap://ldap:389" -D "cn=admin,dc=georchestra,dc=org" -w "secret" -f ldap_migration.ldif
```

### mail attribute on the organizations

The organisations from the LDAP have been modified so that we can now define a `mail` attribute on them via the Console.
a `objectClass: extensibleObject` has to be added to the existing organisations so that they remain compatible with geOrchestra.

See the provided [add_extensible_object_orgs.sh](add_extensible_object_orgs.sh) shell script for an example to migrate the
organizations.

## ⚠️ Console : Area of competence 

By default the `Area of competence` in console is now disabled.

The functionnality can be enabled in `console.properties` with the line : 
```
competenceAreaEnabled=true
```

## Cas server

Cas server has been upgraded to 6.6.15 and [configuration must be updated accordingly](https://github.com/georchestra/datadir/blob/docker-master/cas/config/cas.properties).

```diff
-cas.service-registry.initFromJson=false
+cas.service-registry.core.init-from-json=false

-cas.authn.oidc.jwks.jwks-file=file:///tmp/keystore.jwksdown
+cas.authn.oidc.jwks.file-system.jwks-file=file:///tmp/keystore.jwksdown

-cas.authn.saml-idp.metadata.location=file:///tmp/
+cas.authn.saml-idp.metadata.file-system.location=file:///tmp/
```

## Datafeeder 

Datafeeder now supports CSV geographic and non-geographic files.  

⚠️ Users must have the `IMPORT` role to use datafeeder.

⚠️ The table name now use the title provided during process steps instead of the file name. 

## Data-api

A new application has been introduced in 24.0.x : [georchestra/data-api](https://github.com/georchestra/data-api).

This application is used to provide a REST API complicant to OGC API Features - Part 1 to access data stored in geOrchestra.
