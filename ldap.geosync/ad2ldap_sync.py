#!/usr/bin/python
# -*- coding: utf-8 -*-

# objectif : importer les utilisateurs Active Directory dans LDAP
# source : Pierre Mauduit, pierre.mauduit@camptocamp.com

# dump de l'annuaire ldap :
# slapcat > ~/slapcat.ldif
# modification de l'annuaire avec ldapvi :
# ldapvi --host localhost -D "cn=admin,dc=georchestra,dc=univ-fcomte,dc=fr" -w "********" -b "dc=georchestra,dc=univ-fcomte,dc=fr"

# pré-requis
# apt-get install python-ldap

import ldap
import ldap.modlist
import string
import sys
import base64
import ConfigParser

STR_RED   = "\033[01;31m{0}\033[00m"
STR_GREEN = "\033[1;36m{0}\033[00m"

# Script configuration

# dry-run: set it to False to actually write into the OpenLDAP
#DRY_RUN = True
DRY_RUN = False

# Lecture des paramètres AD et LDAP
config = ConfigParser.RawConfigParser()
config.read('.ldap.conf')

try:
    # Active Directory
    IN_LDAP_URI    = config.get('IN_LDAP', 'IN_LDAP_URI')
    IN_LDAP_BINDDN = config.get('IN_LDAP', 'IN_LDAP_BINDDN')
    IN_LDAP_PASSWD = config.get('IN_LDAP', 'IN_LDAP_PASSWD')

    # OpenLDAP
    OUT_LDAP_URI    = config.get('OUT_LDAP', 'OUT_LDAP_URI')
    OUT_LDAP_BINDDN = config.get('OUT_LDAP', 'OUT_LDAP_BINDDN')
    OUT_LDAP_PASSWD = config.get('OUT_LDAP', 'OUT_LDAP_PASSWD')

except ConfigParser.Error, err:
    print 'Oops, une erreur dans votre fichier de conf (%s)' % err
    sys.exit(1)

print 'IN_LDAP_URI:%s' % (IN_LDAP_URI)
print 'IN_LDAP_BINDDN:%s' % (IN_LDAP_BINDDN)
print 'IN_LDAP_PASSWD:%s' % (IN_LDAP_PASSWD)
print 'OUT_LDAP_URI:%s' % (OUT_LDAP_URI)
print 'OUT_LDAP_BINDDN:%s' % (OUT_LDAP_BINDDN)
print 'OUT_LDAP_PASSWD:%s' % (OUT_LDAP_PASSWD)

# branches of the AD to be browsed. the LDAP scope that will be used is a 'one',
# i.e. the immediate children of the node.
IN_USERS_BASEDN  = [
          'CN=users,dc=ad,dc=georchestra,dc=org',                    
]

# Filters to be applied on each corresponding branches the number of elements
# should match the number of the previous array
IN_USERS_FILTERS = [
          '(objectClass=user)',
]

# some fields can be shared in common between AD and OpenLDAP, but some others
# seem specific. Since we need some basic fields, the object below describes
# the mapping to be followed.
# A field in the AD can be mapped to several fields in the OpenLDAP.

ATTRIBUTES_MAPPING = {
  'cn'             : ['cn'],
  'sn'             : ['sn'],
  'st'             : ['st'],
  'description'    : ['description'],
  'givenName'      : ['givenName'],
  'displayName'    : ['displayName'],
  'mail'           : ['mail'],
  # these seem AD-specific, but can be mapped onto regular attributes OpenLDAP-side
  'sAMAccountName' : ['uid'],
  #'userPrincipalName' : ['uid'],
  'company'        : ['o'],
  'department'     : ['businessCategory'],
  # needed by GeoFence: numerical unique identifier
  'uSNCreated'     : ['employeeNumber'],
}

OUT_USERS_BASEDN  = "ou=users,dc=georchestra,dc=org"
OUT_ROLES_BASEDN = "ou=roles,dc=georchestra,dc=org"
OUT_DEFAULT_GROUPS = [ 'USER' ]
OUT_GROUP_MEMBER = 'member'
OUT_USER_FILTER = '(objectClass=inetOrgPerson)'

# protected users (i.e. users present in the LDAP that should not be removed)
OUT_PROTECTED_USERS = ['testadmin', 'admin', 'geoserver_privileged_user', 'georchestra-ouvert', 'georchestra-restreint' ]

OUT_USERS_OBJECTCLASSES = [ 'top', 'person', 'organizationalPerson', 'inetOrgPerson' ]


# nothing to be configured after this line


# Connection to the AD
inLdapCnx = ldap.initialize(IN_LDAP_URI)
inLdapCnx.simple_bind_s(IN_LDAP_BINDDN, IN_LDAP_PASSWD)
inLdapCnx.protocol_version = ldap.VERSION3

# Connection to the OpenLDAP
outLdapCnx = ldap.initialize(OUT_LDAP_URI)
outLdapCnx.simple_bind_s(OUT_LDAP_BINDDN, OUT_LDAP_PASSWD)
outLdapCnx.protocol_version = ldap.VERSION3

# Dumps the users list given as argument on stdout
def dump_users(users):
  for user in users:
    dn,attrs = user[0]
    print "%s:" % (dn,)
    for k in attrs.keys():
      print "\t%s -> %s" % (k,attrs[k],)
    print "\n"

# Creates a new user in the OpenLDAP
def openldap_create_user(uid, in_user_attrs, dry_run = False):
  newdn = "uid=%s,%s" % (uid, OUT_USERS_BASEDN,)
  # A dict to help build the "body" of the object
  attrs = {}
  for k in ATTRIBUTES_MAPPING.keys():
    translated_attr = in_user_attrs.get(k)
    if translated_attr is not None:
      for k2 in ATTRIBUTES_MAPPING[k]:
        if attrs.get(k2) is None:
          attrs[k2] = translated_attr
        else:
          for elems in translated_attr:
            attrs[k2].append(elems)
  # Adds the objectclasses needed by an inetOrgPerson
  attrs['objectclass'] = OUT_USERS_OBJECTCLASSES
  # Adds a default sn attribute if not present, because inetOrgPerson needs a one
  if 'sn' not in attrs:
    attrs['sn'] = in_user_attrs['cn'][0]
  # Adds a default polygon in 'l' field
  # attrs['l'] = 'SRID=3948;POLYGON ((0 0, 1 1, 2 2, 0 0))'
  attrs['userpassword'] = '{SASL}'+attrs['uid'][0]+'@ad.georchestra.org'  # modify by Alexandre
  ldif = ldap.modlist.addModlist(attrs)
  if not dry_run:
    try:
      outLdapCnx.add_s(newdn,ldif)
    except ldap.LDAPError,e:
      print STR_RED.format("[Error]: %s" % e)
      print STR_RED.format("  uid: %s\n  attributes: %s\n" % (uid, in_user_attrs))
      return False
  else:
      print STR_GREEN.format("[dry-run] Inserting entry %s into OpenLDAP" % (newdn,))
      for k in attrs.keys():
        print STR_GREEN.format("\t%s -> %s" % (k, attrs[k],))
  return True

# Removes a user from the OpenLDAP
def openldap_remove_user(uid, dry_run = False):
  deleteDN = "uid=%s,%s" % (uid, OUT_USERS_BASEDN,)
  try:
    if not dry_run:
      outLdapCnx.delete_s(deleteDN)
    else:
      print STR_RED.format("[dry-run] Removing entry %s from OpenLDAP" % (deleteDN,))
  except ldap.LDAPError, e:
    print STR_RED.format("[Error]: %s" % e)
    return False
  return True

# Adds a user to a group
def openldap_add_user_to_group(user_uid, group_uid, dry_run):
  user_dn = "uid=%s,%s" % (user_uid, OUT_USERS_BASEDN,)
  group_dn = "cn=%s,%s" % (group_uid, OUT_ROLES_BASEDN,)
  mod_attrs = [(ldap.MOD_ADD, OUT_GROUP_MEMBER, user_dn)]
  if not dry_run:
    try:
      outLdapCnx.modify_s(group_dn, mod_attrs)
    except ldap.LDAPError, e:
      print STR_RED.format("[Error]: %s" % e)
      return False
  else:
    print STR_GREEN.format("[dry-run] Affecting user %s to group %s\n" % (user_uid, group_uid,))
  return True

# Looks up user attrs, given a uid and an optional attribute field name
def lookup_user_attributes(uid, user_list, uid_attr_name = 'sAMAccountName'):
  for user in user_list:
    _, attrs_list = user[0]
    # TODO: iterate on each uid ? can be multivalued ?
    if attrs_list[uid_attr_name][0] == uid:
      return attrs_list
  # should not happen
  return {}

# Actually do something
if __name__ == "__main__":
  in_users  = []
  out_users = []
  in_uid = []
  out_uid = []

  # getting users from the AD
  for idx, currentUserBaseDn in enumerate(IN_USERS_BASEDN):
    try:
      ldap_result_id = inLdapCnx.search(currentUserBaseDn, ldap.SCOPE_SUBTREE, IN_USERS_FILTERS[idx], ATTRIBUTES_MAPPING.keys())
      while 1:
        result_type, result_data = inLdapCnx.result(ldap_result_id, 0)
        if (result_data == []):
          break
        else:
          if result_type == ldap.RES_SEARCH_ENTRY:
            # lowercase uid but does not work
            # result_data[0][1]['sAMAccountName'][0] = result_data[0][1]['sAMAccountName'][0].lower() # modify by Alexandre
            in_users.append(result_data)
            # cn and sAMAccountName are mapped as uid into OpenLDAP
            in_uid.append(result_data[0][1]['sAMAccountName'][0])
    except ldap.LDAPError, e:
      print STR_RED.format(e)
    # print AD users to stdout
    print "AD users"
    dump_users(in_users)

  # getting users from the OpenLDAP
  try:
    returned_attrs = [ attr for att in ATTRIBUTES_MAPPING.values() for attr in att ]
    # adding uid if not mapped
    returned_attrs.append("uid")
    ldap_result_id = outLdapCnx.search(OUT_USERS_BASEDN, ldap.SCOPE_ONELEVEL, OUT_USER_FILTER, None)
    while 1:
      result_type, result_data = outLdapCnx.result(ldap_result_id, 0)
      if (result_data == []):
        break
      else:
        if result_type == ldap.RES_SEARCH_ENTRY:
          out_users.append(result_data)
          out_uid.append(result_data[0][1]['uid'][0])
  except ldap.LDAPError, e:
    print STR_RED.format(e)

  # print LDAP users to stdout
  print "LDAP users"
  dump_users(out_users)

  # Step 1: Removing non protected users from the OpenLDAP that are not listed in
  # the Active Directory
  for user in out_uid:
    if user not in OUT_PROTECTED_USERS and user not in in_uid:
      openldap_remove_user(user, DRY_RUN)

  # Step 2: Insert users from the AD that are not in the OpenLDAP yet AND has a mail
  # (using default group)
  for user in in_uid:
    if user not in out_uid:
      user_attributes = lookup_user_attributes(user, in_users)
      print 'user:%s => %s' % (user,user_attributes)
      # only add users with email address
      if "mail" in user_attributes:
        if openldap_create_user(user, user_attributes, DRY_RUN):
          # adding the created user to the default roles
          for group in OUT_DEFAULT_GROUPS:
            openldap_add_user_to_group(user, group, DRY_RUN)

  # end: disconnect
  inLdapCnx.unbind_s()
  outLdapCnx.unbind_s()


