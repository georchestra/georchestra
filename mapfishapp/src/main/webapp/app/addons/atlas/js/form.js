/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons.Atlas");

GEOR.Addons.Atlas.Form = Ext.extend(Object, {

    form: null,

    constructor: function(atlasAddon) {
        this.layerRecord = atlasAddon.layerRecord;
        this.attributeStore = atlasAddon.attributeStore;
        this.printProvider = atlasAddon.printProvider;
        this.map = atlasAddon.map;

        this.form = new Ext.form.FormPanel({
            border: false,
            items: [
                this.getFileNamePanel(), 
                this.getFormatPanel(),
                this.getLayoutPanel(),
                this.getScalePanel(),
                this.getTitlePanel(),
                this.getSubTitlePanel(),
                this.getEmailPanel()
            ]
        });

        this.sep = OpenLayers.i18n("labelSeparator");
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
     * @function getFileNamePanel
     *
     */
    getFileNamePanel: function() {
        return {
            layout: "form",
            labelSeparator: this.sep,
            border: false,
            items: [{
                xtype: "textfield",
                anchor: '-30px',
                name: "outputFilename",
                fieldLabel: this.tr("atlas_outputfilename"),
                value: [
                    new Date().toISOString().slice(0,19).replace(/T|:|-/g, ''),
                    "_",
                    this.layerRecord.get("name").replace(':','_'),
                    "_atlas"
                ].join(''),
                allowBlank: false
            }]
        };
    },


    /**
     * @function getFormatPanel
     *
     */
    getFormatPanel: function() {
        return { // FORMAT (PDF or ZIP)
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
                    labelSeparator: this.sep,
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
                    labelSeparator: this.sep,
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
                        triggerAction: "all"
                    }]
                }]
            }]
        };
    },


    /**
     * @function getLayoutPanel
     *
     */
    getLayoutPanel: function() {
        return { // LAYOUT
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
                    labelSeparator: this.sep,
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
                        value: this.printProvider.layouts.getAt(0).get('name'),
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
                    labelSeparator: this.sep,
                    border: false,
                    items: [{
                        xtype: "combo",
                        name: "dpi",
                        fieldLabel: this.tr("atlas_mapdpi"),
                        emptyText: this.tr("atlas_selectdpi"),
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
        };
    },


    /**
     * @function getScalePanel
     *
     */
    getScalePanel: function() {
        return { // SCALE
            xtype: "fieldset",
            title: this.tr("atlas_scale"),
            autoheight: true,
            items: [{
                layout: "column",
                border: false,
                items: [{
                    columnWidth: 0.4,
                    layout: "form",
                    labelSeparator: this.sep,
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
                    labelSeparator: this.sep,
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
                            map: this.map
                        }),
                        valueField: "scale",
                        displayField: "scale",
                        tpl: [
                            '<tpl for=".">',
                                '<div class="x-combo-list-item">',
                                    '1 : {[OpenLayers.Number.format(values.scale, 0)]}',
                                '</div>',
                            '</tpl>'
                        ].join(''),
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
                        },
                        listeners: {
                            "select": function(cb, r, idx) {
                                cb.setValue(
                                    "1 : " + OpenLayers.Number.format(r.get("scale"), 0)
                                );
                            }
                        }
                    }]
                }]
            }]
        };
    },


    /**
     * @function getTitlePanel
     *
     */
    getTitlePanel: function() {
        return { // TITLE
            xtype: "fieldset",
            autoheight: true,
            title: this.tr("atlas_pagetitle"),
            items: [{
                layout: "column",
                border: false,
                items: [{
                    columnWidth: 0.4,
                    layout: "form",
                    labelSeparator: this.sep,
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
                    labelSeparator: this.sep,
                    border: false,
                    items: [{
                        xtype: "textfield",
                        name: "titleText",
                        value: this.layerRecord.get('title'),
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
        };
    },


    /**
     * @function getSubTitlePanel
     *
     */
    getSubTitlePanel: function() {
        return { // SUBTITLE
            xtype: "fieldset",
            autoheight: true,
            title: this.tr("atlas_pagesubtitle"),
            items: [{
                layout: "column",
                border: false,
                items: [{
                    columnWidth: 0.4,
                    layout: "form",
                    labelSeparator: this.sep,
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
                    labelSeparator: this.sep,
                    border: false,
                    items: [{
                        xtype: "textfield",
                        name: "subtitleText",
                        //value: this.tr("subtitle"),
                        anchor: '-10px',
                        fieldLabel: this.tr("atlas_pagesubtitle"),
                        blankText: this.tr("atlas_subtitle")
                        // allow blank subtitle :
                        /*,
                        validator: function(value) {
                            var radioSubtitle, valid;
                            radioSubtitle = this.findParentByType("form").findBy(function(c) {
                                return ((c.getXType() === "radiogroup") &&
                                    (c.name === "title_method_group"));
                            })[0];
                            valid = !((radioSubtitle.getValue().inputValue === "same") &&
                                (value === ""));
                            return valid;
                        }*/
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
        };
    },


    /**
     * @function getEmailPanel
     *
     */
    getEmailPanel: function() {
        return { // EMAIL
            layout: "form",
            labelSeparator: this.sep,
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
        };
    }
});
