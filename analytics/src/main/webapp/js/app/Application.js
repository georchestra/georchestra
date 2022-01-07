/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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
    'Analytics.store.OGCOrgs',
    'Analytics.store.OGCUsers'
]);

Ext.application({
    name: 'Analytics',
    appFolder:'resources/js/app', // strange that it needs to be here // kind of redundant with the above Ext.Loader paths
    autoCreateViewport: true, // By setting autoCreateViewport to true, the framework will, by convention, include the app/view/Viewport.js file
    models: ['OGCLayer','OGCUser','OGCOrg'],
    stores: [
        'OGCLayers','OGCUsers','OGCOrgs',
        'FilteredOGCLayers','FilteredOGCUsers'
    ],
    controllers: ['OGC', 'Month'],
    launch: function() {
        // TODO
    }
});