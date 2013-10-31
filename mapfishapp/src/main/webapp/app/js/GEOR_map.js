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
 * @include OpenLayers/Control/PanPanel.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Control/PinchZoom.js
 * @include OpenLayers/Control/LoadingPanel.js
 * @include OpenLayers/Kinetic.js
 * @requires OpenLayers/Layer/Grid.js
 * @include OpenLayers/Layer/WMS.js
 * @include OpenLayers/Layer/OSM.js
 * @include OpenLayers/Layer/Grid.js
 * @include OpenLayers/BaseTypes/Size.js
 * @include GeoExt/data/LayerRecord.js
 * @include GeoExt/data/LayerStore.js
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include GEOR_ows.js
 * @include GEOR_wmc.js
 */

Ext.namespace("GEOR");

// see comment below regarding opaque layers and also
// https://github.com/georchestra/georchestra/issues/411
OpenLayers.Layer.Grid.prototype.transitionEffect = null;

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
     * Property: ls
     * {GeoExt.data.LayerStore}
     */
    var ls = null;

    /**
     * Constant: SCALES
     * {Array} The map's scales.
     */
    var SCALES = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: _adding
     * {Boolean}
     */
    var _adding = false;

    /**
     * Method: createMainBaseLayer
     * Create and return the main layer, this layer will not be
     * displayed in the overview map.
     *
     * Returns:
     * {OpenLayers.Layer} The unique base layer in this app.
     */
    var createMainBaseLayer = function() {
        
        // Grid of blank images of 1024x1024
        return new OpenLayers.Layer.Grid("base_layer", '', null, {
            singleTile: false,
            displayInLayerSwitcher: false,
            isBaseLayer: true,
            tileSize: new OpenLayers.Size(1024, 1024),
            getURL: function() {
                return [
                    'data:image/png;base64,iVBORw0KGgoAAAANSUhEU',
                    'gAABAAAAAQAAQMAAABF07nAAAAAAXNSR0IArs4c6QAA',
                    'AANQTFRF////p8QbyAAAAAFiS0dEAIgFHUgAAAAJcEh',
                    'ZcwAACxMAAAsTAQCanBgAAAAHdElNRQfdCQMOJQp/aX',
                    'w9AAAAlklEQVR42u3BAQEAAACCIP+vbkhAAQAAAAAAA',
                    'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADvBgQeAAEN3',
                    'jhkAAAAAElFTkSuQmCC'
                ].join(''); 
            }
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
                title: tr("Location map"),
                minRectSize: 10,
                // with these settings, a unique OSM zoom level is displayed:
                minRatio: 1,
                maxRatio: 100000,
                layers: [
                    new OpenLayers.Layer.OSM("_OSM", [
                        'http://a'+u, 'http://b'+u, 'http://c'+u
                    ], {
                        transitionEffect: 'resize',
                        buffer: 0,
                        // do not fetch useless low zoom level tiles:
                        maxResolution: 9783.939619141,
                        numZoomLevels: 15,
                        zoomOffset: 4,
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
                title: tr("Location map"),
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
        // max extent can be overriden in the WMC,
        // see GEOR.initmap and startup WMC file
        var options = {
            projection: GEOR.config.MAP_SRS,
            units: tr("m"),
            allOverlays: true,
            scales: SCALES,
            maxExtent: new OpenLayers.Bounds(
                GEOR.config.MAP_XMIN,
                GEOR.config.MAP_YMIN,
                GEOR.config.MAP_XMAX,
                GEOR.config.MAP_YMAX
            ),
            theme: null
        };
        var map = new OpenLayers.Map(Ext.applyIf({
            controls: [
                new OpenLayers.Control.Navigation({
                    dragPanOptions: {
                        enableKinetic: true
                    }
                }),
                new OpenLayers.Control.PanPanel(),
                new OpenLayers.Control.LoadingPanel()
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
            // TODO here: keep locally a FIFO of records to add / insert.
            // once XHRs are finished, insert layers in store at the corresponding place.

            // the filter method takes care of adding the records (required because of DescribeLayer XHR)
            if (_adding) {
                LayerStore.superclass.add.call(this, records);
            } else {
                records = filter(records);
            }
        },
        insert: function(index, records) {
            if (_adding) {
                LayerStore.superclass.insert.call(this, index, records);
            } else {
                records = filter(records, index);
            }
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
    var filter = function(records, index) { // optional index property. if present, we should insert at position "index" rather than add
        var errors = [], keep = [];

        Ext.each(records, function(r) {
            var error = checkLayer(r);
            if (error) {
                // these are just warnings in fact, not errors
                // see http://applis-bretagne.fr/redmine/issues/1749
                errors.push(error);
            }

            // r.get('layer').transitionEffect = resize would have been set in WMC,
            // not by the default openlayers GRID layer type,
            // see the overriding in the first lines of this file.
            r.get('layer').transitionEffect =
                (r.get("opaque") === true || r.get('layer').transitionEffect === 'resize') ?
                'resize' : 'map-resize';
            // note: an opaque layer can be considered as a baselayer
            // as a result, we apply a transitionEffect, which suits well for baselayers

            // Format attribution if required:
            var attr = r.get('attribution');
            var layer = r.get('layer');
            if (!attr || !attr.title) {
                var a, 
                    // handle both wms & wfs layers
                    url = layer.url || (layer.protocol && layer.protocol.url);
                if (url) {
                    var b = OpenLayers.Util.createUrlObject(url);
                    if (b && b.host) {
                        a = b.host;
                    }
                }
                r.set('attribution', {
                    title: a
                });
            }

            // set layer.metadataURL if record has metadataURLs
            // so that this can be saved in a WMC context
            if (r.get('metadataURLs') && r.get('metadataURLs')[0]) {
                layer.metadataURL = [r.get('metadataURLs')[0]];
            }

            // Errors should be non-blocking since http://applis-bretagne.fr/redmine/issues/1749
            // so we "keep" every layer, and only display a warning message
            keep.push(r);

            // WMSDescribeLayer for each new WMS Layer
            if (r.get("type") == "WMS" && r.get("_described") !== true) {
                GEOR.waiter.show();
                GEOR.ows.WMSDescribeLayer(r, {
                    success: function(store, records) {
                        var wfsRecord = GEOR.ows.getWfsInfo(records);
                        if (wfsRecord) {
                            r.set("WFS_typeName", wfsRecord.get("typeName"));
                            r.set("WFS_URL", wfsRecord.get("owsURL"));
                        }
                        var wcsRecord = GEOR.ows.getWcsInfo(records);
                        if (wcsRecord) {
                            r.set("WCS_typeName", wcsRecord.get("typeName"));
                            r.set("WCS_URL", wcsRecord.get("owsURL"));
                        }
                        // this is to prevent "reordering layers triggers describeLayer"
                        r.set("_described", true);
                        _adding = true;
                        if (index) {
                            ls.insert(index,[r]); // FIXME: insertion order might not be correct !
                            // solution: sort layerstore by 1) new field 2) opaque
                            // "new field" is filled with WMC layer order...
                        } else {
                            ls.add([r]);
                        }
                        _adding = false; // TODO: add/insert 
                        // FIXME ! Generates incorrect order while restoring context
                    },
                    failure: function() {
                        _adding = true;
                        if (index) {
                            ls.insert(index,[r]); // FIXME: insertion order might not be correct !
                        } else {
                            ls.add([r]);
                        }
                        _adding = false;
                    },
                    scope: this
                });
            } else {
                // add immediately to layerstore
                _adding = true;
                if (index) {
                    ls.insert(index,[r]); // FIXME: insertion order might not be correct !
                } else {
                    ls.add([r]);
                }
                _adding = false;
            }
        });

        if (errors.length > 0) {
            GEOR.util.infoDialog({
                title: tr("Warning after loading layer"),
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
        var prefix = tr("The <b>NAME</b> layer could not appear for that reason: ",
            {'NAME': r.get('title')});
        var minScale = r.get('minScale');
        var maxScale = r.get('maxScale');

        // check if min and max scales are valid (i.e. positive)
        if ((minScale && minScale < 0) || (maxScale && maxScale < 0)) {
            return  prefix + tr("Min/max visibility scales are invalid");
        }

        // check if scales are in a valid range (compared to the map scales)
        if (map.baseLayer && (
            (minScale && minScale < map.baseLayer.maxScale) ||
            (maxScale && maxScale > map.baseLayer.minScale))) {
            return prefix + tr("Visibility range does not match map scales");
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
                return prefix + tr("Geografic extent does not match map extent");
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
        var recordType = GEOR.util.createRecordType(
            GEOR.ows.getRecordFields()
        );
        map = createMap();

        ls = new LayerStore({
            map: map,
            sortInfo: {
                // opaque layers at the bottom
                // as suggested by the WMS spec
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
            tr = OpenLayers.i18n;
            SCALES = GEOR.config.MAP_SCALES;
            return createLayerStore();
        }
    };
})();
