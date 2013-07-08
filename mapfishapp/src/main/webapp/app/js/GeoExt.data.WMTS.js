/**
 * @include GeoExt/data/LayerRecord.js
 * @include OpenLayers/Layer/WMTS.js
 */
 
Ext.namespace("GeoExt.data");

GeoExt.data.WMTSCapabilitiesReader = function(meta, recordType) {
    meta = meta || {};
    if (!meta.format) {
        meta.format = new OpenLayers.Format.WMTSCapabilities();
    }
    if (typeof recordType !== "function") {
        recordType = GeoExt.data.LayerRecord.create(
            recordType || meta.fields || [
                // for the use of geOrchestra only:
                {name: "type", type: "string", defaultValue: "WMTS"},
                // end specific georchestra
                {name: "name", type: "string", mapping: "identifier"},
                {name: "title", type: "string"},
                {name: "abstract", type: "string"},
                {name: "llbbox", mapping: "bounds", convert: function(v){
                    return [v.left, v.bottom, v.right, v.top];
                }},
                {name: "formats"}, // array
                {name: "styles"}, // array of Objects {abstract, identifier, isDefault, keywords, title}
                {name: "keywords"} // Object
                //,{name: "infoFormats"} // array (optional)
            ]
        );
    }
    GeoExt.data.WMTSCapabilitiesReader.superclass.constructor.call(
        this, meta, recordType
    );
};

Ext.extend(GeoExt.data.WMTSCapabilitiesReader, Ext.data.DataReader, {


    /** private: method[read]
     *  :param request: ``Object`` The XHR object which contains the parsed XML
     *      document.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     */
    read: function(request) {
        var data = request.responseXML;
        if (!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },
    
    /** private: method[imageFormat]
     *  :param layer: ``Object`` The layer's capabilities object.
     *  :return: ``String`` The (supposedly) best mime type for requesting 
     *      tiles.
     */
    imageFormat: function(layer) {
        var formats = layer.formats;
        if (OpenLayers.Util.indexOf(formats, "image/png")>-1) {
            return "image/png";
        }
        if (OpenLayers.Util.indexOf(formats, "image/jpeg")>-1) {
            return "image/jpeg";
        }
        if (OpenLayers.Util.indexOf(formats, "image/png8")>-1) {
            return "image/png8";
        }
        if (OpenLayers.Util.indexOf(formats, "image/gif")>-1) {
            return "image/gif";
        }
        return formats[0];
    },
    
    /** private: method[layerStyle]
     *  :param layer: ``Object`` The layer's capabilities object.
     *  :return: ``String`` The default style, if any.
     */
    layerStyle: function(layer) {
        var styles = layer.styles;
        for (var i=0, len=styles.length; i<len; i++){
            if (styles[i].isDefault === true) {
                return styles[i].identifier;
            }
        }
        return (styles[0] && styles[0].identifier) || "";
    },
    
    /** private: method[matrixSet]
     *  :param layer: ``Object`` The layer's capabilities object.
     *  :return: ``String`` a matrixSet.
     */
    matrixSet: function(layer) {
        if (this.meta.matrixSetChooser) {
            var preferedMatrixSet = 
                this.meta.matrixSetChooser(layer.tileMatrixSetLinks);
            if (preferedMatrixSet) {
                return preferedMatrixSet;
            }
        }
        return layer.tileMatrixSetLinks[0].tileMatrixSet;
    },

    /** private: method[readRecords]
     *  :param data: ``DOMElement | String | Object`` A document element or XHR
     *      response string.  As an alternative to fetching capabilities data
     *      from a remote source, an object representing the capabilities can
     *      be provided given that the structure mirrors that returned from the
     *      capabilities parser.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     *  
     *  Create a data block containing Ext.data.Records from an XML document.
     */
    readRecords: function(data) {
        if (typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data);
        }
        if (!!data.error) {
            throw new Ext.data.DataReader.Error("invalid-response", data.error);
        }
        var url = data.operationsMetadata.GetTile.dcp &&
            data.operationsMetadata.GetTile.dcp.http &&
            data.operationsMetadata.GetTile.dcp.http.get;
        var layers = data.contents && data.contents.layers;
        var tileMatrixSets = data.contents && data.contents.tileMatrixSets;
        var records = [];

        if (url && layers) {
            var fields = this.recordType.prototype.fields; 
            var layer, values, options, params, field, v, matrixSet;

            for (var i=0, lenI=layers.length; i<lenI; i++){
                layer = layers[i];
                if (layer.identifier) {
                    values = {};
                    for (var j=0, lenJ=fields.length; j<lenJ; j++) {
                        field = fields.items[j];
                        v = layer[field.mapping || field.name] ||
                        field.defaultValue;
                        v = field.convert(v);
                        values[field.name] = v;
                    }
                    matrixSet = this.matrixSet(layer);
                    options = {
                        url: url,
                        layer: layer.identifier,
                        name: layer.title,
                        format: this.imageFormat(layer),
                        matrixSet: matrixSet,
                        matrixIds: tileMatrixSets[matrixSet].matrixIds,
                        // TODO: TileMatrixLimits, if any, should be extracted from the capabilities and inserted here (tileFullExtent)
                        // (TileMatrixSetLink > TileMatrixSetLimits > TileMatrixLimits[] )
                        
                        // TODO: enable client zoom for WMTS layers, by adding serverResolutions in the layer options
                        style: this.layerStyle(layer)
                    };
                    if (this.meta.layerOptions) {
                        Ext.apply(options, this.meta.layerOptions);
                    }
                    values.layer = new OpenLayers.Layer.WMTS(options);
                    records.push(new this.recordType(values, values.layer.id));
                }
            }
        }

        return {
            totalRecords: records.length,
            success: true,
            records: records
        };

    }
});

GeoExt.data.WMTSCapabilitiesStore = function(c) {
    c = c || {};
    GeoExt.data.WMTSCapabilitiesStore.superclass.constructor.call(
        this,
        Ext.apply(c, {
            proxy: c.proxy || (!c.data ?
                new Ext.data.HttpProxy({url: c.url, disableCaching: false, method: "GET"}) :
                undefined
            ),
            reader: new GeoExt.data.WMTSCapabilitiesReader(
                c, c.fields
            )
        })
    );
};
Ext.extend(GeoExt.data.WMTSCapabilitiesStore, Ext.data.Store);
