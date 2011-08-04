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
 * @include GeoExt/data/LayerRecord.js
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
                msg: "Le contexte fourni n'est pas valide."
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
                    GEOR.wmc.read(response.responseText, options.resetMap || true);
                    options.success && options.success.call(this);
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
            GeoExt.data.LayerStoreMixin
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
                {header: "Serveur", sortable: true, dataIndex: '_serverURL'},
                {header: "Couche", width: 50, sortable: true, dataIndex: 'name'},
                {header: "Description", sortable: true, dataIndex: 'title'}
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
            title: 'Ajouter des couches depuis des services WMS',
            layout: 'fit',
            closeAction: 'close',
            modal: true,
            items: [grid],
            buttons: [{
                text: 'Fermer',
                handler: function() {
                    win.close();
                }
            },{
                text: 'OK',
                handler: function() {
                    Ext.each(sm.getSelections(), function(r) {
                        layerStore.addSorted(r);
                    });
                    win.close();
                }
            }]
        });
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
        if (errors.length) {
            var plural = (errors.length>1) ? "s" : "";
            GEOR.util.errorDialog({
                title: errors.length + " couche" + plural + " non importée" + plural,
                msg: "Les couches nommées " + errors.join(', ') + 
                    " n'ont pas pu être chargées : SRS incompatible ou couche non existante"
            });
        } else {
            var plural = (count>1) ? "s" : "";
            GEOR.util.infoDialog({
                msg: count + " couche" + plural + " importée" + plural
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
        GEOR.waiter.show();
        
        var capabilitiesCallback = function() {
            count -= 1;
            if (count === 0) {
                callback(stores);
            }
        };
        Ext.each(wmsServers, function(wmsServerUrl) {
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
            alert("Le contexte par défaut n'est pas défini "+
                  "(et ce n'est pas du tout normal !)");
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
