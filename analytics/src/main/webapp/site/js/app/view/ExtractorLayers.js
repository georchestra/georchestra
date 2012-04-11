Ext.define('Analytics.view.ExtractorLayers', {
    extend: 'Analytics.view.FilteredExtractorLayers',
    alias: 'widget.extractorlayerslist',
    store: 'ExtractorLayers',

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
        Ext.getStore('FilteredExtractorUsers').load({
            filters: [{
                property: 'layer_name',
                value: rec.get('layer_name')
            }]
        });
        new Ext.Window({
            title: 'Utilisateurs ayant téléchargé la couche '+
                rec.get('layer_name'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredExtractorUsers', {
                border: false
            })]
        }).show();
    }
});