Ext.define('Analytics.view.OGCUsers', {
    extend: 'Analytics.view.FilteredOGCUsers',
    alias: 'widget.ogcuserslist',
    store: 'OGCUsers',
    
    initComponent: function() {
        // in order to have the tooltip over each row:
        this.columns = {
            defaults: {
                renderer: function(value, md){ 
                    md.tdAttr = 'data-qtip="double-cliquez pour afficher le détail"';
                    return value;
                }
            }
        };
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredOGCLayers').load({
            filters: [{
                property: 'user_name',
                value: rec.get('user_name')
            }]
        });
        new Ext.Window({
            title: "Couches ayant été téléchargées par l'utilisateur "+
                rec.get('user_name'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCLayers', {
                border: false
            })]
        }).show();
    }
});