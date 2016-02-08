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
from ldap3.core.exceptions import LDAPKeyError
import re

# Please configure following VARIABLE in order to match your local configuration

# URI of LDAP server
LDAP_URI    = 'ldap://localhost:389'

# whether to bind to LDAP directory with credentials or anonymous
BIND_WITH_CREDENTIALS = True

# credentials to use when connecting to LDAP
LDAP_BINDDN = 'cn=admin,dc=georchestra,dc=org'
LDAP_PASSWD = 'secret'

# where to search groups in LDAP
GROUPS_DN   = 'ou=groups,dc=georchestra,dc=org'

# object class of ldap object contaning group definition
GROUP_OBJECT_CLASS = 'groupOfMembers'



reg = re.compile(r"^uid=([a-zA-Z0-9_-]+),")

if BIND_WITH_CREDENTIALS:
    conn = Connection(LDAP_URI, LDAP_BINDDN, LDAP_PASSWD, auto_bind=True)
else:
    conn = Connection(LDAP_URI, auto_bind=True)

conn.search(search_base = GROUPS_DN,
            search_filter = '(objectClass=%s)' % GROUP_OBJECT_CLASS,
            search_scope = SUBTREE,
            attributes = ['cn', 'member'])

usersGroups = {}
print("""

Browsing Groups 

""")
for entry in conn.entries:
    print("Group found : %s" % entry.cn)
    try:
        for user in entry.member.values:
            if user not in usersGroups:
                usersGroups[user] = [entry.cn.value]
            else:
                usersGroups[user].append(entry.cn.value)
            print("\tUser found : %s" % user)
    except LDAPKeyError:
        continue

print("""

Listing groups *by users*

""")    
sqlUpdates = {}
for key in usersGroups:
    user = key
    matches = reg.match(user)
    user = matches.group(1)
    members = usersGroups[key]
    sqlUpdates[user] = "ARRAY['" + "','".join(members) + "']"
    print("User %s in groups : '%s'" % (user, "','".join(members)))

print("""

Final SQL queries to insert in SQL file

""")
for user in sqlUpdates:
    value = sqlUpdates[user]
    print ("UPDATE ogcstatistics.ogc_services_log_old SET roles = %s WHERE user_name = '%s';" % (value, user))

 
