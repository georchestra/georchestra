/**
 * @include GeoExt/data/LayerRecord.js
 * @include OpenLayers/Layer/WMTS.js
 */
 
Ext.namespace("GeoExt.data");

// TODO: OpenLayers + GeoExt patches (including tests)


// GeoExt WMTS encoder overriding because we don't want to update the whole lib for now.
// no functional change - taken from https://github.com/geoext/geoext/blob/master/lib/GeoExt/data/PrintProvider.js
// on 2013-07-23
GeoExt.data.PrintProvider.prototype.encoders.layers.WMTS = function(layer) {
    var enc = this.encoders.layers.HTTPRequest.call(this, layer);
    enc = Ext.apply(enc, {
        type: 'WMTS',
        layer: layer.layer,
        version: layer.version,
        requestEncoding: layer.requestEncoding,
        style: layer.style,
        dimensions: layer.dimensions,
        params: layer.params,
        matrixSet: layer.matrixSet
    });
    if (layer.matrixIds) {
        if (layer.requestEncoding == "KVP") {
            enc.format = layer.format;
        }
        enc.matrixIds = []
        Ext.each(layer.matrixIds, function(matrixId) {
            enc.matrixIds.push({
                identifier: matrixId.identifier,
                matrixSize: [matrixId.matrixWidth, 
                        matrixId.matrixHeight],
                resolution: matrixId.scaleDenominator * 0.28E-3
                        / OpenLayers.METERS_PER_INCH
                        / OpenLayers.INCHES_PER_UNIT[layer.units],
                tileSize: [matrixId.tileWidth, matrixId.tileHeight],
                topLeftCorner: [matrixId.topLeftCorner.lon, 
                        matrixId.topLeftCorner.lat]
            });
        })
        return enc;
    }
    else {
        return Ext.apply(enc, {
            formatSuffix: layer.formatSuffix,
            tileOrigin: [layer.tileOrigin.lon, layer.tileOrigin.lat],
            tileSize: [layer.tileSize.w, layer.tileSize.h],
            maxExtent: (layer.tileFullExtent != null) ? layer.tileFullExtent.toArray() : layer.maxExtent.toArray(),
            zoomOffset: layer.zoomOffset,
            resolutions: layer.serverResolutions || layer.resolutions
        });
    }
};
// end overriding

// this is an addition:
GeoExt.data.PrintProvider.prototype.encoders.legends.gx_wmtslegend = function(legend, scale) {
    return this.encoders.legends.gx_urllegend.call(this, legend);
};


/**
 * @include GeoExt/widgets/LegendImage.js
 * @requires GeoExt/widgets/LayerLegend.js
 */
GeoExt.WMTSLegend = Ext.extend(GeoExt.LayerLegend, {

    /** private: method[initComponent]
     *  Initializes the WMS legend. For group layers it will create multiple
     *  image box components.
     */
    initComponent: function() {
        GeoExt.WMTSLegend.superclass.initComponent.call(this);
        var layer = this.layerRecord.getLayer();
        this._noMap = !layer.map;
        layer.events.register("moveend", this, this.onLayerMoveend);
        this.update();
    },
    
    /** private: method[onLayerMoveend]
     *  :param e: ``Object``
     */
    onLayerMoveend: function(e) {
        if (e.zoomChanged === true || this._noMap) {
            delete this._noMap;
            this.update();
        }
    },

    /** private: method[getLegendUrl]

     *  :return: ``String`` The legend URL.
     *
     *  Get the legend URL of a layer.
     */
    getLegendUrl: function() {
        var rec = this.layerRecord,
            layer = rec.getLayer();

        var mapDenominator = layer.map && layer.map.getScale();
        if (!mapDenominator) {
            return;
        }

        var styles = rec.get("styles"),
            url, style, legends, legend;

        for (var i=0, l=styles.length; i<l; i++) {
            style = styles[i];
            if (style.identifier === layer.style) {
                legends = style.legends;
                if (legends) {
                    // get the legend for the current layer scale
                    for (var j=0, ll=legends.length; j<ll; j++) {
                        legend = legends[j];
                        if (!legend.href) {
                            continue;
                        }
                        var hasMin = legend.hasOwnProperty("minScaleDenominator"),
                            hasMax = legend.hasOwnProperty("maxScaleDenominator");
                        if (!hasMin && !hasMax) {
                            return legend.href;
                        }
                        if (!hasMin && mapDenominator < legend.maxScaleDenominator) {
                            return legend.href;
                        }
                        if (!hasMax && mapDenominator >= legend.minScaleDenominator) {
                            return legend.href;
                        }
                        if (mapDenominator < legend.maxScaleDenominator && 
                            mapDenominator >= legend.minScaleDenominator) {

                            return legend.href;
                        }
                    }
                }
                break;
            }
        }
        return url;
    },

    /** private: method[update]
     *  Update the legend, adding, removing or updating
     *  the box component.
     */
    update: function() {
        var layer = this.layerRecord.getLayer();
        if(!(layer && layer.map)) {
            return;
        }
        GeoExt.WMTSLegend.superclass.update.apply(this, arguments);

        var newCmp = {
            xtype: "gx_legendimage",
            url: this.getLegendUrl()
        };
        if (this.items.getCount() == 2) {
            var cmp = this.items.itemAt(1);
            if (cmp.url !== newCmp.url) {
                this.remove(cmp);
                cmp.destroy();
                if (newCmp.url !== undefined) {
                    this.add(newCmp);
                }
            }
        } else if (newCmp.url) {
            this.add(newCmp);
        }
        this.doLayout();
    },

    /** private: method[beforeDestroy]
     */
    beforeDestroy: function() {
        var layer = this.layerRecord.getLayer();
        layer && layer.events &&
            layer.events.unregister("moveend", this, this.onLayerMoveend);

        GeoExt.WMTSLegend.superclass.beforeDestroy.apply(this, arguments);
    }

});

/** private: method[supports]
 *  Private override
 */
GeoExt.WMTSLegend.supports = function(layerRecord) {
    return layerRecord.getLayer() instanceof OpenLayers.Layer.WMTS ? 1 : 0;
};

/** api: legendtype = gx_wmslegend */
GeoExt.LayerLegend.types["gx_wmtslegend"] = GeoExt.WMTSLegend;

/** api: xtype = gx_wmslegend */
Ext.reg('gx_wmtslegend', GeoExt.WMTSLegend);

/*
OpenLayers.Format.OWSCommon.v1.prototype.readers.ows = OpenLayers.Util.extend(
    OpenLayers.Format.OWSCommon.v1.prototype.readers.ows, {
        "WGS84BoundingBox": function(node, obj) {
            var boundingBox = {};
            boundingBox.crs = node.getAttribute("crs");
            if (obj.BoundingBox) {
                obj.BoundingBox.push(boundingBox);
            } else {
                obj.projection = boundingBox.crs; // FIXME: creates an unwanted projection key in our WMTS layer object !!!
                boundingBox = obj;
           }
           this.readChildNodes(node, boundingBox);
        }
    }
);
*/

// Monkey patching openlayers

/**
 * @requires OpenLayers/Format/WMTSCapabilities/v1_0_0.js
 */
OpenLayers.Format.WMTSCapabilities.v1_0_0.prototype.readers.wmts = OpenLayers.Util.extend(
    OpenLayers.Format.WMTSCapabilities.v1_0_0.prototype.readers.wmts, {
        "Layer": function(node, obj) {
            var layer = {
                styles: [],
                formats: [],
                tileMatrixSetLinks: []
            };
            //layer.layers = []; // this layer has nothing to do in here ! (OL Bugfix)
            this.readChildNodes(node, layer);
            obj.layers.push(layer);
        },
        /*
        // version avec tileMatrix id en clé
        "TileMatrixSetLimits": function(node, obj) {
            obj.tileMatrixSetLimits = {};
            this.readChildNodes(node, obj.tileMatrixSetLimits);
        },
        "TileMatrixLimits": function(node, obj) {
            var o = this.readChildNodes(node);
            obj[o.tileMatrix] = o;
        },
        */
        // version avec TileMatrixSetLimits sous forme de tableau
        "TileMatrixSetLimits": function(node, obj) {
            obj.tileMatrixSetLimits = [];
            this.readChildNodes(node, obj);
        },
        "TileMatrixLimits": function(node, obj) {
            var tileMatrixLimits = {};
            this.readChildNodes(node, tileMatrixLimits);
            obj.tileMatrixSetLimits.push(tileMatrixLimits);
        },
        "MinTileRow": function(node, obj) {
            obj.minTileRow = parseInt(this.getChildValue(node)); 
        },
        "MaxTileRow": function(node, obj) {
            obj.maxTileRow = parseInt(this.getChildValue(node)); 
        },
        "MinTileCol": function(node, obj) {
            obj.minTileCol = parseInt(this.getChildValue(node)); 
        },
        "MaxTileCol": function(node, obj) {
            obj.maxTileCol = parseInt(this.getChildValue(node)); 
        },
        "TileMatrix": function(node, obj) {
            // node could be child of wmts:TileMatrixSet or wmts:TileMatrixLimits
            if (obj.identifier) {
                // node is child of wmts:TileMatrixSet
                var tileMatrix = {
                    supportedCRS: obj.supportedCRS
                };
                this.readChildNodes(node, tileMatrix);
                obj.matrixIds.push(tileMatrix);
            } else {
                obj.tileMatrix = this.getChildValue(node);
            }
        },
        "LegendURL": function(node, obj) {
            obj.legends = obj.legends || [];
            var legend = {
                format: node.getAttribute("format"),
                href: node.getAttribute("xlink:href")
            };
            var width = node.getAttribute("width"),
                height = node.getAttribute("height"),
                minScaleDenominator = node.getAttribute("minScaleDenominator"),
                maxScaleDenominator = node.getAttribute("maxScaleDenominator");
            if (width) {
                legend.width = width;
            }
            if (height) {
                legend.height = height;
            }
            if (minScaleDenominator) {
                legend.minScaleDenominator = minScaleDenominator;
            }
            if (maxScaleDenominator) {
                legend.maxScaleDenominator = maxScaleDenominator;
            }
            obj.legends.push(legend);
        },
        "InfoFormat": function(node, obj) {
            obj.infoFormats = obj.infoFormats || [];
            obj.infoFormats.push(this.getChildValue(node));
        }
    }
);


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
                {name: "queryable", type: "boolean"},
                {name: "llbbox", mapping: "bounds", convert: function(v){
                    return [v.left, v.bottom, v.right, v.top];
                }},
                {name: "formats"}, // array
                {name: "infoFormats"}, // array
                {name: "styles"}, // array of Objects {abstract, identifier, isDefault, keywords, title}
                {name: "keywords"} // Object
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
        var operationsMetadata = data.operationsMetadata,
            url = operationsMetadata.GetTile.dcp &&
                operationsMetadata.GetTile.dcp.http &&
                operationsMetadata.GetTile.dcp.http.get; // when using georchestra branch in georchestra/openlayers
                //operationsMetadata.GetTile.dcp.http.get[0].url; // when using openlayers master (and TODO: check that corresponding constraint is KVP)
        
        var layers = data.contents && data.contents.layers;
        var tileMatrixSets = data.contents && data.contents.tileMatrixSets;
        
        // compute all server-supported resolutions, in order to build a serverResolutions array
        // we create a temporary data structure to speed up future lookups.
        var tileMatrixSetsResolutions = {}, tileMatrixSetsScales = {};
        Ext.iterate(tileMatrixSets, function(tileMatrixSetId, tileMatrixSet) {
            tileMatrixSetsResolutions[tileMatrixSetId] = {};
            tileMatrixSetsScales[tileMatrixSetId] = {};
            Ext.each(tileMatrixSet.matrixIds, function(matrixId) {
                tileMatrixSetsScales[tileMatrixSetId][matrixId.identifier] = 
                    matrixId.scaleDenominator;
                tileMatrixSetsResolutions[tileMatrixSetId][matrixId.identifier] = 
                    OpenLayers.Util.getResolutionFromScale(
                        1/matrixId.scaleDenominator, "Meter"
                    );
            });
        });
        
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
                    values.queryable = !!operationsMetadata.GetFeatureInfo;
                    matrixSet = this.matrixSet(layer);

                    // compute server supported resolutions array & min/maxScale for the chosen matrixSet
                    var resolutions = [], minScaleDenominator, maxScaleDenominator, tileMatrixSetLink,
                        tileMatrixSetLinks = layer.tileMatrixSetLinks;
                    for (var j=0, len=tileMatrixSetLinks.length; j<len; j++) {
                        tileMatrixSetLink = tileMatrixSetLinks[j];
                        if (tileMatrixSetLink.tileMatrixSet === matrixSet) {
                            if (tileMatrixSetLink.tileMatrixSetLimits) {
                                Ext.each(tileMatrixSetLink.tileMatrixSetLimits, function(tileMatrixSetLimit) {
                                    resolutions.push(tileMatrixSetsResolutions[matrixSet][tileMatrixSetLimit.tileMatrix]);
                                    var scale = tileMatrixSetsScales[matrixSet][tileMatrixSetLimit.tileMatrix];
                                    if (!minScaleDenominator || minScaleDenominator > scale) {
                                        minScaleDenominator = scale;
                                    }
                                    if (!maxScaleDenominator || maxScaleDenominator < scale) {
                                        maxScaleDenominator = scale;
                                    }
                                })
                            }
                            break;
                        }
                    }
                    options = {
                        url: url,
                        layer: layer.identifier,
                        name: layer.title,
                        format: this.imageFormat(layer),
                        matrixSet: matrixSet,
                        matrixIds: tileMatrixSets[matrixSet].matrixIds,
                        style: this.layerStyle(layer)
                    };
                    if (resolutions.length) {
                        resolutions.sort(function(a,b){
                            return b-a;
                        })
                        Ext.apply(options, {
                            resolutions: resolutions,
                            minScale: 1/maxScaleDenominator,
                            maxScale: 1/(minScaleDenominator-1)
                            // Note: "minus one" added to prevent IGN's GeoPortail layer 
                            // from not appearing at ScaleDenominator = 2132.7295838497840572
                        });
                    }
                    if (this.meta.clientZoomEnabled) {
                        options.serverResolutions = resolutions;
                    }
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
