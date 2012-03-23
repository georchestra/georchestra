Ext.define('Analytics.view.ExtractorLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.extractorlayerslist',
    store: 'ExtractorLayers',

    initComponent: function() {
        this.columns = [{
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
        }];
        
        this.callParent();
    }
});