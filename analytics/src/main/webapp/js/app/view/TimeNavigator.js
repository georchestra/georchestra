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

Ext.define('Analytics.view.TimeNavigator', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.timenavigator',
    tr: null,

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    defaults: {
        flex: 1,
        style: 'text-align:center;'
    },

    initComponent: function() {
        tr = Analytics.Lang.i18n;
        this.items = [{
            xtype: 'container',
            items: [{
                xtype: 'button',
                scale: 'medium',
                id: 'previous',
                cls: 'centered',
                text: tr('previous month'),
                icon: 'resources/images/famfamfam/resultset_previous.png',
                iconAlign: 'left',
                width: 180
            }]
        }, {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack  : 'start'
            },
            items:[{
                cls: 'dateCenter',
                xtype: 'container',
                html: this.formatDate(new Date())
            },{
                xtype: 'container',
                cls: 'dateCenterBut',
                items: [{
                    xtype: 'button',
                    scale: 'medium',
                    id: 'switchMode',
                    text: tr('Global statistics'),
                    width: 180
                }]
            }]
        }, {
            xtype: 'container',
            items: [{
                xtype: 'button',
                scale: 'medium',
                id: 'next',
                cls: 'centered',
                text: tr('next month'),
                icon: 'resources/images/famfamfam/resultset_next.png',
                iconAlign: 'right',
                width: 180
            }]
        }];
        this.callParent();
    },

    replaceDate: function(date) {
        this.changeText(this.formatDate(date));
    },

    changeText: function(txt) {
        this.getComponent(1).getComponent(0).update(txt);
    },

    formatDate: function(date) {
        return Ext.Date.format(date, 'F Y');
    },

    toGlobalMode: function(btn) {
        this.getChildByElement('previous').setVisible(false);
        this.getChildByElement('next').setVisible(false);
        this.changeText(tr('Global statistics'));
        btn.setText(tr('Monthly statistics'));
    },

    toMonthlyMode: function(date,btn) {
        this.getChildByElement('previous').setVisible(true);
        this.getChildByElement('next').setVisible(true);
        this.changeText(this.formatDate(date));
        btn.setText(tr('Global statistics'));
    },
});