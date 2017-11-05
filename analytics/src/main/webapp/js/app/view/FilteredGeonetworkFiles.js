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

Ext.define('Analytics.view.FilteredGeonetworkFiles', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.filteredgeonetworkfileslist',
    store: 'FilteredGeonetworkFiles',
    
    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        this.columns = Ext.apply(this.columns || {}, {
            items: [{
                dataIndex: 'metadata_id',
                flex: 0, // will not be resized
                width: 100,
                text: tr('Metadata'),
                renderer: function(v) {
                    if (!v) return;
                    var url = '/geonetwork/?id='+v;
                    return '<a href="'+url+'" target="_blank">'+v+'</a>'
                }
            }, {
                dataIndex: 'filename',
                flex: 1, // will be resized
                width: 570,
                text: tr('File')
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