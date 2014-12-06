# Post-deploy configuration

This is a higly recommended reading !

## GeoServer / GeoWebCache

### Proxy base URL

For GeoServer, the proxy base url can be set in the admin UI, via Settings > Global > Proxy URL.
This is one of the first thing to do once your geoserver instance is deployed, or you won't be able to add local layers using our viewer.

The proxy base URL should be set to something like this: http(s)://your.server.fqdn/geoserver without a trailing slash.
After saving the form, you should check in the WMS capabilities that the service URLs are as expected.

For the standalone GeoWebCache, TODO 

### Native JAI

By default, GeoServer and GeoWebCache ship with a the JAVA JAI classes, which run everywhere, but are not as fast as the native one.
Raster operations will be ~ two times faster when the native JAI and imageio are installed: 

```
sudo apt-get install libjai-imageio-core-java
```

Then, make sure that the following 5 jars are loaded by your geoserver and geowebcache tomcat classloaders: jai_codec.jar, jai_core.jar, jai_imageio.jar, clibwrapper_jiio.jar, mlibwrapper_jai.jar

This is usually done by symlinking them from their original location (something like ```/usr/share/java```) to the ```${catalina.base}/lib``` directory (for the common classloader).


### Control-Flow

For fairness reasons, you should setup limits to the number of concurrent requests handled by your GeoServer. 

By default, geOrchestra GeoServer ships with the [control flow](http://docs.geoserver.org/stable/en/user/extensions/controlflow/index.html) module installed.

If you have followed this guide, your geoserver probably also uses our recommended "geoserver data dir", which includes a basic [controlflow config file](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/controlflow.properties).

If not, you should create a custom ```controlflow.properties``` file in your geoserver "data dir".  
Please refer to the [control-flow module documentation](http://docs.geoserver.org/latest/en/user/extensions/controlflow/index.html) for the syntax.

### Fonts

GeoServer uses the fonts available to the JVM for WMS styling.
You should install the "core fonts for the web":

```
sudo apt-get install ttf-mscorefonts-installer
```

Restart tomcat-geoserver0 and check on the /geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.JVMFontsPage page that these are loaded.

### Supported SRS

By default, GeoServer supports more than 2000 spatial reference systems.  
This is really a lot, but you're probably not interested in 99% of them, and they clutter your GetCapabilities documents.

It's easy to restrict the list to the most useful ones: in the WMS and WCS admin pages, fill the "Limited SRS list" textarea with, eg:
```
2154, 3857, 3942, 3943, 3944, 3945, 3946, 3947, 3948, 3949, 3950, 4171, 4258, 4326, 23030, 23031, 23032, 32630, 32631, 32632, 4171, 4271, 3758
```
... and don't forget to submit the form.

### Fine tuning

Please refer to the excellent "[Running in a Production Environment](http://docs.geoserver.org/stable/en/user/production/index.html)" section of the GeoServer documentation.

For GeoWebCache, a collection of tips and tricks can be found here: [http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html](http://geo-solutions.blogspot.fr/2012/05/tips-tricks-geowebcache-tweaks.html)



## GeoNetwork

TODO:
 * proxied base url
 *

## GeoFence

  * configure geoserver instance with geoserver_privileged_user and his LDAP password (defaults to the one provided by 
https://github.com/georchestra/template/blob/14.06/build_support/shared.maven.filters#L103)



