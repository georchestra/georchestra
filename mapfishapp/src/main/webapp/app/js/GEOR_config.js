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
         * Constant: HEADER_HEIGHT
         * Integer value representing the header height, as set in the shared maven filters
         * Defaults to 90
         */
        HEADER_HEIGHT: getCustomParameter("HEADER_HEIGHT", 90),

        /**
         * Method: DEFAULT_WMC
         * runtime method to get the current default WMC
         */
        DEFAULT_WMC: function() {
            if (GEOR.config.CONTEXTS &&
                GEOR.config.CONTEXTS[0] &&
                GEOR.config.CONTEXTS[0][2]) {
                return GEOR.config.CONTEXTS[0][2];
            }
            alert("Administrator: "+
                "GEOR.config.CONTEXTS is not configured as expected !");
            // should not happen:
            return "default.wmc";
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
         * Constant: ADDONS
         * An array of addons config objects.
         * Defaults to []
         *
         * An "addon config object" is an object with the following properties:
         *  id - {String} required identifier, which *MUST* :
         *        * be stable across deployments in order to let your users recover their tools
         *        * be unique in the ADDONS array
         *  name - {String} required addon name, which, once lowercased, gives the addon folder name
         *  title - {Object} a required hash storing addon titles by lang key
         *  description - {Object} a required hash storing addon descriptions by lang key
         *  roles - {Array} optional array of roles allowed to use this addon - defaults to [], which means everyone is allowed to.
         *          eg: ["ROLE_SV_ADMIN"] will allow the current addon for admin users only
         *  group - {String} an optional group for mutual exclusion between activated tools - default group is "tools"
         *  options - {Object} an optional config object which overrides the package default_options (in manifest.json)
         *  thumbnail - {String} an optional thumbnail path, relative to app/addons/{addon_name.toLowerCase()}/ (defaults to img/icon.png)
         *
         */
        ADDONS: getCustomParameter("ADDONS",
            []),

        /**
         * Constant: CONTEXTS
         * {Array} the array of arrays describing the available contexts
         *
         * Each "context array" consists of 4 mandatory fields:
         *   * the first field is the label which appears in the UI
         *   * the second one is the path to the thumbnail
         *   * the third one is the path to the context (WMC) file
         *   * the last one is a comment which will be shown on thumbnail hovering
         *
         * Example config :
         *   [
         *      ["OpenStreetMap", "app/img/contexts/osm.png", "default.wmc", "A unique OSM layer"],
         *      ["Orthophoto", "app/img/contexts/ortho.png", "context/ortho.wmc", "Orthophoto 2009"],
         *      ["Forêts", "app/img/contexts/forets.png", "context/forets.wmc", "Les 3 couches forêts sur fond OSM"]
         *   ]
         *
         * Defaults to ["OpenStreetMap", "app/img/contexts/osm.png", "default.wmc", "A unique OSM layer"]
         * Should *not* be empty !
         */
        CONTEXTS: getCustomParameter("CONTEXTS", [
            ["OpenStreetMap", "app/img/contexts/osm.png", "default.wmc", "A unique OSM layer"]
        ]),

        /**
         * Constant: GEOSERVER_WFS_URL
         * The URL to GeoServer WFS.
         * This is required if and only if the "referentials" module is activated.
         * Defaults to /geoserver/wfs
         */
        GEOSERVER_WFS_URL: getCustomParameter("GEOSERVER_WFS_URL",
            "/geoserver/wfs"),

        /**
         * Constant: GEOSERVER_WMS_URL
         * The URL to the GeoServer WMS.
         * This is required if and only if OSM_AS_OVMAP is set to false.
         * Defaults to /geoserver/wms
         */
        GEOSERVER_WMS_URL: getCustomParameter("GEOSERVER_WMS_URL",
            "/geoserver/wms"),

        /**
         * Constant: GEONETWORK_BASE_URL
         * The base URL to the local GeoNetwork server.
         * Required for CSW Browser module.
         * Defaults to "/geonetwork"
         */
        GEONETWORK_BASE_URL: getCustomParameter("GEONETWORK_BASE_URL",
            "/geonetwork"),

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
            ['http://sdi.georchestra.org/geonetwork/srv/fre/csw', 'le catalogue geOrchestra démo'],
            ['/geonetwork/srv/fre/csw', 'le catalogue local']
        ]),

        /**
         * Constant: DEFAULT_CSW_URL
         * CSW URL which should be used by default for freetext search
         * Note: must be one of the URLs in the above CATALOGS config option
         */
        DEFAULT_CSW_URL: getCustomParameter("DEFAULT_CSW_URL",
            'http://sdi.georchestra.org/geonetwork/srv/fre/csw'),

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
         * Constant: CSW_FILTER_PROPERTIES
         * A list of properties queried on catalog search.
         * Use ['AnyText'] to allow search on all metadata fields,
         * or use a subset of ISO queryable properties to limit search
         * on those properties.
         * Defaults to ['Title','AlternateTitle','Abstract','Subject','OrganisationName']
         */
        CSW_FILTER_PROPERTIES: getCustomParameter("CSW_FILTER_PROPERTIES", [
            'Title',
            'AlternateTitle',
            'Abstract',
            'Subject',
            'OrganisationName'
        ]),

        /**
         * Constant: NO_THUMBNAIL_IMAGE_URL
         * URL to a thumbnail image shown when none is provided by the CSW service
         * Defaults to the provided one ('app/img/nopreview.png')
         */
        NO_THUMBNAIL_IMAGE_URL: getCustomParameter("NO_THUMBNAIL_IMAGE_URL",
            'app/img/nopreview.png'),

        /**
         * Constant: DEFAULT_THESAURUS_KEY
         * Key (as the one in the response from /geonetwork/srv/fre/xml.thesaurus.getList)
         * of the thesaurus to use as the default (selected) one.
         *
         * local.theme.test is the only one exported by GeoNetwork by default.
         * It is highly recommended to upload new thesauri and to change this setting.
         */
        DEFAULT_THESAURUS_KEY: getCustomParameter("DEFAULT_THESAURUS_KEY",
            'local.theme.test'),

        /**
         * Constant: MAX_FEATURES
         * The maximum number of vector features displayed.
         * Defaults to 1000
         */
        MAX_FEATURES: getCustomParameter("MAX_FEATURES",
            1000),

        /**
         * Constant: MAX_LENGTH
         * The maximum number of chars in a XML response
         * before triggering an alert.
         * Defaults to 2 millions
         */
        MAX_LENGTH: getCustomParameter("MAX_LENGTH",
            2048*1024),

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
             * "wmsc_url": undefined
             */
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
         * Constant: DEACCENTUATE_REFERENTIALS_QUERYSTRING
         * {Boolean} Whether to deaccentuate the referentials widget query string
         * Defaults to true
         */
        DEACCENTUATE_REFERENTIALS_QUERYSTRING: getCustomParameter("DEACCENTUATE_REFERENTIALS_QUERYSTRING",
            true),

        /**
         * Constant: NS_LOC
         * {String} The referentials layers' namespace alias as defined in
         *    the GeoServer configuration.
         * Defaults to "geor_loc"
         */
        NS_LOC: getCustomParameter("NS_LOC", "geor_loc"),

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
         * Defaults to the Well-known scale set GoogleMapsCompatible (see WMTS spec appendix E)
         */
        MAP_SCALES: getCustomParameter("MAP_SCALES", [
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
        ]),

        /**
         * Constant: MAP_SRS
         * {String} The default map SRS code.
         * Defaults to EPSG:3857
         */
        MAP_SRS: getCustomParameter("MAP_SRS", "EPSG:3857"),

        /**
         * Constant: MAP_XMIN aka "left"
         * {Float} The max extent xmin in MAP_SRS coordinates.
         * Defaults to -20037508.34 (EPSG:3857 left)
         */
        MAP_XMIN: getCustomParameter("MAP_XMIN", -20037508.34),

        /**
         * Constant: MAP_YMIN aka "bottom"
         * {Float} The max extent ymin in MAP_SRS coordinates.
         * Defaults to -20037508.34 (EPSG:3857 bottom)
         */
        MAP_YMIN: getCustomParameter("MAP_YMIN", -20037508.34),

        /**
         * Constant: MAP_XMAX aka "right"
         * {Float} The max extent xmax in MAP_SRS coordinates.
         * Defaults to 20037508.34 (EPSG:3857 right)
         */
        MAP_XMAX: getCustomParameter("MAP_XMAX", 20037508.34),

        /**
         * Constant: MAP_YSMAX aka "top"
         * {Float} The max extent ymax in MAP_SRS coordinates
         * Defaults to 20037508.34 (EPSG:3857 top)
         */
        MAP_YMAX: getCustomParameter("MAP_YMAX", 20037508.34),

        /**
         * Constant: POINTER_POSITION_SRS_LIST
         * {Array} The cursor position will be displayed using these SRS.
         * Defaults to [["EPSG:4326", "WGS 84"],["EPSG:3857", "Spherical Mercator"]]
         */
        POINTER_POSITION_SRS_LIST: getCustomParameter("POINTER_POSITION_SRS_LIST",  [
            ["EPSG:4326", "WGS 84"],
            ["EPSG:3857", "Spherical Mercator"]
        ]),

        /**
         * Constant: PROJ4JS_STRINGS
         * {Object} The list of supported SRS with their definitions.
         * Defaults to "EPSG:4326", "EPSG:3857" & "EPSG:900913" being defined
         * Note that "EPSG:900913" is required if OSM_AS_OVMAP is set to true
         */
        PROJ4JS_STRINGS: getCustomParameter("PROJ4JS_STRINGS", {
            "EPSG:4326": "+title=WGS 84, +proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs",
            "EPSG:3857": "+title=Web Spherical Mercator, +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs",
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
         * {Object} Describes the geonames search options for the searchJSON web service.
         * (documentation here: http://www.geonames.org/export/geonames-search.html)
         *
         * Defaults to France/populated places
         *
         * Note that it is possible to restrict search to an admin area
         * by specifying either an adminCode1 or adminCode2 or adminCode3
         * See http://download.geonames.org/export/dump/admin1CodesASCII.txt for adminCode1
         * Aquitaine matches '97' while Bretagne (Brittany) matches 'A2'
         */
        GEONAMES_FILTERS: getCustomParameter("GEONAMES_FILTERS", {
            username: 'georchestra', // please replace this username by yours !
            // You can create a geonames account here: http://www.geonames.org/login
            // It is then required to enable your account to query the free web services
            // by visiting http://www.geonames.org/manageaccount
            country: 'FR',         // France
            //adminCode1: '97',    // Region
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
         * Constant: ROLES_FOR_EDIT
         * {Array} roles required for the edit functions to show up
         * Empty array means the module is available for everyone
         * Defaults to ['ROLE_ADMINISTRATOR']
         */
        ROLES_FOR_EDIT: getCustomParameter("ROLES_FOR_EDIT",
            ['ROLE_ADMINISTRATOR']),

        /**
         * Constant: PRINT_LAYOUTS_ACL
         * {Object} roles required for each print layout
         * Empty array means "layout is available for everyone"
         */
        PRINT_LAYOUTS_ACL: getCustomParameter("PRINT_LAYOUTS_ACL", {
            // A4 allowed for everyone:
            'A4 landscape': [],
            'A4 portrait': [],
            'Letter landscape': [],
            'Letter portrait': [],
            // A3 not allowed for unconnected users (guests):
            'A3 landscape': ['ROLE_SV_USER', 'ROLE_SV_REVIEWER', 'ROLE_SV_EDITOR', 'ROLE_SV_ADMIN'],
            'A3 portrait': ['ROLE_SV_USER', 'ROLE_SV_REVIEWER', 'ROLE_SV_EDITOR', 'ROLE_SV_ADMIN']
        }),

        /**
         * Constant: DEFAULT_PRINT_LAYOUT
         * {String} The default (ie selected) print layout.
         * Defaults to "A4 landscape".
         * Note: be sure to choose a layout available for everyone
         */
        DEFAULT_PRINT_LAYOUT: getCustomParameter("DEFAULT_PRINT_LAYOUT",
            "A4 landscape"),

        /**
         * Constant: DEFAULT_PRINT_RESOLUTION
         * {String} The default (ie selected) print resolution.
         * Defaults to "91"
         */
        DEFAULT_PRINT_RESOLUTION: getCustomParameter("DEFAULT_PRINT_RESOLUTION",
            "91"),

        /**
         * Constant: PDF_FILENAME
         * {String} The PDF filename prefix.
         * Defaults to "georchestra_${yyyy-MM-dd_hhmmss}"
         */
        PDF_FILENAME: getCustomParameter("PDF_FILENAME",
            "georchestra_${yyyy-MM-dd_hhmmss}"),

        /**
         * Constant: HELP_URL
         * {String} URL of the help ressource.
         * Defaults to "http://cms.geobretagne.fr/assistance"
         */
        HELP_URL: getCustomParameter("HELP_URL",
            "http://cms.geobretagne.fr/assistance"),

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
         * Constant: EDITABLE_LAYERS
         * {RegExp} 
         * 
         */
        EDITABLE_LAYERS: getCustomParameter("EDITABLE_LAYERS",
            /.*/i),

        /**
         * Constant: FORCE_LOGIN_IN_TOOLBAR
         * {Boolean} If true, the login link is always shown in the app toolbar.
         * Defaults to false.
         */
        FORCE_LOGIN_IN_TOOLBAR: getCustomParameter("FORCE_LOGIN_IN_TOOLBAR",
            false),

        /**
         * Constant: SEND_MAP_TO
         * {Array} List of menu items configs
         *
         * Each menu item config **must** have the following properties: 
         *  - name: the link name. Will be localized by OpenLayers.i18n
         *  - url: the template url for the link. Must contain one of 
         *   {context_url}, {map_url} or {id} strings, which will be resp. 
         *   replaced by the generated WMC link, the map permalink and the map id.
         *
         * Each menu item config **may** have the following properties: 
         *  - qtip: the tip appearing on menu item hover. Will be localized by OpenLayers.i18n
         *  - iconCls: the CSS class which will be appended to the menu item
         */
        SEND_MAP_TO: getCustomParameter("SEND_MAP_TO", [
            {"name": "Mobile viewer", "url": "http://sdi.georchestra.org/sviewer/?wmc={context_url}", "qtip": "Mobile compatible viewer on sdi.georchestra.org"},
            {"name": "Desktop viewer", "url": "http://sdi.georchestra.org/mapfishapp/?wmc={context_url}", "qtip": "Desktop viewer on sdi.georchestra.org"}
        ]),

        /**
         * Constant: WMTS_SERVERS
         * {Array} List of externals WMTS to display in the WMTS servers tab.
         */
        WMTS_SERVERS: getCustomParameter("WMTS_SERVERS", [
            {"name": "GéoBretagne rasters", "url": "http://tile.geobretagne.fr/gwc02/service/wmts"},
            {"name": "GéoBretagne OSM", "url": "http://osm.geobretagne.fr/gwc01/service/wmts"}
        ]),

        /**
         * Constant: WMS_SERVERS
         * {Array} List of externals WMS to display in the WMS servers tab.
         */
        WMS_SERVERS: getCustomParameter("WMS_SERVERS", [
            {"name": "GéoBretagne", "url": "http://geobretagne.fr/geoserver/wms"},
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
            {"name": "GéoBretagne", "url": "http://geobretagne.fr/geoserver/wfs"},
            {"name": "BMO/OpenStreetMap", "url": "http://bmo.openstreetmap.fr/ows"},
            {"name": "Corine Land Cover", "url": "http://sd1878-2.sivit.org/geoserver/wfs"}
        ])
    // No trailing comma for the last line (or IE will complain)
    };
})();
