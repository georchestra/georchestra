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