Ext.define('Analytics.store.ExtractorLayers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.ExtractorLayer',
    model: 'Analytics.model.ExtractorLayer',
    remoteSort: true,
    sorters: [{property: 'count', direction: 'DESC'}],
    autoLoad: true
});