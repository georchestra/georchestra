Ext.define('Analytics.view.ExtractorUsers', {
    extend: 'Analytics.view.FilteredExtractorUsers',
    alias: 'widget.extractoruserslist',
    store: 'ExtractorUsers',

    initComponent: function() {
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec) {
        Ext.getStore('FilteredExtractorLayers').load({
            filters: [{
                property: 'username',
                value: rec.get('username')
            }]
        });
        new Ext.Window({
            title: "Couches ayant été téléchargées par l'utilisateur "+
                rec.get('username'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredExtractorLayers', {
                border: false
            })]
        }).show();
    }
});