Ext.namespace("GEOR.Addons");

GEOR.Addons.Annotation = function(map, options) {
    this.map = map;
    this.options = options;
    this.control = null;
    this.item = null;
    this.window = null;
};

// If required, may extend or compose with Ext.util.Observable
//Ext.extend(GEOR.Addons.Annotation, Ext.util.Observable, {
GEOR.Addons.Annotation.prototype = {
    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {

        var annotation = new GEOR.Annotation({
            map: this.map,
            popupOptions: {unpinnable: false, draggable: true}
        });
        this.window = new Ext.Window({
            title: OpenLayers.i18n('annotation.drawing_tools'),
            width: 440,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: 'annotation',
            items: [{
                xtype: 'toolbar',
                border: false,
                items: annotation.actions
            }],
            listeners: {
                "hide": function() {
                    item.setChecked(false);
                },
                scope: this
            }
        });

        var lang = OpenLayers.Lang.getCode(),
            item = new Ext.menu.CheckItem({
                text: record.get("title")[lang] || record.get("title")["en"],
                qtip: record.get("description")[lang] || record.get("description")["en"],
                iconCls: "addon-annotation",
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
            this.window.show();
            this.window.alignTo(
                Ext.get(this.map.div),
                "t-t",
                [0, 5],
                true
            );
        } else {
            this.window.hide();
        }
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.window.hide();
        this.control = null;
        this.map = null;
    }
};
