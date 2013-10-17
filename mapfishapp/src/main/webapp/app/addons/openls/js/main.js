Ext.namespace("GEOR.Addons");

GEOR.Addons.OpenLS = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.OpenLS.prototype = {

    window: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.window = new Ext.Window({
            title: OpenLayers.i18n('openls.window_title'),
            width: 440,
            height: 100,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: "openls",
            items: [{
                
            }],
            listeners: {
                "hide": function() {
                    
                }
            }
        });
        var lang = OpenLayers.Lang.getCode(),
            item = new Ext.menu.Item({
                text: record.get("title")[lang] || record.get("title")["en"],
                qtip: record.get("description")[lang] || record.get("description")["en"],
                iconCls: "addon-openls",
                handler: this.showWindow,
                scope: this
            });
        this.item = item;
        return item;
    },

    /**
     * Method: showWindow
     */
    showWindow: function() {
        this.window.show();
        this.window.alignTo(
            Ext.get(this.map.div),
            "t-t",
            [0, 5],
            true
        );
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.window.hide();
        this.map = null;
    }
};