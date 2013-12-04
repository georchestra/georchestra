Ext.define('Analytics.store.FilteredGeonetworkFiles', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.GeonetworkFile',
    model: 'Analytics.model.GeonetworkFile',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});