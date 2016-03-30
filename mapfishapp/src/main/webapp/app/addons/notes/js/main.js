Ext.namespace("GEOR.Addons");

GEOR.Addons.Notes = Ext.extend(GEOR.Addons.Base, {
    /**
     * Method: init
     */
    init: function (record) {
        if (false) {
            // addon placed in toolbar
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
    onToggle: function (btn, pressed) {
        if (pressed) {
            this.map.events.register('click', this.map, this.addNote);
        } else {
            //TODO
        }
    },
    /**
     * Method: addNote
     *
     */
    addNote: function (evt) {
        var lonlat = this.getLonLatFromViewPortPx(evt.xy);
        lonlat.transform(this.projection, new OpenLayers.Projection("EPSG:4326"));
        this.window = new Ext.Window({
            title: "Add a note",
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
                        fieldLabel: "Email",
                        width: 240,
                        name: "email",
                        allowBlank: false
                    }, {
                        xtype: "textarea",
                        width: 240,
                        height: 120,
                        fieldLabel: "Comment",
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
                        value: "context string"
                    }
                ],
                buttons: [{
                    text: "submit",
                    handler: function (b, e) {
                        if (b.findParentByType('form').getForm().isValid()) {
                            b.findParentByType('form').getForm().submit({
                                url: GEOR.config.PATHNAME + "/ws/note/backend/backend1",
                                method: "POST",
                                //TODO : Response is note well handled
                                success: function (response) {
                                    var o = Ext.decode(response.responseText);
                                    this.findParentByType('window').close();
                                },
                                failure: function (form, action) {
                                    GEOR.util.errorDialog({
                                        msg: "Cannot create note"
                                    })
                                }
                            });
                        }
                    },
                    scope: this
                }] // buttons
            }] // windows items
        });
        this.window.show();
    }


});
