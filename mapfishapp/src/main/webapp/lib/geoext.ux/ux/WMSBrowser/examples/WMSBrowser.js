var WMSBrowser, mapPanel, browserWindow, tree, browserOptions = {}, serverStore;

Ext.onReady(function() {
    //GeoExt.Lang.set('fr');

    Ext.QuickTips.init();

    serverStore = new Ext.data.SimpleStore({
        fields: ['url'],
        data : [
            ['./data/wmscap.xml']
            ,['./data/dev4g.mapgears.com_cgi-bin_mswms_gmap.xml']
            ,['./data/dev4g.mapgears.com_cgi-bin_mswms_bdga.xml']
            ,['./data/dev4g.mapgears.com_cgi-bin_mswms_gmap_incompatible_srs.xml']
            ,['./data/dev4g.mapgears.com_cgi-bin_mswms_gmap_1.3.0.xml']
            //,['http://dev4g.mapgears.com/cgi-bin/mswms_gmap']
        ]
    });

    var actions = [];

    var action = new Ext.Action({
      text: 'WMSBrowser',
      handler: openWindow,
      scope: this,
      tooltip: OpenLayers.i18n('Add currently selected layers')
    });
    actions.push(action);

    var options = {
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        units: "m",
        numZoomLevels: 18,
        maxResolution: 156543.0339,
        maxExtent: new OpenLayers.Bounds(-20037508, -20037508,
                                         20037508, 20037508.34)
    };
    map = new OpenLayers.Map('map', options);

    mapPanel = new GeoExt.MapPanel({
        region: "center",
        layers: [new OpenLayers.Layer.OSM()],
        map: map,
        tbar: new Ext.Toolbar(actions)
    });

    tree = new Ext.tree.TreePanel({
        region: 'east',
        root: new GeoExt.tree.LayerContainer({
            text: 'Map Layers',
            layerStore: mapPanel.layers,
            leaf: false,
            expanded: true
        }),
        enableDD: true,
        width: 170
    });

    new Ext.Panel({
        renderTo: "content",
        layout: "border",
        width: 570,
        height: 350,
        items: [mapPanel, tree]
    });

    map.setCenter(new OpenLayers.LonLat(-10762333.581055,5968203.1676758), 2);
});

var openWindow = function() {
    if(!browserWindow) {
        var myBrowserOptions = Ext.apply(browserOptions, {
            border: false,
            region: "east",
            zoomOnLayerAdded: false,
            closeOnLayerAdded: false,
            allowInvalidUrl: true,
            alertPopupTimeout: 2000,
            // === proxyHost === uncomment to use the local proxy
            //proxyHost: "./WMSBrowserProxy.php?url=",
            serverStore: serverStore,
            mapPanelPreviewOptions: {height: 170, collapsed: false},
            layerStore: mapPanel.layers
        });

        WMSBrowser = new GeoExt.ux.WMSBrowser(myBrowserOptions);

        browserWindow = new Ext.Window({
            resizable: true,
            modal: false,
            closeAction: 'hide',
            width: 550,
            height: 450,
            title: OpenLayers.i18n("WMSBrowser"),
            layout: 'fit',
            items: [WMSBrowser]
        });
    }

    browserWindow.show();
};
