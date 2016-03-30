Ext.namespace("GEOR.Addons");

GEOR.Addons.Notes = Ext.extend(GEOR.Addons.Base, {
    /**
     * Method: init
     */
    init: function(record) {
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: "button",
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: "notes-icon",
                listeners: {
                    "toggle": this.onToggle,
                    scope: this
                },
                scope: this
            });
            this.target.doLayout();
        } else {
            // addon places in "tools menus"
            this.item = new Ext.menu.CheckItem({
                xtype: 'button',
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "notes-icon",
                checked: false,
                listeners: {
                    "checkchange": this.onToggle,
                    scope: this
                }
            });
        }
    },
    /**
     * Method: onToggle
     *
     */
    onToggle: function(btn, pressed) {
        if (pressed) {
            this.map.events.register('click', this, this.addNote);
        } else {
            this.map.events.unregister('click', this, this.addNote);
        }
    },
    /**
     * Method: addNote
     *
     */
    addNote: function(evt) {
        var lonlat = this.map.getLonLatFromViewPortPx(evt.xy);
        lonlat.transform(this.map.projection,
            new OpenLayers.Projection("EPSG:4326"));
        this.window = new Ext.Window({
            title: this.tr("notes_title"),
            width: 420,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            items: [{
                xtype: 'form',
                width: 410,
                items: [
                    {
                        xtype: "textfield",
                        fieldLabel: this.tr("notes_email"),
                        width: 240,
                        name: "email",
                        allowBlank: false,
                        value: GEOR.config.USEREMAIL || ""
                    }, {
                        xtype: "textarea",
                        width: 240,
                        height: 120,
                        fieldLabel: this.tr("notes_comment"),
                        name: "comment",
                        allowBlank: false
                    }, {
                        xtype: "hidden",
                        name: "latitude",
                        value: lonlat.lat
                    }, {
                        xtype: "hidden",
                        name: "longitude",
                        value: lonlat.lon
                    }, {
                        xtype: "hidden",
                        name: "map_context",
                        value: GEOR.wmc.write()
                    }
                ],
                buttons: [{
                    text: this.tr("notes_submit"),
                    handler: function(b) {
                        if (b.findParentByType("form").getForm().isValid()) {
                            b.findParentByType("form").getForm().submit({
                                url: GEOR.config.PATHNAME +
                                    "/ws/note/backend/" + this.options.backend,
                                method: "POST",
                                success: function() {
                                    b.findParentByType("window").close();
                                },
                                failure: function(form, action) {
                                    GEOR.util.errorDialog({
                                        msg: this.tr("notes_cannotsave" + ":" + action.result.msg)
                                    });
                                }
                            });
                        }
                    },
                    scope: this
                }] // buttons
            }] // windows items
        });
        this.window.show();
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
        GEOR.Addons.Base.prototype.destroy.call(this);
    }


});
