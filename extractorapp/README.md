Extractorapp
============

Extractorapp allows SDI users to download data bundles from existing OGC web services (WFS for vector and WCS for rasters).

By default, the application allows extraction of layers and services which have been configured through the STARTUP_LAYERS and STARTUP_SERVICES configuration variables in the profile's GEOR_custom.js

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
    
Note that it is also possible to use a classical form submission and send this JSON string as the form's **data** field value.


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