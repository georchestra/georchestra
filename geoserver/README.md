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
$ mvn clean install -Pgeofence -Pgeofence-server

```

## Building the geOrchestra debian package for GeoServer

Into this directory

```
$ mvn clean package deb:package -PdebianPackage -pl webapp
```


## Building the external GeoFence webapp

Into `geofence/src` subdirectory

```
$ mvn clean install
$ mvn package deb:package -Ppostgis -PdebianPackage -pl gui/web
```
## Extra properties needed for GeoFence

If using GeoFence, make sure the following environment variable is defined before launching your application server:

```
-DGEOSERVER_XSTREAM_WHITELIST=org.geoserver.geoserver.authentication.auth.GeoFenceAuthenticationProviderConfig
```

And also the `georchestra.datadir` variable.




