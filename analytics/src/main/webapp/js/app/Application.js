/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

Ext.Loader.setConfig({
    enabled: true,
    disableCaching: false,
    paths: {
        'Ext': 'resources/js/lib/external/ext/src',
        'Analytics': 'resources/js/app'
    }
});

Ext.require([
    'Ext.grid.Panel',
    'Ext.data.Store',
    'Ext.layout.container.Border',
    'Ext.tab.Panel',
    // 'Analytics.store.*' surprisingly does not work, so:
    'Analytics.store.OGCLayers',
    'Analytics.store.OGCGroups',
    'Analytics.store.OGCUsers',
    'Analytics.store.GeonetworkFiles',
    'Analytics.store.GeonetworkUsers',
    'Analytics.store.GeonetworkGroups',
    'Analytics.store.ExtractorLayers',
    'Analytics.store.ExtractorGroups',
    'Analytics.store.ExtractorUsers'
]);

Ext.application({
    name: 'Analytics',
    appFolder:'resources/js/app', // strange that it needs to be here // kind of redundant with the above Ext.Loader paths
    autoCreateViewport: true, // By setting autoCreateViewport to true, the framework will, by convention, include the app/view/Viewport.js file
    models: ['OGCLayer','OGCUser','OGCGroup','GeonetworkFile','GeonetworkUser','GeonetworkGroup','ExtractorLayer','ExtractorUser','ExtractorGroup'],
    stores: [
        'OGCLayers','OGCUsers','OGCGroups',
        'GeonetworkFiles','GeonetworkUsers','GeonetworkGroups',
        'ExtractorLayers','ExtractorUsers', 'ExtractorGroups',
        'FilteredOGCLayers','FilteredOGCUsers',
        'FilteredGeonetworkFiles','FilteredGeonetworkUsers',
        'FilteredExtractorLayers','FilteredExtractorUsers'
    ],
    controllers: ['Geonetwork', 'Extractor', 'OGC', 'Month'],
    launch: function() {
        // TODO
    }
});