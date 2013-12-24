Ext.define('Analytics.view.ExtractorUsers', {
    extend: 'Analytics.view.FilteredExtractorUsers',
    alias: 'widget.extractoruserslist',
    store: 'ExtractorUsers',

    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        // in order to have the tooltip over each row:
        this.columns = {
            defaults: {
                renderer: function(value, md){
                    var qtip=tr("double-click to see details");
                    md.tdAttr = 'data-qtip="'+qtip+'"';
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
            title: tr("The layers have been downloaded by the user")+" "+
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