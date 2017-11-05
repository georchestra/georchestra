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

Ext.namespace("GEOR.Addons");

GEOR.Addons.Base = Ext.extend(Object, {

    constructor: function(mp, options) {
        this.mapPanel = mp;
        this.map = mp.map;
        this.options = options;
        this.components = null;
        this.lang = OpenLayers.Lang.getCode();
        if (this.options.target) {
            var t = this.options.target.split("_"),
                target = t[0];
            this.position = parseInt(t[1]) || 0;
            this.target = null;
            switch (target) {
                // top toolbar:
                case "tbar":
                    this.target = this.mapPanel.getTopToolbar();
                    break;
                // bottom toolbar:
                case "bbar":
                    this.target = this.mapPanel.getBottomToolbar();
                    break;
                // mini tabpanel in lower right corner:
                case "tabs":
                    this.target = Ext.getCmp("tabs");
                    break;
            }
        }
    },

    /**
     * Method: getTooltip
     */
    getTooltip: function(record) {
        return [
            "<b>",
            this.getText(record),
            "</b><br>",
            this.getQtip(record)
        ].join('');
    },

    /**
     * Method: getText
     */
    getText: function(record) {
        return record.get("title")[this.lang]
            || record.get("title")["en"];
    },

    /**
     * Method: getQtip
     */
    getQtip: function(record) {
        return record.get("description")[this.lang]
            || record.get("description")["en"];
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        if (this.target) {
            Ext.each(this.components, function(cmp) {
                this.target.remove(cmp);
            }, this);
            this.components = null;
        }
        this.map = null;
        this.mapPanel = null;
    }
});