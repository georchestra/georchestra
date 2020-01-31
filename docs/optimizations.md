# Optimizations

## GeoServer

These steps are recommended, but they will not be very helpful if your data is not carefully prepared.
Please refer to a recent "[GeoServer on steroids](http://fr.slideshare.net/geosolutions/gs-steroids-foss4ge2014)" presentation for more information on GIS data optimizations. 

### libjpeg-turbo Map Encoder

Installing the libjpeg-turbo map encoder improves the throughput of your service by accelerating JPEG compression and decompression.

It requires:
 * native libs installed with eg. the [libjpeg-turbo-official debian package](http://sourceforge.net/projects/libjpeg-turbo/files/).
```
dpkg -i libjpeg-turbo-official_x.y.z_amd64.deb
```
installs the following files:
```
/opt/libjpeg-turbo/lib64/libturbojpeg.so.0
/opt/libjpeg-turbo/lib64/libjpeg.so
/opt/libjpeg-turbo/lib64/libjpeg.so.62
/opt/libjpeg-turbo/lib64/libturbojpeg.so
```
 * geoserver compiled using the ```libjpeg-turbo``` profile
 
 eg:
 ```mvn -P-all,geoserver -Plibjpeg-turbo -Dmaven.test.skip=true clean install```

 * in ```/etc/default/tomcat-geoserver0```:
```
JAVA_OPTS="$JAVA_OPTS \
            -Djava.library.path=/usr/lib/jni:/opt/libjpeg-turbo/lib64/"
```

Restart tomcat and check the new libs are taken into account: ```cat /var/log/tomcat9/geoserver0.log | grep turbo``` should display ```[turbojpeg.TurboJPEGMapResponse] - The turbo jpeg encoder is available for usage```

### Marlin Renderer

Marlin is an antialised rendering engine, which plugs into the JVM to replace the native implementation. 
Marlin combines the advantages of both rendering engines it replaces: it has the scalability of OpenJDK's "Pisces", and the speed of Oracle's "Ductus". 
Note that it only works on recent versions of Oracle and OpenJDK (>= 7).

Installing it is not difficult:
 * [grab the latest release](https://github.com/bourgesl/marlin-renderer/releases)
 * put the ```marlin-x.y.z.jar``` file into ```/usr/share/tomcat9/lib/``` (don't forget to chmod a+r marlin*.jar)
 * in ```/etc/defaults/tomcat-geoserver0```, add the following:

```
JAVA_OPTS="$JAVA_OPTS \
            -Xbootclasspath/a:"/usr/share/tomcat9/lib/marlin-x.y.z.jar" \
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

### More fonts

Add 'contrib' to your sourcelist : 
```
deb http://ftp.fr.debian.org/debian buster main contrib
```

And install the fonts : 
```
apt-get update
apt-get install ttf-mscorefonts-installer
```

And restart Geoserver : `service tomcat-geoserver0 restart`

To see if they are correctly installed, open the GeoServer "server status" page (````/geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.StatusPage```) and check that there are 72 fonts available.

### Fine tuning

Please refer to these excellent references:
 * "[Running in a Production Environment](http://docs.geoserver.org/stable/en/user/production/index.html)" section of the official GeoServer documentation,
 * [GeoServer training](http://geoserver.geo-solutions.it/edu/en/index.html) by GeoSolutions


## GeoWebCache

An interesting collection of tips and tricks can be found here: [http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html](http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html)
