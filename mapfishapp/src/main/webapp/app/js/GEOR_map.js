/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include OpenLayers/Map.js
 * @include OpenLayers/Util.js
 * @include OpenLayers/Control/OverviewMap.js
 * @include OpenLayers/Control/Attribution.js
 * @include OpenLayers/Control/ZoomPanel.js
 * @include OpenLayers/Control/PanPanel.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Layer/WMS.js
 * @include OpenLayers/Layer/XYZ.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/LayerStore.js
 * @include GEOR_config.js
 * @include GEOR_ows.js
 * @include GEOR_wmc.js
 * @include GEOR_proj4jsdefs.js
 */

Ext.namespace("GEOR");

GEOR.map = (function() {

    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map
     */
    var map = null;

    /**
     * Constant: SCALES
     * {Array} The map's scales.
     */
    var SCALES = [
        1066.364791734,
        2132.729583468,
        4265.459166936,
        8530.918333871,
        17061.836667742,
        34123.673335484,
        68247.346670968,
        136494.693341936,
        272989.386683873,
        545978.773367746,
        1091957.546735491,
        2183915.093470982,
        4367830.186941965,
        8735660.373883929,
        17471320.747767858,
        34942641.495535716,
        69885282.991071432,
        139770565.982142864,
        279541131.964285732,
        559082263.928571464
    ];
    
    /**
     * Method: createMainBaseLayer
     * Create and return the main layer, this layer will not be
     * displayed in the overview map.
     *
     * Returns:
     * {OpenLayers.Layer} The unique base layer in this app.
     */
    var createMainBaseLayer = function() {
        return new OpenLayers.Layer("base_layer", {
            displayInLayerSwitcher: false,
            isBaseLayer: true
        });
    };

    /**
     * Method: createOverviewMap
     * Create and return the overview map control.
     *
     * Parameters:
     * mapOptions - {Object} The main map's options.
     *
     * Returns:
     * {OpenLayers.Control.OverviewMap} The control.
     */
    var createOverviewMap = function(mapOptions) {
        if (GEOR.config.OSM_AS_OVMAP) {
            var u = '.tile.openstreetmap.org/${z}/${x}/${y}.png';
            return new OpenLayers.Control.OverviewMap({
                mapOptions: {
                    theme: null,
                    scales: null,
                    maxExtent: null,
                    allOverlays: false,
                    controls: [new OpenLayers.Control.Attribution()]
                },
                title: 'carte de situation',
                minRectSize: 10,
                minRatio: 128,
                maxRatio: 128,
                layers: [
                    new OpenLayers.Layer.OSM("_OSM", [
                        'http://a'+u, 'http://b'+u, 'http://c'+u
                    ], {
                        transitionEffect: 'resize',
                        buffer: 0,
                        attribution: [
                            "(c) <a href='http://openstreetmap.org/'>OSM</a>",
                            "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>by-sa</a>"
                        ].join(' ')
                    })
                ]
            });
        } else {
            return new OpenLayers.Control.OverviewMap({
                mapOptions: Ext.applyIf({
                    scales: [4 * SCALES[SCALES.length - 1]],
                    controls: []
                }, mapOptions),
                title: 'carte de situation',
                minRectSize: 10,
                layers: [new OpenLayers.Layer.WMS(
                    "__geor_overview__",
                    GEOR.config.GEOSERVER_WMS_URL,
                    {layers: GEOR.config.OVMAP_LAYER_NAME, format: 'image/png'},
                    {singleTile: true}
                )]
            });
        }
    };

    /**
     * Method: createMap
     * Return the application's map instance.
     *
     * Returns:
     * {OpenLayers.Map} The map instance.
     */
    var createMap = function() {
        // the default projection is EPSG:2154 and the default
        // max extent is (83000,6700000,420000,6900000).
        //
        // max extent can be overriden in the WMC, see GEOR.initmap
        // and GEOR.wmc

        var options = {
            projection: 'EPSG:2154',
            units: 'm',
            allOverlays: true,
            scales: SCALES,
            maxExtent: new OpenLayers.Bounds(
                //-357823.2365,6037008.6939,2146865.3059,8541697.2363
                83000, 6700000,
                420000, 6900000
            ),
            theme: null
        };
        var map = new OpenLayers.Map(Ext.applyIf({
            controls: [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanPanel(),
                new OpenLayers.Control.ZoomPanel()
            ]
        }, options));
        map.addControl(createOverviewMap(options));
        return map;
    };

    /** api: constructor
     * A specific GeoExt.data.LayerStore.
     */
    var LayerStore = Ext.extend(GeoExt.data.LayerStore, {
        add: function(records) {
            records = filter(records);
            LayerStore.superclass.add.call(this, records);
        },
        insert: function(index, records) {
            records = filter(records);
            LayerStore.superclass.insert.call(this, index, records);
        }
    });

    /**
     * Method: filter
     * Filter the records and set "hideInLegend" in the records
     * when necessary (so the corresponding layers don't appear
     * in the legend panel).
     * Also modifies attribution field if necessary.
     * Generally speaking, handles every operation needed before 
     * the records are added to the layerStore.
     *
     * Parameters:
     * records - {Array({GeoExt.data.LayerRecord})} The records.
     *
     * Returns:
     * {Array({GeoExt.data.LayerRecord})} The records that pass
     *     the filter.
     */
    var filter = function(records) {
        var errors = [], keep = [];
        
        Ext.each(records, function(r) {
            var error = checkLayer(r);
            if (error) {
                // these are just warnings in fact, not errors 
                // see http://csm-bretagne.fr/redmine/issues/1749
                errors.push(error);
            } 
            
            // Note: queryable is required in addition to opaque, 
            // because opaque is not a standard WMC feature
            // This enables us to remove rasters from legend panel
            if (r.get("opaque") === true || r.get("queryable") === false) { 
                // this record is valid, set its "hideInLegend"
                // data field to true if the corresponding layer
                // is a raster layer, i.e. its "opaque" data
                // field is true
                r.set("hideInLegend", true);
                // we set opaque to true so that non queryable 
                // layers are considered as baselayers
                r.set("opaque", true);
            }
            // Note that the ultimate solution would be to do a getCapabilities 
            // request for each OGC server advertised in the WMC
            
            // Format attribution if required:
            var attr = r.get('attribution');
            var layer = r.get('layer');
            if (!attr || !attr.title) {
                var a;
                if (layer.url) {
                    var b = OpenLayers.Util.createUrlObject(layer.url);
                    if (b && b.host) {
                        a = b.host;
                    }
                }
                r.set('attribution', {
                    title: a || GEOR.config.DEFAULT_ATTRIBUTION
                });
            }
            
            // set layer.metadataURL if record has metadataURLs
            // so that this can be saved in a WMC context
            if (r.get('metadataURLs') && r.get('metadataURLs')[0]) {
                layer.metadataURL = [r.get('metadataURLs')[0]];
            }
            
            // Errors should be non-blocking since http://csm-bretagne.fr/redmine/issues/1749
            // so we "keep" every layer, and only display a warning message
            keep.push(r);
        });
        
        if (errors.length > 0) {
            GEOR.util.infoDialog({
                title: 'Avertissement suite au chargement de couche',
                msg: errors.join('<br />')
            });
        }
        return keep;
    };

    /**
     * Method: checkLayer
     * Checks if the layer is valid (i.e. can be added to the LayerStore).
     * Doesn't return anything if the layer is valid, returns an error message
     *    if not. 
     *
     * Returns:
     * {String} An error message.
     */
    var checkLayer = function(r) {
        var prefix = 'La couche <b>"' + r.get('title') + '"</b>' +
                     ' pourrait ne pas apparaître pour la raison suivante : ';

        var minScale = r.get('minScale');
        var maxScale = r.get('maxScale');
        
        // check if min and max scales are valid (i.e. positive)
        if ((minScale && minScale < 0) || (maxScale && maxScale < 0)) {
            return  prefix + 'Les échelles min/max de visibilité sont invalides.';
        }

        // check if scales are in a valid range (compared to the map scales)
        if (map.baseLayer && (
            (minScale && minScale < map.baseLayer.maxScale) ||
            (maxScale && maxScale > map.baseLayer.minScale))) {
            return prefix + 'La plage de visibilité ne correspond pas aux échelles de la carte.';
        }

        // check if layer extent and map extent match
        if (r.get('llbbox')) {
            var llbbox = r.get('llbbox'); 
            llbbox = new OpenLayers.Bounds(llbbox[0], llbbox[1], llbbox[2], llbbox[3]);
            
            var mapbbox = map.getMaxExtent().clone();
            mapbbox.transform(
                map.getProjectionObject(),
                new OpenLayers.Projection("EPSG:4326")
            );
            
            if (!llbbox.intersectsBounds(mapbbox)) {
                return prefix + "L'étendue géographique ne correspond pas à celle de la carte.";
            }
        }
    };

    /**
     * Method: createLayerStore
     * Return the application's global layer store.
     *
     * Returns:
     * {GeoExt.data.LayerStore} The global layer store.
     */
    var createLayerStore = function() {
        var recordType = GeoExt.data.LayerRecord.create(
            GEOR.ows.getRecordFields()
        );
        map = createMap();

        var ls = new LayerStore({
            map: map,
            sortInfo: {
                field: 'opaque',
                direction: 'DESC'
            },
            fields: recordType
        });

        var layer = createMainBaseLayer();
        ls.add([new recordType({
                title: layer.name,
                layer: layer
            }, layer.id)]
        );

        return ls;
    };

    /*
     * Public
     */

    return {

        /**
         * APIMethod: create
         * Create the application's global layer store.
         *
         * Returns:
         * {GeoExt.data.LayerStore} The application's global layer store.
         */
        create: function() {
            return createLayerStore();
        }
    };
})();
