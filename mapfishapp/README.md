Mapfishapp
==========

Mapfishapp is geOrchestra's advanced viewer and editor.

With it, you can :
 * browse through CSW, WMTS, WMS & WFS services and add any layer to the current map,
 * upload geo files for viewing (shapefile, mif/mid, tab, kml, gml, gpx),
 * create custom SLDs and style WMS layers, 
 * query layers either with a simple tool or an advanced one supporting conditions on attributes and geometries,
 * share your map with a permalink, or save it as a WMC file and restore it later,
 * load and create custom tools for specific needs,
 * export feature attributes,
 * [print](https://github.com/georchestra/template/blob/master/mapfishapp/WEB-INF/print/README.md) your map,
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

Currently, the integrated editor supports:
 * attributes editing for all layers and geometry types (according to the feature model publicized by WFS DescribeFeatureType),
 * drawing new points, lines and simple polygons with snapping on surrounding features.

Every WMS layer:
 * with a WFS equivalent service, **and**
 * whose URL matches the GEOR.config.EDITABLE_LAYERS regexp (provided by your config) 
... is available for edition.

By default:
 * the template config provides ```GEOR.config.EDITABLE_LAYERS = /.*@shared.server.name@.*/i``` which means that all WMS layers served by the platform host will be editable.
 * members of the ADMINISTRATOR group have the ability to see the edit functions provided by /mapfishapp/. This can be configured in GEOR_custom.js (in your profile), please have a look at the ROLES_FOR_EDIT config option.

In case the user does not have the rights to edit a layer, the first transaction will fail, and the changes will be lost.

There is no handling of concurrent edition nor feature locking for now.

NOTE: the previous **NS_EDIT** config option is deprecated

Your data in mapfishapp
========================

The geOrchestra viewer is able to query your data via OGC webservices. 
This implies that vector data is transmitted as XML over the air (via WMS getFeatureInfo or WFS getFeature).

Before reporting errors, please check that your data is correct.

Typically, layer names & field names should not:
 - include spaces nor accentuated chars,
 - start with a number.
 
Browsers like IE or FF will typically fail, while Chromium might just ignore the incorrect fields.

You should also take care not to insert special chars in the service description fields. Eg: ```"Service WMS de GéoPicardie - Département de l'Oise"``` will break your capabilities for IE, while it will work with ```"Service WMS de GéoPicardie - Département de lʼOise"``` or ```"Service WMS de GéoPicardie - Département de l&apos;Oise"```. GeoServer should do the mapping, but it does not at the moment.
Here are the corresponding strings to use:
```
"   &quot;
'   &apos;
<   &lt;
>   &gt;
&   &amp;
```

How to run the viewer without Tomcat ?
======================================

This mode is useful for **demo** or **development** purposes.

The *first* time, you need to previously compile mapfishapp and all its dependencies

    $ ./mvn -Dmaven.test.skip=true -Ptemplate -P-all,mapfishapp install;

then, each time you want to test a change in the configuration or the mapfishapp module:

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
