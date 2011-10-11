/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOR_map.js
 */

Ext.namespace("GEOR");

GEOR.criteria = ['what'];

Ext.onReady(function() {
    
    /*
     * Setting of OpenLayers global vars.
     */
    OpenLayers.Lang.setCode('fr');
    OpenLayers.Number.thousandsSeparator = " ";
    OpenLayers.ImgPath = 'app/img/openlayers/';
    OpenLayers.DOTS_PER_INCH = GEOR.config.MAP_DOTS_PER_INCH;
    OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

    /*
     * Setting of Ext global vars.
     */
    Ext.BLANK_IMAGE_URL = "lib/externals/ext/resources/images/default/s.gif";
    Ext.apply(Ext.MessageBox.buttonText, {
        yes: "Oui",
        no: "Non",
        ok: "OK",
        cancel: "Annuler"
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
        "storeloaded"
    );
    GEOR.observable = o;
    
    var search = function() {
        GEOR.waiter.show();
        store.load({
            params: {
                xmlData: GEOR.csw.getPostData({
                    nav: GEOR.nav.getParameters()
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
            height: 90,
            el: "go_head"
        }] : [];
        
    var textItem = new Ext.Toolbar.TextItem({
        text: ''
    });
        
    vpItems.push({
        region: "center", 
        autoScroll: true,
        layout: 'fit',
        //contentEl: 'center'
        items: [GEOR.dataview.getCmp()],
        bbar: [{
            text: '<<',
            handler: GEOR.nav.begin,
            tooltip: "aller au début des résultats",
            width: 30
        },{
            text: '<',
            handler: GEOR.nav.previousPage,
            tooltip: "page précédente",
            width: 30
        },{
            text: '>',
            handler: GEOR.nav.nextPage,
            tooltip: "page suivante",
            width: 30
        },{
            text: '>>',
            handler: GEOR.nav.end,
            tooltip: "aller à la fin des résultats",
            width: 30
        }, textItem]
    }, 
    {
        region: "west",
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
            title: "Quelles données cherchez vous ?",
            collapsible: false,
            collapsed: false,
            height: 100
        }), Ext.apply(GEOR.where.getCmp(), {
            title: "Sur quel territoire ?",
            collapsed: false,
            height: 280
        })],
        buttons: [{
            text: 'effacer',
            handler: function() {
                GEOR.nav.reset();
                var c = GEOR.criteria;
                for (var i=0,l=c.length;i<l;i++) {
                    GEOR[c[i]].reset();
                }
            }
        },{
            text: 'chercher',
            handler: function() {
                GEOR.nav.reset();
                search();
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

    o.on("searchrequest", search);
    o.on("storeloaded", function(options) {
        var navText = GEOR.nav.update(options.store);
        textItem.setText(navText);
    });
});
