Ext.define('Analytics.view.FilteredGeonetworkFiles', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredgeonetworkfileslist',
    store: 'FilteredGeonetworkFiles',
    
    initComponent: function() {
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'metadata_id',
                flex: 0, // will not be resized
                width: 100,
                text: 'Métadonnée',
                renderer: function(v) {
                    if (!v) return;
                    var url = '/geonetwork/?id='+v;
                    return '<a href="'+url+'" target="_blank">'+v+'</a>'
                }
            }, {
                dataIndex: 'filename',
                flex: 1, // will be resized
                width: 570,
                text: 'Fichier'
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