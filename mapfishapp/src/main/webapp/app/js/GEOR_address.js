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

Ext.namespace("GEOR");

/*
 * @include GeoExt.ux/OpenAddressesSearchCombo.js
 */

GEOR.address = (function() {

    /**
     * Constant: URL
     * {String} The URL to the OpenAddresses web service.
     */
    var URL = "/addrapp/addresses";

    /**
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;
      
    /**
     * Method: createOACombo
     * Create the OpenAddresses search combo.
     *
     * Returns:
     * {GeoExt.ux.openaddresses.OpenAddressesSearchCombo} The combo.
     */ 
    var createOACombo = function() {
        var a = new GeoExt.ux.OpenAddressesSearchCombo({
            map: map, 
            url: URL,
            zoom: map.baseLayer.numZoomLevels-1,
            tpl: '<tpl for="."><div class="x-combo-list-item" qtip="{housenumber}, {street} - {city}">{housenumber}, {street} - {city}</div></tpl>',
            fieldLabel: "Aller à",
            lang: 'fr',
            minChars: 3,
            labelSeparator: " :",
            width: 180,
            listWidth: 180
        });
        a.loadingText = "recherche en cours...";
        a.emptyText = "ex: 4, Hugo, Brest";
        return a;
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Returns the recenter panel config.
         *
         * Parameters:
         * m - {Openlayers.Map} The map object
         *
         * Returns:
         * {Ext.FormPanel} recenter panel config 
         */
        create: function(m) {
            map = m;
            return {
                xtype: 'form',
                labelWidth: 50,
                items: [createOACombo()]
            };
        }
    };
})();
