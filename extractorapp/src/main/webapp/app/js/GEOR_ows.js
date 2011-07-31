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

/*
 * @include OpenLayers/Format/WMSDescribeLayer.js
 * @include OpenLayers/Format/WMSDescribeLayer/v1_1.js
 * @include OpenLayers/Format/WFSDescribeFeatureType.js
 * @include OpenLayers/Protocol/WFS.js
 * @include OpenLayers/Protocol/WFS/v1_0_0.js
 * @include GeoExt/data/WMSDescribeLayerStore.js
 * @include GeoExt/data/AttributeStore.js
 * @include GeoExt/data/WMSCapabilitiesStore.js
 * @include OpenLayers/Format/WFSCapabilities/v1_0_0.js
 * @include OpenLayers/Format/WMSCapabilities/v1_1_1.js
 * @include GeoExt/data/WFSCapabilitiesStore.js
 * @include OpenLayers/Strategy/Fixed.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Util.js
 */

Ext.namespace("GEOR");

/**
 * Module: GEOR.ows
 * A utility module for creating OWS stores, sending OWS requests, etc.
 */
GEOR.ows = (function() {
    /*
     * Private
     */
    
    /**
     * Constant: defaultRecordFields
     * {Array} The fields shared by each layer record in this app.
     */
    var defaultRecordFields = [
        {name: "name", type: "string"},
        {name: "title", type: "string"},
        {name: "abstract", type: "string"},
        {name: "queryable", type: "boolean"},
        {name: "opaque", type: "boolean"},
        {name: "noSubsets", type: "boolean"},
        {name: "cascaded", type: "int"},
        {name: "fixedWidth", type: "int"},
        {name: "fixedHeight", type: "int"},
        {name: "minScale", type: "float"},
        {name: "maxScale", type: "float"},
        {name: "prefix", type: "string"},
        {name: "formats"}, // array
        {name: "styles"}, // array
        {name: "srs"}, // object
        {name: "dimensions"}, // object
        {name: "bbox"}, // object
        {name: "llbbox"}, // array
        {name: "attribution"}, // object
        {name: "keywords"}, // array
        {name: "identifiers"}, // object
        {name: "authorityURLs"}, // object
        {name: "metadataURLs"}, // array
        {name: "hideInLegend", type: "boolean", defaultValue: false}
    ];
    
    /**
     * Constant: WMS_BASE_PARAMS
     * {Object} The base params for WMS requests.
     */
    var WMS_BASE_PARAMS = {
        "SERVICE": "WMS",
        "VERSION": "1.1.1",
        "EXCEPTIONS": "application/vnd.ogc.se_inimage",
        "FORMAT": "image/png"
    };

    /**
     * Constant: WFS_BASE_PARAMS
     * {Object} The base params for WFS requests.
     *
     * When using WFS 1.0.O, GetFeature requests do not include the srsName.
     * Thus, we loose the ability to display geometries from features
     * served via GeoServer.
     * Fixed with http://trac.openlayers.org/ticket/2228
     */
    var WFS_BASE_PARAMS = {
        "SERVICE": "WFS",
        "VERSION": "1.0.0"
    };

    /**
     * Constant: WCS_BASE_PARAMS
     * {Object} The base params for WCS requests.
     */
    var WCS_BASE_PARAMS = {
        "SERVICE": "WCS",
        "VERSION": "1.1.0"
    };

    /**
     * Constant: WFS_FEATURE_NAMESPACES
     * {Array} Array of supported feature namespaces.
     */
    var WFS_FEATURE_NAMESPACES = [
        "http://mapserver.gis.umn.edu/mapserver"
    ];

    /**
     * Method: buildUrlFromParams
     * Generate the url given a baseUrl and params object.
     *
     * Parameters:
     * baseUrl - baseUrl of the service
     * params - params object
     */
    var buildUrlFromParams = function(baseUrl, params) {
        var ps = OpenLayers.Util.getParameterString(params);
        return OpenLayers.Util.urlAppend(baseUrl, ps);
    };

    /**
     * Method: loadStore
     * Register a "load" listener on the store, and load it.
     *
     * Parameters:
     * store - {Ext.data.Store} The store.
     * success - {Function} Callback function called when the
     *      store has been successfully loaded.
     * failure - {Function} Callback function called when the
     *      store could not be loaded.
     * scope - {Object} The callback execution scope.
     */
    var loadStore = function(store, success, failure, scope) {
        store.on({
            load: function() {
                store.purgeListeners();
                if (success) {
                    success.apply(scope, arguments);
                }
            },
            loadexception: function() {
                store.purgeListeners();
                if (failure) {
                    failure.apply(scope, arguments);
                }
            }
        });
        store.load();
    };

    /*
     * Public
     */
    return {
        /**
         * Property: matchGeomProperty
         * {Regex} The regex to use to match geometry properties in WFS
         * DescribeFeatureType responses.
         */
        matchGeomProperty: /^gml:(Multi)?(Point|LineString|Polygon|Curve|Surface|Geometry)PropertyType$/,
        
        /**
         * Property: defaultLayerOptions
         * {Object} Default WMS layer options
         */
        defaultLayerOptions: {
            singleTile: true,
            ratio: 1,
            transitionEffect: 'resize'
        },

        /**
         * Method: getSymbolTypeFromAttributeStore
         *   Extract symbol type from an attributeStore
         *
         * Parameters:
         * store - {GeoExt.data.AttributeStore} A geoext attribute store
         *
         * Returns
         * type - {String} The symbol type
         */
        getSymbolTypeFromAttributeStore: function(store) {
            var matchGeomProperty = GEOR.ows.matchGeomProperty;
            var idx = store.find("type", matchGeomProperty);
            var ret;

            if (idx > -1) {
                var match = store.getAt(idx).get("type").match(matchGeomProperty);
                var type = ({
                    "Point": "Point",
                    "LineString": "Line",
                    "Polygon": "Polygon",
                    "Curve": "Line",
                    "Surface": "Polygon"
                })[match[2]];
                ret = { type: type, multi: match[1] };
            }

            return ret;
        },

        /**
         * Method: getWfsUrl
         * Given an array of records obtained from loading an
         * {GeoExt.data.WMSDescribeLayerStore} store return
         * the first record whose owsType is "WFS".
         *
         * Parameters:
         * records - {Array({Ext.data.Record})} Array of 
         *     records.
         *
         * Returns
         * record - {Ext.data.Record} The first matching record.
         */
        getWfsInfo:function(records) {
            for (var i=0, len=records.length; i<len; i++) {
                r = records[i];
                if (r.get("owsType") == "WFS" &&
                    r.get("owsURL") &&
                    r.get("typeName")) {
                    return r;
                }
            }
            return undefined;
        },

        /**
         * APIMethod: WMSDescribeLayer
         * Create a {GeoExt.data.WMSDescribeLayerStore} store from the layer,
         * load it if the success callback function is provided, and
         * return it.
         *
         * Parameters:
         * layername - {String} The layer from which to create the store.
         * options - {Object} An object with the properties:
         * - success - {Function} Callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback execution scope.
         * - storeOptions - {Object} Additional store options.
         */
        WMSDescribeLayer: function(layername, options) {
            options = options || {};
            var storeOptions = Ext.applyIf({
                baseParams: Ext.applyIf({
                    "REQUEST": "DescribeLayer",
                    "LAYERS": layername,
                    "WIDTH": 1,
                    "HEIGHT": 1
                }, WMS_BASE_PARAMS)
            }, options.storeOptions);
            var store = new GeoExt.data.WMSDescribeLayerStore(storeOptions);
            if (options.success) {
                loadStore(store, options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WMSCapabilities
         * Create a {GeoExt.data.WMSCapabilitiesStore} store, load it
         * if a callback function is provided, and return it.
         *
         * Parameters:
         * options - {Object} An object with the properties:
         * - success - {Function} Callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback execution scope.
         * - storeOptions - {Object} Additional store options.
         */
        WMSCapabilities: function(options) {
            options = options || {};
            var layerOptions = (options.storeOptions && 
                options.storeOptions.layerOptions) ? 
                    options.storeOptions.layerOptions : {};
            var storeOptions = Ext.applyIf({
                baseParams: Ext.applyIf({
                    "REQUEST": "GetCapabilities"
                }, WMS_BASE_PARAMS),
                layerOptions: Ext.apply({}, 
                    layerOptions, 
                    GEOR.ows.defaultLayerOptions
                ),
                fields: defaultRecordFields
            }, options.storeOptions);
            var store = new GeoExt.data.WMSCapabilitiesStore(storeOptions);
            if (options.success) {
                loadStore(store,
                          options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WFSCapabilities
         * Create a {GeoExt.data.WFSCapabilitiesStore} store, load it
         * if a callback function is provided, and return it.
         *
         * Parameters:
         * options - {Object} An object with the properties:
         * - success - {Function} Callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback function's execution scope.
         * - storeOptions - {Object} Additional store options.
         */
        WFSCapabilities: function(options) {
            options = options || {};
            var storeOptions = Ext.applyIf({
                baseParams: Ext.apply({
                    "REQUEST": "GetCapabilities"
                }, options.vendorParams || {}, WFS_BASE_PARAMS)
            }, options.storeOptions);
            var store = new GeoExt.data.WFSCapabilitiesStore(storeOptions);
            if (options.success) {
                loadStore(store, options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WFSDescribeFeatureTypeUrl
         * Generate the url to query a WFS DescribeFeatureType given a
         * baseUrl of the WFS service and a typeName.
         *
         * Parameters:
         * baseUrl - baseUrl of the WFS service
         * typeName - typeName used to build the url.
         */
        WFSDescribeFeatureTypeUrl: function(baseUrl, typeName) {
            var params = Ext.applyIf({
                "REQUEST": "DescribeFeatureType",
                "TYPENAME": typeName
            }, WFS_BASE_PARAMS);

            return buildUrlFromParams(baseUrl, params);
        },

        /**
         * APIMethod: WCSDescribeCoverageUrl
         * Generate the url to query a WCS DescribeCoverage given a
         * baseUrl of the WCS service and an identifier.
         *
         * Parameters:
         * baseUrl - baseUrl of the WCS service
         * identifier - identifier used to build the url.
         */
        WCSDescribeCoverageUrl: function(baseUrl, identifier) {
            var params = Ext.applyIf({
                "REQUEST": "DescribeCoverage",
                "IDENTIFIERS": identifier
            }, WCS_BASE_PARAMS);

            return buildUrlFromParams(baseUrl, params);
        }

    };
})();
