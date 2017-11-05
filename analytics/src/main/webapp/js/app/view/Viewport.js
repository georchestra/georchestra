/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        var tabs = [];
        if (GEOR.config.OGC_STATISTICS === true) {
            tabs.push({
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
            });
        }
        if (GEOR.config.DOWNLOAD_FORM === true) {
            tabs.push({
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
            });
        }
        if (!tabs.length) {
            tabs.push({
                tabConfig: {
                    title: tr('analytics')
                },
                bodyStyle: "padding: 10px;",
                html: tr("Nothing to show in here, ogc services statistics and download form are deactivated. Please contact your administrator.")
            });
        }
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
            items: tabs
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