# Setup remote AD or LDAP authentication via SASL

The SASL protocol is used to delegate password validation to a remote Active Directory or LDAP. 
This is particularly handy when you already have an AD or LDAP instance and your users should authenticate against it.

## Prerequisites

```
sudo apt-get install sasl2-bin
```

Check that ldap is in the sasl's list of supported services:

```
saslauthd -v
```

## SASL Configuration

Edit the ```/etc/default/saslauthd``` file:

```
[...]
START=yes
[...]
MECHANISMS="ldap"
[...]
```

Also the ```/etc/saslauthd.conf``` file:

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

... in which:
 * ```ldap_servers``` is the Active Directory server IP
 * ```ldap_search_base``` is your AD's search base
 * ```ldap_filter``` is the filter that is used to link geOrchestra accounts to those coming from your AD
 * ```ldap_bind_dn``` is the user dn that will be used to connect to your remote AD


Finally, restart the SASL service: 

```
service saslauthd restart
```

At this stage, it is possible to test whether SASL operates fine, by attempting a connection with an AD user:

```
testsaslauthd -u <AD_user> -p <password>
```

## LDAP Configuration

Instruct LDAP to use SASL:
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

Create the ```/etc/ldap/sasl2/slapd.conf``` file (if it doesn't exist) and fill it with:
```
pwcheck_method: saslauthd
saslauthd_path: /var/run/saslauthd/mux
```

Add the ldap user to the ```sasl``` group: 
```
adduser openldap sasl
```

Please note that as soon as the ```/etc/ldap/sasl2/slapd.conf``` file is created, the "-Y EXTERNAL" ldapmodify option no longer works. 
One would have to rename this file during the operation (eg ```/etc/ldap/sasl2/slapd.conf.stop```).


Finally, when importing AD users into the geOrchestra LDAP, and for each user whose password must be validated by the remote Active Directory, fill the password field with:
```
{SASL}<user>@<server>
```
