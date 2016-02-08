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

Ext.define('Analytics.view.GeonetworkFiles', {
    extend: 'Analytics.view.FilteredGeonetworkFiles',
    alias: 'widget.geonetworkfileslist',
    store: 'GeonetworkFiles',

    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        // in order to have the tooltip over each row:
        this.columns = {
            defaults: {
                renderer: function(value, md){ 
                    var qtip=tr("double-click to see details");
                    md.tdAttr = 'data-qtip="'+qtip+'"';
                    return value;
                }
            }
        };
        this.callParent();
    },

    onItemDoubleClick: function(view, rec) {
        var st = Ext.getStore('FilteredGeonetworkUsers');
        st.filters.clear();       
        st.filter([{
            property: 'filename',
            value: rec.get('filename')
        }, {
            property: 'metadata_id',
            value: rec.get('metadata_id')
        }]);

        new Ext.Window({
            title: [
                tr('The last users who downloaded the file')+" ",
                rec.get('filename'),
                tr('from metadata'),
                rec.get('metadata_id')
            ].join(' '),
            closeAction: 'hide',
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkUsers', {
                border: false
            })]
        }).show();
    }
});
