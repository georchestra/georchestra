
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
