/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace("GEOR");

/*
 * @include GeoExt.ux/OpenAddressesSearchCombo.js
 * @include GEOR_config.js
 */

GEOR.address = (function() {

    /**
     * Constant: URL
     * {String} The URL to the OpenAddresses web service.
     */
    var URL = null;

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
        var tr = OpenLayers.i18n;
        var a = new GeoExt.ux.OpenAddressesSearchCombo({
            map: map,
            url: URL,
            zoom: map.baseLayer.numZoomLevels-1,
            tpl: '<tpl for="."><div class="x-combo-list-item" qtip="{housenumber}, {street} - {city}">{housenumber}, {street} - {city}</div></tpl>',
            fieldLabel: tr("Go to: "),
            lang: 'fr',
            minChars: 3,
            labelSeparator: "",
            width: 180,
            listWidth: 180
        });
        a.loadingText = tr("searching...");
        a.emptyText = tr("adressSearchExemple");
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
            URL = GEOR.config.ADDRESS_URL;
            map = m;
            return {
                xtype: 'form',
                labelWidth: 50,
                items: [createOACombo()]
            };
        }
    };
})();
