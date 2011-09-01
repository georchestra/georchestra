/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/FilterBuilder.js
 * @include Styler/widgets/FeatureRenderer.js
 * @include Styler/widgets/ScaleLimitPanel.js
 * @include Styler/widgets/PointSymbolizer.js
 * @include Styler/widgets/LineSymbolizer.js
 * @include Styler/widgets/PolygonSymbolizer.js
 * @include Styler/widgets/TextSymbolizer.js
 */

Ext.namespace("Styler");
Styler.RulePanel = Ext.extend(Ext.TabPanel, {
    
    /**
     * Property: symbolType
     * {String} One of "Point", "Line", or "Polygon".  Default is "Point".
     */
    symbolType: "Point",

    /**
     * Property: rule
     * {OpenLayers.Rule} Optional rule provided in the initial configuration.
     */
    rule: null,
    
    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} A configured attributes store for use in
     *     the filter property combo.
     */
    attributes: null,
    
    /**
     * Property: attributesComboConfig
     * {Object} optional config options for the attributes combobox
     */
    attributesComboConfig: null,
    
    /**
     * Property: pointGraphics
     * {Array} A list of objects to be used as the root of the data for a
     *     JsonStore.  These will become records used in the selection of
     *     a point graphic.  If an object in the list has no "value" property,
     *     the user will be presented with an input to provide their own URL
     *     for an external graphic.  By default, names of well-known marks are
     *     provided.  In addition, the default list will produce a record with
     *     display of "external" that create an input for an external graphic
     *     URL.
     *
     * Fields:
     * display - {String} The name to be displayed to the user.
     * preview - {String} URL to a graphic for preview.
     * value - {String} Value to be sent to the server.
     * mark - {Boolean} The value is a well-known name for a mark.  If false,
     *     the value will be assumed to be a url for an external graphic.
     */

    /**
     * Property: nestedFilters
     * {Boolean} Allow addition of nested logical filters.  This sets the
     *     allowGroups property of the filter builder.  Default is true.
     */
    nestedFilters: true,
    
    /**
     * Property: minScaleLimit
     * {Number} Lower limit for scale denominators.  Default is what you get when
     *     you assume 20 zoom levels starting with the world in Spherical
     *     Mercator on a single 256 x 256 tile at zoom 0 where the zoom factor
     *     is 2.
     */
    minScaleLimit: Math.pow(0.5, 19) * 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,

    /**
     * Property: maxScaleLimit
     * {Number} Lower limit for scale denominators.  Default is what you get
     *     when you project the world in Spherical Mercator onto a single
     *     256 x 256 pixel tile and assume OpenLayers.DOTS_PER_INCH (this
     *     corresponds to zoom level 0 in Google Maps).
     */
    maxScaleLimit: 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,
    
    /**
     * Property: scaleLevels
     * {Number} Number of scale levels to assume.  This is only for scaling
     *     values exponentially along the slider.  Scale values are not
     *     required to one of the discrete levels.  Default is 20.
     */
    scaleLevels: 20,
    
    /**
     * Property: scaleSliderTemplate
     * {String} Template for the tip displayed by the scale threshold slider.
     *
     * Can be customized using the following keywords in curly braces:
     * zoom - the zoom level
     * scale - the scale denominator
     * type - "Min" or "Max"
     *
     * Default is "{type} Scale 1:{scale}".
     */
    scaleSliderTemplate: "Echelle {type} 1:{scale}",
    
    /**
     * Method: modifyScaleTipContext
     * Called from the multi-slider tip's getText function.  The function
     *     will receive two arguments - a reference to the panel and a data
     *     object.  The data object will have scale, zoom, and type properties
     *     already calculated.  Other properties added to the data object
     *     are available to the <scaleSliderTemplate>.
     */
    modifyScaleTipContext: Ext.emptyFn,

    initComponent: function() {
        
        var defConfig = {
            plain: true,
            border: false
        };
        Ext.applyIf(this, defConfig);
        
        if(!this.rule) {
            this.rule = new OpenLayers.Rule({
                name: this.uniqueRuleName()
            });
        }
        
        this.activeTab = 0;
        
        this.textSymbolizer = new Styler.TextSymbolizer({
            symbolizer: this.rule.symbolizer["Text"],
            attributes: this.attributes,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        });
        
        var maxScaleDenominator, maxScaleLimit;
        var minScaleDenominator, minScaleLimit;
        maxScaleDenominator =
            this.rule.maxScaleDenominator != null ?
                this.rule.maxScaleDenominator : undefined;
        maxScaleLimit =
            this.maxScaleLimit != null ?
                this.maxScaleLimit : undefined;
        minScaleDenominator =
            this.rule.minScaleDenominator != null ?
                this.rule.minScaleDenominator : undefined;
        minScaleLimit =
            this.minScaleLimit != null ?
                this.minScaleLimit : undefined;

        this.scaleLimitPanel = new Styler.ScaleLimitPanel({
            maxScaleDenominator: maxScaleDenominator,
            maxScaleLimit: maxScaleLimit,
            limitMaxScale: !!maxScaleDenominator,
            minScaleDenominator: minScaleDenominator,
            minScaleLimit: minScaleLimit,
            limitMinScale: !!minScaleDenominator,
            scaleLevels: this.scaleLevels,
            scaleSliderTemplate: this.scaleSliderTemplate,
            modifyScaleTipContext: this.modifyScaleTipContext,
            listeners: {
                change: function(comp, min, max) {
                    this.rule.minScaleDenominator = min;
                    this.rule.maxScaleDenominator = max;
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        });
        
        this.filterBuilder = new Styler.FilterBuilder({
            allowGroups: this.nestedFilters,
            filter: this.rule && this.rule.filter,
            attributes: this.attributes,
            filterPanelOptions: {
                attributesComboConfig: this.attributesComboConfig || {}
            },
            listeners: {
                change: function(builder) {
                    var filter = builder.getFilter(); 
                    this.rule.filter = filter;
                    this.fireEvent("change", this, this.rule)
                },
                scope: this
            }
        });
        
        this.items = [{
            title: "Libellés",
            autoScroll: true,
            bodyStyle: {"padding": "10px"},
            items: [{
                xtype: "fieldset",
                title: "Libellés",
                autoHeight: true,
                checkboxToggle: true,
                collapsed: !this.rule.symbolizer["Text"],
                items: [
                    this.textSymbolizer
                ],
                listeners: {
                    collapse: function() {
                        delete this.rule.symbolizer["Text"];
                        this.fireEvent("change", this, this.rule);
                    },
                    expand: function() {
                        // in ext3 collapsed fieldset are not rendered
                        // so we have to call doLayout by hand when
                        // expanding the fieldset
                        this.textSymbolizer.doLayout();
                        this.rule.symbolizer["Text"] = this.textSymbolizer.symbolizer;
                        this.fireEvent("change", this, this.rule);
                    },
                    scope: this
                }
            }]
        }];
        if (Styler.Util.getSymbolTypeFromRule(this.rule) || this.symbolType) {
            this.items = [{
                title: "Simple",
                autoScroll: true,
                items: [this.createHeaderPanel(), this.createSymbolizerPanel()]
            }, this.items[0], {
                title: "Avancé",
                defaults: {
                    style: {
                        margin: "7px"
                    }
                },
                autoScroll: true,
                items: [{
                    xtype: "fieldset",
                    title: "Limite par échelle",
                    checkboxToggle: true,
                    collapsed: !(this.rule && (this.rule.minScaleDenominator || this.rule.maxScaleDenominator)),
                    autoHeight: true,
                    items: [this.scaleLimitPanel],
                    listeners: {
                        collapse: function(){
                            delete this.rule.minScaleDenominator;
                            delete this.rule.maxScaleDenominator;
                            this.fireEvent("change", this, this.rule)
                        },
                        expand: function(){
                            // in ext3 collapsed fieldset are not rendered
                            // so we have to call doLayout by hand when
                            // expanding the fieldset
                            this.scaleLimitPanel.doLayout();
                            if (this.scaleLimitPanel.scaleSlider) {
                                this.scaleLimitPanel.scaleSlider.syncThumb();
                            }
                            var changed = false;
                            if (this.scaleLimitPanel.limitMinScale) {
                                this.rule.minScaleDenominator = this.scaleLimitPanel.minScaleDenominator;
                                changed = true;
                            }
                            if (this.scaleLimitPanel.limitMaxScale) {
                                this.rule.maxScaleDenominator = this.scaleLimitPanel.maxScaleDenominator;
                                changed = true;
                            }
                            if (changed) {
                                this.fireEvent("change", this, this.rule)
                            }
                        },
                        scope: this
                    }
                }, {
                    xtype: "fieldset",
                    title: "Limite par condition",
                    checkboxToggle: true,
                    collapsed: !(this.rule && this.rule.filter),
                    autoHeight: true,
                    items: [this.filterBuilder],
                    listeners: {
                        collapse: function(){
                            delete this.rule.filter;
                            this.fireEvent("change", this, this.rule)
                        },
                        expand: function(){
                            var changed = false;
                            this.rule.filter = this.filterBuilder.getFilter();
                            this.fireEvent("change", this, this.rule)
                        },
                        scope: this
                    }
                }]
            }]
        };
        this.items[0].autoHeight = true;

        this.addEvents(
            /**
             * Event: change
             * Fires when any rule property changes.
             *
             * Listener arguments:
             * panel - {Styler.RulePanel} This panel.
             * rule - {OpenLayers.Rule} The updated rule.
             */
            "change"
        ); 
        
        this.on({
            tabchange: function(panel, tab) {
                tab.doLayout();
            },
            scope: this
        });

        Styler.RulePanel.superclass.initComponent.call(this);
    },
    
    /**
     * Method: uniqueRuleName
     * Generate a unique rule name.  This name will only be unique for this
     *     session assuming other names are created by the same method.  If
     *     name needs to be unique given some other context, override it.
     */
    uniqueRuleName: function() {
        return OpenLayers.Util.createUniqueID("rule_");
    },
    
    /**
     * Method: createHeaderPanel
     * Creates a panel config containing rule name, symbolizer, and scale
     *     constraints.
     */
    createHeaderPanel: function() {
        this.symbolizerSwatch = new Styler.FeatureRenderer({
            symbolType: this.symbolType,
            symbolizer: this.rule.symbolizer[this.symbolType],
            isFormField: true,
            fieldLabel: "Symbole"
        });
        return {
            xtype: "form",
            border: false,
            labelAlign: "top",
            defaults: {border: false},
            style: {"padding": "0.3em 0 0 1em"},
            items: [{
                layout: "column",
                defaults: {
                    border: false,
                    style: {"padding-right": "1em"}
                },
                items: [{
                    layout: "form",
                    width: 150,
                    items: [{
                        xtype: "textfield",
                        fieldLabel: "Nom",
                        anchor: "95%",
                        value: this.rule && (this.rule.title || this.rule.name || ""),
                        listeners: {
                            change: function(el, value) {
                                this.rule.title = value;
                                this.fireEvent("change", this, this.rule);
                            },
                            scope: this
                        }
                    }]
                }, {
                    layout: "form",
                    width: 70,
                    items: [this.symbolizerSwatch]
                }]
            }]
        };
    },

    /**
     * Method: createSymbolizerPanel
     */
    createSymbolizerPanel: function() {
        return {
            xtype: "gx_" + this.symbolType.toLowerCase() + "symbolizer",
            symbolizer: this.rule.symbolizer[this.symbolType],
            pointGraphics: (this.symbolType === "Point") ? this.pointGraphics : undefined,
            bodyStyle: {"padding": "10px"},
            border: false,
            labelWidth: 70,
            defaults: {
                labelWidth: 70
            },
            listeners: {
                change: function(symbolizer) {
                    this.symbolizerSwatch.setSymbolizer(symbolizer);
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        };
    }

});

Ext.reg('gx_rulepanel', Styler.RulePanel); 
