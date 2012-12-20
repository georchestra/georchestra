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
 * Should we include either v1_0_0.js or v1_1_0.js or both?
 * This app produces v1.1 compliant WMCs, but one might want
 * to restore v1.0 compliant WMCs too => both formats are useful
 * @include OpenLayers/Format/WMC/v1_0_0.js
 * @include OpenLayers/Format/WMC/v1_1_0.js
 * @include GeoExt/data/WMCReader.js
 * @include GEOR_util.js
 * @include GEOR_ows.js
 */

Ext.namespace("GEOR");

GEOR.wmc = (function() {
    /*
     * Private
     */

    /**
     * Property: wmcFormat
     * {OpenLayers.Format.WMC} The format to read/write WMC.
     */
    var wmcFormat = null;

    /**
     * Property: layerStore
     * {GeoExt.data.LayerStore} The application's layer store.
     */
    var layerStore = null;

    /**
     * Property: wmcReader
     * {GeoExt.data.WMCReader} The wmc reader.
     */
    var wmcReader = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: writeWmcContext
     * Writes a wmc context object given a layer store.
     *
     * Parameters:
     * ls - {GeoExt.data.LayerStore} The layer store.
     *
     * Returns:
     * {Object} A wmc context object.
     */
    var writeWmcContext = function(ls) {
        var map = ls.map;
        var context = {
            bounds: map.getExtent(),
            maxExtent: map.maxExtent,
            projection: map.projection,
            size: map.getSize(),
            layersContext: []
        };

        ls.each(function (record) {
            var layer = record.get('layer');
            if (!(layer instanceof OpenLayers.Layer.WMS)) {
                return;
            }
            
            // having styles referenced in the layer's metadata object 
            // prevents the WMC format to obtain them from layer.params.
            // In geOrchestra we're not using layer.metadata since we have much better
            // (layer records) => we're better off removing layer.metadata.styles here:
            layer.metadata.styles = null;
            // Note: this fixes http://applis-bretagne.fr/redmine/issues/4510
            var layerContext = wmcFormat.layerToContext(layer); 
            
            // only the first metadataURL can be saved to WMC:
            // see http://applis-bretagne.fr/redmine/issues/2091
            if (layerContext.metadataURL && layerContext.metadataURL[0]) {
                if (typeof layerContext.metadataURL[0] == 'string') {
                    layerContext.metadataURL = layerContext.metadataURL[0];
                } else if (layerContext.metadataURL[0].href) {
                    layerContext.metadataURL = layerContext.metadataURL[0].href;
                } else {
                    delete layerContext.metadataURL;
                }
            }
            var queryable = record.get('queryable');
            var styles = record.get('styles');
            var formats = record.get('formats');

            if (queryable !== undefined) {
                layerContext.queryable = queryable;
            }

            if (styles !== undefined && styles.length > 0) {
                // if the context style has its href property
                // set, which means the layer has an SLD
                // parameters, we don't empty the styles
                // array because we don't want to loose
                // those styles
                var layerContextStyles = layerContext.styles;
                if (!layerContextStyles[0].href) {
                    layerContext.styles = [];
                }
                Ext.each(styles, function (item) {
                    var style = {};
                    Ext.apply(style, {current: false}, item);
                    if(layer.params.STYLES === style.name) {
                        style.current = true;
                    }
                    layerContext.styles.push(style);
                });
            }

            if (formats !== undefined && formats.length > 0) {
                layerContext.formats = [];
                Ext.each(formats, function (item, index, all) {
                    var format = {};
                    var f = (item instanceof Object) ? item.value : item;
                    Ext.apply(format, {current: false}, {value: f});
                    if(layer.params.FORMAT == f) {
                        format.current = true;
                    }
                    layerContext.formats.push(format);
                });
            }

            context.layersContext.push(layerContext);
        });

        return context;
    };


    return {

        /**
         * APIMethod: init
         * Initialize this module
         *
         * Parameters:
         * ls - {GeoExt.data.LayerStore} The layer store instance.
         */
        init: function(ls) {
            layerStore = ls;
            tr = OpenLayers.i18n;

            wmcFormat = new OpenLayers.Format.WMC({
                //layerOptions: GEOR.ows.defaultLayerOptions
                // why should we apply default layer options and not use those provided by the WMC ?
            });
            wmcReader = new GeoExt.data.WMCReader(
                {format: wmcFormat},
                layerStore.recordType
            );
        },

        /*
         * APIMethod: write
         * Write a wmc string given the layer store.
         *
         * Parameters:
         * options - {Object} Optional object to pass to wmc format write method
         *
         * Returns:
         * {String} The WMC string.
         */
        write: function(options) {
            var context = writeWmcContext(layerStore);
            return wmcFormat.write(context, options);
        },

        /*
         * APIMethod: read
         * Restore the layer store from a wmc string.
         *
         * Parameters:
         * wmcString - {XML | String} The XML|string describing the context to restore.
         * resetMap - {Boolean} Specifies if the map and the fake base layer must
                      be reset.
         * zoomToWMC - {Boolean} Whether to zoom to WMC bbox or not, defaults to true
         */
        read: function(wmcString, resetMap, zoomToWMC) {
            var map = layerStore.map;
            var newContext = wmcFormat.read(wmcString, {}); // get context from wmc
                                                         // using non-API feature

            if(map.getProjection() && (newContext.projection !== map.getProjection())) {
                // bounding box from wmc does not have the same projection system
                // as the current map
                GEOR.util.errorDialog({
                    msg: tr("The .wmc file cannot be restored. Its spatial " +
                        "reference system is different from the system of " +
                        "the current map")
                });
                return;
            }

            // remove all current layers except the lowest index one
            // (our fake base layer)
            for (var i = map.layers.length -1; i >= 1; i--) {
                map.layers[i].destroy();
            }

            var maxExtent = newContext.maxExtent;
            if (resetMap === true && maxExtent) {
                map.setOptions({maxExtent: maxExtent});
                var fakeBaseLayer = map.layers[0];
                fakeBaseLayer.addOptions({maxExtent: maxExtent});
            }

            Ext.each(wmcReader.readRecords(newContext).records, function(r) {
                // restore metadataURLs in record
                var context = null;
                for (var i=0, l = newContext.layersContext.length; i<l; i++) {
                    if (newContext.layersContext[i]['name'] === r.get('name') &&
                        newContext.layersContext[i]['url'] === r.get('layer').url) {
                        context = newContext.layersContext[i];
                        break;
                    }
                }
                if (context && context.metadataURL) {
                    r.set("metadataURLs", [context.metadataURL]);
                }
                // add layer from wmc to the current map
                layerStore.addSorted(r);
            });

            if (zoomToWMC === true || zoomToWMC === undefined) {
                // zoom to closest extent specified in the WMC doc
                // (hence second argument is true,
                // see http://applis-bretagne.fr/redmine/issues/2398)
                map.zoomToExtent(newContext.bounds, true);
            }
        }
    };
})();
