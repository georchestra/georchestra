/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[BoundingBoxPanel]
 *  BoundingBox Panel
 *  ---------------------
 *  Panel to display bbox coordinate and keep in sync with a vector layer
 */

Ext.onReady(function() {
    var map = new OpenLayers.Map({
        controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.PanPanel(),
            new OpenLayers.Control.ZoomPanel()
        ]
    });
    var layer = new OpenLayers.Layer.OSM("OSM");
    var vectorLayer = new OpenLayers.Layer.Vector("Vector");
    map.addLayer(layer);
    map.addLayer(vectorLayer);

    var boundingBoxPanel = new GeoExt.ux.form.BoundingBoxPanel({
        title: "BoundingBoxPanel example",
        renderTo: "bboxpanel",
        vectorLayer: vectorLayer,
        width: 600
    });

    new GeoExt.MapPanel({
        title: "MapPanel",
        renderTo: "mappanel",
        height: 400,
        width: 600,
        id: "mappanel",
        map: map
    });
});
