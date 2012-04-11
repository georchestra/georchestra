Ext.define('Analytics.view.OGCUsers', {
    extend: 'Analytics.view.FilteredOGCUsers',
    alias: 'widget.ogcuserslist',
    store: 'OGCUsers',
    
    initComponent: function() {
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredOGCLayers').load({
            filters: [{
                property: 'username',
                value: rec.get('username')
            }]
        });
        new Ext.Window({
            title: "Couches ayant été téléchargées par l'utilisateur "+
                rec.get('username'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCLayers', {
                border: false
            })]
        }).show();
    }
});