Ext.define('KitchenSink.view.examples.grids.GroupedGrid', {
    extend: 'KitchenSink.view.examples.Example',
    requires: ['KitchenSink.store.Restaurants', 'Ext.grid.feature.Grouping'],
    
    items: [
        {
            xtype: 'grid',
            
            title: 'Restaurants',
            frame: true,
            
            store: 'Restaurants',
            
            columns: [
                { text: 'Name', flex: 1, dataIndex: 'name' },
                { text: 'Cuisine', flex: 1, dataIndex: 'cuisine' }
            ],
            
            features: [
                Ext.create('Ext.grid.feature.Grouping',{
                    hideGroupedHeader: true,
                    groupHeaderTpl: 'Cuisine: {name} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
                })
            ]
        }
    ]
});
