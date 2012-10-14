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
      projection: 'EPSG:900913',
      maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34),
      resolutions: [ 156543.03392804097,
                     78271.516964020484,
                     39135.758482010242,
                     19567.879241005121,
                     9783.9396205025605,
                     4891.9698102512803,
                     2445.9849051256401,
                     1222.9924525628201,
                     611.49622628141003,
                     305.74811314070502,
                     152.87405657035251,
                     76.437028285176254,
                     38.218514142588127,
                     19.109257071294063,
                     9.5546285356470317,
                     4.7773142678235159,
                     2.3886571339117579,
                     1.194328566955879,
                     0.59716428347793948,
                     0.29858214173896974 ],
      transitionEffect: 'resize',
      displayOutsideMaxExtent: true,
      units: 'm',
      buffer:0,
      attribution:'<span style="background-color:#fff">data by <a href="http://openstreetmap.org">openstreetmap</a></span>'
    },
    layerFactory: function() {
      return [
        new OpenLayers.Layer.WMS('Baselayer','http://maps.qualitystreetmap.org/tilecache/tilecache.py', {layers:'osm',format: 'image/png' },{tileSize:new OpenLayers.Size(256,256), isBaseLayer: true})
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
        ['Geoserver', 'http://@shared.server.name@/geoserver/wms?']
    ]
};


