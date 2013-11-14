Ext.define('Analytics.view.OGCLayers', {
    extend: 'Analytics.view.FilteredOGCLayers',
    alias: 'widget.ogclayerslist',
    store: 'OGCLayers',

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
    	var st = Ext.getStore('FilteredOGCUsers');
    	st.filters.clear();
        st.filter([{
            property: 'layer',
            value: rec.get('layer')
        }, {
            property: 'service',
            value: rec.get('service')
        }, {
            property: 'request',
            value: rec.get('request')
        }]);
        
        new Ext.Window({
            title: [
                tr('The last users who downloaded the layer'),
                rec.get('layer'),
                tr('service'),
                rec.get('service')
            ].join(' '),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCUsers', {
                border: false
            })]
        }).show();
    }
});