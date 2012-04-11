Ext.define('Analytics.view.ExtractorLayers', {
    extend: 'Analytics.view.FilteredExtractorLayers',
    alias: 'widget.extractorlayerslist',
    store: 'ExtractorLayers',

    initComponent: function() {
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredExtractorUsers').load({
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
            items: [Ext.create('Analytics.view.FilteredExtractorUsers', {
                border: false
            })]
        }).show();
    }
});