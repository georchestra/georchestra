/*global
 Ext, GeoExt, OpenLayers, GEOR
 */

Ext.namespace("GEOR.Addons");

GEOR.Addons.Notes = Ext.extend(GEOR.Addons.Base, {

    toggleGroup: "notes",
    layer: null,
    icon: null,

    /**
     * Method: init
     */
    init: function(record) {
        this.icon = GEOR.config.PATHNAME + "/ws/addons/" + record.get("name").toLowerCase() + "/" + this.options.icon;

        this.layer = new OpenLayers.Layer.Vector("__georchestra_notes", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style(null, {
                    rules: [new OpenLayers.Rule({
                        symbolizer: {
                            "Point": {
                                "externalGraphic": this.icon,
                                "graphicWidth": 18,
                                "graphicHeight": 18,
                                "graphicXOffset": -9,
                                "graphicYOffset": -18
                            }
                        }
                    })]
                }),
                "temporary": new OpenLayers.Style(null, {
                    rules: [new OpenLayers.Rule({
                        symbolizer: {
                            "Point": {
                                "externalGraphic": this.icon,
                                "graphicWidth": 18,
                                "graphicHeight": 18,
                                "graphicXOffset": -9,
                                "graphicYOffset": -18
                            }
                        }
                    })]
                })
            })
        });

        if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
            this.map.addLayer(this.layer);
        }

        var action = new GeoExt.Action({
            text: this.getText(record),
            qtip: this.getQtip(record),
            map: this.map,
            //group is required for CheckItem
            group: "_notes",
            //toggleGroup is required for button
            toggleGroup: "notes",
            icon: this.icon,
            control: new OpenLayers.Control.DrawFeature(this.layer,
                OpenLayers.Handler.Point, {
                    eventListeners: {
                        "featureadded": this.addNote,
                        scope: this
                    }
                }
            )
        });

        // If there are many addons and some of them are in the toolbar and
        // others in the menu, we do not manage if more than 1 is activated.
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, action);
            this.target.doLayout();
        } else {
            // addon placed in menu
            this.items = new Ext.menu.CheckItem(action);
        }
    },

    /**
     * Method: addNote
     *
     */
    addNote: function(obj) {
        //Deactivate control. Adding many notes at the same time is not supported
        obj.object.deactivate();

        var geometry = obj.feature.geometry.clone();
        geometry.transform(this.map.projection,
            new OpenLayers.Projection("EPSG:4326"));

        var followUpHidden = new Ext.form.Hidden({
            // The checkbox named followup_checkbox will only be send if it is checked.
            // A listener on this checkbox will change the value of this hidden field based
            // on its value.
            xtype: "hidden",
            name: "followup",
            value: false
        });

        var form = new Ext.form.FormPanel({
            autoHeight: true,
            bodyStyle: "padding: 5px;",
            labelSeparator: tr("labelSeparator"),
            items: [{
                xtype: "textarea",
                width: 240,
                height: 100,
                fieldLabel: this.tr("notes_comment"),
                name: "comment",
                allowBlank: false
            }, {
                xtype: "checkbox",
                labelStyle: "width:160px",
                fieldLabel: this.tr("notes_follow_up"),
                name: "followup_checkbox",
                listeners: {
                    "change": {
                        fn: function(checkbox, newValue) {
                            followUpHidden.setRawValue(newValue);
                        }
                    }
                }
            }, {
                xtype: "textfield",
                fieldLabel: this.tr("notes_email"),
                width: 240,
                name: "email",
                value: GEOR.config.USEREMAIL || "",
                vtype: "email",
                validator: function(value) {
                    return (followUpHidden.getValue() === "false" || value !== "");
                }
            }, {
                xtype: "hidden",
                name: "latitude",
                value: geometry.y
            }, {
                xtype: "hidden",
                name: "longitude",
                value: geometry.x
            }, {
                xtype: "hidden",
                name: "map_context",
                value: GEOR.wmc.write()
            },
                followUpHidden],
            buttons: [{
                text: this.tr("notes_cancel"),
                handler: function() {
                    this.window.close();
                },
                scope: this
            }, {
                text: this.tr("notes_submit"),
                iconCls: 'btn-add',
                handler: function(b) {
                    if (b.findParentByType("form").getForm().isValid()) {
                        b.findParentByType("form").getForm().submit({
                            url: GEOR.config.PATHNAME + "/ws/note/backend/" + this.options.backend,
                            method: "POST",
                            success: function() {
                                this.window.close();
                                GEOR.helper.msg(this.tr("notes_title"), this.tr("notes_saved"));
                            },
                            failure: function(form, action) {
                                GEOR.util.errorDialog({
                                    msg: this.tr("notes_cannotsave" + ":" + action.result.msg)
                                });
                            },
                            scope: this
                        });
                    }
                },
                scope: this
            }]
        });

        this.window = new Ext.Window({
            title: this.tr("notes_title"),
            width: 400,
            closable: true,
            listeners: {
                "close": this.clearLayer,
                "show": function() {
                    var field = form.getForm().findField("comment");
                    field.focus('', 50);
                },
                scope: this
            },
            resizable: false,
            border: false,
            layout: "fit",
            items: form
        });
        this.window.show();
    },

    /**
     *  Method : clearLayer
     *
     */
    clearLayer: function() {
        this.layer.destroyFeatures();
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
     */
    destroy: function() {
        if (this.window) {
            this.window.destroy();
        }
        this.layer.destroy();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
