Ext.define('Analytics.view.GeonetworkFiles', {
    extend: 'Analytics.view.FilteredGeonetworkFiles',
    alias: 'widget.geonetworkfileslist',
    store: 'GeonetworkFiles',

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
        var st = Ext.getStore('FilteredGeonetworkUsers');
        st.filters.clear();       
        st.filter([{
            property: 'filename',
            value: rec.get('filename')
        }, {
            property: 'metadata_id',
            value: rec.get('metadata_id')
        }]);

        new Ext.Window({
            title: [
                tr('The last users who downloaded the file'),
                rec.get('filename'),
                tr('the metadata'),
                rec.get('metadata_id')
            ].join(' '),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkUsers', {
                border: false
            })]
        }).show();
    }
});
