Ext.application({
    name: 'KitchenSink',

    controllers: [
        'Main'
    ],

    launch: function() {
        Ext.create('KitchenSink.view.Viewport');
    }
});
