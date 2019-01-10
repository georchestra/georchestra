# Post-deploy configuration

This is a highly recommended reading !

## GeoServer

### Proxy base URL

For GeoServer, the proxy base url can be set in the admin UI, via Settings > Global > Proxy URL.
This is one of the first thing to do once your geoserver instance is deployed, or you won't be able to add local layers using our viewer.

The proxy base URL should be set to something like this: http(s)://your.server.fqdn/geoserver without a trailing slash.
After saving the form, you should check in the WMS capabilities that the service URLs are as expected.


### Supported SRS

By default, GeoServer supports more than 2000 spatial reference systems.  
This is really a lot, but you're probably not interested in 99% of them, and they clutter your GetCapabilities documents.

It's easy to restrict the list to the most useful ones: in the WMS and WCS admin pages, fill the "Limited SRS list" textarea with, eg:
```
2154, 3857, 3942, 3943, 3944, 3945, 3946, 3947, 3948, 3949, 3950, 4171, 
4258, 4326, 23030, 23031, 23032, 32630, 32631, 32632, 4171, 4271, 3758
```
... and don't forget to submit the form.

## Standalone GeoWebCache 

For the standalone GeoWebCache, the proxy should be automatically configured (via [geowebcache.properties](https://github.com/georchestra/datadir/blob/18.06/geowebcache/geowebcache.properties)).

## GeoNetwork

On the  ```/geonetwork/srv/eng/config``` page, you should:

* fill the "Site" and "Server" sections.  

In the server section, fill the fields according to your setup, eg:
```
Preferred Protocol  HTTP
Host                georchestra.mydomain.org
Port                80 	￼     
Secure Port	    8443
```

 * enable "XLINK RESOLVER"
 * enable INSPIRE + search panel
 * check "use Proxy" in case your connection to the internet is proxied
 * set feedback email

## GeoFence

In GeoFence, you should configure your geoserver instance with:
 * instance name = default-gs
 * description = my geoserver instance
 * base url = http://georchestra.mydomain.org/geoserver
 * username = geoserver_privileged_user 
 * password = the LDAP password of the above user, which should be the same as [the one referenced in your datadir](https://github.com/georchestra/datadir/blob/18.06/README.md).
