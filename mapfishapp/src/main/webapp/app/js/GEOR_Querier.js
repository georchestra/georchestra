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
 * @include GEOR_waiter.js
 * @include GEOR_ows.js
 * @include GEOR_util.js
 */

/* globals Ext, GeoExt, OpenLayers, GEOR */

Ext.namespace("GEOR");


GEOR.Querier = Ext.extend(Ext.Window, {

    layout: "fit",
    closeAction: "close",
    border: false,
 
    /**
     * Property: filterbuilder
     * {String} The local Styler.FilterBuilder instance
     */
    filterbuilder: null,

    /**
     * Property: record
     * {Ext.data.Record} The WMS or WFS layer record
     */
    record: null,

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    map: null,

    /**
     * Property: layer
     * {OpenLayers.Layer.Vector} The layer on which geometries can be drawn
     */
    layer: null,

    /**
     * Property: attributeStore
     * {GeoExt.data.AttributeStore} 
     */
    attributeStore: null,
 
    /**
     * Property: _geometryName
     * {String} The layer geometry name, obtained by DescribeFeatureType
     */
    _geometryName: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {
        var r = this.record,
            type = r.get("type"),
            isWFS = type === "WFS",
            layer = r.get("layer"), 
            name = r.get("title") || layer.name || "",
            geometryName = "";

        this.title = OpenLayers.i18n("Request on NAME", {
            "NAME": name
        });

        var pseudoRecord = {
            typeName: isWFS ? 
                r.get("WFS_typeName") : r.get("name"),
            owsURL: isWFS ? 
                layer.protocol.url : r.get("WFS_URL")
        };

        GEOR.waiter.show();
        // get layer model through WFS DescribeFeatureType:
        this.attributeStore = GEOR.ows.WFSDescribeFeatureType(pseudoRecord, {
            extractFeatureNS: true,
            success: function() {
                // we get the geometry column name, and remove the corresponding record from store
                var idx = this.attributeStore.find("type", GEOR.ows.matchGeomProperty);
                if (idx > -1) {
                    // we have a geometry
                    var r = this.attributeStore.getAt(idx),
                        geometryName = r.get("name");
                    // create the protocol:
                    this.protocol = GEOR.ows.WFSProtocol(pseudoRecord, this.map, {
                        geometryName: geometryName
                    });
                    this._geometryName = geometryName;
                    // remove geometry from attribute store:
                    this.attributeStore.remove(r);
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


        this.layer = new OpenLayers.Layer.Vector("__georchestra_filterbuilder", {
            displayInLayerSwitcher: false,
            styleMap: GEOR.util.getStyleMap({
                "default": {
                    strokeWidth: 2,
                    strokeColor: "#ee5400",
                    fillOpacity: 0
                }
            })
        });

        this.filterbuilder = new Styler.FilterBuilder({
            defaultBuilderType: Styler.FilterBuilder.ALL_OF,
            filterPanelOptions: {
                attributesComboConfig: {
                    displayField: "name",
                    listWidth: 165,
                    tpl: GEOR.util.getAttributesComboTpl()
                }
            },
            attributes: this.attributeStore,
            toolbarType: "tbar",
            map: this.map,
            allowGroups: true,
            noConditionOnInit: false,
            deactivable: true,
            autoScroll: true,
            allowSpatial: true,
            stateProvider: Ext.state.Manager.getProvider(),
            vectorLayer: this.layer
        });
        this.items = [this.filterbuilder];

        this.buttons = [{
            text: OpenLayers.i18n("Close"),
            handler: this.close,
            scope: this
        }, {
            text: OpenLayers.i18n("Search"),
            handler: this.search,
            scope: this
        }];

        this.addEvents(
            /**
             * @event searchresults
             * Fires when we've received a response from server 
             *
             * Listener arguments:
             * options - {Object} A hash containing response, model and format
             */
            "searchresults",

            /**
             * @event search
             * Fires when the user presses the search button
             *
             * Listener arguments:
             * panelCfg - {Object} Config object for a panel 
             */
            "search"
        );

        GEOR.Querier.superclass.initComponent.call(this);
    },


    /**
     * Method: _checkFilter
     * Checks that a filter is not missing items.
     *
     * Parameters:
     * filter - {OpenLayers.Filter} the filter
     *
     * Returns:
     * {Boolean} Filter is correct ?
     */
    _checkFilter: function(filter) {
        var filters = filter.filters || [filter];
        for (var i=0, l=filters.length; i<l; i++) {
            var f = filters[i];
            if (f.CLASS_NAME == "OpenLayers.Filter.Logical") {
                if (!this._checkFilter(f)) {
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
    },


    /**
     * Method: search
     * Gets the Filter Encoding string and sends the getFeature request
     */
    search: function() {
        // we quickly check if nothing lacks in filter
        var filter = this.filterbuilder.getFilter();
        if (!this._checkFilter(filter)) {
            return;
        }

        this.fireEvent("search", {
            // TODO: tell on which layerRecord the search is running ?
            html: tr("<div>Searching...</div>")
        });

        // we deactivate draw controls before the request is done.
        this.filterbuilder.deactivateControls();

        // we need to pass the geometry name at protocol creation, 
        // so that the format has the correct geometryName too.
        this.protocol.read({
            maxFeatures: GEOR.config.MAX_FEATURES,
            // some mapserver versions require that we list all fields to return 
            // (as seen with 5.6.1):
            // see http://applis-bretagne.fr/redmine/issues/1996
            propertyNames: this.attributeStore.collect("name").concat(this._geometryName),
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
                
                var model = (this.attributeStore.getCount() > 0) ? 
                    new GEOR.FeatureDataModel({
                        attributeStore: this.attributeStore
                    }) : null;

                this.fireEvent("searchresults", {
                    features: response.features,
                    model: model,
                    //tooltip: name + " - " + tr("WFS GetFeature on filter"), // FIXME
                    title: "Querier results" // GEOR.util.shortenLayerName(name) // FIXME
                });
            },
            scope: this
        });
    },


    /** private: method[destroy]
     */
    destroy: function() {
        this.filterbuilder.tearDown();
        GEOR.Querier.superclass.destroy.call(this);
    }

});
