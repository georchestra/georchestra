var redLiningPanel, mapPanel;

Ext.onReady(function() {
    OpenLayers.Lang.setCode('fr');

    OpenLayers.Request.GET({
        url: "../manifest.json",
        async: false,
        success: function(response) {
            var o = (new OpenLayers.Format.JSON()).read(response.responseText);
            OpenLayers.Lang.fr =
                OpenLayers.Util.extend(OpenLayers.Lang.fr, o.i18n.fr);
        }
    });

    Ext.QuickTips.init();

    mapPanel = new GeoExt.MapPanel({
        region: "center",
        layers: [new OpenLayers.Layer.OSM()],
        map: {
            allOverlays: false,
            maxExtent: new OpenLayers.Bounds(
                -128 * 156543.0339,
                -128 * 156543.0339,
                128 * 156543.0339,
                128 * 156543.0339
            ),
            maxResolution: 156543.0339,
            units: "m",
            projection: "EPSG:900913"
        },
        center: [-11685000, 4827000],
        zoom: 5
    });

    annotation = new GEOR.Annotation({
        //downloadService: 'http://localhost:5000/filemanager/download',
        map: mapPanel.map,
        popupOptions: {unpinnable: false, draggable: true}
    });

    new Ext.Panel({
        renderTo: "content",
        layout: "border",
        width: 650,
        height: 350,
        items: [mapPanel]
    });

    var win = new Ext.Window({
        title: OpenLayers.i18n('Drawing tools'),
        width: 420,
        closable: false,
        resizable: false,
        border: false,
        shadow: true,
        cls: 'annotation',
        items: [{
            xtype: 'toolbar',
            border: false,
            items: annotation.actions
        }]
    });
    win.show();
    win.alignTo(
        GeoExt.MapPanel.guess().body,
        "t-t",
        [0, 5],
        true
    );
});
