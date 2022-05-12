# GeoServer in geOrchestra

![geoserver](https://github.com/georchestra/georchestra/workflows/geoserver/badge.svg)

geOrchestra comes with it's own GeoServer version, which is a very light fork for customization (header & geofence integration, mainly).

If needed, geOrchestra is able to work with an unmodified, standard [GeoServer](http://geoserver.org/) instance, provided the [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir) custom datadir is used.


## Building GeoServer flavors

GeoServer:
```
make war-build-geoserver
```
... or `make deb-build-geoserver` to build a Debian package.


GeoServer **with integrated GeoFence** app:
```
make war-build-geoserver-geofence
```
... or `make deb-build-geoserver-geofence` to build a Debian package.


## GeoFence

See [the documentation](../docs/setup/tomcat.md#note-for-geofence-users).

## Authentication

geOrchestra's GeoServer runs behind a proxy which handles user authentication
for on behalf of all geOrchestra back-end services (GeoServer, GeoNetwork, console, etc).
Once authenticated, every proxied request contains a per-application configurable
set of request headers with pre-authenticated user credentials.

GeoServer expects the standard `sec-username` and `sec-roles` headers, with
the pre-authenticated username and list of roles respectively.

These headers will be picked up by `org.geoserver.security.filter.GeoServerRequestHeaderAuthenticationFilter`.

When first starting a geOrchestra docker-compose cluster, this filter will be
configured to use the above mentioned headers as established [this configuration
file](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/security/filter/proxy/config.xml)
at the [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir) repository.



