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

Ext.define('Analytics.controller.Geonetwork', {
    extend: 'Analytics.controller.Base',
    stores: ['GeonetworkUsers', 'GeonetworkFiles', 'GeonetworkGroups'],
    
    init: function() {

    },
    
    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getGeonetworkUsersStore();
        var filesStore = this.getGeonetworkFilesStore();
        var groupsStore = this.getGeonetworkGroupsStore();
        
        this.application.on({
            "monthchanged": function(opCfg) {
                this.month = opCfg.params.month;
                this.year = opCfg.params.year;
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(filesStore, opCfg);
            	this.loadStoreWithDate(groupsStore, opCfg);
            },
            "modechanged": function(opCfg) {
                this.month = opCfg.params.month;
                this.year = opCfg.params.year;
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(filesStore, opCfg);
            	this.loadStoreWithDate(groupsStore, opCfg);
            },
            scope: this
        });
        
        this.control({
            'geonetworkuserslist tool': {
                click: this.handleExport
            },
            'geonetworkfileslist tool': {
                click: this.handleExport
            },
            'geonetworkgroupslist tool': {
                click: this.handleExport
            },
            'filteredgeonetworkuserslist tool': {
                click: this.handleExport
            },
            'filteredgeonetworkfileslist tool': {
                click: this.handleExport
            },
            scope: this
        });
        
        // only done once here:
        this.callParent();
    }
});