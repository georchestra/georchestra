/**
 * Copyright (c) 2008 The Open Planning Project
 * 
 * @requires Styler.js
 */

Styler.SchemaManager = OpenLayers.Class({
    
    /**
     * ConfigProperty: map
     * {OpenLayers.Map} The map.
     */
    map: null,
    
    /**
     * Property: attributeStores
     * {Object} Hash, keyed by the id property of the map's layers, with
     *     an attributes store for the layer.
     */
    attributeStores: null,
    
    /**
     * Property: matchGeomProperty
     */
    matchGeomProperty: /^gml:(Multi)?(Point|LineString|Polygon|Curve|Surface|Geometry)PropertyType$/,
    
    /**
     * Constructor: Styler.SchemaManager
     * Create an object for loading remote schemas.
     *
     * Parameters:
     * map - {OpenLayers.Map} Schemas for all WMS layers will be loaded -
     *     assuming layer of same typename exists at wfs endpoint.
     */
    initialize: function(map) {
        this.map = map;
        this.attributeStores = {};
        var layer;
        for(var i=0; i<this.map.layers.length; ++i) {
            layer = this.map.layers[i];
            if(layer instanceof OpenLayers.Layer.WMS) {
                this.attributeStores[layer.id] = new GeoExt.data.AttributeStore({
                    url: layer.url.split("?")[0].replace("/wms", "/wfs"),
                    baseParams: {
                        version: "1.1.1",
                        request: "DescribeFeatureType",
                        typename: layer.params["LAYERS"]
                    }
                });
            }
        }
    },
    
    /**
     * Method: loadAll
     * Load schema for all layers.
     *
     * Parameters:
     * callback - {Function} Function to be called when schema loading is done.
     */
    loadAll: function(callback) {
        var loaders = [];
        for(var id in this.attributeStores) {
            loaders.push(this.createLoader(this.attributeStores[id]));
        }
        Styler.dispatch(loaders, callback);
    },
    
    /**
     * Method: createLoader
     */
    createLoader: function(store) {
        return function(done) {
            store.load({callback: done});
        };
    },
    
    /**
     * Method: getGeometryName
     * Determine the name of the first geometry field in the schema.
     *
     * Parameters:
     * layer - {OpenLayers.Layer.WMS}
     *
     * Returns:
     * {String} The geometry attribute name.
     */
    getGeometryName: function(layer) {
        var store = this.attributeStores[layer.id];
        var index = store.find("type", this.matchGeomProperty);
        var name;
        if(index > -1) {
            name = store.getAt(index).get("name");
        }
        return name;
    },
    
    /**
     * Method: getSymbolType
     * Determine the symbol type (Point, Line, or Polygon) given a layer.  This
     *     is based on the first geometry type field in the schema.  If the
     *     type is gml:GeometryType, the symbol type will be undefined.
     *
     * Parameters:
     * layer - {OpenLayers.Layer.WMS}
     *
     * Returns:
     * {String} The symbol type or undefined if not specific.
     */
    getSymbolType: function(layer) {
        var store = this.attributeStores[layer.id];
        var index = store.find("type", this.matchGeomProperty);
        var type;
        if(index > -1) {
            var match = store.getAt(index).get("type").match(this.matchGeomProperty);
            type = ({
                "Point": "Point",
                "LineString": "Line",
                "Polygon": "Polygon",
                "Curve": "Line",
                "Surface": "Polygon"
            })[match[2]];
        }
        return type;
    }

});
