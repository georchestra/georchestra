Ext.define('Analytics.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: [
        'Analytics.view.ExtractorLayers',
        'Analytics.view.ExtractorUsers',
        'Analytics.view.ExtractorGroups',
        'Analytics.view.GeonetworkFiles',
        'Analytics.view.GeonetworkUsers',
        'Analytics.view.GeonetworkGroups',
        'Analytics.view.OGCLayers',
        'Analytics.view.OGCUsers',
        'Analytics.view.OGCGroups',
        'Analytics.view.TimeNavigator'
    ],
    
    layout: 'border',
    
    initComponent: function() {
        var tr = Analytics.Lang.i18n;
        this.items = [{
            xtype: 'box',
            id: 'geor_header',
            region: 'north', 
            height: GEOR.config.HEADER_HEIGHT,
            contentEl: 'go_head'
        }, {
            region: 'center',
            xtype: 'tabpanel',
            // required in order to control all panel tools:
            deferredRender: false,
            items: [{
                tabConfig: {
                    title: tr('Downloads from GeoNetwork'),
                    tooltip: tr('Select this tab to access the statistics of downloads from the catalog')
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: tr('Files'),
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'geonetworkfileslist'
                }, {
                    title: tr('Users'),
                    region: 'center',
                    xtype: 'geonetworkuserslist'
                }, {
                    title: tr('Organisms'),
                    split: true,
                    region: 'east',
                    width: '25%',
                    xtype: 'geonetworkgroupslist'
                }]
            }, {
                tabConfig: {
                    title: tr('Personalized extractions'),
                    tooltip: tr('Select this tab to access to the statistics of the extractor')
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: tr('Layers'),
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'extractorlayerslist'
                }, {
                    title: tr('Users'),
                    region: 'center',
                    xtype: 'extractoruserslist'
                },{
                    title: tr('Organisms'),
                    split: true,
                    region: 'east',
                    width: '25%',
                    xtype: 'extractorgroupslist'
                }]
            }, {
                tabConfig: {
                    title: tr('OGC Services'),
                    tooltip: tr('Select this tab to access the OGC service statistics')
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: tr('Layers'),
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'ogclayerslist'
                }, {
                    title: tr('Users'),
                    region: 'center',
                    xtype: 'ogcuserslist'
                },{
                    title: tr('Organisms'),
                    split: true,
                    region: 'east',
                    width: '25%',
                    xtype: 'ogcgroupslist'
                }]
            }]
        }, {
            region: 'south',
            border: false,
            height: 100,
            minHeight: 100,
            maxHeight: 100,
            split: true,
            xtype: 'timenavigator'
        }];
        this.callParent();
    }
    
});