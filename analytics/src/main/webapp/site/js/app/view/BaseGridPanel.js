Ext.define('Analytics.view.BaseGridPanel', {
    extend: 'Ext.grid.Panel',
    requires: ['Ext.PagingToolbar'],
    border: true,
    forceFit: true,
    
    initComponent: function() {
        Ext.apply(this, {
            tools: [{
                type: 'save',
                tooltip: "Export CSV",
                handler: this.handleExport
            }],
            selModel: {
                mode: 'single'
            },
            // Evolution: use http://docs.sencha.com/ext-js/4-0/#!/example/grid/infinite-scroll.html
            bbar: Ext.create('Ext.PagingToolbar', {
                store: this.getStore(),
                displayInfo: true,
                beforePageText: "Page",
                afterPageText: "sur {0}",
                displayMsg: 'Enregistrements {0} à {1} sur {2}',
                emptyMsg: "Aucun enregistrement"
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