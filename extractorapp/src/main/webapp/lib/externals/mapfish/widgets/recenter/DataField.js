/* 
 * Copyright (C) 2007-2009  Camptocamp 
 * 
 * This file is part of MapFish Client 
 * 
 * MapFish Client is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * MapFish Client is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with MapFish Client.  If not, see <http://www.gnu.org/licenses/>. 
 */ 
 
/** 
 * @requires widgets/recenter/Base.js
 * @include core/Protocol/MapFish.js
 * @include GeoExt/data/FeatureReader.js
 * @include GeoExt/data/ProtocolProxy.js
 */ 
 
Ext.namespace('mapfish.widgets', 'mapfish.widgets.recenter'); 
 
/** 
 * Class: mapfish.widgets.recenter.DataField 
 * Display a list of elements corresponding to the user input and recenters
 * on the selected element. 
 * 
 * Inherits from: 
 * - {<mapfish.widgets.recenter.Base>} 
 */ 
 
/** 
 * Constructor: mapfish.widgets.recenter.DataField
 * 
 * Parameters: 
 * config - {Object} The config object 
 * Returns: 
 * {<mapfish.widgets.recenter.DataField>} 
 */ 
mapfish.widgets.recenter.DataField = function(config) { 
    Ext.apply(this, config);
    if (!this.protocol && this.url) {
        this.protocol = new mapfish.Protocol.MapFish({
            url: this.url,
            params: {
                no_geom: true,
                limit: this.limit
            }
        });
    }
    mapfish.widgets.recenter.DataField.superclass.constructor.call(this); 
}; 
 
Ext.extend(mapfish.widgets.recenter.DataField, mapfish.widgets.recenter.Base, { 

    /**
     * APIProperty: protocol
     * {<OpenLayers.Protocol>} A protocol instance, if not set in the config
     *     passed to the constructor, and if the url option is set, a MapFish
     *     protocol is created using the url option.
     */
    protocol: null,

    /**
     * APIProprerty: url
     * {String} A URL string, used to configure the MapFish protocol this
     *     widget's constructor creates if no protocol is set in the
     *     config object.
     */
    url: null,

    /**
     * APIProperty: limit
     * {Number} An integer specifying the maximum number of features the
     *     MapFish web service must return, only applies if the protocol
     *     option is not set and if the url option is set, defaults to
     *     10.
     */
    limit: 10,

    /**
     * APIProperty: fieldLabel
     * {String} The label used in front of the combo box, defaults to
     *     null.
     */
    fieldLabel: null,

    /**
     * APIProperty: displayField
     * {String} The name of the field whose values are displayed in
     *     the combo box, mandatory.
     */
    displayField: null,

    /**
     * APIProperty: queryParam
     * {String} The name of the field used as the filter property.
     */
    queryParam: null,

    /**
     * Method: addItems
     * Overrides super-class addItems method. Builds the form inputs.
     */
    addItems: function() {
        this.removeAll();

        var store = new Ext.data.Store({
            reader: new GeoExt.data.FeatureReader({}, [
                {name: this.displayField}
            ]),
            proxy: new GeoExt.data.ProtocolProxy({
                protocol: this.protocol
            })
        });

        var combo = new Ext.form.ComboBox({
            fieldLabel: this.fieldLabel,
            name: this.displayField,
            mode: 'remote',
            minChars: 2,
            typeAhead: true,
            forceSelection: true,
            hideTrigger: true,
            displayField: this.displayField,
            emptyText: OpenLayers.i18n('mf.recenter.emptyText'),
            queryParam: this.queryParam || this.displayField,
            store: store,
            listeners: {
                select : function(combo, record, index) {
                    this.onComboSelect(record);
                },
                specialkey: function(combo, event) {
                    if (event.getKey() == event.ENTER) {
                        this.onComboSelect(record);
                    }
                },
                scope: this
            }
        });

        this.add(combo);

        // add a filter to the options passed to proxy.load, proxy.load
        // itself passes these options to protocol.read
        store.on({
            beforeload: function(store, options) {
                options.filter = new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.LIKE,
                    property: combo.queryParam,
                    value: store.baseParams[combo.queryParam]
                });
                // remove the queryParam from the store's base
                // params not to pollute the query string
                delete store.baseParams[combo.queryParam];
            },
            scope: this
        });
    },

    /**
     * Method: onComboSelect
     * Triggered when an item is selected in the list of answers.
     *
     * Parameters:
     * record - {<GeoExt.data.FeatureRecord>}
     */
    onComboSelect: function(record) {
        var feature = record.get("feature");
        if (feature.geometry instanceof OpenLayers.Geometry.Point &&
            this.defaultZoom) {
            this.recenterOnCoords(feature.geometry.x, feature.geometry.y);
        } else {
            // Format.GeoJSON sets the bbox value read from the feature
            // object in the feature instance, Format.GML sets it in
            // the geometry; yeah it sucks
            var bounds = feature.bounds || feature.geometry.getBounds();
            this.recenterOnBbox(bounds);
        }
    }
}); 
 
Ext.reg('datafieldrecenter', mapfish.widgets.recenter.DataField);
