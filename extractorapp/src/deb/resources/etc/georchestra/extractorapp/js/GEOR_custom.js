/**
 * Sample geOrchestra extractor config file
 *
 * Instructions: copy this buffer into GEOR_custom.js,	
 * uncomment lines you wish to modify and 
 * modify the corresponding values to suit your needs.
 */
	
Ext.namespace("GEOR");

	
GEOR.custom = {

    /**
     * Constant: HEADER_HEIGHT
     * Integer value representing the header height, as set in the shared maven filters
     * Defaults to 90
     */
    HEADER_HEIGHT: 90,

    /**
     * Constant: DOWNLOAD_FORM
     * Boolean: should the app display a form requesting user data and data usage ?
     * Defaults to true (see shared.download_form.activated var in shared.maven.filters file)
     */
    DOWNLOAD_FORM: false,

    /**
     * Constant: PDF_URL
     * String: the URL to the downloaded data Terms Of Use
     * Defaults to /header/cgu.pdf (see shared.download_form.pdf_url var in shared.maven.filters file)
     */
    PDF_URL: "//header/cgu.pdf",

    /***** Beginning of config options which can be set in this file *****/

    /**
     * Constant: SUPPORTED_RASTER_FORMATS
     * List of supported raster formats.
     * Defaults to GeoTiff & Tiff
     *
    SUPPORTED_RASTER_FORMATS: [
        ["geotiff", "GeoTiff"],
        ["tiff", "Tif + TFW"]
    ],*/

    /**
     * Constant: SUPPORTED_VECTOR_FORMATS
     * List of supported vector formats.
     * Defaults to SHP, MIF/MID, TAB, KML
     *
    SUPPORTED_VECTOR_FORMATS: [
        ["shp", "Shapefile"],
        ["mif", "Mif/Mid"],
        ["tab", "TAB"],
        ["kml", "KML"]
    ],*/

    /**
     * Constant: SUPPORTED_RESOLUTIONS
     * List of supported resolutions.
     * Defaults to 0.2 0.5 1 2 5 10 meters
     *
    SUPPORTED_RESOLUTIONS: [
        ["0.2", "0.2"],
        ["0.5", "0.5"],
        ["1", "1"],
        ["2", "2"],
        ["5", "5"],
        ["10", "10"]
    ],*/

    /**
     * Constant: DEFAULT_RESOLUTION
     * Defaults to 10 meters
     * Please read https://github.com/georchestra/georchestra/issues/726
     *
    DEFAULT_RESOLUTION: 10,*/

    /**
     * Constant: GEOSERVER_WMS_URL
     * The URL to GeoServer WMS.
     */
    GEOSERVER_WMS_URL: "//geoserver/wms",

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
     * Defaults to 1000 / 39.37 / 0.28
     * see https://github.com/georchestra/georchestra/issues/736
     */
    //MAP_DOTS_PER_INCH: 1000 / 39.37 / 0.28,

    /**
     * Constant: GLOBAL_EPSG
     * SRS of the map used to select the global extraction parameters
     */
    //GLOBAL_EPSG: "EPSG:4326",

    /**
     * Constant: MAP_XMIN aka "left"
     * {Float} The max extent xmin in GLOBAL_EPSG coordinates.
     * Defaults to -180
     */
    //MAP_XMIN: -180,

    /**
     * Constant: MAP_YMIN aka "bottom"
     * {Float} The max extent ymin in GLOBAL_EPSG coordinates.
     * Defaults to -90
     */
    //MAP_YMIN: -90,

    /**
     * Constant: MAP_XMAX aka "right"
     * {Float} The max extent xmax in GLOBAL_EPSG coordinates.
     * Defaults to 180
     */
    //MAP_XMAX: 180,

    /**
     * Constant: MAP_YSMAX aka "top"
     * {Float} The max extent ymax in GLOBAL_EPSG coordinates
     * Defaults to 90
     */
    //MAP_YMAX: 90,

    /**
     * Constant: BASE_LAYER_NAME
     * The WMS base layer which will be displayed under each extracted layer.
     * Defaults to "geor:countries"
     */
    BASE_LAYER_NAME: "geor:countries",

    /**
     * Constant: NS_LOC
     * {String} The referentials layers' namespace alias as defined in
     *    the GeoServer configuration.
     * Defaults to "geor_loc"
     */
    //NS_LOC: "geor_loc",

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
    /*SUPPORTED_REPROJECTIONS: [
        ["EPSG:4326", "EPSG:4326 - WGS84"],
        ["EPSG:3857", "Spherical Mercator"]
    ],*/

    /**
     * Constant: METRIC_MAP_SCALES
     * {Array} The map scales for the case where the SRS is metric.
     * Defaults to null, which means scales will be automatically computed
     *
    METRIC_MAP_SCALES: [
        266.5911979812228585,
        533.1823959624461134,
        1066.3647919248918304,
        2132.7295838497840572,
        4265.4591676995681144,
        8530.9183353991362289,
        17061.8366707982724577,
        34123.6733415965449154,
        68247.3466831930771477,
        136494.6933663861796617,
        272989.3867327723085907,
        545978.7734655447186469,
        1091957.5469310886252288,
        2183915.0938621788745877,
        4367830.1877243577491754,
        8735660.3754487154983508,
        17471320.7508974309967016,
        34942641.5017948619934032,
        69885283.0035897239868063,
        139770566.0071793960087234,
        279541132.0143588959472254,
        559082264.0287178958533332
    ],*/

    /**
     * Constant: GEOGRAPHIC_MAP_SCALES
     * {Array} The map scales for the case where the SRS is based on angles.
     * Defaults to null, which means scales will be automatically computed
     */
    //GEOGRAPHIC_MAP_SCALES: null,
        
    /**
     * Constant: MAP_POS_SRS1
     * {String} The cursor position will be displayed using this SRS.
     * Defaults to "EPSG:4326"
     */
    //MAP_POS_SRS1: "EPSG:4326",
    
    /**
     * Constant: MAP_POS_SRS2
     * {String} The cursor position will be displayed using this SRS.
     * Defaults to ""
     */
    //MAP_POS_SRS2: "",

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
        [0, "None"],
        [10, "BUFFER meters"],
        [50, "BUFFER meters"],
        [100, "BUFFER meters"],
        [500, "BUFFER meters"],
        [1000, "BUFFER kilometer"],
        [5000, "BUFFER kilometers"],
        [10000, "BUFFER kilometers"]
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
    STARTUP_LAYERS: [
        {
            owstype: "WMS",
            owsurl: "http://sdi.georchestra.org/geoserver/wms",
            layername: "gshhs:GSHHS_l_L2"
        }
    ],
    
    /**
     * Constant: STARTUP_SERVICES
     * {Array} OGC services loaded at startup if none are sent
     */
    STARTUP_SERVICES: [
        {
            text: "Example layers",
            owstype: "WMS",
            owsurl: "http://sdi.georchestra.org/geoserver/wms"
        }
    ]

    /**
     * Constant: SPLASH_SCREEN
     * {String} The message to display on extractorapp startup
     * Defaults to null, which means no message will be displayed
     *
    ,SPLASH_SCREEN: [
        "Afin d'utiliser au mieux la fonctionnalité d'extraction en ligne, nous vous ",
        "invitons à respecter les conseils suivants : ",
        "<br/><br/>",
        "Pour les données <b>image</b> : ",
        "La taille maximale pour une extraction de l'orthophotographie à 50cm est d'environ 9 Km². ",
        "Au-delà, l'extraction risque de ne pas aboutir. ",
        "<br/>",
        "Le format ECW est limité à des fichiers de 500 Mo maximum. ",
        "Privilégiez plutôt les formats JPEG 2000 ou TIF. ",
        "<br/><br/>",
        "Pour les données <b>vecteur</b> : ",
        "Les couches comportant un trop grand nombre d'objets (~ million) ",
        "ne pourront pas être extraites. ",
        "<br/><br/>",
        "Si vous ne parvenez pas à extraire une couche à l'aide de l'extracteur en ",
        "ligne, <a href=\"mailto:psc@georchestra.org\">prenez contact</a> avec l'administrateur."].join(""),
    */

    /**
     * Constant: HELP_URL
     * {String} URL of the help ressource.
     * Defaults to "http://cms.geobretagne.fr/assistance"
     *
    ,HELP_URL: "http://cms.geobretagne.fr/assistance"
    */

    // No trailing comma for the last line (or IE will complain)
};
