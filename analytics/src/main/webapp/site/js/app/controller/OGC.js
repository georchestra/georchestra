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
        
        // only done once in geonetwork controller:
        //this.callParent();
    }
});