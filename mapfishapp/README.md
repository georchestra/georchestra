Mapfishapp
==========

Mapfishapp is geOrchestra's advanced viewer.

With it, you can :
 * browse through CSW, WMS & WFS services and add any layer to the current map,
 * create custom SLDs and style WMS layers, 
 * query layers either with a simple tool or an advanced one supporting conditions on attributes and geometries,
 * save your map as a WMC file and restore it later,
 * export feature attributes,
 * print your map,
 * and many more ...


Parameters
==========

The application accepts several GET parameters :
 * **wmc** points to a WMC file in order to override the default context
 * **lang** can be set to any of the following : fr, en, es
 * **debug** when set to true, the application loads unminified javascript files
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
 * the string column can have any name, but it's content should be uppercased (this is to overcome a WFS limitation),
 * the geometry column can be of any type (point, line, polygon) but if it's a polygon, it should be as simple as possible (a bounding box is the best option).


How to run the viewer without Tomcat ?
======================================

This mode is useful for **demo** or **development** purposes.

    $ cd mapfishapp
    $ ../mvn -Ptemplate jetty:run

Point your browser to [http://localhost:8080/mapfishapp/?noheader=true](http://localhost:8080/mapfishapp/?noheader=true) 


**Want to trick the viewer into thinking you're logged in ?**

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-roles = ROLE_SV_USER or ROLE_SV_EDITOR or ROLE_SV_ADMIN
 
Note: this works only because the security proxy is not runnning.
