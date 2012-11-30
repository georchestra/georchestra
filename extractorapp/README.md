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
================================================

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
