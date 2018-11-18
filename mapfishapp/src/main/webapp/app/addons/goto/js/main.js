/*global
 Ext, GeoExt, OpenLayers, GEOR
 */

Ext.namespace("GEOR.Addons");

GEOR.Addons.Goto = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.sep = OpenLayers.i18n("labelSeparator");
        this.layer = new OpenLayers.Layer.Vector("__georchestra_goto_addon", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": this.options.graphicStyle
            })
        });
        this.map.addLayer(this.layer);

        this.store = new Ext.data.JsonStore({
            data: this.options.projections,
            idProperty: "srs",
            fields: ["srs", "name", "labels"]
        });
        if (this.store.getCount() > 0) {
            this.defaultSRS = this.store.getAt(0);
        } else {
            alert("Goto addon: config error - no projection defined in options.")
        }

        this.combo = new Ext.form.ComboBox({
            mode: "local",
            displayField: "name",
            valueField: "srs",
            fieldLabel: OpenLayers.i18n("goto.srs.label"),
            editable: false,
            store: this.store,
            value: this.options.projections[0].srs,
            triggerAction: "all",
            width: 120,
            listeners: {
                "select": this.onComboSelect,
                scope: this
            }
        });

        if (this.target.getXType() === "tabpanel") {
            this.components = this.target.insert(this.position, 
                this.createTabpanelComponent());
        } else {
            alert("Goto addon: config error - unsupported target.");
        }
        this.target.doLayout();
    },

    /**
     * Method: getFields
     *
     */
    getFields: function() {
        var out = {},
            fields = this.components.findByType("numberfield");
        Ext.each(fields, function(f) {
            var name = f.getName();
            if (name == "x") {
                out.x = f;
            } else if (name == "y") {
                out.y = f;
            }
        });
        return out;
    },

    /**
     * Method: onComboSelect
     *
     */
    onComboSelect: function(cb, r) {
        var fields = this.getFields();
        fields.x.setValue("");
        fields.x.label.update(r.get("labels")[0]+this.sep);
        fields.y.setValue("");
        fields.y.label.update(r.get("labels")[1]+this.sep);
    },

    /**
     * Method: createTabpanelComponent
     *
     */
    createTabpanelComponent: function() {
        return {
            xtype: "form",
            border: false,
            title: OpenLayers.i18n("goto.title"),
            defaults: {
                border: false
            },
            items: [{
                layout: "form",
                labelSeparator: this.sep,
                //labelAlign: "right",
                labelWidth: 150,
                items: this.combo
            }, {
                layout: "column",
                bodyStyle: "padding-top: 5px",
                defaults: {
                    columnWidth: 0.5,
                    border: false,
                    layout: "form",
                    labelSeparator: this.sep,
                    labelAlign: "right",
                    labelWidth: 60
                },
                items: [{
                    items: {
                        xtype: "numberfield",
                        anchor: "-1em",
                        name: "x",
                        fieldLabel: this.defaultSRS.get("labels")[0],
                        enableKeyEvents: true,
                        listeners: {
                            "keypress": this.onKeyPressed,
                            scope: this
                        }
                    }
                }, {
                    items: {
                        xtype: "numberfield",
                        anchor: "-1em",
                        name: "y",
                        fieldLabel: this.defaultSRS.get("labels")[1],
                        enableKeyEvents: true,
                        listeners: {
                            "keypress": this.onKeyPressed,
                            scope: this
                        }
                    }
                }]
            }]
        };
    },

    /**
     * Method: onKeyPressed
     *
     */
    onKeyPressed: function(f, e) {
        if (e.getKey() === e.ENTER) {
            this.layer.destroyFeatures();
            var fields = this.getFields();
            if (fields.x.getValue() && fields.y.getValue()) {
                var ll = new OpenLayers.LonLat(fields.x.getValue(),
                    fields.y.getValue());
                ll.transform(new OpenLayers.Projection(this.combo.getValue()),
                    this.map.getProjection());
                if (this.options.zoomLevel) {
                    this.map.setCenter(ll, this.options.zoomLevel);
                } else {
                    this.map.setCenter(ll);
                }
                this.layer.addFeatures([
                    new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Point(
                            ll.lon, ll.lat
                        )
                    )
                ]);
            }
        }
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.layer.destroyFeatures();
        this.map.removeLayer(this.layer);
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
