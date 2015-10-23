# geOrchestra
[![Build Status](https://travis-ci.org/georchestra/georchestra.svg?branch=master)](https://travis-ci.org/georchestra/georchestra)

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 2.10), an **OGC server** (GeoServer 2.5.4 and GeoWebCache 1.5.4) with fine-grained access control (based on GeoFence), an **advanced viewer and editor**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

## Releases

A new release is published every 6 months and is supported during 12 months. Stable versions are named by their release date, eg 14.06 was published in June 2014.  

Before downloading, you might be interested in the [release notes](RELEASE_NOTES.md) and the [kanban board](https://huboard.com/georchestra/georchestra) we're using to manage issues.

## Download

To download the latest stable version (currently 15.06), use the following command line:
```
git clone --recursive https://github.com/georchestra/georchestra.git ~/georchestra
```

## Install

To install geOrchestra, you will have to:
 * [create your own configuration repository](doc/config.md), based on the [template](https://github.com/georchestra/template) we provide,
 * [build the web applications](doc/build.md) with your config,
 * [setup the middleware](doc/setup.md) (apache, tomcat, postgresql, openldap),
 * [deploy the webapps](doc/deploy.md), [check](doc/check.md) they're working as expected and finally [configure](doc/post-deploy_config.md) them.

There are also several [optimizations](doc/optimizations.md), [good practices](doc/good_practices.md) and [tutorials](doc/tutorials.md) that are worth reading.

The minimum system requirement is 2 cores and 4Gb RAM, but we recommend at least 4 cores and 8 Gb RAM for a production instance.
More RAM is of course better !

## Community

If you need more information, please ask on the [geOrchestra mailing list](https://groups.google.com/forum/#!forum/georchestra). 

For help setting up your instance, or for dev-related questions, use the [#georchestra](https://kiwiirc.com/client/irc.freenode.net/georchestra) IRC channel or the [dev/tech list](https://groups.google.com/forum/#!forum/georchestra-dev).

## More

Additional information can be found in the [georchestra.org](http://www.georchestra.org/) website and in the following links:
 * [catalog](https://github.com/georchestra/geonetwork/blob/georchestra-15.06/README.md): standard GeoNetwork with a light customization, 
 * [viewer](mapfishapp/README.md) (aka mapfishapp): a robust, OGC-compliant webgis with editing capabilities,
 * [extractor](extractorapp/README.md) (aka extractorapp): able to create zips from data served through OGC web services and send an email when your extraction is done, 
 * [geoserver](http://geoserver.org/): the reference implementation for many OGC web services,
 * [geowebcache](http://geowebcache.org/): a fast and easy to use tile cache,
 * [geofence](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md): optional, advanced OGC web services security,
 * [simple catalog](catalogapp/README.md) (aka catalogapp): a very lightweight UI to query CSW services,
 * [analytics](analytics/README.md): admin-oriented module, a front-end to the [ogc-server-statistics](ogc-server-statistics/README.md) and [downloadform](downloadform/README.md) modules,
 * [ldapadmin](ldapadmin/README.md): also an admin-oriented module, to manage users and groups,
 * [header](header/README.md): the common header which is used by all modules,
 * [epsg-extension](epsg-extension/README.md): a plugin to override the geotools srs definitions.
