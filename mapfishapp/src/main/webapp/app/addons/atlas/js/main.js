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
     * this will be sent as JSON to the atlas server
     */
    spec: {},

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

    layerRecord: null,

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
                tooltip: this.getTooltip(record),
                iconCls: this.iconCls,
                listeners: {
                    "click": this.menuAction,
                    scope: this
                },
                scope: this
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

        // FIXME: why do we need an event if the communication is inside this addon ?
        // why not, but it seems a bit overkill...
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
            "featurelayerready": function(spec) {
                var format = new OpenLayers.Format.JSON();
                OpenLayers.Request.POST({
                    url: this.options.atlasServerUrl,
                    data: format.write(spec),
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
            },
            scope: this
        });

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
                    } else {
                        this.window = this._buildWindow();
                    }
                },
                scope: this
            }
        });

        this.attributeStore = new Ext.data.ArrayStore({
            fields: ["name"],
            data: []
        });
    },


    /**
     * Method: buildWindow
     *
     */
    _buildWindow: function() {
        return new Ext.Window({
            title: this.title,
            minWidth: 550,
            width: 700,
            autoHeight: true,
            constrainHeader: true,
            bodyStyle: {
                padding: "10px 10px",
                "background-color": "white"
            },
            border: false,
            closable: true,
            closeAction: "hide",
            listeners: {
                "show": function() {
                    this.window.setTitle([
                        this.tr("Atlas of layer"),
                        ' \"',
                        this.layerRecord.get("title"),
                        '\"'
                    ].join(''));
                    this.buildFieldsStore(this.layerRecord);
                },
                scope: this
            },
            items: [{
                xtype: "form",
                border: false,
                items: [{ // FILE NAME
                    layout: "form",
                    labelSeparator: this.tr("labelSeparator"),
                    border: false,
                    items: [{
                        xtype: "textfield",
                        anchor: '-30px',
                        name: "outputFilename",
                        fieldLabel: this.tr("atlas_outputfilename"),
                        value: new Date().toISOString().slice(0,19).replace(/T|:|-/g, '')+"_atlas",
                        allowBlank: false
                    }]
                }, { // FORMAT (PDF or ZIP)
                    xtype: "fieldset",
                    autoheight: true,
                    title: this.tr("atlas_format"),
                    items: [{
                        layout: "column",
                        border: false,
                        items: [{
                            columnWidth: 0.5,
                            border: false,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            items: [{
                                xtype: "combo",
                                name: "outputFormat",
                                fieldLabel: this.tr("atlas_format"),
                                editable: false,
                                typeAhed: false,
                                anchor: '-10px',
                                mode: "local",
                                triggerAction: "all",
                                store: {
                                    xtype: "arraystore",
                                    id: 0,
                                    fields: ["formatId", "formatDescription"],
                                    data: [
                                        ["pdf", "PDF"],
                                        ["zip", "ZIP"]
                                    ]
                                },
                                valueField: "formatId",
                                displayField: "formatDescription",
                                allowBlank: false,
                                listeners: {
                                    "select": function(cb, r) {
                                        var form = cb.findParentByType("form"),
                                            combos = form.findBy(function(c) {
                                                return ((c.getXType() === "combo") &&
                                                    (c.name === "prefix_field"));
                                            });
                                        if (r.get('formatId') === "zip") {
                                            combos[0].show();
                                        } else {
                                            combos[0].hide();
                                        }
                                    },
                                    "render": function(cb) {
                                        // forcing other component hiding:
                                        cb.setValue('pdf');
                                        cb.fireEvent('select', cb, cb.getStore().getAt(0));
                                    }
                                }
                            }]
                        }, {
                            columnWidth: 0.5,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "combo",
                                name: "prefix_field",
                                anchor: '-10px',
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
                            }]
                        }]
                    }]
                }, { // LAYOUT
                    xtype: "fieldset",
                    autoheight: true,
                    title: this.tr("atlas_layout"),
                    items: [{
                        layout: "column",
                        border: false,
                        items: [{
                            columnWidth: 0.5,
                            border: false,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            items: [{
                                xtype: "combo",
                                name: "layout",
                                anchor: '-10px',
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
                            }, {
                                xtype: "checkbox",
                                name: "displayLegend",
                                labelStyle: "width:120px",
                                fieldLabel: this.tr("atlas_displaylegend")
                            }]
                        }, {
                            columnWidth: 0.5,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "combo",
                                name: "dpi",
                                fieldLabel: "Map dpi",
                                emptyText: "Select print resolution",
                                anchor: '-10px',
                                editable: false,
                                typeAhead: false,
                                autoComplete: false,
                                mode: "local",
                                store: this.printProvider.dpis,
                                value: this.printProvider.dpis.getAt(0).get('value'),
                                displayField: "name",
                                valueField: "value",
                                triggerAction: "all",
                                allowBlank: false
                            }]
                        }]
                    }]
                }, { // SCALE
                    xtype: "fieldset",
                    title: this.tr("atlas_scale"),
                    autoheight: true,
                    items: [{
                        layout: "column",
                        border: false,
                        items: [{
                            columnWidth: 0.4,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "radiogroup",
                                columns: 1,
                                hideLabel: true,
                                name: "scale_method_group",
                                listeners: {
                                    "change": function(rg, checked) {
                                        var form = rg.findParentByType("form"),
                                            combos = form.findBy(function(c) {
                                            return ((c.getXType() === "combo") &&
                                                (c.name === "scale_manual"));
                                        });
                                        if (checked.inputValue === "manual") {
                                            combos[0].show();
                                        } else {
                                            combos[0].hide();
                                        }
                                    },
                                    "render": function(rg) {
                                        // artifically forcing "manual" field hiding through:
                                        rg.fireEvent('change', rg, {inputValue: "bbox"});
                                    }
                                },
                                items: [{
                                    xtype: "radio",
                                    boxLabel: this.tr("atlas_bbox"),
                                    name: "r_scale_method",
                                    checked: true,
                                    inputValue: "bbox"
                                }, {
                                    xtype: "radio",
                                    boxLabel: this.tr("atlas_scalemanual"),
                                    name: "r_scale_method",
                                    inputValue: "manual"
                                }]
                            }]
                        }, {
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            columnWidth: 0.6,
                            border: false,
                            items: [{
                                xtype: "combo",
                                name: "scale_manual",
                                anchor: '-10px',
                                fieldLabel: this.tr("atlas_scale"),
                                emptyText: this.tr("atlas_selectscale"),
                                mode: "local",
                                triggerAction: "all",
                                store: new GeoExt.data.ScaleStore({
                                    map: this.mapPanel
                                }),
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
                            }]
                        }]
                    }]
                }, { // TITLE
                    xtype: "fieldset",
                    autoheight: true,
                    title: this.tr("atlas_pagetitle"),
                    items: [{
                        layout: "column",
                        border: false,
                        items: [{
                            columnWidth: 0.4,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "radiogroup",
                                columns: 1,
                                hideLabel: true,
                                name: "title_method_group",
                                listeners: {
                                    "change": function(rg, checked) {
                                        var form = rg.findParentByType("form"),
                                            titleField = form.findBy(function(c) {
                                            return ((c.getXType() === "textfield") &&
                                                (c.name === "titleText"));
                                        }),
                                            fieldField = form.findBy(function(c) {
                                            return ((c.getXType() === "combo") &&
                                                (c.name === "titleField"));
                                        });
                                        if (checked.inputValue === "same") {
                                            titleField[0].show();
                                            fieldField[0].hide();
                                        } else {
                                            titleField[0].hide();
                                            fieldField[0].show();
                                        }
                                    },
                                    "render": function(rg) {
                                        // artifically forcing "manual" field hiding through:
                                        rg.fireEvent('change', rg, {inputValue: "same"});
                                    }
                                },
                                items: [{
                                    boxLabel: this.tr("atlas_sametitle"),
                                    name: "titleMethod",
                                    inputValue: "same",
                                    checked: true
                                }, {
                                    boxLabel: this.tr("atlas_fieldtitle"),
                                    name: "titleMethod",
                                    inputValue: "field"
                                }]
                            }]

                        }, {
                            columnWidth: 0.6,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "textfield",
                                name: "titleText",
                                anchor: '-10px',
                                fieldLabel: this.tr("atlas_pagetitle"),
                                blankText: this.tr("atlas_title"),
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
                            }, {
                                xtype: "combo",
                                name: "titleField",
                                anchor: '-10px',
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
                            }]
                        }]
                    }]
                }, { // SUBTITLE
                    xtype: "fieldset",
                    autoheight: true,
                    title: this.tr("atlas_pagesubtitle"),
                    items: [{
                        layout: "column",
                        border: false,
                        items: [{
                            columnWidth: 0.4,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "radiogroup",
                                hideLabel: true,
                                columns: 1,
                                name: "subtitle_method_group",
                                items: [{
                                    boxLabel: this.tr("atlas_samesubtitle"),
                                    name: "subtitleMethod",
                                    inputValue: "same",
                                    checked: true
                                }, {
                                    boxLabel: this.tr("atlas_fieldsubtitle"),
                                    name: "subtitleMethod",
                                    inputValue: "field"
                                }],
                                listeners: {
                                    "change": function(rg, checked) {
                                        var form = rg.findParentByType("form"),
                                            subtitleField = form.findBy(function(c) {
                                            return ((c.getXType() === "textfield") &&
                                                (c.name === "subtitleText"));
                                        }),
                                            subfieldField = form.findBy(function(c) {
                                            return ((c.getXType() === "combo") &&
                                                (c.name === "subtitleField"));
                                        });
                                        if (checked.inputValue === "same") {
                                            subtitleField[0].show();
                                            subfieldField[0].hide();
                                        } else {
                                            subtitleField[0].hide();
                                            subfieldField[0].show();
                                        }
                                    },
                                    "render": function(rg) {
                                        // artifically forcing "manual" field hiding through:
                                        rg.fireEvent('change', rg, {inputValue: "same"});
                                    }
                                },
                            }]
                        }, {
                            columnWidth: 0.6,
                            layout: "form",
                            labelSeparator: this.tr("labelSeparator"),
                            border: false,
                            items: [{
                                xtype: "textfield",
                                name: "subtitleText",
                                anchor: '-10px',
                                fieldLabel: this.tr("atlas_pagesubtitle"),
                                blankText: this.tr("atlas_subtitle"),
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
                            }, {
                                xtype: "combo",
                                name: "subtitleField",
                                anchor: '-10px',
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
                            }]
                        }]
                    }]
                }, { // EMAIL
                    layout: "form",
                    labelSeparator: this.tr("labelSeparator"),
                    border: false,
                    labelWidth: 420,
                    items: [{
                        xtype: "textfield",
                        anchor: '-10px',
                        name: "email",
                        value: GEOR.config.USEREMAIL,
                        fieldLabel: this.tr("atlas_emaillabel"),
                        allowBlank: false,
                        vtype: "email"
                    }]
                }],
                buttons: [{
                    text: this.tr("atlas_cancel"),
                    handler: function() {
                        this.window.hide();
                    },
                    scope: this
                }, {
                    text: this.tr("atlas_submit"),
                    width: 100,
                    iconCls: this.options.iconCls,
                    handler: function(b) {
                        var formValues;
                        if (b.findParentByType("form").getForm().isValid()) {
                            formValues = b.findParentByType("form").getForm().getFieldValues();
                            this.parseForm(formValues);
                        }
                    },
                    scope: this
                }]
            }]
        });
    },

    /**
     * @function menuAction
     *
     * Ext component's (button or menuitem) handler used to launch atlas form
     *
     * @param atlasMenu - button or menuitem to which the handler is attached
     */

    menuAction: function(atlasMenu) {
        if (this.window.isVisible()) {
            return;
        }

        var atlasLayersStore = new GeoExt.data.LayerStore({
            fields: GEOR.ows.getRecordFields()
        });

        this.mapPanel.layers.each(function(layerRecord) {
            // we only act on vector layers that have a WFS counterpart:
            if (layerRecord.hasEquivalentWFS()) {
                atlasLayersStore.add(layerRecord); // layerRecord.copy() ?
            }
        });
        
        /// this is a temporary window in order to choose the layer on which the addon acts
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
                labelSeparator: this.tr("labelSeparator"),
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
                            this.layerRecord = record;
                            win.close();
                            this.window.show();
                        },
                        scope: this
                    }
                }]
            }]
        }).show();

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
    resultPanelHandler: function(resultpanel) { // TODO: find selected items in resultpanel !!!!!!!!!

        this.resultPanelFeatures = resultpanel._store;
// QUESTION: do we build our attribute store client side or do we rely on a fragile link (layer name) to find it from the server side ?
        var attributeStoreData = [];
        Ext.each(resultpanel._model.getFields(), function(fieldname) {
            attributeStoreData.push([fieldname]);
        });
        this.attributeStore = new Ext.data.ArrayStore({
            fields: ["name"],
            data: attributeStoreData.sort(function(a,b){
                return GEOR.util.sortFn(a[0],b[0]);
            })
        });

        var fieldsCombo = this.window.findBy(function(c) {
            return ((c.getXType() === "combo") &&
                ((c.name === "titleField") || (c.name === "subtitleField" || (c.name === "prefix_field"))));
        });
        Ext.each(fieldsCombo, function(fieldCombo) {
            fieldCombo.bindStore(this.attributeStore);
            fieldCombo.reset();
        }, this);

        var layerRecord;
        this.mapPanel.layers.each(function(r) {
            // find layerRecord based on title
            // this is very fragile, but it seems difficult to do better right now
            if (resultpanel.title === GEOR.util.shortenLayerName(r.get("title"))) {
                layerRecord = r;
            }
        });
        this.layerRecord = layerRecord;
        this.window.show();
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
        this.layerRecord = layerRecord;
        this.window.show();
    },

    /**
     * @function parseForm - parse form values
     * @private
     *
     * @param formValues - form values as returned by Ext.form.BasicForm.getFieldValues()
     * @param autoSubmit - Should we fire "featurelayerready" when parsing is done ?
     *     This will send request to atlas server
     */
    parseForm: function(formValues, autoSubmit) {
        var scaleParameters, titleSubtitleParameters;

        autoSubmit = autoSubmit || true;
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
            titleSubtitleParameters, formValues["prefix_field"], autoSubmit, formValues["resultPanel"]);

        // Form submit is triggered by "featurelayerready" event

        this.window.hide();
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
     * @param autoSubmit {Boolean} - Should we fire "featurelayerready" when parsing is done ?
     * @param resultPanel {Boolean} - True atlas is generated from result panel actions menu
     *     This will send request to atlas server
     */
    createFeatureLayerAndPagesSpecs: function(atlasLayer, scaleParameters, titleSubtitleParameters, fieldPrefix,
        autoSubmit, resultPanel) {

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
                this.attributeStore.sort('name');
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