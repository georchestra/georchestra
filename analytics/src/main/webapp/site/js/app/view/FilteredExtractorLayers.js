Ext.define('Analytics.view.FilteredExtractorLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredextractorlayerslist',
    store: 'FilteredExtractorLayers',

    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'ows_type',
                text: 'Service OGC'
            }, {
                dataIndex: 'ows_url',
                text: 'URL du service'
            }, {
                dataIndex: 'layer_name',
                text: 'Couche'
            }, {
                dataIndex: 'count',
                text: 'Nombre de requêtes'
            }]
        });
        
        this.callParent();
    }
});