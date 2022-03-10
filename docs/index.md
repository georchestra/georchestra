geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 4), an **OGC server** (GeoServer 2.18) with fine-grained access control (based on GeoFence), an **advanced viewer and editor**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

## Releases

There are major and patch releases:
 * Major releases are supported during 12 months.
 * Migrating from one patch release to another does not require any configuration change. It is highly recommended.

Have a look at the [version numbering scheme](releases.md) and the [release notes](https://github.com/georchestra/georchestra/releases) for more information.


## Install

Depending on your goals and skills, there are several ways to install geOrchestra:

 * a [docker composition](https://github.com/georchestra/docker/blob/master/docker-compose.yml), which pulls pre-built images from [docker hub](https://hub.docker.com/u/georchestra/), is perfect for a quick start. Provided you have a good download speed and recent machine (8Gb required), you'll be up and running within 10 minutes. Read [how to run geOrchestra on Docker](https://github.com/georchestra/docker/blob/master/README.md) here. Use the branch matching the target version (`master` for dev purposes).
 * a contributed [ansible playbook](https://github.com/georchestra/ansible) allows you to spin an instance in a few minutes. This is probably the easiest way to create a small server, since it takes care of installing the middleware, fetching the webapps and configuring them. Same as above: use the branch matching target version.
 * [debian packages](https://packages.georchestra.org/) are perfect to create complex production architectures, but you'll have to [install and configure the middleware](setup.md) first. The community provides these packages on a "best effort" basis, with no warranty at all.
 * you could also use the [generic wars](https://packages.georchestra.org/) with their "[datadir](https://github.com/georchestra/datadir)", as an alternate method. The above packages provide both.
 * finally, [building from the sources](build.md) is also possible, but only relevant when customizations are needed. One gets custom WAR files, packages or docker images that can be [deployed](deploy.md) to dev, test, or production servers.

If you opt for the middleware setup by yourself, there are several [optimizations](optimizations.md), [good practices](good_practices.md) and [tutorials](tutorials.md) that are worth reading.
Note that the minimum system requirement is 2 cores and 8Gb RAM, but we recommend at least 4 cores and 16 Gb RAM for a production instance.
More RAM is of course better !


## Community

If you need more information, please ask on the [geOrchestra mailing list](https://groups.google.com/forum/#!forum/georchestra).

For help setting up your instance, or for dev-related questions, use the [#georchestra](https://kiwiirc.com/client/irc.freenode.net/georchestra) IRC channel or the [dev/tech list](https://groups.google.com/forum/#!forum/georchestra-dev).

If you found a bug or want to propose improvements and new features, please [fill a new issue](https://github.com/georchestra/georchestra/issues/new/choose) in the GitHub tracker.

If you want to report a **security issue**, please don't fill an issue. Instead, send a mail to <psc@georchestra.org>, you will be later contacted for more details.

## More

Additional information can be found in the [georchestra.org](http://www.georchestra.org/) website and in the following links:
 * [catalog](https://github.com/georchestra/geonetwork/blob/georchestra-gn3.4-18.06/README.md): standard GeoNetwork with a light customization,
 * [viewer](https://github.com/georchestra/georchestra/blob/master/mapfishapp/README.md) (aka mapfishapp): a robust, OGC-compliant webgis with editing capabilities,
 * [extractor](https://github.com/georchestra/georchestra/blob/master/extractorapp/README.md) (aka extractorapp): able to create zips from data served through OGC web services and send an email when your extraction is done,
 * [geoserver](http://geoserver.org/): the reference implementation for many OGC web services,
 * [geowebcache](http://geowebcache.org/): a fast and easy to use tile cache,
 * [geofence](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md): optional, advanced OGC web services security,
 * [analytics](https://github.com/georchestra/georchestra/blob/master/analytics/README.md): admin-oriented module, a front-end to the [ogc-server-statistics](https://github.com/georchestra/georchestra/blob/master/ogc-server-statistics/README.md) module,
 * [console](https://github.com/georchestra/georchestra/blob/master/console/README.md): also an admin-oriented module, to manage users and groups,
 * [header](https://github.com/georchestra/georchestra/blob/master/header/README.md): the common header which is used by all modules,
 * [atlas](https://github.com/georchestra/georchestra/blob/master/atlas/README.md): a server-side component to print multi-page PDF with one geographic feature per page.
