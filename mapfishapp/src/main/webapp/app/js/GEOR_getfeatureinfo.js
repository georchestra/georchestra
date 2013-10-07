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
 * @include OpenLayers/Control/WMSGetFeatureInfo.js
 * @include OpenLayers/Control/WMTSGetFeatureInfo.js
 * @include OpenLayers/Format/WMSGetFeatureInfo.js
 * @include OpenLayers/Projection.js
 * @include GEOR_FeatureDataModel.js
 */

Ext.namespace("GEOR");

GEOR.getfeatureinfo = (function() {

    /*
     * Private
     */

    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: searchresults
         * Fires when we've received a response from server
         *
         * Listener arguments:
         * options - {Object} A hash containing results
         */
        "searchresults",
        /**
         * Event: search
         * Fires when the user presses the search button
         *
         * Listener arguments:
         * panelCfg - {Object} Config object for a panel
         */
        "search",
        /**
         * Event: shutdown
         * Fires when GFI tool is deactivated
         *
         */
        "shutdown"
    );

    /**
     * Property: ctrl
     * {OpenLayers.Control.WMSGetFeatureInfo} The control.
     */
    var ctrl = null;

    /**
     * Equal to true if a research is launched on multiple layers
     * and false if it is on a single layer.
     * If true, it will force the model to be recreated from the features (see onGetfeatureinfo).
     */
    var Xsearch = null;

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    var layerStore = null;

    /**
     * Method: onGetfeatureinfo
     * Callback executed when the GetFeatureInfo response
     * is received.
     *
     * Parameters:
     * info - {Object} Hash of options, with keys: text, features, request, xy.
     */
    var onGetfeatureinfo = function(info) {
        OpenLayers.Element.addClass(map.viewPortDiv, "olDrawBox");

        /* results will be a hashmap of objects keyed on featureType with 4 properties:
         * - model: the corresponding FeatureDataModel
         * - features: features for that featureType
         * - title: the shortened tab title
         * - tooltip: the tooltip for the tab
         */
        var results = {};

        // Features on-the-fly client-side reprojection (this is a hack, OK)
        // Discussion happened in https://github.com/georchestra/georchestra/issues/254
        
        /*
         * We're typically getting this kind of string in the GML:
         *  gml:MultiPolygon srsName="http://www.opengis.net/gml/srs/epsg.xml#3948"
         */
        var r =  /[^]+srsName=\"(.+?)\"[^]+/.exec(info.text);
        if (r) {
            var srsString = r[1];
            /*
             * At this stage, we have to normalize these kinds of strings:
             * http://www.opengis.net/gml/srs/epsg.xml#2154
             * http://www.opengis.net/def/crs/EPSG/0/4326
             * urn:x-ogc:def:crs:EPSG:4326
             * urn:ogc:def:crs:EPSG:4326
             * EPSG:2154
             */
            var srsName = srsString.replace(/.+[#:\/](\d+)$/, "EPSG:$1");
            
            if (map.getProjection() !== srsName) {
                var sourceSRS = new OpenLayers.Projection(srsName),
                    destSRS = map.getProjectionObject();
                Ext.each(info.features, function(f) {
                    f.geometry.transform(sourceSRS, destSRS);
                    if (f.bounds && !!f.bounds.transform) {
                        f.bounds.transform(sourceSRS, destSRS);
                    }
                });
            }
        }
        var coord = map.getLonLatFromPixel(info.xy).transform(map.projection, new OpenLayers.Projection("EPSG:4326"));
        var coordstr = "Lon:" + OpenLayers.Number.format(coord.lon, 5) + " Lat:" + OpenLayers.Number.format(coord.lat, 5);
        // we need to create a results object for each ctrl.layer
        // to gracefully handle the case when no data is found
        Ext.each(ctrl.layers, function (layer) {
            results[layer.params.LAYERS] = {
                title: GEOR.util.shortenLayerName(layer),
                tooltip: layer.name + " - " + coordstr,
                features: []
            };
        });

        // explode info.features in the different layers
        Ext.each(info.features, function (feature) {
            var featureType = feature.gml.featureType;
            results[featureType].features.push(feature);
        });

        // generate FeatureDataModels now that we have all features sorted out by featureType
        Ext.iterate(results, function(featuretype, result) {
            result.model = new GEOR.FeatureDataModel({ features: result.features });
        });

        observable.fireEvent("searchresults", {
            results: results
        });
    };


    /**
     * Method: onBeforegetfeatureinfo
     * Callback executed just before getFeatureInfo request is triggered
     */
    var onBeforegetfeatureinfo = function() {
        // to let OL use its own cursor class:
        OpenLayers.Element.removeClass(map.viewPortDiv, "olDrawBox");

        var msg;
        if(ctrl.layers.length > 0) {
            msg = "<div>Searching...</div>";
        } else {
            msg = "<div>No layer selected</div>";
        }

        observable.fireEvent("search", {
            html: tr(msg)
        });
    };

    /**
     * Method: onLayerVisibilitychanged
     * Callback executed on WMS layer visibility changed
     * We need to deactivate ourselves or update the list of layers queried
     */
    var onLayerVisibilitychanged = function() {
        /* XXX remove visibililtychanged event from the layer */
        /* update ctrl.layers if we're in a multi-layer query */
        if (Xsearch) {
            layers = [];
            for(var i = 0; i < layerStore.data.length; i++) {
                var layerRecord = layerStore.getAt(i);
                if(layerRecord.get("queryable") && layerRecord.getLayer().visibility == true) {
                    layers.push(layerRecord.getLayer());
                }
            }
            ctrl.layers = layers;
        } else {
            if (!ctrl.layers[0].visibility) {
                this.toggle(ctrl.layers[0], false);
            }
        }
    };

    /**
     * Method: onLayerRemoved
     * Callback executed on WMS layer removed from map
     * We need to deactivate ourselves or update the list of layers queried
     */
    var onLayerRemoved = function(options) {
        /* remove options.layer from ctrl.layers if it was being queried in a multi-layer query */
        if (Xsearch) {
            for(var i = 0; i < ctrl.layers.length; i++) {
                if (options.layer === ctrl.layers[i]) {
                    ctrl.layers.splice(i, 1);
                    break;
                }
            }
        } else {
            if (options.layer === ctrl.layers[0]) {
                this.toggle(options.layer, false);
            }
        }
    };

    /**
     * Method: onCtrlactivate
     * Callback executed on control activation
     */
    var onCtrlactivate = function() {
        OpenLayers.Element.addClass(map.viewPortDiv, "olDrawBox");
    };

    /**
     * Method: onCtrldeactivate
     * Callback executed on control deactivation
     */
    var onCtrldeactivate = function() {
        OpenLayers.Element.removeClass(map.viewPortDiv, "olDrawBox");
    };

    /*
     * Public
     */

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
         * m - {OpenLayers.Map} The map instance.
         */
        init: function(l) {
            tr = OpenLayers.i18n;
            map = l.map;
            layerStore = l;
        },

        /**
         * APIMethod: toggle
         *
         * Parameters:
         * record - {GeoExt.data.LayerRecord | OpenLayers.Layer.WMS} the layer
         * record is false if it's a multi-layer query
         * state - {Boolean} Toggle to true or false this layer ?
         */
        toggle: function(record, state) {
            var title, type;
            var layers = [];
            if (record instanceof OpenLayers.Layer.WMS) {
                layers = [record];
                title = layer.name;
                type = "WMS";
            } else if (record instanceof GeoExt.data.LayerRecord) {
                layers = [record.get("layer")];
                title = record.get("title");
                type = record.get("type");
            } else if (record === false) {
                type = "WMS"; // XXX assume all layers queried are WMS ?
                for(var i = 0; i < layerStore.data.length; i++) {
                    var layerRecord = layerStore.getAt(i);
                    if(layerRecord.get("queryable") && layerRecord.getLayer().visibility == true) {
                        layers.push(layerRecord.getLayer());
                    }
                }
            }
            if (layers.length == 0) {
                observable.fireEvent("search", {
                    html: tr("No active layers.")
                });
                observable.fireEvent("shutdown");
            } else if (state) {
                Xsearch = (record === false ? true : false);
                observable.fireEvent("search", {
                    html: Xsearch ? tr("Search on all active layers") :
                             tr("<div>Search on objects active for NAME layer. " +
                             "Clic on the map.</div>",
                             {'NAME': title})
                });

                var ctrlEventsConfig = {
                    "beforegetfeatureinfo": onBeforegetfeatureinfo,
                    "getfeatureinfo": onGetfeatureinfo,
                    "activate": onCtrlactivate,
                    "deactivate": onCtrldeactivate,
                    scope: this
                };

                // we'd like to activate gfi request on layer
                if (ctrl) {
                    // remove visibility events from previous array of layers
                    Ext.each(ctrl.layers, function(l) {
                        l.events.un({
                            "visibilitychanged": onLayerVisibilitychanged,
                            scope: this
                        });
                    });
                    ctrl.events.un(ctrlEventsConfig);
                    ctrl.destroy();
                }
                var controlClass = (type === "WMS") ? 
                    OpenLayers.Control.WMSGetFeatureInfo :
                    OpenLayers.Control.WMTSGetFeatureInfo;

                ctrl = new controlClass({
                    layers: layers,
                    maxFeatures: GEOR.config.MAX_FEATURES,
                    infoFormat: 'application/vnd.ogc.gml'
                });
                ctrl.events.on(ctrlEventsConfig);
                map.addControl(ctrl);
                ctrl.activate();

                Ext.each(layers, function(l) {
                    l.events.on({
                        "visibilitychanged": onLayerVisibilitychanged,
                        scope: this
                    });
                });
                map.events.on({
                    "removelayer": onLayerRemoved,
                    scope: this
                });

            } else {
                // we only want to collapse if the layers array is the same as
                // ctrl.layers, which would mean that we toggled up the button
                // which was already toggled down. Otherwise, it means we went
                // from querying a layer to another, or from/to a single-layer
                // query to a multi-layer query
                var collapse = false;
                if(layers.length == ctrl.layers.length) {
                    collapse = true;
                    for(var i = 0; i < layers.length; i++) {
                        if(ctrl.layers[i] != layers[i]) {
                            collapse = false;
                        }
                    }
                }
                if (collapse) {
                    // we clicked on a toolbar button, which means we have
                    // to stop gfi requests.
                    //
                    // note: IE produces a js error when reloading the page
                    // with the gfi control activated, this is because
                    // ctrl.deactivate() is called here while the control
                    // has been destroyed and its events property set to
                    // null, let's guard against that by not attempting
                    // to deactivate the control if ctrl.events is null.
                    if (ctrl.events !== null) {
                        ctrl.deactivate();
                    }
                    // we need to collapse the south panel.
                    observable.fireEvent("shutdown");
                    // remove visibility events from previous array of layers
                    Ext.each(ctrl.layers, function(l) {
                        l.events.un({
                            "visibilitychanged": onLayerVisibilitychanged,
                            scope: this
                        });
                    });
                    map.events.un({
                        "removelayer": onLayerRemoved,
                        scope: this
                    });
                } else {
                    // we asked for gfi on another layer
                }
            }
       }
   };
})();


