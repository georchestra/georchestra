Ext.namespace("GEOR.Addons");

//Replace Template by a representative name
GEOR.Addons.Template = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: "addon-template",
                handler: this._sampleHandler,
                scope: this
            });
            this.target.doLayout();

        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-template",
                checked: false,
                listeners: {
                    "checkchange": this._sampleHandler,
                    scope: this
                }
            });
        }
    },

    _sampleHandler: function() {
        GEOR.helper.msg(this.options.title, this.tr("addon_template_sample_message"))
    },

    tr: function(str) {
        return OpenLayers.i18n(str);
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        //Place addon specific destroy here

        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
