Ext.define('KitchenSink.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: ['KitchenSink.view.List'],
    
    layout: 'border',
    
    items: [
        {
            region: 'north',
            xtype : 'header'
        },
        
        {
            region: 'center',
            
            layout: {
                type : 'hbox',
                align: 'stretch'
            },
            
            items: [
                {
                    width: 250,
                    bodyPadding: 5,
                    xtype: 'exampleList'
                },
                
                {
                    cls: 'x-example-panel',
                    flex: 1,
                    title: '&nbsp;',
                    id   : 'examplePanel',
                    layout: 'fit',
                    bodyPadding: 0
                }
            ]
        },
        {
            xtype: 'header',
            region: 'south',
            height: 13
        }
    ]
});
