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
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Control/Snapping.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Polygon.js
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
     * Property: drawFeature
     * {OpenLayers.Control.DrawFeature}
     */
    var drawFeature;

    /**
     * Property: snap
     * {OpenLayers.Control.Snapping}
     */
    var snap;

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
     * Property: splitButton
     * {Ext.SplitButton} The edition button
     */
    var splitButton;

    var tr, win, geomType, roGeometry, multiGeom;

    return {
        
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
            GEOR.helper.msg(
                tr("Edit activated"),
                tr("Hover the feature you wish to edit, or choose \"new feature\" in the edit menu"),
                5
            );
            map.events.register("preremovelayer", this, function(o) {
                if (o.layer.id === options.layerRecord.id) {
                    GEOR.edit.deactivate();
                }
            });
            splitButton = options.splitButton;
            splitButton.el.addClass("now-editing");
            splitButton.setText(tr("Editing"));
            geomType = options.layerRecord.get("geometryType"); // Line, Point, Polygon
            multiGeom = options.layerRecord.get("multiGeometry"); // Boolean
            roGeometry = options.roGeometry || false;
            getFeature = new OpenLayers.Control.GetFeature({
                protocol: options.protocol,
                autoActivate: true, // Do not forget to manually deactivate it !
                multiple: false,
                hover: true,
                click: false,
                single: true,
                clickTolerance: 7,
                eventListeners: {
                    "hoverfeature": function(e) {
                        vectorLayer.removeFeatures(vectorLayer.features[0]);
                        vectorLayer.addFeatures([e.feature], {
                            silent: true // we do not want to trigger save on feature added
                        });
                    },
                    "outfeature": function(e) {
                        vectorLayer.removeAllFeatures();
                    },
                    "deactivate": function() {
                        // hack to enable selection of same feature in control's hoverSelect():
                        getFeature.hoverFeature = null;
                        // (this is an OpenLayers bug)
                    }
                }
            });
            strategy = new OpenLayers.Strategy.Save({
                autoDestroy: true
            });
            strategy.events.on({
                "start": function() {
                    GEOR.waiter.show();
                },
                "success": function() {
                    options.layerRecord.getLayer().mergeNewParams({
                        nocache: new Date().valueOf()
                    });
                    win.close();
                },
                "fail": function() {
                    GEOR.util.errorDialog({
                        msg: tr('Synchronization failed.')
                    });
                    // TODO: handle this case
                }
            });
            vectorLayer = new OpenLayers.Layer.Vector("__georchestra_edit", {
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
                        // draw one feature at a time:
                        if (drawFeature) {
                            drawFeature.deactivate();
                            snap.deactivate();
                        }
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
                                header: tr("Req."), // Required
                                tooltip: tr("Required"),
                                dataIndex: "nillable",
                                width: 50,
                                fixed: true,
                                menuDisabled: true,
                                sortable: true,
                                align: "center",
                                renderer: function(v) {
                                    return !v ?
                                        '<img src="'+GEOR.config.PATHNAME+
                                            '/app/img/famfamfam/bullet_red.png" style="width:12px;height:12px;" alt="' + tr("Required") + '">' : 
                                        '<img src="'+GEOR.config.PATHNAME+
                                            '/app/img/nope.gif" style="width:12px;height:12px;" alt="' + tr("Not required") + '">';
                                }
                            }],
                            modifyControlOptions: {
                                clickout: false,
                                toggle: false,
                                mode: OpenLayers.Control.ModifyFeature.RESHAPE | 
                                    OpenLayers.Control.ModifyFeature.DRAG,
                                // hack to make all features unselectable
                                // thus unmodifable, in Read Only mode:
                                geometryTypes: roGeometry ? "none" : null
                                // "none" does not match any of "OpenLayers.Geometry.*"
                            },
                            viewConfig: {
                                forceFit: true
                            },
                            listeners: {
                                "done": function(panel, e) {
                                    //var feature = e.feature, modified = e.modified;
                                    if (e.feature.state != null) {
                                        strategy.save();
                                    }
                                },
                                "cancel": function(panel, e) {
                                    //var feature = e.feature, modified = e.modified;
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
                            title: tr("Edition"),
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
                                    // nothing for now
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
         * Method: draw
         * Note: can be called only when it is activated on the same layer. (TODO: checks)
         */
        draw: function() {
            if (roGeometry) {
                return;
            }
            if (win) {
                win.close();
            }
            //getFeature.deactivate(); // this is to allow snapping !
            selectFeature.deactivate();
            var handler = OpenLayers.Handler[(geomType == 'Line') ? 'Path' : geomType],
            options = {
                holeModifier: "altKey"
            };
            if (multiGeom) {
                options.multi = true
            }
            if (!snap) {
                snap = new OpenLayers.Control.Snapping({
                    layer: vectorLayer, 
                    autoActivate: true
                });
                map.addControl(snap);
            } else {
                snap.activate();
            }
            if (!drawFeature) {
                drawFeature = new OpenLayers.Control.DrawFeature(vectorLayer, handler, {
                    handlerOptions: options,
                    eventListeners: {
                        "featureadded": function(o) {
                            // mimic selection:
                            vectorLayer.events.triggerEvent("featureselected", {
                                feature: o.feature
                            });
                            // this will trigger a call to modifyFeature.select from the FeatureEditorGrid
                        }
                    },
                    autoActivate: true
                });
                map.addControl(drawFeature);
            } else {
                drawFeature.activate();
            }
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
            if (drawFeature) {
                drawFeature.deactivate();
                drawFeature.destroy();
                drawFeature = null;
            }
            if (snap) {
                snap.deactivate();
                snap.destroy();
                snap = null;
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
            if (splitButton) {
                splitButton.el.removeClass("now-editing");
                splitButton.setText(tr("Edition"));
                splitButton = null;
            }
        }
    };
})();
