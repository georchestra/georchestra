Ext.define('Analytics.view.ExtractorLayers', {
    extend: 'Analytics.view.FilteredExtractorLayers',
    alias: 'widget.extractorlayerslist',
    store: 'ExtractorLayers',

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
        var st = Ext.getStore('FilteredExtractorUsers');
        st.filters.clear();
        st.filter([{
            property: 'ows_type',
            value: rec.get('ows_type')
        },{
            property: 'layer_name',
            value: rec.get('layer_name')
        },{
            property: 'ows_url',
            value: rec.get('ows_url')
        }]);
        
        new Ext.Window({
            title: [
                tr('The last users who downloaded the layer'),
                rec.get('ows_type'),
                rec.get('layer_name'),
                tr('service'),
                rec.get('ows_url')
            ].join(' '),
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredExtractorUsers', {
                border: false
            })]
        }).show();
    }
});