Ext.namespace("GEOR.Addons");

GEOR.Addons.OpenLS = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.OpenLS.prototype = {
    win: null,
    addressField: null,
    layer: null,
    popup: null,
    _requestCount: 0,
    _format: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this._format = new OpenLayers.Format.GML({
            xy: this.options.xy
        });
        this.layer = new OpenLayers.Layer.Vector("addon_openls_vectors", {
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
        this.addressField = this._createCbSearch();
        this.win = new Ext.Window({
            title: OpenLayers.i18n('openls.window_title'),
            width: 440,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: "openls",
            items: [{
                xtype: "form",
                labelWidth: 60,
                bodyStyle: "padding:5px;",
                labelSeparator: OpenLayers.i18n("labelSeparator"),
                items: [this.addressField]
            }],
            listeners: {
                "hide": function() {
                    this.popup && this.popup.hide();
                    this.layer.destroyFeatures();
                    this.map.removeLayer(this.layer);
                },
                "show": function() {
                    this.map.addLayer(this.layer);
                },
                scope: this
            }
        });
        var lang = OpenLayers.Lang.getCode(),
            item = new Ext.menu.Item({
                text: record.get("title")[lang] || record.get("title")["en"],
                qtip: record.get("description")[lang] || record.get("description")["en"],
                iconCls: "addon-openls",
                handler: this.showWindow,
                scope: this
            });
        this.item = item;
        return item;
    },

    /**
     * Method: _readPosition
     * Extracts the gml:Point > gml:pos String from the incoming GeocodedAddress
     *
     * Parameters:
     * v - {String}
     * node - {XML} the XML data corresponding to one GeocodedAddress record
     *
     * Returns: {OpenLayers.Geometry.Point}
     */
    _readPosition: function(v, node) {
        var f = this._format;
        return f.parseGeometry.point.call(f, node);
    },

    /*
     * Method: _createCbSearch
     * Returns: {Ext.form.ComboBox}
     */
    _createCbSearch: function() {
        var fields = [
                //{name: 'geom', mapping: 'gml:Point > gml:pos'}, 
                // -> fails in ExtJS on line 26570 in XMLReader's createAccessor method:
                // Ext.DomQuery.selectValue(key, root); 
                // ... where root is the record node and key is the provided mapping.
                // As a result, we're using a custom convert method
                {
                    name: 'geometry', 
                    convert: this._readPosition.createDelegate({
                        _format: this._format
                    })
                },
            ].concat(this.options.GeocodedAddressFields),

        storeOptions = {
            // TODO: use GeoExt.data.ProtocolProxy instead
            proxy: new Ext.data.HttpProxy({
                url: this.options.serviceURL,
                method: "POST"
            }),
            // TODO: implement a GeoExt.data.XLSGeocodeResponseReader
            reader: new Ext.data.XmlReader({
                record: "Response > GeocodeResponse > GeocodeResponseList > GeocodedAddress"
            }, fields),
            listeners: {
                "beforeload": function(store, options) {
                    var params = store.baseParams;
                    this._requestCount += 1;
                    params.xmlData = [ // TODO: config set template string
                        '<?xml version="1.0" encoding="UTF-8"?>',
                        '<XLS',
                           ' xmlns:xls="http://www.opengis.net/xls"',
                           ' xmlns:gml="http://www.opengis.net/gml"',
                           ' xmlns="http://www.opengis.net/xls"',
                           ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
                           ' version="1.2"',
                           ' xsi:schemaLocation="http://www.opengis.net/xls http://schemas.opengis.net/ols/1.2/olsAll.xsd">',
                            '<RequestHeader/>',
                            '<Request requestID="', this._requestCount, '" version="1.2" methodName="LocationUtilityService">',
                               '<GeocodeRequest returnFreeForm="false">',
                                 '<Address countryCode="StreetAddress">', // specific to French GeoPortail Service
                                   '<freeFormAddress>', params['query'], '</freeFormAddress>',
                                 '</Address>',
                               '</GeocodeRequest>',
                            '</Request>',
                        '</XLS>'
                    ].join('');
                    // not to pollute the query string:                        
                    delete params['query'];
                },
                scope: this
            }
        };
        if (this.options.minAccuracy > 0) {
            storeOptions.listeners.load = function(store) {
                store.filterBy(function(record) {
                    return record.get('accuracy') > this.options.minAccuracy;
                }, this);
            };
        }
        if (this.options.sortField) {
            storeOptions.sortInfo =  {
                field: this.options.sortField,
                direction: "DESC"
            };
        }
        var store = new Ext.data.Store(storeOptions),

        tplResult = new Ext.XTemplate(
            '<tpl for="."><div class="x-combo-list-item">',
                this.options.comboTemplate,
            '</div></tpl>'
        );

        return new Ext.form.ComboBox({
            name: "address",
            width: 350,
            emptyText: OpenLayers.i18n('openls.field_emptytext'),
            fieldLabel: OpenLayers.i18n('openls.field_label'),
            store: store,
            loadingText: OpenLayers.i18n('Loading...'),
            queryDelay: 100,
            hideTrigger: true,
            tpl: tplResult,                      // template to display results
            queryParam: 'query',         // do not modify
            minChars: 3,                        // min characters number to
                                                 // trigger the search
            pageSize: 0,                         // removes paging toolbar
            listeners: {
                "select": this._onComboSelect,
                scope: this
            }
        });
    },

    /*
     * Method: _onComboSelect
     * Callback on combo selected
     */
    _onComboSelect: function(combo, record) {
        var bbox, geom, feature,
            from = new OpenLayers.Projection("EPSG:4326"),
            to = this.map.getProjectionObject();

        this.layer.destroyFeatures();
        if (!record.get("geometry")) {
            return;
        }
        geom = record.get("geometry").transform(from, to);
        feature = new OpenLayers.Feature.Vector(geom);
        this.layer.addFeatures([feature]);
        if (!this.popup) {
            this.popup = new GeoExt.Popup({
                location: feature,
                width: 200,
                html: new Ext.XTemplate(
                    '<div class="x-combo-list-item">',
                        this.options.comboTemplate,
                    '</div>'
                ).apply(record.data),
                anchorPosition: "top-left",
                collapsible: false,
                closable: false,
                unpinnable: false
            });
        } else {
            this.popup.body.update(
                new Ext.XTemplate(
                    '<div class="x-combo-list-item">',
                        this.options.comboTemplate,
                    '</div>'
                ).apply(record.data)
            );
            this.popup.location = geom.getBounds().getCenterLonLat();
        }
        this.popup.show();
        if (record.get("bbox")) {
            // we assume lbrt here, like "2.374215;48.829177;2.375391;48.829831"
            // note: this looks very specific to the French Geoportail OLS service
            bbox = OpenLayers.Bounds.fromArray(
                record.get("bbox").split(";")
            ).transform(from, to);
        } else {
            bbox = geom.getBounds();
        }
        this.map.zoomToExtent(bbox);
    },

    /**
     * Method: showWindow
     */
    showWindow: function() {
        this.win.show();
        this.win.alignTo(
            Ext.get(this.map.div),
            "t-t",
            [0, 5],
            true
        );
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.win.hide();
        this.popup.destroy();
        this.popup = null;
        this.layer = null;
        this.map = null;
    }
};