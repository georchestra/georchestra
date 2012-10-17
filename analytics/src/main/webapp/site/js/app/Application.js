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
    'Analytics.store.OGCGroups',
    'Analytics.store.OGCUsers',
    'Analytics.store.GeonetworkFiles',
    'Analytics.store.GeonetworkUsers',
    'Analytics.store.GeonetworkGroups',
    'Analytics.store.ExtractorLayers',
    'Analytics.store.ExtractorGroups',
    'Analytics.store.ExtractorUsers'
]);

Ext.application({
    name: 'Analytics',
    appFolder:'resources/site/js/app', // strange that it needs to be here // kind of redundant with the above Ext.Loader paths
    autoCreateViewport: true, // By setting autoCreateViewport to true, the framework will, by convention, include the app/view/Viewport.js file
    models: ['OGCLayer','OGCUser','OGCGroup','GeonetworkFile','GeonetworkUser','GeonetworkGroup','ExtractorLayer','ExtractorUser','ExtractorGroup'],
    stores: [
        'OGCLayers','OGCUsers','OGCGroups',
        'GeonetworkFiles','GeonetworkUsers','GeonetworkGroups',
        'ExtractorLayers','ExtractorUsers', 'ExtractorGroups',
        'FilteredOGCLayers','FilteredOGCUsers',
        'FilteredGeonetworkFiles','FilteredGeonetworkUsers',
        'FilteredExtractorLayers','FilteredExtractorUsers'
    ],
    controllers: ['Geonetwork', 'Extractor', 'OGC', 'Month'],
    launch: function() {
        // TODO
    }
});