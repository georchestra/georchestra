Ext.define('Analytics.view.OGCUsers', {
    extend: 'Analytics.view.FilteredOGCUsers',
    alias: 'widget.ogcuserslist',
    store: 'OGCUsers',
    
    initComponent: function() {
        var tr = Lang.i18n;
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
    	var st = Ext.getStore('FilteredOGCLayers');
    	st.filters.clear();
    	st.filter([{
            property: 'user_name',
            value: rec.get('user_name')
        }]);
    	
        new Ext.Window({
            title: tr("The layers have been downloaded by the user ")+
                rec.get('user_name'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCLayers', {
                border: false
            })]
        }).show();
    }
});