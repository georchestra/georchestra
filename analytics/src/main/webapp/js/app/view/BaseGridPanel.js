Ext.define('Analytics.view.BaseGridPanel', {
    extend: 'Ext.grid.Panel',
    requires: ['Ext.PagingToolbar', 'Ext.LoadMask'],
    border: true,
    forceFit: true,
    
    initComponent: function() {
        var tr = Analytics.Lang.i18n, 
            store = this.getStore();
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
                store: store,
                displayInfo: true,
                beforePageText: tr("Page"),
                afterPageText: tr("of N1"),
                displayMsg: tr('Records N0 to N1 of N2'),
                emptyMsg: tr("No records")
            }),
            listeners: {
                "itemdblclick": this.onItemDoubleClick,
                "afterrender": function() { 
                    this.loadingmask = new Ext.LoadMask(this, {
                        msg: tr("Loading..."), 
                        store: Ext.data.StoreManager.lookup(store) 
                    });
                },
                scope: this
            }
        });
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec, item, idx, e) {
        // nothing by default
    }
});