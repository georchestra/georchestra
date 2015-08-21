Ext.namespace("GEOR.Addons");

/*
 * TODO :
 * simple / advanced tab
 * translations to spanish and german
 */
GEOR.Addons.Osm2Geor = Ext.extend(GEOR.Addons.Base, {
    win: null,
    layer: null,
    item: null,
    _queryTextArea: null,
    _styleTextArea: null,
    _keepPreviousFeatures: null,

    /**
     * Method: init
     *
     */
    init: function(record) {
        this.layer = new OpenLayers.Layer.Vector(this.tr("OSM data"), {
            displayInLayerSwitcher: true
        });
        this.item = new Ext.menu.Item({
            text: this.tr('OSM to geOrchestra'),
            qtip: this.tr('This addon allows you to display OSM data in your map'),
            iconCls: 'osm2geor-icon',
            handler: this.showWindow,
            scope: this
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
     * Method: createWindow
     *
     */
    createWindow: function() {
        this._queryTextArea = new Ext.form.TextArea({
            name: 'overpassApiQuery',
            width: 375,
            height: 150,
            fieldLabel: this.tr('Query'),
            value: '[timeout:25];(        \n \
    node["highway"]{{BBOX}};      \n \
    way["highway"]{{BBOX}};       \n \
);                              \n \
out body;                       \n \
>;                              \n \
out skel qt;'
        });
        this._styleTextArea = new Ext.form.TextArea({
            name: 'olStyle',
            fieldLabel: this.tr('Style'),
            value: '{"strokeWidth": 2, "strokeColor": "#ddd","fillColor": "#ddd"}',
            width: 375,
            height: 150
        });
        this._keepPreviousFeatures = new Ext.form.Checkbox({
           boxLabel: this.tr('Keep previously loaded features'),
           checked: false
        });

        return new Ext.Window({
            closable: true,
            closeAction: 'hide',
            width: 500,
            height: 400,
            title: this.tr("OSM to geOrchestra"),
            border: false,
            buttonAlign: 'left',
            layout: 'fit',
            items: {
                layout: 'form',
                labelWidth: 80,
                labelSeparator: this.tr("labelSeparator"),
                bodyStyle: 'padding: 5px;',
                items: [
                    this._queryTextArea,
                    this._styleTextArea
                ]
            },
            tools: [{
                id: 'help',
                handler: function() {
                    window.open(this.tr("Osm2Geor_HELP_URL"));
                },
                scope: this
            }, {
                id: 'close',
                handler: function() {
                    this.win.hide();
                },
                scope: this
            }],
            listeners: {
                'show': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
                        this.map.addLayer(this.layer);
                    }
                },
                scope: this
            },
            fbar: [this._keepPreviousFeatures, '->', {
                text: this.tr("Execute"),
                handler: this.queryFeatures,
                scope: this
            }]
        });
    },

    /**
     * Method: queryFeatures
     *
     */
    queryFeatures: function() {
        var ex = this.map.getExtent().transform(
            this.map.getProjectionObject(), 
            new OpenLayers.Projection("EPSG:4326")
        ), query = this._queryTextArea.getValue();
        Ext.Ajax.request({
            scope: this,
            url: GEOR.Addons.Osm2Geor.API_URL,
            method: 'POST',
            xmlData: query.replace(/{{BBOX}}/g, 
                '(' + ex.bottom +',' +ex.left + ',' + ex.top +',' + ex.right +')'
            ),
            success: function(response) {
                if (this._styleTextArea.getValue() != "") {
                    var jsStyle = new OpenLayers.Format.JSON().read(this._styleTextArea.getValue());
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
                // TODO
            }
        });
    },

    /**
     * Method: showWindow
     *
     */
    showWindow: function() {
        if (!this.win) {
            this.win = this.createWindow();
        }
        this.win.show();
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.win && this.win.hide();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});

GEOR.Addons.Osm2Geor.API_URL = 'http://overpass-api.de/api/interpreter';