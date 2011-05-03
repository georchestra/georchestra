/*
 * @include GeoExt/widgets/tree/LayerContainer.js
 */

Ext.namespace('App');

/**
 * Constructor: App.LayerTree
 * Creates a layer tree, i.e. an {Ext.tree.TreePanel} configured with
 * a {GeoExt.tree.LayerContainer}. Use the "layerTree" property to get
 * a reference to this layer tree.
 *
 * Parameters:
 * layerStore - {GeoExt.data.LayerStore} The layer store this layer
 *     tree is connected to.
 * options - {Object} Options to pass to the {Ext.tree.TreePanel}.
 */
App.LayerTree = function(layerStore, options) {

    // Private

    // Public

    Ext.apply(this, {

        /**
         * APIMethod: layerTreePanel
         * {Ext.tree.TreePanel} The layer tree panel. Read-only.
         */
        layerTreePanel: null
    });

    // Main

    options =  Ext.apply({
        root: new GeoExt.tree.LayerContainer({
            layerStore: layerStore,
            leaf: false,
            expanded: true
        }),
        enableDD: true
    }, options);
    this.layerTreePanel = new Ext.tree.TreePanel(options);
};
