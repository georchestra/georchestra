Ext.define('Analytics.view.FilteredExtractorLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredextractorlayerslist',
    store: 'FilteredExtractorLayers',

    initComponent: function() {
        var tr = Lang.i18n;
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'ows_type',
                flex: 0, // will not be resized
                width: 100,
                text: tr('OGC Service')
            }, {
                dataIndex: 'ows_url',
                flex: 1, // will be resized
                width: 200,
                text: tr('Service URL')
            }, {
                dataIndex: 'layer_name',
                flex: 1, // will be resized
                width: 370,
                text: tr('Layer')
            }, {
                dataIndex: 'count',
                flex: 0, // will not be resized
                width: 130,
                text: tr('Number of requests')
            }]
        });
        
        this.callParent();
    }
});