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
 * @include GEOR_geonames.js
 * @include GEOR_address.js
 * @include GEOR_referentials.js
 * @include GEOR_ajaxglobal.js
 * @include GEOR_localStorage.js
 * @include GEOR_waiter.js
 * @include GEOR_config.js
 * @include GEOR_mapinit.js
 * @include GEOR_print.js
 * @include GEOR_wmc.js
 * @include GEOR_tools.js
 * @include GEOR_wmcbrowser.js
 * Note that GEOR_getfeatureinfo.js, GEOR_resultspanel.js, GEOR_querier.js,
 * GEOR_styler.js should be included here, but they are not required by the edit module.
 * In order to make the edit build "light", those files will be added in main.cfg and not here.
 */

Ext.namespace("GEOR");

(function() {

    // monkey patching OpenLayers XML format to add the XML prolog
    // see http://applis-bretagne.fr/redmine/issues/4536
    var fn = OpenLayers.Format.XML.prototype.write;
    OpenLayers.Format.XML.prototype.write = function(node) {
        return '<?xml version="1.0" encoding="UTF-8"?>' + fn(node);
    };

    var checkRoles = function(module, okRoles) {
        // module is available for everyone 
        // if okRoles is empty or undefined:
        if (okRoles === undefined || okRoles.length === 0) {
            return;
        }
        var ok = false;
        // else, check existence of required role to activate module:
        for (var i=0, l=okRoles.length; i<l; i++) {
            if (GEOR.config.ROLES.indexOf(okRoles[i]) >= 0) {
                ok = true;
                break;
            }
        }
        // nullify module if no permission to use:
        if (!ok) {
            GEOR[module] = null;
        }
    };

    // save context string before unloading page
    window.onbeforeunload = function() {
        GEOR.ls.set("latest_context", GEOR.wmc.write());
        return null;
    };

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
        Ext.BLANK_IMAGE_URL = "lib/externals/ext/resources/images/default/s.gif";
        Ext.apply(Ext.MessageBox.buttonText, {
            yes: tr("Yes"),
            no: tr("No"),
            ok: tr("OK"),
            cancel: tr("Cancel")
        });

        /*
         * Setting of proj4js global vars.
         */
        Ext.apply(Proj4js.defs, GEOR.config.PROJ4JS_STRINGS);

        /*
         * Security stuff
         * Deactivate modules if current user roles do not match
         */
        checkRoles('styler', GEOR.config.ROLES_FOR_STYLER);
        checkRoles('querier', GEOR.config.ROLES_FOR_QUERIER);
        checkRoles('print', GEOR.config.ROLES_FOR_PRINTER);

        /*
         * Initialize the application.
         */
        var layerStore = GEOR.map.create();
        var map = layerStore.map;

        GEOR.wmc.init(layerStore);
        GEOR.tools.init(layerStore);
        if (GEOR.print) {
            GEOR.print.init(layerStore);
        }
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

        var recenteringItems = [
            Ext.apply({
                title: tr("Cities"),
                tabTip: tr("Recentering on GeoNames cities")
            }, GEOR.geonames.create(map)),
            Ext.apply({
                title: tr("Referentials"),
                tabTip: tr("Recentering on a selection of referential layers")
            }, GEOR.referentials.create(map, GEOR.config.NS_LOC))
        ];
        if (GEOR.address && GEOR.config.RECENTER_ON_ADDRESSES) {
            recenteringItems.push(Ext.apply({
                title: tr("Addresses"),
                tabTip: tr("Recentering on a given address")
            }, GEOR.address.create(map)));
        }

        /*
         * Create the page's layout.
         */
        var plugins = (GEOR.editing === undefined) ?
            [] : [Ext.ux.PanelCollapsedTitle];

        var eastItems = [
            new Ext.Panel({
                // this panel contains the "manager layer" and
                // "querier" components
                region: (GEOR.editing !== undefined) ? "north" : "center",
                height: 270, // has no effect when region is
                             // "center"
                layout: "card",
                activeItem: 0,
                title: tr("Available layers"),
                plugins: plugins,
                split: (GEOR.editing !== undefined),
                collapsible: (GEOR.editing !== undefined),
                collapsed: (GEOR.editing !== undefined),
                // we use hideMode: "offsets" here to workaround this bug in
                // extjs 3.x, see the bug report:
                // http://www.sencha.com/forum/showthread.php?107119-DEFER-1207-Slider-in-panel-with-collapsed-true-make-slider-weird
                hideMode: 'offsets',
                defaults: {
                    border:false
                },
                items: [
                    Ext.apply({
                        // nothing
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
                items: recenteringItems
            })
        ];
        if (GEOR.editing) {
            eastItems.push(
                Ext.apply({
                    region: "center",
                    title: tr("Editing")
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
            split: true,
            layout: "fit",
            collapsible: true,
            collapsed: true,
            collapseMode: "mini",
            header: false,
            height: 150,
            defaults: {
                border: false,
                frame: false
            },
            items: [{
                bodyStyle: 'padding: 5px',
                html: tr("resultspanel.emptytext")
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

        // the header
        var vpItems = GEOR.header ?
            [{
                xtype: "box",
                id: "geor_header",
                region: "north",
                height: 90,
                el: "go_head"
            }] : [];


        vpItems.push(
            // the map panel
            Ext.apply({
                region: "center"
            }, GEOR.mappanel.create(layerStore)), {
            // the east side
            region: "east",
            layout: "border",
            width: 300,
            minWidth: 300,
            maxWidth: 500,
            split: true,
            collapseMode: "mini",
            collapsible: true,
            frame: false,
            border: false,
            header: false,
            items: eastItems
        }, southPanel);

        // the viewport
        var vp = new Ext.Viewport({
            layout: "border",
            items: vpItems
        });

        // Handle layerstore initialisation
        // with wms/services/wmc from "panier"
        GEOR.mapinit.init(layerStore, function() {
            GEOR.ajaxglobal.init();
            GEOR.tools.restore();
        });
        // Note: we're providing GEOR.ajaxglobal.init as a callback, so that
        // errors when loading WMC are not catched by GEOR.ajaxglobal
        // but by the mapinit module, which handles them more appropriately

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
                        text: tr('Close'),
                        handler: function() {
                            // we also need to hide querier vector layer:
                            eastItems[0].getComponent(1).tearDown();
                            eastItems[0].setTitle(tr("Available layers"));
                            eastItems[0].getLayout().setActiveItem(0);
                        }
                    });
                    panelCfg.buttons.reverse();
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
                },
                "shutdown": function() {
                    southPanel.collapse();
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

        GEOR.wmcbrowser.events.on({
            "contextselected": function(o) {
                try {
                    GEOR.wmc.read(o.wmcString, true, true);
                } catch (err) {
                    return false;
                }
            }
        });
    });
})();
