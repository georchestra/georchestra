Ext.Loader.setConfig({
    enabled: true,
    disableCaching: false,
    paths: {
        'Ext': 'resources/site/js/lib/external/ext/src',
        'Analytics': 'resources/site/js/app'
    }
});

Ext.require([
    'Ext.grid.Panel',
    'Ext.data.Store',
    'Ext.layout.container.Border',
    'Ext.tab.Panel',
    // 'Analytics.store.*' surprisingly does not work, so:
    'Analytics.store.OGCLayers',
    'Analytics.store.OGCUsers',
    'Analytics.store.GeonetworkFiles',
    'Analytics.store.GeonetworkUsers',
    'Analytics.store.ExtractorLayers',
    'Analytics.store.ExtractorUsers'
]);

Ext.application({
    name: 'Analytics',
    appFolder:'resources/site/js/app', // strange that it needs to be here // kind of redundant with the above Ext.Loader paths
    autoCreateViewport: true, // By setting autoCreateViewport to true, the framework will, by convention, include the app/view/Viewport.js file
    models: ['OGCLayer','OGCUser','GeonetworkFile','GeonetworkUser','ExtractorLayer','ExtractorUser'],
    stores: ['OGCLayers','OGCUsers','GeonetworkFiles','GeonetworkUsers','ExtractorLayers','ExtractorUsers'],
    controllers: ['Geonetwork', 'Extractor', 'OGC', 'Month'],
    launch: function() {
        // TODO
    }
});