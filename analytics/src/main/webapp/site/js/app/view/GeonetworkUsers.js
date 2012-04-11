Ext.define('Analytics.view.GeonetworkUsers', {
    extend: 'Analytics.view.FilteredGeonetworkUsers',
    alias: 'widget.geonetworkuserslist',
    store: 'GeonetworkUsers',

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
        Ext.getStore('FilteredGeonetworkFiles').load({
            filters: [{
                property: 'username',
                value: rec.get('username')
            }]
        });
        
        new Ext.Window({
            title: "Fichiers téléchargés par l'utilisateur " +
                rec.get('username'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkFiles', {
                border: false
            })]
        }).show();
    }
});

