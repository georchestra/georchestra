Mapfishapp
==========

![mapfishapp](https://github.com/georchestra/georchestra/workflows/mapfishapp/badge.svg)

Mapfishapp is geOrchestra's advanced viewer and editor.

With it, you can :
 * browse through CSW, WMTS, WMS & WFS services and add any layer to the current map,
 * upload geo files for viewing (shapefile, tab, kml, gml, gpx),
 * create custom SLDs and style WMS layers,
 * query layers either with a simple tool or an advanced one supporting conditions on attributes and geometries,
 * share your map with a permalink, or save it as a WMC file and restore it later,
 * load and create custom tools for specific needs (read [how](src/main/webapp/app/addons/README.md)),
 * export feature attributes,
 * [print](https://github.com/georchestra/template/blob/master/mapfishapp/WEB-INF/print/README.md) your map,
 * and many more ...

There's also a built-in simple feature editor using the WFS-T protocol.

Parameters
==========

The application accepts several GET parameters :
 * **wmc** points to a WMC file in order to override the default context,
 * **addons** is a comma separated list of already known addon ids,
 * **bbox** in the form left,bottom,right,top in WGS84 coordinates overrides the extent of any WMC,
 * **lon** and **lat** in WGS84 coordinates override the extent of any WMC to center the map,
 * **radius** in meters, when set in addition to the lon and lat params, allows to control the resulting zoom level,
 * **file** points to the URL of a "geofile" (zipped SHP, KML, GPX, etc) to open for viewing,
 * **lang** can be set to any of the following : fr, en, es, ru, de,
 * **debug** when set to true, the application loads unminified javascript files,
 * **noheader** when set to true, the application does not load the header.
 * **layername**, **owstype** and **owsurl** are used to load OGC layers, or to browse OGC servers

Valid query strings:
 * ?lon=2.961&lat=45.770&radius=5000
 * ?bbox=2.86,44.84,3.32,45.02
 * ?owstype=WMS&owsurl=http://server/ows
 * ?layername=layer&owstype=WMSLayer&owsurl=http://server/ows
 * ?layername=layer1,layer2&owstype=WMSLayer,WMSLayer&owsurl=http://server1/ows,https://server2/ows
 * ?layername=layer1,layer2&owstype=WMSLayer,WMSLayer&owsurl=http://server1/ows,https://server2/ows&wmc=https://server3/path/to/file.wmc


It is also possible to POST a JSON string to the home controller, for instance :

    {
        "services": [{
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign_r/wms"
        }],
        "layers": [{
            "layername": "ign:ign_bdtopo_departement",
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign/wms",
            "cql_filter": "id_dept = 47"
        }, {
            "layername": "ign:ign_bdtopo_region",
            "owstype": "WMS",
            "owsurl": "http://ids.pigma.org/geoserver/ign/wms"
        }],
        "search": {
            "owsurl":"http://ids.pigma.org/geoserver/ows",
            "cql_filter": "id_dept = 48",
            "typename":"ign:ign_bdtopo_departement"
        }
    }

In response, the viewer will :
* add the above two layers to the map
* display a dialog window showing the layers from the http://ids.pigma.org/geoserver/ign_r/wms WMS server.
* The department will only display features which have id_dept equals to 47
* A searchbar will open with features which have id_dept equals to 48 and recenter on the result extent.



CSWquerier
==========

CSWquerier is the function behind the "find in catalog" form. It performs CSW queries on remote catalogs to find metadata and its linked data (WMS layers).

### Data types

By default, the CSWquerier limits search on ```type = dataset || series```.
Other types will be ignored.

### Words

CSWquerier splits the search phrase into words, using ```,;:/%()!*.[]~&=``` as word separators.
Then, it builds filters based on the ```CSW_FILTER_PROPERTIES``` parameter (see GEOR_custom.js).

Such a search phrase:

    "edoras cadastral parcel"

... becomes this filter set:

    (Title like edoras*
    OR AlternateTitle like edoras*
    OR Abstract like edoras*
    OR Subject like edoras*
    OR OrganisationName like edoras*)
    AND
    (Title like edoras*
    OR AlternateTitle like cadastral*
    OR Abstract like cadastral*
    OR Subject like cadastral*
    OR OrganisationName like cadastral*)
    AND
    (Title like edoras*
    OR AlternateTitle like parcel*
    OR Abstract like parcel*
    OR Subject like parcel*
    OR OrganisationName like parcel*)

... focusing on specific ISO queryables and avoiding false positive results.

If you find it too restrictive, you can opt for the 'AnyText' property :

```
    CSW_FILTER_PROPERTIES = ['AnyText']
```

### Special words

Words prefixed with special characters always will limit the search on respective ISO queryable filters.

 * ```#``` for Subject (keywords) search
   * example ```#Cadastral``` will look for md with subject="Cadastral"

 * ```@``` for OrganisationName search
   * example ```@DREAL``` will look for md with OrganisationName="DREAL"

 * ```?``` for AnyText search
   * example ```?fishermen``` will look for md with AnyText~"fishermen*"

 * ```-``` to exclude a term on AnyText
   * example ```-fishermen``` will exclude md matching AnyText~"fishermen*"

Beware : those searches may be case sensitive depending on the CSW service implementation.


### Exact title/id match

A common usecase is metadata exact match : copy-paste the metadata title, alternate title or id to quickly discover the data.

CSWquerier will always add those filters to this end:
```
  OR Title='searchphrase*'
  OR AlternateTitle='searchphrase'
  OR Identifier='searchphrase'
  OR ResourceIdentifier='searchphrase'
```

### Spatial filter

Metadata search may be restricted to a specific extent using the ```CSW_FILTER_SPATIAL``` parameter, for example :

```
    CSW_FILTER_SPATIAL = [-5,45,0,55]
```

A null value activates the auto extent mode, excluding datas not intersecting the map extent.


Recenter on referentials
========================

The application features a "recenter on referentials" widget, which enables users to search for any object they are familiar with (eg: states, cities, forests, ...).

This widget auto-configures itself with a GeoServer namespace (see `NS_LOC` config option in your datadir's GEOR_custom.js). By default, `NS_LOC` is set to "geor_loc", which means that any layer belonging to the geor_loc namespace will be available in the widget.

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
 * whose URL matches the `GEOR.config.EDITABLE_LAYERS` regexp (provided by the config)

 ... is available for edition.

By default:
 * the config provides `GEOR.config.EDITABLE_LAYERS = /.*/i` which means that all WMS layers will be editable.
 * it is overriden by the `GEOR.custom.EDITABLE_LAYERS` if set. By default, the [datadir](https://github.com/georchestra/datadir/blob/master/mapfishapp/js/GEOR_custom.js) provides `GEOR.custom.EDITABLE_LAYERS = /.*georchestra.mydomain.org.*/i`, which means that all WMS layers served by the platform host will be editable.
 * members of the ADMINISTRATOR group have the ability to see the edit functions provided by /mapfishapp/. This can be configured in GEOR_custom.js (in the datadir), please have a look at the `ROLES_FOR_EDIT` config option.

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

The *first* time only, you have to compile mapfishapp and it's dependencies.
From the project root:

    $ mvn -Dmaven.test.skip=true -P-all,mapfishapp install

Clone the [geOrchestra datadir](https://github.com/georchestra/datadir/) into eg `/etc/georchestra`, checkouting the same branch name as your geOrchestra sources.

Once this is done, running mapfishapp is pretty simple with Jetty:

    $ cd mapfishapp
    $ mvn jetty:run

If you installed the datadir in another directory, eg `/var/tmp/georchestra`, you will have to provide the following options:

    $ mvn -Dgeorchestra.datadir=/var/tmp/georchestra -Dmapfish-print-config=/var/tmp/georchestra/mapfishapp/print/config.yaml jetty:run

Then, point your browser to [http://localhost:8287/mapfishapp/?noheader=true](http://localhost:8287/mapfishapp/?noheader=true).

**Want to trick the viewer into thinking you're logged in ?**

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-roles = ROLE_USER or ROLE_GN_EDITOR or ROLE_GN_ADMIN

Note: this works only because the security proxy is not runnning.

