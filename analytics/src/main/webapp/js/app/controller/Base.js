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

Ext.define('Analytics.controller.Base', {
    extend: 'Ext.app.Controller',
    
    month: null,
    year: null,
    
    init: function() {

    },
    
    loadStoreWithDate : function (store, cfg) {
    	store.getProxy().extraParams = {
    	    month: cfg.params.month,
    	    year: cfg.params.year
    	};
    	store.load();
    },
    
    onLaunch: function() {
        this.application.on({
            'monthchanged': function(opCfg) {
                // update a local copy of the current date (month + year)
                this.month = opCfg.params.month;
                this.year = opCfg.params.year;
            },
            scope: this
        });
    },
    
    handleExport: function(tool, evt) {
        var qso = {
            month: this.month === null ? 
                Ext.Date.format(new Date(), 'm') : this.month,
            year: this.year === null ? 
                Ext.Date.format(new Date(), 'Y') : this.year
        };
        tool.bubble(function(p) {
            if (p && p.store) {
            	var a = new Array();
            	p.store.filters.each(function(it, idx, l) {
            		a.push({
            			property: it.property,
            			value: it.value
            		});
            	});
            	if (p.store.filters.length > 0) {
                    qso.filter = Ext.JSON.encode(a);
                }
                var service = "/analytics/ws/export/" + 
                    p.store.storeId.toLowerCase().replace('filtered','');
                window.location.href = service + "?" + Ext.Object.toQueryString(qso);
                return false;
            }
        }, this);
    }
});