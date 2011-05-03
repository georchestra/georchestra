/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/data/AttributesReader.js
 */

Ext.namespace("Styler.data");
/**
 * Class: Styler.data.AttributesStore
 * Small helper class to make creating stores for remotely-loaded attributes
 *     data easier. AttributesStore is pre-configured with a built-in
 *     {Ext.data.HttpProxy} and {Styler.data.AttributesReader}.  The HttpProxy
 *     is configured to allow caching (disableCaching: false) and uses GET.
 *     If you require some other proxy/reader combination then you'll have to
 *     configure this with your own proxy or create a basic Ext.data.Store
 *     and configure as needed.
 *
 * Extends: Ext.data.Store
 */

/**
 * Constructor: Styler.data.AttributesStore
 * Create a new attributes store object.
 *
 * Parameters:
 * config - {Object} Store configuration.
 *
 * Configuration options:
 * format - {OpenLayers.Format} A parser for transforming the XHR response into
 *     an array of objects representing attributes.  Defaults to an
 *     {OpenLayers.Format.WFSDescribeFeatureType} parser.
 * fields - {Array | Function} Either an Array of field definition objects as
 *     passed to Ext.data.Record.create, or a Record constructor created using
 *     Ext.data.Record.create.  Defaults to ["name", "type"]. 
 */
Styler.data.AttributesStore = function(c) {
    Styler.data.AttributesStore.superclass.constructor.call(
        this,
        Ext.apply(c, {
            proxy: c.proxy || (!c.data ?
                new Ext.data.HttpProxy({url: c.url, disableCaching: false, method: "GET"}) :
                undefined
            ),
            reader: new Styler.data.AttributesReader(
                c, c.fields || ["name", "type"]
            )
        })
    );
};
Ext.extend(Styler.data.AttributesStore, Ext.data.Store);