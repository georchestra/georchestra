/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

Ext.define('Analytics.model.OGCLayer', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'service',
        type: 'string'
    }, {
        name: 'layer',
        type: 'string'
    }, {
        name: 'request',
        type: 'string'
    }, {
        name: 'count',
        type: 'int'
    }],
    proxy: {
        type: 'ajax',
        url: '/analytics/ws/ogc/layers',
        extraParams: {
            month: Ext.Date.format(new Date(), 'n'),
            year: Ext.Date.format(new Date(), 'Y')
        },
        pageParam: undefined,
        reader: {
            type: 'json',
            root: 'results',
            totalProperty: 'total'
        }
    }
});