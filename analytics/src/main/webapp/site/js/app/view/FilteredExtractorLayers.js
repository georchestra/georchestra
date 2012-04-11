Ext.define('Analytics.view.FilteredExtractorLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredextractorlayerslist',
    store: 'FilteredExtractorLayers',

    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'ows_type',
                flex: 0, // will not be resized
                width: 100,
                text: 'Service OGC'
            }, {
                dataIndex: 'ows_url',
                flex: 1, // will be resized
                width: 200,
                text: 'URL du service'
            }, {
                dataIndex: 'layer_name',
                flex: 1, // will be resized
                width: 370,
                text: 'Couche'
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