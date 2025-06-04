/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define('Analytics.controller.Month', {
    extend: 'Ext.app.Controller',
    
    refs: [{
        // A component query
        selector: 'viewport > timenavigator',
        ref: 'timeNavigator'
    }],
    
    date: null,
    mode: 'monthly', // global,monthly
    
    init: function() {
        this.date = new Date();
        this.control({
            '.timenavigator > container > button': {
                click: this.onMonthChanged
            },
	        '.timenavigator button[id="switchMode"]': {
	            click: this.onModeChanged
	        }
        });
    },
    
    onModeChanged: function(btn) {
    	this.mode = (this.mode == 'global') ? 'monthly' : 'global';
    	
    	if(this.mode == 'global') {
    		this.getTimeNavigator().toGlobalMode(btn);
    		this.application.fireEvent('modechanged', {
	            params: {
	                month: 0,
	                year: 0
	            }
	        });
    	} else {
    		this.getTimeNavigator().toMonthlyMode(this.date, btn);
    		this.application.fireEvent('monthchanged', {
                params: {
                    month: Ext.Date.format(this.date, 'n'),
                    year: Ext.Date.format(this.date, 'Y')
                }
            });
    	}
    },
    
    onMonthChanged: function(btn) {
        // new date:
        this.date = Ext.Date.add(this.date, Ext.Date.MONTH, 
            (btn.id === 'previous') ? -1 : 1
        );
        // update display:
        this.getTimeNavigator().replaceDate(this.date);
        // trigger stores update by the way of an application-wide event:
        var opCfg = {
            params: {
                month: Ext.Date.format(this.date, 'n'),
                year: Ext.Date.format(this.date, 'Y')
            }
        };
        this.application.fireEvent('monthchanged', opCfg);
    }
});