/**
 * Copyright (c) 2009 Camptocamp
 */

Ext.namespace("Styler.form");
Styler.form.SpatialComboBox = Ext.extend(Ext.form.ComboBox, {
    
    allowedTypes: [ // TODO: i18n
        [OpenLayers.Filter.Spatial.INTERSECTS, "intersection avec"],
        [OpenLayers.Filter.Spatial.WITHIN, "à l'intérieur de"],
        [OpenLayers.Filter.Spatial.CONTAINS, "contient l'objet"]
    ],

    allowBlank: false,

    mode: "local",

    triggerAction: "all",

    width: 100,

    editable: false,
  
    initComponent: function() {
        var defConfig = {
            displayField: "name",
            valueField: "value",
            store: new Ext.data.SimpleStore({
                data: this.allowedTypes,
                fields: ["value", "name"]
            }),
            value: (this.value === undefined) ? this.allowedTypes[0][0] : this.value
        };
        Ext.applyIf(this, defConfig);
        
        Styler.form.SpatialComboBox.superclass.initComponent.call(this);
    }
});

Ext.reg("gx_spatialcombo", Styler.form.SpatialComboBox);