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
                        value: atlasLayersStore.getAt(0) ? atlasLayersStore.getAt(0).get("name") : null,
                        mode: "local",
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
                        fieldLabel: "Bounding box padding (%)",
                        value: 15
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
                        name: "pageTitle",
                        fieldLabel: "Field for page title",
                        mode: "local",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["name", "type"],
                            //TODO tr
                            data: [
                                ["default name", "default type"],
                            ]
                        },
                        valueField: "name",
                        displayField: "name",
                        scope: this
                    },
                    {
                        xtype: "radiogroup",
                        fieldLabel: "Page subtitle option",
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
                        name: "pageSubtitle",
                        fieldLabel: "Field for page subtitle",
                        mode: "local",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["name", "type"],
                            //TODO tr
                            data: [
                                ["default name", "default type"],
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
                            var formValues, layersRelatedValues,
                                scaleParameters = {};
                            if (b.findParentByType("form").getForm().isValid()) {
                                formValues = b.findParentByType("form").getForm().getFieldValues();

                                //copy some parameter
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

                                this.atlasConfig.baseLayers = [
                                    {
                                        "type": "osm",
                                        "baseURL": "http://otile1.mqcdn.com/tiles/1.0.0/map/",
                                        "imageExtension": "png",
                                        "opacity": 0.3
                                    }
                                ];

                                this.createFeatureLayerAndPagesSpecs(formValues["atlasLayer"], scaleParameters);

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
     * Method createPagesSpecs
     * TODO Check MFP v3 layers/type specification (order is important)
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters) {
        var layer, page, gml, wfsFeature,
            pages = [];
        gml = new OpenLayers.Format.GML();

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

                OpenLayers.Request.GET({
                    url: layerStore.get("WFS_URL"),
                    params: {
                        service: "wfs",
                        version: "1.1.0",
                        request: "GetFeature",
                        typeName: layerStore.get("WFS_typeName"),
                        maxFeatures: 2
                    },
                    success: function(resp) {
                        Ext.each(resp._object.responseXML.getElementsByTagName("FeatureCollection")[0].getElementsByTagName("featureMembers")[0].children, function(gmlFeature) {
                            wfsFeature = gml.parseFeature(gmlFeature);

                            page = {};
                            page.center = [wfsFeature.geometry.getCentroid().x, wfsFeature.geometry.getCentroid().y];
                            //TODO choose title
                            page.title = "Title";
                            //TODO
                            page.subtitle = "Subtitle";
                            //TODO improve scale
                            page.scale = scaleParameters.scale_manual;
                            //TODO
                            page.filename = "filename.pdf"
                            pages.splice(0, 0, page);
                        });

                        this.atlasConfig.pages = pages;
                        this.events.fireEvent("featurelayerready", this.atlasConfig)
                    },
                    failure: function(resp) {
                        //TODO improve msg
                        alert(resp);
                    },
                    scope: this
                });
            }
        }, this);
    },


    /**
     * Method: buildFieldsStore
     * @param layerRecord
     */
    buildFieldsStore: function(layerRecord) {
        //WIP
        var fieldsRecord = Ext.data.Record.create([
            {name: "name", mapping: "@name"},
            {name: "type", mapping: "@type"}
        ]);
        var fieldsReader = new Ext.data.XmlReader({
            //TODO check agains WFS
            record: "complexType complexContent extension sequence element"

        }, fieldsRecord);

        Ext.Ajax.request({
            url: layerRecord.get("WFS_URL"),
            method: "GET",
            params: {
                request: "DescribeFeatureType",
                version: "1.1.0",
                service: "WFS",
                typeName: layerRecord.get("WFS_typeName")
            },
            callback: function(opt, s, resp) {
                var fieldsRecords = fieldsReader.read(resp._object);
                var fieldsStore = new Ext.data.Store({reader: fieldsReader});
                fieldsStore.loadData(resp._object.responseXML);
                var fieldsCombo = this.window.findBy(function(c) {
                    return ((c.getXType() == "combo") &&
                    ((c.name == "pageTitle") || (c.name == "pageSubtitle")))
                });
                Ext.each(fieldsCombo, function(fieldCombo) {
                    fieldCombo.store = fieldsStore;
                });
            },
            scope: this
        });

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