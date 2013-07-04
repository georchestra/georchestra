/**
 * @include GeoExt/data/LayerRecord.js
 * @include OpenLayers/Layer/WMTS.js
 */
 
Ext.namespace("GeoExt.data");

GeoExt.data.WMTSCapabilitiesReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.WMTSCapabilities();
    }
    if(typeof recordType !== "function") {
        recordType = GeoExt.data.LayerRecord.create(
            recordType || meta.fields || [
                {name: "identifier", type: "string"},
                {name: "title", type: "string"},
                {name: "abstract", type: "string"},
                // TODO: more fields, for instance WGS84BoundingBox (llbbox ?)
                {name: "formats"}, // array
                {name: "styles"}, // array
                {name: "keywords"}, // array
                {name: "infoFormats"} // array
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
        if(!data || !data.documentElement) {
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
        if (layer.opaque && 
            OpenLayers.Util.indexOf(formats, "image/jpeg")>-1) {
            return "image/jpeg";
        }
        if (OpenLayers.Util.indexOf(formats, "image/png")>-1) {
            return "image/png";
        }
        if (OpenLayers.Util.indexOf(formats, "image/png; mode=24bit")>-1) {
            return "image/png; mode=24bit";
        }
        if (OpenLayers.Util.indexOf(formats, "image/gif")>-1) {
            return "image/gif";
        }
        return formats[0];
    },

    /** private: method[imageTransparent]
     *  :param layer: ``Object`` The layer's capabilities object.
     *  :return: ``Boolean`` The TRANSPARENT param.
     */
    imageTransparent: function(layer) {
        return layer.opaque == undefined || !layer.opaque;
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
        if(typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data);
        }
        if (!!data.error) {
            throw new Ext.data.DataReader.Error("invalid-response", data.error);
        }
        var version = data.version;
        //var capability = data.capability || {};
        var url = data.operationsMetadata.GetTile.dcp &&
            data.operationsMetadata.GetTile.dcp.http &&
            data.operationsMetadata.GetTile.dcp.http.get;
        var layers = data.contents && data.contents.layers;
        var records = [];
        
        if(url && layers) {
            var fields = this.recordType.prototype.fields; 
            var layer, values, options, params, field, v;

            for(var i=0, lenI=layers.length; i<lenI; i++){
                layer = layers[i];
                if(layer.identifier) {
                    values = {};
                    for(var j=0, lenJ=fields.length; j<lenJ; j++) {
                        field = fields.items[j];
                        v = layer[field.mapping || field.name] ||
                        field.defaultValue;
                        v = field.convert(v);
                        values[field.name] = v;
                    }
                    /*
                    options = {
                        minScale: layer.minScale,
                        maxScale: layer.maxScale
                    };
                    if(this.meta.layerOptions) {
                        Ext.apply(options, this.meta.layerOptions);
                    }
                    */
                    options = {
                        format: "image/jpeg", //this.imageFormat(layer),
                        url: url,
                        layer: layer.identifier,
                        name: layer.title, // should be automatic ?
                        format: this.imageFormat(layer),
                        matrixSet: "PM", // FIXME
                        style: "normal" // FIXME
                    };
                    /*
                    if (this.meta.layerParams) {
                        Ext.apply(params, this.meta.layerParams);
                    }
                    */
                    if(this.meta.layerOptions) {
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

///////////////////////////////////////

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