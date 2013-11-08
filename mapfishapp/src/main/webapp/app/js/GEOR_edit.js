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
 * @include OpenLayers/Control/GetFeature.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Strategy/Save.js
 * @include GeoExt.ux/FeatureEditorGrid.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.edit = (function() {

    /*
     * Private
     */

    /**
     * Property: getfeature
     * {OpenLayers.Control.GetFeature}
     */
    var getFeature;

    /**
     * Property: selectFeature
     * {OpenLayers.Control.SelectFeature}
     */
    var selectFeature;

    /**
     * Property: vectorLayer
     * {OpenLayers.Layer.Vector}
     */
    var vectorLayer;

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    var map;

    /**
     * Property: strategy
     * {OpenLayers.Strategy.Save}
     */
    var strategy;

    /**
     * Property: menuItem
     * {Ext.menu.Item} The menu item for the current layer in edition
     */
    var menuItem;

    var tr, win;

    return {
    
        /*
         * Observable object
         */
        //events: observable,
        
        /**
         * APIMethod: init
         * Initialize this module 
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */
        init: function(m) { 
            map = m;
            tr = OpenLayers.i18n;
        },
        
        /*
         * Method: activate
         *
         * Parameters:
         * options - {Object}
         */
        activate: function(options) {
            GEOR.edit.deactivate();
            menuItem = options.menuItem;
            menuItem.setText(tr("Stop editing"));
            getFeature = new OpenLayers.Control.GetFeature({
                protocol: options.protocol,
                autoActivate: true, // Do not forget to manually deactivate it !
                multiple: false,
                hover: true,
                click: false,
                single: true,
                maxFeatures: 1,
                clickTolerance: 5,
                eventListeners: {
                    "hoverfeature": function(e) {
                        vectorLayer.removeFeatures(vectorLayer.features[0]);
                        vectorLayer.addFeatures([e.feature], {
                            silent: true // we do not want to trigger save on feature added
                        });
                    }
                }
            });
            strategy = new OpenLayers.Strategy.Save({
                //auto: true,
                autoDestroy: true
            });
            strategy.events.on({
                "start": function() {
                    GEOR.waiter.show();
                },
                "success": function() {
                    options.layer.mergeNewParams({
                        nocache: new Date().valueOf()
                    });
                    win.close();
                    vectorLayer.destroyFeatures();
                    // reactivate getFeature control: (to go on with editing)
                    getFeature.activate();
                },
                "fail": function() {
                    GEOR.util.errorDialog({
                        msg: OpenLayers.i18n('Synchronization failed.')
                    });
                    // TODO: handle this case
                }
            });
            vectorLayer = new OpenLayers.Layer.Vector("_geor_edit", {
                displayInLayerSwitcher: false,
                protocol: options.protocol,
                strategies: [strategy],
                eventListeners: {
                    "featureselected": function(o) {
                        // we do not want to trigger additional useless XHRs 
                        // once one feature has been chosen for edition:
                        getFeature.deactivate();
                        // we have to unselect it because 
                        // the modifyFeatureControl will standalone select it.
                        selectFeature.unselect(o.feature);
                        // sync feature values to store
                        var store = options.store, 
                        a = o.feature.attributes;
                        store.each(function(r) {
                            r.set("value", a[r.get("name")]);
                        });
                        store.feature = o.feature;
                        store.bind.call(store);
                        // display Editor Grid
                        var editorGrid = new GeoExt.ux.FeatureEditorGrid({
                            store: store,
                            forceValidation: true,
                            trackMouseOver: true,
                            allowSave: true,
                            allowCancel: true,
                            allowDelete: true,
                            border: false,
                            hideHeaders: true,
                            viewConfig: {
                                forceFit: true,
                                scrollOffset: 2 // the grid will never have scrollbars
                            },
                            listeners: {
                                "done": function(panel, e) {
                                    var feature = e.feature, modified = e.modified;
                                    if(feature.state != null) {
                                        strategy.save();
                                    }
                                },
                                "cancel": function(panel, e) {
                                    var feature = e.feature, modified = e.modified;
                                    panel.cancel();
                                    win.close();
                                    // reactivate getFeature control: (to go on with editing)
                                    getFeature.activate();
                                    // we call cancel() ourselves so return false here
                                    return false;
                                }
                            }
                        });
                        win = new Ext.Window({
                            title: tr('Feature attributes'),
                            width: 440,
                            height: 350,
                            closable: true,
                            closeAction: "close",
                            resizable: true,
                            border: true,
                            layout: 'fit',
                            items: [editorGrid],
                            listeners: {
                                "hide": function() {

                                },
                                "show": function() {

                                },
                                scope: this
                            }
                        });
                        win.show();
                    }
                }
            });
            selectFeature = new OpenLayers.Control.SelectFeature(vectorLayer, {
                autoActivate: true, // Do not forget to manually deactivate it !
                multiple: false,
                clickout: true,
                toggle: false,
                hover: false,
                highlightOnly: false,
                box: false
            });
            map.addLayer(vectorLayer);
            map.addControls([getFeature, selectFeature]);
        },

        /*
         * Method: deactivate
         */
        deactivate: function() {
            if (selectFeature && getFeature) {
                selectFeature.deactivate();
                getFeature.deactivate();
                // will take care of removing them from map:
                selectFeature.destroy();
                getFeature.destroy();
                selectFeature = null;
                getFeature = null;
            }
            // will take care of destroying protocol 
            // and strategy in correct order, 
            // unregistering listeners, removing from map:
            if (vectorLayer) {
                vectorLayer.destroy();
                vectorLayer = null;
                strategy = null;
            }
            // update layer menu item text
            if (menuItem) {
                menuItem.setText(tr("Edit this layer"));
                menuItem = null;
            }
        }
    };
})();
