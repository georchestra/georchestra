/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOR_ows.js
 * @include GEOR_util.js
 * @include GEOR_config.js
 * @include GEOR_waiter.js
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Filter/Comparison.js
 * @include OpenLayers/Filter/Spatial.js
 * @include GeoExt/data/FeatureStore.js
 * @include GeoExt/data/ProtocolProxy.js
 */
 
Ext.namespace("GEOR");

GEOR.referentials = (function() {

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: beforelayerchange
         * Fires when a layer selection is about to change
         */
        "recenter"
    );

    /*
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;
    
    /*
     * Property: namespace
     * {String} the GeoServer namespace with localization layers
     */
    var namespace = null;
    
    /*
     * Property: buffer
     * {Integer} the current buffer value, in meters
     */
    var buffer = 0;
    
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
                "{[GEOR.util.stringUpperCase(values.feature.attributes."+
                items[i]+")]}"+
                "</strong>";
        }
        return new Ext.XTemplate(
            '<tpl for="."><div class="search-item">'+
            s.join(' - ')+'</div></tpl>');
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
            GEOR.waiter.show();
            comboPanel.disable();
            
            var protocol = record.get('layer').protocol;
            
            var attStore = GEOR.ows.WFSDescribeFeatureType({
                owsURL: protocol.url,
                typeName: namespace + ':' + record.get('name')
            }, {
                "success": function() {
                    // create new formPanel with search combo
                    var panel = new Ext.form.FormPanel({
                        items: [
                            createCbSearch(
                                record, 
                                filterStringType(attStore))
                        ]
                    });
                    // add new panel containing combo to card layout
                    comboPanel.add(panel);
                    // switch panels
                    comboPanel.layout.setActiveItem(panel);
                    comboPanel.enable();
                    cbPanels[idx] = panel;
                },
                "failure": function() {
                    GEOR.waiter.hide();
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
     * {Ext.form.ComboBox} the combobox
     */
    var createLayerCombo = function() {
    
        var store = GEOR.ows.WFSCapabilities({
            storeOptions: {
                url: GEOR.config.GEOSERVER_WFS_URL,
                protocolOptions: {
                    srsName: GEOR.config.GLOBAL_EPSG,
                    srsNameInQuery: true, // see http://trac.osgeo.org/openlayers/ticket/2228
                    url: GEOR.config.GEOSERVER_WFS_URL
                }
            },
            vendorParams: {
                namespace: namespace
            }
        });
    
        return new Ext.form.ComboBox({
            fieldLabel: 'Couche',
            emptyText: 'Choisissez une couche',
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
     * Method: createBufferCombo
     * Creates the buffer combobox
     *
     * Returns:
     * {Ext.form.ComboBox} the combobox
     */
    var createBufferCombo = function() {
    
        var store = new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data: [
                [0,'aucun'],
                [10,'10 mètres'],
                [50,'50 mètres'],
                [100,'100 mètres'],
                [500,'500 mètres'],
                [1000,'1 kilomètre'],
                [5000,'5 kilomètres'],
                [10000,'10 kilomètres']
            ]
        });
    
        return new Ext.form.ComboBox({
            fieldLabel: 'Buffer',
            mode: 'local',
            value: GEOR.config.DEFAULT_BUFFER_VALUE,
            store: store,
            displayField: 'text',
            valueField: 'value',
            width: 160,
            listWidth: 160+18,
            triggerAction: "all",
            editable: false,
            listeners: {
                "select": function(cb, record, idx) {
                    buffer = record.get('value');
                },
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
        var bounds = feature.bounds || feature.geometry.getBounds();
        if (!bounds) {
            return;
        }
        // clone bounds to fix a projection issue since the same feature
        // was returned multiple times
        bounds = bounds.clone();

        var currentSrs = map.getProjection();
        if (currentSrs != GEOR.config.GLOBAL_EPSG) {
            // reproject to match the current srs
            bounds.transform(
                new OpenLayers.Projection(GEOR.config.GLOBAL_EPSG),
                new OpenLayers.Projection(currentSrs)
            );
        }
        
        // Handle positive buffer values:
        if (buffer > 0) {
            var units = map.getProjectionObject().getUnits();
            if (units == 'm' || units == 'meters') {
                bounds.left -= buffer;
                bounds.bottom -= buffer;
                bounds.right += buffer;
                bounds.top += buffer;
            } else if (units == 'degrees') {
                // temporarily transform to a SRS with metric coords
                bounds.transform(
                    new OpenLayers.Projection(currentSrs),
                    new OpenLayers.Projection("EPSG:900913")
                );
                bounds.left -= buffer;
                bounds.bottom -= buffer;
                bounds.right += buffer;
                bounds.top += buffer;
                bounds.transform(
                    new OpenLayers.Projection("EPSG:900913"),
                    new OpenLayers.Projection(currentSrs)
                );
            }
        }
        
        // Handle zero size bounding boxes:
        if (bounds.getWidth() + bounds.getHeight() > 0) {
            map.zoomToExtent(bounds.scale(1.05)); // scale() does not modify the object it applies to
        } else {
            map.setCenter(bounds.getCenterLonLat(), map.baseLayer.numZoomLevels-1); 
            // the default extraction area is the visible area:
            bounds = map.getExtent();
        }
        
        // Send the bounds object to the layer options (using current map SRS):
        observable.fireEvent('recenter', bounds);
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
        // toUpperCase required, since all the DBF data is UPPERCASED
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
                    "beforeload": function(store, options) {
                        // add a filter to the options passed to proxy.load, 
                        // proxy.load passes these options to protocol.read
                        var queryString = store.baseParams['query'];
                        options.filter = buildFilter(queryString, attributes);
                        
                        // We don't need the geometry, 
                        // just its bounds, and attributes:
                        options.propertyNames = attributes;
                        // This means that a spatial index is required !
                        
                        // remove the queryParam from the store's base
                        // params not to pollute the query string:
                        delete queryString;
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
            emptyText: disabled ? '' : 'localité ?',
            store: store,
            listeners: {
                "select": function(combo, record, index) {
                    onComboSelect(record);
                },
                "specialkey": function(combo, event) {
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
            "load": function(){
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

        /*
         * Observable object
         */
        events: observable,

        /**
         * APIMethod: create
         * Returns the recenter panel config.
         *
         * Parameters:
         * m - {Openlayers.Map} The map object
         * ns - {String} The GeoServer namespace for localisation layers.
         *
         * Returns:
         * {Ext.FormPanel} recenter panel config 
         */
        create: function(m, ns) {
        	map = m;
            namespace = ns;
            buffer = GEOR.config.DEFAULT_BUFFER_VALUE;
            
            comboPanel = new Ext.Panel({
                layout: 'card',
                height: 35,
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
                items: [{
                    html: "<span>Le recentrage modifie également l'emprise de la couche sélectionnée, lorsque celle-ci est visible.</span>",
                    region: 'north',
                    bodyCssClass: 'paneltext',
                    height: 50
                },{
                    xtype: 'form',
                    labelWidth: 50,
                    //height: 20,
                    region: 'center',
                    labelSeparator: ' :',
                    items: [
                        createLayerCombo(),
                        createBufferCombo()
                    ]
                }, comboPanel]
            };
        }
    };
})();
