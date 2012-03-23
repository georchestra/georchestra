Ext.define('Analytics.view.GeonetworkUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.geonetworkuserslist',
    store: 'GeonetworkUsers',

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