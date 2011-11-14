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
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include GEOR_cswbrowser.js
 * @include GEOR_wmsbrowser.js
 * @include GEOR_wfsbrowser.js
 */

Ext.namespace("GEOR");

GEOR.layerfinder = (function() {

    /*
     * Private
     */
    
    
    /**
     * Property: layerStore
     * {GeoExt.data.LayerStore} a reference to the application layer store
     */
    var layerStore = null;
    
    /**
     * Property: currentTab
     * {String} a local cache of the currently active tab 
     * (one of "csw", "wms", "wfs")
     */
    var currentTab = "csw";
    
    /**
     * Property: panels
     * {Object} referencing the panels in the tabPanel.
     */
    var panels = {
        "csw": null,
        "wms": null,
        "wfs": null
    };
    
    /**
     * Property: selectedRecords
     * {Object} referencing the records selected in each panel
     */
    var selectedRecords = {
        "csw": [],
        "wms": [],
        "wfs": []
    };
    
    /**
     * Property: addButton
     * {Ext.Button} a reference to the "add" button
     */
    var addButton = null;

    /**
     * Method: createTabPanel
     * Return the main tab panel.
     *
     * Returns:
     * {Ext.TabPanel}
     */
    var createTabPanel = function() {
        
        var selectionChangedListener = function(tab) {
            return function(records) {
                selectedRecords[tab] = records;
                if (records.length) {
                    addButton.enable();
                } else {
                    addButton.disable();
                }
            };
        };
        GEOR.cswbrowser.events.on({
            "selectionchanged": selectionChangedListener.call(this, "csw")
        });
        GEOR.wmsbrowser.events.on({
            "selectionchanged": selectionChangedListener.call(this, "wms")
        });
        GEOR.wfsbrowser.events.on({
            "selectionchanged": selectionChangedListener.call(this, "wfs")
        });
        
        panels["csw"] = GEOR.cswbrowser.getPanel({
            tabTip: "Trouvez des couches par mots clés"
        });
        var mapSRS = layerStore.map.getProjection();
        panels["wms"] = GEOR.wmsbrowser.getPanel({
            srs: mapSRS,
            tabTip: "Trouvez des couches en interrogeant des serveurs WMS"
        });
        panels["wfs"] = GEOR.wfsbrowser.getPanel({
            srs: mapSRS,
            tabTip: "Trouvez des couches en interrogeant des serveurs WFS"
        });
        
        return new Ext.TabPanel({
            border: false,
            activeTab: 0,
            // required for WMS & WFS panels to have correct layout:
            deferredRender: true,
            items: [panels["csw"], panels["wms"], panels["wfs"]],
            listeners: {
                'tabchange': function (tp, p) {
                    switch (p) {
                    case panels["csw"]:
                        currentTab = "csw";
                        break;
                    case panels["wms"]:
                        currentTab = "wms";
                        break;
                    case panels["wfs"]:
                        currentTab = "wfs";
                        break;
                    }
                    if (selectedRecords[currentTab].length>0) {
                        addButton.enable();
                    } else {
                        addButton.disable();
                    }
                }
            }
        });
    };

    /**
     * Method: capabilitiesSuccess
     * Success callback for the WMS capabilities request issued 
     * when adding layers from the catalog tab
     *
     * Parameters:
     * record - {GeoExt.data.LayerRecord}
     */
    // TODO : factorize & centralize this code on layer added in application layerStore
    var capabilitiesSuccess = function(record) {
        var data = record.data;
        var layerName = record.get('name');
        return function(store, records) {
            var index = store.find("name", layerName);
            if(index < 0) {
                GEOR.util.errorDialog({
                    msg: "La couche "+layerName+" n'a pas été trouvée "+
                        "dans le service WMS.<br/><br/>"+
                        "Peut-être n'avez-vous pas le droit d'y accéder "+
                        "ou alors cette couche n'est plus disponible."
                });
                return;
            }
            var r = records[index];
            var srs = layerStore.map.getProjection();
            if(!r.get('srs') || (r.get('srs')[srs] !== true)) {
                GEOR.util.errorDialog({
                    msg: "La projection de la couche n'est pas compatible."
                });
                return;
            }
            // Set the copyright information to the "attribution" field
            if (data.rights && !r.get("attribution")) {
                r.set("attribution", {title: data.rights});
            }
            // If we have a metadataURL coming from the catalog,
            // we use it instead of the one we get from the capabilities
            // (as asked by Lydie - see http://csm-bretagne.fr/redmine/issues/1599#note-5)
            if (data.metadataURL) {
                r.set("metadataURLs", [data.metadataURL]);
            }
            layerStore.addSorted(r);
        };
    };

    /**
     * Method: describeFeaturetypeSuccess
     * Success callback for the WFS DescribeFeaturetype request issued 
     * when adding layers from the "WFS layers" tab
     *
     * Parameters:
     * record - {GeoExt.data.LayerRecord}
     */
    var describeFeaturetypeSuccess = function(record) {
        var layer = record.get('layer');
        return function(store, records) {
            // find geometry column name 
            var idx = store.find('type', GEOR.ows.matchGeomProperty);
            if (idx > -1) {
                // we have a geometry
                var r = store.getAt(idx);
                //record.set('geometryName', r.get('name')); // later on ?
                layer.protocol.setGeometryName(r.get('name'));
                layerStore.addSorted(record.clone());
            } else {
                GEOR.util.errorDialog({
                    msg: "La couche "+p.featureType+" ne possède pas de colonne géométrique valide."
                });
            }
        };
    };
    
    /**
     * Method: addSelectedLayers
     * Adds the selected OGC layers to the given layerStore.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application layer store.
     */
    var addSelectedLayers = function() {
        var records = selectedRecords[currentTab];
        var recordsToAdd = [];
        // TODO here: we miss GEOR.waiter.show()
        // The pb is that it would be hidden on first XHR success.
        // => we have to implement this properly with a counter.
        
        // we need to clone the layers
        for(var i=0, len=records.length; i<len; i++) {
            var record = records[i];
            if(record instanceof GeoExt.data.LayerRecord) {
                // we're coming from the WMS or WFS tab
                var layer = record.get("layer");
                if (layer instanceof OpenLayers.Layer.WMS) {
                    // WMS layer just need cloning 
                    // (well, for the moment - see http://csm-bretagne.fr/redmine/issues/1996)
                    recordsToAdd.push(record.clone());
                } else {
                    // WFS layers need cloning of protocol (and strategy) too ?
                    
                    // For WFS layers, we need to get more information 
                    // (typically the geometry name)
                    // from WFS DescribeFeatureType
                    var p = layer.protocol;
                    GEOR.ows.WFSDescribeFeatureType({
                        typeName: p.featureType,
                        owsURL: p.url
                    },{
                        success: describeFeaturetypeSuccess.call(this, record),
                        failure: function() {
                            GEOR.util.errorDialog({
                                msg: "La requête WFS DescribeFeatureType vers "+
                                    p.url+" pour la couche "+p.featureType+" a malheureusement échoué"
                            });
                        },
                        scope: this
                    });
                }
            } else if(record.get("name")) {
                // we're coming from the CSW tab
                // convert records to layer records
                var store = new GEOR.ows.WMSCapabilities({
                    storeOptions: {
                        url: record.data.wmsurl
                    },
                    success: capabilitiesSuccess.call(this, record),
                    failure: function() {
                        GEOR.util.errorDialog({
                            msg: "La requête WMS getCapabilities vers "+
                                record.data.wmsurl+" a malheureusement échoué"
                        });
                    }
                });
            }
        }
        Ext.each(recordsToAdd, function(r) {
            layerStore.addSorted(r);
        });
    };

    /*
     * Public
     */
    return {
        
        /**
         * APIMethod: create
         * Return the window for layers adding management.
         *
         * Parameters:
         * ls - {GeoExt.data.LayerStore} The application layer store.
         * animateFrom - {String} Id or element from which the window 
         *  should animate while opening
         *
         * Returns:
         * {Ext.Window}
         */
        create: function(ls, animateFrom) {
            layerStore = ls;
            addButton = new Ext.Button({
                text: 'Ajouter',
                disabled: true,
                handler: function() {
                    addSelectedLayers();
                    switch (currentTab) {
                    case "csw":
                        GEOR.cswbrowser.clearSelection();
                        break;
                    case "wms":
                        GEOR.wmsbrowser.clearSelection();
                        break;
                    case "wfs":
                        GEOR.wfsbrowser.clearSelection();
                        break;
                    default:
                        break;
                    }
                    win.hide();
                },
                scope: this
            });
            var win = new Ext.Window({
                title: 'Ajouter des couches',
                constrainHeader: true,
                layout: 'fit',
                animateTarget: GEOR.config.ANIMATE_WINDOWS && animateFrom,
                width: 650,
                height: 450,
                closeAction: 'hide',
                modal: false,
                items: createTabPanel(),
                buttons: [
                    addButton,
                    {
                        text: 'Annuler',
                        handler: function() {
                            win.hide();
                        }
                    }
                ]
            });
            return win;
        }
    };
})();


/**
 * A customized TwinTriggerField, currently used in keyword xlink search
 * Taken from the Extjs examples and adapted (translations)
 */

Ext.app.OWSUrlField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function() {
        Ext.app.OWSUrlField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e) {
            if (e.getKey() == e.ENTER) {
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent: false,
    validateOnBlur: false,
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-search-trigger',
    hideTrigger1: true,
    width: 180,
    hasSearch: false,
    paramName: 'query',
    
    cancelRequest: function() {
        var proxy = this.store.proxy;
        var conn = proxy.getConnection();
        if (conn.isLoading()) {
            conn.abort();
        }
        this.store.fireEvent("exception");
    },

    onTrigger1Click: function() {
        this.cancelRequest();
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
            // conf
            var conf = Ext.get('conf');
            if (conf) {
                conf.enableDisplayMode().show();
            }
        }
    },

    onTrigger2Click: function() {
        this.cancelRequest();
        
        // trim raw value:
        var url = this.getRawValue().replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (url.length < 1) {
            this.onTrigger1Click();
            return;
        }
        if (!GEOR.util.isUrl(url)) {
            GEOR.util.errorDialog({
                msg: "URL non conforme."
            });
            return;
        }
        // update url for OWS getCapabilities request
        this.store.proxy.setUrl(url);
        this.store.load({
            callback: this.callback,
            scope: this,
            add: false
        });

        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
        // conf
        var conf = Ext.get('conf');
        if (conf) {
            conf.enableDisplayMode().hide();
        }
    }
});
