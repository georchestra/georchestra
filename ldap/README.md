# LDAP

This folder holds the required files to configure and populate an OpenLDAP directory before installing the [geOrchestra](http://www.georchestra.org) SDI.  

Please refer to the [geOrchestra documentation](https://github.com/georchestra/georchestra/blob/master/README.md) for instructions, and **use the branch matching your geOrchestra version** !

## groupofmembers.ldif

This file imports ```groupOfMembers``` LDAP objectClass into OpenLdap available schemas. It allows to have empty groups, which the default ```groupOfNames``` doesn't permit. ```groupOfMembers``` comes from RFC2037bis and is used in lots of places.

## bootstrap.ldif

This file creates the database.

Note that, depending on your Debian version, you might need to use a different version of this file: 
 * **geOrchestra <= 14.12** is supposed to be installed on **Debian Wheezy**, where OpenLdap's default backend is **HDB**
 * **geOrchestra >= 15.06** targets **Debian Jessie** where the backend is **LMDB**

Please refer to [issue 856](https://github.com/georchestra/georchestra/issues/856) for more information.


## root.ldif

This files creates the root DN, which is by default ```dc=georchestra,dc=org```.


## georchestra.ldif

This file creates a basic LDAP tree for geOrchestra.

It creates 2 Organisational Units (ou):
 * ```ou=users,dc=georchestra,dc=org``` 
 * ```ou=groups,dc=georchestra,dc=org```

The basic users:
 * ```testuser``` is a member of SV_USER. The password is **testuser**.
 * ```testreviewer``` is a member of SV_REVIEWER. The password is **testreviewer**.
 * ```testeditor``` is a member of SV_EDITOR. The password is **testeditor**.
 * ```testadmin``` is a member of SV_ADMIN, ADMINISTRATOR and MOD_* groups. The password is **testadmin**
 * ```geoserver_privileged_user``` is a required user. It is internally used by the extractorapp, mapfishapp & geofence modules. The default password is ```gerlsSnFd6SmM``` (you should change it, and update the ```shared.privileged.geoserver.pass``` option in your shared.maven.filters file).

The groups:
 * ```ADMINISTRATOR``` is for GeoServer administrators,
 * ```MOD_LDAPADMIN``` grants access to the ldapadmin webapp (where one can manage users and groups),
 * ```MOD_ANALYTICS``` grants access rights to the analytics application,
 * ```MOD_EXTRACTORAPP``` grants access to the extractor application,
 * ```SV_ADMIN``` is for GeoNetwork administrators,
 * ```SV_EDITOR``` is for metadata editors,
 * ```SV_REVIEWER``` is for metadata reviewers,
 * ```SV_USER``` is for the basic SDI users,
 * ```PENDING``` is the landing group for people asking an account on the platform. This group gives no right by default.
