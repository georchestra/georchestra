Ext.namespace("GEOR.Addons");

GEOR.Addons.Osm2Geor = Ext.extend(GEOR.Addons.Base, {
    win: null,
    layer: null,
    item: null,
    _queryTextArea: null,
    _styleTextArea: null,
    _keepPreviousFeatures: null,

    init: function(record) {
        this.layer = new OpenLayers.Layer.Vector("__georchestra_osm2geor", {
            displayInLayerSwitcher: true
        });
        this.item =  new Ext.menu.Item({
            text:    'OSM 2 geOrchestra',
            qtip:    'OSM to geOrchestra addon',
            iconCls: 'osm2geor-icon',
            handler: this.showWindow,
            scope:   this
        });
    },

    createWindow: function() {
        this._queryTextArea = new Ext.form.TextArea({
            name       : 'overpassApiQuery',
            width      : 375,
            height     : 150,
            fieldLabel : 'Overpass API query',
            value      : '[timeout:25];(        \n \
    node["highway"]{{BBOX}};      \n \
    way["highway"]{{BBOX}};       \n \
);                              \n \
out body;                       \n \
>;                              \n \
out skel qt;'
        });
        this._styleTextArea = new Ext.form.TextArea({
            name       : 'olStyle',
            fieldLabel : 'OpenLayers style',
            value      : '{"strokeWidth": 2, "strokeColor": "#ddd","fillColor": "#ddd"}',
            width      : 375,
            height     : 150
        });
        this._keepPreviousFeatures = new Ext.form.Checkbox({
           boxLabel   : 'Keep previously loaded features',
           checked    : false
        });

        return new Ext.Window({
            closable: true,
            closeAction: 'hide',
            width: 500,
            height: 400,
            title: "OSM 2 geOrchestra",
            border: false,
            buttonAlign: 'left',
            layout: 'form',
            items: [ this._queryTextArea, this._styleTextArea ],
            listeners: {
                'show': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
                        this.map.addLayer(this.layer);
                    }
                },
                scope: this
            },

            fbar: [this._keepPreviousFeatures, '->', {
                text: OpenLayers.i18n("Execute"),
                handler: function() {
                    var ex = this.map.getExtent().transform(
                        this.map.getProjectionObject(), 
                        new OpenLayers.Projection("EPSG:4326")
                    );
                    var query = this._queryTextArea.getValue();
                    query = query.replace(/{{BBOX}}/g, '(' + ex.bottom +',' +ex.left + ',' + ex.top +',' + ex.right +')');
                    Ext.Ajax.request({
                        scope: this,
                        url: 'http://overpass-api.de/api/interpreter',
                        method: 'POST',
                        xmlData: query,
                        success: function(response) {
                            if (this._styleTextArea.getValue() != "") {
                                var jsStyle = new OpenLayers.Format.JSON({}).read(this._styleTextArea.getValue());
                                var customStyle = new OpenLayers.Style(jsStyle);
                                this.layer.styleMap = new OpenLayers.StyleMap(customStyle);
                            } else {
                                this.layer.styleMap = new OpenLayers.StyleMap();
                            }
                            this.layer.redraw();
                            var features = (new OpenLayers.Format.OSM({
                                externalProjection: new OpenLayers.Projection("EPSG:4326"),
                                internalProjection: this.map.getProjectionObject()
                            })).read(response.responseText);
                            if (this._keepPreviousFeatures.checked == false) {
                                this.layer.removeAllFeatures();
                            }
                            this.layer.addFeatures(features);
                        },
                        failure: function() {
                            alert('failure');
                        }
                    });
                },
                scope:this
            },
            {
                text: OpenLayers.i18n("Close"),
                handler: function() {
                    this.win.hide();
                },
                scope: this
            }]
        });
    },

    showWindow: function() {
        if (!this.win) {
            this.win = this.createWindow();
        }
        this.win.show();
    },

    destroy: function() {
        this.win && this.win.hide();
        this.layer = null;
        this.jsonFormat = null;
        this.modifyControl = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});