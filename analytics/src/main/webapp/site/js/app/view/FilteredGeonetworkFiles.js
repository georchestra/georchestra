Ext.define('Analytics.view.FilteredGeonetworkFiles', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredgeonetworkfileslist',
    store: 'FilteredGeonetworkFiles',
    
    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'metadata_id',
                text: 'Métadonnée',
                renderer: function(v) {
                    if (!v) return;
                    var url = 'http://ids.pigma.org/geonetwork/srv/fr/metadata.show?id='+v; // TODO: config for base URL
                    return '<a href="'+url+'" target="_blank">'+v+'</a>'
                }
            }, {
                dataIndex: 'filename',
                text: 'Fichier'
            }, {
                dataIndex: 'count',
                text: 'Nombre de requêtes'
            }]
        });

        this.callParent();
    }

});