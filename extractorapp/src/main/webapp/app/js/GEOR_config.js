/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace("GEOR");

GEOR.config = (function() {

    /**
     * Constant: URL_DEV
     * {String} The URL to the dev host.
     */
    var URL_DEV =
        "http://ns383241.ovh.net/";

    /**
     * Constant: HOST_EXCEPTIONS
     * {Object}
     */
    var HOST_EXCEPTIONS = {
        "localhost": URL_DEV,           // localhost
        "\\.wrk\\.cby": URL_DEV,        // c2c chambéry
        "\\.wrk\\.lsn": URL_DEV,        // c2c lausanne
        "10\\.26\\.10\\..*$": URL_DEV,  // c2c internal
        "192\\.168\\..*$": URL_DEV,     // private net
        "10\\.25\\.40\\..*$": URL_DEV   // c2c VPN
    };

    /**
     * Property: vectorAbility
     * {Number} Integer representing 
     *  browser ability to handle features
     */
    var vectorAbility = null;

    /**
     * Property: urlObj.
     * {Object} The URL object as returned by
     * OpenLayers.Util.createUrlObject().
     */
    var urlObj = null;

    /**
     * Method: getUrlObj
     * Get the URL object corresponding to the URL passed as a
     * parameter.
     *
     * Parameters:
     * url - {String}
     *
     * Returns:
     * {Object} The URL object.
     */
    var getUrlObj = function(url) {
        url = url || window.location.href;
        if (urlObj === null) {
            urlObj = OpenLayers.Util.createUrlObject(url,
                {ignorePort80:true}
            );
        }
        return urlObj;
    };

    /**
     * Method: getBrowserVectorAbility
     * Get an empirical integer parameter
     * about this browser's vector handling abilities.
     *
     * Returns:
     * {Number} The parameter
     */
    var getBrowserVectorAbility = function() {
        // TODO: these figures need to be adapted from experiments
        if (vectorAbility) {
            return vectorAbility;
        } else if (Ext.isChrome) {
            vectorAbility = 35;
        } else if (Ext.isGecko) {
            vectorAbility = 25;
        } else if (Ext.isIE) {
            vectorAbility = 5;
        } else if (Ext.isOpera) {
            vectorAbility = 10;
        } else if (Ext.isSafari) {
            vectorAbility = 35;
        } else {
            // we don't want to prevent future browsers
            // from displaying a great number of features
            vectorAbility = 100;
        }
        return vectorAbility;
    };
    
    /**
     * Method: getComputingPower
     * Get an empirical floating parameter
     * about the client's CPU power (2009 CPU = 1)
     *
     * Returns:
     * {Number} The parameter
     */
    var getComputingPower = function() {
        // not implemented for now.
        // eg: time to load app (not the files) ...
        return 1;
    };

    /**
     * Method: getHostname
     * Get the application's host name.
     *
     * Returns:
     * {String} The application's host name.
     */
    var getHostname = function() {
        return getUrlObj().host;
    };

    /**
     * Method: getBaseURL
     * Get the base URL of the "mapfishapp", "geonetwork" and "geoserver"
     * webapps.
     *
     * Returns:
     * {String} The base URL.
     */
    var getBaseURL = function() {
        var re;
        for (var h in HOST_EXCEPTIONS) {
            if (!HOST_EXCEPTIONS.hasOwnProperty(h)) {
                continue;
            }
            re = new RegExp(h);
            if (getHostname().match(re)) {
                return URL_DEV;
            }
        }
        return "../";
    };
    
    /**
     * Method: getCustomParameter
     *  If parameter paramName exists in GEOR.custom, returns its value
     *  else defaults to the mandatory defaultValue
     *
     * Parameters:
     * paramName - {String} the parameter name
     * defaultValue - {Mixed} the default value if none is 
     *                specified in GEOR.custom
     *
     * Returns:
     * {Mixed} The parameter value
     */
    var getCustomParameter = function(paramName, defaultValue) {
        return (GEOR.custom && GEOR.custom.hasOwnProperty(paramName)) ? 
            GEOR.custom[paramName] : defaultValue;
    };
    
    /*
     * Public
     */
    return {
   
        /**
         * Constant: EXTRACTOR_BATCH_URL
         * The URL to the extractor batch.
         */
        EXTRACTOR_BATCH_URL: "extractor/initiate",
   
        /**
         * Constant: LOGIN_URL
         * The login url.
         */
        LOGIN_URL: "?login",

        /**
         * Constant: LOGOUT_URL
         * The logout url.
         */
        LOGOUT_URL: "/j_spring_security_logout",
        
        /**
         * Constant: SUPPORTED_RASTER_FORMATS
         * List of supported raster formats
         */
        SUPPORTED_RASTER_FORMATS: [
            ["geotiff", "GeoTiff"],
            ["ecw", "ECW"],
            ["jp2ecw", "JPEG 2000"]
        ],

        /**
         * Constant: SUPPORTED_VECTOR_FORMATS
         * List of supported vector formats
         */
        SUPPORTED_VECTOR_FORMATS: [
            ["shp", "Shapefile"],
            ["mif", "Mif/Mid"],
            ["tab", "TAB"]
        ],
        
        /**
         * Constant: DOWNLOAD_FORM
         * Boolean: should the app display a form requesting user data and data usage ?
         * If set to yes, setting up the dlform webapp is mandatory.
         * Defaults to false
         */
        DOWNLOAD_FORM: getCustomParameter("DOWNLOAD_FORM", 
            false),

        /**
         * Constant: PDF_URL
         * String: the URL to the downloaded data Terms Of Use
         * 
         */
        PDF_URL: getCustomParameter("PDF_URL", 
            "/static/cgu.pdf"),
            
        /***** Beginning of config options which can be overriden by GEOR.custom *****/
        
   
        /**
         * Constant: GEOSERVER_WMS_URL
         * The URL to GeoServer WMS.
         */
        GEOSERVER_WMS_URL: getCustomParameter("GEOSERVER_WMS_URL", 
            getBaseURL() + "geoserver/wms"),
   
        /**
         * Constant: GEOSERVER_WFS_URL
         * The URL to GeoServer WFS.
         */
        GEOSERVER_WFS_URL: getCustomParameter("GEOSERVER_WFS_URL",
            getBaseURL() + "geoserver/wfs"),

        /**
         * Constant: MAX_FEATURES
         * The maximum number of vector features displayed.
         */
        MAX_FEATURES: getCustomParameter("MAX_FEATURES",
            50*getBrowserVectorAbility()*getComputingPower()),
        
        /**
         * Constant: MAX_LENGTH
         * The maximum number of chars in a XML response 
         * before triggering an alert.
         */
        MAX_LENGTH: getCustomParameter("MAX_LENGTH",
            400/7*1024*getBrowserVectorAbility()*getComputingPower()),

        /**
         * Constant: MAP_DOTS_PER_INCH
         * {Float} Sets the resolution used for scale computation.
         * Defaults to GeoServer defaults, which is 25.4 / 0.28
         */
        MAP_DOTS_PER_INCH: getCustomParameter("MAP_DOTS_PER_INCH",
            25.4 / 0.28),

        /**
         * Constant: GLOBAL_EPSG
         * SRS of the map used to select the global extraction parameters
         */
        GLOBAL_EPSG: getCustomParameter("GLOBAL_EPSG",
            "EPSG:4326"),

        /**
         * Constant: GLOBAL_MAX_EXTENT
         * Max extent of the global layer
         * Defaults to OpenLayers.Bounds(-180,-90,180,90)
         */
        GLOBAL_MAX_EXTENT: new OpenLayers.Bounds(
            getCustomParameter("MAP_XMIN",-180),
            getCustomParameter("MAP_YMIN",-90),
            getCustomParameter("MAP_XMAX",180),
            getCustomParameter("MAP_YMAX",90)
        ),

        /**
         * Constant: METRIC_MAP_SCALES
         * {Array} The map scales for the case where the SRS is metric.
         * Defaults to null, which means scales will be automatically computed
         */
        METRIC_MAP_SCALES: getCustomParameter("METRIC_MAP_SCALES", null),

        /**
         * Constant: GEOGRAPHIC_MAP_SCALES
         * {Array} The map scales for the case where the SRS is based on angles.
         * Defaults to null, which means scales will be automatically computed
         */
        GEOGRAPHIC_MAP_SCALES: getCustomParameter("GEOGRAPHIC_MAP_SCALES", null),
        
        /**
         * Constant: MAP_POS_SRS1
         * {String} The cursor position will be displayed using this SRS.
         * Set to "" if you do not want to have mouse position displayed.
         * Defaults to "EPSG:2154"
         */
        MAP_POS_SRS1: getCustomParameter("MAP_POS_SRS1", "EPSG:2154"),
        
        /**
         * Constant: MAP_POS_SRS2
         * {String} The cursor position will be displayed using this SRS.
         * Set to "" if you do not want to have mouse position displayed.
         * Defaults to ""
         */
        MAP_POS_SRS2: getCustomParameter("MAP_POS_SRS2", ""),
        
        /**
         * Constant: BASE_LAYER_NAME
         * The WMS base layer which will be displayed under each extracted layer.
         */
        BASE_LAYER_NAME: getCustomParameter("BASE_LAYER_NAME",
            "geor:countries"),
        
        /**
         * Constant: NS_LOC
         * {String} The referentials layers' namespace alias as defined in
         *    the GeoServer configuration.
         * Defaults to "geor_loc"
         */
        NS_LOC: getCustomParameter("NS_LOC", "geor_loc"),

        /**
         * Constant: DEFAULT_WCS_EXTRACTION_WIDTH
         * Default width of the extracted image from WCS. This constant
         * is to be used to calculate the default resolution of WCS.
         *
         * FIXME: not sure it is really useful.
         *
         */
        DEFAULT_WCS_EXTRACTION_WIDTH: getCustomParameter("DEFAULT_WCS_EXTRACTION_WIDTH",
            1024),

        /**
         * Constant: SUPPORTED_REPROJECTIONS
         * List of projections that extractor supports for reprojection
         */
        SUPPORTED_REPROJECTIONS: getCustomParameter("SUPPORTED_REPROJECTIONS", [
            ["EPSG:27562", "EPSG:27562 - Lambert II carto"], 
            ["EPSG:27572", "EPSG:27572 - Lambert II étendu"],
            ["EPSG:2154", "EPSG:2154 - Lambert 93"],
            ["EPSG:4171", "EPSG:4171 - RGF93"],
            ["EPSG:4326", "EPSG:4326 - WGS84"]
        ]),

        /**
         * Constant: EXTRACT_BTN_DISABLE_TIME
         * Duration in seconds for the extract button being disabled after an extraction
         */
        EXTRACT_BTN_DISABLE_TIME: getCustomParameter("EXTRACT_BTN_DISABLE_TIME",
            30),

        /**
         * Constant: LAYERS_CHECKED
         * Layers checked by default or not ?
         */
        LAYERS_CHECKED: getCustomParameter("LAYERS_CHECKED",
            true),
        
        /**
         * Constant: BUFFER_VALUES
         * {Array} Array of buffer values with their display name
         */
        BUFFER_VALUES: getCustomParameter("BUFFER_VALUES", [
            [0,'aucun'],
            [10,'10 mètres'],
            [50,'50 mètres'],
            [100,'100 mètres'],
            [500,'500 mètres'],
            [1000,'1 kilomètre'],
            [5000,'5 kilomètres'],
            [10000,'10 kilomètres']
        ]),
        
        /**
         * Constant: DEFAULT_BUFFER_VALUE
         * Default buffer value in meters.
         * Valid values are those from BUFFER_VALUES
         */
        DEFAULT_BUFFER_VALUE: getCustomParameter("DEFAULT_BUFFER_VALUE",
            0),
        
        /**
         * Constant: STARTUP_LAYERS
         * {Array} OGC layers loaded at startup if none are sent
         */
        STARTUP_LAYERS: getCustomParameter("STARTUP_LAYERS", [
            {
                layername: "ortho",
                owstype: "WMS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            }, {
                layername: "fake layer",
                owstype: "WFS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            },{
                layername: "voies",
                owstype: "WFS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            },{
                owstype: "WMS",
                owsurl: "http://geolittoral.application.equipement.gouv.fr/wms/metropole",
                layername: "Sentiers_littoraux"
            }, {
                owstype: "WMS",
                owsurl: "http://sd1878-2.sivit.org/geoserver/wms",
                layername: "topp:RCLC90_L2E"
            }, {
                owstype: "WMS",
                owsurl: "http://geoservices.brgm.fr/risques",
                layername: "BASIAS_LOCALISE"
            }
        ]),
        
        /**
         * Constant: STARTUP_SERVICES
         * {Array} OGC services loaded at startup if none are sent
         */
        STARTUP_SERVICES: getCustomParameter("STARTUP_SERVICES", [
            {
                text: "BRGM Risques",
                owstype: "WMS",
                owsurl: "http://geoservices.brgm.fr/risques"
            }, {
                text: "Gest'eau",
                owstype: "WMS",
                owsurl: "http://gesteau.oieau.fr/service"
            },{
                text: "Sivit",
                owstype: "WMS",
                owsurl: "http://sd1878-2.sivit.org/geoserver/wms"
            },{
                text: "GeoLittoral",
                owstype: "WMS",
                owsurl: "http://geolittoral.application.equipement.gouv.fr/wms/metropole"
            },{
                text: "BMO/OSM",
                owstype: "WMS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            },{
                text: "BMO/OSM",
                owstype: "WFS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            }, {
                text: "BMO/OSM fake",
                owstype: "WMS",
                owsurl: "http://bmo.openstreetmap.fr/ows2"
            }
        ]),

        /**
         * Constant: SPLASH_SCREEN
         * {String} The message to display on extractorapp startup
         * Defaults to null, which means no message will be displayed
         */
        SPLASH_SCREEN: getCustomParameter("SPLASH_SCREEN", 
            null),

        /**
         * Constant: HELP_URL
         * {String} URL of the help ressource.
         * Defaults to "/doc/html/documentation.html#extractor"
         */
        HELP_URL: getCustomParameter("HELP_URL",
            "/doc/html/documentation.html#extractor")
        // No trailing comma for the last line (or IE will complain)
    };
})();
