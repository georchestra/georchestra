Ext.define('Analytics.model.OGCLayer', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'service',
        type: 'string'
    }, {
        name: 'layer',
        type: 'string'
    }, {
        name: 'request',
        type: 'string'
    }, {
        name: 'count',
        type: 'int'
    }],
    proxy: {
        type: 'ajax',
        url: '/analytics/ws/ogc/layers',
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