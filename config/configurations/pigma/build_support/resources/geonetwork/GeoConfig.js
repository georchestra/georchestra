/*
 * Copyright (C) 2011  Camptocamp
 *
 * This file is part of GeOrchestra
 *
 * GeOrchestra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */


/*
 * This file aims to export some configuration variables from the main
 *  GeoNetwork JS application.
 *  
 * @author pmauduit
 *
 */

/*
 * Note : this file is included after the Env object definition, but before
 * JS libraries inclusion. It is then possible to use the Env object (as
 * shown further with the wmsUrl definition).
 * 
 * FYI, the Env object defines the current variables (see header.xsl) :
 * 
 *          Env.host = "http://<xsl:value-of select="/root/gui/env/server/host"/>:<xsl:value-of select="/root/gui/env/server/port"/>";
 *          Env.locService= "<xsl:value-of select="/root/gui/locService"/>";
 *          Env.locUrl    = "<xsl:value-of select="/root/gui/locUrl"/>";
 *          Env.url       = "<xsl:value-of select="/root/gui/url"/>";
 *          Env.lang      = "<xsl:value-of select="/root/gui/language"/>";
 *          Env.proxy     = "<xsl:value-of select="/root/gui/config/proxy-url"/>";
 */
Ext.namespace("Geonetwork");
Geonetwork.CONFIG = {};

// Configuration variables for the GeoPublisher
Geonetwork.CONFIG.GeoPublisher = {
    // configuration for the base map used in the GeoPublisher interface
    // Map viewer options to use in main map viewer and in editor map viewer
    mapOptions: {
      projection: 'EPSG:4326',
      resolutions: [
        0.703125,
        0.3515625,
        0.17578125,
        0.087890625,
        0.0439453125,
        0.02197265625,
        0.010986328125,
        0.0054931640625,
        0.00274658203125,
        0.001373291015625,
        6.866455078125E-4,
        3.4332275390625E-4,
        1.71661376953125E-4,
        8.58306884765625E-5,
        4.291534423828125E-5,
        2.1457672119140625E-5,
        1.0728836059570312E-5,
        5.364418029785156E-6,
        2.682209014892578E-6,
        1.341104507446289E-6,
        6.705522537231445E-7,
        3.3527612686157227E-7,
        1.6763806343078613E-7],
      maxExtent: new OpenLayers.Bounds(-180.0, -90.0, 180.0, 90.0),
      units: 'degrees',
      restrictedExtent: new OpenLayers.Bounds(-2.2, 42.6, 1.9, 46)
    },
    layerFactory: function() {
      return [
        new OpenLayers.Layer.WMS('Baselayer','/geoserver/wms', {
            layers:'Fond_GIP',
            format: 'image/png'
        },{
            tileSize: new OpenLayers.Size(256, 256), 
            isBaseLayer: true
        })
      ];
    }
}

// Configuration for the minimap on the main page and the map in the editor
Geonetwork.CONFIG.SearchMap = {
    // define layer factory function for creating the layers to be put in the main map.  It is a function rather
    // than an array of layer instances because this allows one definition to be used multiple times
    layerFactory: Geonetwork.CONFIG.GeoPublisher.layerFactory,
    // Map viewer options to use in main map viewer and in editor map viewer
    mapOptions: Geonetwork.CONFIG.GeoPublisher.mapOptions
};
// Configuration for the main (large) on the main page
Geonetwork.CONFIG.MainMap = {
    // in this example the mapSeach and mainMap maps have the same layers but each can have their own layers if desired
    layerFactory: Geonetwork.CONFIG.SearchMap.layerFactory,
    // Map viewer options to use in main map viewer and in editor map viewer
    mapOptions: Geonetwork.CONFIG.SearchMap.mapOptions,
    projections: [
        ['EPSG:4326','LatLong'],
        ['EPSG:2154','France 1'],
        ['EPSG:27562','France 2']
    ],
    servers: [
        ['NASA JPL OneEarth Web Mapping Server (WMS)', 'http://wms.jpl.nasa.gov/wms.cgi?'],
        ['NASA Earth Observations (NEO) WMS', 'http://neowms.sci.gsfc.nasa.gov/wms/wms?'],
        ['DEMIS World Map Server', 'http://www2.demis.nl/mapserver/wms.asp?'],
        ['Geoserver', 'http://localhost:43080/geoserver/wms?']
    ]
};


