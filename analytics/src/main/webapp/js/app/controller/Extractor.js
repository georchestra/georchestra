Ext.define('Analytics.controller.Extractor', {
    extend: 'Analytics.controller.Base',
    stores: ['ExtractorUsers', 'ExtractorLayers', 'ExtractorGroups'],
    
    init: function() {

    },

    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getExtractorUsersStore();
        var layersStore = this.getExtractorLayersStore();
        var groupsStore = this.getExtractorGroupsStore();
        
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
            'extractoruserslist tool': {
                click: this.handleExport
            },
            'extractorlayerslist tool': {
                click: this.handleExport
            },
            'extractorgroupslist tool': {
                click: this.handleExport
            },
            'filterextractoruserslist tool': {
                click: this.handleExport
            },
            'filteredextractorlayerslist tool': {
                click: this.handleExport
            },
            
            scope: this
        });
        
        // only done once in geonetwork controller:
        //this.callParent();
    }
});