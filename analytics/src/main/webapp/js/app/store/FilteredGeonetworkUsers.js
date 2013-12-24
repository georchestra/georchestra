Ext.define('Analytics.store.FilteredGeonetworkUsers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.GeonetworkUser',
    model: 'Analytics.model.GeonetworkUser',
    remoteSort: true,
    remoteFilter: true,
    sorters: [{property: 'count', direction: 'DESC'}]
});