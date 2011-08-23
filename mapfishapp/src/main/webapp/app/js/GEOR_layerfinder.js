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
     * Property: layerStore
     * {GeoExt.data.LayerStore} a reference to the application layer store
     */
    var layerStore = null;
    
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
     * Method: capabilitiesSuccess
     * Success callback for the WMS capabilities request issued 
     * when adding layers from the catalog tab
     *
     * Parameters:
     * layerName - {String}
     */
    // TODO : factorize & centralize this code on layer added in application layerStore
    var capabilitiesSuccess = function(layerName) {
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
            layerStore.addSorted(r);
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
                    success: capabilitiesSuccess.call(this, record.get("name")),
                    failure: function() {
                        GEOR.util.errorDialog({
                            msg: "La requête WMS getCapabilities vers "+
                                data.wmsurl+" a malheureusement échoué"
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
         *
         * Returns:
         * {Ext.Window}
         */
        create: function(ls) {
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