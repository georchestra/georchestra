# GeoServer in geOrchestra

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

If using GeoFence, make sure the following environment variable is defined before launching your application server:

```
-DGEOSERVER_XSTREAM_WHITELIST=org.geoserver.geoserver.authentication.auth.GeoFenceAuthenticationProviderConfig
```

The `geofence.dir` variable should also be configured so that `${geofence.dir}/geofence-datasource-ovr.properties` can resolve the correct properties file in the datadir, e.g.

```
-Dgeofence.dir=/etc/georchestra/geoserver/geofence
```

This extra variable is needed because we did not want to customize a geofence module which then comes in its vanilla version.
