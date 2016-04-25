/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {
    window: null,
    layer: null,
    title: null,
    atlasConfig: {},
    events: null,
    attributeStore: null,
    dpiStore: null,

    printProvider: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.title = this.getText(record);
        this.qtip = this.getQtip(record);
        this.tooltip = this.getTooltip(record);
        this.iconCls = "atlas-icon";

        if (this.target) {
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: 'atlas-icon',
                listeners: {
                    "toggle": this.menuAction,
                    scope: this
                },
                scope: this
            });
            this.target.doLayout();

        }
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

        this.printProvider = new GeoExt.data.MapFishPrintv3Provider({
            method: "POST",
            //TODO read print url from config
            //TODO check customParams
            url: "http://localhost:8181/print/atlas/",
            customParams: {
                title: "Printing Demo",
                comments: "This is a map printed from GeoExt.",
                datasource: []
            }
        });
        this.printProvider.loadCapabilities();
        //TODO We are waiting for capabilites...
        // TODO Do we use event, modify GeoExt.data.PrintProviderBase.loadCapabilities() or else?
        Ext.util.Functions.defer(function() {
            if (this.printProvider.capabilities === null) {
                GEOR.util.errorDialog({
                    msg: this.tr("atlas_connect_printserver_error")
                })
            }
        }, 1000, this);

        this.attributeStore = new Ext.data.ArrayStore({
            fields: ["name"],
            data: [
                [this.tr("atlas_selectlayerfirst")]
            ]
        });

        this.window = new Ext.Window({
            title: this.title,
            width: 680,
            autoHeight: true,
            bodyStyle: {
                padding: "5px 5px 0",
                "background-color": "white"
            },
            border: false,
            closable: true,
            closeAction: "hide",
            items: [{
                xtype: "form",
                items: [
                    {
                        xtype: "fieldset",
                        autoheight: true,
                        title: this.tr("atlas_layout"),
                        style: {
                            margin: "0 5px 10px",
                            "background-color": "white"
                        },
                        items: [
                            {
                                layout: "column",
                                border: false,
                                items: [
                                    {
                                        columnWidth: 0.5,
                                        border: false,
                                        layout: "form",
                                        items: [
                                            {
                                                xtype: "combo",
                                                name: "layout",
                                                allowBlank: false,
                                                fieldLabel: this.tr("atlas_layout"),
                                                editable: false,
                                                typeAhead: false,
                                                emptyText: this.tr("atlas_selectlayout"),
                                                mode: "local",
                                                triggerAction: "all",
                                                store: this.printProvider.layouts,
                                                valueField: "name",
                                                displayField: "name"
                                            },
                                            {
                                                xtype: "checkbox",
                                                name: "displayLegend",
                                                labelStyle: "width:120px",
                                                fieldLabel: this.tr("atlas_displaylegend")
                                            }
                                        ]
                                    },
                                    {
                                        columnWidth: 0.5,
                                        layout: "form",
                                        border: false,
                                        items: [
                                            {
                                                xtype: "combo",
                                                name: "outputFormat",
                                                fieldLabel: this.tr("atlas_format"),
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
                                                name: "dpi",
                                                fieldLabel: "Map dpi",
                                                emptyText: "Select print resolution",
                                                editable: false,
                                                typeAhead: false,
                                                autoComplete: false,
                                                mode: "local",
                                                store: this.printProvider.dpis,
                                                displayField: "name",
                                                valueField: "value",
                                                triggerAction: "all",
                                                allowBlank: false
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: "fieldset",
                        title: this.tr("atlas_scale"),
                        autoheight: true,
                        style: {
                            margin: "0 5px 10px",
                            "background-color": "white"
                        },
                        items: [
                            {
                                layout: "column",
                                border: false,
                                items: [{
                                    columnWidth: 0.4,
                                    layout: "form",
                                    border: false,
                                    items: [
                                        {
                                            xtype: "radiogroup",
                                            columns: 1,
                                            hideLabel: true,
                                            name: "scale_method_group",
                                            items: [
                                                {
                                                    xtype: "radio",
                                                    boxLabel: this.tr("atlas_scalemanual"),
                                                    name: "scale_method",
                                                    inputValue: "manual",
                                                    checked: true,
                                                    listeners: {
                                                        "check": {
                                                            fn: function(cb, checked) {
                                                                var form, combos;
                                                                form = cb.findParentByType("form");
                                                                combos = form.findBy(function(c) {
                                                                    return ((c.getXType() == "combo") &&
                                                                    (c.name == "scale_manual"));
                                                                });
                                                                if (checked) {
                                                                    combos[0].enable();
                                                                } else {
                                                                    combos[0].disable();
                                                                }

                                                            }
                                                        }
                                                    }
                                                }, {
                                                    xtype: "radio",
                                                    boxLabel: this.tr("atlas_bbox"),
                                                    name: "scale_method",
                                                    inputValue: "bbox"
                                                }
                                            ]
                                        }
                                    ]
                                }, {
                                    layout: "form",
                                    columnWidth: 0.6,
                                    border: false,
                                    items: [{
                                        xtype: "combo",
                                        name: "scale_manual",
                                        fieldLabel: this.tr("atlas_scale"),
                                        emptyText: this.tr("atlas_selectscale"),
                                        mode: "local",
                                        triggerAction: "all",
                                        store: new GeoExt.data.ScaleStore({map: this.mapPanel}),
                                        valueField: "scale",
                                        displayField: "scale",
                                        editable: false,
                                        typeAhead: false,
                                        validator: function(value) {
                                            var radio_scale, valid;
                                            radio_scale = this.findParentByType("form").findBy(function(c) {
                                                return ((c.getXType() == "radiogroup") &&
                                                (c.name == "scale_method_group"));
                                            })[0];
                                            if (radio_scale.getValue().inputValue == "manual") {
                                                if (value === "") {
                                                    valid = false;
                                                } else {
                                                    valid = true;
                                                }
                                            } else {
                                                valid = true;
                                            }
                                            return valid;
                                        }

                                    },
                                        {
                                            //TODO replace by add-on config
                                            xtype: "hidden",
                                            name: "scale_padding",
                                            value: 10000
                                        }]
                                }]
                            }

                        ]
                    },
                    {
                        xtype: "fieldset",
                        autoheight: true,
                        title: this.tr("atlas_pagetitle"),
                        style: {
                            margin: "0 5px 10px",
                            "background-color": "white"
                        },
                        items: [
                            {
                                layout: "column",
                                border: false,
                                items: [
                                    {
                                        columnWidth: 0.4,
                                        layout: "form",
                                        border: false,
                                        items: [
                                            {
                                                xtype: "radiogroup",
                                                columns: 1,
                                                hideLabel: true,
                                                name: "title_method_group",
                                                items: [
                                                    {
                                                        boxLabel: this.tr("atlas_sametitle"),
                                                        name: "title_method",
                                                        inputValue: "same",
                                                        checked: true
                                                    },
                                                    {
                                                        boxLabel: this.tr("atlas_fieldtitle"),
                                                        name: "title_method",
                                                        inputValue: "field"
                                                    }
                                                ]
                                            }
                                        ]

                                    },
                                    {
                                        columnWidth: 0.6,
                                        layout: "form",
                                        border: false,
                                        items: [
                                            {
                                                xtype: "textfield",
                                                name: "title_text",
                                                fieldLabel: this.tr("atlas_pagetitle"),
                                                labelStyle: "width:160px",
                                                value: this.tr("atlas_title"),
                                                validator: function(value) {
                                                    var radio_title, valid;
                                                    radio_title = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() == "radiogroup") &&
                                                        (c.name == "title_method_group"));
                                                    })[0];
                                                    if (radio_title.getValue().inputValue == "same") {
                                                        if (value === "") {
                                                            valid = false;
                                                        } else {
                                                            valid = true;
                                                        }
                                                    } else {
                                                        valid = true;
                                                    }
                                                    return valid;
                                                }
                                            },
                                            {
                                                xtype: "combo",
                                                name: "title_field",
                                                labelStyle: "width:160px",
                                                fieldLabel: this.tr("atlas_fieldfortitle"),
                                                emptyText: this.tr("atlas_fieldfortitleselect"),
                                                editable: false,
                                                typeAhead: false,
                                                mode: "local",
                                                store: this.attributeStore,
                                                valueField: "name",
                                                displayField: "name",
                                                triggerAction: "all",
                                                scope: this,
                                                validator: function(value) {
                                                    var radio_title, valid;
                                                    radio_title = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() == "radiogroup") &&
                                                        (c.name == "title_method_group"));
                                                    })[0];
                                                    if (radio_title.getValue().inputValue == "field") {
                                                        if (value === "") {
                                                            valid = false;
                                                        } else {
                                                            valid = true;
                                                        }
                                                    } else {
                                                        valid = true;
                                                    }
                                                    return valid;
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: "fieldset",
                        autoheight: true,
                        title: this.tr("atlas_pagesubtitle"),
                        style: {
                            margin: "0 5px 10px",
                            "background-color": "white"
                        },
                        items: [
                            {
                                layout: "column",
                                border: false,
                                items: [
                                    {
                                        columnWidth: 0.4,
                                        layout: "form",
                                        border: false,
                                        items: [
                                            {
                                                xtype: "radiogroup",
                                                hideLabel: true,
                                                columns: 1,
                                                name: "subtitle_method_group",
                                                items: [
                                                    {
                                                        boxLabel: this.tr("atlas_samesubtitle"),
                                                        name: "subtitle_method",
                                                        inputValue: "same",
                                                        checked: true
                                                    },
                                                    {
                                                        boxLabel: this.tr("atlas_fieldsubtitle"),
                                                        name: "subtitle_method",
                                                        inputValue: "field"
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        columnWidth: 0.6,
                                        layout: "form",
                                        border: false,
                                        items: [
                                            {
                                                xtype: "textfield",
                                                name: "subtitle_text",
                                                labelStyle: "width:160px",
                                                fieldLabel: this.tr("atlas_pagesubtitle"),
                                                //tabTip: "This subtitle will be use for every page",
                                                value: this.tr("atlas_subtitle"),
                                                validator: function(value) {
                                                    var radio_subtitle, valid;
                                                    radio_subtitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() == "radiogroup") &&
                                                        (c.name == "title_method_group"));
                                                    })[0];
                                                    if (radio_subtitle.getValue().inputValue == "same") {
                                                        if (value === "") {
                                                            valid = false;
                                                        } else {
                                                            valid = true;
                                                        }
                                                    } else {
                                                        valid = true;
                                                    }
                                                    return valid;
                                                }
                                            },
                                            {
                                                xtype: "combo",
                                                name: "subtitle_field",
                                                labelStyle: "width:160px",
                                                fieldLabel: this.tr("atlas_fieldforsubtitle"),
                                                emptyText: this.tr("atlas_fieldforsubtitleselect"),
                                                mode: "local",
                                                editable: false,
                                                typeAhead: false,
                                                store: this.attributeStore,
                                                valueField: "name",
                                                displayField: "name",
                                                triggerAction: "all",
                                                scope: this,
                                                validator: function(value) {
                                                    var radio_subtitle, valid;
                                                    radio_subtitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() == "radiogroup") &&
                                                        (c.name == "subtitle_method_group"));
                                                    })[0];
                                                    if (radio_subtitle.getValue().inputValue == "field") {
                                                        if (value === "") {
                                                            valid = false;
                                                        } else {
                                                            valid = true;
                                                        }
                                                    } else {
                                                        valid = true;
                                                    }
                                                    return valid;
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        layout: "column",
                        border: false,
                        style: {
                            margin: "0 5px 10px",
                            "background-color": "white"
                        },
                        items: [
                            {
                                layout: "form",
                                border: false,
                                columnWidth: 0.4,
                                items: [
                                    {
                                        xtype: "textfield",
                                        labelStyle: "width:110px",
                                        width: 120,
                                        name: "outputFilename",
                                        fieldLabel: this.tr("atlas_outputfilename"),
                                        value: this.tr("atlas_ouputfilenamedefault"),
                                        allowBlank: false
                                    }
                                ]
                            },
                            {
                                layout: "form",
                                border: false,
                                columnWidth: 0.6,
                                items: [
                                    {
                                        xtype: "combo",
                                        name: "prefix_field",
                                        labelStyle: "width:160px",
                                        fieldLabel: this.tr("atlas_fieldprefix"),
                                        emptyText: this.tr("atlas_fieldforprefix"),
                                        mode: "local",
                                        editable: false,
                                        typeAhead: false,
                                        store: this.attributeStore,
                                        valueField: "name",
                                        displayField: "name",
                                        triggerAction: "all",
                                        scope: this
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        layout: "form",
                        border: false,
                        style: {
                            padding: "5px 5px 0",
                            "background-color": "white"
                        },
                        items: [
                            {
                                xtype: "textfield",
                                style: {
                                    margin: "0 5px 10px",
                                    "background-color": "white"
                                },
                                name: "email",
                                labelStyle: "width:420px",
                                fieldLabel: this.tr("atlas_emaillabel"),
                                allowBlank: false,
                                vtype: "email"
                            }
                        ]
                    }
                ],
                buttons: [
                    {
                        text: this.tr("atlas_submit"),
                        handler: function(b) {
                            var formValues;
                            if (b.findParentByType("form").getForm().isValid()) {
                                formValues = b.findParentByType("form").getForm().getFieldValues();
                                this.parseForm(formValues);
                            }
                        },
                        scope: this
                    },
                    {
                        text: this.tr("atlas_cancel"),
                        handler: function() {
                            this.window.hide();
                        },
                        scope: this
                    }
                ],
                scope: this
            }
            ],
            scope: this
        });
    },

    /**
     * Method menuAction
     */

    menuAction: function(atlasMenu, e) {
        if (atlasMenu.getXType() === "button") {
            if (!atlasMenu.pressed) {
                return;
            }
        }
        var atlasLayersStore = new GeoExt.data.LayerStore({
            fields: this.mapPanel.layers.fields.items
        });
        this.mapPanel.layers.each(function(layerRecord) {
            if (layerRecord.get("WFS_typeName") || layerRecord.get("WFS_URL")) {
                atlasLayersStore.add(layerRecord);
            }
        });

        var layerSelectPanel = new Ext.Panel({
            layout: "form",
            border: false,
            style: {
                padding: "5px 5px 0",
                "background-color": "white"
            },
            items: [
                {
                    xtype: "combo",
                    name: "atlasLayer",
                    labelStyle: "width:180px",
                    fieldLabel: this.tr("atlas_atlaslayer"),
                    emptyText: this.tr("atlas_emptylayer"),
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
                }
            ]
        });

        this.window.items.itemAt(0).insert(0, layerSelectPanel);
        this.window.on("beforehide", function() {
            if (layerSelectPanel) {
                layerSelectPanel.destroy();
            }

        });


        this.window.show();
        if (atlasMenu.getXType() === "button") {
            atlasMenu.toggle(false);
        }
    },

    /**
     *
     * @param menuitem - menuitem which will receive the callback
     * @param resultpanel - resultpanel on which the callback must be operated
     * @param addon - The current addon.
     *
     */
    resultPanelHandler: function(menuitem, event, resultpanel, addon) {


        var fieldsCombo = this.window.findBy(function(c) {
            return ((c.getXType() == "combo") &&
            ((c.name == "title_field") || (c.name == "subtitle_field" || (c.name == "prefix_field"))));
        });
        Ext.each(fieldsCombo, function(fieldCombo) {
            fieldCombo.bindStore(this.attributeStore);
            fieldCombo.reset();
        }, this);
        addon.window.show();
    },

    layerTreeHandler: function(menuitem, event, layerRecord, addon) {
        //TODO Add panel with layer name
        addon.buildFieldsStore(layerRecord);
        addon.window.show();
    },

    parseForm: function(formValues, autoSubmit) {
        var layersRelatedValues, scaleParameters, titleSubtitleParameters;

        autoSubmit = autoSubmit || true;
        //copy some parameters
        this.atlasConfig.outputFormat = formValues.outputFormat;
        this.atlasConfig.layout = formValues.layout;
        this.atlasConfig.dpi = formValues.dpi;
        this.atlasConfig.projection = this.map.getProjection();
        this.atlasConfig.email = formValues.email;
        this.atlasConfig.displayLegend = formValues.displayLegend;
        this.atlasConfig.outputFilename = formValues.outputFilename;

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
            subtitle_field: formValues["subtitle_field"]

        }

        this.atlasConfig.baseLayers = this.baseLayers(formValues["atlasLayer"]);


        this.createFeatureLayerAndPagesSpecs(formValues["atlasLayer"], scaleParameters,
            titleSubtitleParameters, formValues["prefix_field"], autoSubmit);

        //Form submit is trigger on "featurelayerready event

    },

    /**
     * Method createFeatureLayerAndPagesSpecs
     * TODO Check MFP v3 layers/type specification (order is important)
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters, titleSubtitleParameters, field_prefix, autoSubmit) {
        var layer, page, page_idx, wfsFeatures, page_title, page_subtitle, page_filename, bounds, bbox;

        this.atlasConfig.pages = [];
        page_idx = 0;

        this.mapPanel.layers.each(function(layerStore) {
            layer = layerStore.get("layer");

            if (layerStore.get("name") == atlasLayer) {
                this.atlasConfig.featureLayer = this.printProvider.encodeLayer(layerStore.get("layer"),
                    layerStore.get("layer").getExtent());
                //TODO version may not be required by mapfish - check serverside
                if (layerStore.get("layer").DEFAULT_PARAMS) {
                    this.atlasConfig.featureLayer.version = layerStore.get("layer").DEFAULT_PARAMS.version;
                }
                if (this.atlasConfig.featureLayer.maxScaleDenominator) {
                    delete this.atlasConfig.featureLayer.maxScaleDenominator;
                }
                if (this.atlasConfig.featureLayer.minScaleDenominator) {
                    delete this.atlasConfig.featureLayer.minScaleDenominator;
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

                        if (autoSubmit) {
                            this.events.fireEvent("featurelayerready", this.atlasConfig);
                        }

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

        this.attributeStore = null;
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
                    // FIXME : disabled because it causes problem with combobox (index offset)
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
            fieldCombo.bindStore(this.attributeStore);
            fieldCombo.reset();
        }, this);
    },

    baseLayers: function(atlasLayer) {

        var encodedLayer = null,
            encodedLayers = [];
        this.mapPanel.layers.each(function(layerRecord) {
            if ((layerRecord.get("name") != atlasLayer) && layerRecord.get("layer").visibility) {
                encodedLayer = this.printProvider.encodeLayer(layerRecord.get("layer"), this.map.getMaxExtent())
                encodedLayers.splice(-1, 0, encodedLayer);
            }
        }, this);

        return encodedLayers;
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