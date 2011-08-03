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
 * @include GEOR_mappanel.js
 * @include GEOR_managelayers.js
 * @include GEOR_recenter.js
 * @include GEOR_address.js
 * @include GEOR_referentials.js
 * @include GEOR_ajaxglobal.js
 * @include GEOR_waiter.js
 * @include GEOR_config.js
 * @include GEOR_mapinit.js
 * @include GEOR_print.js
 * @include GEOR_wmc.js
 * Note that GEOR_getfeatureinfo.js, GEOR_resultspanel.js, GEOR_querier.js, 
 * GEOR_styler.js should be included here, but they are not required by the edit module.
 * In order to make the edit build "light", those files will be added in main.cfg and not here.
 */

Ext.namespace("GEOR");

(function() {

    Ext.onReady(function() {

        /*
         * Setting of OpenLayers global vars.
         */

        OpenLayers.Lang.setCode('fr');
        OpenLayers.Number.thousandsSeparator = " ";
        OpenLayers.ImgPath = 'app/img/openlayers/';
        OpenLayers.DOTS_PER_INCH = GEOR.config.MAP_DOTS_PER_INCH;
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
        if (GEOR.config.ANONYMOUS) {
            GEOR.styler = null;
            GEOR.querier = null;
        }

        GEOR.ajaxglobal.init();
        var layerStore = GEOR.map.create();
        var map = layerStore.map;
        GEOR.wmc.init(layerStore);
        GEOR.print.init(layerStore);
        if (GEOR.getfeatureinfo) {
            GEOR.getfeatureinfo.init(map);
        }
        if (GEOR.querier) {
            GEOR.querier.init(map);
        }
        if (GEOR.resultspanel) {
            GEOR.resultspanel.init(map);
        }
        GEOR.waiter.init();

        // handle layerstore initialisation with wms/services/wmc from "panier"
        GEOR.mapinit.init(layerStore);

        /*
         * Create the page's layout.
         */

        var eastItems = [
            new Ext.Panel({
                // this panel contains the "manager layer" and
                // "querier" components
                region: (GEOR.editing !== undefined) ? "north" : "center",
                height: 270, // has no effect when region is
                             // "center"
                layout: "card",
                activeItem: 0,
                title: "Couches disponibles",
                split: (GEOR.editing !== undefined),
                collapsible: (GEOR.editing !== undefined),
                collapsed: (GEOR.editing !== undefined),
                defaults: {
                    border:false
                },
                items: [
                    Ext.apply({
                    }, GEOR.managelayers.create(layerStore))
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
                    }, GEOR.recenter.create(map)),
                    Ext.apply({
                        title: "Adresses"
                    }, GEOR.address.create(map)),
                    Ext.apply({
                        title: "Référentiels"
                    }, GEOR.referentials.create(map, "geob_loc"))
                ]
            })
        ];
        if (GEOR.editing) {
            eastItems.push(
                Ext.apply({
                    region: "center", 
                    title: "Edition"
                }, GEOR.editing.create(map))
            );
        }

        // this panel serves as the container for
        // the "search results" panel
        var southPanel = new Ext.Panel({
            region: "south",
            hidden: !GEOR.resultspanel, // hide this panel if
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
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.hide();
                    }
                },
                "expand": function() {
                    // when the user expands the panel
                    // show the features in the layer
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.show();
                    }
                }
            }
        });

        // the viewport
        var vp = new Ext.Viewport({
            layout: "border",
            items: [
                Ext.apply({region: "center"}, 
                    GEOR.mappanel.create(layerStore)), 
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
        if (GEOR.querier) {
            var querierTitle;
            GEOR.querier.events.on({
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
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.clean();
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
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.populate(options);
                    }
                }
            });
        }

        if (GEOR.getfeatureinfo) {
            GEOR.getfeatureinfo.events.on({
                "search": function(panelCfg) {
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.clean();
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
                    if (GEOR.resultspanel) {
                        GEOR.resultspanel.populate(options);
                    }
                }
            });
        }
        
        if (GEOR.resultspanel) {
            GEOR.resultspanel.events.on({
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

        GEOR.managelayers.events.on({
            "selectstyle": function(layerRecord, styles) {
                updateLayerParams(layerRecord, null, styles);
            }
        });

        if (GEOR.styler) {
            GEOR.styler.events.on({
                "sldready": function(layerRecord, sld) {
                    updateLayerParams(layerRecord, sld, null);
                    GEOR.managelayers.unselectStyles(layerRecord);
                }
            });
        }
    });
})();
