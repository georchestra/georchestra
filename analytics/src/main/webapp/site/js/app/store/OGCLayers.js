Ext.define('Analytics.store.OGCLayers', {
    extend: 'Ext.data.Store',
    requires: 'Analytics.model.OGCLayer',
    model: 'Analytics.model.OGCLayer',
    remoteSort: true,
    sorters: [{property: 'count', direction: 'DESC'}],
    autoLoad: true
});