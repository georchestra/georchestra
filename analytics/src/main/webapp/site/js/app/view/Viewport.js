Ext.define('Analytics.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: [
        'Analytics.view.ExtractorLayers',
        'Analytics.view.ExtractorUsers',
        'Analytics.view.GeonetworkFiles',
        'Analytics.view.GeonetworkUsers',
        'Analytics.view.OGCLayers',
        'Analytics.view.OGCUsers',
        'Analytics.view.TimeNavigator'
    ],
    
    layout: 'border',
    
    initComponent: function() {
        this.items = [{
            xtype: 'box',
            id: 'geor_header',
            region: 'north', 
            height: 90,
            contentEl: 'go_head'
        }, {
            region: 'center',
            xtype: 'tabpanel',
            // required in order to control all panel tools:
            deferredRender: false,
            items: [{
                tabConfig: {
                    title: 'Téléchargements depuis GeoNetwork',
                    tooltip: 'Sélectionnez cet onglet pour accéder aux statistiques des téléchargements de fichiers depuis le catalogue'
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: 'Fichiers',
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'geonetworkfileslist'
                }, {
                    title: 'Utilisateurs',
                    region: 'center',
                    xtype: 'geonetworkuserslist'
                }]
            }, {
                tabConfig: {
                    title: 'Extractions personnalisées',
                    tooltip: 'Sélectionnez cet onglet pour accéder aux statistiques des extractions'
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: 'Couches',
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'extractorlayerslist'
                }, {
                    title: 'Utilisateurs',
                    region: 'center',
                    xtype: 'extractoruserslist'
                }]
            }, {
                tabConfig: {
                    title: 'Services OGC',
                    tooltip: 'Sélectionnez cet onglet pour accéder aux statistiques des services OGC de la plateforme'
                },
                layout: 'border',
                defaults: {border: false},
                items: [{
                    title: 'Couches',
                    region: 'west',
                    split: true,
                    width: '50%',
                    xtype: 'ogclayerslist'
                }, {
                    title: 'Utilisateurs',
                    region: 'center',
                    xtype: 'ogcuserslist'
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