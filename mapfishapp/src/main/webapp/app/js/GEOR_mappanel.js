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
 * @include OpenLayers/Projection.js
 * @include GeoExt/widgets/MapPanel.js
 * @include GEOR_toolbar.js
 * @include GEOR_config.js
 * @include GEOR_scalecombo.js
 */

Ext.namespace("GEOR");

GEOR.mappanel = (function() {

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: mpControl
     * {OpenLayers.Control.MousePosition}
     */
    var mpControl = null;

    /**
     * Method: formatMousePositionOutput
     * creates a mouse position formatter 
     *
     * Parameters:
     * {String} projCode The EPSG code.
     *
     * Returns:
     * {Function}
     */
    var formatMousePositionOutput = function(projCode) {
        var format, firstPrefix, secondPrefix, 
            p = new OpenLayers.Projection(projCode);
        if (!p.proj) {
            alert("Missing definition of "+projCode+" for the output mouse position !");
            return;
        }
        if (p.proj.projName === "longlat") {
            format = function(n) {return OpenLayers.Number.format(n, 5)};
            firstPrefix = "Lon = ";
            secondPrefix = ", Lat = ";
        } else {
            format = function(n) {return OpenLayers.Number.format(n, 0)};
            firstPrefix = "X = ";
            secondPrefix = ", Y = ";
        }
        return function(lonlat) {
            return [firstPrefix, format(lonlat.lon), 
                secondPrefix, format(lonlat.lat)].join('');
        }
    };

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
        return new OpenLayers.Control.MousePosition({
            div: div,
            displayProjection: new OpenLayers.Projection(projCode),
            formatOutput: formatMousePositionOutput(projCode)
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

        // Scale combobox
        items.push(Ext.apply({
            width: 110,
            value: tr('scale picker')
        }, GEOR.scalecombo.create(map)));

        // Scale Line
        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div",
            cls: "olControlScaleLine"
        });
        items.push(div);
        map.addControl(new OpenLayers.Control.ScaleLine({div: div}));

        // greedy spacer
        items.push("->");

        // Pointer coordinates
        var srsList = GEOR.config.POINTER_POSITION_SRS_LIST,
            srs = srsList[0][0];
        
        items.push(tr("Coordonnées en "));
        items.push({
            xtype: 'combo',
            width: 110,
            store: srsList,
            value: srsList[0][1],
            editable: false,
            tpl: [
                '<tpl for=".">',
                '<div class="x-combo-list-item" ext:qtip="{field2} - {field1}" >',
                '{field2}',
                '</div>',
                '</tpl>'
            ].join(''),
            triggerAction: 'all',
            mode: 'local',
            listeners: {
                select: function(combo, record, index) {
                    mpControl.displayProjection = 
                        new OpenLayers.Projection(record.data['field1']);
                    mpControl.formatOutput = 
                        formatMousePositionOutput(record.data['field1']);
                }
            }
        });

        div = Ext.DomHelper.append(Ext.getBody(), {
            tag: "div",
            cls: "mouseposition"
        });
        items.push(div);
        mpControl = buildMousePositionCtrl(srs, div);
        map.addControl(mpControl);

        return {
            id: "bbar",
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
            tr = OpenLayers.i18n;
            return new GeoExt.MapPanel({
                xtype: "gx_mappanel",
                region: "center",
                id: "mappanel",
                stateful: false,
                map: map,
                layers: layerStore,
                tbar: GEOR.toolbar.create(layerStore),
                bbar: new Ext.Toolbar(buildBbarCfg(map))
                // hack for better ergonomics:
                //,updateMapSize: function() {}
                // but is responsible of https://github.com/georchestra/georchestra/issues/367
            });
        }
    };
})();
