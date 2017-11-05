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

Ext.define('Analytics.view.BaseGridPanel', {
    extend: 'Ext.grid.Panel',
    requires: ['Ext.PagingToolbar', 'Ext.LoadMask'],
    border: true,
    forceFit: true,
    
    initComponent: function() {
        var tr = Analytics.Lang.i18n, 
            store = this.getStore();
        Ext.apply(this, {
            tools: [{
                type: 'save',
                tooltip: tr("Export to CSV")
            }],
            selModel: {
                mode: 'single'
            },
            // Evolution: use http://docs.sencha.com/ext-js/4-0/#!/example/grid/infinite-scroll.html
            bbar: Ext.create('Ext.PagingToolbar', {
                store: store,
                displayInfo: true,
                beforePageText: tr("Page"),
                afterPageText: tr("of N1"),
                displayMsg: tr('Records N0 to N1 of N2'),
                emptyMsg: tr("No records")
            }),
            listeners: {
                "itemdblclick": this.onItemDoubleClick,
                "afterrender": function() { 
                    this.loadingmask = new Ext.LoadMask(this, {
                        msg: tr("Loading..."), 
                        store: Ext.data.StoreManager.lookup(store) 
                    });
                },
                scope: this
            }
        });
        this.callParent();
    },
    
    onItemDoubleClick: function(view, rec, item, idx, e) {
        // nothing by default
    }
});