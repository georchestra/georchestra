geOrchestra
===========

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 2.10), an **OGC server** (GeoServer 2.3.2 and GeoWebCache 1.5.1) with fine-grained access control (based on GeoFence), an **advanced viewer and editor**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

Here are our main modules:
 * [catalog](https://github.com/georchestra/geonetwork/blob/georchestra-14.06/README.md) (aka GeoNetwork)
 * [viewer](mapfishapp/README.md) (aka mapfishapp)
 * [extractor](extractorapp/README.md) (aka extractorapp)
 * [geofence](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md)
 * [simple catalog](catalogapp/README.md) (aka catalogapp)
 * [analytics](analytics/README.md)
 * [ldapadmin](ldapadmin/README.md)
 * [downloadform](downloadform/README.md)
 * [ogc-server-statistics](ogc-server-statistics/README.md)
 * [header](header/README.md)

A new release is published every 6 months and is supported during 12 months. Stable versions are named by their release date, eg 14.06 was published in June 2014.  
The development branch is ```master```. Please refer to the [release notes](RELEASE_NOTES.md) for more information.

To download the latest stable version, use the following command line:
```
git clone --recursive https://github.com/georchestra/georchestra.git
```

To install geOrchestra, you will have to:
 * [create your own configuration repository](doc/config.md), based on the [template](https://github.com/georchestra/template) we provide,
 * [build the web applications](doc/build.md) with your config,
 * [setup the middleware](doc/setup.md) (apache, tomcat, postgresql, openldap ...),
 * [deploy the webapps](doc/deploy.md).
