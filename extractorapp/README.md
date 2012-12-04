Extractorapp
============

Extractorapp allows SDI users to download data bundles from existing OGC web services (WFS for vector and WCS for rasters).

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
 * **noheader** when set to true, the application does not load the static header
 * **lang** can be set to any of the following : fr, en, es


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


How to run the extractor without Tomcat ?
=========================================

This mode is useful for **demo** or **development** purposes.

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
