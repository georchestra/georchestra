Install notes for a fresh Debian stable, based on a unique tomcat instance.





    


GeoServer
=========

* "Data dir"

GeoServer configuration files reside in a particular directory, which is (incorrectly) called the "data dir".

Regarding this data dir, there are 2 recommendations: 
1) it should not reside inside the deployed webapp,
2) before starting geoserver for the first time, it should be manually populated with a provided "[minimal data dir](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/README.md)".

To this purpose:

```
sudo -u tomcat mkdir /path/to/geoserver/data/dir
cd /path/to/geoserver/data/
```

Finally, you should clone the provided minimal data dir, either branch ```master``` for regular geoserver security or ```geofence``` if you are using geofence.
```
sudo -u tomcat git clone -b master https://github.com/georchestra/geoserver_minimal_datadir.git dir
```


* Tomcat

Required JAVA_OPTS for GeoServer :

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS
    -Xms2G -Xmx2G -XX:PermSize=256m -XX:MaxPermSize=256m \
    -DGEOSERVER_DATA_DIR=/path/to/geoserver/data/dir \
    -DGEOWEBCACHE_CACHE_DIR=/path/to/geowebcache/cache/dir \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF8 \
    -Djavax.servlet.request.encoding=UTF-8 \
    -Djavax.servlet.response.encoding=UTF-8 \
    -server \
    -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=2 \
    -XX:SoftRefLRUPolicyMSPerMB=36000 \
    -XX:NewRatio=2 \
    -XX:+AggressiveOpts "  
```

Be sure to provide the correct ```GEOSERVER_DATA_DIR``` path !

* Fonts

GeoServer uses the fonts available to the JVM for WMS styling.
You may have to install the "core fonts for the web" on your server if you need them.

	sudo apt-get install ttf-mscorefonts-installer

Restart your geoserver tomcat and check on /geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.JVMFontsPage that these are loaded.

* Native JAI
 
GeoServer and GeoWebCache take great advantage of the native JAI availability.

	sudo apt-get install libjai-imageio-core-java

Then, make sure that the following 5 jars are loaded by your geoserver and geowebcache tomcats classloaders: jai_codec.jar, jai_core.jar, jai_imageio.jar, clibwrapper_jiio.jar, mlibwrapper_jai.jar

This is usually done by symlinking them from their original location (something like ```/usr/share/java```) to the ```${catalina.base}/lib``` directory (for the common classloader).


* Fine tuning (optional but highly recommended)

Please refer to the excellent "[Running in a Production Environment](http://docs.geoserver.org/stable/en/user/production/index.html)" section of the GeoServer documentation.

You may also want to setup limits to the number of concurrent requests handled by your GeoServer. By default, geOrchestra GeoServer ships with the [control flow](http://docs.geoserver.org/stable/en/user/extensions/controlflow/index.html) module installed, but not activated. To do so, you have to create a custom ```controlflow.properties``` file in your geoserver data dir. Please refer to the module documentation for the syntax.

For GeoWebCache, a collection of tips and tricks can be found here:  http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html

GeoNetwork
==========

Be sure to include those options in your tomcat JAVA_OPTS setup:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Dgeonetwork.dir=/path/to/geonetwork-data-dir \
    -Dgeonetwork[-private].schema.dir=/path/to/tomcat/webapps/geonetwork[-private]/WEB-INF/data/config/schema_plugins \
    -Dgeonetwork.jeeves.configuration.overrides.file=/path/to/tomcat/webapps/geonetwork[-private]/WEB-INF/config-overrides-georchestra.xml"
```

... where brackets indicate optional strings, depending on your setup.


Extractorapp
============

Again, it is required to include custom options in your tomcat JAVA_OPTS setup:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Dorg.geotools.referencing.forceXY=true \
    -Dextractor.storage.dir=/path/to/temporary/extracts/"
```

Note: if the epsg-extension module is installed, one can manage custom EPSG codes by adding:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -DCUSTOM_EPSG_FILE=file://$CATALINA_BASE/conf/epsg.properties"
```

... in which a sample epsg.properties file can be found [here](server-deploy-support/src/main/resources/c2c/tomcat/conf/epsg.properties)


Production ready setup
======================

The above setup is great for testing purposes.

If you plan to use geOrchestra with a large number of users, or if high availability is a concern, it is recommended to split the webapps across several Tomcat instances, eventually load balancing GeoServer. 

The recommended production setup is to have 2 or 3 tomcat instances:
 - one for the security proxy and CAS
 - one for geoserver
 - one for all the other webapps

The following [contributed guide](http://geo.viennagglo.fr/doc/index.html) explains how to setup a geOrchestra instance with 3 tomcats. It is a recommended reading, but it is only available in French at the moment.
Feel free to ask for guidance on the https://groups.google.com/forum/#!forum/georchestra-dev mailing list if you need help.
