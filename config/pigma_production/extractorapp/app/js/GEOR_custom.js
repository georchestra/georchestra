/**
 * geOrchestra extractor config file
 */

Ext.namespace("GEOR");

GEOR.custom = {
    /**
     * Constant: GEOSERVER_WMS_URL
     * The URL to GeoServer WMS.
     */
    //GEOSERVER_WMS_URL: "/geoserver/wms",

    /**
     * Constant: GEOSERVER_WFS_URL
     * The URL to GeoServer WFS.
     */
    //GEOSERVER_WFS_URL: "/geoserver/wfs",

    /**
     * Constant: MAX_FEATURES
     * The maximum number of vector features displayed.
     */
    //MAX_FEATURES: 500,
    
    /**
     * Constant: MAX_LENGTH
     * The maximum number of chars in a XML response 
     * before triggering an alert.
     */
    //MAX_LENGTH: 500000,

    /**
     * Constant: MAP_DOTS_PER_INCH
     * {Float} Sets the resolution used for scale computation.
     * Defaults to GeoServer defaults, which is 25.4 / 0.28
     */
    //MAP_DOTS_PER_INCH: 25.4 / 0.28,

    /**
     * Constant: GLOBAL_EPSG
     * SRS of the map used to select the global extraction parameters
     */
    GLOBAL_EPSG: "EPSG:4326",

    /**
     * Constant: MAP_XMIN aka "left"
     * {Float} The max extent xmin in GLOBAL_EPSG coordinates.
     * Defaults to -180
     */
    MAP_XMIN: -2.2,

    /**
     * Constant: MAP_YMIN aka "bottom"
     * {Float} The max extent ymin in GLOBAL_EPSG coordinates.
     * Defaults to -90
     */
    MAP_YMIN: 42.6,

    /**
     * Constant: MAP_XMAX aka "right"
     * {Float} The max extent xmax in GLOBAL_EPSG coordinates.
     * Defaults to 180
     */
    MAP_XMAX: 1.9,

    /**
     * Constant: MAP_YSMAX aka "top"
     * {Float} The max extent ymax in GLOBAL_EPSG coordinates
     * Defaults to 90
     */
    MAP_YMAX: 46,
    
    /**
     * Constant: BASE_LAYER_NAME
     * The WMS base layer which will be displayed under each extracted layer.
     * Defaults to "geor:countries"
     */
    BASE_LAYER_NAME: "pigma:baselayer",
        
    /**
     * Constant: NS_LOC
     * {String} The referentials layers' namespace alias as defined in
     *    the GeoServer configuration.
     * Defaults to "geor_loc"
     */
    NS_LOC: "pigma_loc",
        
    /**
     * Constant: DEFAULT_WCS_EXTRACTION_WIDTH
     * Default width of the extracted image from WCS. This constant
     * is to be used to calculate the default resolution of WCS.
     * Defaults to 1024
     *
     * FIXME: not sure it is really useful.
     *
     */
    //DEFAULT_WCS_EXTRACTION_WIDTH: 1024,

    /**
     * Constant: SUPPORTED_REPROJECTIONS
     * List of projections that extractor supports for reprojection
     */
    SUPPORTED_REPROJECTIONS: [
        ["EPSG:27563", "EPSG:27563 - Lambert Sud France"], 
        ["EPSG:27572", "EPSG:27572 - Lambert II étendu"],
        ["EPSG:2154", "EPSG:2154 - Lambert 93"],
        ["EPSG:3943", "EPSG:3943 - Lambert-93 CC43"],
        ["EPSG:3944", "EPSG:3944 - Lambert-93 CC44"],
        ["EPSG:3945", "EPSG:3945 - Lambert-93 CC45"],
        ["EPSG:4171", "EPSG:4171 - RGF93"],
        ["EPSG:4326", "EPSG:4326 - WGS84"]
    ],

    /**
     * Constant: EXTRACT_BTN_DISABLE_TIME
     * Duration in seconds for the extract button being disabled after an extraction
     * Defaults to 30
     */
    //EXTRACT_BTN_DISABLE_TIME: 30,

    /**
     * Constant: LAYERS_CHECKED
     * Layers checked by default or not ?
     * Defaults to true
     */
    //LAYERS_CHECKED: true,
    
    /**
     * Constant: BUFFER_VALUES
     * {Array} Array of buffer values with their display name
     */
    /*BUFFER_VALUES: [
        [0,'aucun'],
        [10,'10 mètres'],
        [50,'50 mètres'],
        [100,'100 mètres'],
        [500,'500 mètres'],
        [1000,'1 kilomètre'],
        [5000,'5 kilomètres'],
        [10000,'10 kilomètres']
    ],*/
    
    /**
     * Constant: DEFAULT_BUFFER_VALUE
     * Default buffer value in meters.
     * Valid values are those from BUFFER_VALUES
     * Defaults to 0
     */
    //DEFAULT_BUFFER_VALUE: 0,
        
    /**
     * Constant: STARTUP_LAYERS
     * {Array} OGC layers loaded at startup if none are sent
     */
    STARTUP_LAYERS: [],
    
    /**
     * Constant: STARTUP_SERVICES
     * {Array} OGC services loaded at startup if none are sent
     */
    STARTUP_SERVICES: [
        {
            text: "PIGMA",
            owstype: "WMS",
            owsurl: "http://ns383241.ovh.net/geoserver/pigma/wms"
        }
    ],
    
    /**
     * Constant: HELP_URL
     * {String} URL of the help ressource.
     * Defaults to "/doc/html/documentation.html#extractor"
     */
    HELP_URL: "/doc/html/documentation.html#extractor"
    
    // No trailing comma for the last line (or IE will complain)
};