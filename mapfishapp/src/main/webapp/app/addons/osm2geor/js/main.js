Ext.namespace("GEOR.Addons");

/*
 * TODO :
 * simple / advanced tab
 */
GEOR.Addons.Osm2Geor = Ext.extend(GEOR.Addons.Base, {
    win: null,
    layer: null,
    item: null,
    _queryTextArea: null,
    _styleTextArea: null,
    _keepPreviousFeatures: null,
    _jsonFormat: null,
    _title: null,

    /**
     * Method: init
     *
     */
    init: function(record) {
        this.layer = this.createLayer();
        this._jsonFormat = new OpenLayers.Format.JSON();
        this._title = this.getText(record);
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
                text: this._title,
                qtip: this.getQtip(record),
                iconCls: 'osm2geor-icon',
                handler: this.showWindow,
                scope: this
            });
        }
        this._queryComboStore = [];
        Ext.each(this.options.queries, function(q){
            this._queryComboStore.push({
                name: q.name[this.lang],
                query: q.query.replace(";", ";\n")
            });
        }, this);
        this._styleComboStore = [];
        Ext.each(this.options.styles, function(s){
            this._styleComboStore.push({
                name: s.name[this.lang],
                style: s.style
            });
        }, this);
    },

    /**
     * Method: createLayer
     *
     */
    createLayer: function() {
        return new OpenLayers.Layer.Vector(this.tr("osm2geor_layername"), {
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
            return this.tr("osm2geor_syntaxerror");
        }
    },

    /**
     * Method: createWindow
     *
     */
    createWindow: function() {
        this._queryCombo = new Ext.form.ComboBox({
            editable: false,
            triggerAction: 'all',
            height: 30,
            anchor: '95%',
            fieldLabel: this.tr('osm2geor_query'),
            loadingText: tr("Loading..."),
            mode: 'local',
            store: new Ext.data.JsonStore({
                data: this._queryComboStore,
                fields: ['name', 'query']
            }),
            value: this._queryComboStore[0].query,
            listeners: {
                "select": function(c, r) {
                    this._queryTextArea.setValue(r.get("query"));
                },
                scope: this
            },
            valueField: 'query',
            displayField: 'name',
            tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
        });
        this._queryTextArea = new Ext.form.TextArea({
            name: 'overpassApiQuery',
            height: 100,
            anchor: '95%',
            fieldLabel: "",
            value: this._queryComboStore[0].query
        });
        this._styleCombo = new Ext.form.ComboBox({
            editable: false,
            triggerAction: 'all',
            height: 30,
            anchor: '95%',
            fieldLabel: this.tr('osm2geor_style'),
            loadingText: tr("Loading..."),
            mode: 'local',
            store: new Ext.data.JsonStore({
                data: this._styleComboStore,
                fields: ['name', 'style']
            }),
            value: this._styleComboStore[0].style,
            listeners: {
                "select": function(c, r) {
                    this._styleTextArea.setValue(
                        this.prettify(r.get("style"))
                    );
                },
                scope: this
            },
            valueField: 'style',
            displayField: 'name',
            tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
        });
        this._styleTextArea = new Ext.form.TextArea({
            name: 'olStyle',
            fieldLabel: "",
            value: this.prettify(this._styleComboStore[0].style),
            height: 100,
            anchor: '95%',
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
           boxLabel: this.tr('osm2geor_keepfeatures'),
           checked: false
        });

        return new Ext.Window({
            closable: true,
            closeAction: 'hide',
            constrainHeader: true,
            width: 450,
            height: 350,
            minHeight: 350,
            maxHeight: 350,
            title: this._title,
            border: false,
            buttonAlign: 'left',
            layout: 'fit',
            items: {
                layout: 'form',
                labelWidth: 80,
                labelSeparator: this.tr("labelSeparator"),
                bodyStyle: 'padding: 5px;',
                items: [
                    this._queryCombo,
                    this._queryTextArea,
                    this._styleCombo,
                    this._styleTextArea
                ]
            },
            tools: [{
                id: 'help',
                handler: function() {
                    window.open(this.tr("osm2geor_help_url"));
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
                text: this.tr("osm2geor_run"),
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