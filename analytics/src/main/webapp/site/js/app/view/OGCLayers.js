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
    	var st = Ext.getStore('FilteredOGCUsers');
    	st.filters.clear();
        st.filter([{
            property: 'layer',
            value: rec.get('layer')
        }, {
            property: 'service',
            value: rec.get('service')
        }, {
            property: 'request',
            value: rec.get('request')
        }]);
        
        new Ext.Window({
            title: [
                'Utilisateurs ayant téléchargé la couche',
                rec.get('layer'),
                'du service',
                rec.get('service')
            ].join(' '),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCUsers', {
                border: false
            })]
        }).show();
    }
});