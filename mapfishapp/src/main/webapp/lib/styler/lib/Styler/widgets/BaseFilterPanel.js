/**
 * Copyright (c) 2009 Camptocamp
 */

Ext.namespace("Styler");
Styler.BaseFilterPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: filter
     * {OpenLayers.Filter} Optional non-logical filter provided in the initial
     *     configuration.  To retrieve the filter, use <getFilter> instead
     *     of accessing this property directly.
     */
    filter: null,

    initComponent: function() {
        
        var defConfig = {
            plain: true,
            layout: 'fit',
            border: false
        };
        Ext.applyIf(this, defConfig);
        
        if(!this.filter) {
            this.filter = this.createDefaultFilter();
        }

        this.items = this.createFilterItems();
        
        this.addEvents(
            /**
             * Event: change
             * Fires when the filter changes.
             *
             * Listener arguments:
             * filter - {OpenLayers.Filter} This filter.
             */
            "change"
        );

        Styler.BaseFilterPanel.superclass.initComponent.call(this);
    },
    
    /**
     * Method: createDefaultFilter
     * Overridden by children to change the default filter.
     *
     * Returns:
     * {OpenLayers.Filter}
     */
    createDefaultFilter: function() {
        return new OpenLayers.Filter();
    },
    
    /**
     * Method: createFilterItems
     * Creates a panel config containing filter parts.
     */
    createFilterItems: function() {
        return [];
    },
    
    /**
     * Method: tearDown
     * To be run before panel is removed from parent.
     *      May return false to cancel the remove
     */
    tearDown: function() {
        return true;
    }
    
});
