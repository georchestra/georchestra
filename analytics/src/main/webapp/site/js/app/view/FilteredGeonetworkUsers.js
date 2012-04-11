Ext.define('Analytics.view.FilteredGeonetworkUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredgeonetworkuserslist',
    store: 'FilteredGeonetworkUsers',

    initComponent: function() {
        this.columns = [{
            dataIndex: 'username',
            text: 'Nom'
        }, {
            dataIndex: 'count',
            text: 'Nombre de requêtes'
        }];
        
        this.callParent();
    }
});
