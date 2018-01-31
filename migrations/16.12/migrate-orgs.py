# Copyright (C) 2009-2017 by the geOrchestra PSC
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
from difflib import SequenceMatcher

# Please configure following VARIABLE in order to match your local configuration

# URI of LDAP server
LDAP_URI = 'ldap://localhost:389'

# Main LDAP domain
LDAP_DOMAIN = 'dc=georchestra,dc=org'

# credentials to use when connecting to LDAP
LDAP_BINDDN = 'cn=admin,%s' % LDAP_DOMAIN
LDAP_PASSWD = 'secret'

# where to search orgs in LDAP
ORGS_BRANCH_NAME = 'orgs'
ORGS_DN = 'ou=%s,%s' % (ORGS_BRANCH_NAME, LDAP_DOMAIN)

# where to search users in LDAP
USERS_DN = 'ou=users,%s' % LDAP_DOMAIN

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

    sanitize_regexp = re.compile('[^a-zA-Z0-9_-]')

    def __init__(self, base_user_dn, base_org_dn, connection):
        self.base_user_dn = base_user_dn
        self.base_org_dn = base_org_dn
        self.connection = connection
        self.created_orgs = {}

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

    def findSimilar(self, org_name):
        max_ratio = 0
        max_org_id = None
        max_org_name = None
        for key in self.created_orgs:
            ratio = SequenceMatcher(None, org_name, self.created_orgs[key]).ratio()
            if ratio > max_ratio:
                max_ratio = ratio
                max_org_id = key
                max_org_name = self.created_orgs[key]
            if(ratio > 0.6):
                return (True, key, self.created_orgs[key], SequenceMatcher(None, org_name, self.created_orgs[key]).ratio())
        return (False, max_org_id, max_org_name, max_ratio)

    def createOrg(self, org_id, org_name):
        (match, similar_org_id, similar_org_name, score) = self.findSimilar(org_name)
        if match:
            print("Similar org detected : %s is similar to %s %s (%s)" % (org_name, similar_org_name, score, similar_org_id))
        else :
            print("No match found : Max ratio %s with : %s (%s)" % (score, similar_org_name, similar_org_id))
        if not self.connection.add("cn=%s,%s" % (org_id, self.base_org_dn),
                                   ["groupOfMembers", "top"],
                                   {"businessCategory" : "REGISTERED",
                                    "o" : org_name,
                                    "seeAlso": "o=%s,%s" % (org_id, self.base_org_dn)}):
            raise LdapError("Unable to create groupOfMembers : %s" % org_id)
        self.created_orgs[org_id] = org_name
        if not self.connection.add("o=%s,%s" % (org_id, self.base_org_dn),
                                   ["organization", "top"]):
            raise LdapError("Unable to create organization : %s" % org_id)

    def exists(self, org_id):
        return self.connection.search(search_base=self.base_org_dn, search_filter='(cn=%s)' % org_id)


reg = re.compile(r"^cn=([^,]+),%s" % ORGS_DN)

conn = Connection(LDAP_URI, LDAP_BINDDN, LDAP_PASSWD, auto_bind=True)
conn2 = Connection(LDAP_URI, LDAP_BINDDN, LDAP_PASSWD, auto_bind=True)

# Create Orgs organizational unit if needed
if not conn.search(search_base=LDAP_DOMAIN, search_filter='(ou=%s)' % ORGS_BRANCH_NAME):
    if not conn.add(ORGS_DN,
                               ["organizationalUnit", "top"],
                               {"ou" : ORGS_BRANCH_NAME}):
        raise LdapError("Unable to create orgs organizational unit")
    print('Successful creation of %s entry' % ORGS_DN)

conn.search(search_base=USERS_DN,
            search_filter='(objectClass=%s)' % USER_OBJECT_CLASS,
            search_scope=SUBTREE,
            attributes=['uid', 'cn', 'memberOf', 'o'])

orgHelper = OrganizationHelper(USERS_DN, ORGS_DN, conn2)

# Browsing Users
for user in conn.entries:

    uid = user.uid.value

    # check if 'o' field exists
    if "o" not in user or len(user.o) == 0:
        print("No org specified, skipping this user : '%s'\n" % user.uid)
        continue
    else:
        print("Migrating User '%s' with org : '%s'" % (user.uid, user.o))

    # Sanitize org name
    org_cn = orgHelper.sanitizeOrgName(user.o.value.strip())
    org_dn = "cn=%s,%s" % (org_cn, ORGS_DN)

    # check if organization exists and create it if necessary
    if not orgHelper.exists(org_cn):
        print("Creating new organization : %s" % (org_dn))
        orgHelper.createOrg(org_cn, user.o.value.strip())

    # add user to org (if not already present)
    orgHelper.addUserToOrg(user.uid, org_cn)

    # remove 'o' attribute of user
    if conn2.modify("uid=%s,%s" % (user.uid.value, USERS_DN), {"o": [(MODIFY_DELETE, [user.o.value])]}):
        print("Deleting 'o' attribut on uid=%s,%s" % (user.uid.value, USERS_DN))
    else:
        raise LdapError("Unable to remove 'o' attribut on uid=%s,%s" % (user.uid.value, USERS_DN))

    # Add new line to separate each user migration
    print("")
