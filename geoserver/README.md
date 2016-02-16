# geOrchestra specific GeoServer build documentations

## GeoServer

Into `geoserver-submodule/src` subdirectory

### GeoServer without GeoFence

```
$ cd geoserver-submodule/src
# without GeoFence:
$ mvn clean install
```

### GeoServer with GeoFence's own UI

```
$ mvn clean install -Pgeofence
```

### GeoServer with integrated GeoFence UI

```
$ mvn clean install -Pgeofence-server

```

People willing to use GeoFence are advised to use the integrated version.


## Building the geOrchestra debian package for GeoServer

Into this directory:

```
$ mvn clean package deb:package -PdebianPackage --pl webapp
```


## Building the external GeoFence webapp

Into `geofence/src` subdirectory

```
$ mvn clean install
$ mvn package deb:package -Ppostgis -PdebianPackage --pl gui/web
```
## Extra properties needed for GeoFence

If using GeoFence, make sure the following environment variable is defined before launching your application server:

```
-DGEOSERVER_XSTREAM_WHITELIST=org.geoserver.geoserver.authentication.auth.GeoFenceAuthenticationProviderConfig
```

The `geofence.dir` variable should also be configured so that `${geofence.dir}/geofence-datasource-ovr.properties` can resolve the correct properties file in the datadir, e.g.

```
-Dgeofence.dir=/etc/georchestra/geoserver/geofence
```

This extra variable is needed because we did not want to customize a geofence module which then come in its vanilla version.

