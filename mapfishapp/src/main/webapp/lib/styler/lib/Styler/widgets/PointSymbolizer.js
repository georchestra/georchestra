/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/FillSymbolizer.js
 * @include Styler/widgets/StrokeSymbolizer.js
 * @include GeoExt/widgets/tips/SliderTip.js
 */

Ext.namespace("Styler");

Styler.PointSymbolizer = Ext.extend(Ext.Panel, {

    /**
     * Property: symbolizer
     * {Object} A symbolizer object that will be used to fill in form values.
     *     This object will be modified when values change.  Clone first if
     *     you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /**
     * Property: pointGraphics
     * {Array} A list of objects to be used as the root of the data for a
     *     JsonStore.  These will become records used in the selection of
     *     a point graphic.  If an object in the list has no "value" property,
     *     the user will be presented with an input to provide their own URL
     *     for an external graphic.  By default, names of well-known marks are
     *     provided.  In addition, the default list will produce a record with
     *     display of "external" that create an input for an external graphic
     *     URL.
     *
     * Fields:
     * display - {String} The name to be displayed to the user.
     * preview - {String} URL to a graphic for preview.
     * value - {String} Value to be sent to the server.
     * mark - {Boolean} The value is a well-known name for a mark.  If false,
     *     the value will be assumed to be a url for an external graphic.
     */
    pointGraphics: [
        {display: OpenLayers.i18n("Circle"), value: "circle", mark: true},
        {display: OpenLayers.i18n("Square"), value: "square", mark: true},
        {display: OpenLayers.i18n("Triangle"), value: "triangle", mark: true},
        {display: OpenLayers.i18n("Star"), value: "star", mark: true},
        {display: OpenLayers.i18n("Cross"), value: "cross", mark: true},
        {display: OpenLayers.i18n("X"), value: "x", mark: true},
        {display: OpenLayers.i18n("External")}
    ],
    
    /**
     * Property: defaultSymbolizer
     * {Object} Default symbolizer properties to be used where none provided.
     */
    defaultSymbolizer: {
        "graphicName": "circle",
        "rotation": 0,
        "pointRadius": 3,
        "fillColor": "#ffffff",
        "fillOpacity": 1,
        "strokeDashstyle": "solid",
        //"strokeColor": "#666666",
        //"strokeWidth": 1,
        "strokeOpacity": 1
    },
    
    /**
     * Property: external
     * {Boolean} Currently using an external graphic.
     */
    external: null,
    
    /**
     * Property: layout
     * {String} form
     */
    layout: "form",

    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }        
        Ext.applyIf(this.symbolizer, this.defaultSymbolizer);
        
        this.external = !!this.symbolizer.externalGraphic;

        this.markPanel = new Ext.Panel({
            border: false,
            collapsed: this.external,
            layout: "form",
            items: [{
                xtype: "gx_fillsymbolizer",
                symbolizer: this.symbolizer,
                labelWidth: this.labelWidth,
                labelAlign: this.labelAlign,
                listeners: {
                    change: function(symbolizer) {
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "gx_strokesymbolizer",
                symbolizer: this.symbolizer,
                labelWidth: this.labelWidth,
                labelAlign: this.labelAlign,
                listeners: {
                    change: function(symbolizer) {
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }]
        });
        
        this.urlField = new Ext.form.TextField({
            name: "url",
            fieldLabel: OpenLayers.i18n("URL"),
            value: this.symbolizer.externalGraphic,
            hidden: true,
            listeners: {
                change: function(field, value) {
                    this.symbolizer.externalGraphic = value;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        });
        
        this.graphicPanel = new Ext.Panel({
            border: false,
            collapsed: !this.external,
            layout: "form",
            items: [this.urlField, {
                xtype: "slider",
                name: "opacity",
                fieldLabel: OpenLayers.i18n("Opacity"),
                value: (this.symbolizer.graphicOpacity === null) ? 100 : this.symbolizer.graphicOpacity * 100,
                isFormField: true,
                listeners: {
                    changecomplete: function(slider, value) {
                        this.symbolizer.graphicOpacity = value / 100;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                },
                plugins: [
                    new GeoExt.SliderTip({
                        getText: function(thumb) {
                            return thumb.value + "%";
                        }
                    })
                ],
                width: 100 // TODO: push this to css                
            }]
        });

        this.items = [{
            xtype: "combo",
            name: "mark",
            fieldLabel: OpenLayers.i18n("Symbol"),
            store: new Ext.data.JsonStore({
                data: {root: this.pointGraphics},
                root: "root",
                fields: ["value", "display", "preview", {name: "mark", type: "boolean"}]
            }),
            value: this.external ? 0 : this.symbolizer.graphicName,
            displayField: "display",
            valueField: "value",
            tpl: new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div class="x-combo-list-item gx-pointsymbolizer-mark-item">' +
                    '<tpl if="preview">' +
                        '<img src="{preview}" alt="{display}"/>' +
                    '</tpl>' +
                    '<span>{display}</span>' +
                '</div></tpl>'
            ),
            mode: "local",
            allowBlank: false,
            triggerAction: "all",
            editable: false,
            listeners: {
                select: function(combo, record) {
                    var mark = record.get("mark");
                    var value = record.get("value");
                    if(!mark) {
                        if(value) {
                            this.urlField.hide();
                            // this to hide the container - otherwise the label remains
                            this.urlField.getEl().up('.x-form-item').setDisplayed(false);
                            this.symbolizer.externalGraphic = value;
                        } else {
                            this.urlField.show();
                            this.urlField.getEl().up('.x-form-item').setDisplayed(true);
                        }
                        if(!this.external) {
                            this.external = true;
                            this.updateGraphicDisplay();
                        }
                    } else {
                        if(this.external) {
                            this.external = false;
                            delete this.symbolizer.externalGraphic;
                            this.updateGraphicDisplay();
                        }
                        this.symbolizer.graphicName = value;
                    }
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, {
            xtype: "textfield",
            name: "size",
            fieldLabel: OpenLayers.i18n("Radius"),
            value: this.symbolizer.pointRadius,
            listeners: {
                change: function(field, value) {
                    this.symbolizer.pointRadius = value;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, {
            xtype: "textfield",
            name: "rotation",
            fieldLabel: OpenLayers.i18n("Rotation"),
            value: this.symbolizer.rotation,
            listeners: {
                change: function(field, value) {
                    this.symbolizer.rotation = value;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, this.markPanel, this.graphicPanel
        ];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 

        Styler.PointSymbolizer.superclass.initComponent.call(this);

    },
    
    updateGraphicDisplay: function() {
        if(this.external) {
            this.markPanel.collapse();
            this.graphicPanel.expand();
        } else {
            this.graphicPanel.collapse();
            this.markPanel.expand();
        }
        // TODO: window shadow fails to sync
    }
    
    
});

Ext.reg('gx_pointsymbolizer', Styler.PointSymbolizer);
