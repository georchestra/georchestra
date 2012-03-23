Ext.define('Analytics.controller.Extractor', {
    extend: 'Analytics.controller.Base',
    stores: ['ExtractorUsers', 'ExtractorLayers'],
    
    init: function() {

    },
    
    onLaunch: function() {
        // Use the automatically generated getter to get the stores
        var usersStore = this.getExtractorUsersStore();
        var layersStore = this.getExtractorLayersStore();
        
        this.application.on({
            "monthchanged": function(opCfg) {
                usersStore.load(opCfg);
                layersStore.load(opCfg);
            }
        });
        
        this.control({
            'extractoruserslist tool': {
                click: this.handleExport
            },
            'extractorlayerslist tool': {
                click: this.handleExport
            },
            scope: this
        });
        
        // only done once in geonetwork controller:
        //this.callParent();
    }
});