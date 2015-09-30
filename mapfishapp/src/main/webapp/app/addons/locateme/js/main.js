Ext.namespace("GEOR.Addons");

/**
 * TODO: while pressed, follow user location
 **/
GEOR.Addons.LocateMe = Ext.extend(GEOR.Addons.Base, {
    layer: null,
    item: null,

    /**
     * Method: init
     *
     */
    init: function(record) {
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: 'locateme-icon',
                listeners: {
                    "toggle": function(b, pressed) {
                        if (pressed) {
                            this.locateme();
                        } else {
                            this.layer.destroyFeatures();
                        }
                    },
                    scope: this
                },
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
        this.layer = new OpenLayers.Layer.Vector("__georchestra_locateme", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": this.options.graphicStyle
            })
        });
        this.map.addLayer(this.layer);
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
            this.errorHandling.createDelegate(this)
        );
    },

    /**
     * Method: showLocation
     *
     */
    showLocation: function(pos) {
        var c = pos.coords,
        geom = (new OpenLayers.Geometry.Point(c.longitude, c.latitude)).transform(
            new OpenLayers.Projection("EPSG:4326"),
            this.map.getProjectionObject()
        );
        this.layer.destroyFeatures();
        this.layer.addFeatures([new OpenLayers.Feature.Vector(geom)]);
        this.map.setCenter([geom.x, geom.y]);
    },

    /**
     * Method: errorHandling
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
        this.map.removeLayer(this.layer);
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});