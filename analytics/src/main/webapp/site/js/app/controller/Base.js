Ext.define('Analytics.controller.Base', {
    extend: 'Ext.app.Controller',
    
    month: null,
    year: null,
    
    init: function() {

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
        var month = parseInt(this.month || Ext.Date.format(new Date(), 'm'));
        var year = parseInt(this.year || Ext.Date.format(new Date(), 'Y'));
        tool.bubble(function(p) {
            if (p && p.store) {
                var storeId = p.store.storeId.toLowerCase();
                window.location.href = "/analytics/ws/export/"+storeId+"?month="+month+"&year="+year
                return false;
            }
        }, this);
    }
});