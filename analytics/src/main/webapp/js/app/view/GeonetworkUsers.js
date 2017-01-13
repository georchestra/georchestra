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

Ext.define('Analytics.view.GeonetworkUsers', {
    extend: 'Analytics.view.FilteredGeonetworkUsers',
    alias: 'widget.geonetworkuserslist',
    store: 'GeonetworkUsers',

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
        var st = Ext.getStore('FilteredGeonetworkFiles');
        st.filters.clear();
        st.filter([{
            property: 'username',
            value: rec.get('username')
        }]);
        
        new Ext.Window({
            title: tr("Files downloaded by the user")+" "+
                rec.get('username'),
            closeAction: 'hide',
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredGeonetworkFiles', {
                border: false
            })]
        }).show();
    }
});

