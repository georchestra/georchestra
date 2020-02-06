Extractorapp
============

![extractorapp](https://github.com/georchestra/georchestra/workflows/extractorapp/badge.svg)

Extractorapp allows SDI users to download data bundles from existing OGC web services (WFS for vector and WCS for rasters).
Extraction jobs are queued and can be managed by any admin user.
The application notifies by email the requesting user that the job has been taken into account, and when it is finished.

The users can extract layers from inside the mapfishapp viewer, thanks to the [extractor addon](../mapfishapp/src/main/webapp/app/addons/extractor/README.md) that shows a "Download" option in the layers, if the user is connected and has the rights to do so (`ROLE_EXTRACTORAPP` role by default).

Note: in previous versions, the extractorapp had its own UI. It has been removed and the preferred way to extract data now is through the [extractor addon](mapfishapp/src/main/webapp/app/addons/extractor/README.md) in the mapfishapp viewer. It's also possible to use the REST API (not covered by this README).

Metadata extraction
===================

Note that extractorapp includes an XML metadata file in the data bundle when available.

This metadata file is extracted if the WMS `GetCapabilities` response, for the selected layer, contains a `<MetadataURL>` entry with
- the `text/xml` format
- the `ISO19115:2003` type.

For example:

```
<MetadataURL type="ISO19115:2003">
    <Format>text/xml</Format>
    <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.university.edu/metadata/roads.xml" />
</MetadataURL>
```

This metadata information is available only after some configuration in the WMS server. When configuring a layer in GeoServer (http://docs.geoserver.org/stable/en/user/webadmin/data/layers.html#basic-info), fill:
- the **Type** field with `ISO19115:2003`
- the **Format** field with `text/xml`
- the **URL** with the address of the XML ISO19115 metadata document. In GeoNetwork, the XML metadata documents can be accessed by the following URL pattern: `/geonetwork/srv/en/xml.metadata.get?uuid=...`.

Note that various metadata URLs may be specified for a layer in the WMS server, for example an XML metadata document for machines and an HTML document for humans:

```
<MetadataURL type="ISO19115:2003">
    <Format>text/xml</Format>
    <OnlineResource xlink:type="simple" xlink:href="http://www.geopicardie.fr/geonetwork/srv/fre/xml.metadata.get?uuid=c3bf8ed4-2967-4800-ad9c-33d9bf87ab94"/>
</MetadataURL>
<MetadataURL type="ISO19115:2003">
    <Format>text/html</Format>
    <OnlineResource xlink:type="simple" xlink:href="http://www.geopicardie.fr/geonetwork/apps/georchestra/?uuid=c3bf8ed4-2967-4800-ad9c-33d9bf87ab94"/>
</MetadataURL>
```

Raster resolution
=================

The default raster resolution is set by the value of the ```DEFAULT_RESOLUTION``` config option (defaults to 10 meters).

Note that, since [#726](https://github.com/georchestra/georchestra/issues/726) (released with 14.12), any layer with a MetadataURL pointing to a valid XML document mentioning the raster resolution will be extracted with this native resolution by default.
To this end, administrators will have to make sure that the XPATH expression provided by the ```METADATA_RESOLUTION_XPATH``` config option is correct for their setup.


Admin UI
========

Members of the ```ADMINISTRATOR``` LDAP group have the ability to manage extraction jobs at this URL : /extractorapp/admin/

Jobs (except the running one) can be manually paused, cancelled, set to a higher or a lower priority.

The UI is just a lightweight frontend to a REST API.


How to configure the user interface (mapfishapp extractor addon) ?
==================================================================

See the addon's [README](../mapfishapp/src/main/webapp/app/addons/extractor/README.md), and its [datadir](https://github.com/georchestra/datadir/tree/18.06/mapfishapp/addons/extractor).


How to customize the default emails ?
=====================================

For each extraction job, two emails are sent :
 * the first one is an acknowledgment from the platform that the job has been taken into account,
 * the second one is sent when the job is finished. It contains the link to download an archive.

Templates for these emails are defined in the [datadir](https://github.com/georchestra/datadir/tree/18.06/extractorapp/templates).
This gives you the opportunity to adapt them in your own datadir.

By default, the ack mail template does not support string substitution (apart from `{publicUrl}`, that will be replaced by the instance URL), but the second email template does.
These variables are:
 * **link** the HTTP link to download the data,
 * **emails** the recipient emails,
 * **expiry** the expiry date in days,
 * **successes** the layers for which the extraction succeeded,
 * **failures** the layers for which the extraction failed,
 * **oversized** the layers for which the extraction failed due to an oversized bounding box.

These template variables are defined in [EmailFactoryDefault.java](src/main/java/extractorapp/ws/EmailFactoryDefault.java)

How to run the extractor without Tomcat ?
=========================================

This mode is useful for **demo** or **development** purposes.

The *first* time, you need to previously compile extractorapp and all its dependencies

    $ mvn -Dmaven.test.skip=true -P-all,extractorapp install;

then, each time you want to test a change in the configuration or the extractorapp module:

    $ cd extractorapp
    $ mvn jetty:run

Point your browser to [http://localhost:8283/extractorapp/admin/](http://localhost:8283/extractorapp/admin/) to see the administration interface.


**Want to trick the extractor into thinking you're logged in ?**

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-email = you@provider.com
 * sec-roles = ROLE_USER or ROLE_GN_EDITOR or ROLE_GN_ADMIN

Note: this works only because the security proxy is not runnning.



Debugging
=========

If you want to actively debug, you have to set the environment variable MAVEN_OPTS:

    $ export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"

Then run jetty normally. In eclipse (or other IDE), create a run configuration that attaches to port 8000.
In the above configuration the server will run with or without a debugger attached.
If you want to debug startup then change 'suspend=n' to 'suspend=y'.
With this configuration, the server will wait for a debugger to attach before starting.

To disable the debugging simply reset MAVEN_OPTS :

    $ export MAVEN_OPTS=


Unit tests
==========

The client server unit tests have a timeout of 30 seconds.  It means that if they fail then the tests will take a very long time.
Change the time down to 3s instead to debug the tests. Individually, they should normally pass with a 3 second timeout.
A few will fail somewhat randomly with that timeout but once the obvious errors are fixed, then increase timeout again to see if they pass.
The timeout is set in ParallelTest.scala.

If you want to debug unit tests you need to do the following:

    $ mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000" test

This is because each test is run in its own process so the MAVEN_OPTS are not passed to the tests. Since suspend=y each test will wait until you attach the debugger.
