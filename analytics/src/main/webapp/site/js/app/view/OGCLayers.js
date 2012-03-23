Ext.define('Analytics.view.OGCLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.ogclayerslist',
    store: 'OGCLayers',

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