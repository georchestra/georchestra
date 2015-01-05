# EPSG-extension

This module allows to locally override the geotools projection definitions in mapfishapp and extractorapp.  
This is useful to introduce new definitions, or when they are incorrect (eg: EPSG:27572).

If the property ```CUSTOM_EPSG_FILE``` (in tomcat JAVA_OPTS) is set to a valid file, its definitions are loaded.  
eg:
```
JAVA_OPTS="$JAVA_OPTS \
              -DCUSTOM_EPSG_FILE=/opt/epsg.properties"
```

If there is no such property, the local [epsg.properties](src/main/resources/org/geotools/referencing/factory/epsg/epsg.properties) file is used.
