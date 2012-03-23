Ext.define('Analytics.model.OGCUser', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'user_name',
        type: 'string'
    }, {
        name: 'count',
        type: 'int'
    }],
    proxy: {
        type: 'ajax',
        url: '/analytics/ws/ogc/users', 
        extraParams: {
            month: Ext.Date.format(new Date(), 'n'),
            year: Ext.Date.format(new Date(), 'Y')
        },
        pageParam: undefined,
        reader: {
            type: 'json',
            root: 'results',
            totalProperty: 'total'
        }
    }
});
