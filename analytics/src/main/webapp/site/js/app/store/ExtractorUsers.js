Ext.define('Analytics.store.ExtractorUsers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.ExtractorUser',
    model: 'Analytics.model.ExtractorUser',
    remoteSort: true,
    sorters: [{property: 'count', direction: 'DESC'}],
    autoLoad: true
});