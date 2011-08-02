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
 * @include OpenLayers/Control/MousePosition.js
 * @include OpenLayers/Control/ScaleLine.js
 * @include OpenLayers/Control/LoadingPanel.js
 * @include GeoExt/widgets/MapPanel.js
 * @include GEOR_toolbar.js
 * @include GEOR_scalecombo.js
 * @include GEOR_proj4jsdefs.js
 */

Ext.namespace("GEOR");

GEOR.mappanel = (function() {

    var cmp_id = 'GEOR_mappanel';

    /**
     * Method: buildMousePositionCtrl
     * Build a mouse position control.
     *
     * Parameters:
     * {String} projCode The EPSG code.
     * {DOMElement} The DOM element the control must be drawn in.
     *
     * Returns:
     * {OpenLayers.Control.MousePosition}
     */
    var buildMousePositionCtrl = function(projCode, div) {
        var options = {
            div: div,
            displayProjection: new OpenLayers.Projection(projCode),
            formatOutput: function(lonlat) {
                // "this" holds a reference to the MousePosition
                // control
                return this.displayProjection.toString() +
                       ": " + OpenLayers.Number.format(lonlat.lon, 0) +
                       " / " + OpenLayers.Number.format(lonlat.lat, 0);
            }
        };
        return new OpenLayers.Control.MousePosition(options);
    };

    /**
     * Method: buildLoadingPanelCtrl
     * Build a loading panel control.
     *
     * Parameters:
     * {DOMElement} The DOM element the control must be drawn in.
     *
     * Returns:
     * {OpenLayers.Control.LoadingPanel}
     */
    var buildLoadingPanelCtrl = function(div) {
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

    /**
     * Method: buildBbarCfg
     * Build the bottom toolbar config
     *
     * Parameters:
     * map - {OpenLayers.Map}
     *
     * Returns:
     * {Object} An object with a "buttons" property referencing
     *     an array of toolbar items.
     */
    var buildBbarCfg = function(map) {
        var div, items = [];

        // Scale Line
        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div", 
            id: cmp_id+"_scaleline",
            cls: "olControlScaleLine"
        });
        items.push(div);
        map.addControl(new OpenLayers.Control.ScaleLine({div: div}));
        
        // Loading panel
        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div", 
            id: cmp_id+"_loading_panel",
            cls: "olControlLoadingPanel"
        });
        items.push(div);
        map.addControl(buildLoadingPanelCtrl(div));

        // greedy spacer
        items.push("->");

        // EPSG:2154 mouse position
        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div", 
            qtip: "Coordonnées du pointeur en Lambert 93",
            id: cmp_id+"_mp2154", 
            cls: "mouseposition"
        });
        items.push(div);
        map.addControl(buildMousePositionCtrl("EPSG:2154", div));

        // EPSG:3948 mouse position
        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div", 
            qtip: "Coordonnées du pointeur en RGF93/CC48",
            id: cmp_id+"_mp3948", 
            cls: "mouseposition"
        });
        items.push(div);
        map.addControl(buildMousePositionCtrl("EPSG:3948", div));

        // Scale combobox
        items.push(Ext.apply({
            width: 130
        }, GEOR.scalecombo.create(map)));

        return {
            items: items
        };
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Return the map panel config.
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application layer store.
         */
        create: function(layerStore) {
            var map = layerStore.map;
            return {
                xtype: "gx_mappanel",
                id: "mappanel",
                map: map,
                layers: layerStore,
                tbar: GEOR.toolbar.create(layerStore),
                bbar: new Ext.Toolbar(buildBbarCfg(map)),
                // hack for better ergonomics:
                updateMapSize: function(){}
            };
        }
    };
})();
