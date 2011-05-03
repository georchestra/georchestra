/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/RulePanel.js
 */

Ext.namespace("Styler");
Styler.RuleBuilder = Ext.extend(Ext.TabPanel, {

    /**
     * Property: rules
     * {Array(OpenLayers.Rule)} Optional list of rules provided in the initial
     *     configuration.
     */
    rules: null,
    
    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} A configured attributes store for use in
     *     the filter property combo.
     */
    attributes: null,
    
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
    pointGraphics: undefined,

    /**
     * Property: nestedFilters
     * {Boolean} Allow addition of nested logical filters.  This sets the
     *     allowGroups property of the filter builder.  Default is true.
     */
    nestedFilters: true,
    
    /**
     * Property: minScaleLimit
     * {Number} Lower limit for scale denominators.  No default set here.
     *     Default is provided by the rule panel.
     */
    minScaleLimit: undefined,

    /**
     * Property: maxScaleLimit
     * {Number} Lower limit for scale denominators.  No default set here.
     *     Default is provided by the rule panel.
     */
    maxScaleLimit: undefined,
    
    /**
     * Property: defaultSymbolizers
     * {Object} Properties are symbolizer types ("Point", "Line", or "Polygon").
     *     Values are individual symbolizers.  Properties from these symbolizer
     *     will be applied to partial symbolizers provided on rules.  These
     *     should match the defaults applied by the server in the same case.
     *     
     * TODO: Confirm the precedence of externalGraphic over graphicName in the
     *     OpenLayers renderers and SLD parser.
     */
    defaultSymbolizers: {
        "Point": {
            graphicName: "square",
            pointRadius: 3,
            strokeColor: "#000000",
            strokeWidth: 1,
            strokeOpacity: 1,
            fillColor: "#808080",
            fillOpacity: 1
        },
        "Line": {
            strokeColor: "#000000",
            strokeWidth: 1,
            strokeOpacity: 1
        },
        "Polygon": {
            fillColor: "#808080",
            fillOpacity: 1
        }
    },
    
    enableTabScroll: true,

    initComponent: function() {
        
        var defConfig = {
            plain: true,
            border: false,
            deferredRender: false
        };
        Ext.applyIf(this, defConfig);
        
        if(!this.rules) {
            this.rules = [];
        }
        
        var numRules = this.rules.length;
        this.rulePanels = new Array(numRules);
        var rule;
        for(var i=0; i<numRules; ++i) {
            rule = this.rules[i];
            // apply default symbolizer properties
            // TODO: decide if we really want to extend the symbolizers this way
            // an alternative is to let the renderer apply defaults only for
            // rendering - but you'd want to be able to configure the defaults
            if(!rule.symbolizer) {
                rule.symbolizer = {};
            }
            rule.symbolizer[this.symbolType] = Ext.applyIf(
                rule.symbolizer && rule.symbolizer[this.symbolType] || {},
                this.defaultSymbolizers[this.symbolType]
            );
            this.rulePanels[i] = this.createRulePanel(this.rules[i]);
        }
        
        if(numRules) {
            this.activeTab = 0;
        }

        this.items = this.rulePanels;
        
        if(!this.bbar) {
            this.bbar = [{
                text: "add rule",
                iconCls: "add",
                handler: function() {
                    this.addRule();
                },
                scope: this
            }];
        }

        this.addEvents(
            /**
             * Event: change
             * Fires when any of the rules change.
             *
             * Listener arguments:
             * builder - {Styler.RuleBuilder} This builder.
             * rule - {OpenLayers.Rule} The changed rule.
             */
            "change",
            
            /**
             * Event: ruleadded
             * Fires when a rule is added.
             *
             * Listener arguments:
             * builder - {Styler.RuleBuilder} This builder.
             * rule - {OpenLayers.Rule} The added rule.
             */
            "ruleadded",

            /**
             * Event: rulearemoved
             * Fires when a rule is removed.
             *
             * Listener arguments:
             * builder - {Styler.RuleBuilder} This builder.
             * rule - {OpenLayers.Rule} The removed rule.
             */
            "ruleremoved"
            
        );
        
        this.on({
            remove: function(builder, panel) {
                this.removeRule(panel.rule);
            },
            tabchange: function(panel, tab) {
                tab.doLayout();
            },
            scope: this
        });
        
        Styler.RuleBuilder.superclass.initComponent.call(this);
    },
    
    /**
     * Method: addRule
     * Add a default rule and create a new rule panel.
     * 
     * Returns:
     * {OpenLayers.Rule} the added rule
     */
    addRule: function() {
        var nameAndTitle = this.uniqueNameAndTitle();
        var symbolizer = {};
        symbolizer[this.symbolType] = Ext.apply(
            {}, this.defaultSymbolizers[this.symbolType]
        );
        var rule = new OpenLayers.Rule({
            name: nameAndTitle[0],
            title: nameAndTitle[1],
            symbolizer: symbolizer
        });
        this.rules.push(rule);
        var panel = this.createRulePanel(rule);
        this.rulePanels.push(panel);
        this.add(panel);
        this.setActiveTab(panel);
        this.fireEvent("ruleadded", this, rule);
        this.fireEvent("change", this, rule);
        return rule;
    },
    
    /**
     * Method: removeRule
     * Called when a panel is removed.  Removes the rule from the list of rules.
     */
    removeRule: function(rule) {
        var index = this.rules.indexOf(rule);
        if(index >= 0) {
            var panel = this.rulePanels[index];
            this.rules.remove(rule);
            this.rulePanels.remove(panel);
            this.fireEvent("ruleremoved", this, rule);
            this.fireEvent("change", this, rule);
        }
    },
    
    uniqueNameAndTitle: function() {
        var nameNum = 0;
        var titleNum = 0;
        var rule, name, title, match;
        for(var i=0; i<this.rules.length; ++i) {
            rule = this.rules[i];
            name = rule.name;
            if(name) {
                match = name.match(/^rule_(\d+)/);
                if(match) {
                    nameNum = Math.max(nameNum, parseInt(match[1]));
                }
            }
            title = rule.title;
            if(title) {
                match = title.match(/^Untitled (\d+)/);
                if(match) {
                    titleNum = Math.max(titleNum, parseInt(match[1]));
                }
            }
        }
        return ["rule" + (nameNum + 1), "Untitled " + (titleNum + 1)];
    },
    
    /**
     * Method: showRule
     * Display the panel for a rule.
     */
    showRule: function(rule) {
        var index = this.rules.indexOf(rule);
        if(index >= 0) {
            this.setActiveTab(this.rulePanels[index]);
        }
    },
    
    /**
     * Method: createRulePanel
     * Generate a rule panel given a rule.
     *
     * Parameters:
     * rule - {OpenLayers.Rule} A rule
     *
     * Returns:
     * {Styler.RulePanel} A rule panel.
     */
    createRulePanel: function(rule) {
        return new Styler.RulePanel({
            title: rule.title || rule.name,
            closable: true,
            autoScroll: true,
            border: false,
            rule: rule,
            pointGraphics: this.pointGraphics,
            nestedFilters: this.nestedFilters,
            symbolType: this.symbolType,
            attributes: this.attributes,
            listeners: {
                change: function(panel, rule) {
                    panel.setTitle(rule.title || rule.name);
                    this.fireEvent("change", this, rule);
                },
                scope: this
            }
        });
    }


});

Ext.reg('gx_rulebuilder', Styler.RuleBuilder); 
