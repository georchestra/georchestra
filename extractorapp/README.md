Extractorapp
============

Extractorapp allows SDI users to download data bundles from existing OGC web services (WFS for vector and WCS for rasters).
Extraction jobs are queued and can be managed by any admin user. 
The application notifies by email the requesting user that the job has been take into account, and when it is finished.

By default, the application allows extraction of layers and services which have been configured through the STARTUP_LAYERS and STARTUP_SERVICES configuration variables in the profile's GEOR_custom.js


Parameters
==========

This behavior can be dynamically overriden by POSTing a JSON content to the home controller:

    {
	    "layers": [{
	        "layername": "ign:ign_bdtopo_departement",
	        "owstype": "WMS",
	        "owsurl": "http://ids.pigma.org/geoserver/ign/wms"
	    }],
        "services": [{
            "owstype": "WMS",
	        "owsurl": "http://ids.pigma.org/geoserver/ign_r/wms"
        }]
	}
    
Note that it is possible to use a classical form submission and send this JSON string as the form's **data** field value.

The application also accepts several GET parameters :
 * **debug** when set to true, the application loads unminified javascript files
 * **noheader** when set to true, the application does not load the header
 * **lang** can be set to any of the following : fr, en, es


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


Admin UI
========

Admin users have the ability to manage the job queue at this URL : /extractorapp/admin/

Jobs (except the running one) can be manually paused, cancelled, set to a higher or a lower priority.


How to allow unprotected access ?
=================================

By default, the application is not available to unauthenticated users. They are redirected to the CAS login page with this code in index.jsp :

    <c:choose>
        <c:when test='<%= anonymous == true %>'>
            <script type="text/javascript">
            window.location = "?login";
            </script>
        </c:when>
    </c:choose>
    
To grant access to all users, copy index.jsp in your profile and remove the above code. 


How to customize the default emails ?
=====================================

For each extraction job, two emails are sent :
 * the first one is an acknowledgment from the platform that the job has been taken into account,
 * the second one is sent when the job is finished. It contains the link to download an archive.

Templates for these emails can be found in config/defaults/extractorapp/WEB-INF/templates/
This gives you the opportunity to override them by copying to your own profile.

By default, the ack mail template does not support string substitution, but the second email template does.
These variables are:
 * **link** the HTTP link to download the data,
 * **emails** the recipient emails,
 * **expiry** the expiry date in days,
 * **successes** the layers for which the extraction succeeded,
 * **failures** the layers for which the extraction failed,
 * **oversized** the layers for which the extraction failed due to an oversized bounding box.

These template variables are defined in extractorapp/src/main/java/extractorapp/ws/EmailFactoryDefault.java

Note that you are free to define your own variables by using a custom EmailFactory, such as extractorapp/src/main/java/extractorapp/ws/EmailFactoryPigma.java. 
In this case, be sure to specify emailfactory=org.georchestra.extractorapp.ws.EmailFactoryPigma in your_config/extractorapp/maven.filter


How to run the extractor without Tomcat ?
=========================================

This mode is useful for **demo** or **development** purposes.

The *first* time, you need to previously compile extractorapp and all its dependencies

    $ ./mvn -Dmaven.test.skip=true -Ptemplate -P-all,extractorapp install;

then, each time you want to test a change in the configuration or the extractorapp module:

    $ cd extractorapp
    $ ../mvn -Ptemplate jetty:run

Point your browser to [http://localhost:8080/extractorapp/?noheader=true](http://localhost:8080/extractorapp/?noheader=true) 


**Want to trick the extractor into thinking you're logged in ?**

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-email = you@provider.com
 * sec-roles = ROLE_SV_USER or ROLE_SV_EDITOR or ROLE_SV_ADMIN
 
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
