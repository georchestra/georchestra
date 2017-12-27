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
 * @include OpenLayers/Control/SelectFeature.js
 */

Ext.namespace("GEOR");

GEOR.selectfeature = (function() {

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
         * options - {Object} A hash containing response and format
         */
        "searchresults",

        /**
         * Event: shutdown
         * Fires when GFI tool is deactivated
         *
         */
        "shutdown"
    );

    /**
     * Property: ctrl
     * {OpenLayers.Control.SelectFeature} The control.
     */
    var ctrl = null;

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

    /**
     * Property: selectedFeatures
     * {Object} features indexed by their id
     */
    var selectedFeatures = null;

    /**
     * Method: onLayerVisibilitychanged
     * Callback executed on WMS layer visibility changed
     * We need to deactivate ouselves
     */
    var onLayerVisibilitychanged = function() {
        if (!ctrl.layer.visibility) {
            this.toggle(ctrl.layer, false);
        }
    };
    
    /**
     * Method: onLayerRemoved 
     * Callback executed on WMS layer removed from map
     * We need to deactivate ouselves
     */
    var onLayerRemoved = function(options) {
        if (options.layer === ctrl.layer) {
            this.toggle(options.layer, false);
        }
    };
    
    /**
     * Method: fireSearchresults
     * 
     */
    var fireSearchresults = function() {
        var fs = [];
        Ext.iterate(selectedFeatures, function(k, v) {
            fs.push(v.clone());
        });
        observable.fireEvent("searchresults", {
            features: fs,
            //ctrl: ctrl, // commented out to fix https://github.com/georchestra/georchestra/issues/785
            tooltip: ctrl.layer.name + " - " + tr("OpenLayers SelectFeature"),
            layerId: "selectfeature_" + ctrl.layer.id,
            title: GEOR.util.shortenLayerName(ctrl.layer.name),
            // we do not want the generated vector layer 
            // to be added to the map object:
            addLayerToMap: false
        });
    };

    /**
     * Method: onFeatureselected
     * Callback
     */
    var onFeatureselected = function(o) {
        var f = o.feature;
        selectedFeatures[f.id] = f;
        fireSearchresults();
    };

    /**
     * Method: onFeatureunselected
     * Callback
     */
    var onFeatureunselected = function(o) {
        delete selectedFeatures[o.feature.id];
        fireSearchresults();
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
        init: function(m) { 
            map = m;
            tr = OpenLayers.i18n;
            selectedFeatures = {};
        },

        /**
         * APIMethod: deactivate
         *
         */
        deactivate: function() {
            if (ctrl) {
                // we need to collapse the south panel.
                observable.fireEvent("shutdown");

                if (ctrl.events !== null) {
                    observable.fireEvent("searchresults", {
                        features: [],
                        layerId: "selectfeature_" + ctrl.layer.id,
                        addLayerToMap: false
                    });
                    selectedFeatures = {};
                    ctrl.unselectAll();
                    ctrl.deactivate();
                }

                if (ctrl.layer && ctrl.layer.events) {
                    ctrl.layer.events.un({
                        "featureselected": onFeatureselected,
                        "featureunselected": onFeatureunselected,
                        "visibilitychanged": onLayerVisibilitychanged,
                        scope: this
                    });
                }
                map.events.un({
                    "removelayer": onLayerRemoved,
                    scope: this
                });
            }
        },

        /**
         * APIMethod: toggle
         *
         * Parameters:
         * record - {GeoExt.data.LayerRecord | OpenLayers.Layer.Vector} the layer
         * state - {Boolean} Toggle to true or false this layer ?
         */
        toggle: function(record, state) {
            var layer, title;
            if (record instanceof OpenLayers.Layer.Vector) {
                layer = record;
                title = layer.name;
            } else if (record instanceof GeoExt.data.LayerRecord) {
                layer = record.get("layer");
                title = record.get("title");
            }
            if (state) {
            
                if (ctrl) {
                    ctrl.destroy();
                }
                ctrl = new OpenLayers.Control.SelectFeature(layer, {
                    multiple: true,
                    multipleKey: (Ext.isMac ? "metaKey" : "ctrlKey"),
                    toggle: true,
                    hover: false,
                    box: false,
                    clickout: false // required
                });
                ctrl.handlers.feature.stopDown = false;
                map.addControl(ctrl);
                ctrl.activate();
                
                layer.events.on({
                    "featureselected": onFeatureselected,
                    "featureunselected": onFeatureunselected,
                    "visibilitychanged": onLayerVisibilitychanged,
                    scope: this
                });
                map.events.on({
                    "removelayer": onLayerRemoved,
                    scope: this
                });
                
            } else {

                var ctrls = map.getControlsBy('active', true),
                    re = /OpenLayers\.Control\.(WMS|WMTS)GetFeatureInfo/,
                    collapse = true;
                for (var i = 0 ; i < ctrls.length; i++) {
                    if (re.test(ctrls[i].CLASS_NAME)) {
                        collapse = false;
                    }
                };

                if (ctrl.layer === layer) {
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
                        observable.fireEvent("searchresults", {
                            layerId: "selectfeature_" + ctrl.layer.id,
                            features: [],
                            addLayerToMap: false
                        });
                        selectedFeatures = {};
                        ctrl.unselectAll();
                        ctrl.deactivate();
                    }
                    if (collapse) {
                        // we need to collapse the south panel.
                        observable.fireEvent("shutdown");
                    }
                } else {
                    // we asked for gfi on another layer
                }
                // in either case, we clean events on ctrl's layer
                ctrl.layer.events && ctrl.layer.events.un({
                    "featureselected": onFeatureselected,
                    "featureunselected": onFeatureunselected,
                    "visibilitychanged": onLayerVisibilitychanged,
                    scope: this
                });
                map.events.un({
                    "removelayer": onLayerRemoved,
                    scope: this
                });
            }
        }
    };
})();
