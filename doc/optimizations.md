# Optimizations

## GeoServer

These steps are recommended, but they will not be very helpful if your data is not carefully prepared.
Please refer to a recent "GeoServer on steroids" presentation for more information on GIS data optimizations. 

### Native JAI & ImageIO

By default, GeoServer ship with the java JAI classes which run everywhere, but are not as fast as the native ones.  
Raster operations will be ~ two times faster when the native JAI and imageio are installed, so: 

```
sudo apt-get install libjai-core-java libjai-imageio-core-java
```

Then, make sure that the following 5 jars are loaded by your geoserver and geowebcache tomcat classloaders:
 * ```jai_codec.jar```, ```jai_core.jar```, ```mlibwrapper_jai.jar``` for the native JAI
 * ```jai_imageio.jar```, ```clibwrapper_jiio.jar``` for ImageIO

This is usually done by symlinking them from their original location (something like ```/usr/share/java```) to the ```${catalina.base}/lib``` directory (for the common classloader).

eg:
```
cd /var/lib/tomcat-geoserver0/lib
sudo ln -s /usr/share/java/jai_core.jar .
sudo ln -s /usr/share/java/jai_codec.jar .
sudo ln -s /usr/share/java/mlibwrapper_jai.jar .
sudo ln -s /usr/share/java/jai_imageio.jar .
sudo ln -s /usr/share/java/clibwrapper_jiio.jar .
sudo service tomcat-geoserver0 restart
```

To see if they are correctly installed, open the GeoServer "server status" page (````/geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.StatusPage```) and check that the "Native JAI" and "Native JAI ImageIO" values are set to true.

Then, head to the "Settings" > "JAI" section:
 * allow a bigger fraction of the geoserver heap size to be used for the JAI: 0.75 rather than 0.5
 * check the Tile Recycling, JPEG Native Acceleration, PNG Native Acceleration & Mosaic Native Acceleration boxes


### Marlin Renderer

Marlin is an antialised rendering engine, which plugs into the JVM to replace the native implementation. 
Marlin combines the advantages of both rendering engines it replaces: it has the scalability of OpenJDK's "Pisces", and the speed of Oracle's "Ductus". 
Note that it only works on recent versions of Oracle and OpenJDK (>= 7).

Installing it is not difficult:
 * [grab the latest release](https://github.com/bourgesl/marlin-renderer/releases)
 * put the ```marlin-0.4.4.jar``` file into ```/usr/share/tomcat6/lib/``` (don't forget to chmod a+r marlin*.jar)
 * in ```/etc/defaults/tomcat-geoserver0```, add the following:

```
JAVA_OPTS="$JAVA_OPTS \
            -Xbootclasspath/a:"/usr/share/tomcat6/lib/marlin-0.4.4.jar" \
            -Dsun.java2d.renderer=org.marlin.pisces.PiscesRenderingEngine"
```

Finally, restart tomcat-geoserver0 and check the jar has been loaded with:
```
cat /var/lib/tomcat-geoserver0/logs/catalina.out | grep Marlin
```
It should display "Marlin software rasterizer = ENABLED"


### Control-Flow

For fairness reasons, and also to make your geoserver more scalable, you should setup limits to the number of concurrent requests handled by your GeoServer. 

By default, geOrchestra GeoServer ships with the [control flow](http://docs.geoserver.org/stable/en/user/extensions/controlflow/index.html) module installed.

If you have followed this guide, your geoserver probably also uses our recommended "geoserver data dir", which includes a basic [controlflow config file](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/controlflow.properties).

If not, you should create a custom ```controlflow.properties``` file in your geoserver "data dir".  
Please refer to the [control-flow module documentation](http://docs.geoserver.org/latest/en/user/extensions/controlflow/index.html) for the syntax.


### Fine tuning

Please refer to these excellent references:
 * "[Running in a Production Environment](http://docs.geoserver.org/stable/en/user/production/index.html)" section of the official GeoServer documentation,
 * [GeoServer training](http://geoserver.geo-solutions.it/edu/en/index.html) by GeoSolutions


## GeoWebCache

GeowebCache also leverages the Native JAI & ImageIO native libs. See above for instructions on how to install them.

An interesting collection of tips and tricks can be found here: [http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html](http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html)
