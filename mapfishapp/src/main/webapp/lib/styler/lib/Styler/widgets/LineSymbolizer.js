/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/StrokeSymbolizer.js
 */

Ext.namespace("Styler");

Styler.LineSymbolizer = Ext.extend(Ext.Panel, {

    /**
     * Property: symbolizer
     * {Object} A symbolizer object that will be used to fill in form values.
     *     This object will be modified when values change.  Clone first if
     *     you do not want your symbolizer modified.
     */
    symbolizer: null,

    initComponent: function() {
        
        this.items = [{
            xtype: "gx_strokesymbolizer",
            symbolizer: this.symbolizer,
            listeners: {
                change: function(symbolizer) {
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
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 

        Styler.LineSymbolizer.superclass.initComponent.call(this);

    }
    
    
});

Ext.reg('gx_linesymbolizer', Styler.LineSymbolizer);