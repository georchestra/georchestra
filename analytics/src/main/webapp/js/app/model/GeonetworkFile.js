Ext.define('Analytics.model.GeonetworkFile', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'filename',
        type: 'string'
    }, {
        name: 'metadata_id',
        type: 'string'
    }, {
        name: 'count',
        type: 'int'
    }],
    proxy: {
        type: 'ajax',
        url: '/analytics/ws/geonetwork/files',
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