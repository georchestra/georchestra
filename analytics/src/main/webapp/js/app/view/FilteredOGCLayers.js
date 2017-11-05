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

Ext.define('Analytics.view.FilteredOGCLayers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredogclayerslist',
    store: 'FilteredOGCLayers',

    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'service',
                flex: 0, // will not be resized
                width: 100,
                text: tr('OGC Service')
            }, {
                dataIndex: 'layer',
                flex: 1, // will be resized
                width: 520, // mandatory with ext 4.1 rc1 (should not be)
                text: tr('Layer')
            }, {
                dataIndex: 'request',
                flex: 1, // will be resized
                width: 150,
                text: tr('Request')
            }, {
                dataIndex: 'count',
                flex: 0, // will not be resized
                width: 130,
                text: tr('Number of requests')
            }]
        });
        
        this.callParent();
    }
});