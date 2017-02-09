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
 * @include GEOR_wmc.js
 * @include GEOR_config.js
 * @include GEOR_ows.js
 * @include GEOR_util.js
 * @include GEOR_waiter.js
 * @include GEOR_localStorage.js
 * @include OpenLayers/Projection.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Format/CQL.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/LayerStore.js
 * @include GeoExt/data/WMSCapabilitiesReader.js
 * @include GeoExt/data/WFSCapabilitiesReader.js
 */

Ext.namespace("GEOR");

GEOR.mapinit = (function() {
    /*
     * Private
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        "searchresults"
    );

    /**
     * Property: layerStore
     * {GeoExt.data.LayerStore} The application's layer store.
     */
    var layerStore = null;

    /**
     * Property: initState
     * {Array} shorthand for GEOR.initstate
     */
    var initState = null;
    
    /**
     * Property: initSearch
     * {Object} shorthand for GEOR.initsearch
     */
    var initSearch = null;

    /**
     * Property: cb
     * {Function} executed after this init function has done its job
     */
    var cb = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: customRecenter
     * Convenient method telling whether to use the WMC bbox 
     * or the incoming GET parameters
     *
     * Returns:
     * {Boolean} If true, use incoming GET params
     */
    var customRecenter = function() {
        return GEOR.config.CUSTOM_BBOX !== '' || GEOR.config.CUSTOM_CENTER !== ',';
    };

    /**
     * Method: zoomToCustomExtent
     * Updates the map extent to the one given in parameters
     */
    var zoomToCustomExtent = function() {
        // Zoom to custom bbox: (see http://applis-bretagne.fr/redmine/issues/4502)
        var map = layerStore.map,
            mapProjObj = map.getProjectionObject();

        if (GEOR.config.CUSTOM_BBOX !== '') {
            var forcedBounds = OpenLayers.Bounds.fromString(GEOR.config.CUSTOM_BBOX);
            forcedBounds.transform(new OpenLayers.Projection("EPSG:4326"), mapProjObj);
            map.zoomToExtent(forcedBounds, true);
        } else if (GEOR.config.CUSTOM_CENTER !== ',') {
            var forcedCenter = OpenLayers.LonLat.fromString(GEOR.config.CUSTOM_CENTER);
            forcedCenter.transform(new OpenLayers.Projection("EPSG:4326"), mapProjObj);
            var z;
            if (GEOR.config.CUSTOM_RADIUS !== '') {
                var radius = parseInt(GEOR.config.CUSTOM_RADIUS),
                    bounds = new OpenLayers.Bounds(forcedCenter.lon, forcedCenter.lat, 
                        forcedCenter.lon, forcedCenter.lat),
                    units = mapProjObj.getUnits();

                if (units == 'm' || units == 'meters') {
                    bounds.left -= radius;
                    bounds.bottom -= radius;
                    bounds.right += radius;
                    bounds.top += radius;
                } else {
                    // We assume units == 'degrees' here.
                    // temporarily transform to a SRS with metric coords
                    bounds.transform(
                        mapProjObj,
                        new OpenLayers.Projection("EPSG:900913")
                    );
                    bounds.left -= radius;
                    bounds.bottom -= radius;
                    bounds.right += radius;
                    bounds.top += radius;
                    bounds.transform(
                        new OpenLayers.Projection("EPSG:900913"),
                        mapProjObj
                    );
                }
                z = map.getZoomForExtent(bounds, true);
            }
            map.setCenter(forcedCenter, z);
        }
    };

    /**
     * Method: updateStoreFromWMC
     * Updates the app LayerStore from a given WMC
     *
     * Parameters:
     * wmcUrl - {String} The WMC document URL.
     * options - {Object} an optional object with the following properties:
     *           resetMap - {String} Specifies if resetMap must be passed to
     *                      the GEOR.wmc.read function. Defaults to true.
     *           success - {Function} Callback function to be called once the
     *                      WMC is read.
     *           failure - {Function} Callback function to be called when the
     *                      WMC is not valid.
     *           scope - {Object} the callbacks' scope - defaults to this
     */
    var updateStoreFromWMC = function(wmcUrl, options) {
        options = options || {};
        GEOR.waiter.show();
        var failure = function() {
            GEOR.waiter.hide();
            GEOR.util.infoDialog({
                msg: tr("The provided context is not valid")
            });
            options.failure && options.failure.call(this);
        };
        OpenLayers.Request.GET({
            url: wmcUrl,
            success: function(response) {
                // we need to manually hide the waiter since
                // GEOR.ajaxglobal.init has not run yet:
                GEOR.waiter.hide();
                try {
                    GEOR.wmc.read(response.responseXML || response.responseText, 
                        options.resetMap || true, !customRecenter());

                    options.success && options.success.call(this);

                    zoomToCustomExtent();
                    // and finally we're running our global success callback:
                    cb.call();
                } catch(err) {
                    failure.call(options.scope || this);
                }
            },
            failure: failure,
            scope: options.scope || this
        });
    };

    /**
     * Method: getUniqueWxsServers
     * Convenience method for getting unique WMS server URLs
     *
     * Parameters:
     * initState - {Array} GEOR.initstate array
     *
     * Returns:
     * {Object} a hash with keys "WMSLayer", "WFSLayer", "WFS" and "WMS" indexing arrays of
     *          unique WMS/WFS server URLs
     */
    var getUniqueWxsServers = function(initState) {
        var t = {
            "WMSLayer": [],
            "WMS": [],
            "WFSLayer": [],
            "WFS": []
        };
        Ext.each(initState, function(item) {
            if (item.url && item.type && t[item.type].indexOf(item.url)< 0) {
                t[item.type].push(item.url);
            }
        });
        return t;
    };

    /**
     * Method: updateStoreFromWxS
     * Handles addition of WxS services to map
     *
     * Parameters:
     * stores - {Object} Hash containing stores keyed by server url
     * type - {String} WMS or WFS, depending on the service
     */
    var updateStoreFromWxS = function(stores, type) {
        var recordType = (type === "WMS") ?
            new GeoExt.data.WMSCapabilitiesReader().recordType :
            new GeoExt.data.WFSCapabilitiesReader().recordType;
        recordType.prototype.fields.add(new Ext.data.Field({
            name: "_serverURL", type: "string"
        }));

        var GroupingLayerStore = Ext.extend(
            Ext.data.GroupingStore,
            new GeoExt.data.LayerStoreMixin
        );
        var gls = new GroupingLayerStore({
            fields: recordType,
            groupField: '_serverURL',
            listeners: {
                'add': function(store, records, idx) {
                    Ext.each(records, function(r) {
                        var layer = r.get("layer");
                        if (layer && layer.url) {
                            r.set("_serverURL", layer.url);
                        } else if (layer && layer.protocol && layer.protocol.url) {
                            r.set("_serverURL", layer.protocol.url);
                        }
                    });
                }
            }
        });

        var srs = layerStore.map.getProjection();
        for (var key in stores) {
            if (stores.hasOwnProperty(key)) {
                var records = stores[key].getRange();
                Ext.each(records, function(record) {
                    /**
                     * XXX for WFS we dont check srs since at that point
                     * we have no way to know if it's supported
                     */
                    if ((record.get('layer') instanceof OpenLayers.Layer.Vector) ||
                        (record.get('srs') && record.get('srs')[srs] === true)) {

                        gls.add([record]);
                    }
                });
            }
        }

        var sm = new Ext.grid.CheckboxSelectionModel({
            sortable: true
        });
        var grid = new Ext.grid.GridPanel({
            store: gls,
            columns: [
                sm,
                {header: tr("Server"), sortable: true, dataIndex: '_serverURL'},
                {header: tr("Layer"), sortable: true, dataIndex: 'title'},
                {header: tr("Description"), sortable: false, dataIndex: 'abstract'}
            ],
            view: new Ext.grid.GroupingView({
                forceFit:true,
                hideGroupedColumn: true
            }),
            sm: sm,
            frame: false,
            border: false,
            width: 700,
            height: 450
        });
        var win = new Ext.Window({
            title: tr("Add layers from "+type+" services"),
            layout: 'fit',
            constrainHeader: true,
            closeAction: 'close',
            modal: false,
            items: [grid],
            width: 700,
            buttons: [{
                text: tr("Close"),
                handler: function() {
                    win.close();
                }
            },{
                text: tr("OK"),
                handler: function() {
                    Ext.each(sm.getSelections(), function(r) {
                        layerStore.addSorted(r);
                    });
                    win.close();
                }
            }]
        });
        GEOR.waiter.hide();
        win.show();
    };

    /**
     * Method: updateStoreFromWxSLayer
     * Handles addition of WxS layers to map
     *
     * Parameters:
     * stores - {Object} Hash containing stores keyed by server url
     * type - {String} WMS or WFS, depending on the service
     * (unused because info already present in item.type ?)
     */
    var updateStoreFromWxSLayer = function(stores, type) {
        // extract from stores layers which were initially requested
        var records = [], record;
        var errors = [], count = 0;
        Ext.each(initState, function(item) {
            if ( (item.type == "WMSLayer" || item.type == "WFSLayer") && item.type == type+'Layer' ) {
                record = stores[item.url].queryBy(function(r) {
                    return (r.get('name') == item.name);
                }).first();
                if (record) {
                    // handle cql_filter param in JSON POST
                    if( type == "WFS" ) {
                        if ( item.hasOwnProperty("cql_filter") ) {
                            record.getLayer().filter = (new OpenLayers.Format.CQL()).read(item.cql_filter);
                        }
                    } else {
                        if ( item.hasOwnProperty("cql_filter") ) {
                            record.getLayer().params.CQL_FILTER = item.cql_filter;
                        }
                    }
                    
                    // set metadataURLs in record, data comes from GeoNetwork
                    if (item.metadataURL) {
                        record.set("metadataURLs", [item.metadataURL]);
                    }
                    records.push(record);
                } else {
                    errors.push(item.name);
                }
            }
        });
        
        // support for JSON search parameter
        if( initSearch.hasOwnProperty('typename') && initSearch.hasOwnProperty('owsurl') && initSearch.hasOwnProperty('cql_filter') ) {
            var record = {
                typeName: initSearch.typename,
                owsURL: initSearch.owsurl
            }
            var filter = (new OpenLayers.Format.CQL()).read(initSearch.cql_filter);
            
            var attStore = GEOR.ows.WFSDescribeFeatureType(record, {
                extractFeatureNS: true,
                success: function() {
                    var geometryName;
                    // we list all fields, including the geometry
                    layerFields = attStore.collect('name');
                    // we get the geometry column name
                    var idx = attStore.find('type', GEOR.ows.matchGeomProperty);
                    if (idx > -1) {
                        // we have a geometry
                        var r = attStore.getAt(idx);
                        geometryName = r.get('name');
                        
                        GEOR.ows.WFSProtocol(record, layerStore.map, {geometryName: geometryName}).read({
                            maxFeatures: GEOR.config.MAX_FEATURES,
                            propertyNames: layerFields || [],
                            filter: filter,
                            callback: function(response) {
                                if (!response.success()) {
                                    return;
                                }
                                
                                var model =  (attStore.getCount() > 0) ? new GEOR.FeatureDataModel({
                                    attributeStore: attStore
                                }) : null;
                                
                                observable.fireEvent("searchresults", {
                                    features: response.features,
                                    model: model,
                                    tooltip: initSearch.typename + " - " + tr("WFS GetFeature on filter"),
                                    title: GEOR.util.shortenLayerName(initSearch.typename)
                                });
                            },
                            scope: this
                        });
                    } else {
                        GEOR.util.infoDialog({
                            msg: tr("querier.layer.no.geom")
                        });
                    }
                },
                failure: function() {
                    GEOR.util.errorDialog({
                        msg: tr("querier.layer.error")
                    });
                },
                scope: this
            });
        }

        // check their srs against map's srs
        var srs = layerStore.map.getProjection();
        Ext.each(records, function(record) {
            /**
             * XXX for WFS we dont check srs since at that point
             * we have no way to know if it's supported
             */
            if (record.get('layer') instanceof OpenLayers.Layer.WMS &&
               (!record.get('srs') || (record.get('srs')[srs] !== true))) {

                errors.push(record.get('name'));
                return;
            }
            count += 1;
            layerStore.addSorted(record);
        });
        GEOR.waiter.hide();
        if (errors.length) {
            GEOR.util.errorDialog({
                title: (errors.length>1) ?
                    tr("NB layers not imported", {'NB': errors.length}) :
                    tr("One layer not imported"),
                msg: tr("mapinit.layers.load.error",
                    {'LIST': errors.join(', ')})
            });
        } else {
            var plural = (count>1) ? "s" : "";
            GEOR.util.infoDialog({
                msg: (count>1) ?
                    tr("NB layers imported", {'NB': count}):
                    (count==1) ? tr("One layer imported"):
                    tr("No layer imported")
            });
        }
    };

    /**
     * Method: createStores
     * Method responsible for creating WMSCapabilities stores
     * When all done, executes a given callback
     *
     * Parameters:
     * wxsServers - {Array} Array of WxS server urls
     * callback - {Function} The callback
     *            (which takes a *stores* object as argument)
     */
    var createStores = function(wxsServers, callback, type) {
        var count = wxsServers.length;
        var stores = {};
        var capabilitiesCallback = function() {
            count -= 1;
            if (count === 0) {
                callback(stores, type);
            }
        };
        Ext.each(wxsServers, function(wxsServerUrl) {
            GEOR.waiter.show();
            var u = GEOR.util.splitURL(wxsServerUrl);
            params = {
                storeOptions: {
                    url: u.serviceURL
                },
                baseParams: u.params,
                success: capabilitiesCallback,
                failure: capabilitiesCallback
            };
            if (type == "WMS") {
                stores[wxsServerUrl] = GEOR.ows.WMSCapabilities(params);
            } else { /* WFS */
                /* XXX only for WFS, and gross since we dont know the advertised srs of each available layer.. */
                params.storeOptions.protocolOptions = {
                    srsNameInQuery: true,
                    srsName: layerStore.map.getProjection()
                };
                stores[wxsServerUrl] = GEOR.ows.WFSCapabilities(params);
            }
        });
    };

    /**
     * Method: loadLayers
     * Load WMS layers.
     *
     * Parameters:
     * initState - {Array} GEOR.initstate array
     */
    var loadLayers = function(initState) {
        var wxsServers = getUniqueWxsServers(initState);
        createStores(wxsServers['WMSLayer'], updateStoreFromWxSLayer, "WMS");
        createStores(wxsServers['WMS'], updateStoreFromWxS, "WMS");
        createStores(wxsServers['WFSLayer'], updateStoreFromWxSLayer, "WFS");
        createStores(wxsServers['WFS'], updateStoreFromWxS, "WFS");
    };

    /**
     * Method: loadDefaultWMC
     * Loads the default WMC
     *
     */
    var loadDefaultWMC = function() {
        if (GEOR.ls.get("default_context")) {
            // restore default context
            updateStoreFromWMC(GEOR.ls.get("default_context"));
        } else if (GEOR.ls.get("latest_context")) {
            // restore latest context
            GEOR.wmc.read(GEOR.ls.get("latest_context"), true, !customRecenter());
            zoomToCustomExtent();
            // and finally we're running our global success callback:
            cb.call();
        } else {
            updateStoreFromWMC(GEOR.config.DEFAULT_WMC);
        }
    };

    /**
     * Method: toGeoJSONSuccess
     * Success callback for file to geojson conversion 
     *
     */
    var toGeoJSONSuccess = function(resp) {
        var features,
            fc = (new OpenLayers.Format.JSON()).read(resp.responseText);
        if (!fc) {
            GEOR.util.errorDialog({
                title: tr("Error while loading file"),
                msg: OpenLayers.i18n("Incorrect server response.")
            });
            return;
        } else if (fc.success !== "true") {
            GEOR.util.errorDialog({
                title: tr("Error while loading file"),
                msg: OpenLayers.i18n(fc.error)
            });
            return;
        }
        features = (new OpenLayers.Format.GeoJSON()).read(fc.geojson);
        if (!features || features.length == 0) {
            GEOR.util.errorDialog({
                title: tr("Error while loading file"),
                msg: OpenLayers.i18n("No features found.")
            });
            return;
        }
        var recordType = GeoExt.data.LayerRecord.create(
            GEOR.ows.getRecordFields()
        );
        var filename, 
            cmpts = GEOR.config.CUSTOM_FILE.split('/');
        if (cmpts.length) {
            filename = cmpts[cmpts.length-1];
        } else {
            filename = "geofile";
        }
        var name = GEOR.util.shortenLayerName(filename),
        layer = new OpenLayers.Layer.Vector(name, {
            styleMap: GEOR.util.getStyleMap(),
            rendererOptions: {
                zIndexing: true
            }
        });
        layer.addFeatures(features);
        // we need to manually hide the waiter since
        // GEOR.ajaxglobal.init has not run yet:
        GEOR.waiter.hide();
        layerStore.addSorted(new recordType({
            layer: layer
        }, layer.id));
    };

    return {
        /*
         * Observable object
         */
        events: observable,
        
        /**
         * APIMethod: init
         * Initialize this module
         *
         * Parameters:
         * ls - {GeoExt.data.LayerStore} The layer store instance.
         * callback - {Function} exec. after a WMC has been successfully loaded
         */
        init: function(ls, callback) {
            layerStore = ls;
            tr = OpenLayers.i18n;
            cb = callback || OpenLayers.Util.Void;

            // the default WMC is either the one provided by the admin in GEOR.custom,
            // or the first one publicized by mapfishapp's ContextController.java 
            // in GEOR.config.CONTEXTS
            GEOR.config.DEFAULT_WMC = GEOR.custom.DEFAULT_WMC ||
                // first context publicized by ContextController:
                (GEOR.config.CONTEXTS[0] && GEOR.config.CONTEXTS[0]["wmc"]) ||
                // this last one should not happen
                "context/default.wmc";

            var url;
            // POSTing a content to the app (which results in GEOR.initstate
            // being set) has priority over everything else:
            if (!GEOR.initstate || !GEOR.initstate[0]) {
                // if a custom WMC is provided as GET parameter, load it:
                if (GEOR.config.CUSTOM_WMC) {
                    url = GEOR.config.CUSTOM_WMC;
                    // to recover contexts stored in plain files and indexed with a 19 digits string:
                    if (/^ws\/wmc\/geodoc(\d{19}).wmc$/.test(GEOR.config.CUSTOM_WMC)) {
                        url = GEOR.config.PATHNAME + '/' + url;
                    }
                    // to recover contexts stored in database and indexed with a 32 chars string:
                    if (/^ws\/wmc\/geodoc(\w{32}).wmc$/.test(GEOR.config.CUSTOM_WMC)) {
                        url = GEOR.config.PATHNAME + '/' + url;
                    }
                    updateStoreFromWMC(url, {
                        failure: loadDefaultWMC
                    });
                } else {
                    loadDefaultWMC();
                }
                if (GEOR.config.CUSTOM_FILE) {
                    // load the given file on top of the WMC
                    GEOR.waiter.show();
                    Ext.Ajax.request({
                        method: 'POST',
                        disableCaching: true,
                        url: GEOR.config.PATHNAME + "/ws/togeojson/",
                        params: {
                            "url": GEOR.config.CUSTOM_FILE,
                            "srs": ls.map.getProjection()
                        },
                        success: toGeoJSONSuccess,
                        failure: function(resp) {
                            GEOR.waiter.hide();
                            var fc = (new OpenLayers.Format.JSON()).read(resp.responseText);
                            GEOR.util.errorDialog({
                                title: tr("Error while loading file"),
                                msg: OpenLayers.i18n(fc.error)
                            });
                        },
                        scope: this
                    });
                }
                // Handle the case where OGC layers/servers are being sent via GET parameters...
                // eg ?layername=commune_bdcarto,opendata:carroyage
                // &owstype=WMSLayer,WMSLayer
                // &owsurl=http://geobretagne.fr/geoserver/dreal_b/ows,https://preprod.ppige-npdc.fr/geoserver/ows
                var p = GEOR.util.splitURL(window.location.href).params;
                if (p.hasOwnProperty('LAYERNAME') && p.hasOwnProperty('OWSTYPE')
                    && p.hasOwnProperty('OWSURL')) {
                    // load the given layer on top of the WMC
                    if (Ext.isArray(p.OWSURL) && Ext.isArray(p.OWSTYPE)
                        && Ext.isArray(p.LAYERNAME) && p.OWSURL.length === p.OWSTYPE.length
                        && p.OWSURL.length === p.LAYERNAME.length) {
                        // several layers, eventually from different servers
                        initState = [];
                        Ext.each(p.OWSURL, function(item, idx) {
                            initState.push({
                                "url": p.OWSURL[idx],
                                "type": p.OWSTYPE[idx],
                                "name": p.LAYERNAME[idx]
                            });
                        });
                    } else if (Ext.isString(p.OWSURL) && Ext.isString(p.OWSTYPE)
                        && Ext.isString(p.LAYERNAME)) {
                        // only one layer
                        initState = [{
                            "url": p.OWSURL,
                            "type": p.OWSTYPE,
                            "name": p.LAYERNAME
                        }];
                    } else {
                        // query string error
                        GEOR.util.errorDialog({
                            msg: OpenLayers.i18n("Error while decoding querystring")
                        });
                    }
                    loadLayers(initState);
                }
                return;
            }

            initState = GEOR.initstate;
            initSearch = GEOR.initsearch;
            // Based on GEOR.initstate, determine whether
            // to load WMC or WxS layers or WxS services
            if (initState.length == 1 && initState[0].type == "WMC" &&
                initState[0].url) {
                // load given WMC
                updateStoreFromWMC(initState[0].url, {
                    resetMap: false
                    // we do not need the failure callback,
                    // since resetMap is false:
                    //,failure: loadDefaultWMC
                });
            } else {
                // load default WMC and other layers. We need to make
                // sure the WMC is loaded prior to loading other layers,
                // this is so that the map object and fake base layer are
                // properly configured when adding the other layers
                // to the map
                updateStoreFromWMC(GEOR.config.DEFAULT_WMC, {
                    success: function() {
                        loadLayers(initState);
                    }
                });
            }
        }
    };
})();
