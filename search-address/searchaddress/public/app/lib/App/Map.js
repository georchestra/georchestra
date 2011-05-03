/*
 * @include OpenLayers/Projection.js
 * @include OpenLayers/Map.js
 * @include OpenLayers/Layer/XYZ.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Control/PanZoom.js
 * @include OpenLayers/Control/ArgParser.js
 * @include OpenLayers/Control/Attribution.js
 * @include OpenLayers/Control/ScaleLine.js
 * @include OpenLayers/Control/OverviewMap.js
 * @include GeoExt/widgets/MapPanel.js
 * @include App/Tools.js
 */

Ext.namespace('App');

/**
 * Constructor: App.Map
 * Creates a {GeoExt.MapPanel} internally. Use the "mapPanel" property
 * to get a reference to the map panel.
 *
 * Parameters:
 * options - {Object} Options passed to the {GeoExt.MapPanel}.
 */
App.Map = function(options) {

    // Private

    /**
     * Method: getLayers
     * Returns the list of layers.
     *
     * Returns:
     * {Array({OpenLayers.Layer})} An array of OpenLayers.Layer objects.
     */
    var getLayers = function() {
        var osm = new OpenLayers.Layer.OSM();
        return [osm];
    };

    // Public

    Ext.apply(this, {

        /**
         * APIProperty: mapPanel
         * The {GeoExt.MapPanel} instance. Read-only.
         */
        mapPanel: null
    });

    // Main

    // create map
    var mapOptions = {
        projection: new OpenLayers.Projection("EPSG:900913"),
        maxExtent: new OpenLayers.Bounds(
            -20037508.34, 
            -20037508.34,
            20037508.34, 
            20037508.34
        ),
        restrictedExtent: new OpenLayers.Bounds(
            275784,
            5444704,
            972278,
            5939405
        ),
        units: "m",
        theme: null, // or OpenLayers will attempt to load it default theme
        controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.PanZoom(),
            new OpenLayers.Control.ArgParser(),
            new OpenLayers.Control.Attribution(),
            new OpenLayers.Control.ScaleLine(),
            new OpenLayers.Control.OverviewMap()
        ]
    };
    var map = new OpenLayers.Map(mapOptions);
    map.addLayers(getLayers());

    // create map panel
    options = Ext.apply({
        map: map,
        tbar: (new App.Tools(map)).toolbar,
        stateId: "map",
        prettyStateKeys: true
    }, options);
    this.mapPanel = new GeoExt.MapPanel(options);
};
