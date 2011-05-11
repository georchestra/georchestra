/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include OpenLayers/Map.js
 * @include OpenLayers/Layer/WMS.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include OpenLayers/Geometry/Polygon.js
 * @include OpenLayers/Control/Attribution.js
 * @include OpenLayers/Control/ZoomPanel.js
 * @include OpenLayers/Control/PanPanel.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Control/MousePosition.js
 * @include OpenLayers/Control/LoadingPanel.js
 */

Ext.namespace("GEOB");

GEOB.map = (function() {

    /*
     * Private
     */

    /**
     * Method: buildLoadingPanelCtrl
     * Build a loading panel control.
     *
     * Parameters
     * map - {OpenLayers.Map} The application's global map instance.
     *
     * Returns:
     * {OpenLayers.Control.LoadingPanel}
     */
    var buildLoadingPanelCtrl = function(map) {
        
        Ext.DomHelper.append(map.div, {
            tag: "div", 
            id: "extractor_loading_panel",
            cls: "olControlLoadingPanel"
        });
        var div = OpenLayers.Util.getElement("extractor_loading_panel");
        
        return new OpenLayers.Control.LoadingPanel({
            div: div,
            minimizeControl: function(evt) {
                this.div.style.display = "none";
                this.maximized = false;
                if (evt) {
                    OpenLayers.Event.stop(evt);
                }
            },
            maximizeControl: function(evt) {
                this.div.style.display = "block";
                this.maximized = true;
                if (evt) {
                    OpenLayers.Event.stop(evt);
                }
            }
        });
    };

    /*
     * Public
     */

    return {

        /**
         * APIMethod: create
         * Create the application's global map.
         *
         * Returns:
         * {OpenLayers.Map} The application's global map instance.
         */
        create: function() {
            
            var map = new OpenLayers.Map({
                controls: [
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.PanPanel(),
                    new OpenLayers.Control.ZoomPanel(),
                    new OpenLayers.Control.Attribution()
                ],
                theme: null
            });
            map.addControl(buildLoadingPanelCtrl(map));
            
            return map;
        },

        /**
         * APIMethod: createVectorLayer
         * Create the vector layer.
         * 
         * Returns:
         * {OpenLayers.Layer.Vector} The vector layer.
         */
        createVectorLayer: function() {
            return new OpenLayers.Layer.Vector("Bbox layer", {
                geometryType: OpenLayers.Geometry.Polygon
            });
        },

        /**
         * APIMethod: getBaseLayer
         * Create and return the base layer
         *
         * Parameters:
         * options - {Object} Options to be passed to layer creation.
         * 
         * Returns:
         * {OpenLayers.Layer} The base layer.
         */
        getBaseLayer: function(options) {
            return new OpenLayers.Layer.WMS(
                "Couche de base",
                GEOB.config.GEOSERVER_WMS_URL,
                {layers: GEOB.config.BASE_LAYER_NAME}, 
                OpenLayers.Util.applyDefaults(options, GEOB.config.BASE_LAYER_OPTIONS)
            );
        }
    };
})();
