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
     * Internationalization
     */
    var tr = OpenLayers.i18n;

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
     * Property: nsalias
     * {String} the GeoServer namespace alias with localization layers
     */
    var nsalias = null;
    
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
     * Property: geometryName
     * {String} the selected layer geometry name
     */
    var geometryName = null;
    
    /*
     * Property: formElementSize
     * {Integer} size in pixels of the form elements
     */
    var formElementSize = 160;
    
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
            '<tpl for=".">'+
            '<div class="x-combo-list-item {[xindex % 2 === 0 ? "even" : "odd"]}">'+
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
                typeName: nsalias + ':' + record.get('name')
            }, {
                "success": function() {
                    // create new formPanel with search combo
                    var panel = new Ext.form.FormPanel({
                        items: [
                            createCbSearch(
                                record, 
                                attStore)
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
                namespace: nsalias
            }
        });
    
        return new Ext.form.ComboBox({
            fieldLabel: tr('Referential'),
            emptyText: tr('Select'),
            forceSelection: true,
            store: store,
            displayField: 'title',
            width: formElementSize,
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
        var data = [];
        Ext.each(GEOR.config.BUFFER_VALUES, function(b) {
            data.push([b[0], tr(b[1], {
                "BUFFER": (b[0] > 999) ? b[0]/1000 : b[0]
            })]);
        });
        var store = new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data: data
        });
    
        return new Ext.form.ComboBox({
            fieldLabel: 'Buffer',
            mode: 'local',
            value: GEOR.config.DEFAULT_BUFFER_VALUE,
            store: store,
            displayField: 'text',
            forceSelection: true,
            valueField: 'value',
            width: formElementSize,
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
     * attStore - {GeoExt.data.AttributeStore}
     *
     * Returns:
     * {Ext.form.ComboBox} 
     */
    var createCbSearch = function(record, attStore) {
        var store, disabled = false;
        
        if (record && record.get('layer')) {
            // find geometry name
            var idx = attStore.find('type', GEOR.ows.matchGeomProperty);
            if (idx > -1) {
                // we have a geometry
                var r = attStore.getAt(idx);
                geometryName = r.get('name');
            } else {
                // this message is destinated to the administrator
                // no need to display a nice dialog.
                alert(
                    tr('The selected layer does not have a geometric column')
                );
            }
            // find the string attribute names:
            var attributes = filterStringType(attStore);
            // create the feature store:
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
                        
                        // with GeoServer2, we need the geometry
                        // since GS2 does not publish bounds as GS1 did
                        // see http://applis-bretagne.fr/redmine/issues/2083
                        options.propertyNames = attributes.concat([geometryName]);
                        
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
            fieldLabel: tr('Recenter on'),
            loadingText: tr('Loading...'),
            name: 'nothing',
            mode: 'remote',
            minChars: 2,
            disabled: disabled,
            editable: true,
            forceSelection: true,
            selectOnFocus: true,
            width: formElementSize,
            queryDelay: 100,
            hideTrigger: true,
            queryParam: 'query', // do not modify
            tpl: buildTemplate(attributes),
            pageSize: 0,
            emptyText: disabled ? '' : tr('location ?'),
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
         *
         * Returns:
         * {Ext.FormPanel} recenter panel config 
         */
        create: function(m) {
        	map = m;
            // the GeoServer namespace alias for localisation layers:
            nsalias = GEOR.config.NS_LOC;
            buffer = GEOR.config.DEFAULT_BUFFER_VALUE;
            var labelWidth = 85;
            
            comboPanel = new Ext.Panel({
                layout: 'card',
                height: 35,
                activeItem: 0,
                region: 'south',
                defaults: {
                    labelSeparator: ' :',
                    loadingText: tr('Loading...'),
                    labelWidth: labelWidth,
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
                    html: tr('referentials.help'),
                    region: 'north',
                    bodyCssClass: 'paneltext',
                    height: 50
                },{
                    xtype: 'form',
                    labelWidth: labelWidth,
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
