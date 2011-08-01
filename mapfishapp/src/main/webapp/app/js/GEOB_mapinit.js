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

/*
 * @include GEOB_wmc.js
 * @include GEOB_config.js
 * @include GEOB_ows.js
 * @include GEOB_waiter.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/WMSCapabilitiesReader.js
 */

Ext.namespace("GEOB");

GEOB.mapinit = (function() {
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
     * {Array} shorthand for GEOB.initstate
     */
    var initState = null;
    
    /**
     * Method: updateStoreFromWMC
     * Updates the app LayerStore from a given WMC
     *
     * Parameters:
     * wmcUrl - {String} The WMC document URL.
     * resetMap - {String} Specifies if resetMap must be passed to
     *            the GEOB.wmc.read function.
     * callback - {Function} Callback function to be called once the
                  WMC is read.
     */
    var updateStoreFromWMC = function(wmcUrl, resetMap, callback) {
        GEOB.waiter.show();
        OpenLayers.Request.GET({
            url: wmcUrl,
            success: function(response) {
                try {
                    GEOB.wmc.read(response.responseText, resetMap);
                    if (callback) {
                        callback();
                    }
                } catch(err) {
                    GEOB.util.errorDialog({
                        msg: "Le contexte n'est pas valide."
                    });
                }
            }
        });
    };

    /**
     * Method: getUniqueWmsServers
     * Convenience method for getting unique WMS server URLs 
     *
     * Parameters:
     * initState - {Array} GEOB.initstate array
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
            GEOB.util.errorDialog({
                title: errors.length + " couche" + plural + " non importée" + plural,
                msg: "Les couches nommées " + errors.join(', ') + 
                    " n'ont pas pu être chargées : SRS incompatible ou couche non existante"
            });
        } else {
            var plural = (count>1) ? "s" : "";
            GEOB.util.infoDialog({
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
        GEOB.waiter.show();
        
        var capabilitiesCallback = function() {
            count -= 1;
            if (count === 0) {
                callback(stores);
            }
        };
        Ext.each(wmsServers, function(wmsServerUrl) {
            stores[wmsServerUrl] = new GEOB.ows.WMSCapabilities({
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
     * initState - {Array} GEOB.initstate array
     */
    var loadLayers = function(initState) {
        var wmsServers = getUniqueWmsServers(initState);
        createStores(wmsServers['WMSLayer'], updateStoreFromWMSLayer);
        createStores(wmsServers['WMS'], updateStoreFromWMS);
    };

    return {
    
        /**
         * APIMethod: init
         * Initialize this module 
         *
         * Parameters:
         * ls - {GeoExt.data.LayerStore} The layer store instance.
         */
        init: function(ls) {
            layerStore = ls;

            if (!GEOB.initstate || GEOB.initstate === null || 
                !GEOB.initstate[0]) {
                // load default WMC
                updateStoreFromWMC(GEOB.config.DEFAULT_WMC, true);
                return;
            }
            initState = GEOB.initstate;
            
            // determine whether to load WMC or WMS layers or WMS services

            if (initState.length == 1 && initState[0].type == "WMC" && 
                initState[0].url) {
                // load given WMC
                updateStoreFromWMC(initState[0].url);
            } else {
                // load default WMC and other layers. We need to make
                // sure the WMC is loaded prior to loading other layers,
                // this is so the map object and fake base layer are
                // properly configured when adding the other layers
                // to the map
                updateStoreFromWMC(
                    GEOB.config.DEFAULT_WMC, true,
                    function() {
                        loadLayers(initState);
                    }
                );
            }
        }
    };
})();
