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
 * @include OpenLayers/Control/GetFeature.js
 * @include OpenLayers/Control/ModifyFeature.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Strategy/Save.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.edit = (function() {

    /*
     * Private
     */

    /**
     * Property: getfeature
     * {OpenLayers.Control.GetFeature}
     */
    var getFeature;

    /**
     * Property: modifyFeature
     * {OpenLayers.Control.ModifyFeature}
     */
    var modifyFeature;

    /**
     * Property: vectorLayer
     * {OpenLayers.Layer.Vector}
     */
    var vectorLayer;

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    var map;

    /**
     * Property: strategy
     * {OpenLayers.Strategy.Save}
     */
    var strategy;

    var tr;

    return {
    
        /*
         * Observable object
         */
        //events: observable,
        
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
        },
        
        /*
         * Method: activate
         *
         * Parameters:
         * options - {Object}
         */
        activate: function(options) {
            GEOR.edit.deactivate();
            getFeature = new OpenLayers.Control.GetFeature({
                protocol: options.protocol,
                autoActivate: true, // Do not forget to manually deactivate it !
                multiple: false,
                hover: true,
                click: false,
                single: true,
                maxFeatures: 1,
                clickTolerance: 5,
                eventListeners: {
                    "hoverfeature": function(e) {
                        vectorLayer.removeFeatures(vectorLayer.features[0]);
                        vectorLayer.addFeatures([e.feature], {
                            silent: true // we do not want to trigger save on feature added
                        });
                    }/*,
                    "outfeature": function(e) {
                        vectorLayer.removeFeatures([e.feature]);
                    }*/
                }
            });
            strategy = new OpenLayers.Strategy.Save({
                auto: true,
                autoDestroy: true
            });
            strategy.events.on({
                "start": function() {
                    GEOR.waiter.show();
                },
                "success": function() {
                    options.layer.mergeNewParams({
                        nocache: new Date().valueOf()
                    });
                },
                "fail": function() {
                    options.layer.setVisibility(true);
                    GEOR.util.errorDialog({
                        msg: OpenLayers.i18n('Synchronization failed.')
                    });
                    // TODO: handle this case
                }
            });
            vectorLayer = new OpenLayers.Layer.Vector("_geor_edit", {
                displayInLayerSwitcher: false,
                protocol: options.protocol,
                strategies: [strategy],
                eventListeners: {
                    "beforefeaturemodified": function(o) {
                        // we do not want to trigger additional useless XHRs 
                        // once one feature has been chosen for edition:
                        getFeature.deactivate();
                        // TODO: show attributes panel for edition, configure ModifyFeature control
                    },
                    "afterfeaturemodified": function(o) {
                        // reactivate getFeature once feature is unselected
                        getFeature.activate();
                    }
                }
            });
            modifyFeature = new OpenLayers.Control.ModifyFeature(vectorLayer, {
                autoActivate: true, // Do not forget to manually deactivate it !
                clickout: true,
                toggle: false,
                mode: OpenLayers.Control.ModifyFeature.DRAG | 
                    OpenLayers.Control.ModifyFeature.RESHAPE
            });
            map.addLayer(vectorLayer);
            map.addControls([getFeature, modifyFeature]);
        },

        /*
         * Method: deactivate
         *
         * Parameters:
         *  - {}
         */
        deactivate: function() {
            // TODO: inform managelayers that edition has stopped on other layer ?
            // (to update its menu item)
            if (modifyFeature && getFeature) {
                modifyFeature.deactivate();
                getFeature.deactivate();
                // will take care of removing them from map:
                modifyFeature.destroy();
                getFeature.destroy();
                modifyFeature = null;
                getFeature = null;
            }
            // will take care of destroying protocol 
            // and strategy in correct order, 
            // unregistering listeners, removing from map:
            if (vectorLayer) {
                vectorLayer.destroy();
                vectorLayer = null;
                strategy = null;
            }
        }
    };
})();
