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

Ext.namespace("GEOB");

GEOB.recenter = (function() {

    /*
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;
      
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
            baseParams: {
                country: 'FR',         // France
                adminCode1: 'A2',      // Bretagne
                style: 'short',        // verbosity of results
                lang: 'fr',
                featureClass: 'P',     // class category: populated places
                maxRows: 20            // maximal number of results	
            }
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
            fieldLabel: 'Aller à',
            labelSeparator: ' :',
            store: dsGeonames,
            loadingText: 'Chargement...',
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
                    map.setCenter(lonlat, map.numZoomLevels - 2);
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
