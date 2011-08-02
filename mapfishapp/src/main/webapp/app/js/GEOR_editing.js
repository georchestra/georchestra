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
 * @include GEOR_Editing/GEOR_EditingPanel.js
 * @include GEOR_Editing/GEOR_LayerEditingPanel.js
 * @include GEOR_ows.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.editing = (function() {

    /*
     * Private
     */

    /**
     * Constant: NSALIAS
     * {String} The editing layers' namespace alias as defined in
     *    the GeoServer configuration.
     */
    var NSALIAS = "geob_edit";

    /*
     * Public
     */
    return {
    
        /**
         * APIMethod: create
         * Create the editing panel.
         *
         * Returns:
         * {<GEOR.Editing.EditingPanel>} The editing panel config.
         */
        create: function(map) {
            var store = GEOR.ows.WFSCapabilities({
                storeOptions: {
                    url: GEOR.config.GEOSERVER_WFS_URL,
                    protocolOptions: {
                        srsName: map.getProjection(),
                        url: GEOR.config.GEOSERVER_WFS_URL
                    },
                    layerOptions: {
                        dispayInLayerSwitcher: false
                    }
                },
                vendorParams: {
                    namespace: NSALIAS
                }
            });
            return {
                xtype: "geor_editingpanel",
                map: map,
                store: store,
                nsAlias: NSALIAS
            };
        }
    };
})();
