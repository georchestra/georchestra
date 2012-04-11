Ext.define('Analytics.view.FilteredExtractorLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredextractorlayerslist',
    store: 'FilteredExtractorLayers',

    initComponent: function() {
        this.columns = [{
            dataIndex: 'service',
            text: 'Service OGC'
        }, {
            dataIndex: 'url',
            text: 'URL du service'
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