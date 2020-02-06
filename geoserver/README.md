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
