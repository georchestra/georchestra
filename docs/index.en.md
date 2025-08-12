---
hide:
  - navigation
  - toc
---

# Home

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 4), an **OGC server** (GeoServer 2.25) with fine-grained access control (based on GeoFence), an **advanced viewer and editor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

## Releases

There are major and patch releases:
* Major releases are supported during 12 months.
* Migrating from one patch release to another does not require any configuration change. It is highly recommended.

Have a look at the [version numbering scheme](releases.md) and the [release notes](https://github.com/georchestra/georchestra/releases) for more information.


## Community

If you need more information, please ask on the [geOrchestra mailing list](https://groups.google.com/forum/#!forum/georchestra).

For help setting up your instance, or for dev-related questions, use the [#georchestra](https://matrix.to/#/#georchestra:osgeo.org) IRC channel or the [dev/tech list](https://groups.google.com/forum/#!forum/georchestra-dev).

If you found a bug or want to propose improvements and new features, please [fill a new issue](https://github.com/georchestra/georchestra/issues/new/choose) in the GitHub tracker.

If you want to report a **security issue**, please don't fill an issue. Instead, send a mail to <psc@georchestra.org>, you will be later contacted for more details.


## More

Additional information can be found in the [georchestra.org](http://www.georchestra.org/) website and in the following links:
* [catalog](https://github.com/georchestra/geonetwork/): standard GeoNetwork with a light customization,
* [viewer](https://github.com/georchestra/mapstore2-georchestra#readme) (aka mapstore): a robust, OGC-compliant webgis with editing capabilities,
* [geoserver](http://geoserver.org/): the reference implementation for many OGC web services,
* [geowebcache](http://geowebcache.org/): a fast and easy to use tile cache,
* [geofence](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md): optional, advanced OGC web services security,
* [analytics](https://github.com/georchestra/georchestra/blob/master/analytics/README.md): admin-oriented module, a front-end to the [ogc-server-statistics](https://github.com/georchestra/georchestra/blob/master/ogc-server-statistics/README.md) module,
* [console](https://github.com/georchestra/georchestra/blob/master/console/README.md): also an admin-oriented module, to manage users and groups,
* [header](https://github.com/georchestra/georchestra/blob/master/header/README.md): the common header which is used by all modules,


## Requirements

These requirements are indicatives. For any doubt, please ask [the community](https://www.georchestra.org/community.html).

* Compatible with Linux-based systems. Debian stable recommended.
* Java 11 up to Java 17. Java 21 for the gateway.
* Tomcat 9 or Jetty 9
* PostreSQL 15 and higher



