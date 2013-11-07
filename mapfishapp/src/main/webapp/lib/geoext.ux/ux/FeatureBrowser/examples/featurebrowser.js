Ext.onReady(function() {

    // required for tooltips
    Ext.QuickTips.init();

    features = [
        new OpenLayers.Feature.Vector(null, {
            name: 'toto',
            age: '20',
            photo: 'http://cloud.ohloh.net/attachments/30093/GeoExt_med.png',
            type: 'employee'
        }),
        new OpenLayers.Feature.Vector(null, {
            foo: 'bar',
            dude: 'truite',
            type: 'thing'
        })
    ];

    var browser = new GeoExt.ux.FeatureBrowser({
        title: 'Feature Browser - no template',
        renderTo: Ext.getBody(),
        width: 200,
        height: 200,
        features: features,
        bodyStyle: "padding: 5px;"
    });

    var browser = new GeoExt.ux.FeatureBrowser({
        title: 'Feature Browser - \'type\' attribute dependent template',
        renderTo: Ext.getBody(),
        width: 200,
        height: 200,
        features: features,
        tplFeatureAttribute: 'type',
        tpl: {
            'employee': new Ext.Template(
                '<b>{name}</b>({age})<br />',
                '<img src="{photo}" />'
            )
        },
        bodyStyle: "padding: 5px;"
    });
    var browser = new GeoExt.ux.FeatureBrowser({
        title: 'Feature Browser - single feature',
        renderTo: Ext.getBody(),
        width: 200,
        height: 200,
        features: [features[0]],
        bodyStyle: "padding: 5px;"
    });
});
