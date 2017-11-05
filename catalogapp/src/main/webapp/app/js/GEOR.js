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

/*
 * @include GEOR_waiter.js
 * @include GEOR_dataview.js
 * @include GEOR_csw.js
 * @include GEOR_nav.js
 * @include GEOR_what.js
 * @include GEOR_where.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.criteria = ['what', 'where'];

Ext.onReady(function() {
    var tr = OpenLayers.i18n;

    /*
     * Setting of OpenLayers global vars.
     */
    OpenLayers.Lang.setCode(GEOR.config.LANG);
    OpenLayers.Number.thousandsSeparator = " ";
    OpenLayers.ImgPath = 'app/img/openlayers/';
    OpenLayers.DOTS_PER_INCH = GEOR.config.MAP_DOTS_PER_INCH;
    OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

    /*
     * Setting of Ext global vars.
     */
    Ext.BLANK_IMAGE_URL = "app/img/s.gif";
    Ext.apply(Ext.MessageBox.buttonText, {
        yes: tr("Yes"),
        no: tr("No"),
        ok: tr("OK"),
        cancel: tr("Cancel")
    });
    Ext.QuickTips.init();

    /*
     * Initialize the application.
     */

    GEOR.waiter.init();

    var store = GEOR.dataview.init();


    var o = new Ext.util.Observable();
    o.addEvents(
        /**
         * Event: searchrequest
         * Fires when the server needs to be queried for new results
         */
        "searchrequest",

        /**
         * Event: storeloaded
         * Fires when the new records are loaded in store
         */
        "storeloaded",

        /**
         * Event: itemselectionchanged
         * Fires when list item selection has changed
         */
        "itemselectionchanged",

        /**
         * Event: itemzoom
         * Fires when a user clicks on zoom
         */
        "itemzoom"
    );
    GEOR.observable = o;

    var whereFilter;
    var search = function(options) {
        GEOR.waiter.show();
        if (options && options.where) {
            whereFilter = options.where;
        }
        GEOR.dataview.bind(store);
        store.load({
            params: {
                xmlData: GEOR.csw.getPostData({
                    nav: GEOR.nav.getParameters(),
                    where: whereFilter
                })
            }
        });
    };

    /*
     * Create the page's layout.
     */

    // the header
    var vpItems = GEOR.header ?
        [{
            xtype: "box",
            id: "geor_header",
            region: "north",
            height: GEOR.config.HEADER_HEIGHT,
            el: "go_head"
        }] : [];

    var bbar = new Ext.Toolbar({
        cls: "centerbbar"
    });

    vpItems.push({
        region: "center",
        autoScroll: true,
        layout: 'fit',
        items: [GEOR.dataview.getCmp()],
        bbar: bbar
    }, {
        region: "west",
        cls: "westpanel",
        width: 280,
        minWidth: 280,
        maxWidth: 280,
        autoScroll: true,
        split: true,
        collapseMode: "mini",
        collapsible: true,
        frame: false,
        //border: false,
        header: false,
        defaults: {
            collapsible: true,
            collapsed: true,
            titleCollapse: true,
            border: false
        },
        items: [Ext.apply(GEOR.what.getCmp(), {
          title: tr("Which data are you searching for ?"),
            collapsible: false,
            collapsed: false,
            height: 90
        }), Ext.apply(GEOR.where.getCmp(), {
            title: tr("On which area ?"),
            collapsed: false,
            height: 280
        })/*, {
            title: tr("When ?")
        }*/],
        buttons: [{
          text: tr('clean'),
            cls: 'bigbtn',
            iconCls: 'geor-btn-reset',
            handler: function() {
                GEOR.nav.reset();
                var c = GEOR.criteria;
                for (var i=0,l=c.length;i<l;i++) {
                    GEOR[c[i]].reset();
                }
            }
        },{
          text: tr('search'),
            cls: 'bigbtn',
            iconCls: 'geor-btn-search',
            handler: function() {
                GEOR.nav.reset();
                search({
                    where: GEOR.where.getFilter()
                });
            }
        }]
    });

    // the viewport
    var vp = new Ext.Viewport({
        layout: "border",
        items: vpItems
    });

    /*
     * Register to events on various modules to deal with
     * the communication between them. Really, we're
     * acting as a mediator between the modules with
     * the objective of making them independent.
     */
    o.on("searchrequest", function(options) {
        if (options && options.reset) {
            GEOR.nav.reset();
        }
        search();
    });
    o.on("storeloaded", function(options) {
        GEOR.nav.update(options.store, bbar);
        // scroll to top result:
        GEOR.dataview.scrollToTop();
        // display results' bboxes:
        GEOR.where.display(options.store.getRange());
    });
    o.on("itemselectionchanged", function(options) {
        var records = options.records;
        GEOR.where.highlight(records);
        var l = options.total;
        var t = ''
        if (l > 1) {
          t = tr('various.results', {'RESULTS': l})
        } else if (l) {
          t = tr('one.result')
        }
        bbar.selText.setText(t);
        bbar.selText.getEl().highlight();
    });
    o.on("itemzoom", function(options) {
        GEOR.where.zoomTo(options.record);
    });
});
