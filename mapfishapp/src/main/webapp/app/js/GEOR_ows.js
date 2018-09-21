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
 * @include OpenLayers/Format/WMSDescribeLayer/v1_1.js
 * @include OpenLayers/Format/WFSDescribeFeatureType.js
 * @include OpenLayers/Protocol/WFS/v1_0_0.js
 * @include OpenLayers/Protocol/WFS/v1_1_0.js
 * @include OpenLayers/Protocol/WFS/v2_0_0.js
 * @include GeoExt/data/WMSDescribeLayerStore.js
 * @include GeoExt/data/AttributeStore.js
 * @include GeoExt/data/WMSCapabilitiesStore.js
 * @include GeoExt/data/WMTSCapabilitiesStore.js
 * @include GeoExt/data/WFSCapabilitiesStore.js
 * @include OpenLayers/Format/WMSCapabilities/v1_1_0.js
 * @include OpenLayers/Format/WMSCapabilities/v1_1_1.js
 * @include OpenLayers/Format/WMSCapabilities/v1_3_0.js
 * @include OpenLayers/Format/WMTSCapabilities/v1_0_0.js
 * @include OpenLayers/Format/WFSCapabilities/v1_0_0.js
 * @include OpenLayers/Format/WFSCapabilities/v1_1_0.js
 * @include OpenLayers/Format/WFSCapabilities/v2_0_0.js
 * @include OpenLayers/Strategy/Fixed.js
 * @include OpenLayers/Layer/Vector.js
 * @requires OpenLayers/Layer/WMS.js
 * @include OpenLayers/Layer/WMTS.js
 * @requires GEOR_config.js
 * @include GEOR_waiter.js
 * @include GEOR_util.js
 * @include OpenLayers/Format/GML/v2.js
 * @include OpenLayers/Format/GML/v3.js
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
        // for the use of geOrchestra only:
        {name: "type", type: "string"},
        {name: "_described", type: "boolean", defaultValue: false},
        {name: "_wfs_capabilities"}, // object
        {name: "WCS_typeName", type: "string"},
        {name: "WCS_URL", type: "string"},
        {name: "WFS_typeName", type: "string"},
        {name: "WFS_URL", type: "string"},
        {name: "geometryType", type: "string", defaultValue: "unknown"}, // Line, Point, Polygon
        {name: "multiGeometry", type: "boolean"},
        {name: "layergroup", type: "boolean", defaultValue: false},
        // end geOrchestra use
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
     * Constant: attributeStoreFields
     * {Array} The fields shared by each attributeStore record in this app.
     */
    var attributeStoreFields = ["name", "type", "restriction", {name:"nillable", type: "boolean"}, "annotation", "value"];
    // Note: a NOT NULL clause for a field in postgresql db is translated by GeoServer 1.7.x into
    //  <xsd:element maxOccurs="1" minOccurs="1" name="nom" nillable="true" type="xsd:string"/>
    // while in GeoServer 2.x, it leads to the correct xsd:
    //  <xsd:element maxOccurs="1" minOccurs="1" name="nom" nillable="false" type="xsd:string"/>
    // thus, mapfishapp/edit form will not display required fields as such with GeoServer 1.7.x

    /**
     * Constant: WMS_BASE_PARAMS
     * {Object} The base params for WMS requests.
     */
    var WMS_BASE_PARAMS = {
        "SERVICE": "WMS",
        // version not specified => highest version is returned
        // the correct parser will be used automagically.
        //"VERSION": "1.1.1"
    };

    /**
     * Constant: WMTS_BASE_PARAMS
     * {Object} The base params for WMTS requests.
     */
    var WMTS_BASE_PARAMS = {
        "SERVICE": "WMTS",
        "VERSION": "1.0.0"
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
        "SERVICE": "WFS"
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

    /**
     * Method: getWxsInfo
     */
    var getWxsInfo = function(records, owsType) {
        var i, len = records.length, r;
        for (i=0; i<len; i++) {
            r = records[i];
            if (r.get("owsType") == owsType &&
                r.get("owsURL") &&
                r.get("typeName")) {
                return r;
            }
        }
        return null;
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
         * Property: wmsVersionToExceptionsMapping
         * {Object} We want WMS XML exceptions.
         * But the EXCEPTIONS parameter value changes with the WMS version ...
         */
        wmsVersionToExceptionsMapping: {
            "1.0.0": "WMS_XML",
            "1.1.0": "application/vnd.ogc.se_xml",
            "1.1.1": "application/vnd.ogc.se_xml",
            "1.3.0": "XML"
        },

        /**
         * Property: wmsVersionToSLDVersionMapping
         * {Object} Starting with WMS 1.3, the SLD_VERSION parameter is mandatory.
         * But the SLD_VERSION parameter value changes with the WMS version ...
         * see https://github.com/georchestra/georchestra/issues/636
         */
        wmsVersionToSLDVersionMapping: {
            "1.0.0": "1.0.0",
            "1.1.0": "1.0.0",
            "1.1.1": "1.0.0",
            "1.3.0": "1.1.0"
        },

        /**
         * Property: defaultWMSLayerOptions
         * {Object} Default OpenLayers WMS layer options
         */
        defaultWMSLayerOptions: {
            singleTile: GEOR.config.TILE_SINGLE,
            gutter: 10,
            buffer: 0,
            tileSize: new OpenLayers.Size(GEOR.config.TILE_WIDTH, GEOR.config.TILE_HEIGHT)
        },

        /**
         * Property: defaultWMTSLayerOptions
         * {Object} Default OpenLayers WMTS layer options
         */
        defaultWMTSLayerOptions: {
            buffer: 0
        },

        getRecordFields: function() {
            return defaultRecordFields;
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
                var match = store.getAt(idx).get("type").match(
                    matchGeomProperty
                );
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
         * record - {Ext.data.Record} The first matching record, null otherwise
         */
        getWfsInfo:function(records) {
            return getWxsInfo(records, "WFS");
        },

        getWcsInfo:function(records) {
            return getWxsInfo(records, "WCS");
        },

        /**
         * Method: getWFSCapURL
         * Creates a WFS capabilities URL given a record
         * from a WMSDescribeLayerStore
         *
         * Parameters:
         * record - {Object}
         *
         * Returns:
         * {String} The full URL string.
         */
        getWFSCapURL: function(record) {
            var url = record.owsURL || record.get("owsURL");
            var p = Ext.apply(
                {"REQUEST": "GetCapabilities"},
                WFS_BASE_PARAMS,
                OpenLayers.Util.getParameters(url)
            );
            return OpenLayers.Util.urlAppend(
                OpenLayers.Util.removeTail(url),
                OpenLayers.Util.getParameterString(p)
            );
        },

        /**
         * APIMethod: WMSDescribeLayer
         * Create a {GeoExt.data.WMSDescribeLayerStore} store from the layer,
         * load it if the success callback function is provided, and
         * return it.
         *
         * Parameters:
         * layer - {GeoExt.data.LayerRecord} or {OpenLayers.Layer.WMS} The
         *     layer from which to create the store.
         * options - {Object} An object with the properties:
         * - success - {Function} Callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback execution scope.
         * - storeOptions - {Object} Additional store options.
         */
        WMSDescribeLayer: function(layer, options) {
            if (layer instanceof GeoExt.data.LayerRecord) {
                layer = layer.get("layer");
            }
            options = options || {};
            var url = (layer.url instanceof Array) ? layer.url[0] : layer.url;
            var storeOptions = Ext.applyIf({
                // For some reason, if layer.url ends up with ?
                // the generated request URL is not correct
                // see http://applis-bretagne.fr/redmine/issues/1979
                url: url.replace(/\?$/,''),
                baseParams: Ext.applyIf({
                    "REQUEST": "DescribeLayer",
                    "LAYERS": layer.params.LAYERS,
                    // DescribeLayer should use the same WMS version
                    // as the getmap requests on this layer: ... but ...
                    "VERSION": "1.1.1", //rather than layer.params.VERSION,
                    // this is because describe layer 1.3.0 is not yet supported by GeoServer
                    // see: https://github.com/georchestra/georchestra/issues/186
                    "SLD_VERSION": "1.0.0", // force here a correct "SLD_VERSION" for WMS 1.1.1
                    // (to be removed when the WMS DescribeLayer VERSION value is dynamic again)
                    //
                    // WIDTH and HEIGHT params seem to be required for
                    // some versions of MapServer (typ. 5.6.1)
                    // see http://applis-bretagne.fr/redmine/issues/1979
                    "WIDTH": 1,
                    "HEIGHT": 1
                }, WMS_BASE_PARAMS)
            }, options.storeOptions);
            var store = new GeoExt.data.WMSDescribeLayerStore(storeOptions);
            if (options.success) {
                loadStore(store,
                          options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WFSDescribeFeatureType
         * Create a {GeoExt.data.AttributeStore} store from the
         * WMSDescribeLayer record, load it if a callback function
         * is provided, and return it.
         *
         * Parameters:
         * record - {Ext.data.Record|Object} Record with "owsURL" and
         *     "typeName" fields or object with same keys.
         *     Can get modified by addition of a featureNS property
         *     if options.extractFeatureNS is true
         * options - {Object} An object with the properties:
         * - success - {Function} A callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback execution scope.
         * - storeOptions - {Object} Additional store options.
         * - extractFeatureNS - {Boolean} Optional boolean specifying
         *             whether the featureNS should be extracted from
         *             WFSDescribeFeatureType response
         */
        WFSDescribeFeatureType: function(record, options) {
            options = options || {};

            r = (record instanceof Ext.data.Record) ? {
                typeName: record.get("typeName"),
                owsURL: record.get("owsURL")
            } : record;

            var store;

            if (options.extractFeatureNS) {
                // we extract featureNS from WFSDescribeFeatureType response
                // and set it in the original record, so that it can be used
                // later for protocol creation

                // FIXME: what about options.storeOptions in this case ?
                store = new GeoExt.data.AttributeStore({
                    //reader: new GeoExt.data.AttributeReader({}, attributeStoreFields)
                    fields: attributeStoreFields
                });

                Ext.Ajax.request({
                    url: r.owsURL.replace(/\?$/,''),
                    method: 'GET',
                    disableCaching: false,
                    headers: {
                        "Content-Type": "application/xml; charset=UTF-8"
                    },
                    params: Ext.applyIf({
                        "REQUEST": "DescribeFeatureType",
                        "TYPENAME": r.typeName,
                        // VERSION is required (from the OGC spec) - but GeoServer does well without it.
                        // This is essentially for MapServer:
                        "VERSION": "1.0.0"
                        // for more information, read https://github.com/georchestra/georchestra/issues/314
                    }, WFS_BASE_PARAMS),
                    success: function(resp) {

                        var data = resp.responseXML;
                        if (!data || !data.documentElement) {
                            data = resp.responseText;
                        }

                        // Begin hack
                        // Since WFS version is no more a default param, we need to retrieve the WFS version of the layer
                        // to initialize the WFSProtocol with matching version
                        // Ideally, we should call a capabilities request to get the WFS version, but it is too slow
                        // So we get the version from the describefeaturetype interpreting the gml version in the schema definition
                        var version;

                        if (resp.responseText.indexOf('http://www.opengis.net/gml/3.2') > 0) {
                        	version = "2.0.0";
                        } else if (resp.responseText.indexOf('http://www.opengis.net/gml') > 0 && resp.responseText.indexOf('gml/3.1.1/base/gml.xsd') > 0) {
                        	version = "1.1.0";
                        } else {
                        	version = "1.0.0";
                        }
                        // End hack

                        var format = new OpenLayers.Format.WFSDescribeFeatureType();
                        var jsObj = format.read(data);

                        if (record instanceof Ext.data.Record) {
                            record.set("WFSversion", version);
                            record.set("featureNS", jsObj.targetNamespace);
                        } else {
                            record.WFSversion = version;
                            record.featureNS = jsObj.targetNamespace;
                        }

                        store.on({
                            load: function() {
                                store.purgeListeners();
                                if (options.success) {
                                    options.success.apply(options.scope, arguments);
                                }
                            }
                        });
                        store.loadData(data);
                    },
                    failure: options.failure || function() {},
                    scope: options.scope || this
                });

            } else {

                var storeOptions = Ext.applyIf({
                    url: r.owsURL.replace(/\?$/,''),
                    fields: attributeStoreFields,
                    baseParams: Ext.applyIf({
                        "REQUEST": "DescribeFeatureType",
                        "TYPENAME": r.typeName,
                        // see above comment regarding VERSION:
                        "VERSION": "1.0.0"
                    }, WFS_BASE_PARAMS)
                }, options.storeOptions || {});
                store = new GeoExt.data.AttributeStore(storeOptions);
                if (options.success) {
                    loadStore(store,
                              options.success, options.failure, options.scope);
                }
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
            var baseParams = options.baseParams || {};
            var fields = defaultRecordFields;
            fields[0].defaultValue = "WMS";
            var recordType = GEOR.util.createRecordType(fields);
            var storeOptions = Ext.applyIf({
                rawAttribution: true, // see https://github.com/geoext/geoext/pull/90
                baseParams: Ext.apply({
                    "REQUEST": "GetCapabilities"
                }, baseParams, WMS_BASE_PARAMS),
                layerOptions: Ext.apply({},
                    layerOptions,
                    GEOR.ows.defaultWMSLayerOptions
                ),
                fields: recordType
            }, options.storeOptions);
            var store = new GeoExt.data.WMSCapabilitiesStore(storeOptions);
            if (options.success) {
                loadStore(store,
                          options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WMTSCapabilities
         * Create a {GeoExt.data.WMTSCapabilitiesStore} store, load it
         * if a callback function is provided, and return it.
         *
         * Parameters:
         * options - {Object} An object with the properties:
         * - mapSRS - {String} the current map SRS, which will be used to
         *   choose the best available TileMatrixSet (optional).
         * - success - {Function} Callback function called when the
         *   store has been successfully loaded.
         * - failure - {Function} Callback function called when the
         *   store could not be loaded.
         * - scope - {Object} The callback execution scope.
         * - storeOptions - {Object} Additional store options.
         */
        WMTSCapabilities: function(options) {
            options = options || {};
            var layerOptions = (options.storeOptions &&
                options.storeOptions.layerOptions) ?
                    options.storeOptions.layerOptions : {};
            var baseParams = options.baseParams || {};
            var storeOptions = Ext.applyIf({
                baseParams: Ext.apply({
                    "REQUEST": "GetCapabilities"
                }, baseParams, WMTS_BASE_PARAMS),
                layerOptions: Ext.apply({
                    // would be good for WMTS base layers only:
                    //transitionEffect: 'resize'
                    projection: options.mapSRS ||
                        GeoExt.MapPanel.guess().map.getProjection()
                }, layerOptions, GEOR.ows.defaultWMTSLayerOptions),
                fields: [
                    {name: "type", type: "string", defaultValue: "WMTS"}, // specific for georchestra
                    // those from the standard WMTS capabilities reader:
                    {name: "name", type: "string", mapping: "identifier"},
                    {name: "title", type: "string"},
                    {name: "abstract", type: "string"},
                    {name: "queryable", type: "boolean"},
                    {name: "llbbox", mapping: "bounds", convert: function(v) {
                        return [v.left, v.bottom, v.right, v.top];
                    }},
                    {name: "formats"}, // array
                    {name: "infoFormats"}, // array
                    {name: "styles"}, // array of Objects {abstract, identifier, isDefault, keywords, title}
                    {name: "keywords"} // Object
                ]
            }, options.storeOptions);
            var store = new GeoExt.data.WMTSCapabilitiesStore(storeOptions);
            if (options.success) {
                loadStore(store,
                          options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: hydrateLayerRecord
         * Adds missing fields in {GeoExt.data.LayerRecord} by issuing a
         * WMS capabilities request. In case we have been given a layer
         * served by GeoServer, the request is first issued to the
         * virtual service corresponding to the layer namespace alias.
         * If the request fails to find the layer in its namespace, a second
         * request is issued to the main service URL.
         *
         * Parameters:
         * record - {GeoExt.data.LayerRecord} the input record.
         * options - {Object} An object with the properties:
         * - success - {Function} Callback function
         * - failure - {Function} Callback function
         * - scope - {Object} The callback function's execution scope.
         *
         * Returns:
         * {GeoExt.data.LayerRecord} The same record with hydrated fields.
         */
        hydrateLayerRecord: function(record, options) {
            var url = record.get('layer').url,
                layername = record.get('name');
            if (!options.useMainService && url.indexOf("geoserver/wms") > 0) {
                // try to use virtual service instead of main service
                var nsalias,
                    t = layername.split(':');
                if (t.length > 1) {
                    nsalias = t.shift();
                    layername = t.shift();
                    url = url.replace("geoserver/wms", "geoserver/"+nsalias+"/wms");
                }
            }
            GEOR.waiter.show();
            var store = GEOR.ows.WMSCapabilities({
                storeOptions: {
                    url: url.replace(/\?$/,'')
                },
                success: function(store, records) {
                    var index = store.find("name", layername);
                    if (index < 0) {
                        GEOR.util.errorDialog({
                            msg: OpenLayers.i18n("The NAME layer was not found in WMS service.",
                                {'NAME': layername})
                        });
                        return;
                    }
                    var r = records[index];
                    // replace all fields except layer
                    Ext.each(defaultRecordFields, function(rf) {
                        record.set(rf.name, r.get(rf.name));
                    });
                    if (options.success) {
                        options.success.apply(options.scope);
                    }
                },
                failure: function() {
                    if (!options.useMainService) {
                        GEOR.ows.hydrateLayerRecord(record, Ext.apply(options, {
                            useMainService: true
                        }));
                    } else if (options.failure) {
                        options.failure.apply(options.scope);
                    }
                }
            });
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
                }, options.vendorParams || {}, WFS_BASE_PARAMS),
                fields: [
                    {name: "type", type: "string", defaultValue: "WFS"},
                    {name: "name", type: "string"},
                    {name: "title", type: "string"},
                    {name: "namespace", type: "string", mapping: "featureNS"},
                    {name: "abstract", type: "string"}
                ]
            }, options.storeOptions);
            var store = new GeoExt.data.WFSCapabilitiesStore(storeOptions);
            if (options.success) {
                loadStore(store,
                          options.success, options.failure, options.scope);
            }
            return store;
        },

        /**
         * APIMethod: WFSProtocol
         * Create an {OpenLayers.Protocol.WFS} instance.
         *
         * Parameters:
         * record - {Ext.data.Record|Object} Record or Hash with "owsURL" and
         *     "typeName" and "featureNS" fields.
         * map - {OpenLayers.Map} Map object.
         * options - {Object} Additional protocol options
         *
         * Returns:
         * {OpenLayers.Protocol.WFS} The protocol.
         */
        WFSProtocol: function(record, map, options) {
            record = (record instanceof Ext.data.Record) ? {
                typeName: record.get("typeName"),
                featureNS: record.get("featureNS"),
                owsURL: record.get("owsURL"),
                WFSversion : record.get("WFSversion")
            } : record;
            var featureType, featurePrefix;
            var parts = record.typeName.split(":");
            if (parts.length > 1) {
                featurePrefix = parts[0];
                featureType = parts[1];
            } else {
                featurePrefix = null;
                featureType = parts[0];
            }
            options = Ext.applyIf({
                url: record.owsURL.replace(/\?$/,''),
                featureType: featureType,
                featureNS: record.featureNS,
                featurePrefix: featurePrefix || 'feature',
                srsNameInQuery: true, // see http://trac.osgeo.org/openlayers/ticket/2228
                srsName: map.getProjection(),
                version: record.WFSversion
            }, options || {});
            return new OpenLayers.Protocol.WFS(options);
        }
    };
})();

// hack for SLD_VERSION override in case the SLD is produced by mapfishapp
// see https://github.com/georchestra/georchestra/issues/636
(function() {
    var p = OpenLayers.Layer.WMS.prototype, fn = p.getFullRequestString;
    p.getFullRequestString = function() {
        var params = this.params;
        if (params.VERSION == "1.3.0" && /ws\/sld\/geodoc/.test(params.SLD)) {
            // force SLD version when handling our own SLD documents:
            params.SLD_VERSION = "1.0.0";
        }
        return fn.apply(this, arguments);
    };
})();
