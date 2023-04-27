# LDAP

![LDAP](https://github.com/georchestra/georchestra/workflows/LDAP/badge.svg)

This folder holds the required files to configure and populate an OpenLDAP directory before installing the [geOrchestra](http://www.georchestra.org) SDI.

Please refer to the [geOrchestra documentation](https://github.com/georchestra/georchestra/blob/master/docs/setup/openldap.md) for instructions.

## groupofmembers.ldif

This file imports ```groupOfMembers``` LDAP objectClass into OpenLdap available schemas. It allows to have empty groups, which the default ```groupOfNames``` doesn't permit. ```groupOfMembers``` comes from RFC2037bis and is used in lots of places.

## bootstrap.ldif

This file creates the database, with an LMDB backend (please refer to [issue 856](https://github.com/georchestra/georchestra/issues/856) for more information regarding the backend type).


## root.ldif

This files creates the root DN, which is by default ```dc=georchestra,dc=org```.


## georchestra.ldif

This file creates a basic LDAP tree for geOrchestra.

It creates 5 Organisational Units (ou):
 * one for users: ```ou=users,dc=georchestra,dc=org```
 * one for pending users (waiting for validation): ```ou=pendingusers,dc=georchestra,dc=org```
 * one for roles: ```ou=roles,dc=georchestra,dc=org```
 * one for orgs: ```ou=orgs,dc=georchestra,dc=org```
 * and a last one for pending orgs (waiting for validation): ```ou=pendingorgs,dc=georchestra,dc=org```

The basic users:
 * ```testuser``` has the USER role. The password is **testuser**.
 * ```testreviewer``` has the USER & GN_REVIEWER roles. The password is **testreviewer**.
 * ```testeditor``` has the USER & GN_EDITOR roles. The password is **testeditor**.
 * ```testadmin``` has the USER, GN_ADMIN, ADMINISTRATOR and MOD_* roles. The password is **testadmin**.
 * ```testdelegatedadmin``` has the USER role. Is able to grant the EXTRACTORAPP & GN_EDITOR roles to members of the psc & c2c orgs. The password is **testdelegatedadmin**.
 * ```geoserver_privileged_user``` is a required user. It is internally used by the geofence module. The default password is ```gerlsSnFd6SmM``` (you should change it, and update the datadir as explained in its [README](https://github.com/georchestra/datadir/blob/18.06/README.md)).
 * ```testpendinguser``` is inside the pending users organizational unit, which means an admin has to validate it. The password is **testpendinguser**.

Please note that `test*` users should be deleted before going into production !

The roles:
 * ```SUPERUSER``` grants access to the console webapp (where one can manage users and roles),
 * ```ADMINISTRATOR``` is for GeoServer administrators,
 * ```GN_ADMIN``` is for GeoNetwork administrators,
 * ```GN_EDITOR``` is for metadata editors,
 * ```GN_REVIEWER``` is for metadata reviewers,
 * ```REFERENT``` allows users to edit their own organisation (basic information only),
 * ```USER``` is for the basic SDI users.

Other roles can be defined by the platform administrator, using eg the console.

Finally, it creates 4 organizations:
 * `psc` with the following members: `testadmin` and `testuser`
 * `c2c` with the following member: `testeditor`
 * `cra` with the following member: `testreviewer`
 * `pendingorg`, inside the pending orgs organizational unitm, which means an admin has to validate it. It contains the following member: `testpendinguser`.

Note that for each organization, two objects are created in the LDAP tree:
 * an `organization` object that contains the fields that describe the organization (`o`, `businessCategory`, `postalAddress`)
 * a `groupOfMembers` object that mainly contains one `member` entry for each of its members.


## Adding objectClass georchestraUser on users

In some corner cases, it can be necessary to manually add objectClass `georchestraUser` on some users.

This can be done with this script:

```bash
cat <<EOF > add_geochestraUser.py
#!/usr/bin/env python3

import argparse
import uuid
import sys
from collections import OrderedDict

import ldif


def main():
    parser = argparse.ArgumentParser(description='Generate patch for incomplete LDAP georchestra users.')
    parser.add_argument(
        'infile',
        nargs='?',
        type=argparse.FileType('rb'),
        default=sys.stdin.buffer,
        help="input ldif file",
    )
    parser.add_argument(
        'outfile',
        nargs='?',
        type=argparse.FileType('wb'),
        default=sys.stdout.buffer,
        help="output ldif patch file",
    )
    args = parser.parse_args()

    generate_patch(args.infile, args.outfile)


def generate_patch(infile, outfile):
    parser = ldif.LDIFParser(infile)
    writer = ldif.LDIFWriter(outfile)

    for dn, record in parser.parse():
        if (
            'inetOrgPerson' in record["objectClass"]
            and not "georchestraUser" in record["objectClass"]
        ):
            sys.stdout.write(f"""
dn: {dn}
changetype: modify
add: objectClass
objectClass: georchestraUser
-
add: georchestraObjectIdentifier
georchestraObjectIdentifier: {str(uuid.uuid4())}
"""
            )


if __name__ == "__main__":
    main()
EOF
```

Which can be used like this:

```bash
python3 -m venv venv
venv/bin/pip install ldif

ldapsearch -x -LLL \
   -H "ldap://ldap:389" \
   -D "cn=admin,dc=georchestra,dc=org" \
   -w "secret" \
   -b "ou=users,dc=georchestra,dc=org" \
   '(&(objectClass=inetOrgPerson)(!(objectClass=georchestraUser)))' \
| venv/bin/python add_georchestraUser.py \
| ldapmodify -x \
   -H "ldap://ldap:389" \
   -D "cn=admin,dc=georchestra,dc=org" \
   -w "secret"
```
