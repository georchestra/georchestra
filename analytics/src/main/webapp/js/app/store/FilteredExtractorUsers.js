Ext.define('Analytics.store.FilteredExtractorUsers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.ExtractorUser',
    model: 'Analytics.model.ExtractorUser',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});