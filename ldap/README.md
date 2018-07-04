# LDAP

This folder holds the required files to configure and populate an OpenLDAP directory before installing the [geOrchestra](http://www.georchestra.org) SDI.  

Please refer to the [geOrchestra documentation](https://github.com/georchestra/georchestra/blob/master/README.md) for instructions, and **use the branch matching your geOrchestra version** !

## groupofmembers.ldif

This file imports ```groupOfMembers``` LDAP objectClass into OpenLdap available schemas. It allows to have empty groups, which the default ```groupOfNames``` doesn't permit. ```groupOfMembers``` comes from RFC2037bis and is used in lots of places.

## bootstrap.ldif

This file creates the database, with an LMDB backend (please refer to [issue 856](https://github.com/georchestra/georchestra/issues/856) for more information regarding the backend type).


## root.ldif

This files creates the root DN, which is by default ```dc=georchestra,dc=org```.


## georchestra.ldif

This file creates a basic LDAP tree for geOrchestra.

It creates 3 Organisational Units (ou):
 * one for users: ```ou=users,dc=georchestra,dc=org``` 
 * an other one for roles: ```ou=roles,dc=georchestra,dc=org```
 * a last one for orgs: ```ou=orgs,dc=georchestra,dc=org```

The basic users:
 * ```testuser``` has the USER role. The password is **testuser**.
 * ```testpendinguser``` has the PENDING role, which means an admin has to validate it. The password is **testpendinguser**.
 * ```testreviewer``` has the USER & GN_REVIEWER roles. The password is **testreviewer**.
 * ```testeditor``` has the USER & GN_EDITOR roles. The password is **testeditor**.
 * ```testadmin``` has the USER, GN_ADMIN, ADMINISTRATOR and MOD_* roles. The password is **testadmin**
 * ```testdelegatedadmin``` has the USER role. Is able to grant the EXTRACTORAPP & GN_EDITOR roles to members of the psc & c2c orgs. The password is **testdelegatedadmin**
 * ```geoserver_privileged_user``` is a required user. It is internally used by the extractorapp, mapfishapp & geofence modules. The default password is ```gerlsSnFd6SmM``` (you should change it, and update the ```shared.privileged.geoserver.pass``` option in your shared.maven.filters file).

Please note that `test*` users should be deleted before going into production !

The roles:
 * ```SUPERUSER``` grants access to the console webapp (where one can manage users and roles),
 * ```ADMINISTRATOR``` is for GeoServer administrators,
 * ```MOD_EXTRACTORAPP``` grants access to the extractor application,
 * ```GN_ADMIN``` is for GeoNetwork administrators,
 * ```GN_EDITOR``` is for metadata editors,
 * ```GN_REVIEWER``` is for metadata reviewers,
 * ```USER``` is for the basic SDI users,
 * ```PENDING``` is the landing group for people asking an account on the platform. This group gives no right by default.

Other roles can be defined by the platform administrator, using eg the console.
