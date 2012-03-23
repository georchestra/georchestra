Ext.define('Analytics.view.ExtractorUsers', {
    extend: 'Analytics.view.BaseGridPanel',
    alias: 'widget.extractoruserslist',
    store: 'ExtractorUsers',

    initComponent: function() {
        this.columns = [{
            dataIndex: 'username',
            text: 'Nom'
        }, {
            dataIndex: 'count',
            text: 'Nombre de requêtes'
        }];
        
        this.callParent();
    }
});