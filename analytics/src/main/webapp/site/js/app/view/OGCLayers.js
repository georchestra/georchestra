Ext.define('Analytics.view.OGCLayers', {
    extend: 'Analytics.view.FilteredOGCLayers',
    alias: 'widget.ogclayerslist',
    store: 'OGCLayers',

    initComponent: function() {
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredOGCUsers').load({
            filters: [{
                property: 'layer',
                value: rec.get('layer')
            }]
        });
        new Ext.Window({
            title: 'Utilisateurs ayant téléchargé la couche '+
                rec.get('layer'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCUsers', {
                border: false
            })]
        }).show();
    }
});