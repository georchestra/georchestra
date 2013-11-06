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
            if (!getFeature) {
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
            }
            if (!vectorLayer) {
                strategy = new OpenLayers.Strategy.Save({
                    auto: true
                });
                strategy.events.on({
                    "success": function() {
                        options.layer.mergeNewParams({
                            nocache: new Date().valueOf()
                        });
                    },
                    "fail": function() {
                        GEOR.util.errorDialog({
                            msg: OpenLayers.i18n('Synchronization failed.')
                        });
                    }
                });
                vectorLayer = new OpenLayers.Layer.Vector("_geor_edit", {
                    displayInLayerSwitcher: false,
                    protocol: options.protocol,
                    strategies: [
                        strategy
                    ],
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
                            // TODO: auto-commit
                        }
                    }
                });
                modifyFeature = new OpenLayers.Control.ModifyFeature(vectorLayer, {
                    autoActivate: true, // Do not forget to manually deactivate it !
                    clickout: true,
                    toggle: false,
                    mode: OpenLayers.Control.ModifyFeature.DRAG | OpenLayers.Control.ModifyFeature.RESHAPE
                });
            }
            map.addLayer(vectorLayer);
            map.addControls([getFeature, modifyFeature]);

        }
    };
})();
