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
 * @include GEOR_util.js
 * @include GEOR_cswbrowser.js
 * @include GEOR_wmsbrowser.js
 */

Ext.namespace("GEOR");

GEOR.layerfinder = (function() {

    /*
     * Private
     */
    
    /**
     * Property: currentTab
     * {String} a local cache of the currently active tab 
     * (one of "welcome", "csw", "wms")
     */
    var currentTab = "welcome";
    
    /**
     * Property: panels
     * {Object} referencing the panels in the tabPanel.
     */
    var panels = {
        "csw": null,
        "wms": null
    };
    
    /**
     * Property: selectedRecords
     * {Object} referencing the records selected in each panel
     */
    var selectedRecords = {
        "csw": [],
        "wms": []
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
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application layer store.
     *
     * Returns:
     * {Ext.TabPanel}
     */
    var createTabPanel = function(layerStore) {
        
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
        
        panels["csw"] = GEOR.cswbrowser.getPanel();
        panels["wms"] = GEOR.wmsbrowser.getPanel({
            srs: layerStore.map.getProjection()
        });
        
        return new Ext.TabPanel({
            border: false,
            activeTab: 0,
            deferredRender: true, // required for WMS panel to have correct layout
            items: [panels["csw"], panels["wms"]],
            listeners: {
                'tabchange': function (tp, p) {
                    switch (p) {
                    case panels["csw"]:
                        currentTab = "csw";
                        break;
                    case panels["wms"]:
                        currentTab = "wms";
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
     * Method: addSelectedLayers
     * Adds the selected OGC layers to the given layerStore.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application layer store.
     */
    var addSelectedLayers = function(layerStore) {
        var records = selectedRecords[currentTab];
        var recordsToAdd = [];
        // we need to clone the layers
        for(var i=0, len=records.length; i<len; i++) {
            var record = records[i];
            if(record instanceof GeoExt.data.LayerRecord) {
                // we're coming from the WMS tab
                recordsToAdd.push(record.clone());
            } else if(record.get("name")) {
                // we're coming from the CSW tab
                // convert records to layer records
                var data = record.data;
                var store = new GEOR.ows.WMSCapabilities({
                    storeOptions: {
                        url: data.wmsurl
                    },
                    success: function(store, records) { // TODO: JSHINT says: "Don't make functions within a loop."
                        var index = store.find("name", this.layerName);
                        if(index < 0) {
                            GEOR.util.errorDialog({
                                msg: "La couche n'a pas été trouvée dans le service WMS.<br/>" +
                                     "Peut-être n'avez-vous pas le droit d'accéder à cette couche ou alors la couche n'est plus disponible."
                            });
                            return;
                        }
                        var r = records[index];
                        var srs = this.layerStore.map.getProjection();
                        if(!r.get('srs') || (r.get('srs')[srs] !== true)) {
                            GEOR.util.errorDialog({
                                msg: "La projection de la couche n'est pas compatible."
                            });
                            return;
                        }
                        // set the copyright information to the "attribution" field
                        // TODO: check all this works again after OpenLayers has been upgraded !
                        if (data.rights && !r.get("attribution")) {
                            r.set("attribution", {title: data.rights});
                        }
                        // if we have a metadataURL coming from the catalog,
                        // we use it instead of the one we get from the capabilities
                        // (as asked by Lydie - see http://csm-bretagne.fr/redmine/issues/1599#note-5)
                        if (data.metadataURL) {
                            r.set("metadataURLs", [data.metadataURL]);
                        }
                        this.layerStore.addSorted(r);
                    },
                    scope: {
                        layerStore: layerStore,
                        layerName: record.get("name")
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
         * layerStore - {GeoExt.data.LayerStore} The application layer store.
         *
         * Returns:
         * {Ext.Window}
         */
        create: function(layerStore) {
            addButton = new Ext.Button({
                text: 'Ajouter',
                disabled: true,
                handler: function() {
                    addSelectedLayers(layerStore);
                    switch (currentTab) {
                    case "csw":
                        GEOR.cswbrowser.clearSelection();
                        break;
                    case "wms":
                        GEOR.wmsbrowser.clearSelection();
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
                layout: 'fit',
                width: 650,
                height: 450,
                closeAction: 'hide',
                modal: false,
                items: createTabPanel(layerStore),
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