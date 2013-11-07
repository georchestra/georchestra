/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux.tree");

/*
 * @requires GeoExt.ux/widgets/WMSBrowser.js
 * @requires GeoExt.ux/widgets/tree/WMSBrowserRootNode.js
 */

/** api: (define)
 *  module = GeoExt.ux.tree
 *  class = WMSBrowserTreePanel
 */

/** api: constructor
 *  .. class:: WMSBrowserTreePanel
 */
GeoExt.ux.tree.WMSBrowserTreePanel = Ext.extend(Ext.tree.TreePanel, {

    rootVisible: false,

    layout: 'absolute',

    x: 0,

    y: 0,

    region: 'center',

    anchor: '50% 100%',

    border: false,

    autoScroll: true,

    useArrows: true,

    animate: true,

    root: null,

    /** api: config[wmsbrowser]
     * :class:`GeoExt.ux.data.WMSBrowser` A reference to the main browser object
     */
    wmsbrowser: null,

    /** private: method[constructor]
     */
    constructor: function(config) {
        Ext.apply(this, config);
        Ext.apply(this, {listeners: {
            'checkchange': function(node, checked) {
                if (checked === true) {
                    if (!this.isLayerCompatible(node.attributes.layer)) {
                        node.getUI().checkbox.checked = false;
                    } else {
                        this.addLayerToPreview(node.attributes.layer);
                        this.setLayerNameFromCheckedNodes();
                        this.loadLayerMetadata(node.attributes.layer);
                    }
                } else {
                    this.removeLayerFromPreview(node.attributes.layer);
                    this.setLayerNameFromCheckedNodes();
                    this.wmsbrowser.resetCenterFormPanel();
                }
            }
        }});

        this.root = new GeoExt.ux.tree.WMSBrowserRootNode(config);
        this.layerStore = this.wmsbrowser.layerStore;
        this.map = this.layerStore.map;

        arguments.callee.superclass.constructor.call(this, config);
    },

    /** private: method[loadURL]
     *  :param url: ``String``
     *
     *  Set the root loader url to given url.
     */
    loadURL: function(url) {
        this.getRootNode().setLoaderURL(url);
    },

    /** private: method[addLayerToPreview]
     *  :param layer: :class:`OpenLayers.Layer.WMS`
     *
     *  Add the layer to the preview map.
     */
    addLayerToPreview: function(layer) {
        var layerPreview = this.wmsbrowser.layerPreview;
        var mapPanelPreview = this.wmsbrowser.mapPanelPreview;

        if (!layerPreview) {
            layerPreview = layer.clone();
            layerPreview.mergeNewParams({
                format: "image/png",
                transparent: "true"
            });

            layerPreview.mergeNewParams(
                {'LAYERS': [layerPreview.params.LAYERS]}
            );

            mapPanelPreview.map.addLayer(layerPreview);

            if (mapPanelPreview.collapsed) {
                this.wmsbrowser.hideLayerPreview();
            }

            this.wmsbrowser.zoomToRecordLLBBox(layerPreview);

            this.wmsbrowser.layerPreview = layerPreview;
        } else {
            layerPreview.params.LAYERS.push(
                layer.params.LAYERS
            );
            layerPreview.mergeNewParams(
                {'LAYERS': layerPreview.params.LAYERS}
            );

            this.wmsbrowser.zoomToRecordLLBBox(layerPreview, false);
        }
    },

    /** private: method[removeLayerFromPreview]
     *  :param layer: :class:`OpenLayers.Layer.WMS`
     *
     *  Remove the layer from the preview map.
     */
    removeLayerFromPreview: function(layer) {
        var layerPreview = this.wmsbrowser.layerPreview;

        if (!layerPreview) {
            return;
        }

        var layers = layerPreview.params.LAYERS;
        var index = OpenLayers.Util.indexOf(
            layers, layer.params.LAYERS
        );

        if (index != -1) {
            layers.splice(index, 1);
        }

        if (layers.length == 0) {
            this.wmsbrowser.resetLayerPreview();
        } else {
            layerPreview.mergeNewParams({'LAYERS': layers});
        }
    },

    /** private: method[setLayerNameFromCheckedNodes]
     *  Set the textfield used for the layer name value to the collection of
     *  the currently checked node layer titles and names.
     */
    setLayerNameFromCheckedNodes: function() {
        this.wmsbrowser.layerNameField.setValue(
            this.root.getLayerNameFromCheckedNodes()
        );
    },

    /** private: method[addLayer]
     *  :return: ``Boolean`` Whether the layer was added or not due to an
     *                       error.
     *
     *  Create a new :class:`OpenLayers.Layer.WMS` object from the currently
     *  checked nodes and add it to the map.
     */
    addLayer: function() {
        var layerAdded = false;
        var layerPreview = this.wmsbrowser.layerPreview;
        var map = this.wmsbrowser.layerStore.map;

        if (!this.root.hasChildNodes()) {
            this.wmsbrowser.fireEvent(
                'genericerror', this.wmsbrowser.pleaseInputURLText
            );
        } else if (layerPreview) {
            if (this.wmsbrowser.isLayerNameValid()) {
                var newLayer = this.root.getNewLayerFromCheckedNodes();
                newLayer.name = this.wmsbrowser.layerNameField.getValue();

                var addLayer = this.wmsbrowser.fireEvent('beforelayeradded',
                                                         {'layer': newLayer});

                if (addLayer !== false) {
                    map.addLayer(newLayer);
                    this.wmsbrowser.fireEvent('layeradded',
                                              {'layer': newLayer});
                    layerAdded = true;

                    if(this.wmsbrowser.zoomOnLayerAdded) {
                        // zoom to added layer extent
                        // (in the current map projection)
                        var bounds = OpenLayers.Bounds.fromArray(
                            newLayer.metadata.llbbox
                        );
                        map.zoomToExtent(bounds.transform(
                            new OpenLayers.Projection("EPSG:4326"),
                            new OpenLayers.Projection(map.getProjection())
                        ));                    
                    }
                }
            }
        } else {
            this.wmsbrowser.fireEvent(
                'genericerror', this.wmsbrowser.pleaseCheckALayerInTreeText
            );
        }

        return layerAdded;
    },

    /** private: method[loadLayerMetadata]
     *  :param layer: :class:`OpenLayers.Layer.WMS`
     *
     *  Load the layer metadata in a specific form.  Currently loads 'abstract'
     *  metadata only.
     */
    loadLayerMetadata: function(layer) {
        if (layer && layer.metadata.abstract) {
            this.wmsbrowser.descriptionField.setValue(layer.metadata.abstract);
        } else {
            this.wmsbrowser.descriptionField.setValue("");
        }
    },

    /** private: method[isLayerCompatible]
     *  :return: ``Boolean`` Whether the layer is compatible with the current
     *                       map
     *
     *  Checks if a specific :class:`OpenLayers.Layer.WMS` object can be added
     *  to the map.  To be compatible, it must :
     *    - support the current map projection
     *    - must at least intersects the map maxextent
     */
    isLayerCompatible: function(layer) {
        var compatible = true;
        var reasons = [];

        var srs = this.map.getProjection();
        var mapMaxExtent = this.map.getMaxExtent().clone().transform(
            new OpenLayers.Projection(this.map.getProjection()),
            new OpenLayers.Projection('EPSG:4326')
        );

        // validate srs
        if(!(layer.metadata.srs[srs] === true ||
            OpenLayers.Util.indexOf(layer.metadata.srs, srs) >= 0)) {
            if(srs == 'EPSG:900913' && layer.metadata.srs['EPSG:3857'] === true){
                srs = 'EPSG:3857';
            }
            else {
                compatible = false;
                reasons.push(
                    this.wmsbrowser.srsNotSupportedShortText +
                    " (" + this.map.getProjection() + ")"
                );
            }
        }

        // validate extent
        var layerExtent = layer.metadata.llbbox;
        var extent;
        if (layerExtent) 
        {
            if(typeof layerExtent == "string") {
                extent = OpenLayers.Bounds.fromString(layerExtent);
            } else if(layerExtent instanceof Array) {
                extent = OpenLayers.Bounds.fromArray(layerExtent);
            }
        }

        if (!(!extent || mapMaxExtent.intersectsBounds(extent, false))) {
            compatible = false;
            reasons.push(
                this.wmsbrowser.extentNotSupportedShortText +
                " (" + this.map.getExtent().toBBOX() + ")"
            );
        }

        // output a message if not valid
        if (!compatible) {
            var layerName = "";
            if (layer.metadata.title != "") {
                layerName = layer.metadata.title + " : ";
            } else if (layer.metadata.name != "") {
                layerName = layer.metadata.name + " : ";
            }
            var message = layerName + this.wmsbrowser.layerCantBeAddedText + reasons.join(', ');
            this.wmsbrowser.fireEvent('genericerror', message);
        }

        return compatible;
    }
});
