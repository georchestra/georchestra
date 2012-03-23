Ext.define('Analytics.view.BaseGridPanel', {
    extend: 'Ext.grid.Panel',
    requires: ['Ext.PagingToolbar'],
    
    forceFit: true,
    
    initComponent: function() {
        this.tools = [{
            type: 'save',
            tooltip: "Export CSV",
            handler: this.handleExport
        }];
        
        // Evolution: use http://docs.sencha.com/ext-js/4-0/#!/example/grid/infinite-scroll.html
        this.bbar = Ext.create('Ext.PagingToolbar', {
            store: this.getStore(),
            displayInfo: true,
            beforePageText: "Page",
            afterPageText: "sur {0}",
            displayMsg: 'Enregistrements {0} à {1} sur {2}',
            emptyMsg: "Aucun enregistrement"
        });
        
        this.callParent();
    }
});