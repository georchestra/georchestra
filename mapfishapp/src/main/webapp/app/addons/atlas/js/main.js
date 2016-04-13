/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {
    window: null,
    layer: null,
    title: null,
    atlasConfig: {},


    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        // create a menu item for the "tools" menu:
        this.item = new Ext.menu.CheckItem({
            text: this.getText(record),
            qtip: this.getQtip(record),
            iconCls: "atlas-icon",
            checked: false,
            listeners: {
                "click": {
                    fn: this.menuAction,
                    scope: this
                }
            }
        });
        this.title = this.getText(record);

        this.events = new Ext.util.Observable();
        this.events.addEvents(
            /**
             * Event: featurelayerready"
             * Fires the layer and pages object are ready
             */
            "featurelayerready"
        );
        this.events.on({
            "featurelayerready": function(atlasConfig) {
                var json;
                json = new OpenLayers.Format.JSON();
                OpenLayers.Request.POST({
                    url: "http://localhost:8180/apps/atlas/login",
                    data: json.write(atlasConfig)
                });
            }
        });
    }
    ,

    /**
     * Method menuAction
     */

    menuAction: function(atlas, e) {
        var atlasLayersStore = new GeoExt.data.LayerStore({
            fields: this.mapPanel.layers.fields.items
        });
        this.mapPanel.layers.each(function(layerRecord) {
            if (layerRecord.get("WFS_typeName") || layerRecord.get("WFS_URL")) {
                atlasLayersStore.add(layerRecord);
            }
        });

        this.window = new Ext.Window({
            title: this.title,
            width: 420,
            height: 540,
            closable: true,
            items: [{
                xtype: "form",
                items: [
                    {
                        xtype: "combo",
                        name: "atlasLayer",
                        fieldLabel: this.tr("atlas_atlaslayer"),
                        emptyText: "Select layer...",
                        mode: "local",
                        editable: false,
                        typeAhead: false,
                        triggerAction: "all",
                        store: atlasLayersStore,
                        valueField: "name",
                        displayField: "title",
                        allowBlank: false,
                        listeners: {
                            select: {
                                fn: function(combo, record) {
                                    this.buildFieldsStore(record);
                                },
                                scope: this
                            },
                            scope: this
                        },
                        scope: this
                    },
                    {
                        xtype: "combo",
                        name: "outputFormat",
                        //TODO tr
                        fieldLabel: "Format",
                        value: "pdf",
                        editable: false,
                        typeAhed: false,
                        mode: "local",
                        triggerAction: "all",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["formatId", "formatDescription"],
                            data: [
                                ["pdf", "PDF"],
                                ["zip", "zip"]
                            ]
                        },
                        valueField: "formatId",
                        displayField: "formatDescription",
                        allowBlank: false
                    },
                    {
                        xtype: "combo",
                        name: "layout",
                        //TODO tr
                        fieldLabel: "Layout",
                        editable: false,
                        typeAhead: false,
                        value: "A4 portrait",
                        mode: "local",
                        triggerAction: "all",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["layoutId", "layoutDescription"],
                            //TODO tr
                            data: [
                                ["A4 portrait", "A4 portrait"],
                                ["A4 landscape", "A4 landscape"],
                                ["A3 portrait", "A3 portrait"],
                                ["A3 landscape", "A3 landscape"]
                            ]
                        },
                        valueField: "layoutId",
                        displayField: "layoutDescription"
                    },
                    {
                        xtype: "radiogroup",
                        fieldLabel: "Scale determination",
                        name: "scale_method_group",
                        items: [
                            {
                                xtype: "radio",
                                boxLabel: "Bounding box",
                                name: "scale_method",
                                inputValue: "bbox"
                            },
                            {
                                xtype: "radio",
                                boxLabel: "Manual scale",
                                name: "scale_method",
                                inputValue: "manual",
                                checked: true
                            }
                        ]
                    },
                    {
                        //TODO tr
                        xtype: "textfield",
                        name: "scale_manual",
                        fieldLabel: "Scale",
                        value: this.map.getScale()
                    },
                    {
                        //TODO tr
                        xtype: "textfield",
                        name: "scale_padding",
                        fieldLabel: "Bounding box padding (m)",
                        value: 10000
                    },
                    {
                        xtype: "hidden",
                        name: "dpi",
                        //TODO improve management of dpi
                        value: "216"
                    },
                    {
                        //TODO tr
                        xtype: "textfield",
                        name: "email",
                        fieldLabel: "Email"
                    },
                    {
                        //TODO tr
                        xtype: "checkbox",
                        name: "displayLegend",
                        fieldLabel: "Display legend"
                    },
                    {
                        xtype: "radiogroup",
                        fieldLabel: "Page title option",
                        name: "title_method_group",
                        items: [
                            {
                                boxLabel: "Same title for every page",
                                name: "title_method",
                                inputValue: "same",
                                checked: true
                            },
                            {
                                boxLabel: "Use a field from the atlas layer as title",
                                name: "title_method",
                                inputValue: "field"
                            }
                        ]
                    },
                    {
                        //TODO tr
                        xtype: "textfield",
                        name: "title_text",
                        fieldLabel: "Page title",
                        //tabTip: "This title will be use for every page",
                        value: "Title"
                    },
                    {
                        //TODO tr
                        xtype: "combo",
                        name: "title_field",
                        fieldLabel: "Field for page title",
                        emptyText: "Select title field",
                        editable: false,
                        typeAhead: false,
                        mode: "local",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["name"],
                            //TODO tr
                            data: [
                                ["Select atlas layer first"]
                            ]
                        },
                        valueField: "name",
                        displayField: "name",
                        scope: this
                    },
                    {
                        xtype: "radiogroup",
                        fieldLabel: "Page subtitle option",
                        name: "subtitle_method_group",
                        items: [
                            {
                                boxLabel: "Same subtitle for every page",
                                name: "subtitle_method",
                                inputValue: "same",
                                checked: true
                            },
                            {
                                boxLabel: "Use a field from the atlas layer as subtitle",
                                name: "subtitle_method",
                                inputValue: "field"
                            }
                        ]
                    },
                    {
                        //TODO tr
                        xtype: "textfield",
                        name: "subtitle_text",
                        fieldLabel: "Page subtitle",
                        //tabTip: "This subtitle will be use for every page",
                        value: "Subtitle"
                    },
                    {
                        //TODO tr
                        xtype: "combo",
                        name: "subtitle_field",
                        fieldLabel: "Field for page subtitle",
                        emptyText: "Select subtitle field",
                        mode: "local",
                        editable: false,
                        typeAhead: false,
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["name"],
                            //TODO tr
                            data: [
                                ["Select atlas layer first"]
                            ]
                        },
                        valueField: "name",
                        displayField: "name",
                        scope: this
                    },
                    {
                        //TODO tr
                        xtype: "combo",
                        name: "prefix_field",
                        fieldLabel: "Field for filename prefix",
                        emptyText: "Select prefix field",
                        mode: "local",
                        editable: false,
                        typeAhead: false,
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["name"],
                            //TODO tr
                            data: [
                                ["Select atlas layer first"]
                            ]
                        },
                        valueField: "name",
                        displayField: "name",
                        scope: this
                    }


                ],
                buttons: [
                    {
                        //TODO tr
                        text: "Submit",
                        handler: function(b) {
                            var formValues, layersRelatedValues, scaleParameters, titleSubtitleParameters;
                            if (b.findParentByType("form").getForm().isValid()) {
                                formValues = b.findParentByType("form").getForm().getFieldValues();

                                //copy some parameters
                                this.atlasConfig.outputFormat = formValues.outputFormat;
                                this.atlasConfig.layout = formValues.layout;
                                this.atlasConfig.dpi = formValues.dpi;
                                this.atlasConfig.projection = this.map.getProjection();
                                this.atlasConfig.email = formValues.email;
                                this.atlasConfig.displayLegend = formValues.displayLegend;

                                scaleParameters = {
                                    scale_manual: formValues["scale_manual"],
                                    scale_method: formValues["scale_method_group"].inputValue,
                                    scale_padding: formValues["scale_padding"]
                                };

                                titleSubtitleParameters = {
                                    title_method: formValues["title_method_group"].inputValue,
                                    title_text: formValues["title_text"],
                                    title_field: formValues["title_field"],
                                    subtitle_method: formValues["title_method_group"].inputValue,
                                    subtitle_text: formValues["subtitle_text"],
                                    subtitle_field: formValues["subtitle_field"],

                                }

                                this.atlasConfig.baseLayers = [
                                    {
                                        "type": "osm",
                                        "baseURL": "http://otile1.mqcdn.com/tiles/1.0.0/map/",
                                        "imageExtension": "png",
                                        "opacity": 0.3
                                    }
                                ];


                                this.createFeatureLayerAndPagesSpecs(formValues["atlasLayer"], scaleParameters,
                                    titleSubtitleParameters, formValues["prefix_field"]);

                                //Form submit is trigger on "featurelayerready event


                            }
                        },
                        scope: this
                    },
                    {
                        //TODO tr
                        text: "Cancel",
                        handler: function() {
                            this.window.close();
                        },
                        scope: this
                    }
                ],
                scope: this
            }
            ],
            scope: this
        })
        ;
        this.window.show();
    },

    /**
     * Method createFeatureLayerAndPagesSpecs
     * TODO Check MFP v3 layers/type specification (order is important)
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters, titleSubtitleParameters, field_prefix) {
        var layer, page, page_idx, wfsFeatures, page_title, page_subtitle, page_filename, bounds, bbox;

        this.atlasConfig.pages = [];
        page_idx = 0;

        this.mapPanel.layers.each(function(layerStore) {
            layer = layerStore.get("layer");

            if (layerStore.get("name") == atlasLayer) {
                this.atlasConfig.featureLayer = {};
                this.atlasConfig.featureLayer.type = layerStore.get("type");
                this.atlasConfig.featureLayer.baseURL = layer.url;
                this.atlasConfig.featureLayer.layers = [layer.params.LAYERS];
                this.atlasConfig.featureLayer.version = layer.params.VERSION;
                if (layer.params.TRANSPARENT) {
                    this.atlasConfig.featureLayer.customParams = {
                        transparent: true
                    };
                }

                this.protocol.read({
                    //See GEOR_Querier "search" method
                    //TODO set maxFeatures
                    //maxFeatures: GEOR.config.MAX_FEATURES,
                    maxFeatures: 2,
                    propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
                    callback: function(response) {
                        if (!response.success()) {
                            return;
                        }
                        wfsFeatures = response.features;
                        Ext.each(wfsFeatures, function(wfsFeature) {

                            page = {};
                            //we add the page at the beginning to reference it on asynchronous buffer
                            this.atlasConfig.pages.splice(page_idx, 0, page);

                            if (titleSubtitleParameters.title_method == "same") {
                                page_title = titleSubtitleParameters.title_text;
                            } else {
                                page_title = wfsFeature.attributes[titleSubtitleParameters.title_field];
                            }
                            this.atlasConfig.pages[page_idx].title = page_title;


                            if (titleSubtitleParameters.subtitle_method == "same") {
                                page_subtitle = titleSubtitleParameters.subtitle_text;
                            } else {
                                page_subtitle = wfsFeature.attributes[titleSubtitleParameters.subtitle_field];
                            }
                            this.atlasConfig.pages[page_idx].subtitle = page_subtitle;

                            if (scaleParameters.scale_method == "manual") {
                                this.atlasConfig.pages[page_idx].center =
                                    [wfsFeature.geometry.getCentroid().x, wfsFeature.geometry.getCentroid().y];
                                this.atlasConfig.pages[page_idx].scale = scaleParameters.scale_manual;
                            } else {
                                var bWkt, json, wkt, bbox, bufferData, cur_idx;
                                json = new OpenLayers.Format.JSON();
                                wkt = new OpenLayers.Format.WKT();
                                if (!(wfsFeature.geometry instanceof OpenLayers.Geometry.Point)) {
                                    bounds = wfsFeature.geometry.getBounds();
                                    //TODO Revisit after form validation (this will not be available for point)
                                    bbox = new OpenLayers.Bounds(bounds.left - scaleParameters.scale_padding,
                                        bounds.bottom - scaleParameters.scale_padding,
                                        bounds.right + scaleParameters.scale_padding,
                                        bounds.top + scaleParameters.scale_padding
                                    ).toArray();
                                } else {
                                    bounds = wfsFeature.geometry.getBounds();
                                    //TODO - Read from add-on config
                                    bbox = bounds.scale(1.1).toArray();
                                }
                                this.atlasConfig.pages[page_idx].bbox = bbox;
                            }

                            if (field_prefix === "") {
                                page_filename = page_idx.toString() + "_atlas.pdf"
                            } else {
                                page_filename = wfsFeature.attributes[field_prefix] + "_" + page_idx.toString() +
                                    "_atlas.pdf";
                            }
                            this.atlasConfig.pages[page_idx].filename = page_filename;

                            page_idx = page_idx + 1;


                        }, this);

                            this.events.fireEvent("featurelayerready", this.atlasConfig);

                    },
                    scope: this

                })
            }
        }, this);
    },


    /**
     * Method: buildFieldsStore
     * @param layerRecord
     */
    buildFieldsStore: function(layerRecord) {

        GEOR.waiter.show();
        //Code from GEOR_querier
        var pseudoRecord = {
            owsURL: layerRecord.get("WFS_URL"),
            typeName: layerRecord.get("WFS_typeName"),
        }

        this.attributeStore = GEOR.ows.WFSDescribeFeatureType(pseudoRecord, {
            extractFeatureNS: true,
            success: function() {
                // we get the geometry column name, and remove the corresponding record from store
                var idx = this.attributeStore.find("type", GEOR.ows.matchGeomProperty);
                if (idx > -1) {
                    // we have a geometry
                    var r = this.attributeStore.getAt(idx),
                        geometryName = r.get("name");
                    // create the protocol:
                    this.protocol = GEOR.ows.WFSProtocol(pseudoRecord, this.map, {
                        geometryName: geometryName
                    });
                    this._geometryName = geometryName;
                    // remove geometry from attribute store:
                    this.attributeStore.remove(r);
                } else {
                    GEOR.util.infoDialog({
                        msg: this.tr("querier.layer.no.geom")
                    });
                }
            },
            failure: function() {
                GEOR.util.errorDialog({
                    msg: this.tr("querier.layer.error")
                });
            },
            scope: this
        });
        //End of code from GEOR_querier

        var fieldsCombo = this.window.findBy(function(c) {
            return ((c.getXType() == "combo") &&
            ((c.name == "title_field") || (c.name == "subtitle_field" || (c.name == "prefix_field"))));
        });
        Ext.each(fieldsCombo, function(fieldCombo) {
            fieldCombo.reset();
            fieldCombo.bindStore(this.attributeStore);
        }, this);
    },

    /**
     * Method: tr
     *
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    }
    ,

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
})
;