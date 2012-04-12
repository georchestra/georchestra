Ext.define('Analytics.controller.OGC', {
    extend: 'Analytics.controller.Base',
    stores: ['OGCUsers', 'OGCLayers'],
    
    init: function() {

    },
    
    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getOGCUsersStore();
        var layersStore = this.getOGCLayersStore();
        
        this.application.on({
            "monthchanged": function(opCfg) {
            	this.loadStoreWithDate(usersStore, opCfg);
            	this.loadStoreWithDate(layersStore, opCfg);
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
            scope: this
        });
        
        // only done once in geonetwork controller:
        //this.callParent();
    }
});