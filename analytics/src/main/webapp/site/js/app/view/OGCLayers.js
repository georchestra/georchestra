Ext.define('Analytics.view.OGCLayers', {
    extend: 'Analytics.view.FilteredOGCLayers',
    alias: 'widget.ogclayerslist',
    store: 'OGCLayers',

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