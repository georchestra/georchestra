/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler");
Styler.RuleChooser = Ext.extend(Ext.Panel, {

    featureRulesTpl:
        '<h2>' +
        OpenLayers.i18n("Styling rules that apply for this feature") +
        '</h2>' +
        '<ul class="x-matching-rules"><tpl for="matchingRules"><li>' +
        '{[values.title || OpenLayers.i18n("Default")]}'+
        '</li></tpl></ul>',
    otherRulesTpl:
        '<h2>' + OpenLayers.i18n('{type} for the "{layer}" layer') +
        '</h2>' +
        '<ul class="x-other-rules"><tpl for="otherRules"><li>' +
        '{[values.title || OpenLayers.i18n("Default")]}'+
        '</li></tpl></ul>',
    newRuleTpl: '<h2 class="x-new-rule">' +
                OpenLayers.i18n('Create a new styling rule') + '</h2>',
    
    otherRules: null,
    matchingRules: null,
    
    feature: null,
    layer: null,
    rulesStore: null,

    initComponent: function() {
        this.addEvents("ruleselected", "newruleselected");
        
        this.on("render", function() {
            this.body.on("click", this.onClick, this);
        }, this);
    },
    
    setFeature: function(feature, layer) {
        this.feature = feature;
        this.layer = layer || feature.layer;
        this.updateRules();
    },
    
    setLayer: function(layer) {
        this.feature = null;
        this.layer = layer;
        this.updateRules();
    },

    updateRules: function() {
		var i = 0;
        //TODO change this when RulesStore is also capable of handling vector
        // layers
        var rules = this.rulesStore ?
            this.rulesStore.getStyle(this.layer).rules :
            this.layer.styleMap.styles["default"].rules;
        this.matchingRules = [];
        this.otherRules = [];
        for(i=0; i<rules.length; ++i) {
            rule = rules[i];
            if(this.feature && rule.evaluate(this.feature)) {
                this.matchingRules.push(rule);
            } else {
                this.otherRules.push(rule);
            }
        }
        var template;
        template = new Ext.XTemplate((this.feature ?
                this.featureRulesTpl : "") + this.otherRulesTpl + this.newRuleTpl);
                
        var data = {
            matchingRules: this.matchingRules,
            otherRules: this.otherRules,
            type: this.feature ?
                OpenLayers.i18n("Other styling rules") :
                OpenLayers.i18n("Styling rules"),
            layer: this.layer.title || this.layer.name
        };

        if(this.rendered) {
            template.overwrite(this.body, data);
        } else {
            this.html = template.applyTemplate(data);
        }
    },
    
    onClick: function(e) {
        var clicked = e.getTarget('li');
        var i = 0;
        if(clicked) {
            var matching = e.getTarget('ul.x-matching-rules');
            if(matching) {
                for(i=0; i<matching.childNodes.length; ++i) {
                    if(matching.childNodes[i] === clicked) {
                        break;
                    }
                }
                this.fireEvent("ruleselected", this.layer, this.matchingRules[i]);
            } else {
                var other = e.getTarget('ul.x-other-rules') || {childNodes: []};
                for(i=0; i<other.childNodes.length; ++i) {
                    if(other.childNodes[i] === clicked) {
                        break;
                    }
                }
                this.fireEvent("ruleselected", this.layer, this.otherRules[i]);
            }
        } else if(e.getTarget('.x-new-rule')) {
            this.fireEvent("newruleselected", this.layer);
        }
    }
});
