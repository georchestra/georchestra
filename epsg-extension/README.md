# EPSG-extension

This module allows to locally override the geotools projection definitions.  
This is useful to introduce new definitions, or when they are incorrect (eg: EPSG:27572).

If the property ```CUSTOM_EPSG_FILE``` (in tomcat JAVA_OPTS) is set to a valid file, its definitions are loaded.  
eg:
```
JAVA_OPTS="$JAVA_OPTS \
              -DCUSTOM_EPSG_FILE=/opt/epsg.properties"
```

If there is no such property, the local [epsg.properties](src/main/resources/org/geotools/referencing/factory/epsg/epsg.properties) file is used.

## building

Extractor app and GeoServer both use different versions of this plugin because of dependencies.  
So this plugin must be build with the correct parameters.

eg:
```
mvn install -Dgt.version=9.2
```
