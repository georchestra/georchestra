Ext.namespace("GEOR.Addons");

/*
 * TODO :
 * simple / advanced tab
 * query store (drop down list)
 */
GEOR.Addons.Osm2Geor = Ext.extend(GEOR.Addons.Base, {
    win: null,
    layer: null,
    item: null,
    _queryTextArea: null,
    _styleTextArea: null,
    _keepPreviousFeatures: null,
    _jsonFormat: null,

    /**
     * Method: init
     *
     */
    init: function(record) {
        this.layer = this.createLayer();
        this._jsonFormat = new OpenLayers.Format.JSON();
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: 'osm2geor-icon',
                handler: this.showWindow,
                scope: this
            });
            this.target.doLayout();
        } else {
            // addon placed in "tools menu"
            this.item = new Ext.menu.Item({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: 'osm2geor-icon',
                handler: this.showWindow,
                scope: this
            });
        }
    },

    /**
     * Method: createLayer
     *
     */
    createLayer: function() {
        return new OpenLayers.Layer.Vector(this.tr("OSM data"), {
            displayInLayerSwitcher: true,
            eventListeners: {
                "removed": function(){
                    this.win.hide();
                },
                scope: this
            }
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
     * Method: prettify
     *
     */
    prettify: function(a) {
        return this._jsonFormat.write(
            this._jsonFormat.read(a),
            true
        );
    },

    /**
     * Method: validator
     *
     */
    validator: function(v) {
        try {
            this.prettify(v);
            return true;
        } catch (e) {
            return "syntax error";
        }
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
            value: this.options.defaultQuery.replace(";", ";\n")
        });
        this._styleTextArea = new Ext.form.TextArea({
            name: 'olStyle',
            fieldLabel: this.tr('Style'),
            value: this.prettify(this.options.defaultStyle),
            width: 375,
            height: 150,
            validator: this.validator.createDelegate(this),
            listeners: {
                "blur": function(f) {
                    var s = f.getValue();
                    try {
                        s = this.prettify(s);
                        f.setValue(s);
                    } catch (e) {}
                },
                scope: this
            }
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
        ), 
        query = [
            '[timeout:25];(',
            this._queryTextArea.getValue(),
            '); out body; >; out skel qt;'
        ].join('');
        GEOR.waiter.show();
        Ext.Ajax.request({
            method: 'POST',
            url: this.options.API_URL,
            xmlData: query.replace(/{{BBOX}}/g, 
                '(' + ex.bottom +',' +ex.left + ',' + ex.top +',' + ex.right +')'
            ),
            success: function(response) {
                if (this._styleTextArea.getValue() != "") {
                    var s = this._jsonFormat.read(this._styleTextArea.getValue());
                    this.layer.styleMap = GEOR.util.getStyleMap({
                        "default": s,
                        "select": Ext.applyIf({
                            strokeWidth: 5,
                            strokeColor: "blue"
                        }, s)
                    });
                }
                this.layer.redraw();
                var features = (new OpenLayers.Format.OSM(Ext.applyIf({
                    externalProjection: new OpenLayers.Projection("EPSG:4326"),
                    internalProjection: this.map.getProjectionObject()
                }, this.options.formatOptions))).read(response.responseText);
                if (!this._keepPreviousFeatures.checked) {
                    this.layer.removeAllFeatures();
                }
                this.layer.addFeatures(features);
            },
            failure: function() {
                GEOR.util.errorDialog({
                    msg: tr("Unreachable server")
                });
            },
            scope: this
        });
    },

    /**
     * Method: showWindow
     *
     */
    showWindow: function() {
        if (!this.layer) {
            this.layer = this.createLayer();
        }
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
        this.layer.destroy();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});