Ext.define('Analytics.store.GeonetworkGroups', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.GeonetworkGroup',
    model: 'Analytics.model.GeonetworkGroup',
    remoteSort: true,
    remoteFilter: true,
    autoLoad: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});