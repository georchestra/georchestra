Mapfishapp
==========

Mapfishapp is geOrchestra's advanced viewer.

With it, you can :
 * browse through CSW, WMS & WFS services and add any layer to the current map,
 * create custom SLDs and style WMS layers, 
 * query layers either with a simple tool or an advanced one supporting conditions on attributes and geometries,
 * share your map with a permalink, or save it as a WMC file and restore it later,
 * load and create custom tools for specific needs,
 * export feature attributes,
 * print your map,
 * and many more ...

There's also a built-in simple feature editor using the WFS-T protocol.

Parameters
==========

The application accepts several GET parameters :
 * **wmc** points to a WMC file in order to override the default context,
 * **bbox** in the form left,bottom,right,top in WGS84 coordinates overrides the extent of any WMC,
 * **lon** and **lat** in WGS84 coordinates override the extent of any WMC to center the map,
 * **radius** in meters, when set in addition to the lon and lat params, allows to control the resulting zoom level,
 * **lang** can be set to any of the following : fr, en, es,
 * **debug** when set to true, the application loads unminified javascript files,
 * **noheader** when set to true, the application does not load the static header


It is also possible to POST a JSON string to the home controller, for instance :

    {
        "services": [{
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign_r/wms"
        }],
        "layers": [{
            "layername": "ign:ign_bdtopo_departement",
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign/wms"
        }, {
            "layername": "ign:ign_bdtopo_region",
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign/wms"
        }]
    }

In response, the viewer will add the above two layers to the map, and display a dialog window showing the layers from the http://ids.pigma.org/geoserver/ign_r/wms WMS server.


Recenter on referentials
========================

The application features a "recenter on referentials" widget, which enables users to search for any object they are familiar with (eg: states, cities, forests, ...).

This widget auto-configures itself with a GeoServer namespace (see **NS_LOC** config option in your config's GEOR_custom.js). By default, NS_LOC is set to "geor_loc", which means that any layer belonging to the geor_loc namespace will be available in the widget.

Each "referential" layer should obey these simple rules:
 * it has exactly one geometry column and one string column,
 * the string column can have any name, but its content should be uppercased (this is to overcome a WFS limitation),
 * the geometry column can be of any type (point, line, polygon) but if it's a polygon, it should be as simple as possible (a bounding box is the best option).


Feature editor
==============

Members of the groups SV_EDITOR, SV_REVIEWER or SV_ADMIN can reach the editor at /mapfishapp/edit

The editor looks for layers accessible via WFS-T in one GeoServer namespace (see **NS_EDIT** config option in your config's GEOR_custom.js). By default, NS_EDIT is set to "geor_edit", which means that any layer belonging to the geor_edit namespace and editable for the current user will be available.

Currently, only points, lines and simple polygons can be digitalized, and their attributes filled according to the feature model publicized by the WFS server.
Snapping on existing features is activated by default.

Once the update session is finished, the user can sync his work on the server. 
Unfortunately, there is no handling of concurrent edition for now.


How to run the viewer without Tomcat ?
======================================

This mode is useful for **demo** or **development** purposes.

    $ cd config
    $ ../mvn -Ptemplate install
    $ cd ../mapfishapp
    $ ../mvn -Ptemplate jetty:run

Point your browser to [http://localhost:8080/mapfishapp/?noheader=true](http://localhost:8080/mapfishapp/?noheader=true) 


**Want to trick the viewer into thinking you're logged in ?**

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-roles = ROLE_SV_USER or ROLE_SV_EDITOR or ROLE_SV_ADMIN
 
Note: this works only because the security proxy is not runnning.

How to use GDAL native libraries for file upload functionality ?
================================================================

The file upload functionality, that allows to upload a vectorial data file to mapfishapp in order to display it as a layer, relies normally on GeoTools. However, the supported file formats are limited (at 07/12/2013: shp, mif, gml and kml). In order to increase the number of supported file formats, you can install GDAL and GDAL java bindings libraries on the server. This would give access, for example, to extra formats such as gpx or tab.

The key element for calling the GDAL/OGR native library from mapfishapp is the **imageio-ext library** (see https://github.com/geosolutions-it/imageio-ext/wiki). It relies on:
 * jar files, that are included at build by maven,
 * a java binding library for GDAL, based on the JNI framework,
 * and obviously the GDAL library.

The latter can be installed, on Debian-based distributions, with the libgdal1 package:

    sudo apt-get install libgdal1

Some more work is needed for installing the java binding library for GDAL, as there is still no deb package for it (for more information, see https://bugs.launchpad.net/ubuntu/+source/gdal/+bug/786790 - note that packages exist for ruby and perl bindings, hopefully the java's one will be released soon).
