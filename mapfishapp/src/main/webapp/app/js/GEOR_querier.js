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
 * Notes about the -AT-requires and -AT-include annotations below:
 *
 * - -AT-requires OpenLayers/Filter/Comparison.js is required because
 *   Styler/widgets/form/ComparisonComboBox.js, which is
 *   included by Styler/widgets/FilterPanel.js, requires it
 * - -AT-requires OpenLayers/Filter/Spatial.js is required because
 *   Styler/widgets/form/SpatialComboBox.js, which is
 *   included by Styler/widgets/SpatialFilterPanel.js, requires it
 * - -AT-include OpenLayers/Filter/Logical.js is required because
 *   Styler/widgets/FilterBuilder.js uses OpenLayers.Filter.Logical
 */

/*
 * @requires OpenLayers/Filter/Comparison.js
 * @requires OpenLayers/Filter/Spatial.js
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Format/CQL.js
 * @include OpenLayers/Format/Filter.js
 * @include OpenLayers/Format/XML.js
 // see https://github.com/georchestra/georchestra/issues/482 :
 * @include OpenLayers/Format/WKT.js
 * @include OpenLayers/Control/ModifyFeature.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Polygon.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/AttributeStore.js
 * @include Styler/widgets/FilterBuilder.js
 * @include Ext.state.LocalStorageProvider.js
 * @include GEOR_waiter.js
 * @include GEOR_ows.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.querier = (function() {

    /*
     * Private
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: ready
         * Fires when the filterbuilder panel is ready 
         *
         * Listener arguments:
         * filterBuilderCfg - {Object} Config object for a panel 
         *  with a filterbuilder
         */
        "ready",
        /**
         * Event: showrequest
         * Fires when the filterbuilder panel is already built 
         * and we just need to display it.
         */
        "showrequest",
        /**
         * Event: searchresults
         * Fires when we've received a response from server 
         *
         * Listener arguments:
         * options - {Object} A hash containing response, model and format
         */
        "searchresults",
        /**
         * Event: search
         * Fires when the user presses the search button
         *
         * Listener arguments:
         * panelCfg - {Object} Config object for a panel 
         */
        "search"
    );
    
    /**
     * Property: record
     * {Ext.data.Record} The matching record for a WFS layer
     *      Fields: "owsType" (should be "WFS"), "owsURL" & "typeName"
     */
    var record = null;
    
    /**
     * Property: attStore
     * {GeoExt.data.AttributeStore} 
     */
    var attStore = null;
    
    /**
     * Property: geometryName
     * {String} The geometry column name
     */
    var geometryName = null;
    
    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;
    
    /**
     * Property: styleMap
     * {OpenLayers.StyleMap} StyleMap used for vectors
     */
    var styleMap = null;
    
    /**
     * Property: cp
     * {Ext.state.Provider} the state provider
     */  
    var cp = null;
    
    /**
     * Property: layerFields
     * {Array} an array of fields for the current layer
     * It is extracted from the WFS DescribeFeatureType operation
     */  
    var layerFields = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    var name = null;

    /**
     * Method: checkFilter
     * Checks that a filter is not missing items.
     *
     * Parameters:
     * filter - {OpenLayers.Filter} the filter
     *
     * Returns:
     * {Boolean} Filter is correct ?
     */
    var checkFilter = function(filter) {
        var filters = filter.filters || [filter];
        for (var i=0, l=filters.length; i<l; i++) {
            var f = filters[i];
            if (f.CLASS_NAME == 'OpenLayers.Filter.Logical') {
                if (!checkFilter(f)) {
                    return false;
                }
            } else if (!(f.value && f.type && 
                (f.property || f.CLASS_NAME == "OpenLayers.Filter.Spatial"))) {
                GEOR.util.infoDialog({
                    msg: tr("Fields of filters with a red mark are mandatory")
                });
                return false;
            }
        }
        return true;
    };

    /**
     * Method: search
     * Gets the Filter Encoding string and sends the getFeature request
     */
    var search = function() {
        var filterbuilder = this.findParentByType("gx_filterbuilder");
        // we quickly check if nothing lacks in filter
        var filter = filterbuilder.getFilter();
        if (!checkFilter(filter)) {
            return;
        }
    
        observable.fireEvent("search", {
            html: tr("<div>Searching...</div>")
        });
        
        // we deactivate draw controls before the request is done.
        filterbuilder.deactivateControls();
        
        // we need to pass the geometry name at protocol creation, 
        // so that the format has the correct geometryName too.
        GEOR.ows.WFSProtocol(record, map, {geometryName: geometryName}).read({
            maxFeatures: GEOR.config.MAX_FEATURES,
            // some mapserver versions require that we list all fields to return 
            // (as seen with 5.6.1):
            // see http://applis-bretagne.fr/redmine/issues/1996
            propertyNames: layerFields || [], 
            filter: filter,
            callback: function(response) {
                // Houston, we've got a pb ...
                
                // When using WFS 1.0, getFeature requests are honored with geodata in the original data's SRS 
                // ... which might not be the map SRS !
                // When using GeoServer, an additional parameter can be used (srsName) to ask for feature reprojection, 
                // which is not part of the WFS 1.0 spec
                // When using MapServer, it seems that we have no way to do the same.
                
                // So, basically, we have two solutions :
                // - stick to WFS 1.1 spec, which leaves WFS servers with old MapServers unqueryable. 
                //   We don't want that.
                // - stick to WFS 1.0 spec and add the srsName parameter (in case we're in front of a GeoServer). 
                // In that case, WFSes made with MapServer will return geometries in the original data SRS ... 
                // which could be parsed, and reprojected on the client side using proj4js when user agrees to do so
                
                if (!response.success()) {
                    return;
                }
                
                var model =  (attStore.getCount() > 0) ? new GEOR.FeatureDataModel({
                    attributeStore: attStore
                }) : null;

                observable.fireEvent("searchresults", {
                    features: response.features,
                    model: model,
                    tooltip: name + " - " + tr("WFS GetFeature on filter"),
                    title: GEOR.util.shortenLayerName(name)
                });
            },
            scope: this
        });
    };
    
    /**
     * Method: buildPanel
     *
     */
    var buildPanel = function(layerName, r) {
        record = r;
        name = layerName;
        observable.fireEvent("ready", {
            xtype: 'gx_filterbuilder',
            title: tr("Request on NAME", {
                'NAME': GEOR.util.shortenLayerName(layerName)
            }),
            defaultBuilderType: Styler.FilterBuilder.ALL_OF,
            filterPanelOptions: {
                attributesComboConfig: {
                    displayField: "name",
                    listWidth: 165,
                    tpl: GEOR.util.getAttributesComboTpl()
                }
            },
            allowGroups: false,
            noConditionOnInit: true,
            deactivable: true,
            cookieProvider: cp,
            autoScroll: true,
            buttons: [
                {
                    text: tr("Search"),
                    handler: search
                },
                {
                    text: tr("Filter"),
                    handler: filterLayer
                }
            ],
            map: map,
            attributes: attStore,
            allowSpatial: true,
            vectorLayer: new OpenLayers.Layer.Vector('__georchestra_filterbuilder',{
                displayInLayerSwitcher: false,
                styleMap: styleMap
            })
        });
    };
    
    var filterLayer = function() {
        var filterbuilder = this.findParentByType("gx_filterbuilder");
        var filter = filterbuilder.getFilter();
        if (!checkFilter(filter)) {
            return;
        }
        
        var layers = map.getLayersBy('id',record.id);
        
        if( layers.length > 0 ) {
            if( filter.CLASS_NAME == "OpenLayers.Filter.Spatial" || filter.CLASS_NAME == "OpenLayers.Filter.Comparison" || ('filters' in filter && filter.filters.length > 0) ) {
                layers[0].mergeNewParams({'CQL_FILTER': (new OpenLayers.Format.CQL()).write(filter)});
            } else {
                layers[0].mergeNewParams({'CQL_FILTER': null});
            }
        }
    }

    return {
    
        /*
         * Observable object
         */
        events: observable,
        
        /**
         * APIMethod: init
         * Initialize this module 
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */
        init: function(m) { 
            map = m;
            tr = OpenLayers.i18n;
            
            // storage !
            cp = new Ext.state.LocalStorageProvider({
                prefix: "geor-viewer-"
            });
            Ext.state.Manager.setProvider(cp);
            
            var style = OpenLayers.Util.extend({}, 
                        OpenLayers.Feature.Vector.style['default']);
            
            styleMap = GEOR.util.getStyleMap({
                "default": {
                    strokeWidth: 2,
                    strokeColor: "#ee5400",
                    fillOpacity: 0
                }
            });
        },
        
        /*
         * Method: create
         *
         * Parameters:
         * layerName - {String} the "nice" layer name.
         * record - {GeoExt.data.LayerRecord} a WMSDescribeLayer record
         *          with at least three fields "owsURL", "typeName" and "featureNS"
         * success - {Function} optional success callback
         */
        create: function(layerName, record, success) {
            GEOR.waiter.show();
            attStore = GEOR.ows.WFSDescribeFeatureType(record, {
                extractFeatureNS: true,
                success: function() {
                    // we list all fields, including the geometry
                    layerFields = attStore.collect('name');
                    // we get the geometry column name, and remove the corresponding record from store
                    var idx = attStore.find('type', GEOR.ows.matchGeomProperty);
                    if (idx > -1) { 
                        // we have a geometry
                        var r = attStore.getAt(idx);
                        geometryName = r.get('name');
                        attStore.remove(r);
                        if (success) {
                            success.call(this);
                        }
                        buildPanel(layerName, record);
                    } else {
                        GEOR.util.infoDialog({
                            msg: tr("querier.layer.no.geom")
                        });
                    }
                },
                failure: function() {
                    GEOR.util.errorDialog({
                        msg: tr("querier.layer.error")
                    });
                },
                scope: this
            });

        }
    };
})();
