
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
 * @requires GeoExt.ux/widgets/tree/WMSBrowserTreePanel.js
 */

/** api: (define)
 *  module = GeoExt.ux.tree
 *  class = WMSBrowserRootNode
 */

/** api: constructor
 *  .. class:: WMSBrowserRootNode
 */
GeoExt.ux.tree.WMSBrowserRootNode = Ext.extend(Ext.tree.AsyncTreeNode, {

    /** private: property[INIT_URL]
     *  ``String`` The url used on first load.  This is a hack to allow the tree
     *  to be rendered.
     */
    INIT_URL: "__foo__",

    /** api: config[wmsbrowser]
     * :class:`GeoExt.ux.data.WMSBrowser` A reference to the main browser object
     */
    wmsbrowser: null,

    /** private: config[loader]
     * :class:`GeoExt.tree.WMSCapabilitiesLoader`
     */
    loader: null,

    /** private: method[constructor]
     */
    constructor: function(config) {
        Ext.apply(this, config);
        Ext.apply(this, {loader: new GeoExt.tree.WMSCapabilitiesLoader({
            url: "__foo__",
            layerOptions: {buffer: 0, ratio: 1},
            layerParams: {'TRANSPARENT': 'TRUE'},
            // customize the createNode method to add a checkbox to nodes
            createNode: function(attr) {
                attr.checked = attr.leaf ? false : undefined;
                return GeoExt.tree.WMSCapabilitiesLoader.prototype.createNode.apply(this, [attr]);
            }
        })});

        arguments.callee.superclass.constructor.call(this, config);

        // events registration
        this.on('load', this.onWMSCapabilitiesLoad, this);
        this.on('loadexception', this.onWMSCapabilitiesLoadException, this);
    },

    /** private: method[setLoaderURL]
     *  :param url: ``String``
     *
     *  Set the loader url to the given url and reload.
     */
    setLoaderURL: function(url) {
        this.loader.url = url;
        this.reload();
    },

    /** private: method[onWMSCapabilitiesLoad]
     *  Called on "load" event.  Fires any "success" or "failure" events/methods
     */
    onWMSCapabilitiesLoad: function() {
        if (this.hasChildNodes()) {
            this.wmsbrowser.fireEvent('getcapabilitiessuccess');
        } else if (this.loader.url != this.INIT_URL) {
            this.onWMSCapabilitiesStoreLoadException();
        }
    },

    /** private: method[onWMSCapabilitiesStoreLoadException]
     *  Called on load failure.  Fires the according event.
     */
    onWMSCapabilitiesStoreLoadException: function() {
        this.wmsbrowser.fireEvent('getcapabilitiesfail');
    },

    /** private: method[getLayerNameFromCheckedNodes]
     *  :return:  ``String``
     *  Collect and return all checked node layer title or name into a single
     *  string separated by ','.
     */
    getLayerNameFromCheckedNodes: function() {
        var layerName = [];

        this.cascade(function(){
            var layer = this.attributes.layer;

            // skip nodes without layers or not checked 
            if (!layer || !this.getUI().isChecked()) {
                return;
            }

            if (layer.metadata.title != "") {
                layerName.push(layer.metadata.title);
            } else if (layer.metadata.name != "") {
                layerName.push(layer.metadata.name);
            }
        });

        return layerName.join(', ');
    },

    /** private: method[getNewLayerFromCheckedNodes]
     *  :return:  :class:`OpenLayers.Layer.WMS`
     *
     *  From all currently checked nodes, create and return a new
     *  :class:`OpenLayers.Layer.WMS` object.  All 'layers' parameters are
     *  merged together.
     *
     *  Note: this method doesn't set the layer name using the textbox.
     */
    getNewLayerFromCheckedNodes: function() {
        var newLayer;

        this.cascade(function(){
            var layer = this.attributes.layer;

            // skip nodes without layers or not checked 
            if (!layer || !this.getUI().isChecked()) {
                return;
            }

            if (!newLayer) {
                newLayer = layer.clone();

                // this is hardcoded
                newLayer.mergeNewParams({
                    format: "image/png",
                    transparent: "true"
                });

                newLayer.mergeNewParams(
                    {'LAYERS': [newLayer.params.LAYERS]}
                );
            } else {
                newLayer.params.LAYERS.push(
                    layer.params.LAYERS
                );
                newLayer.mergeNewParams(
                    {'LAYERS': newLayer.params.LAYERS}
                );
            }
        });

        return newLayer;
    }
});
