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
            cls: 'centered',
            html: this.formatDate(new Date())
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
        this.remove(this.getComponent(1));
        this.insert(1, new Ext.container.Container({
            cls: 'centered',
            html: this.formatDate(date)
        }));
    },
    
    formatDate: function(date) {
        return Ext.Date.format(date, 'F Y');
    }
});