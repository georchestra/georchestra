# Setting up OpenLDAP and a basic LDAP tree

There are 2 main ways of having OpenLDAP configured :
 * One using a single conf file (on debian/ubuntu systems, located in /etc/ldap/sldapd.conf)
 * A new one which tends to store the configuration into a specific LDAP branch (name cn=config), and composed of several files located generally into /etc/ldap/slapd.d).

We document here the second case (slapd.d-style configuration).

Note : It's also possible to delegate the authentication of certain users to a remote Active Directory or LDAP, see the [SASL Doc page](../tutorials/sasl.md).

## Prerequisites

```
sudo apt-get install slapd ldap-utils
```

You will need to provide the LDAP administrator password. Choose a strong one.


Before creating the LDAP tree, you should have a look at the [users and groups](../../ldap/README.md) we'll be adding.



## Database entry

The file [bootstrap.ldif](../../ldap/bootstrap.ldif) creates the database and an administrator account (```cn=admin,dc=georchestra,dc=org```) with a password set by default to ```secret```. You should change it.

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f bootstrap.ldif
```

If successful, the above command should display: ```adding new entry "olcDatabase=mdb,cn=config"```.


## Root DN

To create the root DN, use the [root.ldif](../../ldap/root.ldif) file:

```
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f root.ldif
```

This will ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn, which was set with the previous command.


## groupOfMembers objectClass

Groups in geOrchestra are instances of groupOfMembers objects, which allows empty groups (contrary to groupOfNames, which were used in previous geOrchestra releases).
This objectClass comes from rfc2307bis and is not available by default in OpenLDAP. As a result, we need to import its structure into the ```cn=config``` database, using the [groupofmembers.ldif](../../ldap/groupofmembers.ldif) file:

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f groupofmembers.ldif
```

## The "memberof" overlay

The optional "memberof" overlay is great to check if a user is a member of a given group.
Use the [memberof.ldif](../../ldap/memberof.ldif) file to add the module and configure the overlay.

Before inserting it, watch for the database name it targets... currently `olcDatabase={1}mdb,cn=config`.
If the database you created with the `bootstrap.ldif` file is named differently, please adjust the `memberof.ldif` file content accordingly.

Then:
```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f memberof.ldif 
```

Caution: by default, we're adding the overlay to the ```{1}mdb,cn=config``` database. You may have to customize this if your setup is different (having a look at the ```/etc/ldap/slapd.d/cn=config/``` directory).


## geOrchestra users and groups

The [georchestra.ldif](../../ldap/georchestra.ldif) file creates the default geOrchestra users & groups:

```
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f georchestra.ldif
```

This will also ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn.


Note that you are free to customize the users (entries under the "users" OrganizationUnit) to fit your needs, provided you keep the required ```geoserver_privileged_user```.



# Managing the directory

To manage the directory, there are (at least) 3 options:

 * from the command line, use ldapvi (install with ```sudo apt-get install ldapvi```):

```
ldapvi --host localhost -D "cn=admin,dc=georchestra,dc=org" -w "secret" -b "dc=georchestra,dc=org"
```

 * [Apache Directory Studio](http://directory.apache.org/studio/), a powerful desktop client.
 * our own [console](/console/README.md) web application, available at ```/console/console/``` to  members of the ```MOD_LDAPADMIN``` group, is probably the easiest one.
