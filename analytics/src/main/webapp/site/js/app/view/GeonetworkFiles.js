Ext.define('Analytics.view.GeonetworkFiles', {
    extend: 'Analytics.view.FilteredGeonetworkFiles',
    alias: 'widget.geonetworkfileslist',
    store: 'GeonetworkFiles',

    initComponent: function() {
        this.callParent();
    },

    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredGeonetworkUsers').load({
            filters: [{
                property: 'filename',
                value: rec.get('filename')
            }, {
                property: 'metadata_id',
                value: rec.get('metadata_id')
            }]
        });
        new Ext.Window({
            title: [
                'Utilisateurs ayant téléchargé le fichier ',
                rec.get('filename'),
                ' de la métadonnée ',
                rec.get('metadata_id')
            ].join(''),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkUsers', {
                border: false
            })]
        }).show();
    }
});
