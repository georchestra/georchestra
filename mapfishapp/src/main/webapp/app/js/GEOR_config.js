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
     * {String} The URL to the OWS dev host.
     */
    var URL_DEV =
        "http://ns383241.ovh.net/";
    // FIXME: this should not be here...

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
            urlObj = OpenLayers.Util.createUrlObject(url, {
                ignorePort80:true
            });
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
            vectorAbility = 50;
        } else if (Ext.isGecko) {
            vectorAbility = 30;
        } else if (Ext.isIE) {
            vectorAbility = 10;
        } else if (Ext.isOpera) {
            vectorAbility = 25;
        } else if (Ext.isSafari) {
            vectorAbility = 50;
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

        /***** Beginning of config options which can be overriden by GEOR.custom *****/

        /**
         * Constant: DEFAULT_WMC
         * The path to the application's default WMC.
         * Defaults to "default.wmc"
         */
        DEFAULT_WMC: getCustomParameter("DEFAULT_WMC", "default.wmc"),

        /**
         * Constant: DEFAULT_PRINT_FORMAT
         * {String} The default (ie selected) print layout format.
         * Defaults to "A4 paysage"
         */
        DEFAULT_PRINT_FORMAT: getCustomParameter("DEFAULT_PRINT_FORMAT",
            "A4 paysage"),

        /**
         * Constant: DEFAULT_PRINT_FORMAT
         * {String} The default (ie selected) print resolution.
         * Defaults to "127"
         */
        DEFAULT_PRINT_RESOLUTION: getCustomParameter("DEFAULT_PRINT_RESOLUTION",
            "127"),

        /**
         * Constant: PDF_FILENAME
         * {String} The PDF filename prefix.
         * Defaults to "georchestra_${yyyy-MM-dd_hhmmss}"
         */
        PDF_FILENAME: getCustomParameter("PDF_FILENAME",
            "georchestra_${yyyy-MM-dd_hhmmss}"),

        /**
         * Constant: GEOSERVER_WFS_URL
         * The URL to GeoServer WFS.
         * This is required if and only if the edit application is used
         * or if the "referentials" module is activated.
         * Defaults to /geoserver/wfs
         */
        GEOSERVER_WFS_URL: getCustomParameter("GEOSERVER_WFS_URL",
            getBaseURL() + "geoserver/wfs"),

        /**
         * Constant: GEOSERVER_WMS_URL
         * The URL to the GeoServer WMS.
         * This is required if and only if OSM_AS_OVMAP is set to false.
         * Defaults to /geoserver/wms
         */
        GEOSERVER_WMS_URL: getCustomParameter("GEOSERVER_WMS_URL",
            getBaseURL() + "geoserver/wms"),

        /**
         * Constant: GEONETWORK_URL
         * The URL to the GeoNetwork server.
         * Defaults to "/geonetwork/srv/fr"
         */
        GEONETWORK_URL: getCustomParameter("GEONETWORK_URL",
            getBaseURL() + "geonetwork/srv/fr"),

        /**
         * Constant: CSW_GETDOMAIN_SORTING
         * true to case insensitive sort (client side) the keywords
         * got from a CSW getDomain request. false to disable
         * client side sorting
         * (which is preferable in case of too many keywords).
         * Defaults to false
         */
        CSW_GETDOMAIN_SORTING: getCustomParameter("CSW_GETDOMAIN_SORTING",
            false),

        /**
         * Constant: THESAURUS_NAME
         * Thesaurus name to display for the CSW GetDomain request.
         * Defaults to 'mots clés du catalogue'
         */
        THESAURUS_NAME: getCustomParameter("THESAURUS_NAME", 'mots clés du catalogue'),

        /**
         * Constant: CATALOGS
         * List of catalogs for freetext search
         */
        CATALOGS: getCustomParameter("CATALOGS", [
            ['http://geobretagne.fr/geonetwork/srv/fr/csw', 'le catalogue GeoBretagne'],
            ['http://ids.pigma.org/geonetwork/srv/fr/csw', 'le catalogue PIGMA'],
            ['http://sandre.eaufrance.fr/geonetwork_CSW/srv/fr/csw', 'le catalogue du Sandre']
        ]),

        /**
         * Constant: DEFAULT_CSW_URL
         * CSW URL which should be used by default for freetext search
         * Note: must be one of the URLs in the above CATALOGS config option
         */
        DEFAULT_CSW_URL: getCustomParameter("DEFAULT_CSW_URL",
            'http://geobretagne.fr/geonetwork/srv/fr/csw'),

        /**
         * Constant: MAX_CSW_RECORDS
         * The maximum number of CSW records queried for catalog search
         * Note: if you set this to a low value, you run the risk of not having
         * enough results (even 0). On the contrary, setting a very high value
         * might result in browser hanging (too much XML data to parse).
         * Defaults to 20.
         */
        MAX_CSW_RECORDS: getCustomParameter("MAX_CSW_RECORDS", 20),

        /**
         * Constant: NO_THUMBNAIL_IMAGE_URL
         * URL to a thumbnail image shown when none is provided by the CSW service
         * Defaults to the provided one ('app/img/nopreview.png')
         */
        NO_THUMBNAIL_IMAGE_URL: getCustomParameter("NO_THUMBNAIL_IMAGE_URL",
            'app/img/nopreview.png'),

        /**
         * Constant: DEFAULT_THESAURUS_KEY
         * Key (as the one in the response from /geonetwork/srv/fr/xml.thesaurus.getList)
         * of the thesaurus to use as the default (selected) one.
         * Defaults to 'local._none_.geobretagne' FIXME: should be something else
         */
        DEFAULT_THESAURUS_KEY: getCustomParameter("DEFAULT_THESAURUS_KEY",
            'local._none_.geobretagne'),

        /**
         * Constant: MAX_FEATURES
         * The maximum number of vector features displayed.
         * Defaults to a value estimated by an empirical formula
         */
        MAX_FEATURES: getCustomParameter("MAX_FEATURES",
            50*getBrowserVectorAbility()*getComputingPower()),

        /**
         * Constant: MAX_LENGTH
         * The maximum number of chars in a XML response
         * before triggering an alert.
         * Defaults to a value estimated by an empirical formula
         */
        MAX_LENGTH: getCustomParameter("MAX_LENGTH",
            400/7*1024*getBrowserVectorAbility()*getComputingPower()),


        /**
         * Constant: DEFAULT_ATTRIBUTION
         * Default attribution for layers which don't have one.
         * Defaults to ''
         */
        DEFAULT_ATTRIBUTION: getCustomParameter("DEFAULT_ATTRIBUTION", ''),

        /**
         * Constant: OSM_AS_OVMAP
         * Boolean: if true, use OSM mapnik as overview map baselayer
         * instead of GEOR.config.OVMAP_LAYER_NAME.
         * Defaults to true
         */
        OSM_AS_OVMAP: getCustomParameter("OSM_AS_OVMAP", true),

        /**
         * Constant: OVMAP_LAYER_NAME
         * The name of the base layer which will be displayed in the overview map.
         * This is required if and only if OSM_AS_OVMAP is set to false.
         * This layer must be served by the server GEOSERVER_WMS_URL as image/png
         * Defaults to "geor_loc:DEPARTEMENTS"
         */
        OVMAP_LAYER_NAME: getCustomParameter("OVMAP_LAYER_NAME",
            "geor_loc:DEPARTEMENTS"),

        /**
         * Constant: WMSC2WMS
         * Hash allowing correspondance between WMS-C server URLs and WMS server URLs for print
         * This assumes that layers share the same name on both servers
         * Eventually, Administrator can setup a mirror WMS server configured to consume WMS-C and serve them as WMS ...
         */
        WMSC2WMS: getCustomParameter("WMSC2WMS", {
            /**
             * Example usage:
             *
             * "wmsc_url": "wms_url",
             *
             * For a WMSC with no WMS counterpart,
             * referencing the wmsc_url here allows the user
             * to be warned that this layer will not be printed:
             *
             * "wmsc_url": undefined,
             */
            "http://osm.geobretagne.fr/service/wms":
                "http://maps.qualitystreetmap.org/geob_wms",
            "http://geobretagne.fr/geoserver/gwc/service/wms":
                undefined // no trailing comma
        }),


        /**
         * Constant: MAP_DOTS_PER_INCH
         * {Float} Sets the resolution used for scale computation.
         * Defaults to GeoServer defaults, which is 25.4 / 0.28
         */
        MAP_DOTS_PER_INCH: getCustomParameter("MAP_DOTS_PER_INCH",
            25.4 / 0.28),


        /**
         * Constant: RECENTER_ON_ADDRESSES
         * {Boolean} whether to display the recenter on addresses tab.
         * Defaults to false
         */
        RECENTER_ON_ADDRESSES: getCustomParameter("RECENTER_ON_ADDRESSES",
            false),

        /**
         * Constant: ADDRESS_URL
         * {String} The URL to the OpenAddresses web service.
         * Required if and only if RECENTER_ON_ADDRESSES is set to true.
         * Defaults to "/addrapp/addresses"
         */
        ADDRESS_URL: getCustomParameter("ADDRESS_URL",
            "/addrapp/addresses"),

        /**
         * Constant: NS_LOC
         * {String} The referentials layers' namespace alias as defined in
         *    the GeoServer configuration.
         * Defaults to "geor_loc"
         */
        NS_LOC: getCustomParameter("NS_LOC", "geor_loc"),


        /**
         * Constant: NS_EDIT
         * {String} The editing layers' namespace alias as defined in
         *    the GeoServer configuration.
         * Defaults to "geor_edit"
         */
        NS_EDIT: getCustomParameter("NS_EDIT", "geor_edit"),


        /**
         * Constant: CSW_GETDOMAIN_PROPERTY
         * {String} the property used to query the CSW for keywords.
         * Defaults to "subject"
         */
        CSW_GETDOMAIN_PROPERTY: getCustomParameter("CSW_GETDOMAIN_PROPERTY",
            "subject"),


        /**
         * Constant: MAP_SCALES
         * {Array} The map's scales.
         * Defaults to GeoBretagne GWC compliant scales
         */
        MAP_SCALES : getCustomParameter("MAP_SCALES", [
            266.591197934,
            533.182395867,
            1066.364791734,
            2132.729583468,
            4265.459166936,
            8530.918333871,
            17061.836667742,
            34123.673335484,
            68247.346670968,
            136494.693341936,
            272989.386683873,
            545978.773367746,
            1091957.546735491,
            2183915.093470982,
            4367830.186941965,
            8735660.373883929
        ]),

        /**
         * Constant: MAP_SRS
         * {String} The default map SRS code.
         * Defaults to EPSG:2154
         */
        MAP_SRS: getCustomParameter("MAP_SRS", "EPSG:2154"),

        /**
         * Constant: MAP_XMIN aka "left"
         * {Float} The max extent xmin in MAP_SRS coordinates.
         * Defaults to -357823.2365 (EPSG:2154 left)
         */
        MAP_XMIN: getCustomParameter("MAP_XMIN", -357823.2365),

        /**
         * Constant: MAP_YMIN aka "bottom"
         * {Float} The max extent ymin in MAP_SRS coordinates.
         * Defaults to 6037008.6939 (EPSG:2154 bottom)
         */
        MAP_YMIN: getCustomParameter("MAP_YMIN", 6037008.6939),

        /**
         * Constant: MAP_XMAX aka "right"
         * {Float} The max extent xmax in MAP_SRS coordinates.
         * Defaults to 1313632.3628 (EPSG:2154 right)
         */
        MAP_XMAX: getCustomParameter("MAP_XMAX", 1313632.3628),

        /**
         * Constant: MAP_YSMAX aka "top"
         * {Float} The max extent ymax in MAP_SRS coordinates
         * Defaults to 7230727.3772 (EPSG:2154 top)
         */
        MAP_YMAX: getCustomParameter("MAP_YMAX", 7230727.3772),

        /**
         * Constant: MAP_POS_SRS1
         * {String} The cursor position will be displayed using this SRS.
         * Set to "" if you do not want to have mouse position displayed.
         * Defaults to "EPSG:2154"
         */
        //MAP_POS_SRS1: getCustomParameter("MAP_POS_SRS1", "EPSG:2154"),
        
        POINTER_POSITION_SRS_LIST: [
            ["EPSG:2154", "Lambert 93"],
            ["EPSG:4326", "WGS 84"]
        ],

        /**
         * Constant: PROJ4JS_STRINGS
         * {Object} The list of supported SRS with their definitions.
         * Defaults to "EPSG:2154" & "EPSG:900913" being defined
         * Note that "EPSG:900913" is required if OSM_AS_OVMAP is set to true
         */
        PROJ4JS_STRINGS: getCustomParameter("PROJ4JS_STRINGS", {
            "EPSG:2154": "+title=RGF-93/Lambert 93, +proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
            "EPSG:900913": "+title=Web Spherical Mercator, +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs"
        }),

        /**
         * Constant: TILE_SINGLE
         * {Boolean} When false, activates WMS tiled requests.
         * Defaults to false
         */
        TILE_SINGLE: getCustomParameter("TILE_SINGLE", false),

        /**
         * Constant: TILE_WIDTH
         * {Integer} Width of the WMS tiles in pixels.
         * Defaults to 512
         */
        TILE_WIDTH: getCustomParameter("TILE_WIDTH", 512),

        /**
         * Constant: TILE_HEIGHT
         * {Integer} Height of the WMS tiles in pixels.
         * Defaults to 512
         */
        TILE_HEIGHT: getCustomParameter("TILE_HEIGHT", 512),

        /**
         * Constant: GEONAMES_FILTERS
         * {Object} Describes the geonames options.
         * Defaults to France/Bretagne/populated places
         */
        GEONAMES_FILTERS: getCustomParameter("GEONAMES_FILTERS", {
            country: 'FR',         // France
            adminCode1: 'A2',      // Bretagne
            style: 'short',        // verbosity of results
            lang: 'fr',
            featureClass: 'P',     // class category: populated places
            maxRows: 20            // maximal number of results
        }),

        /**
         * Constant: GEONAMES_ZOOMLEVEL
         * {Integer} The number of zoom levels from maximum zoom level
         *           to zoom to, when using GeoNames recentering
         * Should always be >= 1.
         * Defaults to 5
         */
        GEONAMES_ZOOMLEVEL: getCustomParameter("GEONAMES_ZOOMLEVEL", 5),

        /**
         * Constant: ANIMATE_WINDOWS
         * {Boolean} Display animations on windows opening/closing
         * Defaults to true
         */
        ANIMATE_WINDOWS: getCustomParameter("ANIMATE_WINDOWS", true),

        /**
         * Constant: DISPLAY_VISIBILITY_RANGE
         * {Boolean} Display the layer visibility range in layer tree
         * Defaults to true
         */
        DISPLAY_VISIBILITY_RANGE: getCustomParameter("DISPLAY_VISIBILITY_RANGE", true),

        /**
         * Constant: ROLES_FOR_STYLER
         * {Array} roles required for the styler to show up
         * Empty array means the module is available for everyone
         * Defaults to ['ROLE_SV_USER', 'ROLE_SV_REVIEWER', 'ROLE_SV_EDITOR', 'ROLE_SV_ADMIN']
         */
        ROLES_FOR_STYLER: getCustomParameter("ROLES_FOR_STYLER",
            ['ROLE_SV_USER', 'ROLE_SV_REVIEWER', 'ROLE_SV_EDITOR', 'ROLE_SV_ADMIN']),

        /**
         * Constant: ROLES_FOR_QUERIER
         * {Array} roles required for the querier to show up
         * Empty array means the module is available for everyone
         * Defaults to []
         */
        ROLES_FOR_QUERIER: getCustomParameter("ROLES_FOR_QUERIER",
            []),

        /**
         * Constant: ROLES_FOR_PRINTER
         * {Array} roles required to be able to print
         * Empty array means printing is available for everyone
         * Defaults to []
         */
        ROLES_FOR_PRINTER: getCustomParameter("ROLES_FOR_PRINTER",
            []),

        /**
         * Constant: HELP_URL
         * {String} URL of the help ressource.
         * Defaults to "http://www.geobretagne.fr/web/guest/assistance"
         */
        HELP_URL: getCustomParameter("HELP_URL",
            "http://www.geobretagne.fr/web/guest/assistance"),

        /**
         * Constant: DISPLAY_SELECTED_OWS_URL
         * {Boolean} - If set to false, do not display the selected WMS/WFS server URL
         * in the second field from the "Add layers" popup window.
         * (pretty much useless, I know...)
         * Defaults to true.
         */
        DISPLAY_SELECTED_OWS_URL: getCustomParameter("DISPLAY_SELECTED_OWS_URL",
            true),

        /**
         * Constant: CONFIRM_LAYER_REMOVAL
         * {Boolean} Do we want a popup dialog to appear on layer removal ?
         * Defaults to false
         */
        CONFIRM_LAYER_REMOVAL: getCustomParameter("CONFIRM_LAYER_REMOVAL",
            false),

        /**
         * Constant: WMS_SERVERS
         * {Array} List of externals WMS to display in the WMS servers tab.
         */
        WMS_SERVERS: getCustomParameter("WMS_SERVERS", [
            {"name": "GeoBretagne", "url": "http://geobretagne.fr/geoserver/wms"},
            {"name": "Région Bretagne", "url": "http://kartenn.region-bretagne.fr/geoserver/wms"},
            {"name": "Sandre/zonages", "url": "http://services.sandre.eaufrance.fr/geo/zonage"},
            {"name": "Sandre/ouvrages", "url": "http://services.sandre.eaufrance.fr/geo/ouvrage"},
            {"name": "Sandre/stations", "url": "http://services.sandre.eaufrance.fr/geo/stations"},
            {"name": "BRGM/géologie", "url": "http://geoservices.brgm.fr/geologie"},
            {"name": "BRGM/risques", "url": "http://geoservices.brgm.fr/risques"},
            {"name": "Cartorisque35, risques naturels", "url": "http://cartorisque.prim.net/wms/35"},
            {"name": "Cartorisque22, risques naturels", "url": "http://cartorisque.prim.net/wms/22"},
            {"name": "Cartorisque29, risques naturels", "url": "http://cartorisque.prim.net/wms/29"},
            {"name": "Cartorisque56, risques naturels", "url": "http://cartorisque.prim.net/wms/56"},
            {"name": "Carmen", "url": "http://ws.carmen.application.developpement-durable.gouv.fr/WFS/10/Nature_Paysage"},
            {"name": "GeoSignal", "url": "http://www.geosignal.org/cgi-bin/wmsmap"},
            {"name": "Corine Land Cover", "url": "http://sd1878-2.sivit.org/geoserver/wms"},
            {"name": "GeoLittoral", "url": "http://geolittoral.application.equipement.gouv.fr/wms/metropole"},
            {"name": "Gest'Eau", "url": "http://gesteau.oieau.fr/service"},
            {"name": "BMO/OpenStreetMap", "url": "http://bmo.openstreetmap.fr/wms"},
            {"name": "IFREMER/littoral", "url": "http://www.ifremer.fr/services/wms1"},
            {"name": "Cartelie/CETE Ouest", "url": "http://mapserveur.application.developpement-durable.gouv.fr/map/mapserv?map%3D%2Fopt%2Fdata%2Fcarto%2Fcartelie%2Fprod%2FCETE_Ouest%2Fxdtyr36laj.www.map"}
        ]),

        /**
         * Constant: WFS_SERVERS
         * {Array} List of externals WFS to display in the WFS servers tab.
         */
        WFS_SERVERS: getCustomParameter("WFS_SERVERS", [
            {"name": "GeoBretagne", "url": "http://geobretagne.fr/geoserver/wfs"},
            {"name": "BMO/OpenStreetMap", "url": "http://bmo.openstreetmap.fr/ows"},
            {"name": "Corine Land Cover", "url": "http://sd1878-2.sivit.org/geoserver/wfs"}
        ])
    // No trailing comma for the last line (or IE will complain)
    };
})();
