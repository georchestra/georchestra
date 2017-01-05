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
 * @include OpenLayers/Format/KML.js
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Projection.js
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

GEOR.ResultsPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: title
     * {String}
     */
    title: OpenLayers.i18n("Search"),

    /**
     * Property: closable
     * {Boolean}
     */
    closable: true,

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    map: null,

    /**
     * Property: sfControl
     * {OpenLayers.Control.SelectFeature} The control used for the 
     * feature selection model
     */
    sfControl: null,

    //TODO doc
    id: "resultPanel",

    /**
     * Property: noDelete
     * {Boolean} do not show the delete button
     */
    noDelete: false,

    /**
     * Private: store
     * {GeoExt.data.FeatureStore}
     */
    _store: null,

    /**
     * Private: vectorLayer
     * {OpenLayers.Layer.Vector} The vector layer on which we display results
     */
    _vectorLayer: null,

    /**
     * Private: model
     * {GEOR.FeatureDataModel} data model
     */
    _model: null,

    /**
     * Private: LS_PREFIX
     */
    LS_PREFIX: "geor-viewer-symbolizer-",

    /**
     * Method: _exportBtnHandler
     * Triggers the download dialog for export of the store's content
     */
    _exportBtnHandler: function(filetype) {
        var t = this._store.getCount();
        if (t === 0) {
            return;
        }
        GEOR.waiter.show();
        var data = [], features = [], att, record, raw,
            fields = this._model.getFields(),
            grid = this.findByType("grid")[0];
        if (!grid) {
            return;
        }
        var sm = grid.getSelectionModel(),
            bypass = false,
            payload;
        if (sm.getCount() == 0) {
            bypass = true;
        }
        switch (filetype) {
            case "csv":
                for (var i=0; i<t; i++) {
                    record = this._store.getAt(i);
                    if (bypass || sm.isSelected(record)) {
                        raw = [];
                        att = record.getFeature().attributes;
                        // see http://applis-bretagne.fr/redmine/issues/4084
                        for (var j=0, ll=fields.length; j<ll; j++) {
                            raw.push(att[fields[j]] || '');
                        }
                        data.push(raw);
                    }
                }
                payload = (new OpenLayers.Format.JSON()).write({
                    columns: this._model.getFields(),
                    data: data
                });
                break;
            default: // kml || gml
                for (var i=0; i<t; i++) {
                    record = this._store.getAt(i);
                    if (bypass || sm.isSelected(record)) {
                        features.push(record.getFeature());
                    }
                }
                if (features[0] && features[0].geometry == null) {
                    GEOR.waiter.hide();
                    GEOR.util.infoDialog({
                        msg: OpenLayers.i18n("Export is not possible: features have no geometry")
                    });
                    return;
                }
                var ip = this.map.getProjectionObject(),
                    ep = new OpenLayers.Projection("EPSG:4326");
                if (filetype == "gml") {
                    payload = (new OpenLayers.Format.GML({
                        featureNS: "http://www.georchestra.org/features",
                        'internalProjection': ip,
                        'externalProjection': ep
                    })).write(features);
                    break;
                } else if (filetype == "kml") {
                    payload = (new OpenLayers.Format.KML({
                        'foldersName': "geOrchestra",
                        'internalProjection': ip,
                        'externalProjection': ep
                    })).write(features);
                    break;
                }
        }
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/"+filetype+"/",
            data: payload,
            success: function(response) {
                var o = Ext.decode(response.responseText);
                window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
            }
        });
    },

    /**
     * Method: _zoomToFeatures
     * Sets the map extent in order to see all results
     *
     * Parameters:
     * features - {Array(Openlayers.Feature.Vector)} an optional array of features
     *  If not provided, this method will zoom to the vector layer extent.
     *
     */
    _zoomToFeatures: function(features) {
        var bounds, layerBounds = null;
        var map = this.map;

        if (features && features[0]) {
            bounds = new OpenLayers.Bounds();
            Ext.each(features, function(f) {
                if (f.bounds) {
                    bounds.extend(f.bounds);
                } else if (f.geometry) {
                    bounds.extend(f.geometry.getBounds());
                }
            });
        } else if (this._vectorLayer.features.length) {
            bounds = this._vectorLayer.getDataExtent();
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
    },

    /**
     * Method: _getFid
     *
     */
    _getFid: function(feature) {
        return feature.fid && feature.fid.split(".")[0];
    },

    /**
     * Method: _createVectorLayer
     *
     */
    _createVectorLayer: function(styleMapOverrides) {
        this._vectorLayer = new OpenLayers.Layer.Vector("__georchestra_results_"+this.id, {
            displayInLayerSwitcher: false,
            styleMap: GEOR.util.getStyleMap(styleMapOverrides),
            rendererOptions: {
                zIndexing: true
            }
        });
    },

    /**
     * Method: _storeGeometry
     * Aggregates selected features' geometries and stores it in LocalStorage
     * for later use in querier.
     *
     */
    _storeGeometry: function() {
        // compute aggregation of geometries
        var features = (this._vectorLayer.selectedFeatures.length ? 
            this._vectorLayer.selectedFeatures : this._vectorLayer.features),
            components = [], type;
        Ext.each(features, function(feature) {
            if (/OpenLayers\.Geometry\.Multi.*/.test(feature.geometry.CLASS_NAME)) {
                // multi-geometry
                Ext.each(feature.geometry.components, function(cmp) {
                    // check that we are not adding pears with bananas
                    if (!type) {
                        type = cmp.CLASS_NAME;
                        components.push(cmp.clone());
                    } else if (cmp.CLASS_NAME == type){
                        components.push(cmp.clone());
                    }
                });
            } else {
                // simple geometry
                if (!type) {
                    type = feature.geometry.CLASS_NAME;
                    components.push(feature.geometry.clone());
                } else if (feature.geometry.CLASS_NAME == type){
                    components.push(feature.geometry.clone());
                }
            }
        });
        // store the geometry for later use
        var singleType = type.substr(type.lastIndexOf('.')+1),
            geometry = new OpenLayers.Geometry["Multi"+singleType](components);
        var provider = Ext.state.Manager.getProvider();
        provider.set('geometry',
            provider.encodeValue(geometry.toString())
        );
        GEOR.util.infoDialog({
            msg: OpenLayers.i18n("Geometry successfully stored in this browser")
        });
    },

    /**
     * Method: _createGridPanel
     * Empties the container panel, creates and loads the gridPanel into it
     *
     */
    _createGridPanel: function() {
        var tr = OpenLayers.i18n;

        var columnModel = this._model.toColumnModel({
            sortable: true
        });

        var c = this._store.getCount();
        var plural = (c>1) ? "s" : "";

        var tbtext = new Ext.Toolbar.TextItem({
            text: (c == GEOR.config.MAX_FEATURES) ?
            tr("resultspanel.maxfeature.reached", {
                'NB': GEOR.config.MAX_FEATURES
            }) :
            (c>1) ? tr("NB results", {'NB': c}) :
            (c>0) ? tr("One result") :
            tr("No result")
        });

        var actionsItem = {
            text: tr("Actions"),
            tooltip: tr("Actions on the selection or on all results if no row is selected"),
            menu: new Ext.menu.Menu({
                items: [{
                    text: tr("Zoom"),
                    iconCls: 'geor-btn-zoom',
                    tooltip: tr("Zoom to results extent"),
                    handler: function() {
                        var features = this.getSelectedFeatures();
                        this._zoomToFeatures(features);
                    },
                    scope: this
                }, {
                    text: tr("Export"),
                    iconCls: 'geor-export',
                    menu: [{
                        text: "CSV",
                        tooltip: tr("Export results as") + " CSV",
                        handler: this._exportBtnHandler.createDelegate(this, ["csv"]),
                        scope: this
                    }, {
                        text: "GML",
                        tooltip: tr("Export results as") + " GML",
                        handler: this._exportBtnHandler.createDelegate(this, ["gml"]),
                        scope: this
                    }, {
                        text: "KML",
                        tooltip: tr("Export results as") + " KML",
                        handler: this._exportBtnHandler.createDelegate(this, ["kml"]),
                        scope: this
                    }]
                }, {
                    text: tr("Store the geometry"),
                    iconCls: 'geor-geom-save',
                    tooltip: tr("Aggregates the geometries of the selected features and stores it in your browser for later use in the querier"),
                    handler: this._storeGeometry,
                    scope: this
                }]
            })
        };

        var bbar = [
            {
                text: tr("Clean"),
                tooltip: tr("Clean all results on the map and in the table"),
                disabled: this.noDelete === true,
                handler: function() {
                    this._vectorLayer.destroyFeatures();
                    tbtext.hide();
                },
                scope: this
            },
            {
                text: tr("Symbology"),
                tooltip: tr("Edit this panel's features symbology"),
                handler: function(b) {
                    var feature = this._vectorLayer.features[0];
                    if (!feature) {
                        return;
                    }
                    var fid = this._getFid(feature),
                        symbolType = feature.geometry.CLASS_NAME.replace(/OpenLayers\.Geometry\.(Multi)?|String/g, ""),
                        symbolizer = Ext.apply({}, this._vectorLayer.styleMap.styles["default"].defaultStyle);

                    var win = new Ext.Window({
                        title: tr("Symbology"),
                        layout: "fit",
                        width: 400,
                        height: 400,
                        closeAction: "close",
                        constrainHeader: true,
                        animateTarget: GEOR.config.ANIMATE_WINDOWS && b.el,
                        items: [{
                            xtype: "gx_" + symbolType.toLowerCase() + "symbolizer",
                            symbolizer: symbolizer,
                            bodyStyle: {
                                "padding": "10px"
                            },
                            border: false,
                            labelWidth: 70,
                            defaults: {
                                labelWidth: 70
                            },
                            listeners: {
                                "change": function(symbolizer) {
                                    this._vectorLayer.style = symbolizer;
                                    this._vectorLayer.redraw(true);
                                    if (fid) {
                                        // we make the symbolizer persist across requests
                                        // through localStorage:
                                        GEOR.ls.set(this.LS_PREFIX + fid, Ext.encode(symbolizer));
                                    }
                                },
                                scope: this
                            }
                        }],
                        buttons: [{
                            text: tr("Reset"),
                            handler: function() {
                                GEOR.ls.remove(this.LS_PREFIX + fid);
                                this._vectorLayer.style = null;
                                this._vectorLayer.styleMap = GEOR.util.getStyleMap();
                                this._vectorLayer.redraw(true);
                                win.close();
                            },
                            scope: this
                        }, {
                            text: tr("OK"),
                            handler: function() {
                                win.close();
                            }
                        }]
                    }).show();
                },
                scope: this
            },
            '->', tbtext, '-',
            {
                text: tr("Select"),
                menu: new Ext.menu.Menu({
                    items: [{
                        text: tr("All"),
                        handler: function() {
                            var grid = this.findByType("grid")[0];
                            if (grid) {
                                grid.getSelectionModel().selectAll();
                            }
                        },
                        scope: this
                    },{
                        text: tr("None"),
                        handler: function() {
                            var grid = this.findByType("grid")[0];
                            if (grid) {
                                grid.getSelectionModel().clearSelections();
                            }
                        },
                        scope: this
                    },{
                        text: tr("Invert selection"),
                        handler: function() {
                            var grid = this.findByType("grid")[0];
                            if (grid) {
                                var sm = grid.getSelectionModel(),                            
                                    recordsToSelect = [];
                                this._store.each(function(record) {
                                    if (!sm.isSelected(record)) {
                                        recordsToSelect.push(record);
                                    }
                                });
                                sm.clearSelections();
                                sm.selectRecords(recordsToSelect);
                            }
                        },
                        scope: this
                    }]
                })
            }, '-',
            actionsItem
        ];


        /** Loading Addon actions
         *
         * To be able to insert their own action in the ResultsPanel "actions" menu,
         * addons must have the `resultPanelAction` option set to true
         * and an API method named `resultPanelHandler`
         * with the `(menuitem, event, resultPanel)` signature.
         * The `resultPanelHandler` scope is set to the addon.
         */
        var addonsState = GEOR.tools.getAddonsState();
        Ext.each(GEOR.config.ADDONS, function(cfg) {
            if (addonsState[cfg.id] && cfg.options && cfg.options.resultPanelAction === true) {
                var a = GEOR.tools.getAddon(cfg.id);
                actionsItem.menu.addItem({
                    text: a.title,
                    iconCls: a.iconCls,
                    tooltip: a.qtip,
                    handler: a.resultPanelHandler.createDelegate(a, [this])
                });
            }
        }, this);

        if (!this.sfControl) {
            // we need to create the SelectFeature control by ourselves
            // because we need to modify its internal properties
            // and we cannot get a reference to these when the control is created
            // inside the GeoExt.grid.FeatureSelectionModel
            this.sfControl = new OpenLayers.Control.SelectFeature(this._vectorLayer, {
                toggle: true,
                multipleKey: Ext.isMac ? "metaKey" : "ctrlKey"
            });
            this.map.addControl(this.sfControl);
            // see http://applis-bretagne.fr/redmine/issues/1983
            this.sfControl.handlers.feature.stopDown = false;
        }
        this.removeAll();
        var me = this;
        this.add({
            xtype: "grid",
            viewConfig: {
                // we add an horizontal scroll bar in case
                // there are too many attributes to display:
                forceFit: (columnModel.length < 10)
            },
            store: this._store,
            columns: columnModel,
            sm: new GeoExt.grid.FeatureSelectionModel({
                singleSelect: false,
                selectControl: this.sfControl
            }),
            frame: false,
            border: false,
            buttonAlign: 'left',
            bbar: bbar,
            listeners: {
                "rowdblclick": function(grid, rowIndex, e) {
                    me._zoomToFeatures([me._store.getAt(rowIndex).get('feature')]);
                },
                "beforedestroy": function() {
                    me._vectorLayer.destroyFeatures();
                    this.selModel.unbind(); // required to handle issue 256
                    // http://applis-bretagne.fr/redmine/issues/show/256
                    // this deactivates Feature handler,
                    // and moves search_results layer back to normal z-index
                    return true;
                }
            }
        });
    },

    /**
     * Method: populate
     * Callback executed when we receive the XML data.
     *
     * Parameters:
     * options - {Object} Hash with keys: features, addLayerToMap and model (both optional)
     * addLayerToMap defaults to true
     */
    populate: function(options) {
        var tr = OpenLayers.i18n;

        var features = options.features;
        if (!features || features.length === 0) {
            GEOR.waiter.hide();
            this._vectorLayer && this._vectorLayer.removeAllFeatures();
            this.removeAll();
            this.add({
                bodyStyle: 'padding:1em;',
                html: tr("<p>No result for that request.</p>")
            });
            return;
        }

        // retrieve matching symbolizer, if any.
        var fid = this._getFid(features[0]),
            styleMapOverrides = {};
        if (fid) {
            // restore custom StyleMap
            var storedSymbolizer = GEOR.ls.get(this.LS_PREFIX + fid);
            if (storedSymbolizer) {
                var s = Ext.decode(storedSymbolizer);
                styleMapOverrides = {
                    "default": s,
                    "select": s
                }
            }
        }
        // create vector layer
        if (!this._vectorLayer) {
            this._createVectorLayer(styleMapOverrides);
        }

        if (options.addLayerToMap !== false) {
            if (OpenLayers.Util.indexOf(this.map.layers, this._vectorLayer) < 0) {
                this.map.addLayer(this._vectorLayer);
            }
        } else if (OpenLayers.Util.indexOf(this.map.layers, this._vectorLayer) >= 0) {
            this.map.removeLayer(this._vectorLayer);
        }

        if (options.model) {
            this._model = options.model;
        } else {
            // we need to compute the model from the features we got.
            this._model = new GEOR.FeatureDataModel({
                features: features
            });
        }

        this._store = new GeoExt.data.FeatureStore({
            layer: this._vectorLayer,
            fields: this._model.toStoreFields()/*,
            initDir: (options.addLayerToMap === false) ? 
                GeoExt.data.FeatureStore.LAYER_TO_STORE : 
                GeoExt.data.FeatureStore.LAYER_TO_STORE|GeoExt.data.FeatureStore.STORE_TO_LAYER
            */
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
        this._store.loadData(features);
        this._createGridPanel();
    },

    /**
     * APIMethod: getSelectedFeatures
     * This method returns the selected features in the grid.
     *
     */
    getSelectedFeatures: function() {
        var grid = this.findByType("grid")[0];
        if (!grid) {
            return [];
        }
        var sm = grid.getSelectionModel(),
            selectedFeatures = [],
            bypass = (sm.getCount() == 0);
        this._store.each(function(record) {
            if (bypass || sm.isSelected(record)) {
                selectedFeatures.push(record.get("feature"));
            }
        });
        return selectedFeatures;
    },

    /**
     * APIMethod: getModel
     *
     */
    getModel: function() {
        return this._model;
    },

    /**
     * APIMethod: clean
     * This method destroys the features in the vector layer.
     *
     */
    clean: function() {
        if (this._vectorLayer) {
            this._vectorLayer.setVisibility(false);
        }
    },

    /**
     * APIMethod: lower
     * Hides the attached vector layer.
     */
    lower: function() {
        if (this._vectorLayer) {
            this._vectorLayer.setVisibility(false);
        }
    },

    /**
     * APIMethod: raise
     * Displays the attached vector layer.
     */
    raise: function() {
        if (this._vectorLayer) {
            this._vectorLayer.setVisibility(true);
        }
    }

});
