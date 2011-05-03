package extractorapp.ws.extractor

/**
 * Encapsulates some sample data used by the specs
 */
object SampleData {
    val wfsURL = "http://drebretagne-geobretagne.int.lsn.camptocamp.com:80/geoserver/wfs/WfsDispatcher"
    val vectorLayerName = "geob:communes_geofla"
    val wcsURL = "http://drebretagne-geobretagne.int.lsn.camptocamp.com:80/geoserver/wcs/WcsDispatcher2"
    val rasterLayerName = "geob_loc:PAYS_region"
    val sampleJSON = """|{  
                        |  "emails" : [
                        |       "address1",
                        |       "address2"
                        |   ],
                        |   "globalProperties": {
                        |       "projection": "EPSG:2154",
                        |       "rasterFormat": "png",
                        |       "vectorFormat": "shp",
                        |       "resolution":.25,
                        |       "bbox": {
                        |           "srs": "EPSG:2154",
                        |           "value": [111335.2,6704491.9,400594.5,6881488.6]
                        |       }
                        |   },
                        |   "layers": [
                        |       {
                        |           "owsUrl": "%s",
                        |           "owsType": "WFS",
                        |           "layerName": "%s",
                        |           "projection": null,
                        |           "format": null,
                        |           "bbox": {
                        |               "srs": "EPSG:4326",
                        |               "value": [0,45,6,55]
                        |           }
                        |       }, {
                        |           "owsUrl": "%s",
                        |           "owsType": "WCS",
                        |           "layerName": "%s",
                        |           "projection": "EPSG:27572",
                        |           "bbox": null
                        |       }
                        |   ]
                        |}""".stripMargin.format(wfsURL, vectorLayerName, wcsURL, rasterLayerName)

}