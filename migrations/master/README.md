# From 22.x to master

## GeoNetwork 4.0 to 4.2 migration notes

### Virtual CSWs and subportals

The new GeoNetwork version dropped support for what was called "virtual CSW" (basically
a CSW endpoint filtered by a custom Lucene query on the index), in favor of the "subportals".

You will find in this directory a python script named `migrate-virtual-csw-to-subportals.py`,
which can be used as a base to convert virtual CSWs to subportals.

To ensure backward compatibility with previously expected CSW endpoints, you will have to rewrite the
URLs into your webserver configuration as follows:

```
from:
/geonetwork/srv/(.*)/csw-(.*)
to:
/geonetwork/csw-$2/$1/csw
```

## Geoserver

In Docker context we've set new java option GEOWEBCACHE_CONFIG_DIR to /mnt/geoserver_datadir/gwc.

So you may need to move following files from `geowebcache_tiles` to `geoserver_datadir/gwc`:
- geowebcache.xml
- geowebcache-diskquota.xml
- geowebcache-diskquota-jdbc.xml

## Homepage / Proxy

In geochestra/docker, we've added a container named *static* based on Nginx with the home page.

As we want to serve the home page on URL "/", the default route in traefik is now the *static* container.

If you have custom services behind proxy, you might need to add some `ProxyPath` directives in proxy labels to route corresponding requests through *proxy* container.

ex:

```
services:
  proxy:
    labels:
     - >-
        traefik.http.routers.proxy.rule=Host(`georchestra-127-0-1-1.traefik.me`) && (
        PathPrefix(`/_static`)
        || PathPrefix(`/login`)
        || PathPrefix(`/usedetails`)
        ...
        )
```

You might need to reuse or adapt those changes for your setup.




## LDAP

The `idatafeeder` user was added to the ldap schema.

This user is internally used by datafeeder to import datasets and metadata into geoserver and geonetwork.

To upgrade the ldap, use the following command with the [ldap_migration.ldif](ldap_migration.ldif) file:

```
ldapmodify -H "ldap://ldap:389" -D "cn=admin,dc=georchestra,dc=org" -w "secret" -f ldap_migration.ldif 
```
