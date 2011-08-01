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
     * {String} The URL to the Camptocamp geOrchestra dev host.
     */
    var URL_DEV =
        "http://c2cpc83.camptocamp.com/";

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

    /*
     * Public
     */
    return {
   
        /**
         * Constant: GEOSERVER_WMS_URL
         * The URL to GeoServer WMS.
         */
        GEOSERVER_WMS_URL: getBaseURL() + "geoserver/wms",
   
        /**
         * Constant: GEOSERVER_WFS_URL
         * The URL to GeoServer WFS.
         */
        GEOSERVER_WFS_URL: getBaseURL() + "geoserver/wfs",
   
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
         * Constant: MAX_FEATURES
         * The maximum number of vector features displayed.
         */
        MAX_FEATURES: 50*getBrowserVectorAbility()*getComputingPower(),
        
        /**
         * Constant: MAX_LENGTH
         * The maximum number of chars in a XML response 
         * before triggering an alert.
         */
        MAX_LENGTH: 400/7*1024*getBrowserVectorAbility()*getComputingPower(),

        /**
         * Constant: GLOBAL_EPSG
         * SRS of the map used to select the global extraction parameters
         */
        GLOBAL_EPSG: "EPSG:4326",

        /**
         * Constant: GLOBAL_MAX_EXTENT
         * Max extent of the global layer
         */
//        GLOBAL_MAX_EXTENT: new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),
        GLOBAL_MAX_EXTENT: new OpenLayers.Bounds(-180,-90,180,90),
        
        /**
         * Constant: BASE_LAYER_NAME
         * The WMS base layer which will be displayed under each extracted layer.
         */
        BASE_LAYER_NAME: "geob_pub:countries",

        /**
         * Constant: DEFAULT_WCS_EXTRACTION_WIDTH
         * Default width of the extracted image from WCS. This constant
         * is to be used to calculate the default resolution of WCS.
         */
        DEFAULT_WCS_EXTRACTION_WIDTH: 1024,

        /**
         * Constant: SUPPORTED_REPROJECTIONS
         * List of projections that extractor supports for reprojection
         */
        SUPPORTED_REPROJECTIONS: [
            ["EPSG:27562", "EPSG:27562 - Lambert II carto"], 
            ["EPSG:27572", "EPSG:27572 - Lambert II étendu"],
            ["EPSG:2154", "EPSG:2154 - Lambert 93"],
            ["EPSG:3948", "EPSG:3948 - Lambert-93 CC48"],
            ["EPSG:4171", "EPSG:4171 - RGF93"],
            ["EPSG:4326", "EPSG:4326 - WGS84"]
        ],

        /**
         * Constant: SUPPORTED_RASTER_FORMATS
         * List of supported raster formats
         */
        SUPPORTED_RASTER_FORMATS: [
            ["geotiff", "GeoTiff"]
        ],

        /**
         * Constant: SUPPORTED_VECTOR_FORMATS
         * List of supported vector formats
         */
        SUPPORTED_VECTOR_FORMATS: [
            ["shp", "Shapefile"],
            ["mif", "Mif/Mid"]
        ],

        /**
         * Constant: EXTRACT_BTN_DISABLE_TIME
         * Duration in seconds for the extract button being disabled after an extraction
         */
        EXTRACT_BTN_DISABLE_TIME: 30,

        /**
         * Constant: LAYERS_CHECKED
         * Layers checked by default or not ?
         */
        LAYERS_CHECKED: true,
        
        /**
         * Constant: DEFAULT_BUFFER_VALUE
         * Default buffer value in meters.
         * Valid values are : 0, 10, 50, 100, 500, 1000, 5000, 10000
         */
        DEFAULT_BUFFER_VALUE: 0
    };
})();
