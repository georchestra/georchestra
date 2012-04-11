Ext.define('Analytics.view.FilteredOGCLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogclayerslist',
    store: 'FilteredOGCLayers',

    initComponent: function() {
        this.columns = [{
            dataIndex: 'service',
            text: 'Service OGC'
        }, {
            dataIndex: 'layer',
            text: 'Couche'
        }, {
            dataIndex: 'count',
            text: 'Nombre de requêtes'
        }];
        
        this.callParent();
    }
});