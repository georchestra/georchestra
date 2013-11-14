Ext.define('Analytics.view.GeonetworkUsers', {
    extend: 'Analytics.view.FilteredGeonetworkUsers',
    alias: 'widget.geonetworkuserslist',
    store: 'GeonetworkUsers',

    initComponent: function() {
        var tr = OpenLayers.i18n;
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
        var st = Ext.getStore('FilteredGeonetworkFiles');
        st.filters.clear();
        st.filter([{
            property: 'username',
            value: rec.get('username')
        }]);
        
        new Ext.Window({
            title: tr("Files downloaded by the user") +
                rec.get('username'),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkFiles', {
                border: false
            })]
        }).show();
    }
});

