# Setting up OpenLDAP and a basic LDAP tree

There are 2 main ways of having OpenLDAP configured :
 * One using a single conf file (on debian/ubuntu systems, located in /etc/ldap/sldapd.conf)
 * A new one which tends to store the configuration into a specific LDAP branch (name cn=config), and composed of several files located generally into /etc/ldap/slapd.d).

We document here the second case (slapd.d-style configuration).

Note : It's also possible to deleguate the athentication of certain users to a remote Active Directory or LDAP, see the [SASL Doc page](https://github.com/jusabatier/georchestra/blob/patch-3/doc/setup/sasl.md).

## Prerequisites

```
sudo apt-get install slapd ldap-utils
```

You will need to provide the LDAP administrator password. Choose a strong one.


Before creating the LDAP tree, you should have a look at the [users and groups](https://github.com/georchestra/LDAP/blob/master/README.md) we'll be adding.



## Database entry

The file **georchestra-bootstrap.ldif** creates the database and an administrator account (```cn=admin,dc=georchestra,dc=org```) with a password set by default to ```secret```. You should change it.

```
wget --no-check-certificate https://raw.githubusercontent.com/georchestra/LDAP/YY.MM/georchestra-bootstrap.ldif -O /tmp/bootstrap.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/bootstrap.ldif
```
... where YY.MM stands for the georchestra version you're using (eg: 14.12). 

If successful, the above command should display: ```adding new entry "olcDatabase=hdb,cn=config"```.


## Root DN

To create the root DN, use the **georchestra-root.ldif** file:

```
wget --no-check-certificate https://raw.githubusercontent.com/georchestra/LDAP/YY.MM/georchestra-root.ldif -O /tmp/root.ldif
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f /tmp/root.ldif
```

This will ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn, which was set with the previous command.


## geOrchestra users and groups

The **georchestra.ldif** file creates the default geOrchestra users & groups:

```
wget --no-check-certificate https://raw.githubusercontent.com/georchestra/LDAP/YY.MM/georchestra.ldif -O /tmp/georchestra.ldif
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f /tmp/georchestra.ldif
```

This will also ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn.


Note that you are free to customize the users (entries under the "users" OrganizationUnit) to fit your needs, provided you keep the required ```geoserver_privileged_user```.


## The "memberof" overlay

The optional "memberof" overlay is great to check if a user is a member of a given group.
Use the **georchestra-memberof.ldif** file to add the module and configure the overlay.

```
wget --no-check-certificate https://raw.githubusercontent.com/georchestra/LDAP/YY.MM/georchestra-memberof.ldif -O /tmp/memberof.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/memberof.ldif 
```

Caution: by default, we're adding the overlay to the ```{1}hdb,cn=config``` database. You may have to customize this if your setup is different (having a look at the ```/etc/ldap/slapd.d/cn=config/``` directory).


# Managing the directory

To manage the directory, there are (at least) 3 options:

 * from the command line, use ldapvi (install with ```sudo apt-get install ldapvi```):

```
ldapvi --host localhost -D "cn=admin,dc=georchestra,dc=org" -w "secret" -b "dc=georchestra,dc=org"
```

 * [Apache Directory Studio](http://directory.apache.org/studio/), a powerful desktop client.
 * our own [ldapadmin](/ldapadmin/README.md) web application, available at ```/ldapadmin/privateui/``` to  members of the ```MOD_LDAPADMIN``` group, is probably the easiest one.
