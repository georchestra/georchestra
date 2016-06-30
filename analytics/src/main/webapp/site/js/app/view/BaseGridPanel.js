Ext.define('Analytics.view.BaseGridPanel', {
    extend: 'Ext.grid.Panel',
    requires: ['Ext.PagingToolbar'],
    border: true,
    forceFit: true,
    
    initComponent: function() {
        var tr = Lang.i18n;
        Ext.apply(this, {
            tools: [{
                type: 'save',
                tooltip: tr("Export to CSV")
            }],
            selModel: {
                mode: 'single'
            },
            // Evolution: use http://docs.sencha.com/ext-js/4-0/#!/example/grid/infinite-scroll.html
            bbar: Ext.create('Ext.PagingToolbar', {
                store: this.getStore(),
                displayInfo: true,
                beforePageText: tr("Page"),
                afterPageText: tr("of N1"),
                displayMsg: tr('Records N0 to N1 of N2'),
                emptyMsg: tr("No records")
            }),
            listeners: {
                "itemdblclick": this.onItemDoubleClick,
                scope: this
            }
        });
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec, item, idx, e) {
        // nothing by default
    }
});