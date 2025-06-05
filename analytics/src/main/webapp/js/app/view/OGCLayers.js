/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define('Analytics.view.OGCLayers', {
    extend: 'Analytics.view.FilteredOGCLayers',
    alias: 'widget.ogclayerslist',
    store: 'OGCLayers',

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
    	var st = Ext.getStore('FilteredOGCUsers');
    	st.filters.clear();
        st.filter([{
            property: 'layer',
            value: rec.get('layer')
        }, {
            property: 'service',
            value: rec.get('service')
        }, {
            property: 'request',
            value: rec.get('request')
        }]);
        
        new Ext.Window({
            title: [
                tr('The last users who downloaded the layer'),
                rec.get('layer'),
                tr('from service'),
                rec.get('service')
            ].join(' '),
            closeAction: 'hide',
            width: 800,
            height: 400,
            layout: 'fit',
            items: [Ext.create('Analytics.view.FilteredOGCUsers', {
                border: false
            })]
        }).show();
    }
});