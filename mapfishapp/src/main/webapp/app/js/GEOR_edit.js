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
 * @requires GeoExt/data/AttributeStore.js
 * @include GeoExt/widgets/Popup.js
 * @include GeoExt.ux/FeatureEditorGrid.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

// missing piece in AttributeStore:
GeoExt.data.AttributeStore.prototype.unbind = function() {
    this.un("update", this.onUpdate, this);
    this.un("load", this.onLoad, this);
    this.un("add", this.onAdd, this);
};

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
                clickTolerance: 7,
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
                styleMap: GEOR.util.getStyleMap(),
                protocol: options.protocol,
                strategies: [strategy],
                eventListeners: {
                    "featureselected": function(o) {
                        // we do not want to trigger additional useless XHRs 
                        // once one feature has been chosen for edition:
                        getFeature.deactivate();
                        // we have to unselect the feature because 
                        // the modifyFeatureControl will standalone select it.
                        // we also have to deactivate the control, which will do both:
                        selectFeature.deactivate(); // calls unselect(o.feature) before
                        // sync feature values to store
                        var store = options.store, 
                        a = o.feature.attributes;
                        store.each(function(r) {
                            r.set("value", a[r.get("name")]);
                            // reset the dirty flag:
                            r.commit(true); // equivalent to r.dirty = false;
                        });
                        store.feature = o.feature;
                        store.bind.call(store);
                        // display Editor Grid
                        var editorGrid = new GeoExt.ux.FeatureEditorGrid({
                            store: store,
                            forceValidation: true,
                            trackMouseOver: true,
                            width: 400,
                            cls: "editorgrid",
                            allowSave: true,
                            allowCancel: true,
                            allowDelete: true,
                            border: false,
                            hideHeaders: false,
                            // make the value column the biggest:
                            autoExpandColumn: "value",
                            extraColumns: [{
                                header: tr("Type"),
                                dataIndex: "type",
                                width: 50,
                                fixed: true,
                                menuDisabled: true,
                                sortable: true,
                                renderer: function(v) {
                                    var r = v.split(":");
                                    return tr(r.pop());
                                }
                            }, {
                                header: tr("Nillable"),
                                dataIndex: "nillable",
                                width: 50,
                                fixed: true,
                                menuDisabled: true,
                                sortable: true,
                                renderer: function(v) {
                                    return v ? 
                                        '<img src="app/img/famfamfam/tick.gif" style="width:12px;height:12px;" alt="' + tr("Yes") + '">' : 
                                        '<img src="app/img/nope.gif" style="width:12px;height:12px;" alt="' + tr("No") + '">';
                                }
                            }],
                            modifyControlOptions: {
                                clickout: false,
                                toggle: false,
                                mode: OpenLayers.Control.ModifyFeature.RESHAPE | 
                                    OpenLayers.Control.ModifyFeature.DRAG
                            },
                            viewConfig: {
                                forceFit: true
                            },
                            listeners: {
                                "done": function(panel, e) {
                                    var feature = e.feature, modified = e.modified;
                                    if (feature.state != null) {
                                        strategy.save();
                                    }
                                },
                                "cancel": function(panel, e) {
                                    var feature = e.feature, modified = e.modified;
                                    // closing window will cause destroy of it and associated components, 
                                    // including editorGrid:
                                    win.close();
                                    // we call cancel() ourselves so return false here
                                    return false;
                                }
                            }
                        });
                        win = new GeoExt.Popup({
                            location: store.feature,
                            title: tr('Feature attributes'),
                            height: 250,
                            panIn: false,
                            border: true,
                            anchorPosition: "top-left",
                            collapsible: false,
                            closable: true,
                            closeAction: "close",
                            resizable: true,
                            unpinnable: true,
                            constrainHeader: true,
                            layout: 'fit',
                            items: [editorGrid],
                            listeners: {
                                "destroy": function() {
                                    // called after close:
                                    store.unbind.call(store);
                                    vectorLayer.removeAllFeatures();
                                },
                                "close": function() {
                                    if (store.feature && store.feature.layer) {
                                        editorGrid.cancel();
                                        // cancel() makes the assumption that feature.layer is defined
                                        // which is not the case here when the feature is deleted.
                                    }
                                    // reactivate getFeature control: (to go on with editing)
                                    getFeature.activate();
                                    selectFeature.activate();
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
            // to allow map panning while on feature:
            selectFeature.handlers.feature.stopDown = false;
            map.addLayer(vectorLayer);
            map.addControls([getFeature, selectFeature]);
        },

        /*
         * Method: deactivate
         */
        deactivate: function() {
            if (win) {
                win.close();
            }
            if (selectFeature && getFeature) {
                // remove the 2 lines below when
                // http://trac.openlayers.org/ticket/2210 is fixed
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
