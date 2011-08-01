/*
 * Copyright (C) Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOB_map.js
 * @include GEOB_mappanel.js
 * @include GEOB_managelayers.js
 * @include GEOB_recenter.js
 * @include GEOB_address.js
 * @include GEOB_referentials.js
 * @include GEOB_ajaxglobal.js
 * @include GEOB_waiter.js
 * @include GEOB_config.js
 * @include GEOB_mapinit.js
 * @include GEOB_print.js
 */

Ext.namespace("GEOB");

(function() {

    Ext.onReady(function() {

        /*
         * Setting of OpenLayers global vars.
         */

        OpenLayers.Lang.setCode('fr');
        OpenLayers.Number.thousandsSeparator = " ";
        OpenLayers.ImgPath = 'app/img/openlayers/';
        OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;
        OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;

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
        
        /*
         * Initialize the application.
         */

        // deactivate styler and queryer if anonymous
        if (GEOB.config.ANONYMOUS) {
            GEOB.styler = null;
            GEOB.querier = null;
        }

        GEOB.ajaxglobal.init();
        var layerStore = GEOB.map.create();
        var map = layerStore.map;
        GEOB.wmc.init(layerStore);
        GEOB.print.init(layerStore);
        if (GEOB.getfeatureinfo) {
            GEOB.getfeatureinfo.init(map);
        }
        if (GEOB.querier) {
            GEOB.querier.init(map);
        }
        if (GEOB.resultspanel) {
            GEOB.resultspanel.init(map);
        }
        GEOB.waiter.init();

        // handle layerstore initialisation with wms/services/wmc from "panier"
        GEOB.mapinit.init(layerStore);

        /*
         * Create the page's layout.
         */

        var eastItems = [
            new Ext.Panel({
                // this panel contains the "manager layer" and
                // "querier" components
                region: (GEOB.editing !== undefined) ? "north" : "center",
                height: 270, // has no effect when region is
                             // "center"
                layout: "card",
                activeItem: 0,
                title: "Couches disponibles",
                split: (GEOB.editing !== undefined),
                collapsible: (GEOB.editing !== undefined),
                collapsed: (GEOB.editing !== undefined),
                defaults: {
                    border:false
                },
                items: [
                    Ext.apply({
                    }, GEOB.managelayers.create(layerStore))
                ]
            }),
            new Ext.TabPanel({
                // this panel contains the components for
                // recentering the map
                region: "south",
                collapseMode: "mini",
                collapsible: true,
                deferredRender: false,
                activeTab: 0,
                hideCollapseTool: true,
                split: true,
                height: 100,
                minHeight: 70,
                maxHeight: 100,
                defaults: {
                    frame: false,
                    border: false,
                    bodyStyle: "padding: 5px"
                },
                items: [
                    Ext.apply({
                        title: "GeoNames"
                    }, GEOB.recenter.create(map)),
                    Ext.apply({
                        title: "Adresses"
                    }, GEOB.address.create(map)),
                    Ext.apply({
                        title: "Référentiels"
                    }, GEOB.referentials.create(map, "geob_loc"))
                ]
            })
        ];
        if (GEOB.editing) {
            eastItems.push(
                Ext.apply({
                    region: "center", 
                    title: "Edition"
                }, GEOB.editing.create(map))
            );
        }

        // this panel serves as the container for
        // the "search results" panel
        var southPanel = new Ext.Panel({
            region: "south",
            hidden: !GEOB.resultspanel, // hide this panel if
                                        // the resultspanel
                                        // module is undefined
            collapsible: true,
            split: true,
            layout: "fit",
            title: "Résultats de requête",
            collapsed: true,
            height: 150,
            defaults: {
                border: false,
                frame: false
            },
            items: [{
                bodyStyle: 'padding: 5px',
                html: "<p>Sélectionnez l'outil d'interrogation "+
                "ou construisez une requête sur une couche.<br />"+
                "Les attributs des objets s'afficheront dans ce cadre.</p>"
            }],
            listeners: {
                "collapse": function() {
                    // when the user collapses the panel
                    // hide the features in the layer
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.hide();
                    }
                },
                "expand": function() {
                    // when the user expands the panel
                    // show the features in the layer
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.show();
                    }
                }
            }
        });

        // the viewport
        var vp = new Ext.Viewport({
            layout: "border",
            items: [
                Ext.apply({region: "center"}, 
                    GEOB.mappanel.create(layerStore)), 
                { 
                    region: "east",
                    layout: "border",
                    width: 300,
                    minWidth: 280,
                    maxWidth: 500,
                    split: true,
                    collapseMode: "mini",
                    collapsible: true,
                    frame: false,
                    border: false,
                    items: eastItems
                },
                southPanel
            ]
        });

        /*
         * Register to events on various modules to deal with
         * the communication between them. Really, we're
         * acting as a mediator between the modules with
         * the objective of making them independent.
         */
        if (GEOB.querier) {
            var querierTitle;
            GEOB.querier.events.on({
                "ready": function(panelCfg) {
                    // clear the previous filterbuilder panel, if exists
                    if (eastItems[0].getComponent(1)) {
                        eastItems[0].remove(eastItems[0].getComponent(1));
                    }
                    panelCfg.buttons.push({
                        text: 'Fermer',
                        handler: function() {
                            // we also need to hide querier vector layer:
                            eastItems[0].getComponent(1).tearDown();
                            eastItems[0].setTitle("Couches disponibles");
                            eastItems[0].getLayout().setActiveItem(0);
                        }
                    });
                    querierTitle = panelCfg.title;
                    eastItems[0].setTitle(querierTitle);
                    var panel = Ext.apply(panelCfg, {
                        // whatever here
                        title: null
                    });
                    eastItems[0].add(panel);
                    eastItems[0].getLayout().setActiveItem(1);
                    eastItems[0].getComponent(1).setUp();
                    eastItems[0].doLayout(); // required
                },
                "showrequest": function() {
                    eastItems[0].setTitle(querierTitle); 
                    eastItems[0].getLayout().setActiveItem(1);
                    eastItems[0].getComponent(1).setUp();
                    eastItems[0].doLayout(); // required
                },
                "search": function(panelCfg) {
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.clean();
                    }
                    southPanel.removeAll();
                    var panel = Ext.apply({
                        bodyStyle: 'padding:5px'
                    }, panelCfg);
                    southPanel.add(panel);
                    southPanel.doLayout();
                    southPanel.expand();  
                },
                "searchresults": function(options) {
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.populate(options);
                    }
                }
            });
        }

        if (GEOB.getfeatureinfo) {
            GEOB.getfeatureinfo.events.on({
                "search": function(panelCfg) {
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.clean();
                    }
                    southPanel.removeAll();
                    var panel = Ext.apply({
                        bodyStyle: 'padding:5px'
                    }, panelCfg);
                    southPanel.add(panel);
                    southPanel.doLayout();
                    southPanel.expand();  
                },
                "searchresults": function(options) {
                    if (GEOB.resultspanel) {
                        GEOB.resultspanel.populate(options);
                    }
                }
            });
        }
        
        if (GEOB.resultspanel) {
            GEOB.resultspanel.events.on({
                "panel": function(panelCfg) {
                    southPanel.removeAll();
                    southPanel.add(panelCfg);
                    southPanel.doLayout();
                }
            });
        }

        // a function for updating the layer's SLD and STYLES
        // params
        var updateLayerParams = function(layerRecord, sld, styles) {
            layerRecord.get("layer").mergeNewParams({
                'SLD': (sld)?sld:null,
                'STYLES': (styles)?styles:null
            });
        };

        GEOB.managelayers.events.on({
            "selectstyle": function(layerRecord, styles) {
                updateLayerParams(layerRecord, null, styles);
            }
        });

        if (GEOB.styler) {
            GEOB.styler.events.on({
                "sldready": function(layerRecord, sld) {
                    updateLayerParams(layerRecord, sld, null);
                    GEOB.managelayers.unselectStyles(layerRecord);
                }
            });
        }
    });
})();
