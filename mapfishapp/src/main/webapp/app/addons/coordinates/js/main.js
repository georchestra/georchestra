/*global
 Ext, GeoExt, OpenLayers, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Coordinates = Ext.extend(GEOR.Addons.Base, {

    up: false,
    popups: null,

    init: function(record) {
        this.popups = [];
        var style = {
            externalGraphic: GEOR.config.PATHNAME + "/app/addons/coordinates/img/target.png",
            graphicWidth: 16,
            graphicHeight: 16
        };
        this.layer = new OpenLayers.Layer.Vector("__georchestra_coordinates", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": style,
                "temporary": style
            }),
            eventListeners: {
                "featureadded": this.onFeatureadded,
                scope: this
            }
        });
        this.map.addLayer(this.layer);

        this.control = new OpenLayers.Control.DrawFeature(
            this.layer, OpenLayers.Handler.Point
        );
        this.map.addControl(this.control);

        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: "button",
                tooltip: this.getTooltip(record),
                iconCls: "addon-coordinates",
                handler: this.onClick,
                scope: this
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.Item({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-coordinates",
                checked: false,
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
    onClick: function(item) {
        if (!this.up) {
            this.up = true;
            this.control.activate();
        }
    },

    /**
     * Method: onFeatureadded
     *
     */
    onFeatureadded: function(o) {
        this.up = false;
        this.control.deactivate();
        var f = o.feature,
        popup = new GeoExt.Popup({
            map: this.map,
            title: this.tr("Coordinates"),
            cls: "addon-coordinates-popup",
            bodyStyle: "padding:5px;",
            unpinnable: false,
            resizable: false,
            closeAction: "close",
            location: f,
            anchored: true,
            html: this.buildContent(f),
            listeners: {
                "close": function() {
                    f.destroy();
                }
            }
        });
        this.popups.push(popup);
        popup.show();
    },

    /**
     * Method: buildContent
     *
     */
    buildContent: function(feature) {
        var geom = feature.geometry,
            orig = new OpenLayers.Projection(this.map.getProjection())
            out = [];
        Ext.each(this.options.projections, function(p) {
            var dest = new OpenLayers.Projection(p.srs)
            var g = geom.clone().transform(orig, dest);
            var str = [
                "<div class=\"coords\">",
                    "<p><b>", p.name,"</b></p>",
                    "<p>", this.getCoordinatesLabel(dest, 0), this.tr("labelSeparator"), GEOR.util.round(g.x, p.decimals),"</p>",
                    "<p>", this.getCoordinatesLabel(dest, 1), this.tr("labelSeparator"), GEOR.util.round(g.y, p.decimals),"</p>",
                "</div>"
            ].join("");
            out.push(str);
        }, this);
        return out.join("<hr>");
    },

    /**
     * Method: tr
     *
     */
    tr: function(str) {
        return OpenLayers.i18n(str);
    },

    /**
     * Method: getCoodinatesLabel
     *
     */
    getCoordinatesLabel: function(p, idx) {
        if ((idx !== 0) && (idx !== 1)) {
            alert("Coordinates addon: the only values accepted in idx are 0 and 1, got: " + idx)
        }
        if (p.proj.projName === "longlat") {
            return OpenLayers.i18n("coordinates.longlat." + idx);
        } else {
            return OpenLayers.i18n("coordinates.xy." + idx);
        }
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.up = false;
        Ext.each(this.popups, function(p) {
            p.destroy();
        });
        this.control.deactivate();
        this.map.removeControl(this.control);
        this.layer.destroyFeatures();
        this.map.removeLayer(this.layer);
        this.control = null;
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
