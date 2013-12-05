Ext.namespace("GEOR.Addons");

GEOR.Addons.Cadastre = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.Cadastre.BaseComboConfig = {
    forceSelection: false,
    width: 190,
    itemSelector: '.x-combo-list-item'
};
GEOR.Addons.Cadastre.BaseFormConfig = {
    labelWidth: 80,
    labelSeparator: OpenLayers.i18n("labelSeparator"),
    bodyStyle: 'padding: 10px',
    height: 110
};

GEOR.Addons.Cadastre.prototype = {
    item: null,
    stores: {},
    layer: null,
    win: null,
    jsonFormat: null,
    geojsonFormat: null,
    fields: null,
    cbx: null,
    fieldNames: [],

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        var lang = OpenLayers.Lang.getCode();
        Ext.iterate(this.options.tab1, function(k, v) {
            this.fieldNames.push(k);
        }, this);
        // to garantee that fields will always be in the same order:
        this.fieldNames.sort();
        this.jsonFormat = new OpenLayers.Format.JSON();
        this.geojsonFormat = new OpenLayers.Format.GeoJSON();
        this.layer = new OpenLayers.Layer.Vector("addon_cadastre_vectors", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": {
                    graphicName: "cross",
                    pointRadius: 16,
                    strokeColor: "fuchsia",
                    strokeWidth: 2,
                    fillOpacity: 0
                }
            })
        });
        var o = this.options.tab1,
            fields;
        Ext.each(this.fieldNames, function(field) {
            var c = o[field];
            fields = [{
                name: 'id'
            }, {
                name: c.valuefield,
                mapping: 'properties.' + c.valuefield
            }, {
                name: c.displayfield,
                mapping: 'properties.' + c.displayfield
            }, {
                name: 'bbox',
                mapping: 'properties.bbox'
            }];
            // compute which additional properties should be fetched & stored
            // if a custom template is provided:
            if (c.template) {
                var re = /{(.+?)}/g, r;
                while ((r = re.exec(c.template)) !== null) {
                    fields.push({
                        name: r[1],
                        mapping: 'properties.' + r[1]
                    });
                }
            }
            // TODO: use geoext's protocolproxy for tab1 stores
            this.stores[field] = new Ext.data.JsonStore({
                fields: fields
            });
        }, this);
        // return menu item:
        this.item = new Ext.menu.Item({
            text: record.get("title")[lang] || record.get("title")["en"],
            qtip: record.get("description")[lang] || record.get("description")["en"],
            iconCls: 'cadastre-icon',
            handler: this.showWindow,
            scope: this
        });
        return this.item;
    },


    getTabs: function() {
        var tab1, tab2, 
            roles = this.options.roles;

        if (roles) {
            if (!roles.tab1 || roles.tab1.length === 0) {
                tab1 = true;
            } else {
                for (var i = 0; i < roles.tab1.length; i++) {
                    if (GEOR.config.ROLES.indexOf(roles.tab1[i]) >= 0) {
                        tab1 = true;
                        break;
                    }
                }
            }
            if (!roles.tab2 || roles.tab2.length === 0) {
                tab2 = true;
            } else {
                for (var i = 0; i < roles.tab2.length; i++) {
                    if (GEOR.config.ROLES.indexOf(roles.tab2[i]) >= 0) {
                        tab2 = true;
                        break;
                    }
                }
            }
        } else {
            tab1 = true;
            tab2 = true;
        }

        var out = [];
        if (tab1 == true) {
            out.push(this.createTab1Form());
        }
        if (tab2 == true) {
            out.push(this.createTab2Form());
        }
        if (out.length == 0) {
            alert("Cadastre addon config error: no tab is allowed for current user with roles "+
                GEOR.config.ROLES.join(' - ')
            );
        }
        return out;
    },


    showWindow: function() {
        if (!this.win) {
            this.cbx = new Ext.form.Checkbox({
                checked: true,
                boxLabel: OpenLayers.i18n("sync map extent")
            });
            this.win = new Ext.Window({
                closable: true,
                closeAction: 'hide',
                width: 330,
                title: OpenLayers.i18n("addon_cadastre_popup_title"),
                border: false,
                buttonAlign: 'left',
                layout: 'fit',
                items: [{
                    xtype: 'tabpanel',
                    activeTab: 0,
                    items: this.getTabs()
                }],
                fbar: [this.cbx, '->', {
                    text: OpenLayers.i18n("Close"),
                    handler: function() {
                        this.win.hide();
                    },
                    scope: this
                }],
                listeners: {
                    "hide": function() {
                        this.map.removeLayer(this.layer);
                    },
                    scope: this
                }
            });
            this.loadStore(this.fieldNames[0]);
        }
        this.map.addLayer(this.layer);
        this.win.show();
    },


    issuePOST: function(options) {
        GEOR.waiter.show();
        OpenLayers.Request.POST(Ext.apply({
            headers: {
                // for dev purposes:
                //"Authorization": "Basic XXX_token_XXX"
            }
        }, options));
    },


    loadStore: function(fieldName) {
        var n = this.options.tab1[fieldName],
            fieldNameIdx = this.fieldNames.indexOf(fieldName),
            filter = '';
        if (n.hasOwnProperty("matchingproperties")) {
            var filters = [];
            Ext.iterate(n.matchingproperties, function(name, matchingproperty) {
                filters.push([
                    '<ogc:PropertyIsEqualTo>',
                        '<ogc:PropertyName>', matchingproperty, '</ogc:PropertyName>',
                        '<ogc:Literal>', this.fields[this.fieldNames.indexOf(name)].getValue(), '</ogc:Literal>',
                    '</ogc:PropertyIsEqualTo>',
                ].join(''));
            }, this);
            if (filters.length == 1) {
                filter = filters[0];
            } else if (filters.length > 1) {
                filter = '<ogc:And>'+filters.join('')+'</ogc:And>';
            }
            filter = '<ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">' + filter + '</ogc:Filter>';
        }
        if (this.fields) {
            // hack to mimic a "remote" mode combobox loading:
            this.fields[fieldNameIdx].expand();
            this.fields[fieldNameIdx].onBeforeLoad();
        }
        if (n.hasOwnProperty("file")) {
            OpenLayers.Request.GET({
                url: GEOR.config.PATHNAME + "/app/addons/cadastre/"+n.file,
                success: function(resp) {
                    if (resp && resp.responseText) {
                        var o = this.jsonFormat.read(resp.responseText);
                        this.stores[fieldName].loadData(o.features);
                    }
                },
                scope: this
            });
        } else {
            var properties = '';
            this.stores[fieldName].fields.each(function(field){
                if (field.name === 'bbox' || field.name === 'id') return;
                properties += '<ogc:PropertyName>' + field.name + '</ogc:PropertyName>';
            });
            this.issuePOST({
                url: n.wfs,
                data: [
                    '<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" version="1.1.0" service="WFS" outputFormat="json">',
                        '<wfs:Query typeName="', n.typename, '" srsName="', this.map.getProjection(), '">',
                            properties,
                            '<ogc:SortBy>',
                                '<ogc:SortProperty>',
                                    '<ogc:PropertyName>', n.displayfield, '</ogc:PropertyName>',
                                    '<ogc:SortOrder>ASC</ogc:SortOrder>',
                                '</ogc:SortProperty>',
                            '</ogc:SortBy>',
                            filter,
                        '</wfs:Query>',
                    '</wfs:GetFeature>'
                ].join(''),
                success: function(resp) {
                    if (resp && resp.responseText) {
                        var o = this.jsonFormat.read(resp.responseText);
                        this.stores[fieldName].loadData(o.features);
                    }
                },
                scope: this
            });
        }
    },


    getGeometry: function(record, fieldConfig, cb, scope) {
        scope = scope || this;
        var geom, box, f = record.get('feature');
        if (f && f.geometry) {
            geom = f.geometry;
        } else if (f && f.bounds) {
            box = f.bounds;
        } else if (record.get('bbox')) {
            box = OpenLayers.Bounds.fromArray(record.get('bbox'));
        } else if (fieldConfig.geometry) {
            // additional XHR to fetch only the geometry
            this.issuePOST({
                url: fieldConfig.wfs,
                data: [
                    '<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" version="1.1.0" service="WFS" outputFormat="json">',
                        '<wfs:Query typeName="', fieldConfig.typename, '" srsName="', this.map.getProjection(), '">',
                            '<ogc:PropertyName>' + fieldConfig.geometry + '</ogc:PropertyName>',
                            '<ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">',
                                '<ogc:FeatureId fid="', record.get('id'), '"/>',
                            '</ogc:Filter>',
                        '</wfs:Query>',
                    '</wfs:GetFeature>'
                ].join(''),
                success: function(resp) {
                    if (resp && resp.responseText) {
                        var features = this.geojsonFormat.read(resp.responseText),
                            f = features[0];
                        if (f) {
                            cb.call(scope, f.geometry, f.bounds);
                        }
                    }
                },
                scope: this
            });
        } else {
            // no zoom
            return;
        }

        if (!geom && box) {
            if (box.left == box.right && box.bottom == box.top) {
                geom = new OpenLayers.Geometry.Point(box.left, box.bottom);
            } else {
                geom = box.toGeometry();
            }
            cb.call(scope, geom, box);
        }
    },


    zoomToGeometry: function(geometry, box, forceZoom) {
        forceZoom = forceZoom || false;
        box = box || geometry.getBounds();
        if (this.cbx.getValue() === true || forceZoom) {
            this.map.zoomToExtent(box);
        }
        this.layer.destroyFeatures();
        this.layer.addFeatures([
            new OpenLayers.Feature.Vector(geometry)
        ]);
    },


    filterNextField: function(combo, record) {
        var currentField = combo.name,
            nextFieldIdx = this.fieldNames.indexOf(currentField) + 1,
            nextFieldName = this.fieldNames[nextFieldIdx],
            nextField = this.fields[nextFieldIdx],
            field;

        this.getGeometry(record, this.options.tab1[currentField], function(geom, box) {
            this.zoomToGeometry(geom, box, !nextFieldName);
        });

        if (!nextFieldName) {
            return;
        }
        // reset value && enable field N+1
        nextField.reset();
        nextField.enable();
        nextField.focus('', 50);
        // load store for field N+1
        nextField.on('focus', function() {
            // we have to wait that the combo has focus 
            // so that its innerList is created
            this.loadStore(nextFieldName);
        }, this, {single: true});
        // reset & disable all other fields
        for (var i = nextFieldIdx + 1, l = this.fields.length; i < l; i++) {
            field = this.fields[i];
            field.reset();
            field.disable();
        }
    },


    createTab1Form: function() {
        var fields = [],
            o = this.options.tab1;
        Ext.each(this.fieldNames, function(field) {
            var c = o[field];
            fields.push(
                new Ext.form.ComboBox(Ext.apply({
                    name: field,
                    fieldLabel: OpenLayers.i18n("tab1"+field+"label"),
                    store: this.stores[field],
                    valueField: c.valuefield,
                    displayField: c.displayfield,
                    tpl: new Ext.XTemplate(
                        '<tpl for=".">',
                            '<div class="x-combo-list-item">' + (c.template || '{'+c.displayfield+'}') + '</div>',
                        '</tpl>'
                    ),
                    editable: this.options.editableCombos,
                    disabled: field != this.fieldNames[0],
                    mode: 'local',
                    listeners: {
                        "select": this.filterNextField,
                        scope: this
                    }
                }, GEOR.Addons.Cadastre.BaseComboConfig))
            );
        }, this);
        this.fields = fields;
        return new Ext.FormPanel(Ext.apply({
            title: OpenLayers.i18n("tab1title"),
            items: fields,
            listeners: {
                "afterrender": function(form) {
                    this.fields[0].focus('', 50);
                },
                scope: this
            }
        }, GEOR.Addons.Cadastre.BaseFormConfig));
    },


    createTab2Form: function() {
        var fields = [], protocol, store,
            // field1 from tab1 here is not a bug, it's a feature:
            c = this.options.tab1.field1;

        fields.push(new Ext.form.ComboBox(Ext.apply({
            name: "tab2field1",
            fieldLabel: OpenLayers.i18n("tab2field1label"),
            store: this.stores[this.fieldNames[0]],
            valueField: c.valuefield,
            displayField: c.displayfield,
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="x-combo-list-item">' + (c.template || '{'+c.displayfield+'}') + '</div>',
                '</tpl>'
            ),
            editable: this.options.editableCombos,
            disabled: false,
            mode: 'local',
            listeners: {
                "select": function(cb, r) {
                    var f = fields[1];
                    f.enable(); 
                    f.reset();
                    f.focus('', 50);
                    this.getGeometry(r, c, function(geom, box) {
                        this.zoomToGeometry(geom, box);
                    });
                },
                scope: this
            }
        }, GEOR.Addons.Cadastre.BaseComboConfig)));

        c = this.options.tab2.field2;

        protocol = GEOR.ows.WFSProtocol({
            typeName: c.typename,
            owsURL: c.wfs
        }, this.map);

        store = new GeoExt.data.FeatureStore({
            // template is not supported for tab2 field2, contrary to tab1 fields:
            fields: [c.displayfield, c.valuefield, "bbox"], 
            // it's OK to client-side sort, since the store count is not high
            sortInfo: {
                field: c.displayfield,
                direction: 'ASC'
            },
            proxy: new GeoExt.data.ProtocolProxy({
                protocol: protocol
            }),
            listeners: {
                "beforeload": function(store, options) {
                    // add a filter to the options passed to proxy.load, 
                    // proxy.load passes these options to protocol.read
                    var params = store.baseParams;
                    options.filter = new OpenLayers.Filter.Logical({
                        type: "&&",
                        filters: [
                            new OpenLayers.Filter.Comparison({
                                type: OpenLayers.Filter.Comparison.LIKE,
                                property: c.valuefield,
                                // we need to replace accentuated chars by their unaccentuated version
                                // and toUpperCase is required, since all the DBF data is UPPERCASED
                                value: GEOR.util.stringDeaccentuate(params['query']).toUpperCase() + '*'
                            }),
                            new OpenLayers.Filter.Comparison({
                                type: "==",
                                // only one matching property is supported in here:
                                property: c.matchingproperties.field1,
                                value: fields[0].getValue()
                            })
                        ]
                    });
                    options.propertyNames = [c.valuefield, c.displayfield]; 
                    options.headers = {
                        // for dev purposes:
                        //"Authorization": "Basic XXX_token_XXX"
                    };
                    // remove the queryParam from the store's base
                    // params not to pollute the query string:                        
                    delete params['query'];
                },
                scope: this
            }
        });

        fields.push(new Ext.form.ComboBox(Ext.apply({
            name: "tab2field2",
            loadingText: OpenLayers.i18n("Loading..."),
            minChars: 3,
            queryDelay: 100,
            hideTrigger: true,
            queryParam: 'query', // do not modify
            pageSize: 0,
            fieldLabel: OpenLayers.i18n("tab2field2label"),
            store: store,
            valueField: c.valuefield,
            displayField: c.displayfield,
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="x-combo-list-item" ext:qtip="{'+c.displayfield+'}">' + (c.template || '{'+c.displayfield+'}') + '</div>',
                '</tpl>'
            ),
            editable: true,
            disabled: true,
            mode: 'remote',
            listeners: {
                "select": function(cb, r) {
                    this.getGeometry(r, c, function(geom, box) {
                        this.zoomToGeometry(geom, box, true);
                    });
                },
                scope: this
            }
        }, GEOR.Addons.Cadastre.BaseComboConfig)));

        return new Ext.FormPanel(Ext.apply({
            title: OpenLayers.i18n("tab2title"),
            items: fields,
            listeners: {
                "afterrender": function(form) {
                    fields[0].focus('', 50);
                },
                scope: this
            }
        }, GEOR.Addons.Cadastre.BaseFormConfig));
    },


    destroy: function() {
        this.win.hide();
        this.layer = null;
        this.map = null;
    }
};