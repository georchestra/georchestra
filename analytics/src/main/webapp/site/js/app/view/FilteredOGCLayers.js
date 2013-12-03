Ext.define('Analytics.view.FilteredOGCLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogclayerslist',
    store: 'FilteredOGCLayers',

    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'service',
                flex: 0, // will not be resized
                width: 100,
                text: tr('OGC Service')
            }, {
                dataIndex: 'layer',
                flex: 1, // will be resized
                width: 520, // mandatory with ext 4.1 rc1 (should not be)
                text: tr('Layer')
            }, {
                dataIndex: 'request',
                flex: 1, // will be resized
                width: 150,
                text: tr('Request')
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