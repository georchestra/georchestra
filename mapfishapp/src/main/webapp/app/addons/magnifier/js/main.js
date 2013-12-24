Ext.namespace("GEOR.Addons");

GEOR.Addons.Magnifier = function(map, options) {
    this.map = map;
    this.options = options;
    this.control = null;
    this.item = null;
};

// If required, may extend or compose with Ext.util.Observable
//Ext.extend(GEOR.Addons.Magnifier, Ext.util.Observable, { 
GEOR.Addons.Magnifier.prototype = {
    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        var lang = OpenLayers.Lang.getCode(),
            item = new Ext.menu.CheckItem({
                text: record.get("title")[lang] || record.get("title")["en"],
                qtip: record.get("description")[lang] || record.get("description")["en"],
                //iconCls: "addon-magnifier",
                checked: false,
                listeners: {
                    "checkchange": this.onCheckchange,
                    scope: this
                }
            });
        this.item = item;
        return item;
    },

    /**
     * Method: onCheckchange
     * Callback on checkbox state changed
     */
    onCheckchange: function(item, checked) {
        if (checked) {
            var control = new OpenLayers.Control.Magnifier(this.options);
            this.map.addControl(control);
            control.update();
            this.control = control;
        } else {
            this.control && this.control.destroy();
        }
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.control && this.control.destroy();
        this.control = null;
        this.map = null;
    }
};