/**
 * Copyright (c) 2008 The Open Planning Project
 *
 * @include Styler/widgets/FillSymbolizer.js
 * @include Styler/widgets/form/FontComboBox.js
 */
Ext.namespace("Styler");

Styler.TextSymbolizer = Ext.extend(Ext.Panel, {
    
    /**
     * Property: symbolizer
     * {Object} A symbolizer object that will be used to fill in form values.
     *     This object will be modified when values change.  Clone first if
     *     you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /**
     * Property: defaultSymbolizer
     * {Object} Default symbolizer properties to be used where none provided.
     */
    defaultSymbolizer: null,
    
    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} A configured attributes store for use in
     *     the filter property combo.
     */
    attributes: null,
    
    /**
     * Property: haloCache
     * {Object} Stores halo properties while fieldset is collapsed.
     */
    haloCache: null,
    
    border: false,    
    layout: "form",
    
    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }        
        Ext.applyIf(this.symbolizer, this.defaultSymbolizer);

        this.haloCache = {};

        var defAttributesComboConfig = {
            xtype: "combo",
            fieldLabel: "Attribut",
            store: this.attributes,
            editable: false,
            triggerAction: "all",
            allowBlank: false,
            displayField: "name",
            valueField: "name",
            value: this.symbolizer.label && this.symbolizer.label.replace(/^\${(.*)}$/, "$1"),
            listeners: {
                select: function(combo, record) {
                    this.symbolizer.label = "${" + record.get("name") + "}";
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 120
        };
        this.attributesComboConfig = this.attributesComboConfig || {};
        Ext.applyIf(this.attributesComboConfig, defAttributesComboConfig);
        
        this.labelWidth = 80;
        
        
        this.items = [this.attributesComboConfig, {
            cls: "x-html-editor-tb",
            style: "background: transparent; border: none; padding: 0 0em 0.5em;",
            xtype: "toolbar",
            items: [{
                xtype: "gx_fontcombo",
                width: 110,
                value: this.symbolizer.fontFamily,
                listeners: {
                    select: function(combo, record) {
                        this.symbolizer.fontFamily = record.get("text");
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "tbtext",
                text: "Size: "
            }, {
                xtype: "textfield",
                value: this.symbolizer.fontSize,
                width: 30,
                listeners: {
                    valid: function(field) {
                        this.symbolizer.fontSize = Number(field.getValue());
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                enableToggle: true,
                cls: "x-btn-icon",
                iconCls: 'x-edit-bold',
                pressed: this.symbolizer.fontWeight === "bold",
                listeners: {
                    toggle: function(button, pressed) {
                        this.symbolizer.fontWeight = pressed ? "bold" : "normal";
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                enableToggle: true,
                cls: "x-btn-icon",
                iconCls: 'x-edit-italic',
                pressed: this.symbolizer.fontStyle === "italic",
                listeners: {
                    toggle: function(button, pressed) {
                        this.symbolizer.fontStyle = pressed ? "italic" : "normal";
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }]
        }, {
            xtype: "gx_fillsymbolizer",
            symbolizer: this.symbolizer,
            width: 220,
            labelWidth: 70,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }, {
            xtype: "fieldset",
            title: "Halo",
            checkboxToggle: true,
            collapsed: !(this.symbolizer.haloRadius || this.symbolizer.haloColor || this.symbolizer.haloOpacity),
            autoHeight: true,
            labelWidth: 50,
            items: [{
                xtype: "textfield",
                fieldLabel: "Taille",
                anchor: "89%",
                value: this.symbolizer.haloRadius,
                listeners: {
                    valid: function(field) {
                        this.symbolizer.haloRadius = field.getValue();
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "gx_fillsymbolizer",
                symbolizer: {
                    fillColor: this.symbolizer.haloColor,
                    fillOpacity: this.symbolizer.haloOpacity
                },
                width: 220,
                labelWidth: 60,
                listeners: {
                    change: function(symbolizer) {
                        this.symbolizer.haloColor = symbolizer.fillColor;
                        this.symbolizer.haloOpacity = symbolizer.fillOpacity;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }],
            listeners: {
                collapse: function() {
                    this.haloCache = {
                        haloRadius: this.symbolizer.haloRadius,
                        haloColor: this.symbolizer.haloColor,
                        haloOpacity: this.symbolizer.haloOpacity
                    };
                    delete this.symbolizer.haloRadius;
                    delete this.symbolizer.haloColor;
                    delete this.symbolizer.haloOpacity;
                    this.fireEvent("change", this.symbolizer)
                },
                expand: function() {
                    Ext.apply(this.symbolizer, this.haloCache);
                    // in ext3 collapsed fieldset are not rendered
                    // so we have to call doLayout by hand when
                    // expanding the fieldset
                    this.doLayout();
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with text related properties
             *     updated.
             */
            "change"
        ); 
 
        Styler.TextSymbolizer.superclass.initComponent.call(this);
        
    }
    
    
});

Ext.reg('gx_textsymbolizer', Styler.TextSymbolizer); 
