Ext.define('Analytics.controller.Month', {
    extend: 'Ext.app.Controller',
    
    refs: [{
        // A component query
        selector: 'viewport > timenavigator',
        ref: 'timeNavigator'
    }],
    
    date: null,
    mode: 'monthly', // global,monthly
    
    init: function() {
        this.date = new Date();
        this.control({
            '.timenavigator > container > button': {
                click: this.onMonthChanged
            },
	        '.timenavigator button[id="switchMode"]': {
	            click: this.onModeChanged
	        }
        });
    },
    
    onModeChanged: function(btn) {
    	this.mode = (this.mode == 'global') ? 'monthly' : 'global';
    	
    	if(this.mode == 'global') {
    		this.getTimeNavigator().toGlobalMode(btn);
    		this.application.fireEvent('modechanged', {
	            params: {
	                month: 0,
	                year: 0
	            }
	        });
    	} else {
    		this.getTimeNavigator().toMonthlyMode(this.date, btn);
    		this.application.fireEvent('monthchanged', {
                params: {
                    month: Ext.Date.format(this.date, 'n'),
                    year: Ext.Date.format(this.date, 'Y')
                }
            });
    	}
    },
    
    onMonthChanged: function(btn) {
        // new date:
        this.date = Ext.Date.add(this.date, Ext.Date.MONTH, 
            (btn.id === 'previous') ? -1 : 1
        );
        // update display:
        this.getTimeNavigator().replaceDate(this.date);
        // trigger stores update by the way of an application-wide event:
        var opCfg = {
            params: {
                month: Ext.Date.format(this.date, 'n'),
                year: Ext.Date.format(this.date, 'Y')
            }
        };
        this.application.fireEvent('monthchanged', opCfg);
    }
});