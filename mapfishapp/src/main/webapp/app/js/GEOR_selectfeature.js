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
 * @include GEOR_FeatureDataModel.js
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
         * options - {Object} A hash containing response, model and format
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
     * {OpenLayers.Control.SelectFeature} The control.
     */
    var ctrl = null;

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;
    
    /**
     * Property: model
     * {GEOR.FeatureDataModel} data model
     */
    var model = null;
    
    // indexed by their id
    var selectedFeatures = {};
    
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


    var toArray = function(o) {
        var out = [];
        for (var f in o) {
            if (!o.hasOwnProperty(f)) {
                continue;
            }
            out.push(o[f]);
        }
        return out;
    };


    var clone = function(features) {
        var out = new Array(features.length);
        Ext.each(features, function(f, i) {
            out[i] = f.clone();
        });
        return out;
    };


    var onFeatureselected = function(o) {
        var f = o.feature;
        selectedFeatures[f.id] = f;
        
        if (!model || model.isEmpty()) {
            model = new GEOR.FeatureDataModel({
                features: [f]
            });
        }
        
        observable.fireEvent("searchresults", {
            features: clone(toArray(selectedFeatures)),
            model: model,
            tooltip: ctrl.layer.name + " - OpenLayers SelectFeature",
            title: GEOR.util.shortenLayerName(ctrl.layer.name),
            // we do not want the generated vector layer 
            // to be added to the map object:
            addLayerToMap: false
        });
    };


    var onFeatureunselected = function(o) {
        delete selectedFeatures[o.feature.id];

        observable.fireEvent("searchresults", {
            features: clone(toArray(selectedFeatures)),
            model: model,
            tooltip: ctrl.layer.name + " - OpenLayers SelectFeature",
            title: GEOR.util.shortenLayerName(ctrl.layer.name),
            // we do not want the generated vector layer 
            // to be added to the map object:
            addLayerToMap: false
        });
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
                observable.fireEvent("search", {
                    html: '<div>Recherche d\'objets activée sur la couche '+
                        title+'. Cliquez sur la carte.</div>'
                });
            
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
                // clear model cache:
                model = null;
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
                            features: [],
                            addLayerToMap: false
                        });
                        selectedFeatures = {};
                        ctrl.unselectAll();
                        ctrl.deactivate();
                    }
                    // we need to collapse the south panel.
                    observable.fireEvent("shutdown");
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
