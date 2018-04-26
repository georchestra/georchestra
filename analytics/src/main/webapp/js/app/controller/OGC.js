/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

Ext.define('Analytics.controller.OGC', {
    extend: 'Analytics.controller.Base',
    stores: ['OGCUsers', 'OGCLayers', 'OGCGroups'],
    
    init: function() {

    },
    
    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getOGCUsersStore();
        var layersStore = this.getOGCLayersStore();
        var groupsStore = this.getOGCGroupsStore();
        
        this.application.on({
            "monthchanged": function(opCfg) {
            	this.month = opCfg.params.month;
                this.year = opCfg.params.year;
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(layersStore, opCfg);
            	this.loadStoreWithDate(groupsStore, opCfg);
            },
            "modechanged": function(opCfg) {
                this.month = opCfg.params.month;
                this.year = opCfg.params.year;
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(layersStore, opCfg);
            	this.loadStoreWithDate(groupsStore, opCfg);
            },
            scope: this
        });
        
        this.control({
            'ogcuserslist tool': {
                click: this.handleExport
            },
            'ogclayerslist tool': {
                click: this.handleExport
            },
            'ogcgroupslist tool': {
                click: this.handleExport
            },
            'filteredogcuserslist tool': {
                click: this.handleExport
            },
            'filteredogclayerslist tool': {
                click: this.handleExport
            },
            scope: this
        });
        
        // only done once in here:
        this.callParent();
    }
});