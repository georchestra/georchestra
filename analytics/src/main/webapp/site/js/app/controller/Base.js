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
        var month = parseInt(this.month || Ext.Date.format(new Date(), 'm'));
        var year = parseInt(this.year || Ext.Date.format(new Date(), 'Y'));
        tool.bubble(function(p) {
            if (p && p.store) {
            	var a = new Array();
            	p.store.filters.each(function(it, idx, l) {
            		a.push({
            			property : it.property,
            			value : it.value
            		});
            	});
            	var f = p.store.filters.length >0 ? '&filter=' + Ext.JSON.encode(a) : '';
                var storeId = p.store.storeId.toLowerCase().replace('filtered','');
                window.location.href = "/analytics/ws/export/"+storeId+"?month="+month+"&year="+year+f
                return false;
            }
        }, this);
    }
});