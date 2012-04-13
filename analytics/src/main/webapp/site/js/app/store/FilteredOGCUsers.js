Ext.define('Analytics.store.FilteredOGCUsers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.OGCUser',
    model: 'Analytics.model.OGCUser',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});