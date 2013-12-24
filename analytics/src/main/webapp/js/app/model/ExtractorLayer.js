Ext.define('Analytics.model.ExtractorLayer', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'ows_url',
        type: 'string'
    }, {
        name: 'ows_type',
        type: 'string'
    }, {
        name: 'layer_name',
        type: 'string'
    }, {
        name: 'count',
        type: 'int'
    }],
    proxy: {
        type: 'ajax',
        url: '/analytics/ws/extractor/layers', // TODO: change for webservice
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