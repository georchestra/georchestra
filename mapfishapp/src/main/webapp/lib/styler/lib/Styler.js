/**
 * Copyright (c) 2008 The Open Planning Project
 *
 * @include Styler/dispatch.js
 * @include Styler/widgets/RulePanel.js
 * @include Styler/widgets/LegendPanel.js
 * @include Styler/widgets/ScaleSlider.js
 * @include Styler/widgets/tips/ScaleSliderTip.js
 * @include Styler/SchemaManager.js
 * @include Styler/SLDManager.js
 * @include Styler/Util.js
 */

/**
 * Constructor: Styler
 * Create a new styler application.
 *
 * Extends: Ext.util.Observable
 */
var Styler = function() {
    this.addEvents(
        /**
         * Event: layerchanged
         * Fires when the active layer is changed.
         *
         * Listener arguments:
         * layer - {OpenLayers.Layer} The newly active layer.
         */
        "layerchanged",
        
        /**
         * Event: ruleadded
         * Fires when a rule is added.
         *
         * Listener arguments:
         * rule - {OpenLayers.Rule} The rule added.
         */
        "ruleadded",

        /**
         * Event: ruleremoved
         * Fires when a rule is removed.
         *
         * Listener arguments:
         * rule - {OpenLayers.Rule} The rule removed.
         */
        "ruleremoved",

        /**
         * Event: ruleupdated
         * Fires when a rule is modified.
         *
         * Listener arguments:
         * rule - {OpenLayers.Rule} The rule modified.
         */
        "ruleupdated"

    );
    Styler.dispatch(
        [
            function(done) {
                Ext.onReady(function() {
                    this.createLayout();
                    done();
                }, this);
            },
            function(done) {
                this.getCapabilities(done);
            }
        ],
        function() {
            this.createLayers();
            this.getSchemas(this.initEditor.createDelegate(this));
        },
        this
    );
};

Ext.extend(Styler, Ext.util.Observable, {
    
    map: null,
    wmsLayerList: null,
    wfsLayerList: null,
    layerList: null,
    currentLayer: null,
    sldManager: null,
    schemaManager: null,
    symbolTypes: null,
    ruleDlg: null,
    featureDlg: null,
    getFeatureControl: null,
    saving: null,
    windowPositions: {featureDlg: {}, ruleDlg: {}},
    
    /**
     * Method: getCapabilities
     * Fetch WMS and WFS capabilities and merge the two into a single layer
     *     list.
     *
     * Parameters:
     * callback - {Function} Function to be called when merge is complete.
     */
    getCapabilities: function(callback) {
        Styler.dispatch(
            [
                function(done) {
                    this.getWMSCapabilities(done);
                },
                function(done) {
                    this.getWFSCapabilities(done);
                }
            ],
            function() {
                this.mergeCapabilities();
                callback();
            },
            this
        );
    },

    /**
     * Method: getWMSCapabilities
     */
    getWMSCapabilities: function(callback) {
        Ext.Ajax.request({
            url: "/geoserver/wms?",
            method: "GET",
            disableCaching: false,
            success: this.parseWMSCapabilities,
            failure: function() {
                throw(OpenLayers.i18n("Unable to read capabilities from WMS"));
            },
            params: {
                VERSION: "1.1.1",
                REQUEST: "GetCapabilities"
            },
            options: {callback: callback},
            scope: this
        });
    },
    
    /**
     * Method: getWMSCapabilities
     */
    getWFSCapabilities: function(callback) {
        Ext.Ajax.request({
            url: "/geoserver/wfs?",
            method: "GET",
            disableCaching: false,
            success: this.parseWFSCapabilities,
            failure: function() {
                throw(OpenLayers.i18n("Unable to read capabilities from WFS"));
            },
            params: {
                VERSION: "1.1.0",
                REQUEST: "GetCapabilities"
            },
            options: {callback: callback},
            scope: this
        });
    },
    
    /**
     * Method: parseWMSCapabilities
     */
    parseWMSCapabilities: function(response, request) {
        var capabilities = new OpenLayers.Format.WMSCapabilities().read(
            response.responseXML.documentElement ?
            response.responseXML : response.responseText);
        this.wmsLayerList = capabilities.capability.layers;
        request.options.callback();
    },

    /**
     * Method: parseWFSCapabilities
     */
    parseWFSCapabilities: function(response, request) {
        var capabilities = new OpenLayers.Format.WFSCapabilities().read(
            response.responseXML.documentElement ?
            response.responseXML : response.responseText);
        this.wfsLayerList = capabilities.featureTypeList.featureTypes;
        request.options.callback();
    },
    
    /**
     * Method: mergeCapabilities
     * Given layer lists from WMS and WFS capabilities docs, create a single
     *     layer list with entires that appear in both.
     */
    mergeCapabilities: function() {
        this.layerList = [];
        var layer, name;
        var i = 0;
        var j = 0;
        for(i=0; i<this.wmsLayerList.length; ++i) {
            layer = this.wmsLayerList[i];
            name = layer.name;
            for(j=0; j<this.wfsLayerList.length; ++j) {
                if(this.wfsLayerList[j].name === name) {
                    this.layerList.push(layer);
                    break;
                }
            }
        }
    },
    
    /**
     * Method: createLayout
     * Create the layout with a map panel, a layers panel, and a legend panel.
     */
    createLayout: function() {

        this.getFeatureControl = new OpenLayers.Control.GetFeature({});
        this.getFeatureControl.events.on({
            "featureselected": function(e) {
                this.showFeatureInfo(this.currentLayer, e.feature);
            },
            scope: this
        });

        this.mapPanel = new GeoExt.widgets.map.MapPanel({
            border: true,
            region: "center",
            mapOptions: {
                controls: [
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.PanPanel()
                ],
                projection: new OpenLayers.Projection("EPSG:900913"),
                units: "m",
                theme: null,
                maxResolution: 156543.0339,
                maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34,
                                                 20037508.34, 20037508.34)
            },
            controls: [this.getFeatureControl]
        });

        this.legendContainer = new Ext.Panel({
            title: "Legend",
            height: 200,
            autoScroll: true,
            items: [{html: ""}],
            bbar: [{
                text: OpenLayers.i18n("Add new"),
                iconCls: "add",
                disabled: true,
                handler: function() {
                    var panel = this.getLegend();
                    var symbolizer = {};
                    symbolizer[panel.symbolType] =
                        OpenLayers.Format.SLD.v1.prototype.defaultSymbolizer; 
                    var rule = new OpenLayers.Rule({
                        symbolizer: symbolizer
                    });
                    panel.rules.push(rule);
                    this.fireEvent("ruleadded", rule);
                    this.showRule(this.currentLayer, rule, panel.symbolType, function() {
                        if(!this.saving) {
                            panel.rules.remove(rule);
                            this.fireEvent("ruleremoved", rule);
                        }
                    });
                },
                scope: this
            }, {
                text: OpenLayers.i18n("Delete selected"),
                iconCls: "delete",
                disabled: true,
                handler: function() {
                    var panel = this.getLegend();
                    var rule = panel.selectedRule;
                    var message = OpenLayers.i18n(
                        "styler.delete.rule",
                        {'NAME': panel.getRuleTitle(rule)}
                    );
                    Ext.Msg.confirm(
                            OpenLayers.i18n("Delete rule"),
                            message,
                            function(yesno) {
                        if(yesno === "yes") {
                            panel.rules.remove(rule);
                            this.fireEvent("ruleremoved", rule);
                            sldMgr = this.sldManager;
                            sldMgr.saveSld(this.currentLayer, function() {
                                this.ruleDlg.close();
                                this.repaint();
                            }, this);
                        }
                    }, this);
                },
                scope: this
            }]
        });
        
        this.layersContainer = new Ext.Panel({
            autoScroll: true,
            title: OpenLayers.i18n("Layers"),
            anchor: "100%, -200"
        });

        var westPanel = new Ext.Panel({
            border: true,
            layout: "anchor",
            region: "west",
            width: 250,
            split: true,
            collapsible: true,
            collapseMode: "mini",
            items: [
                this.layersContainer, this.legendContainer
            ]
        });

        var viewport = new Ext.Viewport({
            layout: "fit",
            hideBorders: true,
            items: {
                layout: "border",
                deferredRender: false,
                items: [this.mapPanel, westPanel]
            }
        });
        this.map = this.mapPanel.map;
    },
    
    /**
     * Method: createLayers
     * Given the merged layer list, create WMS layers and add them to the map.
     */
    createLayers: function() {
        var layerList = this.layerList;
        var num = layerList.length;
        var layers = new Array(num+1);
        var i = 0;
        layers[0] = new OpenLayers.Layer.Google(
            "Google Physical",
            {type: G_PHYSICAL_MAP, sphericalMercator: true}
        );
        for(i=0; i<num; ++i) {
            var maxExtent = OpenLayers.Bounds.fromArray(
                layerList[i].llbbox).transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    new OpenLayers.Projection("EPSG:900913"));
            layers[i+1] = new OpenLayers.Layer.WMS(
                layerList[i].title, "/geoserver/wms?", {
                    layers: layerList[i].name,
                    styles: layerList[i].styles[0].name,
                    transparent: true,
                    tiled: true,
                    tilesorigin: [
                        maxExtent.left,
                        maxExtent.bottom
                    ],
                    format: "image/png"
                }, {
                    isBaseLayer: false,
                    displayOutsideMaxExtent: true,
                    visibility: false,
                    alpha: OpenLayers.Util.alphaHack(), 
                    maxExtent: maxExtent
                }
            );
        }

        this.layerTree = new Ext.tree.TreePanel({
            border: false,
            loader: new Ext.tree.TreeLoader({
                clearOnLoad: true
            }),
            root: {
                nodeType: "async",
                children: [{
                    nodeType: "olOverlayLayerContainer",
                    text: OpenLayers.i18n("Layers"),
                    expanded: true
                }]
            },
            rootVisible: false,
            lines: false,
            listeners: {
                "checkchange": this.changeLayer,
                scope: this
            }
        });
        this.layersContainer.add(this.layerTree);
        this.layersContainer.doLayout();

        this.map.addLayers(layers);
        var slider = new Styler.ScaleSlider({
            plugins: new Styler.ScaleSliderTip(),
            vertical: true,
            height: 120,
            map: this.map
        }).addToMap();

        this.setCurrentLayer(this.map.layers[1]);
    },
    
    /**
     * Method: getSchemas
     * Request schemas for all layers.  Record the geometry attribute name and
     *     symbol type for each layer.
     */
    getSchemas: function(callback) {
        this.schemaManager = new Styler.SchemaManager(this.map);
        this.schemaManager.loadAll(callback);
    },
    
    /**
     * Method: getStyles
     * Create a new sld manager and initiate loading of all styles.  Call the
     *     callback provided when loading is complete.
     *
     * Parameters:
     * callback - {Function} Function to be called when SLD fetching & parsing
     *     is done.
     */
    getStyles: function(callback) {
        this.sldManager = new Styler.SLDManager(this.map);
        this.sldManager.loadAll(callback);
    },
    
    /**
     * Method: initEditor
     */
    initEditor: function() {
        this.symbolTypes = {};
        this.sldManager = new Styler.SLDManager(this.map);
        this.getFeatureControl.activate();
        this.setCurrentLayer(this.currentLayer);
        this.on({
            "ruleadded": function() {
                this.refreshLegend();
                this.refreshFeatureDlg();
            },
            "ruleremoved": function() {
                this.refreshLegend();
                this.refreshFeatureDlg();
            },
            "ruleupdated": function() {
                this.refreshLegend();
                this.refreshFeatureDlg();
            },
            "layerchanged": function(layer) {
                this.showLegend(layer);
            },
            scope: this
        });
        
        this.showLegend(this.currentLayer);

    },
    
    changeLayer: function(node, checked) {
        if(checked === true && this.currentLayer !== node.layer) {
            this.setCurrentLayer(node.layer);
        }
    },
    
    setCurrentLayer: function(layer) {
        if(layer !== this.currentLayer) {
            if(this.currentLayer) {
                this.currentLayer.setVisibility(false);
            }
            this.map.zoomToExtent(layer.maxExtent);
            this.currentLayer = layer;
            if(this.ruleDlg) {
                this.ruleDlg.destroy();
                delete this.ruleDlg;
            }
            if(this.featureDlg) {
                this.featureDlg.destroy();
                delete this.featureDlg;
            }
            
            this.fireEvent("layerchanged", this.currentLayer);

        }
        if(layer.getVisibility() === false) {
            layer.setVisibility(true);
        }
        // this is getting a bit sloppy - the remainder only works after initEditor
        // and require that setCurrentLayer be called again in initEditor
        if(this.getFeatureControl.active) {
            this.getFeatureControl.protocol = OpenLayers.Protocol.WFS.fromWMSLayer(
                layer, {geometryName: this.schemaManager.getGeometryName(layer)}
            );
        }
    },
    
    /**
     * Method: getRules
     */
    getRules: function(layer, callback) {
        var rules;
        var style = this.sldManager.getStyle(layer);
        if(style) {
            callback.call(this, style.rules);
        } else {
            this.sldManager.loadSld(
                layer,
                layer.params.STYLES,
                function(result) {
                    callback.call(this, result.style.rules);
                }.createDelegate(this)
            );
        }
    },
    
    /**
     * Method: showLegend
     * Initiate the sequence to show the legend for a layer.  Because the layer
     *     geometry type may not be known, the legend will not actually be shown
     *     until the geometry type is determined.  If the active layer changes
     *     before he legend is actually displayed, the sequence will be aborted.
     */
    showLegend: function(layer) {
        var old = this.legendContainer.items.length && this.legendContainer.getComponent(0);
        if(old) {
            this.getAddButton().disable();
            this.legendContainer.remove(old);
        }
        Styler.dispatch(
            [
                function(done, context) {
                    this.getSymbolType(layer, function(type) {
                        context.symbolType = type;
                        done();
                    });
                },
                function(done, context) {
                    this.getRules(layer, function(rules) {
                        context.rules = rules;
                        done();
                    });
                }
            ],
            function(context) {
                if(layer === this.currentLayer) {
                    this.addLegend(layer, context.rules, context.symbolType);
                }
            },
            this
        );
    },
    
    /**
     * Method: addLegend
     * Only called from <showLegend> if the active layer was not called while
     *     the layer symbol type or rules were being determined.
     */
    addLegend: function(layer, rules, type) {
        var deleteButton = this.getDeleteButton();
        var legend = new Styler.LegendPanel({
            rules: rules,
            symbolType: type,
            border: false,
            style: {padding: "10px"},
            selectOnClick: true,
            listeners: {
                "ruleselected": function(panel, rule) {
                    this.showRule(this.currentLayer, rule, panel.symbolType);
                    deleteButton.enable();
                },
                "ruleunselected": function(panel, rule) {
                    deleteButton.disable();
                },
                "rulemoved": function(panel, rule) {
                    legend.disable();
                    this.sldManager.saveSld(this.currentLayer, function() {
                        legend.enable();
                        this.repaint();
                    }, this);
                },
                scope: this
            }
        });
        this.legendContainer.add(legend);
        this.legendContainer.doLayout();
        this.getAddButton().enable();
    },
    
    /**
     * Method: refreshLegend
     * Redraw the legend if shown.
     */
    refreshLegend: function() {
        var legend = this.legendContainer.items.length && this.legendContainer.getComponent(0);
        if(legend) {
            legend.update();
        }
    },
    
    /**
     * Method: refreshFeatureDlg
     * Refresh the feature info shown in any feature dialog.
     */
    refreshFeatureDlg: function() {
        if(this.featureDlg && !this.featureDlg.hidden) {
            var feature = this.featureDlg.getFeature();
            this.showFeatureInfo(this.currentLayer, feature);
        }
    },
    
    /**
     * Method: setSymbolType
     * Set the symbol type for a layer given a feature.
     */
    setSymbolType: function(layer, type) {
        this.symbolTypes[layer.id] = type;
        return type;
    },
    
    /**
     * Method: getSymbolTypeFromFeature
     * Determine the symbol type given a feature.
     *
     * Parameters:
     * feature - {OpenLayers.Feature.Vector}
     *
     * Returns:
     * {String} The symbol type.
     */
    getSymbolTypeFromFeature: function(feature) {
        return feature.geometry.CLASS_NAME.replace(/OpenLayers\.Geometry\.(Multi)?|String/g, "");
    },
    
    /**
     * Method: getSymbolType
     * Get the symbol type for a layer.
     *
     * Parameters:
     * layer - {OpenLayers.Layer.WMS}
     * callback - {Function} Function to call when symbol type is determined.
     *     The callback will be called with the type as an argument.
     */
    getSymbolType: function(layer, callback) {
        var type = this.symbolTypes[layer.id];
        if(type) {
            callback.call(this, type);
        } else {
            type = this.schemaManager.getSymbolType(layer);
            if(type) {
                this.setSymbolType(layer, type);
                callback.call(this, type);
            } else {
                this.getOneFeature(layer, function(features) {
                    type = this.setSymbolType(layer, this.getSymbolTypeFromFeature(features[0]));
                    callback.call(this, type);
                });
            }
        }
    },
    
    showFeatureInfo: function(layer, feature) {
        if(this.featureDlg) {
            this.featureDlg.destroy();
        }
        
        this.getRules(layer, function(rules) {
            this.displayFeatureDlg(layer, feature, rules);
        });
    },
    
    displayFeatureDlg: function(layer, feature, rules) {
        
        // feature needs a layer to evaluate scale constraints
        feature.layer = layer;        
        var matchingRules = [];
        var rule;
        var i = 0;
        for(i=0; i<rules.length; ++i) {
            rule = rules[i];
            if(rule.evaluate(feature)) {
                matchingRules.push(rule);
            }
        }
        
        this.featureDlg = new Ext.Window({
            title: OpenLayers.i18n("styler.feature", 
                                   {'FEATURE': + feature.fid || feature.id}),
            layout: "fit",
            resizable: false,
            width: 220,
            x: this.windowPositions.featureDlg.x,
            y: this.windowPositions.featureDlg.y,
            items: [{
                hideBorders: true,
                border: false,
                autoHeight: true,
                items: [{
                    xtype: "gx_legendpanel",
                    title:
                        OpenLayers.i18n("Rules used to render this feature:"),
                    bodyStyle: {paddingLeft: "5px"},
                    symbolType: this.getSymbolTypeFromFeature(feature),
                    rules: matchingRules,
                    clickableSymbol: true,
                    listeners: {
                        "symbolclick": function(panel, rule) {
                            this.showRule(this.currentLayer,
                                rule, panel.symbolType);
                        },
                        scope: this
                    }
                }, {
                    xtype: "propertygrid",
                    title: OpenLayers.i18n("Attributes of this feature:"),
                    height: 120,
                    source: feature.attributes,
                    autoScroll: true,
                    listeners: {
                        "beforepropertychange": function() {
                            return false;
                        }
                    }
                }]
            }],
            listeners: {
                "move": function(cp, x, y) {
                    this.windowPositions.featureDlg = {x: x, y: y};
                },
                scope: this
            },
            getFeature: function() { return feature; }
        });
        
        this.featureDlg.show();
    },

    /**
     * Method: showRule
     * Show the rule dialog for a particular layer/rule combo.
     */
    showRule: function(layer, rule, symbolType, closeCallback) {
        var newRule = rule.clone();
        if(this.ruleDlg) {
            this.ruleDlg.destroy();
        }
        this.ruleDlg = new Ext.Window({
            title: OpenLayers.i18n(
                "styler.style",
                {'STYLE':
                    (rule.title || rule.name || OpenLayers.i18n("Untitled"))
                }),
            layout: "fit",
            x: this.windowPositions.ruleDlg.x,
            y: this.windowPositions.ruleDlg.y,
            width: 265,
            constrainHeader: true,
            items: [{
                xtype: "gx_rulepanel",
                autoHeight: false,
                autoScroll: true,
                rule: newRule,
                symbolType: Styler.Util.getSymbolTypeFromRule(rule),
                nestedFilters: false,
                scaleLevels: this.map.baseLayer.numZoomLevels,
                minScaleLimit: OpenLayers.Util.getScaleFromResolution(
                    this.map.baseLayer.resolutions[this.map.baseLayer.numZoomLevels-1],
                    this.map.units
                ),
                maxScaleLimit: OpenLayers.Util.getScaleFromResolution(
                    this.map.baseLayer.resolutions[0],
                    this.map.units
                ),
                scaleSliderTemplate:
                    OpenLayers.i18n("styler.div.zoomlevel") +
                    OpenLayers.i18n("styler.div.mapzoom"),
                modifyScaleTipContext: (function(panel, data) {
                    data.mapZoom = this.map.getZoom();
                }).createDelegate(this),
                attributes: new GeoExt.data.AttributeStore({
                    url: "/geoserver/wfs?",
                    baseParams: {
                        version: "1.1.1",
                        request: "DescribeFeatureType",
                        typename: layer.params.LAYERS
                    },
                    ignore: {name: this.schemaManager.getGeometryName(layer)}
                }),
                pointGraphics: [
                    {display: OpenLayers.i18n("Circle"), value: "circle", mark: true, preview: "theme/img/circle.gif"},
                    {display: OpenLayers.i18n("Square"), value: "square", mark: true, preview: "theme/img/square.gif"},
                    {display: OpenLayers.i18n("Triangle"), value: "triangle", mark: true, preview: "theme/img/triangle.gif"},
                    {display: OpenLayers.i18n("Star"), value: "star", mark: true, preview: "theme/img/star.gif"},
                    {display: OpenLayers.i18n("Cross"), value: "cross", mark: true, preview: "theme/img/cross.gif"},
                    {display: OpenLayers.i18n("X"), value: "x", mark: true, preview: "theme/img/x.gif"},
                    {display: OpenLayers.i18n("Custom...")}
                ]
            }],
            bbar: ["->", {
                text: OpenLayers.i18n("Cancel"),
                iconCls: "cancel",
                handler: function() {
                    this.ruleDlg.close();
                },
                scope: this
            }, {
                text: OpenLayers.i18n("Save"),
                iconCls: "save",
                handler: function() {
                    this.saving = true;
                    this.ruleDlg.disable();
                    this.updateRule(rule, newRule);
                    this.sldManager.saveSld(layer, function() {
                        this.ruleDlg.close();
                        this.repaint();
                        this.saving = false;
                    }, this);
                },
                scope: this
            }],
            listeners: {
                close: function() {
                    this.getLegend().unselect();
                    if(closeCallback) {
                        closeCallback.call(this);
                    }
                },
                move: function(cp, x, y) {
                    this.windowPositions.ruleDlg = {x: x, y: y};
                },
                scope: this
            }
        });
        this.ruleDlg.show();
    },
    
    /**
     * Method: updateRule
     * Update the title, symbolizer, filter, and scale constraints of an
     *     existing rule with properties from another rule.
     */
    updateRule: function(rule, newRule) {
        rule.title = newRule.title;
        rule.symbolizer = newRule.symbolizer;
        rule.filter = newRule.filter;
        rule.minScaleDenominator = newRule.minScaleDenominator;
        rule.maxScaleDenominator = newRule.maxScaleDenominator;
        this.fireEvent("ruleupdated", rule);
    },
    
    repaint: function () {
        this.currentLayer.redraw(true);
    },

    getOneFeature: function(layer, callback) {
        Ext.Ajax.request({
            url: "/geoserver/wfs?",
            method: "GET",
            disableCaching: false,
            params: {
                version: "1.0.0",
                request: "GetFeature",
                typeName: layer.params.LAYERS,
                maxFeatures: "1"
            },
            success: function(response) {
                var features = new OpenLayers.Format.GML().read(
                    response.responseXML || response.responseText);
                if(features.length) {
                    callback.call(this, features);
                } else {
                    throw(
                        OpenLayers.i18n("Could not load features from the WFS")
                    );
                }
            },
            failure: function(response) {
                throw(OpenLayers.i18n("Could not load features from the WFS"));
            },
            scope: this
        });
    },
        
    /**
     * Method: getLegend
     */
    getLegend: function() {
        return this.legendContainer.getComponent(0);
    },

    /**
     * Method: getAddButton
     */
    getAddButton: function() {
        return this.legendContainer.getBottomToolbar().items.get(0);
    },
    
    /**
     * Method: getDeleteButton
     */
    getDeleteButton: function() {
        return this.legendContainer.getBottomToolbar().items.get(1);
    }
});

// Global settings
OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;
