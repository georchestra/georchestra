/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {

    /**
     * Maximum number of features for atlas generation
     */
    maxFeatures: null,

    /**
     * Addons title {String}
     */
    title: null,

    /**
     * Addons description {String}
     */
    qtip: null,

    /**
     * css class for icon {String}
     */
    iconCls: null,

    /**
     * atlas configuration  {Object}
     * this will be send as JSON to atlas server
     */
    atlasConfig: {},

    /**
     * Menu item to show atlas form {Ext.menu.CheckItem}
     * @private
     */
    item: null,

    /**
     * Button to show atlas form {Ext.Button}
     * @private
     */
    components: null,

    /**
     * Form window {Ext.Window}
     * @private
     *
     */
    window: null,

    /**
     * Events Mangement {Ext.util.Observable}
     * @private
     */
    events: null,

    /**
     * Store containing attributes name {Ext.data.Store subclass}
     * It is use in some form comboboxes
     * @private
     */
    attributeStore: null,

    /**
     * Selected feature when addons is used from result panel action {GeoExt.data.FeatureStore}
     * @private
     */
    resultPanelFeatures: null,


    /**
     * Print provider used to retrieve print server configuration {GeoExt.data.MapFishPrintv3Provider}
     * @private
     */
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
        this.maxFeatures = this.options.maxFeatures;
        this.iconCls = this.options.iconCls;

        if (this.target) {
            this.components = this.target.insertButton(this.position, {
                xtype: "button",
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: this.iconCls,
                listeners: {
                    "toggle": this.menuAction,
                    scope: this
                },
                scope: this
            });
            this.target.doLayout();

        } else {
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
        }

        this.events = new Ext.util.Observable();
        this.events.addEvents(
            /**
             * @event featurelayerready
             * Fires when the layer and pages object are ready
             */
            "featurelayerready"
        );

        /**
         * Atlas request is submitted on featurelayerready event
         */
        this.events.on({
            "featurelayerready": function(atlasConfig) {
                var json;
                json = new OpenLayers.Format.JSON();
                OpenLayers.Request.POST({
                    url: "http://localhost:8180/apps/atlas/login",
                    data: json.write(atlasConfig),
                    success: function() {
                        GEOR.helper.msg(this.title, this.tr("atlas_submit_success"))
                    },
                    failure: function() {
                        GEOR.util.errorDialog({
                            msg: this.tr("atlas_submit_fail")
                        })
                    },
                    scope: this
                });
            }
        });

        this.printProvider = new GeoExt.data.MapFishPrintv3Provider({
            method: "POST",
            url: this.options.printServerUrl
        });
        this.printProvider.loadCapabilities();
        //TODO We are waiting for capabilites...
        // TODO Do we use event, modify GeoExt.data.PrintProviderBase.loadCapabilities() or else?
        Ext.util.Functions.defer(function() {
            if (this.printProvider.capabilities === "") {
                GEOR.util.errorDialog({
                    msg: this.tr("atlas_connect_printserver_error")
                });
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
                                                                    return ((c.getXType() === "combo") &&
                                                                    (c.name === "scale_manual"));
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
                                            var radioScale, valid;
                                            radioScale = this.findParentByType("form").findBy(function(c) {
                                                return ((c.getXType() === "radiogroup") &&
                                                (c.name === "scale_method_group"));
                                            })[0];
                                            valid = !(radioScale.getValue().inputValue === "manual" && (value === ""));
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
                                                        name: "titleMethod",
                                                        inputValue: "same",
                                                        checked: true
                                                    },
                                                    {
                                                        boxLabel: this.tr("atlas_fieldtitle"),
                                                        name: "titleMethod",
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
                                                name: "titleText",
                                                fieldLabel: this.tr("atlas_pagetitle"),
                                                labelStyle: "width:160px",
                                                value: this.tr("atlas_title"),
                                                validator: function(value) {
                                                    var radioTitle, valid;
                                                    radioTitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() === "radiogroup") &&
                                                        (c.name === "title_method_group"));
                                                    })[0];
                                                    valid = !((radioTitle.getValue().inputValue === "same") &&
                                                    (value === ""));
                                                    return valid;
                                                }
                                            },
                                            {
                                                xtype: "combo",
                                                name: "titleField",
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
                                                    var radioTitle, valid;
                                                    radioTitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() === "radiogroup") &&
                                                        (c.name === "title_method_group"));
                                                    })[0];
                                                    valid = !((radioTitle.getValue().inputValue === "field") &&
                                                    (value === ""));
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
                                                        name: "subtitleMethod",
                                                        inputValue: "same",
                                                        checked: true
                                                    },
                                                    {
                                                        boxLabel: this.tr("atlas_fieldsubtitle"),
                                                        name: "subtitleMethod",
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
                                                name: "subtitleText",
                                                labelStyle: "width:160px",
                                                fieldLabel: this.tr("atlas_pagesubtitle"),
                                                //tabTip: "This subtitle will be use for every page",
                                                value: this.tr("atlas_subtitle"),
                                                validator: function(value) {
                                                    var radioSubtitle, valid;
                                                    radioSubtitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() === "radiogroup") &&
                                                        (c.name === "title_method_group"));
                                                    })[0];
                                                    valid = !((radioSubtitle.getValue().inputValue === "same") &&
                                                    (value === ""));
                                                    return valid;
                                                }
                                            },
                                            {
                                                xtype: "combo",
                                                name: "subtitleField",
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
                                                    var radioSubtitle, valid;
                                                    radioSubtitle = this.findParentByType("form").findBy(function(c) {
                                                        return ((c.getXType() === "radiogroup") &&
                                                        (c.name === "subtitle_method_group"));
                                                    })[0];
                                                    valid = !((radioSubtitle.getValue().inputValue === "field") &&
                                                    (value === ""));
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
                ]
            }
            ]
        });
    },

    /**
     * Method menuAction
     *
     * Ext component handler used to launch atlas form
     *
     * @param atlasMenu - button or menuitem to which the handler is attached
     */

    menuAction: function(atlasMenu) {
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

        var layerPanel = new Ext.Panel({
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

        this.window.items.itemAt(0).insert(0, layerPanel);
        this.window.on("beforehide", function() {
            if (layerPanel) {
                layerPanel.destroy();
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
     * @param event - event which trigger the action
     * @param resultpanel - resultpanel on which the callback must be operated
     * @param addon - The current addon.
     *
     */
    resultPanelHandler: function(menuitem, event, resultpanel, addon) {
        var layerName, fieldsCombo, attributeStoreData, layerPanel;

        layerName = null;

        addon.resultPanelFeatures = resultpanel._store;


        fieldsCombo = addon.window.findBy(function(c) {
            return ((c.getXType() === "combo") &&
            ((c.name === "titleField") || (c.name === "subtitleField" || (c.name === "prefix_field"))));
        });

        attributeStoreData = [];
        Ext.each(resultpanel._model.getFields(), function(fieldname) {
            attributeStoreData.splice(-1, 0, [fieldname]);
        });

        addon.attributeStore = new Ext.data.ArrayStore({
            fields: ["name"],
            data: attributeStoreData
        });
        Ext.each(fieldsCombo, function(fieldCombo) {
            fieldCombo.bindStore(addon.attributeStore);
            fieldCombo.reset();
        }, addon);


        addon.mapPanel.layers.each(function(l) {
            //FIXME or not... find layerRecord based on title
            if (resultpanel.title === l.get("title")) {
                layerName = l.get("name");
            }
        });


        layerPanel = new Ext.Panel({
            layout: "form",
            border: false,
            style: {
                padding: "5px 5px 0",
                "background-color": "white"
            },
            items: [
                {
                    xtype: "label",
                    //TODO tr
                    text: "Atlas of layer " + resultpanel.title
                },
                {
                    xtype: "hidden",
                    name: "resultPanel",
                    value: true
                },
                {
                    xtype: "hidden",
                    name: "atlasLayer",
                    value: layerName
                }
            ]
        });

        addon.window.items.itemAt(0).insert(0, layerPanel);
        addon.window.on("beforehide", function() {
            if (layerPanel) {
                layerPanel.destroy();
            }

        });

        addon.window.show();
    },

    layerTreeHandler: function(menuitem, event, layerRecord, addon) {
        //TODO Improve panel with layer name
        var layerPanel = new Ext.Panel({
            layout: "form",
            border: false,
            style: {
                padding: "5px 5px 0",
                "background-color": "white"
            },
            items: [
                {
                    xtype: "label",
                    //TODO tr
                    text: "Atlas of layer " + layerRecord.get("title")
                },
                {
                    xtype: "hidden",
                    name: "atlasLayer",
                    value: layerRecord.get("name")
                }
            ]
        });

        addon.window.items.itemAt(0).insert(0, layerPanel);
        addon.window.on("beforehide", function() {
            if (layerPanel) {
                layerPanel.destroy();
            }

        });

        addon.buildFieldsStore(layerRecord);
        addon.window.show();
    },

    parseForm: function(formValues, autoSubmit) {
        var scaleParameters, titleSubtitleParameters;

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
            scaleManual: formValues["scale_manual"],
            scaleMethod: formValues["scale_method_group"].inputValue,
            scalePadding: formValues["scale_padding"]
        };

        titleSubtitleParameters = {
            titleMethod: formValues["title_method_group"].inputValue,
            titleText: formValues["titleText"],
            titleField: formValues["titleField"],
            subtitleMethod: formValues["title_method_group"].inputValue,
            subtitleText: formValues["subtitleText"],
            subtitleField: formValues["subtitleField"]

        };

        this.atlasConfig.baseLayers = this.baseLayers(formValues["atlasLayer"]);

        this.createFeatureLayerAndPagesSpecs(formValues["atlasLayer"], scaleParameters,
            titleSubtitleParameters, formValues["prefix_field"], autoSubmit, formValues["resultPanel"]);

        //Form submit is trigger on "featurelayerready" event

        this.window.hide();

    },

    /**
     * Method createFeatureLayerAndPagesSpecs
     * TODO Check MFP v3 layers/type specification (order is important)
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters, titleSubtitleParameters, fieldPrefix, autoSubmit, resultPanel) {
        var layer, pageIdx, wfsFeatures, wfsFeature, bounds, bbox;

        /**
         *
         * Private function to create page object from a feature.
         *
         * @param wfsFeature
         * @return page object
         * @private
         */
        var _pageFromFeature = function(wfsFeature) {
            var page = {};

            if (titleSubtitleParameters.titleMethod === "same") {
                page.title = titleSubtitleParameters.titleText;
            } else {
                page.title = wfsFeature.attributes[titleSubtitleParameters.titleField];
            }


            if (titleSubtitleParameters.subtitleMethod === "same") {
                page.subtitle = titleSubtitleParameters.subtitleText;
            } else {
                page.subtitle = wfsFeature.attributes[titleSubtitleParameters.subtitleField];
            }

            if (scaleParameters.scaleMethod === "manual") {
                page.center =
                    [wfsFeature.geometry.getCentroid().x, wfsFeature.geometry.getCentroid().y];
                page.scale = scaleParameters.scaleManual;
            } else {
                if (!(wfsFeature.geometry instanceof OpenLayers.Geometry.Point)) {
                    bounds = wfsFeature.geometry.getBounds();
                    //TODO Revisit after form validation (this will not be available for point)
                    bbox = new OpenLayers.Bounds(bounds.left - scaleParameters.scalePadding,
                        bounds.bottom - scaleParameters.scalePadding,
                        bounds.right + scaleParameters.scalePadding,
                        bounds.top + scaleParameters.scalePadding
                    ).toArray();
                } else {
                    bounds = wfsFeature.geometry.getBounds();
                    //TODO - Read from add-on config
                    bbox = bounds.scale(1.1).toArray();
                }
                page.bbox = bbox;
            }

            if (fieldPrefix === "") {
                page.filename = pageIdx.toString() + "_atlas.pdf";
            } else {
                page.filename = wfsFeature.attributes[fieldPrefix] + "_" + pageIdx.toString() +
                    "_atlas.pdf";
            }

            return page;

        };

        this.atlasConfig.pages = [];
        pageIdx = 0;

        this.mapPanel.layers.each(function(layerStore) {
            layer = layerStore.get("layer");

            if (layerStore.get("name") === atlasLayer) {
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

                if (resultPanel) {
                    wfsFeatures = this.resultPanelFeatures;

                    if (wfsFeatures.totalLength >= (this.maxFeatures + 1)) {
                        GEOR.util.errorDialog({
                            //TODO tr
                            msg: "Too many features to build atlas, please launch Atlas generation from the result panel with less than " +
                            (this.maxFeatures + 1) + " features."
                        });
                        return;
                    }
                    //FIXME Duplicate of below function!!!
                    wfsFeatures.each(function(record) {

                        wfsFeature = record.data.feature;


                        this.atlasConfig.pages.splice(-1, 0, _pageFromFeature(wfsFeature));

                        pageIdx = pageIdx + 1;


                    }, this);

                    if (autoSubmit) {
                        this.events.fireEvent("featurelayerready", this.atlasConfig);
                    }
                } else {
                    this.protocol.read({
                        //See GEOR_Querier "search" method
                        maxFeatures: this.maxFeatures + 1,
                        propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
                        callback: function(response) {
                            if (!response.success()) {
                                return;
                            }
                            wfsFeatures = response.features;
                            //TODO Use addon max features
                            if (wfsFeatures.length === (this.maxFeatures + 1)) {
                                GEOR.util.errorDialog({
                                    //TODO tr
                                    msg: "Too many features to build atlas, please launch Atlas generation from the result panel with less than " +
                                    (this.maxFeatures + 1) + " features.",
                                    scope: this
                                });
                            }
                            Ext.each(wfsFeatures, function(wfsFeature) {

                                this.atlasConfig.pages.splice(-1, 0, _pageFromFeature(wfsFeature));

                                pageIdx = pageIdx + 1;

                            }, this);

                            if (autoSubmit) {
                                this.events.fireEvent("featurelayerready", this.atlasConfig);
                            }

                        },
                        scope: this

                    });
                }
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
            typeName: layerRecord.get("WFS_typeName")
        };

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
            return ((c.getXType() === "combo") &&
            ((c.name === "titleField") || (c.name === "subtitleField" || (c.name === "prefix_field"))));
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
            if ((layerRecord.get("name") !== atlasLayer) && layerRecord.get("layer").visibility) {
                encodedLayer = this.printProvider.encodeLayer(layerRecord.get("layer"), this.map.getMaxExtent());

                if (encodedLayer) {

                    //TODO Do we force version parameter inclusion%
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