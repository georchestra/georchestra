/*
 * Copyright (C) Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace("GEOB");

GEOB.config = (function() {

    /**
     * Constant: URL_DEV
     * {String} The URL to the Camptocamp GeoBretagne dev host.
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
     * Constant: DEFAULT_WMC
     * {String} The path to the application's default WMC.
     */
    var DEFAULT_WMC = "default.wmc";

    /**
     * Constant: DEV_DEFAULT_WMC
     * {String} The path to the application's development WMC.
     */
    var DEV_DEFAULT_WMC = "dev-default.wmc";

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
     * Property: hostException
     * {Boolean} To cache whether the host is an exception
     * or not.
     */
    var hostException = null;

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
     * Method: isHostException
     * Return true if the client application is running on a host
     * that satisfies the "host exception" criteria, false otherwise.
     */
    var isHostException = function() {
        if (hostException === null) {
            hostException = false;
            for (var h in HOST_EXCEPTIONS) {
                if (HOST_EXCEPTIONS.hasOwnProperty(h)) {
                    var re = new RegExp(h);
                    if (getHostname().match(re)) {
                        hostException = true;
                        break;
                    }
                }
            }
        }
        return hostException;
    };

    /**
     * Method: getDefaultWMCPath
     * Get the path to the application's default WMC.
     *
     * Returns:
     * {String} The path to the default WMC.
     */
    var getDefaultWMCPath = function() {
        return isHostException() ? DEV_DEFAULT_WMC : DEFAULT_WMC;
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
        return isHostException() ? URL_DEV : "../";
    };

    /**
     * Method: getAbsoluteBaseURL
     * Get the complete (absolute) base URL of the "mapfishapp", "geonetwork"
     * and "geoserver" webapps.
     */
    var getAbsoluteBaseURL = function() {
        var url;
        if (isHostException()) {
            url = URL_DEV;
        } else {
            url = getAppURL();
            url = url.slice(0, url.indexOf("mapfishapp"));
        }
        return url;
    };

    /**
     * Method: getAppURL
     * Get the complete (absolute) URL of the "mapfishapp" webapp.
     *
     * Returns:
     * {String} The complete mapfishapp URL.
     */
    var getAppURL = function() {
        var o = getUrlObj();
        var url = o.protocol + '//' + o.host;
        if (o.port && o.port != "80") {
            url += ':' + o.port;
        }
        return url + o.pathname;
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
            vectorAbility = 9;
        } else if (Ext.isIE) {
            vectorAbility = 2;
        } else if (Ext.isOpera) {
            vectorAbility = 5;
        } else if (Ext.isSafari) {
            vectorAbility = 20;
        } else {
            // we don't want to prevent future browsers
            // from displaying a great number of features
            vectorAbility = 50;
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

    return {

        /**
         * Method: _getBaseURL
         * Test method
         */
        _getBaseURL: function(url) {
            urlObj = null;
            hostException = null;
            getUrlObj(url);
            var ret = getBaseURL();
            urlObj = null;
            hostException = null;
            return ret;
        },

        /**
         * Method: _getAbsoluteBaseURL
         * Test method
         */
        _getAbsoluteBaseURL: function(url) {
            urlObj = null;
            hostException = null;
            getUrlObj(url);
            var ret = getAbsoluteBaseURL();
            urlObj = null;
            hostException = null;
            return ret;
        },

        /**
         * Method: _getAppURL
         * Test method
         */
        _getAppURL: function(url) {
            urlObj = null;
            hostException = null;
            getUrlObj(url);
            var ret = getAppURL();
            hostException = null;
            urlObj = null;
            return ret;
        },

        /**
         * Constant: DEFAULT_WMC.
         * The path to the application's default WMC.
         */
        DEFAULT_WMC: getDefaultWMCPath(),

        /**
         * Constant: GEOSERVER_WFS_URL
         * The URL to GeoServer WFS.
         */
        GEOSERVER_WFS_URL: getBaseURL() + "geoserver/wfs",

        /**
         * Constant: GEOSERVER_WMS_URL
         * The URL to GeoServer WMS.
         */
        GEOSERVER_WMS_URL: getBaseURL() + "geoserver/wms",

        /**
         * Constant: GEOSERVER_WMS_ABSOLUTE_URL
         * The absolute URL to GeoServer WMS.
         */
        //GEOSERVER_WMS_ABSOLUTE_URL: getAbsoluteBaseURL() + "geoserver/wms",
        // seems useless now that we have GEOB.config.WMSC2WMS

        /**
         * Constant: GEOWEBCACHE_WMS_URL
         * The URL to GeoWebCache WMS.
         */
        GEOWEBCACHE_WMS_URL: getBaseURL() + "geoserver/gwc/service/wms",

        /**
         * Constant: GEONETWORK_URL
         * The URL to GeoNetwork CSW.
         */
        GEONETWORK_URL: getBaseURL() + "geonetwork/srv/fr",

        /**
         * Constant: MAPFISHAPP_URL
         * The URL to mapfishapp.
         */
        MAPFISHAPP_URL: getAppURL(),

        /**
         * Constant: LOGIN_URL
         * The login url.
         */
        LOGIN_URL: "?login",

        /**
         * Constant: LOGOUT_URL
         * The login url.
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
         * Constant: ANONYMOUS
         * Whether a user is logged in or not: can be overriden
         * dynamically in index.jsp
         */
        ANONYMOUS: true,
        
        /**
         * Constant: USERNAME
         * Username can be overriden dynamically in index.jsp
         */
        USERNAME: null,
        
        /**
         * Constant: DEFAULT_ATTRIBUTION
         * Default attribution for layers which don't have one
         */
        DEFAULT_ATTRIBUTION: 'GeoBretagne',
        
        /**
         * Constant: THESAURUS_NAME
         * Thesaurus name to display for the CSW GetDomain request.
         */
        THESAURUS_NAME: 'mots clés',
        
        /**
         * Constant: DEFAULT_THESAURUS_KEY
         * Key (as the one in the response from /geonetwork/srv/fr/xml.thesaurus.getList) 
         * of the thesaurus to use as the default (selected) one. 
         */
        DEFAULT_THESAURUS_KEY: 'local._none_.geobretagne',
        
        /**
         * Constant: ACCEPTED_MIME_TYPES
         * List of acceptable image mime types
         */
        ACCEPTED_MIME_TYPES: [
            'image/png',
            'image/gif',
            'image/jpeg',
            'image/png8',
            'image/png; mode=24bit'
        ],

        /**
         * Constant: OVMAP_LAYER_NAME
         * The name of the base layer which will be displayed in the overview map
         * Note that is must be served by the server at url GEOB.config.GEOSERVER_WMS_URL as image/png
         * see also GEOB.config.OSM_AS_OVMAP
         */
        OVMAP_LAYER_NAME: "geob_loc:DEPARTEMENT",

        /**
         * Constant: OSM_AS_OVMAP
         * Boolean: if true, use OSM mapnik as overview map baselayer instead of GEOB.config.OVMAP_LAYER_NAME
         */
        OSM_AS_OVMAP: true,
        
        /**
         * Constant: WMSC2WMS
         * Hash allowing correspondance between WMS-C server URLs and WMS server URLs
         * This assumes that layers share the same name
         * Eventually, GeoBretagne can setup a WMS server configured to consume WMS-C and serve them as WMS ...
         */
        WMSC2WMS: {
            /*
            ** Example usage: **
            "referenced_wmsc_url": undefined, // this wmsc_url has no WMS counterpart. Referencing it here allows the user to be warned upon printing.
            "wmsc_url": "wms_url"
            */
            "http://drebretagne-geobretagne.int.lsn.camptocamp.com/geoserver/gwc/service/wms": "http://drebretagne-geobretagne.int.lsn.camptocamp.com/geoserver/wms",
            "http://osm.geobretagne.fr/service/wms": "http://maps.qualitystreetmap.org/geob_wms",
            "http://geobretagne.fr/geoserver/gwc/service/wms": undefined // no trailing comma
        },
        
        /**
         * Constant: CSW_GETDOMAIN_SORTING
         * true to case insensitive sort (client side) the keywords got from a CSW getDomain request
         * false to disable client side sorting (which is preferable in case of too many keywords)
         */
        CSW_GETDOMAIN_SORTING: false
    };
})();
