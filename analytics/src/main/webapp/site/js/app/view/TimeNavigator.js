Ext.define('Analytics.view.TimeNavigator', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.timenavigator',
    
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    
    defaults: {
        flex: 1,
        style: 'text-align:center;'
    },
    
    initComponent: function() {
        this.items = [{
            xtype: 'container',
            items: [{
                xtype: 'button',
                scale: 'medium',
                id: 'previous',
                cls: 'centered',
                text: 'mois précédent',
                icon: 'resources/site/images/famfamfam/resultset_previous.png',
                iconAlign: 'left',
                width: 180
            }]
        }, {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack  : 'start'
            },
            items:[{
            	cls: 'dateCenter',
            	xtype: 'container',
            	html: this.formatDate(new Date())
            },{
            	xtype: 'container',
            	cls: 'dateCenterBut',
            	items: [{
                    xtype: 'button',
                    scale: 'medium',
                    id: 'switchMode',
                    text: 'statistiques globales',
                    width: 180
                }]
            }]
        }, {
            xtype: 'container',
            items: [{
                xtype: 'button',
                scale: 'medium',
                id: 'next',
                cls: 'centered',
                text: 'mois suivant',
                icon: 'resources/site/images/famfamfam/resultset_next.png',
                iconAlign: 'right',
                width: 180
            }]
        }];
        this.callParent();
    },
    
    replaceDate: function(date) {
    	this.changeText(this.formatDate(date));
    },
    
    changeText: function(txt) {
    	this.getComponent(1).getComponent(0).update(txt);
    },
    
    formatDate: function(date) {
        return Ext.Date.format(date, 'F Y');
    },
    
	toGlobalMode: function(btn) {
		this.getChildByElement('previous').setVisible(false);
		this.getChildByElement('next').setVisible(false);
		this.changeText('Statistiques globales');
		btn.setText('statistiques mensuelles');
    },
    
    toMonthlyMode: function(date,btn) {
		this.getChildByElement('previous').setVisible(true);
		this.getChildByElement('next').setVisible(true);
		this.changeText(this.formatDate(date));
		btn.setText('statistiques globales');
    },
});