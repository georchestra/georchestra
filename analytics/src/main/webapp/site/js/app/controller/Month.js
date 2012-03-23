Ext.define('Analytics.controller.Month', {
    extend: 'Ext.app.Controller',
    
    refs: [{
        // A component query
        selector: 'viewport > timenavigator',
        ref: 'timeNavigator'
    }],
    
    date: null,
    
    init: function() {
        this.date = new Date();
        this.control({
            '.timenavigator button': {
                click: this.onMonthChanged
            }
        });
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