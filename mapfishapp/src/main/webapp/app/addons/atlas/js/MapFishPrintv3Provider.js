/**
 * Copyright (c) 2015 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @require GeoExt/data/PrintProviderBase.js
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = PrintProvider
 *  base_link = `Ext.util.Observable <http://dev.sencha.com/deploy/dev/docs/?class=Ext.util.Observable>`_
 */
Ext.namespace("GeoExt.data");

/** api: example
 *  Minimal code to print as much of the current map extent as possible as
 *  soon as the print service capabilities are loaded, using the first layout
 *  reported by the print service:
 *
 *  .. code-block:: javascript
 *
 *      var mapPanel = new GeoExt.MapPanel({
 *          renderTo: "mappanel",
 *          layers: [new OpenLayers.Layer.WMS("wms", "/mapserver/wms",
 *              {layers: "tasmania_state_boundaries"})],
 *          center: [146.56, -41.56],
 *          zoom: 7
 *      });
 *      var printProvider = new GeoExt.data.MapFishPrintv3Provider({
 *          url: "/printserver/pdf",
 *          listeners: {
 *              "loadcapabilities": function() {
 *                  var printPage = new GeoExt.data.PrintPage({
 *                      printProvider: printProvider
 *                  });
 *                  printPage.fit(mapPanel, true);
 *                  printProvider.print(mapPanel, printPage);
 *              }
 *          }
 *      });
 */

/** api: constructor
 *  .. class:: MapFishPrintv3Provider
 *
 *  Provides an interface to a Mapfish print module version 3.
 */
GeoExt.data.MapFishPrintv3Provider = Ext.extend(GeoExt.data.PrintProviderBase, {

    /** private: method[buildStores]
     *
     *  Build the JSON stores
     */
    buildStores: function() {
        this.scales = new Ext.data.JsonStore({
            sortInfo: {field: "value", direction: "DESC"},
            fields: ['name', {name: "value", type: "float"}]
        });

        this.dpis = new Ext.data.JsonStore({
            sortInfo: {field: "value", direction: "ASC"},
            fields: ['name', {name: "value", type: "int"}]
        });

        // Optional outputformats
        if (this.outputFormatsEnabled === true) {
            this.outputFormats = new Ext.data.JsonStore({
                root: "formats",
                sortInfo: {field: "name", direction: "ASC"},
                fields: ["name"]
            });
        }

        this.layouts = new Ext.data.JsonStore({
            root: "layouts",
            fields: ["name", "attributes", "size"]
        });
    },

    /** private: method[getCapabilitiesURL]
     *
     *  Get capabilities URL.
     */
    getCapabilitiesURL: function() {
        return this.url + "capabilities.json";
    },

    /** private: method[toObject]
     */
    toObject: function(array) {
        var result = [];
        Ext.each(array, function(value) {
            result.push({
                'value': value
            });
        });
        return result;
    },

    /** private: method[viewAlert]
     */
    viewAlert: function(message) {
        window.alert(message);
        throw message;
    },

    /** private: method[arrayToObjects]
     */
    arrayToObjects: function(array) {
        var objects = [];
        Ext.each(array, function(item) {
            objects.push({
                value: item,
                name: item
            });
        });
        return objects;
    },

    /** api: method[setLayout]
     *  :param layout: ``Ext.data.Record`` the record of the layout.
     *
     *  Sets the layout for this printProvider.
     */
    setLayout: function(layout) {
        this.setAttributes(layout.get('attributes'));

        var mapAttributes = this.getAttributesByType('MapAttributeValues');
        if (!mapAttributes) {
            this.viewAlert("This layout doesn't contain any map");
        }
        if (mapAttributes.length != 1) {
            this.viewAlert("This layout contains more than one map");
        }
        var overviewMapAttributes = this.getAttributesByType('OverviewMapAttributeValues');
        if (overviewMapAttributes && overviewMapAttributes.length != 1) {
            this.viewAlert("This layout contains more than one overview map");
        }
        var legendAttributes = this.getAttributesByType('LegendAttributeValue');
        if (legendAttributes && legendAttributes.length != 1) {
            this.viewAlert("This layout contains more than one legend");
        }

        layout.set('size', {
            width: mapAttributes[0].clientInfo.width,
            height: mapAttributes[0].clientInfo.height
        });
        layout.set('rotation', true);
        this.scales.loadData(this.arrayToObjects(mapAttributes[0].clientInfo.scales));
        this.dpis.loadData(this.arrayToObjects(mapAttributes[0].clientInfo.dpiSuggestions));
        this.setDpi(this.dpis.getAt(0));

        GeoExt.data.MapFishPrintv3Provider.superclass.setLayout.apply(this, arguments);
    },

    /** private: method[loadStores]
     */
    loadStores: function() {
        this.layouts.loadData(this.capabilities);
        this.setLayout(this.layouts.getAt(0));

        // In rare cases (YAML+MFP-dependent) no Output Formats are returned
        if (this.outputFormatsEnabled && this.capabilities.outputFormats) {
            this.outputFormats.loadData(this.capabilities);
            var defaultOutputIndex = this.outputFormats.find('name', this.defaultOutputFormatName);
            this.setOutputFormat(defaultOutputIndex > -1 ? this.outputFormats.getAt(defaultOutputIndex) : this.outputFormats.getAt(0));
        }
        this.fireEvent("loadcapabilities", this, this.capabilities);
    },

    /** private: method[validatePrintOptions]
     */
    validatePrintOptions: function(pages) {
        if (pages.length != 1) {
            this.fireEvent("invalidspec", this);
            return false;
        }
        return true;
    },

    /** api: method[requestPrint]
     *
     *  :param map: ``GeoExt.MapPanel`` or ``OpenLayers.Map`` The map to print.
     *  :param pages: ``Array`` of :class:`GeoExt.data.PrintPage` or
     *      :class:`GeoExt.data.PrintPage` page(s) to print.
     *  :param encodedLayers: ``Object`` used to describe the layers
     *  :param encodedOverviewLayers: ``Object`` used to describe the layers
     *  :param encodedLegends: ``Object``used to describe the legends
     *  :param callback: ``Function`` called then the print is done
     */
    requestPrint: function(map, pages, encodedLayers, encodedOverviewLayers, encodedLegends, callback) {
        var outputFormat = this.outputFormat ? this.outputFormat.get("name") : this.defaultOutputFormatName;
        var page = pages[0];

        var jsonData = {
            layout: this.layout.get("name"),
            outputFormat: outputFormat,
            attributes: {}
        };

        // do a clone
        Ext.apply(jsonData.attributes, this.customParams);

        var mapAttributes = this.getAttributesByType("MapAttributeValues");
        var overviewMapAttributes = this.getAttributesByType("OverviewMapAttributeValues");
        var legendAttributes = this.getAttributesByType("LegendAttributeValue");

        jsonData.attributes[mapAttributes[0].name] = {
            projection: map.getProjection(),
            dpi: this.dpi.get("value"),
            rotation: -page.rotation,
            center: [page.center.lon, page.center.lat],
            scale: page.scale.get("value"),
            longitudeFirst: true,
            layers: encodedLayers
        };

        if (overviewMapAttributes && encodedOverviewLayers) {
            jsonData.attributes[overviewMapAttributes[0].name] = {
                layers: encodedOverviewLayers
            };
        }

        if (legendAttributes && encodedLegends) {
            jsonData.attributes[legendAttributes[0].name] = {
                "classes": encodedLegends
            };
        }

        Ext.Ajax.request({
            url: this.url + "report." + outputFormat,
            method: 'POST',
            jsonData: jsonData,
            headers: { "Content-Type": "application/json; charset=" + this.encoding },
            success: function(response) {
                callback(Ext.decode(response.responseText));
            },
            failure: function(response) {
                this.fireEvent("printexception", this, response);
            },
            params: this.baseParams,
            scope: this
        });
    },

    /** api: method[getStatus]
     *  Get the status of a specific job
     *
     *  :param job: ``String`` the job specification
     *  :param callback: ``Function`` the function called with the status
     */
    getStatus: function(job, callback) {
        Ext.Ajax.request({
            url: this.url + 'status/' + job.ref + '.json',
            method: 'GET',
            success: function(response) {
                callback(job, true, Ext.decode(response.responseText));
            },
            failure: function(response) {
                callback(job, false);
                this.fireEvent("printexception", this, response);
            },
            params: this.baseParams,
            scope: this
        });
    },

    /** api: method[getDownloadURL]
     *  Get the URL to download the result for a job
     *
     *  :param job: ``String`` the job specification
     */
    getDownloadURL: function(job) {
        return this.url + 'report/' + job.ref;
    },

    /** private: property[encodersOverride]
     *  ``Object`` Encoders override for all print content
     */
    encodersOverride: {
        "layers": {
            "WMS": function(layer) {
                var enc = this.encoders.layers._HTTPRequest.call(this, layer);
                Ext.apply(enc, {
                    type: layer.singleTile ? 'wms' : "tiledwms",
                    layers: [layer.params.LAYERS].join(",").split(","),
                    imageFormat: layer.params.FORMAT,
                    styles: [layer.params.STYLES].join(",").split(",")
                });
                var param;
                for (var p in layer.params) {
                    param = p.toLowerCase();
                    if (layer.params[p] != null && !layer.DEFAULT_PARAMS[param] &&
                    "layers,styles,width,height,srs".indexOf(param) == -1) {
                        if (!enc.customParams) {
                            enc.customParams = {};
                        }
                        enc.customParams[p] = layer.params[p];
                    }
                }
                if (! layer.singleTile) {
                    enc.tileSize = [layer.tileSize.w, layer.tileSize.h];
                }
                return enc;
            },
            "WMTS": function(layer) {
                var enc = this.encoders.layers._HTTPRequest.call(this, layer);
                enc = Ext.apply(enc, {
                    type: 'wmts',
                    layer: layer.layer,
                    version: layer.version,
                    requestEncoding: layer.requestEncoding,
                    style: layer.style,
                    dimensions: layer.dimensions,
                    dimensionParams: layer.params,
                    matrixSet: layer.matrixSet,
                    imageFormat: layer.format,
                    matrices: []
                });
                if (layer.requestEncoding === "REST") {
                    if (enc.baseURL.indexOf("{") === -1) {
                        if (!enc.baseURL.match(/\/$/)) {
                            enc.baseURL += "/";
                        }
                        enc.baseURL += layer.version + "/" + layer.layer + "/" + layer.style + "/";
                        if (layer.dimensions) {
                            for (var i = 0; i < layer.dimensions.length; i++) {
                                enc.baseURL += "{" + layer.dimensions[i] + "}/";
                            }
                        }
                        enc.baseURL += "{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.";
                        enc.baseURL += layer.formatSuffix;
                    }
                }
                if (layer.matrixIds) {
                    Ext.each(layer.matrixIds, function(matrixId) {
                        enc.matrices.push({
                            identifier: matrixId.identifier,
                            matrixSize: [matrixId.matrixWidth,
                                    matrixId.matrixHeight],
                            scaleDenominator: matrixId.scaleDenominator,
                            tileSize: [matrixId.tileWidth, matrixId.tileHeight],
                            topLeftCorner: [matrixId.topLeftCorner.lon,
                                    matrixId.topLeftCorner.lat]
                        });
                    });
                }
                else {
                    Ext.each(layer.serverResolutions, function(resolution, index) {
                        enc.matrices.push({
                            identifier: index,
                            matrixSize: [
                                Math.ceil((layer.maxExtent.right - layer.maxExtent.left) / resolution / layer.tileSize.w),
                                Math.ceil((layer.maxExtent.top - layer.maxExtent.bottom) / resolution / layer.tileSize.h)
                            ],
                            scaleDenominator: resolution / 0.28E-3 
                                * OpenLayers.METERS_PER_INCH
                                * OpenLayers.INCHES_PER_UNIT[layer.units],
                            tileSize: [layer.tileSize.w, layer.tileSize.h],
                            topLeftCorner: [
                                layer.getTileOrigin().lon,
                                layer.getTileOrigin().lat
                            ]
                        });
                    });
                }
                return enc;
            },
            "OSM": function(layer) {
                var enc = this.encoders.layers._HTTPRequest.call(this, layer);
                return Ext.apply(enc, {
                    type: 'osm',
                    baseURL: enc.baseURL.substr(0, enc.baseURL.indexOf("$")),
                    layer: layer.layername,
                    maxExtent: layer.maxExtent.toArray(),
                    tileSize: [layer.tileSize.w, layer.tileSize.h],
                    resolutions: layer.serverResolutions || layer.resolutions,
                    imageExtension: enc.baseURL.split('.').pop()
                });
            },
            "Vector": function(layer, bounds) {
                var enc = this.encoders.features.GeoJsonWithStyle.call(this, layer, bounds);
                // don't send empty layer
                if (!enc.geoJson.features.length === 0) {
                    return;
                }

                enc = {
                    type: "geojson",
                    style: Ext.apply(enc.styles, {
                        version: "1",
                        styleProperty: "_gx_style"
                    }),
                    geoJson: enc.geoJson
                }

                Ext.apply(enc, this.encoders.layers._Layer.call(this, layer));

                return enc;
            }
        },
        "legends": {
            "gx_urllegend": function(legend) {
                var enc = this.encoders.legends.base.call(this, legend);
                enc[0].classes.push({
                    name: "",
                    icons: [this.getAbsoluteUrl(legend.items.get(1).url)]
                });
                return enc;
            }
        }
    },

    /** api: method[supportProgress]
     *  Return true if it support progress (Print job queue with status)
     */
    supportProgress: function() {
        return true;
    }
});
