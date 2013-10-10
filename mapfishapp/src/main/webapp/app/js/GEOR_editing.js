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
 The two following items are for GeoExt.form.toFilter (pulled by GeoExt.plugins.AttributeForm)
 Note that OpenLayers/Filter/Comparison.js is also added in the [first] section of jsbuild
 in order to be sure that its namespaces are defined before GeoExt.form.toFilter is read
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Filter/Comparison.js
 */

Ext.namespace("GEOR");

GEOR.editing = (function() {

    /*
     * Private
     */

    /**
     * Constant: NSALIAS
     * {String} The editing layers' namespace alias as defined in
     * the GeoServer configuration. Acts as a shortcut for the WFST
     * virtual service providing editable layers.
     */
    var NSALIAS = null;

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
            NSALIAS = GEOR.config.NS_EDIT;
            var store = GEOR.ows.WFSCapabilities({
                var onlineResource = GEOR.config.GEOSERVER_WFS_URL.replace(/(\/geoserver\/)/i,"$1" + NSALIAS + "/");
                storeOptions: {
                    url: onlineResource,
                    protocolOptions: {
                        srsName: map.getProjection(),
                        srsNameInQuery: true, // see http://trac.osgeo.org/openlayers/ticket/2228
                        url: onlineResource
                    },
                    layerOptions: {
                        dispayInLayerSwitcher: false
                    }
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
