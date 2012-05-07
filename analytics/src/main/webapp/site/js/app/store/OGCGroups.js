Ext.define('Analytics.store.OGCGroups', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.OGCGroup',
    model: 'Analytics.model.OGCGroup',
    remoteSort: true,
    remoteFilter: true,
    autoLoad: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});