Ext.define('Analytics.view.FilteredGeonetworkUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredgeonetworkuserslist',
    store: 'FilteredGeonetworkUsers',

    initComponent: function() {
        var tr = Lang.i18n;
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'username',
                flex: 1, // will be resized
                width: 700, // mandatory with ext 4.1 rc1 (should not be)
                text: tr('Name')
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
