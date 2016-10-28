Ext.namespace("GEOR.Addons");

GEOR.Addons.BANGeocoder = Ext.extend(GEOR.Addons.Base, {
    win: null,
    addressField: null,
    layer: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {

        // create layer : container of geometry to display adress selected
        this.layer = new OpenLayers.Layer.Vector("__georchestra_bangeocoder", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": {
                    graphicWidth: 20,
                    graphicHeight: 32,
                    graphicYOffset: -28, // shift graphic up 28 pixels
                    //Change the location of picture if not find
                    externalGraphic: GEOR.config.PATHNAME + 'app/css/images/pwrs/geoPin.png',
                }
            })
        });

        // create main window containing free text combo
        this.addressField = this._createCbSearch();
        this.win = new Ext.Window({
            title: OpenLayers.i18n('banGeocoder.window_title'),
            constrainHeader: true,
            width: 312,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: "bangeocoder",
            items: [this.addressField],
            listeners: {
                "hide": function() {
                    this.map.removeLayer(this.layer);
                    this.item && this.item.setChecked(false);
                    this.components && this.components.toggle(false);
                },
                "show": function() {
                    this.map.addLayer(this.layer);
                },
                scope: this
            }
        });

        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: 'addon-bangeocoder',
                handler: this.showWindow,
                scope: this
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-bangeocoder",
                handler: this.showWindow,
                scope: this
            });
        }
    },


    /*
     * Method: _createCbSearch
     * Returns: {Ext.form.ComboBox}
     * Create combo use to search free text input by user and display proposition (autocompletion)
     */
    _createCbSearch: function() {

        // get options from config.json file
        var banGeocoderOptions = this.options;

        // create store to pass free text and get result from service (URL)
        var store = new Ext.data.JsonStore({
            proxy: new Ext.data.HttpProxy({
                url: banGeocoderOptions.geocodeServiceUrl, // set service URL in manifest.json file, more informations in README
                method: 'GET',
                autoLoad: true
            }),
            storeId: 'geocodeStore',
            root: 'features',
            fields: [{
                name: 'typeGeometry',
                convert: function(v, rec) {
                    return rec.geometry.type;
                }
            }, {
                name: 'coordinates',
                convert: function(v, rec) {
                    return rec.geometry.coordinates;
                }
            }, {
                name: 'id',
                convert: function(v, rec) {
                    return rec.properties.id;
                }
            }, {
                name: 'label',
                convert: function(v, rec) {
                    return rec.properties.label;
                }
            }],
            totalProperty: 'limit',
            listeners: {
                "beforeload": function(q) {
                    store.baseParams.q = store.baseParams["query"];
                    store.baseParams.limit = banGeocoderOptions.limitResponse; // number of result is default set to 5, change it in config.json file, more informations in README
                    delete store.baseParams["query"];
                }
            }
        });

        // Create search combo
        return new Ext.form.ComboBox({
            emptyText: OpenLayers.i18n('banGeocoder.field_emptytext'),
            fieldLabel: OpenLayers.i18n('banGeocoder.field_label'),
            id: 'comboGeocoder',
            displayField: 'label',
            loadingText: OpenLayers.i18n('Loading...'),
            width: 300,
            store: store,
            hideTrigger: true,
            pageSize: 0,
            minChars: 5,
            listeners: {
                "select": this._onComboSelect,
                scope: this
            }
        });
    },


    /*
     * Method: _onComboSelect
     * Callback on combo selected to create and display address location from geometry
     * 
     */

    _onComboSelect: function(combo, record) {
        if (this.layer.features.length > 0) {
            this.layer.destroyFeatures();
        }

        var from = new OpenLayers.Projection("EPSG:4326"); // default GeoJSON SRS return by the service 
        var to = this.map.getProjectionObject();

        //get coordinates from GeoJson
        var fromCoordX = record.json.geometry.coordinates[0];
        var fromCoordY = record.json.geometry.coordinates[1];

        //create feature from GeoJson geometry

        // get geometry
        var geom = new OpenLayers.Geometry.Point(fromCoordX, fromCoordY).transform(from, to);

        // create point from geometry find in GeoJSON and create vector feature from point geometry
        var point = new OpenLayers.Geometry.Point(geom.x, geom.y);
        var feature = new OpenLayers.Feature.Vector(point);

        // add point feature to layer and zoom on    
        this.layer.addFeatures(feature);
        this.map.setCenter(new OpenLayers.LonLat(geom.x, geom.y), 10);
    },


    /**
     * Method: showWindow
     */
    showWindow: function() {
        this.win.show();
    },


    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.win.destroy();
        this.layer.destroyFeatures();

        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});