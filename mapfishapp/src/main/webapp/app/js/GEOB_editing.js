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
 * @include GEOB_Editing/GEOB_EditingPanel.js
 * @include GEOB_Editing/GEOB_LayerEditingPanel.js
 * @include GEOB_ows.js
 * @include GEOB_config.js
 */

Ext.namespace("GEOB");

GEOB.editing = (function() {

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
         * {<GEOB.Editing.EditingPanel>} The editing panel config.
         */
        create: function(map) {
            var store = GEOB.ows.WFSCapabilities({
                storeOptions: {
                    url: GEOB.config.GEOSERVER_WFS_URL,
                    protocolOptions: {
                        srsName: map.getProjection(),
                        url: GEOB.config.GEOSERVER_WFS_URL
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
                xtype: "geob_editingpanel",
                map: map,
                store: store,
                nsAlias: NSALIAS
            };
        }
    };
})();
