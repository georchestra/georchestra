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
 * @include OpenLayers.Control.OutOfRangeLayers.js
 */

Ext.namespace("GEOR");

GEOR.map = (function() {

    /*
     * Private
     */

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

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
                    new OpenLayers.Control.Attribution(),
                    new OpenLayers.Control.OutOfRangeLayers({
                        prefix: tr("Layer probably invisible at this scale: "),
                        autoActivate: true
                    })
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
            var style = OpenLayers.Util.extend({},
                OpenLayers.Feature.Vector.style['default']);
            OpenLayers.Util.extend(style, {
                label: "${getArea}",
                fontColor: "blue",
                fontSize: "12px",
                fontFamily: "Courier New, monospace",
                fontWeight: "bold",
                labelAlign: "cm",
                labelXOffset: 0,
                labelYOffset: 0,
                labelOutlineColor: "white",
                labelOutlineWidth: 3
            })
            return new OpenLayers.Layer.Vector("Bbox layer", {
                geometryType: OpenLayers.Geometry.Polygon,
                styleMap: new OpenLayers.StyleMap(
                    new OpenLayers.Style(style, {
                        context: {
                            getArea: function(f) {
                                if (f && f.attributes && f.attributes.area) {
                                    return f.attributes.area;
                                }
                                return "";
                            }
                        }
                    })
                )
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
                tr("Base Layer"),
                GEOR.config.GEOSERVER_WMS_URL,
                {layers: GEOR.config.BASE_LAYER_NAME, format: "image/png"},
                OpenLayers.Util.applyDefaults(options, {
                    projection: GEOR.config.GLOBAL_EPSG,
                    maxExtent: GEOR.config.GLOBAL_MAX_EXTENT,
                    isBaseLayer: true,
                    maxResolution: "auto",
                    displayInLayerSwitcher: false,
                    singleTile: true,
                    ratio: 1,
                    transitionEffect: 'resize'
                })
            );
        }
    };
})();
