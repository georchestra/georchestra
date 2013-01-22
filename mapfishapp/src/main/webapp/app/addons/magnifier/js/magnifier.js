Ext.namespace("GEOR.Addons");

GEOR.Addons.Magnifier = function(map, options) {
    this.map = map;
    this.options = options;
    this.control = null;
};

// possibly extend Ext.util.Observable:
//Ext.extend(GEOR.Addons.Magnifier, Ext.util.Observable, { 

Ext.extend(GEOR.Addons.Magnifier, Ext.util.Observable, {

    // TODO: doc (record)
    init: function(record) {
        var lang = OpenLayers.Lang.getCode();
        return new Ext.menu.CheckItem({
            text: record.get("title")[lang],
            qtip: record.get("description")[lang],
            group: "measure",
            iconCls: "addon-magnifier",
            listeners: {
                "checkchange": this.onCheckchange,
                scope: this
            }
        });
    },

    onCheckchange: function(item, checked) {
        if (checked) {
            var control = new OpenLayers.Control.Magnifier(this.options);
            this.map.addControl(control);
            control.update();
            this.control = control;
        } else {
            this.control.destroy();
        }
    }

});