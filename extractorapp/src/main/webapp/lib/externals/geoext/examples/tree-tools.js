/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.examples");

// this function creates a toolbar with a layer opacity
// slider and an information button, it is used to
// configure the layer node ui to add a toolbar
// for each node in the layer tree
GeoExt.examples.tbar = function(node, ct) {
    return new Ext.Toolbar({
        cls: "gx-toolbar",
        buttons: [new GeoExt.LayerOpacitySlider({
            layer: node.layer,
            aggressive: true,
            plugins: new GeoExt.LayerOpacitySliderTip(),
            width: 100
        })]
    });
};

// this function takes action based on the "action"
// parameter, it is used as a listener to layer
// nodes' "action" events
GeoExt.examples.act = function(node, action, evt) {
    var layer = node.layer;
    switch(action) {
    case "down":
        layer.map.raiseLayer(layer, -1);
        break;
    case "up":
        layer.map.raiseLayer(layer, +1);
        break;
    case "delete":
        layer.destroy();
        break;
    }
};

// a custom layer node UI class, for use with the second tree (see below).
GeoExt.examples.CustomLayerNodeUI = Ext.extend(GeoExt.tree.LayerNodeUI, {
    actions: [{
        action: "delete",
        qtip: "delete"
    }, {
        action: "up",
        qtip: "move up",
        update: function(el) {
            // "this" references the tree node
            var layer = this.layer, map = layer.map;
            if (map.getLayerIndex(layer) == map.layers.length - 1) {
                el.hide();
            } else {
                el.show();
            }
        }
    }, {
        action: "down",
        qtip: "move down",
        update: function(el) {
            // "this" references the tree node
            var layer = this.layer, map = layer.map;
            if (map.getLayerIndex(layer) == 1) {
                el.hide();
            } else {
                el.show();
            }
        }
    }],
    component: GeoExt.examples.tbar
});

Ext.onReady(function() {
    Ext.QuickTips.init();

    // the map panel
    var mapPanel = new GeoExt.MapPanel({
        border: true,
        region: "center",
        center: [146.1569825, -41.6109735],
        zoom: 6,
        layers: [
            new OpenLayers.Layer.WMS("Tasmania State Boundaries",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_state_boundaries"
                }, {
                    buffer: 0,
                    // exclude this layer from layer container nodes
                    displayInLayerSwitcher: false
               }),
            new OpenLayers.Layer.WMS("Water",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_water_bodies",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Cities",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_cities",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Tasmania Roads",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_roads",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                })
        ]
    });

    // the first layer tree panel. If this tree the node actions and
    // component are set using the loader's "baseAttrs" property.
    var tree1 = new Ext.tree.TreePanel({
        border: true,
        region: "center",
        title: "Layer Tree 1",
        split: true,
        collapsible: true,
        autoScroll: true,
        loader: {
            applyLoader: false
        },
        root: {
            nodeType: "gx_layercontainer",
            loader: {
                baseAttrs: {
                    actions: [{
                        action: "delete",
                        qtip: "delete"
                    }, {
                        action: "up",
                        qtip: "move up",
                        update: function(el) {
                            // "this" references the tree node
                            var layer = this.layer, map = layer.map;
                            if (map.getLayerIndex(layer) == map.layers.length - 1) {
                                el.addClass('disabled');
                            } else {
                                el.removeClass('disabled');
                            }
                        }
                    }, {
                        action: "down",
                        qtip: "move down",
                        update: function(el) {
                            // "this" references the tree node
                            var layer = this.layer, map = layer.map;
                            if (map.getLayerIndex(layer) == 1) {
                                el.addClass('disabled');
                            } else {
                                el.removeClass('disabled');
                            }
                        }
                    }],
                    checked: null,
                    component: GeoExt.examples.tbar
                }
            }
        },
        rootVisible: false,
        lines: false,
        listeners: {
            action: GeoExt.examples.act
        }
    });

    // the second layer tree panel. In this tree the CustomLayerNodeUI
    // class is used for each node of the layer container.
    var tree2 = new Ext.tree.TreePanel({
        border: true,
        region: "south",
        height: 300,
        title: "Layer Tree 2",
        split: true,
        collapsible: true,
        autoScroll: true,
        loader: {
            applyLoader: false
        },
        root: {
            nodeType: "gx_layercontainer",
            loader: {
                uiProviders: {
                    "ui": GeoExt.examples.CustomLayerNodeUI
                },
                baseAttrs: {
                    uiProvider: "ui"
                }
            }
        },
        rootVisible: false,
        lines: false,
        listeners: {
            action: GeoExt.examples.act
        }
    });

    // the viewport
    new Ext.Viewport({
        layout: "fit",
        hideBorders: true,
        items: {
            layout: "border",
            deferredRender: false,
            items: [
                mapPanel, {
                region: "west",
                width: 250,
                layout: "border",
                items: [
                    tree1,
                    tree2
                ]
            }, {
                region: "east",
                contentEl: "desc",
                width: 250
            }]
        }
    });
});
