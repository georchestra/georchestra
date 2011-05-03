/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @requires Styler.js
 */

Styler.SLDManager = OpenLayers.Class({
    
    /**
     * ConfigProperty: map
     * {OpenLayers.Map} The map to observe for layer additions
     */
    map: null,
    
    /**
     * Property: layerData
     * {Object} Hash, keyed by the id property of the map's layers, with
     * meta information about each layer.
     * 
     * Currently used metadata properties:
     * - sld {Object} sld object as used by OpenLayers.Format.SLD
     * - style {OpenLayers.Style} style object for the layer
     * - styleName {String} name of the style, as used in the styles param of
     *   the WMS GetMap request
     */
    layerData: null,
    
    /**
     * Constructor: Styler.SLDManager
     * Create an object for loading and saving remote SLD.
     *
     * Parameters:
     * map - {OpenLayers.Map} Remote SLD for WMS layers in this map will be
     *     available for loading and updating.
     */
    initialize: function(map) {
        this.map = map;
        var layer;
        this.layers = [];
        this.layerData = {};
        for(var i=0; i<this.map.layers.length; ++i) {
            layer = this.map.layers[i];
            if(layer instanceof OpenLayers.Layer.WMS) {
                this.layers.push(layer);
            }
        }
    },
    
    /**
     * Method: loadAll
     * Load SLD for all layers.
     *
     * Parameters:
     * callback - {Function} Function to be called when SLD loading is done.
     */
    loadAll: function(callback) {
        var num = this.layers.length;
        var loaders = new Array(num);
        for(var i=0; i<num; ++i) {
            loaders[i] = this.createLoader(this.layers[i]);
        }
        Styler.dispatch(loaders, callback);
    },
    
    /**
     * Method: createLoader
     */
    createLoader: function(layer) {
        return (function(done) {
            this.loadSld(layer, layer.params["STYLES"], done);
        }).createDelegate(this);
    },
    
    getUrl: function(layer, styleName) {
        var url;
        if(layer instanceof OpenLayers.Layer.WMS) {
            url = layer.url.split("?")[0].replace(
                "/wms", "/rest/styles/"+styleName+".sld");
        }
        //TODO handle other layer types
        return url;
    },
    
    loadSld: function(layer, styleName, callback) {
        Ext.Ajax.request({
            url: this.getUrl(layer, styleName),
            method: "GET",
            success: function(request) {
                var sld = new OpenLayers.Format.SLD().read(
                    request.responseXML.documentElement ?
                    request.responseXML : request.responseText);
                //TODO: for now, we just handle the 1st user style of the
                // 1st named layer. Should make that more flexible in the
                // future.
                for(var namedLayer in sld.namedLayers) {
                    break;
                }
                this.layerData[layer.id] = {
                    style: sld.namedLayers[namedLayer].userStyles[0],
                    sld: sld,
                    styleName: styleName
                };
                callback(this.layerData[layer.id]);
            },
            scope: this
        });
    },
    
    saveSld: function(layer, callback, scope) {
        Ext.Ajax.request({
            url: this.getUrl(layer, this.layerData[layer.id].styleName),
            method: "PUT",
            headers: {
                "Content-Type": "application/vnd.ogc.sld+xml; charset=UTF-8"
            },
            xmlData: new OpenLayers.Format.SLD().write(this.layerData[layer.id].sld),
            success: function(request) {
                callback.call(scope || this, request);
            }
        });
    },
    
    getStyle: function(layer) {
        var data = this.layerData[layer.id];
        return data && data.style;
    }
});
