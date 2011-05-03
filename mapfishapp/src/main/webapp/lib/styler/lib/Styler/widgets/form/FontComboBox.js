/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler.form");
Styler.form.FontComboBox = Ext.extend(Ext.form.ComboBox, {
    
    /**
     * Property: fonts
     * {Array} List of font families to choose from.  Default is ["Arial",
     *     "Courier New", "Tahoma", "Times New Roman", "Verdana"].
     */
    fonts: [
        "Arial",
        "Courier New",
        "Tahoma",
        "Times New Roman",
        "Verdana"
    ],
    
    /**
     * Property: defaultFont
     * {String} The <fonts> item to select by default.
     */
    defaultFont: "Tahoma",

    allowBlank: false,
    mode: "local",
    triggerAction: "all",
    editable: false,
  
    initComponent: function() {
        var defConfig = {
            displayField: "text",
            valueField: "text",
            store: this.fonts,
            value: this.defaultFont,
            tpl: new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div class="x-combo-list-item">' +
                    '<span style="font-family: {text};">{text}</span>' +
                '</div></tpl>'
            )
        };
        Ext.applyIf(this, defConfig);
        
        Styler.form.FontComboBox.superclass.initComponent.call(this);
    }
});

Ext.reg("gx_fontcombo", Styler.form.FontComboBox);
