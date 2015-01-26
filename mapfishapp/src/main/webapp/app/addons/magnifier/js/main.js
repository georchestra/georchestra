Ext.namespace("GEOR.Addons");

GEOR.Addons.Magnifier = Ext.extend(GEOR.Addons.Base, {

    /**
     * Property: control
     * {OpenLayers.Control.Magnifier}
     */
    control: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        if (this.target) {
            // create button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: 'addon-magnifier',
                listeners: {
                    "toggle": this.onCheckchange,
                    scope: this
                }
            });
            this.target.doLayout();
        } else {
            // create menu item for the "tools" menu:
            this.item =  new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                checked: false,
                listeners: {
                    "checkchange": this.onCheckchange,
                    scope: this
                }
            });
        }
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

        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});