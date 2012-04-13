Ext.define('Analytics.view.ExtractorUsers', {
    extend: 'Analytics.view.FilteredExtractorUsers',
    alias: 'widget.extractoruserslist',
    store: 'ExtractorUsers',

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
        var st = Ext.getStore('FilteredExtractorLayers');
        st.filters.clear();
        st.filter([{
            property: 'username',
            value: rec.get('username')
        }]);
        
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