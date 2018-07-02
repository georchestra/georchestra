# UPGRADING from 14.06 to 14.12

As said previously, the [documentation](README.md) was improved in order to reflect the most recent project changes.
Most notably, it is now in line with the "3 tomcats"-based setup that drives the [template configuration](https://github.com/georchestra/template) since geOrchestra 14.06.   
It also includes an interesting "[how to upgrade your config](docs/how_to_upgrade.md)" generic guide (that you should read, and maybe contribute to !).

**config changes**

We introduced a new global config option: ```shared.url.scheme``` which defaults to http.  
Set to https (along with ```shared.server.port``` to 443) if your SDI requires it. Do not forget to also change the base url of GeoServer and GeoNetwork in their respectives admin GUI.

In mapfishapp's GEOR_custom.js:
 * the ```WMS_SERVERS```, ```WMTS_SERVERS```, ```WFS_SERVERS``` config options have been removed. The server definitions are now loaded via separate XHR's. You should migrate your content into the newly introduced ```myprofile/mapfishapp/*.json``` files.
 * ```CONTEXT_LOADED_INDICATOR_DURATION``` was added to handle the "context loaded" popping down indicator duration. It defaults to 5 seconds. Set to 0 to disable the indicator.
 * ```CONTEXTS``` was an array of arrays. It is now an array of objects.

Eg, in 14.06:
```js
[
    ["OpenStreetMap", "app/img/contexts/osm.png", "default.wmc", "A unique OSM layer"],
    [...]
]
```

From 14.12 on:
```js
[{
    label: "OpenStreetMap",
    thumbnail: "app/img/contexts/osm.png",
    wmc: "default.wmc",
    tip: "A unique OSM layer",
    keywords: ["OpenStreetMap", "Basemap"]
}, {
    ...
}]
```

Also, the print templates have been improved.  
If you made changes to the previous templates, you have to migrate them, or you may also keep your older templates.

In extractorapp's GEOR_custom.js, several new javascript config options have been added, related to [#726](https://github.com/georchestra/georchestra/issues/726): ```SUPPORTED_RESOLUTIONS```, ```DEFAULT_RESOLUTION```, ```METADATA_RESOLUTION_XPATH```. Make sure your configuration is up to date with the template configuration, or you will get these variable defaults.

Note also the addition of an ```excluded``` directory in the template configuration. The content of this directory will be ignored when creating the configuration jar, which is deployed in each webapp. This is a convenient way to store scripts and so on, versioned with your configuration.

**new repositories**

We decided to publish resources for your server "htdocs" folder.
Have a look at our [georchestra/htdocs](https://github.com/georchestra/htdocs) repository to get some inspiration.

As you may know, since geOrchestra 14.06, we recommend to start from our [minimal GeoServer "data dir"](https://github.com/georchestra/geoserver_minimal_datadir), rather than using GeoServer's default.
For the 14.12 release, we also decided to publish a [minimal GeoNetwork data dir](https://github.com/georchestra/geonetwork_minimal_datadir) too !

**apache configuration**

In geOrchestra's security proxy, there's an OGC proxy which we use to circumvent browser's [same origin policy](http://en.wikipedia.org/wiki/Same-origin_policy).
To prevent anyone to use this proxy (see [#755](https://github.com/georchestra/georchestra/issues/755)), we recommend to restrict access to the proxy by checking the request [Referer header](http://en.wikipedia.org/wiki/HTTP_referer), eg for apache <= 2.2 with:

```
SetEnvIf Referer "^http://my\.sdi\.org/" mysdi
<Proxy http://localhost:8180/proxy/*>
    Order deny,allow
    Deny from all
    Allow from env=mysdi
</Proxy>
```

There's also another improvement when your SDI is not globally accessed through https: securing your communications to the ldapadmin webapp through the following:
```
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/ldapadmin/?.*$
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
```
This can also be used to secure other modules, eg analytics, downloadform...

**building**

If you experience build issues, please clear your local maven repository (rm -rf ~/.m2/repository/), then try again.  

Our [continuous integration process](https://sdi.georchestra.org/ci/job/georchestra-template/) now checks every day that all geOrchestra modules (including GeoFence) build smoothly.  
We also make sure that the following geoserver extensions are compatible: app-schema, authkey, charts, control-flow, css, csw, dds, dxf, feature-aggregate, feature-pregeneralized, geosearch, gdal, imagemap, inspire, istyler, kml, libjpeg-turbo, mysql, ogr, pyramid, script, spatialite, xslt, wps, w3ds. They can be integrated in your geoserver deployment by adding them in the build command line, eg with
```
mvn -Dserver=template -Dmaven.test.skip=true -Pgeofence -Pcontrol-flow,css,csw,gdal,inspire,kml,libjpeg-turbo,ogr,pyramid,spatialite,wps,w3ds clean install
```
