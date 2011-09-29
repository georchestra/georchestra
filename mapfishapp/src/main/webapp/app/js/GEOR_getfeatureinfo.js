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
 * @include OpenLayers/Format/WMSGetFeatureInfo.js
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
     * {OpenLayers.Control.WMSGetFeatureInfo} The control.
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
        
        var features = info.features;
        
        if (!model || model.isEmpty()) {
            model = new GEOR.FeatureDataModel({
                features: features
            });
        }
        
        observable.fireEvent("searchresults", {
            features: features,
            model: model 
            // we do not know the model with GFI at first time.
            // but at second time, we can use cached model
        });
    };
    
    /**
     * Method: onBeforegetfeatureinfo
     * Callback executed just before getFeatureInfo request is triggered
     */
    var onBeforegetfeatureinfo = function() {
        // to let OL use its own cursor class:
        OpenLayers.Element.removeClass(map.viewPortDiv, "olDrawBox");
        
        observable.fireEvent("search", {
            html: '<div>Recherche en cours...</div>'
        }); 
    };
    
    /**
     * Method: onLayerVisibilitychanged
     * Callback executed on WMS layer visibility changed
     * We need to deactivate ouselves
     */
    var onLayerVisibilitychanged = function() {
        if (!ctrl.layers[0].visibility) {
            this.toggle(ctrl.layers[0], false);
        }
    };
    
    /**
     * Method: onLayerRemoved 
     * Callback executed on WMS layer removed from map
     * We need to deactivate ouselves
     */
    var onLayerRemoved = function(options) {
        if (options.layer === ctrl.layers[0]) {
            this.toggle(options.layer, false);
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
        init: function(m) { 
            map = m;
        },

        /**
         * APIMethod: toggle
         *
         * Parameters:
         * record - {GeoExt.data.LayerRecord | OpenLayers.Layer.WMS} the layer
         * state - {Boolean} Toggle to true or false this layer ?
         */
        toggle: function(record, state) {
            var layer, title;
            if (record instanceof OpenLayers.Layer.WMS) {
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
                
                var ctrlEventsConfig = {
                    "beforegetfeatureinfo": onBeforegetfeatureinfo,
                    "getfeatureinfo": onGetfeatureinfo,
                    "activate": onCtrlactivate,
                    "deactivate": onCtrldeactivate,
                    scope: this
                };
            
                // we'd like to activate gfi request on layer
                if (ctrl) {
                    ctrl.events.un(ctrlEventsConfig);
                    ctrl.destroy();
                }
                ctrl = new OpenLayers.Control.WMSGetFeatureInfo({
                    layers: [layer],
                    maxFeatures: GEOR.config.MAX_FEATURES,
                    infoFormat: 'application/vnd.ogc.gml'
                });
                ctrl.events.on(ctrlEventsConfig);
                map.addControl(ctrl);
                ctrl.activate();
                
                layer.events.on({
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
                if (ctrl.layers[0] === layer) {
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
                } else {
                    // we asked for gfi on another layer
                }
                // in either case, we clean events on ctrl's layer
                ctrl.layers[0].events.un({
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


