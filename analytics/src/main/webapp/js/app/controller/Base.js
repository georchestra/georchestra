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
        var qso = {
            month: this.month === null ? 
                Ext.Date.format(new Date(), 'm') : this.month,
            year: this.year === null ? 
                Ext.Date.format(new Date(), 'Y') : this.year
        };
        tool.bubble(function(p) {
            if (p && p.store) {
            	var a = new Array();
            	p.store.filters.each(function(it, idx, l) {
            		a.push({
            			property: it.property,
            			value: it.value
            		});
            	});
            	if (p.store.filters.length > 0) {
                    qso.filter = Ext.JSON.encode(a);
                }
                var service = "/analytics/ws/export/" + 
                    p.store.storeId.toLowerCase().replace('filtered','');
                window.location.href = service + "?" + Ext.Object.toQueryString(qso);
                return false;
            }
        }, this);
    }
});