Ext.define('Analytics.store.GeonetworkUsers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.GeonetworkUser',
    model: 'Analytics.model.GeonetworkUser',
    remoteSort: true,
    sorters: [{property: 'count', direction: 'DESC'}],
    autoLoad: true
});