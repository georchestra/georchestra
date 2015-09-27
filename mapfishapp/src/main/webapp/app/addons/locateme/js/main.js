Ext.namespace("GEOR.Addons");

GEOR.Addons.LocateMe = Ext.extend(GEOR.Addons.Base, {
    layer: null,
    item: null,

    /**
     * Method: init
     *
     */
    init: function(record) {
        this.layer = this.createLayer();
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: 'locateme-icon',
                handler: this.locateme,
                scope: this
            });
            this.target.doLayout();
        } else {
            // addon placed in "tools menu"
            this.item = new Ext.menu.Item({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: 'locateme-icon',
                handler: this.locateme,
                scope: this
            });
        }
    },

    /**
     * Method: createLayer
     *
     */
    createLayer: function() {
        return new OpenLayers.Layer.Vector("locateme_layername", {
            displayInLayerSwitcher: false
        });
    },

    /**
     * Method: showPosition
     *
     */
    locateme: function() {
        if (!navigator.geolocation) {
            GEOR.util.errorDialog({
                msg: this.tr("locateme_nosupport")
            });
            return;
        }
        navigator.geolocation.getCurrentPosition(
            this.showLocation.createDelegate(this),
            this.errorHandling
        );
    },

    /**
     * Method: showLocation
     *
     */
    showLocation: function(pos) {
        // TODO: vector layer position ...
        alert("Latitude: " + pos.coords.latitude + " - Longitude: " + pos.coords.longitude); 
    },

    /**
     * Method: showLocation
     *
     */
    errorHandling: function (error) {
        var msg = "locateme_unknownerror";
        switch(error.code) {
            case error.PERMISSION_DENIED:
                msg = "locateme_denied";
                break;
            case error.POSITION_UNAVAILABLE:
                msg = "locateme_unavailable";
                break;
            case error.TIMEOUT:
                msg = "locateme_timedout";
                break;
        }
        GEOR.util.errorDialog({
            msg: this.tr(msg)
        });
    },

    /**
     * Method: tr
     *
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.layer.destroy();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});