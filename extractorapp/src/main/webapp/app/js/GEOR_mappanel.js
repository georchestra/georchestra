/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

/*
 * @include OpenLayers/Control/MousePosition.js
 * @include OpenLayers/Control/ScaleLine.js
 * @include GeoExt/widgets/MapPanel.js
 * @include GEOR_toolbar.js
 * @include GEOR_config.js
 * @include GEOR_scalecombo.js
 */

Ext.namespace("GEOR");

GEOR.mappanel = (function() {

    var cmp_id = 'GEOR_mappanel';

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

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
        var format = (projCode == "EPSG:4326") ?
            function(n) {return OpenLayers.Number.format(n, 5)} :
            function(n) {return OpenLayers.Number.format(n, 0)} ;
        var options = {
            div: div,
            displayProjection: new OpenLayers.Projection(projCode),
            formatOutput: function(lonlat) {
                // "this" holds a reference to the MousePosition
                // control
                return this.displayProjection.toString() +
                       ": " + format(lonlat.lon) +
                       " / " + format(lonlat.lat);
            }
        };
        return new OpenLayers.Control.MousePosition(options);
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

        // greedy spacer
        items.push("->");

        // First mouse position
        var srs1 = GEOR.config.MAP_POS_SRS1;
        if (srs1) {
            div = Ext.DomHelper.append(Ext.getBody(), {
                tag: "div",
                qtip: tr("mappanel.qtip.coordinates", {'SRS': srs1}),
                id: cmp_id+"_mp1",
                cls: "mouseposition"
            });
            items.push(div);
            map.addControl(buildMousePositionCtrl(srs1, div));
        }
        // Second mouse position
        var srs2 = GEOR.config.MAP_POS_SRS2;
        if (srs2) {
            div = Ext.DomHelper.append(Ext.getBody(), {
                tag: "div",
                qtip: tr("mappanel.qtip.coordinates", {'SRS': srs2}),
                id: cmp_id+"_mp2",
                cls: "mouseposition"
            });
            items.push(div);
            map.addControl(buildMousePositionCtrl(srs2, div));
        }
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
         * map - {OpenLayers.Map} The application map object
         * options - {Object} options applied to the config
         */
        create: function(map, options) {
            return Ext.apply({
                xtype: "gx_mappanel",
                stateful: false,
                id: "mappanel",
                map: map,
                tbar: GEOR.toolbar.create(map),
                bbar: new Ext.Toolbar(buildBbarCfg(map))
            }, options);
        }
    };
})();
