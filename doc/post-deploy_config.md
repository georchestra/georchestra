# Post-deploy configuration

This is a highly recommended reading !

## GeoServer

### Proxy base URL

For GeoServer, the proxy base url can be set in the admin UI, via Settings > Global > Proxy URL.
This is one of the first thing to do once your geoserver instance is deployed, or you won't be able to add local layers using our viewer.

The proxy base URL should be set to something like this: http(s)://your.server.fqdn/geoserver without a trailing slash.
After saving the form, you should check in the WMS capabilities that the service URLs are as expected.


### GeoWebCache

For the standalone GeoWebCache, the proxy should be automatically configured. 

### Fonts

GeoServer uses the fonts available to the JVM for WMS styling through SLD.
To get more fonts than what the JVM offers by default, you should install the "core fonts for the web":

```
sudo apt-get install ttf-mscorefonts-installer
```

Restart tomcat-geoserver0 and check on the ```/geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.JVMFontsPage``` page that more fonts are loaded.


### Supported SRS

By default, GeoServer supports more than 2000 spatial reference systems.  
This is really a lot, but you're probably not interested in 99% of them, and they clutter your GetCapabilities documents.

It's easy to restrict the list to the most useful ones: in the WMS and WCS admin pages, fill the "Limited SRS list" textarea with, eg:
```
2154, 3857, 3942, 3943, 3944, 3945, 3946, 3947, 3948, 3949, 3950, 4171, 
4258, 4326, 23030, 23031, 23032, 32630, 32631, 32632, 4171, 4271, 3758
```
... and don't forget to submit the form.


## GeoNetwork

TODO:
 * proxied base url
 

## GeoFence

  * configure geoserver instance with geoserver_privileged_user and his LDAP password (defaults to the one provided by 
https://github.com/georchestra/template/blob/14.06/build_support/shared.maven.filters#L103)



