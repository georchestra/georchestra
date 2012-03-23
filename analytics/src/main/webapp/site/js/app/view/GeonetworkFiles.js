Ext.define('Analytics.view.GeonetworkFiles', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.geonetworkfileslist',
    store: 'GeonetworkFiles',
 
    initComponent: function() {
        this.columns = [{
            dataIndex: 'metadata_id',
            text: 'Métadonnée',
            renderer: function(v) {
                if (!v) return;
                var url = 'http://ids.pigma.org/geonetwork/srv/fr/metadata.show?uuid='+v; // TODO: config for base URL
                return '<a href="'+url+'" target="_blank">'+v+'</a>'
            }
        }, {
            dataIndex: 'filename',
            text: 'Fichier'
        }, {
            dataIndex: 'count',
            text: 'Nombre de requêtes'
        }];
        
        this.callParent();
    }
});