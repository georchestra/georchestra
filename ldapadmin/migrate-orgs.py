# Copyright (C) 2009-2016 by the geOrchestra PSC
#
# This file is part of geOrchestra.
#
# geOrchestra is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option)
# any later version.
#
# geOrchestra is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along with
# geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
from ldap3 import Connection, SUBTREE
from ldap3 import MODIFY_ADD, MODIFY_DELETE
import re

# Please configure following VARIABLE in order to match your local configuration

# URI of LDAP server
LDAP_URI = 'ldap://localhost:389'

# credentials to use when connecting to LDAP
LDAP_BINDDN = 'cn=admin,dc=georchestra,dc=org'
LDAP_PASSWD = 'secret'

# where to search orgs in LDAP
ORGS_DN = 'ou=orgs,dc=georchestra,dc=org'

# where to search users in LDAP
USERS_DN = 'ou=users,dc=georchestra,dc=org'

# object class of ldap object contaning user definition
USER_OBJECT_CLASS = 'inetOrgPerson'

# End of configurable part, you should not modify things after this line


# utils classes

class LdapError(Exception):
    """exception about ldap update."""

    def __init__(self, msg):
        self.msg = msg

    def __str__(self):
        return self.msg


class OrganizationHelper:
    """Helper for organisation actions :
       * check if an organization exists
       * add user to an organization
       * generate unique organization identifier
       * create organization (empty registered)"""

    sanitize_regexp = re.compile('[^a-zA-Z_]')

    def __init__(self, base_user_dn, base_org_dn, connection):
        self.base_user_dn = base_user_dn
        self.base_org_dn = base_org_dn
        self.connection = connection

    def sanitizeOrgName(self, org_name):
        return self.sanitize_regexp.sub('_', org_name)

    def addUserToOrg(self, user_uid, org_cn):
        user_full_dn = "uid=%s,%s" % (user_uid, self.base_user_dn)
        org_full_dn = "cn=%s,%s" % (org_cn, self.base_org_dn)

        # check if user is not already in this org
        self.connection.search(search_base=self.base_org_dn, search_filter="(cn=%s)" % org_cn,
                               search_scope=SUBTREE, attributes=['cn', 'member'])
        for res in self.connection.entries:
            if "member" in res and res.member == user_full_dn:
                print("User %s is already present in %s, so just removing 'o' attribut" % (user_full_dn, org_full_dn))
                return

        if not self.connection.modify(org_full_dn, {"member": [(MODIFY_ADD, [user_full_dn])]}):
            raise LdapError("Error while adding %s to %s" % (user_uid, org_full_dn))
        else:
            print("Adding %s to %s" % (user_full_dn, org_full_dn))

    def createOrg(self, org_id):
        if not self.connection.add("cn=%s,%s" % (org_id, self.base_org_dn),
                                   ["groupOfMembers", "top"],
                                   {"businessCategory" : "REGISTERED",
                                    "seeAlso": "o=%s,%s" % (org_id, self.base_org_dn)}):
            raise LdapError("Unable to create groupOfMembers : %s" % org_id)

        if not self.connection.add("o=%s,%s" % (org_id, self.base_org_dn),
                                   ["organization", "top"]):
            raise LdapError("Unable to create organization : %s" % org_id)

    def exists(self, org_id):
        return self.connection.search(search_base=self.base_org_dn, search_filter='(cn=%s)' % org_id)


reg = re.compile(r"^cn=([^,]+),%s" % ORGS_DN)

conn = Connection(LDAP_URI, LDAP_BINDDN, LDAP_PASSWD, auto_bind=True)
conn2 = Connection(LDAP_URI, LDAP_BINDDN, LDAP_PASSWD, auto_bind=True)

conn.search(search_base=USERS_DN,
            search_filter='(objectClass=%s)' % USER_OBJECT_CLASS,
            search_scope=SUBTREE,
            attributes=['uid', 'cn', 'memberOf', 'o'])

orgHelper = OrganizationHelper(USERS_DN, ORGS_DN, conn2)

# Browsing Users
for user in conn.entries:

    uid = user.uid.value

    # check if 'o' field exists
    if "o" not in user:
        print("No org specified, skipping this user : '%s'\n" % user.uid)
        continue
    else:
        print("Migrating User '%s' with org : '%s'" % (user.uid, user.o))

    # Sanitize org name
    org_cn = orgHelper.sanitizeOrgName(user.o.value)
    org_dn = "cn=%s,%s" % (org_cn, ORGS_DN)

    # check if organization exists and create it if necessary
    if not orgHelper.exists(org_cn):
        print("Creating new organization : %s" % (org_dn))
        orgHelper.createOrg(org_cn)

    # add user to org (if not already present)
    orgHelper.addUserToOrg(user.uid, org_cn)

    # remove 'o' attribute of user
    if conn2.modify("uid=%s,%s" % (user.uid.value, USERS_DN), {"o": [(MODIFY_DELETE, [user.o.value])]}):
        print("Deleting 'o' attribut on uid=%s,%s" % (user.uid.value, USERS_DN))
    else:
        raise LdapError("Unable to remove 'o' attribut on uid=%s,%s" % (user.uid.value, USERS_DN))

    # Add new line to separate each user migration
    print("")
