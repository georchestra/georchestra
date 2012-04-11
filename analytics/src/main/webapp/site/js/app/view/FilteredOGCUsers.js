Ext.define('Analytics.view.FilteredOGCUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogcuserslist',
    store: 'FilteredOGCUsers',
    
    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'user_name',
                flex: 1, // will be resized
                width: 700, // mandatory with ext 4.1 rc1 (should not be)
                text: 'Nom'
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