Ext.define('Analytics.view.OGCUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.ogcuserslist',
    store: 'OGCUsers',
    
    initComponent: function() {
        this.columns = [{
            dataIndex: 'user_name',
            text: 'Nom'
        }, {
            dataIndex: 'count',
            text: 'Nombre de requêtes'
        }];
        
        this.callParent();
    }
});