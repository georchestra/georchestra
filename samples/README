This directory contains 3 ldif files which aims to facilitate LDAP
configuration.

There are 2 main ways of having openLDAP configured :

- One using a single conf file (on debian/ubuntu systems, located in
  /etc/ldap/sldapd.conf)

- A new one which tends to store the configuration into a specific LDAP branche
  (name cn=config), and composed of several files located generally into
  /etc/ldap/slapd.d).


In case of using slapd.d-style configuration:

The file georchestra-bootstrap.ldif allows to first activate the hdb backend
module (needed to store our LDAP tree) then create the db entry, should mainly
be used this way:
 
$ sudo ldapadd -Y EXTERNAL -H ldapi:/// -f georchestra-bootstrap


If everything was successful with the previous command, you now have to create
the root DN. Note that the previous command should have set the default
administrator account as:

dn: cn=admin,dc=georchestra,dc=org
password: secret

You can then issue the following command in order to create the root DN :
 
$ ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f georchestra-root.ldif 


In case of using "old-syle" slapd.conf:

It should be easier than the previous one. Undocumented for now, but after some
searches on the internet you should be able to achieve it, because it is much
more documented than the new "slapd.d" way.


The last file (georchestra.ldif) contains a LDIF export of a sample LDAP tree.


gidNumber uniqueness

Another file is provided but it is optional: gidnumber-uniqueness.ldif

It aims to add a unicity constraint on each objects under the base
ou=groups,dc=georchestra,dc=org, so that another group is added, it should have
a unique gidNumber attribute.

