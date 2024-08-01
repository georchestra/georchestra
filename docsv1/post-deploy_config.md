# Post-deploy configuration

These are mandatory configuration steps once geOrchestra has been deployed.

## GeoServer

### Proxy base URL

For GeoServer, the proxy base url can be set in the admin UI, via Settings > Global > Proxy URL.
By default, it is set to `${X-Forwarded-Proto}://${X-Forwarded-Host}/geoserver` which should be OK.
Depending on your reverse proxy setup, this might fail guessing the correct FQDN.

In case the capabilities document (eg "/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities") fail to provide the correct FQDN at `/WMS_Capabilities/Capability/Request/GetCapabilities/DCPType/HTTP/Get/OnlineResource`, you should set the proxy base url to something like this: https://your.server.fqdn/geoserver (without a trailing slash).

After saving the form, you should check in the WMS capabilities that the service URLs are as expected.


### Supported SRS

By default, GeoServer supports more than 2000 spatial reference systems.  
This is really a lot, but you're probably not interested in 99% of them, and they clutter your GetCapabilities documents.

It's easy to restrict the list to the most useful ones: in the WMS and WCS admin pages, fill the "Limited SRS list" textarea with, eg for France:
```
2154, 3857, 3942, 3943, 3944, 3945, 3946, 3947, 3948, 3949, 3950, 4171, 
4258, 4326, 23030, 23031, 23032, 32630, 32631, 32632, 4171, 4271, 3758
```
... and don't forget to submit the form.

### In case of trouble with geoserver UI

See [docs/setup/tomcat.md#in-case-of-troubles-with-the-geoserver-ui](https://github.com/georchestra/georchestra/blob/master/docs/setup/tomcat.md#in-case-of-troubles-with-the-geoserver-ui)

## GeoNetwork

On the  ```/geonetwork/srv/fre/admin.console#/settings``` page, you should fill in the "Catalog description" and "Catalog server" sections.  

In the server section:
```
Preferred Protocol  https
Host                georchestra.mydomain.org
Port                443     
```

Next:
 * Enable XLink resolution
 * Enable INSPIRE
 * Change "Resource identifier prefix" from "http://localhost:8080/geonetwork/srv/resources" to whatever suits better (eg: changing the fqdn)


On `/geonetwork/srv/fre/admin.console#/settings/ui` :
 * check the "viewer" box, 
 * check the "external viewer" box, 
 * change the viewer base URL to https://your.fqdn/mapstore/, 
 * set the template URL to `/mapstore/#/?actions=[{"type":"CATALOG:ADD_LAYERS_FROM_CATALOGS","layers":["${service.name}"],"sources":[{"type":"${service.type}","url":"${service.url}"}]}]`


On `/geonetwork/srv/eng/admin.console#/classification` install INSPIRE themes thesaurus and any other relevant thesaurus.

