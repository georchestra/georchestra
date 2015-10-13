Ext.namespace("GEOR.Addons");

GEOR.Addons.Fullscreen = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     */
    init: function(record) {
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: 'fullscreen-icon',
                listeners: {
                    "click": this.onClick,
                    scope: this
                }
            });
            this.target.doLayout();
        } else {
            // addon placed in "tools menu"
            this.item = new Ext.menu.Item({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: 'fullscreen-icon',
                listeners: {
                    "click": this.onClick,
                    scope: this
                }
            });
        }
    },

    /**
     * Method: onClick
     *
     */
    onClick: function() {
        var api = window.fullScreenApi;
        if (api.supportsFullScreen) {
            api.requestFullScreen(
                this.map.div.childNodes[0]
            );
        } else {
            var p = GeoExt.MapPanel.guess().ownerCt;
            p.items.get(0).setSize(0, 0); 
            p.items.get(p.items.getCount() - 2).collapse();
            p.doLayout();
        }
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});