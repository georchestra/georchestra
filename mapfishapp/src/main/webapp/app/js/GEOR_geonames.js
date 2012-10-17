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
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.geonames = (function() {

    /*
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /*
     * Method: createCbSearch
     * Returns: {Ext.form.ComboBox}
     */
    var createCbSearch = function() {

        //Handles data coming from geonames WS
        var dsGeonames = new Ext.data.Store({
            // uses geonames WS (ScriptTagProxy because remote server)
            proxy: new Ext.data.ScriptTagProxy({
                url: 'http://ws.geonames.org/searchJSON'
            }),
            reader: new Ext.data.JsonReader({
                root: 'geonames',
                totalProperty: 'totalResultsCount'
            }, [
                {name: 'name'},
                {name: 'lat'},
                {name: 'lng'}
            ]),
            // geonames filters
            baseParams: GEOR.config.GEONAMES_FILTERS
        });

        // Template to present results
        var tplResult = new Ext.XTemplate(
            '<tpl for="."><div class="search-item">',
                //"<strong>{name}</strong>",
                "{name}",
            '</div></tpl>'
        );

        // Autocomplete ComboBox to populate and display dataStore
        var cbSearch = new Ext.form.ComboBox({
            fieldLabel: tr('Go to: '),
            labelSeparator: '',
            store: dsGeonames,
            loadingText: tr('Loading...'),
            width: 180,
            listWidth: 180,
            queryDelay: 100,
            hideTrigger:true,
            tpl: tplResult,                      // template to display results
            itemSelector: 'div.search-item',     // needed by the template
            queryParam: 'name_startsWith',       // geonames filter
            minChars: 2,                         // min characters number to
                                                  // trigger the search
            pageSize: 0,                         // removes paging toolbar
            listeners: {
                select: function(combo, record, index) {
                    // geonames lon/lat are in EPSG:4326
                    var lonlat = new OpenLayers.LonLat(
                        record.data.lng,
                        record.data.lat
                    );

                    // convert to the map's projection
                    lonlat.transform(
                        new OpenLayers.Projection("EPSG:4326"),
                        map.getProjectionObject()
                    );

                    // center map to POI
                    map.setCenter(lonlat, map.baseLayer.numZoomLevels - GEOR.config.GEONAMES_ZOOMLEVEL);
                }
            }
        });	
        return cbSearch;
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
            tr = OpenLayers.i18n;
            return {
                xtype: 'form',
                labelWidth: 50,
                //title: 'Localisation',
                items: [
                    createCbSearch()
                ]
            };
        }
    };
})();
