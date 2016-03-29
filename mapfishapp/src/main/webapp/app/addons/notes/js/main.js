Ext.namespace("GEOR.Addons");

GEOR.Addons.Notes = Ext.extend(GEOR.Addons.Base, {
    /**
     * Method: init
     */
    init: function(record) {
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
    onToggle: function(btn, pressed) {
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
        lonlat = lonlat.transform(this.projection, new OpenLayers.Projection("EPSG:4326"));
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/note/backend/backend1",
            data: OpenLayers.Util.getParameterString({
                email: "info@abc.com",
                comment: "comment",
                map_context: "context_string",
                latitude: 45,
                longitude: -72
            }),
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
           success: function(response) {
               var o = Ext.decode(response.responseText);
           }
        });
    }
    

});
