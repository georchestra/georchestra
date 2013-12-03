Ext.define('Analytics.store.FilteredExtractorLayers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.ExtractorLayer',
    model: 'Analytics.model.ExtractorLayer',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});