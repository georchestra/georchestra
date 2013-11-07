Ext.onReady(function() {

    // required for tooltips
    Ext.QuickTips.init();

    var map = new OpenLayers.Map();

    var measureLength = new GeoExt.ux.MeasureLength({
        map: map,
        controlOptions: {
            geodesic: true
        },
        toggleGroup: 'tools'
    });

    var measureArea = new GeoExt.ux.MeasureArea({
        map: map,
        decimals: 0,
        toggleGroup: 'tools'
    });
        
    var mapPanel = new GeoExt.MapPanel({
        renderTo: "content",
        width: 800,
        height: 350,
        map: map,
        layers: [new OpenLayers.Layer.WMS("Global Imagery",
            "http://vmap0.tiles.osgeo.org/wms/vmap0",
            {layers: "basic"})],
        center: [16, 48],
        zoom: 5,
        tbar: [measureLength, measureArea]
    });
});
