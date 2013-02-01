Ext.namespace("GEOR.Addons");

GEOR.Addons.CadastreFR = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.CadastreFR.prototype = {
    
    item: null,
    stores: {},
    win: null,
    jsonFormat: null,
    fields: null,
    cbx: null,
    fieldNames: ["field1", "field2", "field3"],

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        var lang = OpenLayers.Lang.getCode();
        this.jsonFormat = new OpenLayers.Format.JSON();
        var o = this.options.tab1;
        Ext.each(this.fieldNames, function(field) {
            var c = o[field];
            this.stores[field] = new Ext.data.JsonStore({
                fields: [{
                    name: c.valuefield,
                    mapping: 'properties.' + c.valuefield
                }, {
                    name: c.displayfield,
                    mapping: 'properties.' + c.displayfield
                }, {
                    name: 'bbox',
                    mapping: 'properties.bbox'
                }]
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
                width: 320,                    
                title: OpenLayers.i18n("addon_cadastre_popup_title"),
                border: false,
                buttonAlign: 'left',
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
                }]
            });
        }
        this.win.show();
    },


    loadStore: function(fieldName) {
        var n = this.options.tab1[fieldName],
            filter = '';
        if (n.hasOwnProperty("matchingproperties")) {
            var filters = [];
            Ext.iterate(n.matchingproperties, function(fieldName, matchingproperty) {
                filters.push([
                    '<ogc:PropertyIsEqualTo>',
                        '<ogc:PropertyName>', matchingproperty, '</ogc:PropertyName>',
                        '<ogc:Literal>', this.fields[this.fieldNames.indexOf(fieldName)].getValue(), '</ogc:Literal>',
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
        if (n.hasOwnProperty("file")) {
            OpenLayers.Request.GET({
                url: "app/addons/cadastrefr/"+n.file,
                //failure: , // TODO
                success: function(resp) {
                    if (resp && resp.responseText) {
                        var o = this.jsonFormat.read(resp.responseText);
                        this.stores[fieldName].loadData(o.features);
                    }
                },
                scope: this
            });
        } else {
            OpenLayers.Request.POST({
                url: n.wfs,
                data: [
                    '<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" version="1.1.0" service="WFS" outputFormat="json">',
                        '<wfs:Query typeName="', n.typename, '" srsName="', this.map.getProjection(), '">',
                            '<ogc:PropertyName>', n.valuefield, '</ogc:PropertyName>',
                            '<ogc:PropertyName>', n.displayfield, '</ogc:PropertyName>',
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
                //failure: , // TODO
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
            field;
        // zoom:
        if (this.cbx.getValue() === true || !nextFieldName) {
            var bbox = record.get('bbox');
            this.map.zoomToExtent(OpenLayers.Bounds.fromArray(bbox), true);
        }
        if (!nextFieldName) {
            return;
        }
        // load store for field N+1
        this.loadStore(nextFieldName);
        // reset value && enable field N+1
        nextField.reset();
        nextField.enable();
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
                    fieldLabel: OpenLayers.i18n("tab1"+field+"label"),
                    store: this.stores[field],
                    valueField: c.valuefield,
                    displayField: c.displayfield,
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
            labelWidth: 100,
            labelSeparator: OpenLayers.i18n("labelSeparator"),
            bodyStyle: 'padding: 10px',
            height: 100,
            items: fields
        });
        return form;
    },

    destroy: function() {        
        this.map = null;
    }
};