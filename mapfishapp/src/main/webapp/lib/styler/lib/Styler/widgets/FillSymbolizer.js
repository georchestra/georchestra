/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include GeoExt/widgets/tips/SliderTip.js
 */

Ext.namespace("Styler");

Styler.FillSymbolizer = Ext.extend(Ext.FormPanel, {
    
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
    
    border: false,
    
    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }        
        Ext.applyIf(this.symbolizer, this.defaultSymbolizer);
        
        this.items = [{
            xtype: "fieldset",
            title: OpenLayers.i18n("Fill"),
            autoHeight: true,
            defaults: {
                width: 100 // TODO: move to css
            },
            items: [{
                xtype: "colorpickerfield",
                fieldLabel: OpenLayers.i18n("Color"),
                name: "color",
                value: this.symbolizer.fillColor,
                listeners: {
                    valid: function(field) {
                        this.symbolizer.fillColor = field.getValue();
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "slider",
                fieldLabel: OpenLayers.i18n("Opacity"),
                name: "opacity",
                value: (this.symbolizer.fillOpacity === null) ? 100 : this.symbolizer.fillOpacity * 100,
                isFormField: true,
                listeners: {
                    changecomplete: function(slider, value) {
                        this.symbolizer.fillOpacity = value / 100;
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
                ]
            }],
            listeners: {
                "collapse": function() {
                    this.symbolizer.fill = false;
                    this.fireEvent("change", this.symbolizer);
                },
                "expand": function() {
                    this.symbolizer.fill = true;
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
             * symbolizer - {Object} A symbolizer with fill related properties
             *     updated.
             */
            "change"
        ); 

        Styler.FillSymbolizer.superclass.initComponent.call(this);
        
    }
    
    
});

Ext.reg('gx_fillsymbolizer', Styler.FillSymbolizer); 
