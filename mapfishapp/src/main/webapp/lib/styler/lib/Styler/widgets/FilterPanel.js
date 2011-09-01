/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @requires Styler/widgets/BaseFilterPanel.js
 * @include Styler/widgets/form/ComparisonComboBox.js
 * @include GeoExt/data/AttributeStore.js
 */

Ext.namespace("Styler");
Styler.FilterPanel = Ext.extend(Styler.BaseFilterPanel, {
    
    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} A configured attributes store for use in
     *     the filter property combo.
     */
    attributes: null,
    
    /**
     * Property: valueContainer
     * 
     */
    valueContainer: null,

    /**
     * Property: comparisonCombo
     * {Ext.form.ComboBox}
     */
    comparisonCombo: null,

    /**
     * Property: filterPanelOptions
     * {Object}
     */
    filterPanelOptions: null,

    initComponent: function() {
    
        if(!this.attributes) {
            this.attributes = new GeoExt.data.AttributeStore();
        }

        this.attributesComboConfig = {
            xtype: "combo",
            store: this.attributes,
            mode: 'local',
            editable: false,
            triggerAction: "all",
            hideLabel: true,
            allowBlank: false,
            blankText: "Ce champ est nécessaire",
            displayField: "name",
            valueField: "name",
            value: this.filter.property,
            listeners: {
                select: function(combo, record) {
                    this.adaptValueField(record);
                    this.filterComparisonBox(record);
                    this.filter.property = record.get("name");
                    this.fireEvent("change", this.filter);
                },
                scope: this
            }
        };
        if (this.filterPanelOptions) {
            if (this.filterPanelOptions.values) {
                this.storeUriProperty = this.filterPanelOptions.values.storeUriProperty;
                this.storeOptions = Ext.apply({}, this.filterPanelOptions.values.storeOptions);
                this.comboOptions = Ext.apply({}, this.filterPanelOptions.values.comboOptions);
            }
            if (this.filterPanelOptions.attributesComboConfig) {
                Ext.apply(this.attributesComboConfig, this.filterPanelOptions.attributesComboConfig);
            }
        }
        
        this.addEvents(
            /**
             * Event: loading
             * Fires when loading data from server
             */
            "loading",
            /**
             * Event: loaded
             * Fires when finished loading data from server
             */
            "loaded"
        );
        
        Styler.FilterPanel.superclass.initComponent.call(this);
    },
   
    /**
     * Method: setEqualComparison
     * set comparison to equal and fires change event
     *
     */
    setEqualComparison: function() {
        this.comparisonCombo.store.filterBy(function(record){
            return (
                (record.get('value') == OpenLayers.Filter.Comparison.EQUAL_TO) ||
                (record.get('value') == OpenLayers.Filter.Comparison.NOT_EQUAL_TO) 
            );
        });
        this.comparisonCombo.setValue(OpenLayers.Filter.Comparison.EQUAL_TO);
        this.filter.type = OpenLayers.Filter.Comparison.EQUAL_TO;
        this.fireEvent("change", this.filter);
    },

    /**
     * Method: adaptValueField
     * 
     *
     * Parameters:
     * record - {Ext.data.Record}
     */
    adaptValueField: function(record) {
        var previousCmp = this.valueContainer.items.items[0];
        var newCmpWidth = (previousCmp instanceof(Ext.form.ComboBox)) ? 
            previousCmp.getSize().width+17 : // 17 = trigger size
            previousCmp.getSize().width;
        
        var recordIsDate = (record.get('type')=='dateTime' || 
            record.get('type')=='date');
        
        var onChange = function(el, value) {
            if (value instanceof Ext.data.Record) {
                value = value.get('value');
            }
            if (recordIsDate) {
                var dt = new Date(value);
                value = dt.format('c');
            }
            this.filter.value = value;
            this.fireEvent("change", this.filter);
        };
        
        if (this.storeUriProperty && record.get(this.storeUriProperty)) {
            this.setEqualComparison();
            var store = new Ext.data.JsonStore(Ext.apply({
                url: record.get(this.storeUriProperty),
                listeners: {
                    "beforeload": function(store, options) {
                        this.fireEvent("loading");
                    },
                    "loadexception": function() {
                        this.fireEvent("loaded");
                    },
                    scope: this
                }
            }, this.storeOptions));

            var field = new Ext.form.ComboBox(Ext.apply({
                store: store,
                mode: 'local',
                width: newCmpWidth, 
                triggerAction: 'all',
                listeners: {
                    'select': onChange,
                    'change': onChange,
                    scope: this
                }
            }, this.comboOptions));
            
            store.on('load', function(store, records){
                field.setValue(records[0].get(this.comboOptions.valueField));
                this.fireEvent("loaded");
                field.fireEvent('change', field, field.getValue());
            }, this);
            
            store.load();
            
        } else if (recordIsDate) {
            var field = new Ext.form.DateField({
                width: newCmpWidth,
                value: '',
                format: 'd/m/Y',
                allowBlank: false,
                blankText: "Ce champ est nécessaire",
                listeners: {
                    'select': onChange,
                    'change': onChange,
                    scope: this
                }
            });
            
        } else {
            var field = new Ext.form.TextField({
                width: newCmpWidth,
                value: '',
                allowBlank: false,
                blankText: "Ce champ est nécessaire"
            });
            field.on('change', onChange, this);
        }

        this.valueContainer.remove(previousCmp);
        this.valueContainer.add(field);
        this.valueContainer.doLayout();
    }, 

    /**
     * Method: filterComparisonBox
     * filter comparison box according to attribute's type 
     *
     * Parameters:
     * record - {Ext.data.Record}
     */
    filterComparisonBox: function(record) {
        this.comparisonCombo.store.clearFilter();
        switch (record.get('type')) {
            case 'boolean':
                this.setEqualComparison();
                this.valueContainer.items.items[0].store.on('load', function() {
                    this.valueContainer.items.items[0].setValue(true);
                    this.filter.value = true;
                    this.fireEvent("change", this.filter);
                }, this);
                break;
            case 'string':
                this.comparisonCombo.store.filterBy(function(record){
                    return (
                        (record.get('value') == OpenLayers.Filter.Comparison.EQUAL_TO) ||
                        (record.get('value') == OpenLayers.Filter.Comparison.NOT_EQUAL_TO) ||
                        (record.get('value') == OpenLayers.Filter.Comparison.LIKE)
                    );
                });
                break;
            case 'integer':
            case 'int':
            case 'float':
                this.comparisonCombo.store.filterBy(function(record){
                    return (record.get('value') != OpenLayers.Filter.Comparison.LIKE);
                });
                break;
            case 'dateTime':
                this.comparisonCombo.store.filterBy(function(record){
                    return (
                        (record.get('value') != OpenLayers.Filter.Comparison.LIKE) &&
                        (record.get('value') != OpenLayers.Filter.Comparison.NOT_EQUAL_TO)
                    );
                });
                break;
            default:
                break;
        }
    },

    /**
     * Method: createDefaultFilter
     * May be overridden to change the default filter.
     *
     * Returns:
     * {OpenLayers.Filter} By default, returns a comarison filter.
     */
    createDefaultFilter: function() {
        return new OpenLayers.Filter.Comparison();
    },
    
    /**
     * Method: createFilterItems
     * Creates a panel config containing filter parts.
     */
    createFilterItems: function() {
        this.valueContainer = new Ext.Panel({
            columnWidth: 0.5,
            items: [{
                xtype: "textfield",
                value: this.filter.value,
                allowBlank: false,
                blankText: "Ce champ est nécessaire",
                listeners: {
                    change: function(el, value) {
                        this.filter.value = value;
                        this.fireEvent("change", this.filter);
                    },
                    scope: this
                }
            }],
            listeners: {
                'resize': function(p, newWidth) {
                    p.findByType("textfield")[0].setWidth(newWidth);
                }
            }
        });

        this.comparisonCombo = new Styler.form.ComparisonComboBox({
            value: this.filter.type,
            width: 50,
            blankText: "Ce champ est nécessaire",
            listeners: {
                select: function(combo, record) {
                    this.filter.type = record.get("value");
                    this.fireEvent("change", this.filter);
                },
                scope: this
            }
        });

        return [{
            layout: "column",
            border: false,
            height: 25,
            defaults: {border: false},
            items: [{
                items: [this.attributesComboConfig],
                columnWidth: 0.5,
                listeners: {
                    'resize': function(p, newWidth) {
                        p.findByType("combo")[0].setWidth(newWidth);
                    }
                }
            }, {
                width: 56,
                style: "padding: 0 3px;",
                items: [this.comparisonCombo]
            }, this.valueContainer]
        }];
    }
});

Ext.reg('gx_filterpanel', Styler.FilterPanel); 
