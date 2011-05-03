/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOB_ows.js
 * @include GEOB_util.js
 * @include GEOB_config.js
 * @include GEOB_waiter.js
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Filter/Comparison.js
 * @include GeoExt/data/FeatureStore.js
 * @include GeoExt/data/ProtocolProxy.js
 */
 
Ext.namespace("GEOB");

GEOB.referentials = (function() {

    /*
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;
    
    /*
     * Property: nsAlias
     * {String} the GeoServer namespace alias (aka prefix) with localization layers
     */
    var nsAlias = null;
    
    /*
     * Property: comboPanel
     * {Ext.Panel} the panel with the card layout
     * It can hold as many formPanels as reference layers
     */
    var comboPanel = null;
    
    /*
     * Property: cbPanels
     * {Array} array of formPanels used in card layout
     */
    var cbPanels = [];
    
    /*
     * Method: buildTemplate
     * Returns the template suitable for the currently selected layer
     *
     * Parameters:
     * items - {Array} array of attributes names string
     *
     * Returns:
     * {Ext.XTemplate} the template to be used in dataview
     */
    var buildTemplate = function(items) {
        items = items || [];
        var l = items.length;
        var s = new Array(l);
        for (var i=0;i<l;i++) {
            s[i]="<strong>"+
                "{[GEOB.util.stringUpperCase(values.feature.attributes."+
                items[i]+")]}"+
                "</strong>";
        }
        return new Ext.XTemplate(
            '<tpl for="."><div class="search-item">'+
            s.join(' - ')+'</div></tpl>'
        );
    };
    
    /*
     * Method: filterStringType
     * Extracts the string attributes names from an AttributeStore
     *
     * Parameters:
     * attStore - {GeoExt.data.AttributeStore}
     *
     * Returns:
     * {Array} array of string attributes names
     */
    var filterStringType = function(attStore) {
        var items = [];
        attStore.each(function(record) {
            var parts = record.get('type').split(':'); // eg: "xsd:string"
            var type = (parts.length == 1) ? parts[0] : parts[1];
            if (type == 'string') {
                items.push(record.get('name')); 
            }
        }, this);
        return items;
    };

    /*
     * Method: onLayerSelected
     * Callback executed on layer selected
     *
     */
    var onLayerSelected = function(combo, record, index) {
        var idx = record.get('name');

        // check if panel already exists
        if (!cbPanels[idx]) {
            GEOB.waiter.show();
            comboPanel.disable();
            
            var protocol = record.get('layer').protocol;
            
            var attStore = GEOB.ows.WFSDescribeFeatureType({
                owsURL: protocol.url,
                typeName: nsAlias + ':' + idx
            }, {
                success: function() {
                    // create new formPanel with search combo
                    var panel = new Ext.form.FormPanel({
                        items: [
                            createCbSearch(
                                record, 
                                filterStringType(attStore)
                            )
                        ]
                    });
                    // add new panel containing combo to card layout
                    comboPanel.add(panel);
                    // switch panels
                    comboPanel.layout.setActiveItem(panel);
                    comboPanel.enable();
                    cbPanels[idx] = panel;
                },
                failure: function() {
                    GEOB.waiter.hide();
                },
                scope: this
            });
        } else {
            comboPanel.layout.setActiveItem(cbPanels[idx]);
        }
    };
    
    /*
     * Method: createLayerCombo
     * Creates the layer combo from WFSCapabilities
     *
     * Returns:
     * {Ext.form.ComboBox} the comobobox
     */
    var createLayerCombo = function() {

        var store = GEOB.ows.WFSCapabilities({
            storeOptions: {
                url: GEOB.config.GEOSERVER_WFS_URL,
                protocolOptions: {
                    srsName: map.getProjection(),
                    // required so that we do not use the proxy if on same machine:
                    url: GEOB.config.GEOSERVER_WFS_URL
                }
            },
            vendorParams: {
                namespace: nsAlias
            }
        });
    
        return new Ext.form.ComboBox({
            fieldLabel: 'Couche',
            store: store,
            displayField: 'title',
            width: 160,
            listWidth: 160+18,
            triggerAction: "all",
            editable: false,
            listeners: {
                select: onLayerSelected,
                scope: this
            }
        });
    };

    /*
     * Method: onComboSelect
     * Callback executed on result selection: zoom to feature
     *
     * Parameters:
     * record - {Ext.data.Record}
     */
    var onComboSelect = function(record) {
        var feature = record.get('feature');
        if (!feature) {
            return;
        }
        if (feature.bounds) {
            var bounds = feature.bounds;
            if (bounds.getWidth() + bounds.getHeight() == 0) {
                map.setCenter(bounds.getCenterLonLat()); 
            } else {
                map.zoomToExtent(bounds.scale(1.05));
            }
        } else if (feature.geometry) {
            var geometry = feature.geometry;
            if (geometry.CLASS_NAME == 'OpenLayers.Geometry.Point') {
                map.setCenter(geometry.getBounds().getCenterLonLat()); 
            } else {
                map.zoomToExtent(geometry.getBounds().scale(1.05));
            }
        }
    };
    
    /*
     * Method: buildFilter
     * Builds the filter needed to perform the WFS query
     *
     * Parameters:
     * queryString - {String} the query string
     * stringAttributes - {Array} Array of string attributes
     *
     * Returns:
     * {OpenLayers.Filter} the filter corresponding to the input string
     * This filter's scope is all the string attributes in the selected layer
     */
    var buildFilter = function(queryString, stringAttributes) {
        var l = stringAttributes.length;
        // we need to replace accentuated chars by their unaccentuated version
        // and toUpperCase is required, since all the DBF data is UPPERCASED
        queryString = GEOB.util.stringDeaccentuate(queryString);
        var filterValue = '*' + queryString.toUpperCase() + '*';
            
        if (l == 1) {
            return new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: stringAttributes[0],
                value: filterValue
            });
        } else {
            var filters = new Array(l);
            for (var i=0;i<l;i++) {
                filters[i] = new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.LIKE,
                    property: stringAttributes[i],
                    value: filterValue
                });
            }
            return new OpenLayers.Filter.Logical({
                type: OpenLayers.Filter.Logical.OR,
                filters: filters
            });
        }
    };
    
    /*
     * Method: createCbSearch
     * Creates the search combo 
     *
     * Parameters:
     * record - {Ext.data.Record}
     * attributes - {Array} the array of string attributes names
     *
     * Returns:
     * {Ext.form.ComboBox} 
     */
    var createCbSearch = function(record, attributes) {
        var store, disabled = false;
        
        if (record && record.get('layer')) {
            store = new GeoExt.data.FeatureStore({
                proxy: new GeoExt.data.ProtocolProxy({
                    protocol: record.get('layer').protocol
                }),
                listeners: {
                    beforeload: function(store, options) {
                        // add a filter to the options passed to proxy.load, 
                        // proxy.load passes these options to protocol.read
                        var params = store.baseParams;
                        options.filter = buildFilter(params['query'], attributes);
                        
                        // We don't need the geometry, 
                        // just its bounds, and attributes:
                        options.propertyNames = attributes;
                        // This means that a spatial index is required !
                        
                        // remove the queryParam from the store's base
                        // params not to pollute the query string:                        
                        delete params['query'];
                    },
                    scope: this
                }
            });
        } else {
            // we need to create the first (fake) combo 
            // (which will never get used)
            // so we create useless store
            store = new GeoExt.data.FeatureStore();
            disabled = true;
        }

        var cb = new Ext.form.ComboBox({
            fieldLabel: 'Aller à',
            loadingText: 'Chargement...',
            name: 'nothing',
            mode: 'remote',
            minChars: 2,
            disabled: disabled,
            forceSelection: true,
            width: 160+18,
            queryDelay: 100,
            listWidth: 160+18,
            hideTrigger: true,
            queryParam: 'query', // do not modify
            tpl: buildTemplate(attributes),
            pageSize: 0,
            itemSelector: 'div.search-item',
            emptyText: disabled ? 'Choisissez une couche' : '',
            store: store,
            listeners: {
                select : function(combo, record, index) {
                    onComboSelect(record);
                },
                specialkey: function(combo, event) {
                    if (event.getKey() == event.ENTER) {
                        onComboSelect(record);
                    }
                },
                scope: this
            }
        });
        
        // hack in order to show the result dataview even
        // in case of "too many features" warning message
        store.on({
            load: function(){
                cb.focus();
                // this one is for IE, 
                // since it's not able to focus the element:
                cb.hasFocus = true;
                // focusing the element enables the expand()
                // method to proceed with success
            },
            scope: this
        });
        
        return cb;
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Returns the recenter panel config.
         *
         * Parameters:
         * m - {Openlayers.Map} The map object
         * ns - {String} The GeoServer namespace alias (aka prefix) for localisation layers.
         *
         * Returns:
         * {Ext.FormPanel} recenter panel config 
         */
        create: function(m, ns) {
        	map = m;
            nsAlias = ns;
            
            comboPanel = new Ext.Panel({
                layout: 'card',
                activeItem: 0,
                region: 'south',
                defaults: {
                    labelSeparator: ' :',
                    loadingText: 'Chargement...',
                    labelWidth: 50,
                    frame: false,
                    border: false
                },
                items: [{
                    xtype: 'form',
                    items: [
                        createCbSearch()
                    ]
                }]
            });
            
            return {
                layout: 'border',
                defaults: {
                    bodyStyle: 'padding: 5px',
                    frame: false,
                    border: false
                },
                items: [
                    {
                        xtype: 'form',
                        labelWidth: 50,
                        region: 'center',
                        labelSeparator: ' :',
                        items: [
                            createLayerCombo()
                        ]
                    },
                    comboPanel
                ]
            };
        }
    };
})();
