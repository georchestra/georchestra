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
This objectClass comes from rfc2307bis and is not available by default in OpenLDAP. As a result, we need to import its structure into the ```cn=config``` database, using the [groupofmembers.ldif](../../ldap/docker-root/groupofmembers.ldif) file:

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f groupofmembers.ldif
```

## OpenLDAP overlays

OpenLDAP directories can be extended via *overlays*, which are enabled *in a database*.

Before enabling an overlay, watch for the database name it targets... by default `olcDatabase={1}mdb,cn=config`.

After a fresh slapd install, you might already have a database set up for you, depending on how the package manager of the distribution is configured, or if the package planned to do so.

To check the database number in your LDAP setup, you can go through the `/etc/ldap/slapd.d/cn=config/` subdirectory and check the files `olcDatabase={x}mdb.ldif`. The different files should contain a `olcSuffix attribute`. The expected geOrchestra database is the one which contains a `olcSuffix: dc=georchestra,dc=org`.

Example on debian 11, where a default database is created, the geOrchestra database is number 2:
```
$grep -r 'olcSuffix: dc=georchestra,dc=org' /etc/ldap/slapd.d/cn\=config/
/etc/ldap/slapd.d/cn=config/olcDatabase={2}mdb.ldif:olcSuffix: dc=georchestra,dc=org
```

The overlay ldif files should match the expected database number in OpenLDAP, and you might need to adapt this number in the LDIF file.

### The "memberof" overlay

The `memberof` overlay is required to check if a user is a member of a given group.
Use the [memberof.ldif](../../ldap/docker-root/memberof.ldif) file to add the module and configure the overlay.

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f memberof.ldif
```

### The "lastbind" overlay

The `lastbind` overlay is required to record last user connection to the platform. It also depends on the `ppolicy` overlay.
Use the [lastbind.ldif](../../ldap/docker-root/lastbind.ldif) file to add the modules and configure the overlay.

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f lastbind.ldif
```

## Add "sshPublicKey" objectClass

The [openssh.ldif](../../ldap/docker-root/openssh.ldif) file optionally creates the "sshPublicKey" objectClass, which is interesting to store `sshPublicKey` in the LDAP:

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f openssh.ldif
```

## Add the "georchestra-specific" LDAP schemas

The required [georchestraSchema.ldif](../../ldap/docker-root/georchestraSchema.ldif) file creates the "georchestraUser" and "georchestraOrg" objectClasses:

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f georchestraSchema.ldif
```


## geOrchestra users and groups

The [georchestra.ldif](../../ldap/docker-root/georchestra.ldif) file creates the default geOrchestra users & groups:

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
 * our own [console](/console/README.md) web application, available at ```/console/manager/``` to  members of the ```SUPERUSER``` group, is probably the easiest one.

# Enabling rotation policy for passwords management 

To enable rotation policy for passwords management, please run the following commands:
```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f ppolicy-rotation.ldif
```
```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f rotationpolicyoverlay.ldif
```
To disable password expire for no humain users (geoserver_privileged_user, idatafeeder), please run the following commands:
```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f pwd_no_expire.ldif
```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f pwd_no_expire_users.ldif
```
If  rotation policy for passwords management is enabled, password has to be set after 12 months.
Alerts will be shown to user during last month.
This duration can be set with the 'pwdMaxAge' option in the 'rotationpolicyoverlay.ldif' file.
