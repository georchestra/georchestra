Ext.namespace("GEOR.Addons");

GEOR.Addons.CadastreFR = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.CadastreFR.prototype = {
    item: null,
    stores: {},
    layer: null,
    win: null,
    jsonFormat: null,
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
        this.jsonFormat = new OpenLayers.Format.JSON();
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
            // TODO: use geoext's protocolproxy
            this.stores[field] = new Ext.data.JsonStore({
                fields: fields
            });
        }, this);
        this.loadStore(this.fieldNames[0]);
        // return menu item:
        this.item = new Ext.menu.Item({
            text: record.get("title")[lang],
            iconCls: 'cadastre-icon',
            qtip: record.get("description")[lang],
            handler: this.showWindow,
            scope: this
        });
        return this.item;
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
                    items: [this.createTab1Form(), {
                        title: OpenLayers.i18n("tab2title"),
                        height: 100,
                        html: "TODO"
                    }]
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
        }
        this.map.addLayer(this.layer);
        this.win.show();
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
                url: "app/addons/cadastrefr/"+n.file,
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
                if (field.name === 'bbox') return;
                properties += '<ogc:PropertyName>' + field.name + '</ogc:PropertyName>';
            });
            OpenLayers.Request.POST({
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

    filterNextField: function(combo, record) {
        var currentField = combo.name,
            nextFieldIdx = this.fieldNames.indexOf(currentField) + 1,
            nextFieldName = this.fieldNames[nextFieldIdx],
            nextField = this.fields[nextFieldIdx],
            field, geom,
            bbox = OpenLayers.Bounds.fromArray(record.get('bbox'));

        if (bbox.left == bbox.right && bbox.bottom == bbox.top) {
            geom = new OpenLayers.Geometry.Point(
                (bbox.left + bbox.right) / 2, 
                (bbox.bottom + bbox.top) / 2
            );
        } else {
            geom = bbox.toGeometry();
        }
        // zoom:
        if (this.cbx.getValue() === true || !nextFieldName) {
            this.map.zoomToExtent(bbox);
        }
        this.layer.destroyFeatures();
        this.layer.addFeatures([
            new OpenLayers.Feature.Vector(geom)
        ]);
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
                new Ext.form.ComboBox({
                    name: field,
                    width: 180,
                    fieldLabel: OpenLayers.i18n("tab1"+field+"label"),
                    store: this.stores[field],
                    valueField: c.valuefield,
                    itemSelector: '.x-combo-list-item',
                    displayField: c.displayfield,
                    tpl: new Ext.XTemplate(
                        '<tpl for=".">',
                            '<div class="x-combo-list-item">' + (c.template || '{'+c.displayfield+'}') + '</div>',
                        '</tpl>'
                    ),
                    editable: this.options.editableCombos,
                    disabled: field != this.fieldNames[0],
                    mode: 'local',
                    triggerAction: 'all',
                    listeners: {
                        "select": this.filterNextField,
                        scope: this
                    }
                })
            );
        }, this);
        this.fields = fields;
        var form = new Ext.FormPanel({
            title: OpenLayers.i18n("tab1title"),
            labelWidth: 80,
            labelSeparator: OpenLayers.i18n("labelSeparator"),
            bodyStyle: 'padding: 10px',
            height: 110,
            items: fields,
            listeners: {
                "afterrender": function(form) {
                    this.fields[0].focus('', 50);
                },
                scope: this
            }
        });
        return form;
    },

    destroy: function() {        
        this.map = null;
    }
};