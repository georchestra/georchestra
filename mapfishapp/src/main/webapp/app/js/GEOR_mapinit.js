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
 * @include OpenLayers/Projection.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/LayerStore.js
 * @include GeoExt/data/WMSCapabilitiesReader.js
 */

Ext.namespace("GEOR");

GEOR.mapinit = (function() {
    /*
     * Private
     */

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
                msg: tr("The provided context is not valid.")
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
                        options.resetMap || true, GEOR.config.CUSTOM_BBOX == '');

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
     * Method: getUniqueWmsServers
     * Convenience method for getting unique WMS server URLs
     *
     * Parameters:
     * initState - {Array} GEOR.initstate array
     *
     * Returns:
     * {Object} a hash with keys "WMSLayer" and "WMS" indexing arrays of
     *          unique WMS server URLs
     */
    var getUniqueWmsServers = function(initState) {
        var t = {
            "WMSLayer": [],
            "WMS": []
        };
        Ext.each(initState, function(item) {
            if (item.url && item.type && t[item.type].indexOf(item.url)< 0) {
                t[item.type].push(item.url);
            }
        });
        return t;
    };

    /**
     * Method: updateStoreFromWMS
     * Handles addition of WMS services to map
     *
     * Parameters:
     * stores - {Object} Hash containing stores keyed by server url
     */
    var updateStoreFromWMS = function(stores) {
        var recordType = new GeoExt.data.WMSCapabilitiesReader().recordType;
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
                    if (record.get('srs') && (record.get('srs')[srs] === true)) {
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
                {header: tr("Layer"), width: 50, sortable: true, dataIndex: 'name'},
                {header: tr("Description"), sortable: true, dataIndex: 'title'}
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
            title: tr("Add layers from WMS services"),
            layout: 'fit',
            constrainHeader: true,
            closeAction: 'close',
            modal: false,
            items: [grid],
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
     * Method: updateStoreFromWMSLayer
     * Handles addition of WMS layers to map
     *
     * Parameters:
     * stores - {Object} Hash containing stores keyed by server url
     */
    var updateStoreFromWMSLayer = function(stores) {
        // extract from stores layers which were initially requested
        var records = [], record;
        var errors = [], count = 0;
        Ext.each(initState, function(item) {
            if (item.type == "WMSLayer") {
                record = stores[item.url].queryBy(function(r) {
                    return (r.get('name') == item.name);
                }).first();
                if (record) {
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

        // check their srs against map's srs
        var srs = layerStore.map.getProjection();
        Ext.each(records, function(record) {
            if(!record.get('srs') || (record.get('srs')[srs] !== true)) {
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
                    {'list': errors.join(', ')})
            });
        } else {
            var plural = (count>1) ? "s" : "";
            GEOR.util.infoDialog({
                msg: (count>1) ?
                    tr("NB layers imported", {'NB': count}):
                    (count==1) ? tr("One layer imported"):
                    tr("Not any layer imported")
            });
        }
    };

    /**
     * Method: createStores
     * Method responsible for creating WMSCapabilities stores
     * When all done, executes a given callback
     *
     * Parameters:
     * wmsServers - {Array} Array of WMS server urls
     * callback - {Function} The callback
     *            (which takes a *stores* object as argument)
     */
    var createStores = function(wmsServers, callback, scope) {
        var count = wmsServers.length;
        var stores = {};

        var capabilitiesCallback = function() {
            count -= 1;
            if (count === 0) {
                callback(stores);
            }
        };
        Ext.each(wmsServers, function(wmsServerUrl) {
            GEOR.waiter.show();
            stores[wmsServerUrl] = new GEOR.ows.WMSCapabilities({
                storeOptions: {
                    url: wmsServerUrl
                },
                success: capabilitiesCallback,
                failure: capabilitiesCallback
            });
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
        var wmsServers = getUniqueWmsServers(initState);
        createStores(wmsServers['WMSLayer'], updateStoreFromWMSLayer);
        createStores(wmsServers['WMS'], updateStoreFromWMS);
    };

    /**
     * Method: loadDefaultWMC
     * Load the default WMC
     *
     */
    var loadDefaultWMC = function() {
        GEOR.waiter.hide();
        if (GEOR.config.DEFAULT_WMC) {
            updateStoreFromWMC(GEOR.config.DEFAULT_WMC);
        } else {
            // this should never happen:
            alert(tr("The default context is not defined (and it is a BIG problem!)"));
        }
    };

    return {

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

            // POSTing a content to the app (which results in GEOR.initstate
            // being set) has priority over everything else:
            if (!GEOR.initstate || GEOR.initstate === null ||
                !GEOR.initstate[0]) {
                // if a custom WMC is provided as GET parameter, load it:
                if (GEOR.config.CUSTOM_WMC) {
                    updateStoreFromWMC(GEOR.config.CUSTOM_WMC, {
                        failure: loadDefaultWMC
                    });
                } else {
                    loadDefaultWMC();
                }
                return;
            }

            initState = GEOR.initstate;
            // Based on GEOR.initstate, determine whether
            // to load WMC or WMS layers or WMS services
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
