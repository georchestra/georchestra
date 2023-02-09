# From 22.X to master

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
