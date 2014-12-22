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
 * @include OpenLayers/Format/OWSContext/v0_3_1.js
 * @include OpenLayers/Projection.js
 * @include GeoExt/data/WMCReader.js
 * @include GEOR_util.js
 * @include GEOR_ows.js
 */

Ext.namespace("GEOR");

GEOR.wmc = (function() {
    /*
     * Private
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: beforecontextrestore
         * Fires when a context is to be restored 
         *
         * Listener arguments:
         * count - {Integer} the number of records to restore
         */
        "beforecontextrestore"
    );

    /**
     * Property: wmcFormat
     * {OpenLayers.Format.WMC} The format to read/write WMC.
     */
    var wmcFormat = null;

    /**
     * Property: owsContextFormat
     * {OpenLayers.Format.OWSContext} The format to read/write OWS Contexts.
     */
    var owsContextFormat = null;

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
            var queryable = record.get('queryable'),
                styles = record.get('styles'),
                abs = record.get('abstract'),
                formats = record.get('formats');

            if (queryable !== undefined) {
                layerContext.queryable = queryable;
            }
            if (abs !== undefined) {
                layerContext["abstract"] = abs;
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
                    if (layer.params.STYLES === style.name) {
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
                    if (layer.params.FORMAT == f) {
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

        /*
         * Observable object
         */
        events: observable,

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
                layerOptions: {
                    // to prevent automatic restoring of PNG rather than JPEG:
                    noMagic: true
                }
            });
            owsContextFormat = new OpenLayers.Format.OWSContext();
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
         * options - {Object} options overriding map context
         *
         * Returns:
         * {String} The WMC string.
         */
        write: function(options) {
            var context = Ext.apply(
                writeWmcContext(layerStore), 
                options || {}
            );
            return wmcFormat.write(context, {
                id: Math.random().toString(16).substr(2)
            });
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
            var map = layerStore.map,
                mapProj, wmcProj, newContext, records;

            try {
                // trying with WMC format
                newContext = wmcFormat.read(wmcString);
            } catch (err) {
                // trying with OWS Context format
                newContext = owsContextFormat.read(wmcString);
            }
            // FAIL:
            if (newContext.layersContext === undefined) {
                GEOR.util.errorDialog({
                    msg: tr("The provided file is not a valid OGC context")
                });
                return false;
            }

            // If the context has been saved in a different projection,
            // we're trying to restore the layers in the current map projection.
            mapProj = map.getProjection();
            wmcProj = newContext.projection;
            if (mapProj && (wmcProj !== mapProj)) {
                // wmc does not have the same projection system
                // as the current map
                GEOR.util.infoDialog({
                    msg: tr("Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !", {
                        PROJCODE1: wmcProj,
                        PROJCODE2: mapProj
                    })
                });
                var reproj = function() {
                    this && this.transform && this.transform(
                        new OpenLayers.Projection(wmcProj), 
                        new OpenLayers.Projection(mapProj)
                    );
                };
                reproj.apply(newContext.bounds);
                reproj.apply(newContext.maxExtent);
                Ext.each(newContext.layersContext, function(l) {
                    reproj.apply(l.maxExtent);
                });
                newContext.projection = map.getProjection();
            }

            var maxExtent = newContext.maxExtent;
            if (resetMap === true) {
                // remove all current layers except the lowest index one
                // (our fake base layer)
                for (var i = map.layers.length -1; i >= 1; i--) {
                    map.removeLayer(map.layers[i]);
                }
                if (maxExtent) {
                    map.setOptions({maxExtent: maxExtent});
                    var fakeBaseLayer = map.layers[0];
                    fakeBaseLayer.addOptions({maxExtent: maxExtent});
                }
            }
            records = wmcReader.readRecords(newContext).records;
            // fire event to let the whole app know about it.
            observable.fireEvent("beforecontextrestore", records.length);
            Ext.each(records, function(r) {
                // restore metadataURLs in record
                var context = null,
                    layer = r.get('layer');
                for (var i=0, l = newContext.layersContext.length; i<l; i++) {
                    if (newContext.layersContext[i]['name'] === r.get('name') &&
                        newContext.layersContext[i]['url'] === layer.url) {
                        context = newContext.layersContext[i];
                        break;
                    }
                }
                if (context && context.metadataURL) {
                    r.set("metadataURLs", [context.metadataURL]);
                }
                // set as type as WMS (might need to be changed when we support more types from OWSContext)
                r.set("type", "WMS");
                // restore opaque status from transitionEffect:
                r.set("opaque", layer.transitionEffect == "resize");
                // change exception format depending on the WMS version: 
                // (see https://github.com/camptocamp/georchestra-pigma-configuration/issues/112)
                var params = layer.params;
                params.EXCEPTIONS = GEOR.ows.wmsVersionToExceptionsMapping[params.VERSION];
                // same is true for the SLD_VERSION parameter:
                // see https://github.com/georchestra/georchestra/issues/636
                params.SLD_VERSION = GEOR.ows.wmsVersionToSLDVersionMapping[params.VERSION];
                // add layer from wmc to the current map
                layerStore.addSorted(r);
            });

            if (zoomToWMC === true || zoomToWMC === undefined) {
                // zoom to closest extent specified in the WMC doc
                // (hence second argument is true,
                // see http://applis-bretagne.fr/redmine/issues/2398)
                map.zoomToExtent(newContext.bounds, true);
            }
            // see https://github.com/georchestra/georchestra/issues/816:
            if (GEOR.config.CONTEXT_LOADED_INDICATOR_DURATION !== 0 && 
                (newContext.title || newContext["abstract"])) {
                GEOR.helper.msg(
                    newContext.title || "", 
                    newContext["abstract"] || "", 
                    GEOR.config.CONTEXT_LOADED_INDICATOR_DURATION
                );
            }
        }
    };
})();
