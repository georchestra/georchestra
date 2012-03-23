Ext.Loader.setConfig({
    enabled: true,
    disableCaching: true, // TODO: set to false for PROD
    paths: {
        'Ext': 'resources/site/js/lib/external/ext/src'
    }
});

Ext.require([
    'Ext.grid.Panel',
    'Ext.data.Store',
    'Ext.layout.container.Border',
    'Ext.tab.Panel'
]);

Ext.application({
    name: 'Analytics',
    appFolder:'resources/site/js/app', // strange that it is not a config option for Ext.Loader path
    autoCreateViewport: true, // By setting autoCreateViewport to true, the framework will, by convention, include the app/view/Viewport.js file
    models: ['OGCLayer','OGCUser','GeonetworkFile','GeonetworkUser','ExtractorLayer','ExtractorUser'],
    stores: ['OGCLayers','OGCUsers','GeonetworkFiles','GeonetworkUsers','ExtractorLayers','ExtractorUsers'],
    controllers: ['Geonetwork', 'Extractor', 'OGC', 'Month'],
    launch: function() {
        // TODO
    }
});