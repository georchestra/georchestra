/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {

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

        /**
         * Atlas request is submitted on featurelayerready event
         *
        this.events.on({
            "featurelayerready": function(spec) {
                console.log(spec);
                var format = new OpenLayers.Format.JSON();
                OpenLayers.Request.POST({
                    url: this.options.atlasServerUrl,
                    data: format.write(spec),
                    success: function() {
                        GEOR.helper.msg(this.title, this.tr("atlas_submit_success"));
                    },
                    failure: function() {
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_submit_fail")
                        });
                    },
                    scope: this
                });
            },
            scope: this
        });*/

        this.printProvider = new GeoExt.data.MapFishPrintv3Provider({
            method: "POST",
            autoLoad: true,
            url: this.options.atlasServerUrl,
            listeners: {
                "loadcapabilities": function(pp, caps) {
                    if (caps === "") {
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_connect_printserver_error")
                        });
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
        if (this.window) {
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
            title: this.tr("Select atlas layer"),
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
                    this._geometryName = geometryName;
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
            propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
            callback: function(response) {
                if (!response.success()) {
                    alert("Error while performing WFS getFeature"); // FIXME
                    return;
                }
                if (response.features.length > this.maxFeatures) {
                    alert("Too many features in layer for an atlas, please select the features through a query"); // FIXME
                    return;
                }
                this.features = response.features;

                /*
                Ext.each(wfsFeatures, function(wfsFeature) {

                    this.spec.pages.splice(-1, 0, 
                        _pageFromFeature(wfsFeature, this));

                    pageIdx = pageIdx + 1;

                }, this);

                //Remove empty pages //shouldn't they be removed immediately ?
                Ext.each(this.spec.pages, function(page, idx) {
                    if (page === undefined) {
                        this.spec.pages.splice(idx, 1);
                    }
                }, this);
                */
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
        var form = (new GEOR.Addons.Atlas.Form(this)).form;
        this.window = new Ext.Window({
            title: [
                this.tr("Atlas of layer"),
                ' \"',
                this.layerRecord.get("title"),
                '\"'
            ].join(''),
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
                    this.window = null;
                },
                scope: this
            },
            items: form,
            buttons: [{
                text: this.tr("atlas_cancel"),
                handler: function() {
                    this.window.close();
                },
                scope: this
            }, {
                text: this.tr("atlas_submit"),
                width: 100,
                iconCls: this.options.iconCls,
                handler: function(b) {
                    if (form.isValid()) {
                        this.parseForm(
                            form.getFieldValues()
                        );
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
     * @function parseForm - parse form values
     * @private
     *
     * @param formValues - form values as returned by Ext.form.BasicForm.getFieldValues()
     */
    parseForm: function(formValues) {
        var scaleParameters, titleSubtitleParameters;

        //copy some parameters
        this.spec.outputFormat = formValues.outputFormat;
        this.spec.layout = formValues.layout;
        this.spec.dpi = formValues.dpi;
        this.spec.projection = this.map.getProjection();
        this.spec.email = formValues.email;
        this.spec.displayLegend = formValues.displayLegend;
        this.spec.outputFilename = formValues.outputFilename;

        scaleParameters = {
            scaleManual: formValues["scale_manual"],
            scaleMethod: formValues["scale_method_group"].inputValue,
            scalePadding: formValues["scale_padding"] // FIXME: undefined
        };

        titleSubtitleParameters = {
            titleMethod: formValues["title_method_group"].inputValue,
            titleText: formValues["titleText"],
            titleField: formValues["titleField"],
            subtitleMethod: formValues["title_method_group"].inputValue,
            subtitleText: formValues["subtitleText"],
            subtitleField: formValues["subtitleField"]
        };

        this.spec.baseLayers = this.baseLayers(formValues["atlasLayer"]);

        this.createFeatureLayerAndPagesSpecs(formValues["atlasLayer"], scaleParameters,
            titleSubtitleParameters, formValues["prefix_field"], formValues["resultPanel"]);

        // Form submit is triggered by "featurelayerready" event

        this.window.close();
    },

    /**
     * @function createFeatureLayerAndPagesSpecs
     * @private
     *
     * Build the part of the atlas configuration related to the feature layer and the pages description
     *
     * @param atlasLayer {String} - Name of the atlas layer
     * @param scaleParameters {Object} - Form values related to the scale management
     * @param titleSubtitleParameters {Object} - Form values related to title and subtitle
     * @param fieldPrefix {String} - Attribute to use a prefix for filename generation
     * @param resultPanel {Boolean} - True atlas is generated from result panel actions menu
     *     This will send request to atlas server
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters, titleSubtitleParameters, fieldPrefix, resultPanel) {

        var autoSubmit = true;
        /**
         *
         * Private function to create page object from a feature.
         *
         * @param wfsFeature
         * @param addon
         * @return {Object} or {undefined}
         * @private
         */
        var _pageFromFeature = function(wfsFeature, addon) {
            var page = {}, bounds, bbox;
            // title
            if (titleSubtitleParameters.titleMethod === "same") {
                page.title = titleSubtitleParameters.titleText;
            } else {
                page.title = wfsFeature.attributes[titleSubtitleParameters.titleField];
            }
            // subtitle
            if (titleSubtitleParameters.subtitleMethod === "same") {
                page.subtitle = titleSubtitleParameters.subtitleText;
            } else {
                page.subtitle = wfsFeature.attributes[titleSubtitleParameters.subtitleField];
            }
            // center + scale
            if (scaleParameters.scaleMethod === "manual") {
                page.center = [wfsFeature.geometry.getCentroid().x, wfsFeature.geometry.getCentroid().y];
                page.scale = scaleParameters.scaleManual;
            } else {
                if (!(wfsFeature.geometry instanceof OpenLayers.Geometry.Point)) {
                    bounds = wfsFeature.geometry.getBounds();
                    bbox = bounds.scale(1 + addon.options.bboxBuffer).toArray();
                } else {
                    GEOR.helper.msg(addon.title, addon.tr("atlas_bbox_point_error"), 10); // FIXME - GEOR.helper.msg probably not appropriate here
                    return undefined;
                }
                page.bbox = bbox;
            }
            // filename
            if (fieldPrefix === "") {
                page.filename = pageIdx.toString() + "_atlas.pdf";
            } else {
                page.filename = wfsFeature.attributes[fieldPrefix] + "_" + pageIdx.toString() +
                    "_atlas.pdf";
            }
            return page;
        };

        this.spec.pages = [];
        var pageIdx = 0;

        this.mapPanel.layers.each(function(layerRecord) {
            var layer = layerRecord.get("layer");

            if (layer === atlasLayer) {
                this.spec.featureLayer = this.printProvider.encodeLayer(layer, layer.getExtent());
                //TODO version may not be required by mapfish - check serverside
                if (layer.DEFAULT_PARAMS) {
                    this.spec.featureLayer.version = layer.DEFAULT_PARAMS.version;
                }
                if (this.spec.featureLayer.maxScaleDenominator) {
                    delete this.spec.featureLayer.maxScaleDenominator;
                }
                if (this.spec.featureLayer.minScaleDenominator) {
                    delete this.spec.featureLayer.minScaleDenominator;
                }

                if (resultPanel) {
                    var wfsFeatures = this.resultPanelFeatures;

                    if (wfsFeatures.totalLength >= (this.maxFeatures + 1)) {
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_too_many_features") +
                                (this.maxFeatures + 1) + this.tr("atlas_too_many_features_after_nb")
                        });
                        autoSubmit = false;
                    }

                    wfsFeatures.each(function(record) {
                        this.spec.pages.splice(-1, 0, 
                            _pageFromFeature(record.getFeature(), this)
                        );
                        pageIdx = pageIdx + 1;
                    }, this);

                    //Remove empty page
                    Ext.each(this.spec.pages, function(page, idx) {
                        if (page === undefined) {
                            this.spec.pages.splice(idx, 1);
                        }
                    }, this);

                    if (autoSubmit) {
                        if (this.spec.pages.length === 0) {
                            GEOR.util.errorDialog({
                                msg: this.tr("atlas_no_pages")
                            });
                        } else {
                            this.events.fireEvent("featurelayerready", this.spec);
                        }

                    }
                } else {
                    this.protocol.read({
                        //See GEOR_Querier "search" method
                        maxFeatures: this.maxFeatures + 1,
                        filter: new OpenLayers.Filter.Spatial({
                            type: "INTERSECTS",
                            value: this.map.getMaxExtent()
                        }),
                        propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
                        callback: function(response) {
                            if (!response.success()) {
                                return;
                            }
                            var wfsFeatures = response.features;

                            if (wfsFeatures.length === (this.maxFeatures + 1)) {
                                GEOR.util.errorDialog({
                                    msg: this.tr("atlas_too_many_features") +
                                        (this.maxFeatures + 1) + this.tr("atlas_too_many_features_after_nb"),
                                    scope: this
                                });
                                autoSubmit = false;
                            }
                            Ext.each(wfsFeatures, function(wfsFeature) {

                                this.spec.pages.splice(-1, 0, 
                                    _pageFromFeature(wfsFeature, this));

                                pageIdx = pageIdx + 1;

                            }, this);

                            //Remove empty pages //shouldn't they be removed immediately ?
                            Ext.each(this.spec.pages, function(page, idx) {
                                if (page === undefined) {
                                    this.spec.pages.splice(idx, 1);
                                }
                            }, this);

                            if (autoSubmit) {
                                if (this.spec.pages.length === 0) {
                                    GEOR.util.errorDialog({
                                        msg: this.tr("atlas_no_pages")
                                    });
                                } else {
                                    this.events.fireEvent("featurelayerready", this.spec);
                                }

                            }

                        },
                        scope: this

                    });
                }
            }
        }, this);
    },




    /**
     * @function baseLayers - Encode all other mapPanel layers than the atlas layer using the print provider
     *
     * @param atlasLayer {String}
     * @returns {Array}
     */
    baseLayers: function(atlasLayer) {
        var encodedLayer = null,
            encodedLayers = [];
        this.mapPanel.layers.each(function(layerRecord) {
            if ((layerRecord.get("name") !== atlasLayer) && layerRecord.get("layer").visibility) {

                /**
                 * TODO Do we want to show the resultPanel symbology in the atlas? Currently, we hide the layer because
                 * it hide the current symbology.
                 */
                if (!((layerRecord.get("layer").name === "__georchestra_print_bounds_") ||
                        (layerRecord.get("layer").name === "__georchestra_results_resultPanel"))) {
                    encodedLayer = this.printProvider.encodeLayer(layerRecord.get("layer"), this.map.getMaxExtent());
                }

                if (encodedLayer) {

                    //TODO Do we force version parameter inclusion?
                    if (layerRecord.get("layer").DEFAULT_PARAMS) {
                        encodedLayer.version = layerRecord.get("layer").DEFAULT_PARAMS.version;
                    }
                    if (encodedLayer.maxScaleDenominator) {
                        delete encodedLayer.maxScaleDenominator;
                    }
                    if (encodedLayer.minScaleDenominator) {
                        delete encodedLayer.minScaleDenominator;
                    }

                    encodedLayers.splice(-1, 0, encodedLayer);
                }
            }
        }, this);

        return encodedLayers;
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