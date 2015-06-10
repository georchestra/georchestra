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

# Setup remote AD authentication via SASL

The implementation of SASL protocol with LDAP allows to delegate verification of the password of a user to a remote Active Directory. This can be useful if you already have accounts and want users to keep the same password within geOrchestra.

## Prerequisites

```
sudo apt-get install sasl2-bin
```

You can check that ldap is in the sasl's list of supported services :

```
saslauthd -v
```

## SASL Configuration

Edit the file /etc/default/saslauthd :

```
[...]
START=yes
[...]
MECHANISMS="ldap"
[...]
```

And the file /etc/saslauthd.conf :

```
ldap_servers: ldap://<AD_server_IP>
ldap_search_base: dc=myorganisation,dc=org
ldap_timeout: 10
ldap_filter: sAMAccountName=%U
ldap_bind_dn: cn=admin,dc=myorganisation,dc=org
ldap_password: <AD_admin_password>
ldap_deref: never
ldap_restart: yes
ldap_scope: sub
ldap_use_sasl: no
ldap_start_tls: no
ldap_version: 3
ldap_auth_method: bind
```

Some explanations:

ldap_servers: Active Directory Server IP

ldap_search_base: Basic prefix of AD

ldap_filter: filter that going to link the login geOrchestra and that of AD

ldap_bind_dn: the user used to connect to the AD

And restart SASL service : 

```
service saslauthd restart
```

You can already at this level test whether SASL operates by attempting a connection with a user of the AD :

```
testsaslauthd -u <AD_user> -p <password>
```

## LDAP Configuration

Now you have to setup LDAP to use SASL.

```
ldapmodify -Q -Y EXTERNAL -H ldapi:/// <<EOF
dn: cn=config
changetype: modify
add: olcSaslHost
olcSaslHost: localhost
EOF

ldapmodify -Q -Y EXTERNAL -H ldapi:/// <<EOF
dn: cn=config
changetype: modify
add: olcSaslSecProps
olcSaslSecProps: none
EOF
```

NB : As soon as the /etc/ldap/sasl2/slapd.conf is created, the "-Y EXTERNAL" ldapmodify option no longer works, because he wants to do everything by sasl. So you have to rename the file for the time of manipulation (/etc/ldap/sasl2/slapd.conf.stop for example).

After that you have to create the /etc/ldap/sasl2/slapd.conf file if it doesn't exist and fill it with :

```
pwcheck_method: saslauthd
saslauthd_path: /var/run/saslauthd/mux
```

And add the ldap user to sasl group : 

```
adduser openldap sasl
```

Now restart your server : 

```
reboot
```

Then you will need to set up a script to import your AD users in the LDAP geOrchestra, the password field will then have to be filled with:

```
{SASL}<user>@<server>
```

for each user whose password must be validated by Active Directory.
