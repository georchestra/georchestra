Ext.define('Analytics.store.FilteredOGCLayers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.OGCLayer',
    model: 'Analytics.model.OGCLayer',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});