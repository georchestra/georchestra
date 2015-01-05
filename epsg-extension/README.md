# EPSG-extension

This plugin allows to locally override the geotools projection definitions in mapfishapp and extractorapp.  
This is useful to introduce new definitions, or when they are incorrect (eg: EPSG:27572).

If the property ```CUSTOM_EPSG_FILE``` is set to a valid file, its definitions are loaded.  

For instance, in ```/etc/default/tomcat-georchestra```:
```
JAVA_OPTS="$JAVA_OPTS -DCUSTOM_EPSG_FILE=/opt/epsg.properties"
```

If there is no such property, the local [epsg.properties](src/main/resources/org/geotools/referencing/factory/epsg/epsg.properties) file is used.
