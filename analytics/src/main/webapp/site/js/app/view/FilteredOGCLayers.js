Ext.define('Analytics.view.FilteredOGCLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogclayerslist',
    store: 'FilteredOGCLayers',

    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'service',
                flex: 0, // will not be resized
                width: 100,
                text: 'Service OGC'
            }, {
                dataIndex: 'layer',
                flex: 1, // will be resized
                width: 520, // mandatory with ext 4.1 rc1 (should not be)
                text: 'Couche'
            }, {
                dataIndex: 'request',
                flex: 1, // will be resized
                width: 150,
                text: 'Requête'
            }, {
                dataIndex: 'count',
                flex: 0, // will not be resized
                width: 130,
                text: 'Nombre de requêtes'
            }]
        });
        
        this.callParent();
    }
});