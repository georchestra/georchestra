# Setting up OpenLDAP and a basic LDAP tree

There are 2 main ways of having OpenLDAP configured :
 * One using a single conf file (on debian/ubuntu systems, located in /etc/ldap/sldapd.conf)
 * A new one which tends to store the configuration into a specific LDAP branch (name cn=config), and composed of several files located generally into /etc/ldap/slapd.d).

We document here the second case (slapd.d-style configuration).


## Prerequisites

```
sudo apt-get install slapd ldap-utils
```

You will need to provide the LDAP administrator password. Choose a strong one.


Let's also get the data by cloning our LDAP repository (and have a look at the [users and groups](https://github.com/georchestra/LDAP/blob/master/README.md) it creates by default):
```
git clone -b YY.MM https://github.com/georchestra/LDAP.git
```
In the above, YY.MM stands for the geOrchestra version you're using (eg: ```14.06``` for the latest stable)


## Database entry

The file **georchestra-bootstrap.ldif** creates the database:

```
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f georchestra-bootstrap.ldif
```

If successful, the above command should display: ```adding new entry "olcDatabase=hdb,cn=config"```.

It also creates a default administrator account with:
```
dn: cn=admin,dc=georchestra,dc=org
password: secret
```


## Root DN

To create the root DN, use the **georchestra-root.ldif** file:

```
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f georchestra-root.ldif
```

This will ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn, which is "secret".



## geOrchestra users and groups

The **georchestra.ldif** file creates the default geOrchestra users & groups:

```
ldapadd -D"cn=admin,dc=georchestra,dc=org" -W -f georchestra.ldif
```

This will also ask the password for the ```cn=admin,dc=georchestra,dc=org``` dn.


Note that you are free to customize the users (entries under the "users" OrganizationUnit) to fit your needs, provided you keep the required geoserver_privileged_user.


## The "memberof" overlay

The optional "memberof" overlay is great to check if a user is a member of a given group.
Use the **georchestra-memberof.ldif** file to add the module and configure the overlay.


# Managing the directory

To manage the directory, there are (at least) 3 options:

 * from the command line, use ldapvi (install with ```sudo apt-get install ldapvi```):

```
ldapvi --host localhost -D "cn=admin,dc=georchestra,dc=org" -w "secret" -b "dc=georchestra,dc=org"
```

 * [Apache Directory Studio](http://directory.apache.org/studio/), a powerful desktop client.
 * our own [ldapadmin](/ldapadmin/README.md) web application, available at /ldapadmin/privateui/ to  members of the ```MOD_LDAPADMIN``` group, is probably the easiest one.
