Ext.define('Analytics.store.ExtractorGroups', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.ExtractorGroup',
    model: 'Analytics.model.ExtractorGroup',
    remoteSort: true,
    remoteFilter: true,
    autoLoad: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});