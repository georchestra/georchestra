Ext.define('Analytics.controller.Geonetwork', {
    extend: 'Analytics.controller.Base',
    stores: ['GeonetworkUsers', 'GeonetworkFiles'],
    
    init: function() {

    },
    
    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getGeonetworkUsersStore();
        var filesStore = this.getGeonetworkFilesStore();
        
        this.application.on({
            "monthchanged": function(opCfg) {
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(filesStore, opCfg);
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
            scope: this
        });
        
        // only done once here:
        this.callParent();
    }
});