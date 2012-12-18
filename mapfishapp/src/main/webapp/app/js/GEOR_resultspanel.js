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
 * @include GeoExt/data/FeatureStore.js
 * @include GeoExt/widgets/grid/FeatureSelectionModel.js
 * @include OpenLayers/Format/GML.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Geometry/Point.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include GEOR_waiter.js
 * @include GEOR_ajaxglobal.js
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include GEOR_FeatureDataModel.js
 */

Ext.namespace("GEOR");

GEOR.resultspanel = (function() {

    /*
     * Private
     */

    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: panel
         * Fires when we have a panel to display south
         */
        "panel"
    );

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;

    /**
     * Property: store
     * {GeoExt.data.FeatureStore}
     */
    var store = null;

    /**
     * Property: vectorLayer
     * {OpenLayers.Layer.Vector} The vector layer on which we display results
     */
    var vectorLayer = null;

    /**
     * Property: model
     * {GEOR.FeatureDataModel} data model
     */
    var model = null;

    /**
     * Property: layerBounds
     * {OpenLayers.Bounds} The cached vector layer bounds
     */
    var layerBounds = null;

    /**
     * Property: sfControl
     * {OpenLayers.Control.SelectFeature} The control used for the feature
     *  selection model
     */
    var sfControl = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: csvExportBtnHandler
     * Triggers the download dialog for CSV export of the store's content
     */
    var csvExportBtnHandler = function() {
        var t = store.getCount();
        if (t === 0) {
            return;
        }
        GEOR.waiter.show();
        var data = new Array(t), att, fields = model.getFields();
        for (var i=0; i<t; i++) {
            data[i] = [];
            att = store.getAt(i).get('feature').attributes;
            // see http://applis-bretagne.fr/redmine/issues/4084
            for (var j=0, ll=fields.length; j<ll; j++) {
                data[i].push(att[fields[j]] || '');
            }
        }
        var format = new OpenLayers.Format.JSON();
        OpenLayers.Request.POST({
            url: "ws/csv/",
            data: format.write({columns: model.getFields(), data: data}),
            success: function(response) {
                var o = Ext.decode(response.responseText);
                window.location.href = o.filepath;
            }
        });
    };

    /**
     * Method: zoomToLayerExtent
     * Sets the map extent in order to see all results
     * Caches the vector layer extent if required
     */
    var zoomToLayerExtent = function() {
        if (!layerBounds) {
            layerBounds = zoomToFeatures();
        } else {
            map.zoomToExtent(layerBounds);
        }
    };

    /**
     * Method: zoomToFeatures
     * Sets the map extent in order to see all results
     *
     * Parameters:
     * features - {Array(Openlayers.Feature.Vector)} an optional array of features
     *  If not provided, this method will zoom to the vector layer extent.
     *
     * Returns:
     * {OpenLayers.Bounds} The scaled vector layer bounds
     */
    var zoomToFeatures = function(features) {
        var bounds, layerBounds = null;
        if (features && features[0]) {
            bounds = new OpenLayers.Bounds();
            Ext.each(features, function(f) {
                if (f.bounds) {
                    bounds.extend(f.bounds);
                } else if (f.geometry) {
                    bounds.extend(f.geometry.getBounds());
                }
            });
        } else if (vectorLayer.features.length) {
            bounds = vectorLayer.getDataExtent();
        } else {
            return;
        }
        if (!bounds || !bounds.left) {
            return;
        }
        if (bounds.getWidth() + bounds.getHeight() !== 0) {
            layerBounds = bounds.scale(1.05);
            map.zoomToExtent(layerBounds);
        } else if (bounds.getWidth() === 0 && bounds.getHeight() === 0) {
            map.setCenter(bounds.getCenterLonLat());
        }
        return layerBounds;
    };

    /**
     * Method: createGridPanel
     * Empties the container panel, creates and loads the gridPanel into it
     *
     * Parameters:
     * store - {GeoExt.data.FeatureStore} our feature store
     */
    var createGridPanel = function(store) {

        var columnModel = model.toColumnModel({
            sortable: true
        });

        var c = store.getCount();
        var plural = (c>1) ? "s" : "";

        var tbtext = new Ext.Toolbar.TextItem({
            text: (c == GEOR.config.MAX_FEATURES) ?
                tr("resultspanel.maxfeature.reached", {'NB': GEOR.config.MAX_FEATURES}):
                (c>1) ? tr("NB results", {'NB': c}) :
                (c>0) ? tr("One result") :
                tr("Not any result")
        });

        var bbar = [{
            text: tr("Clean"),
            tooltip: tr("Clean all results on the map and in the table"),
            handler: function() {
                vectorLayer.destroyFeatures();
                layerBounds = null;
                tbtext.hide();
            }
        }, tbtext, '->', {
            text: tr("Zoom"),
            tooltip: tr("Zoom to results extent"),
            handler: zoomToLayerExtent
        },{
            text: tr("CSV Export"),
            tooltip: tr("Export results as CSV"),
            handler: csvExportBtnHandler
        }];

        if (!sfControl) {
            // we need to create the SelectFeature control by ourselves
            // because we need to modify its internal properties
            // and we cannot get a reference to these when the control is created
            // inside the GeoExt.grid.FeatureSelectionModel
            sfControl = new OpenLayers.Control.SelectFeature(vectorLayer, {
                toggle: true,
                multipleKey: Ext.isMac ? "metaKey" : "ctrlKey"
            });
            map.addControl(sfControl);
            // see http://applis-bretagne.fr/redmine/issues/1983
            sfControl.handlers.feature.stopDown = false;
        }

        observable.fireEvent("panel", {
            xtype: "grid",
            viewConfig: {
                // we add an horizontal scroll bar in case
                // there are too many attributes to display:
                forceFit: (columnModel.length < 10)
            },
            store: store,
            columns: columnModel,
            sm: new GeoExt.grid.FeatureSelectionModel({
                singleSelect: false,
                selectControl: sfControl
            }),
            frame: false,
            border: false,
            buttonAlign: 'left',
            bbar: bbar,
            listeners: {
                "rowdblclick": function(grid, rowIndex, e) {
                    zoomToFeatures([store.getAt(rowIndex).get('feature')]);
                },
                "beforedestroy": function() {
                    this.selModel.unbind(); // required to handle issue 256
                    // http://applis-bretagne.fr/redmine/issues/show/256
                    // this deactivates Feature handler,
                    // and moves search_results layer back to normal z-index
                    return true;
                }
            }
        });

    };

    /**
     * Method: createVectorLayer
     *
     * Returns:
     * {OpenLayers.Layer.Vector}
     */
    var createVectorLayer = function() {
        var defStyle = OpenLayers.Util.extend({},
            OpenLayers.Feature.Vector.style['default']);
        var selStyle = OpenLayers.Util.extend({},
            OpenLayers.Feature.Vector.style['select']);
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(
                OpenLayers.Util.extend(defStyle, {
                    cursor: "pointer",
                    fillOpacity: 0.1,
                    strokeWidth: 3
                })
            ),
            "select": new OpenLayers.Style(
                OpenLayers.Util.extend(selStyle, {
                    cursor: "pointer",
                    strokeWidth: 3,
                    fillOpacity: 0.1,
                    graphicZIndex: 1000
                })
            )
        });
        return new OpenLayers.Layer.Vector("search_results", {
            displayInLayerSwitcher: false,
            styleMap: styleMap,
            rendererOptions: {
                zIndexing: true
            }
        });
    };

    /**
     * Method: populate
     * Callback executed when we receive the XML data.
     *
     * Parameters:
     * options - {Object} Hash with keys: features and model (optional)
     */
    var populate = function(options) {
        // we clear the bounds cache:
        layerBounds = null;

        var features = options.features;
        if (!features || features.length === 0) {
            GEOR.waiter.hide();
            observable.fireEvent("panel", {
                bodyStyle: 'padding:1em;',
                html: tr("<p>Not any result for that request.</p>")
            });
            return;
        }

        if (!vectorLayer) {
            vectorLayer = createVectorLayer();
            map.addLayer(vectorLayer);
        }

        if (options.model) {
            model = options.model;
        } else {
            // we need to compute the model from the features we got.
            model = new GEOR.FeatureDataModel({
                features: features
            });
        }

        store = new GeoExt.data.FeatureStore({
            layer: vectorLayer,
            fields: model.toStoreFields()
        });

        Ext.each(features, function(f, i) {
            // In case we just have a bounds object and no geom,
            // display the bounding box
            if (f.bounds && !f.geometry) {
                if (f.bounds.getWidth() + f.bounds.getHeight() == 0) {
                    // bounds of a single point => create a true point
                    features[i].geometry = new OpenLayers.Geometry.Point(
                        f.bounds.left, f.bounds.top
                    );
                } else {
                    // general case where bounds is a true polygon
                    features[i].geometry = f.bounds.toGeometry();
                }
            }
            // transform URLs into true links
            // see http://applis-bretagne.fr/redmine/issues/4505
            Ext.iterate(f.attributes, function(k, v, o) {
                if (GEOR.util.isUrl(v, false)) {
                    o[k] = '<a href="'+v+'" target="_blank">'+v+'</a>';
                }
            });

        });
        store.loadData(features);
        createGridPanel(store);
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
         * APIMethod: init
         * Initialize this module
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */
        init: function(m) {
            map = m;
            tr = OpenLayers.i18n;
        },

        /**
         * APIMethod: show
         * This method makes the vector layer visible.
         */
        show: function() {
            if (vectorLayer) {
                vectorLayer.setVisibility(true);
            }
        },

        /**
         * APIMethod: hide
         * This method makes the vector layer invisible.
         */
        hide: function() {
            if (vectorLayer) {
                vectorLayer.setVisibility(false);
            }
        },

        /**
         * APIMethod: clean
         * This method destroy the features in the vector layer.
         *
         */
        clean: function() {
            if (vectorLayer) {
                vectorLayer.destroyFeatures();
                layerBounds = null;
            }
        },

        /**
         * APIMethod: populate
         * This method adds features (resulting from a search request)
         * to the vector layer and the results panel.
         *
         * Parameters:
         * options - {Object} Hash with keys: features and model (optional)
         */
        populate: function(options) {
            populate(options);
        }
    };
})();



