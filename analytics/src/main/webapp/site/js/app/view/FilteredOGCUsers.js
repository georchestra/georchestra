Ext.define('Analytics.view.FilteredOGCUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogcuserslist',
    store: 'FilteredOGCUsers',
    
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