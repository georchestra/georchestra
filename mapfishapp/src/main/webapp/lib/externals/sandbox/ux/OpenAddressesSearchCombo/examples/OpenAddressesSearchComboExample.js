/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[GeoNamesSearchCombo]
 *  Geonames Search Combo
 *  ---------------------
 *  Combo to search data in GeoNames
 */

var mapPanel;

Ext.onReady(function() {
    var map = new OpenLayers.Map();
    var layer = new OpenLayers.Layer.OSM("OSM");
    map.addLayer(layer);

    var openAddressesSearchCombo = new GeoExt.ux.OpenAddressesSearchCombo({
        map: map,
        zoom: 15
    });

    new GeoExt.ux.OpenAddressesSearchCombo({
       map: map,
       zoom: 15,
       renderTo: 'OpenAddressesSearch'
    });

    mapPanel = new GeoExt.MapPanel({
        title: "GeoExt MapPanel with OpenAddresses search",
        renderTo: "mappanel",
        height: 400,
        width: 600,
        map: map,
        tbar: [openAddressesSearchCombo]
    });
});
