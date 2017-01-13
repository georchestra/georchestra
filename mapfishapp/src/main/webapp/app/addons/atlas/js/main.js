/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {

    // boolean, set to true when the addon is opened
    active: null,

    // number
    maxFeatures: null,

    // the GeoExt layer record on which we operate
    layerRecord: null,

    // OpenLayers features to send to printer
    features: null,

    // the attributeStore of our features
    attributeStore: null,

    // GeoExt.data.MapFishPrintv3Provider
    printProvider: null,

    // string
    _geometryName: null,
    _geometryType: null,

    // _mapRatios
    _mapRatios: null,

    // current ext window
    window: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        // FIXME
        // strange: this seems needed only when the addon belongs to a layer's action menu:
        this.title = this.getText(record);
        this.qtip = this.getQtip(record);
        this.tooltip = this.getTooltip(record);
        this.maxFeatures = this.options.maxFeatures;
        this.iconCls = this.options.iconCls;
        // end strange

        this.maxFeatures = this.options.maxFeatures;
        this.sep = this.tr("labelSeparator");

        if (this.target) {
            this.components = this.target.insertButton(this.position, {
                xtype: "button",
                tooltip: this.getTooltip(record),
                iconCls: "atlas-icon",
                listeners: {
                    "click": this.menuAction,
                    scope: this
                }
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.Item({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "atlas-icon",
                listeners: {
                    "click": this.menuAction,
                    scope: this
                }
            });
        }

        this.printProvider = new GeoExt.data.MapFishPrintv3Provider({
            method: "POST",
            autoLoad: true,
            url: this.options.atlasServerUrl,
            listeners: {
                "loadcapabilities": function(pp, caps) {
                    if (caps === "") { // FIXME: is an object
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_connect_printserver_error")
                        });
                    } else {
                        // compute & cache map ratios per layout
                        this._mapRatios = {};
                        Ext.each(caps.layouts, function(layout) {
                            Ext.each(layout.attributes, function(a) {
                                // there should be only one attribute with type MapAttributeValues per layout
                                if (a.type === "MapAttributeValues") {
                                    this._mapRatios[layout.name] = a.clientInfo.width / a.clientInfo.height;
                                }
                            }, this)
                        }, this)
                    }
                },
                "beforeencodelayer": function(printProvider, layer) {
                    // to overcome a limitation in which mfpv3 print provider cannot be mixed with v2
                    Ext.apply(printProvider.encoders.layers,
                        GeoExt.data.MapFishPrintv3Provider.prototype.encodersOverride.layers);
                    Ext.apply(printProvider.encoders.legends,
                        GeoExt.data.MapFishPrintv3Provider.prototype.encodersOverride.legends);
                    // do not print vector layers
                    if (layer.CLASS_NAME === "OpenLayers.Layer.Vector") {
                        return false;
                    }
                },
                scope: this
            }
        });
    },


    /**
     * @function menuAction
     *
     * Ext component's (button or menuitem) handler
     * It's role is to select the layer on which to operate
     */
    menuAction: function() {
        if (this.active) {
            return;
        }
        this._selectLayer();
    },


    /**
     * @function _selectLayer
     * Display a dialog to the user, allowing him to select a layer
     * among those which are currently loaded and suitable. 
     */
    _selectLayer: function() {
        this.active = true;
        var atlasLayersStore = new GeoExt.data.LayerStore({
            fields: GEOR.ows.getRecordFields()
        });
        this.mapPanel.layers.each(function(layerRecord) {
            // we only act on vector layers that have a WFS counterpart:
            if (layerRecord.hasEquivalentWFS()) {
                atlasLayersStore.add(layerRecord); // layerRecord.copy() ?
            }
        });
        var win = new Ext.Window({
            title: this.tr("Atlas"),
            width: 400,
            height: 300,
            autoHeight: true,
            constrainHeader: true,
            bodyStyle: {
                padding: "5px 5px 0",
                "background-color": "white"
            },
            border: false,
            closable: true,
            closeAction: "close",
            layout: 'fit',
            items: [{
                layout: "form",
                labelSeparator: this.sep,
                border: false,
                width: 400,
                items: [{
                    xtype: "combo",
                    mode: 'local',
                    store: atlasLayersStore,
                    fieldLabel: this.tr("atlas_atlaslayer"),
                    emptyText: this.tr("atlas_emptylayer"),
                    height: 30,
                    anchor: '95%',
                    editable: false,
                    typeAhead: false,
                    triggerAction: "all",
                    valueField: "layer",
                    displayField: "title",
                    listeners: {
                        "select": function(combo, record) {
                            win.close();
                            // set layer record:
                            this.layerRecord = record;
                            // compute attributeStore (get it from the server)
                            this._getAttributeStore(record);
                        },
                        "close": function() {
                            this.active = false;
                        },
                        scope: this
                    }
                }]
            }]
        }).show();
    },


    /**
     * @function layerTreeHandler
     *
     * Handler for the layer tree Actions menu.
     *
     * scope is set for having the addons as this
     *
     * @param menuitem - menuitem which will receive the handler
     * @param event - event which trigger the action
     * @param layerRecord - layerRecord on which operate
     */
    layerTreeHandler: function(menuitem, event, layerRecord) {
        if (this.active) {
            return;
        }
        // set layer record:
        this.layerRecord = layerRecord;
        // compute attributeStore (get it from the server)
        this._getAttributeStore(layerRecord);
    },


    /**
     * Method: _getAttributeStore
     * @param layerRecord
     */
    _getAttributeStore: function(layerRecord) {
        GEOR.waiter.show();
        var pseudoRecord = {
            owsURL: layerRecord.get("WFS_URL"),
            typeName: layerRecord.get("WFS_typeName")
        };
        this.attributeStore = GEOR.ows.WFSDescribeFeatureType(pseudoRecord, {
            extractFeatureNS: true,
            success: function() {
                // in the following, we're fetching the features silently while the user 
                // fills in the form.
                //
                // we get the geometry column name, and remove the corresponding record from store
                var idx = this.attributeStore.find("type", GEOR.ows.matchGeomProperty);
                if (idx > -1) {
                    // now that we have an attribute store, go on with the dialog:
                    this._buildFormDialog();
                    //
                    // we have a geometry !
                    var r = this.attributeStore.getAt(idx),
                        geometryName = r.get("name");
                    // create the protocol:
                    var protocol = GEOR.ows.WFSProtocol(pseudoRecord, this.map, {
                        geometryName: geometryName
                    });
                    this._getFeatures(protocol); // note that this could be done later (when the user submits the form)...
                    this._geometryName = geometryName; // TODO: check we still need it.
                    this._geometryType = r.get("type");
                    // remove geometry from attribute store (useless in combos)
                    this.attributeStore.remove(r);
                } else {
                    GEOR.util.infoDialog({
                        msg: this.tr("querier.layer.no.geom")
                    });
                }
                this.attributeStore.sort('name');
            },
            failure: function() {
                GEOR.util.errorDialog({
                    msg: this.tr("querier.layer.error")
                });
            },
            scope: this
        });
    },


    /**
     * @function _getFeatures
     *
     */
    _getFeatures: function(protocol) {
        // we need to fetch them async through WFS
        protocol.read({
            maxFeatures: this.maxFeatures + 1,
            filter: new OpenLayers.Filter.Spatial({
                type: "INTERSECTS",
                value: this.map.getMaxExtent()
            }),
            //propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
            propertyNames: this.attributeStore.collect("name"), // trying to remove the geometry from the response (keeping the bboxes)
            callback: function(response) {
                if (!response.success()) {
                    alert("Error while performing WFS getFeature"); // FIXME
                    return;
                }
                if (response.features.length > this.maxFeatures) {
                    GEOR.util.errorDialog({
                        msg: this.tr("atlas_too_many_features") +
                            (this.maxFeatures) + this.tr("atlas_too_many_features_after_nb")
                    });
                    return;
                }
                this.features = response.features;
                // update window title with feature count:
                this.window.setTitle([
                    this.window.title,
                    " - ",
                    this.features.length,
                    " ",
                    this.tr("atlas_features")
                ].join(''));
            },
            scope: this
        });
    },


    /**
     * @function resultPanelHandler
     *
     * Handler for the result panel Actions menu.
     *
     * scope is set to the addon
     *
     * @param resultpanel - resultpanel on which the handler must be operated
     */
    resultPanelHandler: function(resultPanel) {
        if (this.active) {
            return;
        }
        // we get the selected features from resultsPanel:
        this.features = resultPanel.getSelectedFeatures();

        // we get the attributeStore of the features:
        var attributeStoreData = [];
        Ext.each(resultPanel.getModel().getFields(), function(fieldname) {
            attributeStoreData.push([fieldname]);
        });
        this.attributeStore = new Ext.data.ArrayStore({
            fields: ["name"],
            data: attributeStoreData.sort(function(a,b){
                return GEOR.util.sortFn(a[0],b[0]);
            })
        });

        // we try to get the original layerRecord:
        var layerRecord;
        this.mapPanel.layers.each(function(r) {
            // find layerRecord based on title
            // this is very fragile, but it seems difficult to do better right now
            if (resultPanel.title === GEOR.util.shortenLayerName(r.get("title"))) {
                layerRecord = r;
            }
        });
        this.layerRecord = layerRecord;

        // go on with the dialog:
        this._buildFormDialog();
    },


    /**
     * Method: _buildFormDialog
     * When this method is called, we always know on which layer we operate
     */
    _buildFormDialog: function() {
        this.active = true;
        var formPanel = (new GEOR.Addons.Atlas.Form(this)).form,
            basicTitle = [
                this.tr("Atlas"),
                ' - ',
                this.layerRecord.get("title")
            ].join(''),
            title = (this.features) ? 
                basicTitle + " - " + this.features.length + " " + this.tr("atlas_features") :
                basicTitle;

        this.window = new Ext.Window({
            title: title,
            minWidth: 550,
            width: 700,
            autoHeight: true,
            constrainHeader: true,
            bodyStyle: {
                padding: "10px 10px",
                "background-color": "white"
            },
            closable: true,
            closeAction: "close",
            listeners: {
                "close": function() {
                    // to allow new windows to be opened:
                    this.active = false;
                    // reset features cache:
                    this.features = null;
                },
                scope: this
            },
            items: formPanel,
            buttons: [{
                text: this.tr("atlas_cancel"),
                handler: function() {
                    this.window.close();
                },
                scope: this
            }, {
                text: this.tr("atlas_submit"),
                // TODO: disable button as long as this.features.length is 0
                width: 100,
                iconCls: this.options.iconCls,
                handler: function(b) {
                    var form = formPanel.getForm();
                    if (form.isValid()) {
                        this.createSpec(
                            form.getFieldValues()
                        );
                        this.window.close();
                    } else {
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_form_invalid")
                        });
                    }
                },
                scope: this
            }]
        });
        this.window.show();
    },


    /**
     * @function createSpec - parse form values, create spec, POST it.
     * @private
     *
     * @param v - form values as returned by Ext.form.BasicForm.getFieldValues()
     */
    createSpec: function(v) {
        // see https://gist.github.com/Vampouille/1ceb047465047dd1f9fd
        // for the atlas spec
        var spec = {
            email: v.email,
            layout: v.layout,
            outputFormat: v.outputFormat,
            outputFilename: v.outputFilename + "." + v.outputFormat,
            dpi: v.dpi,
            projection: this.map.getProjection(),
            displayLegend: v.displayLegend,
            baseLayers: this.encodeBaseLayers(),
            featureLayer: this.encodeAtlasLayer(),
            pages: this.getPages(v)
        };
        OpenLayers.Request.POST({
            url: this.options.atlasServerUrl,
            data: new OpenLayers.Format.JSON().write(spec),
            success: function() {
                GEOR.util.infoDialog({
                    title: this.title,
                    msg: this.tr("atlas_submit_success")
                });
            },
            failure: function() {
                GEOR.util.errorDialog({
                    msg: this.tr("atlas_submit_fail")
                });
            },
            scope: this
        });
    },


    /**
     * @function encodeBaseLayers - Encode all other mapPanel layers than the atlas layer using the print provider
     *
     * @returns {Array}
     */
    encodeBaseLayers: function() {
        var encodedLayers = [],
            wmsc2wms = this.options.wmsc2wms;
        this.mapPanel.layers.each(function(r) {
            var encodedLayer,
                l = r.getLayer();
            // loop on all visible layers
            // not the atlas layer
            // not the vector layers used by addons (matching "__georchestra")
            if (l.getVisibility() && r !== this.layerRecord && !/^__georchestra/.test(l.name)) {
                // use print provider to encode
                encodedLayer = this.printProvider.encodeLayer(l, this.map.getMaxExtent());
                if (!encodedLayer) {
                    // eg if a vector layer name does not follow
                    // the "name begins with "__georchestra" convention"
                    return;
                }
                // substitute known WMS-C instances by WMS instances serving same layers:
                if (wmsc2wms && wmsc2wms.hasOwnProperty(encodedLayer.baseURL)) {
                    encodedLayer.baseURL = wmsc2wms[encodedLayer.baseURL];
                }
                // we get rid of scale limits, 
                // since they are already taken care of by the current layer style
                delete encodedLayer.maxScaleDenominator;
                delete encodedLayer.minScaleDenominator;
                encodedLayers.push(encodedLayer);
            }
        }, this);
        // reverse is required to get correct the print order:
        return encodedLayers.reverse();
    },


    /**
     * @function encodeAtlasLayer - Encode atlas layer
     *
     * @returns {Object}
     */
    encodeAtlasLayer: function() {
        var layer = this.layerRecord.getLayer(),
            encodedLayer = this.printProvider.encodeLayer(layer, layer.getExtent());

        // we want to get rid of scale limits:
        delete encodedLayer.maxScaleDenominator;
        delete encodedLayer.minScaleDenominator;
        return encodedLayer;
    },


    /**
     * @function getPages
     *
     * @returns {Array}
     */
    getPages: function(values) {
        var pages = [];
        // one page per feature
        // when this code is run, we're sure that this.feature is not empty
        Ext.each(this.features, function(feature) {
            pages.push(this.getPage(feature, values));
        }, this);
        return pages;
    },


    /**
     * @function getPage
     *
     * @returns {Array}
     */
    getPage: function(feature, values) {
        var center,
            a = feature.attributes,
            page = {
            "title": values["title_method"].inputValue == "same" ?
                values.titleText : a[values.titleField],
            "subtitle": values["subtitle_method"].inputValue == "same" ?
                values.subtitleText : a[values.subtitleField]
        };
        if (values.outputFormat == "zip") {
            if (values.prefix_field != "") {
                // TODO: sanitization of a[values.prefix_field] ?
                page.filename = values.outputFilename + "_" + a[values.prefix_field] + ".pdf";
            } else {
                // FIXME: feature.fid is fragile
                page.filename = values.outputFilename + "_" + feature.fid.split(".")[1] + ".pdf";
            }
        } else {
            // stupid server requests that every page has a filename,
            // even if it is not being used
            page.filename = "truite";
        }
        // if geom is point || user has manually chosen a scale
        if (this._geometryType == "gml:PointPropertyType" ||
            feature.geometry.CLASS_NAME == "OpenLayers.Geometry.Point" ||
            values["scale_method"].inputValue == "manual") {

            center = feature.geometry.getCentroid();
            page.center = [center.x, center.y];
            page.scale = values["scale_manual"] || this.options.defaultPointScale;
        } else {
            // we need to adapt bbox to server ratio & print template
            var serverRatio = this._mapRatios[values.layout],
                featureBbox = feature.geometry.getBounds();

            if (featureBbox.getHeight() == 0) {
                // means that only feature width should be considered for inclusion
                page.bbox = this._getBox({
                    bounds: featureBbox,
                    fitTo: "width",
                    ratio: serverRatio
                });
            } else {
                var featureRatio = featureBbox.getWidth() / featureBbox.getHeight();
                if (featureRatio > serverRatio) {
                    // means that feature width is the greatest
                    page.bbox = this._getBox({
                        bounds: featureBbox,
                        fitTo: "width",
                        ratio: serverRatio
                    });
                } else {
                    page.bbox = this._getBox({
                        bounds: featureBbox,
                        fitTo: "height",
                        ratio: serverRatio
                    });
                }
            }
        }
        return page;
    },


    /**
     * @function _getBox
     *
     * Returns an array of page bounds given a bounds object
     * and a fitTo property 
     */
    _getBox: function(o) {
        var out = o.bounds.clone(),
            center = out.getCenterLonLat();

        if (o.fitTo == "width") {
            // we keep out.left and out.right
            var width = out.getWidth();
            out.top = center.lat + width / ( 2 * o.ratio);
            out.bottom = center.lat - width / ( 2 * o.ratio);
        } else {
            // fitTo height
            // we keep out.bottom and out.top
            var height = out.getHeight();
            out.right = center.lon + height / ( 2 * o.ratio);
            out.left = center.lon - height / ( 2 * o.ratio);
        }
        return out.scale(1 + this.options.buffer).toArray();
    },


    /**
     * @function tr
     *
     * Translate string
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    },

    /**
     * @function destroy
     *
     * Destroy the addon
     *
     */
    destroy: function() {
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});