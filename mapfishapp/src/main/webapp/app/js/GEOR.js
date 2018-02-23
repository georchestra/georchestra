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
 * @include GEOR_edit.js
 * @include GEOR_wmcbrowser.js
 * @include GEOR_getfeatureinfo.js
 * @include GEOR_selectfeature.js
 * @include GEOR_ResultsPanel.js
 * @include GEOR_Querier.js
 * @include GEOR_styler.js
 * @include GEOR_wmc.js
 * @include GEOR_helper.js
 * @include Ext.state.LocalStorageProvider.js
 */

Ext.namespace("GEOR");

// monkey patching before app loads
(function(){

    // OpenLayers XML format: adding the XML prolog
    // see http://applis-bretagne.fr/redmine/issues/4536
    var p = OpenLayers.Format.XML.prototype, fn = p.write;
    p.write = function(node) {
        return '<?xml version="1.0" encoding="UTF-8"?>' +
            // fix for https://github.com/georchestra/georchestra/issues/773 :
            fn.apply(this, [node]).replace(new RegExp('xmlns:NS\\d+="" NS\\d+:', 'g'), '');
    };
    var sa = p.setAttributeNS;
    p.setAttributeNS = function(node, uri) {
        sa.apply(this, arguments);
        // fix Chrome 36/37 issue, see https://github.com/georchestra/georchestra/issues/759
        if (uri == this.namespaces.xlink && node.setAttributeNS) {
            node.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:xlink', this.namespaces.xlink);
        }
    };

    // Initialize doc classes, see https://github.com/georchestra/georchestra/issues/539
    // to workaround an ExtJS bug.
    var initExtCss = function() {
        // find the body element
        var bd = document.body || document.getElementsByTagName('body')[0];
        if (!bd) {
            return false;
        }
        var cls = [];
        if (Ext.isGecko) {
            cls.push('ext-gecko');
        }
        Ext.fly(bd, '_internal').addClass(cls);
        return true;
    };
    if (!initExtCss()) {
        Ext.onReady(initExtCss);
    }

    // String fill polyfill
    if (!String.prototype.trim) {
        String.prototype.trim = function () {
            return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
        };
    }

    // Redefine the grid template to enable text selection
    if (!Ext.grid.GridView.prototype.templates) {
        Ext.grid.GridView.prototype.templates = {};
    }
    Ext.grid.GridView.prototype.templates.cell = new Ext.Template(
        '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
        '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
        '</td>'
    );
})();



(function() {

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
    };

    Ext.onReady(function() {
        var tr = OpenLayers.i18n;

        /*
         * Setting of OpenLayers global vars.
         */
        OpenLayers.Lang.setCode(GEOR.config.LANG);
        OpenLayers.Number.thousandsSeparator = " ";
        OpenLayers.ImgPath = GEOR.config.PATHNAME + '/app/img/openlayers/';
        OpenLayers.DOTS_PER_INCH = GEOR.config.MAP_DOTS_PER_INCH;

        /*
         * Setting of Ext global vars.
         */
        Ext.BLANK_IMAGE_URL = GEOR.config.PATHNAME + "/app/img/s.gif";
        Ext.apply(Ext.MessageBox.buttonText, {
            yes: tr("Yes"),
            no: tr("No"),
            ok: tr("OK"),
            cancel: tr("Cancel")
        });

        Ext.isIE11 = navigator.userAgent.toLowerCase().lastIndexOf('rv:11') > -1;
        Ext.isIE = Ext.isIE || Ext.isIE11;

        /*
         * Setting of proj4js global vars.
         */
        Proj4js.libPath = GEOR.config.PATHNAME + "/lib/proj4js/lib/";
        Ext.apply(Proj4js.defs, GEOR.config.PROJ4JS_STRINGS);

        // State manager for the viewer
        Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider({
            prefix: "geor-viewer-"
        }));

        /*
         * Security stuff
         * Deactivate modules if current user roles do not match
         */
        checkRoles('styler', GEOR.config.ROLES_FOR_STYLER);
        checkRoles('Querier', GEOR.config.ROLES_FOR_QUERIER);
        checkRoles('print', GEOR.config.ROLES_FOR_PRINTER);
        checkRoles('edit', GEOR.config.ROLES_FOR_EDIT);

        // deactivate thesaurus tab in layer finder if required:
        if (!GEOR.config.THESAURUS_SEARCH) {
            GEOR["cswbrowser"] = null;
        }

        /*
         * Initialize the application.
         */
        var layerStore = GEOR.map.create(),
            map = layerStore.map,
            mapPanel = GEOR.mappanel.create(layerStore);

        GEOR.wmc.init(layerStore);
        GEOR.tools.init(mapPanel);
        if (GEOR.edit) {
            GEOR.edit.init(map);
        }
        GEOR.getfeatureinfo.init(layerStore);
        GEOR.selectfeature.init(map);
        GEOR.waiter.init();
        GEOR.wmcbrowser.init();
        GEOR.cswquerier.init(map);

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

        var eastItems = [
            new Ext.Panel({
                // this panel contains the "manager layer" component
                region: "center",
                height: 270, // has no effect when region is
                             // "center"
                layout: "card",
                activeItem: 0,
                title: tr("Available layers"),
                split: false,
                collapsible: false,
                collapsed: false,
                // we use hideMode: "offsets" here to workaround this bug in
                // extjs 3.x, see the bug report:
                // http://www.sencha.com/forum/showthread.php?107119-DEFER-1207-Slider-in-panel-with-collapsed-true-make-slider-weird
                //hideMode: 'offsets',
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
                id: "tabs",
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

        var southPanel = new Ext.TabPanel({
            region: "south",
            id: "southpanel",
            stateful: false,
            split: true,
            collapsible: true,
            collapsed: true,
            collapseMode: "mini",
            hideCollapseTool: true,
            header: false,
            height: 200,
            enableTabScroll: true,
            activeTab: 0,
            defaults: {
                layout: 'fit',
                border: false,
                frame: false
            },
            items: [],
            listeners: {
                'collapse': function(panel) {
                    panel.items.each(function(tab) {
                        tab.lower();
                    });
                },
                'expand': function(panel) {
                    panel.getActiveTab().raise();
                },
                'tabchange': function(panel, t) {
                    panel.items.each(function(tab) {
                        tab.lower();
                    });
                    t && t.raise();
                },
                'beforeadd': function(panel, incomingTab) {
                    // if new component itemId matches an already existing one
                    // => clean previous tab
                    var previousTab;
                    panel.items.each(function(tab) {
                        if (tab.itemId && tab.itemId == incomingTab.itemId) {
                            previousTab = tab;
                        }
                    });
                    if (previousTab) {
                        previousTab.clean();
                        panel.remove(previousTab);
                    }
                    return true;
                }
            }
        });

        southPanel.doLayout();

        // the header
        var vpItems = GEOR.header ?
            [{
                xtype: "box",
                id: "geor_header",
                region: "north",
                height: GEOR.config.HEADER_HEIGHT,
                el: "go_head"
            }] : [];

        vpItems.push(
            // the map panel
            mapPanel, {
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

        /*
         * Register to events on various modules to deal with
         * the communication between them. Really, we're
         * acting as a mediator between the modules with
         * the objective of making them independent.
         */

        // When the wmc module is asked to restore a context, it informs
        // the mediator about it, with the number of WMS records
        // to restore. As a result, we're deactivating OGCExceptionReports.
        // But we're always listening to describelayers. When the number of
        // WMS layers to restore is reached, we're reactivating
        // OGCExceptionReports.
        var describeLayerCount;
        GEOR.wmc.events.on({
            "beforecontextrestore": function(count) {
                // prevent OGCExceptionReport warnings during context restore:
                GEOR.ajaxglobal.disableAllErrors = true;
                describeLayerCount = count;
            }
        });
        GEOR.map.events.on({
            "describelayer": function(record) {
                // update the layer panel in layer tree
                // when the layer has been described:
                GEOR.managelayers.updatePanel(record);

                // restore OGCExceptionReport warnings on context restored,
                // and all describelayer queries finished.
                describeLayerCount -= 1;
                if (describeLayerCount == 0) {
                    GEOR.ajaxglobal.disableAllErrors = false;
                }
            }
        });

        // Handle layerstore initialisation
        // with wms/services/wmc from "panier"
        GEOR.mapinit.init(layerStore, function() {
            GEOR.ajaxglobal.init();
            GEOR.tools.restore();
            if (GEOR.print) {
                GEOR.print.init(layerStore);
            }
        });
        // Note: we're providing GEOR.ajaxglobal.init as a callback, so that
        // errors when loading WMC are not catched by GEOR.ajaxglobal
        // but by the mapinit module, which handles them more appropriately
        GEOR.mapinit.events.on({
            "searchresults": function(options) {
                var tab = new GEOR.ResultsPanel({
                    html: tr("resultspanel.emptytext"),
                    itemId: options.layerId,
                    tabTip: options.tooltip,
                    title: options.title,
                    map: map
                });
                tab.populate({
                    features: options.features,
                    // here we do have a valid model (got from describeFeatureType)
                    model: options.model
                });
                southPanel.add(tab);
                southPanel.setActiveTab(tab);
                southPanel.expand();
                tab._zoomToFeatures(options.features);
            }
        });


        if (GEOR.getfeatureinfo) {
            GEOR.getfeatureinfo.events.on({
                "searchresults": function(options) {
                    southPanel.expand();
                    Ext.iterate(options.results, function(featureType, result) {
                        var tab = new GEOR.ResultsPanel({
                            html: tr("resultspanel.emptytext"),
                            itemId: result.layerId, // always one tab per featuretype
                            tabTip: result.tooltip,
                            title: result.title,
                            map: map
                        });
                        tab.populate({
                            features: result.features
                        });
                        southPanel.add(tab);
                        southPanel.setActiveTab(tab);
                    });
                    southPanel.doLayout();
                },
                "shutdown": function() {
                    southPanel.collapse();
                }
            });
        }

        if (GEOR.selectfeature) {
            GEOR.selectfeature.events.on({
                "searchresults": function(options) {
                    southPanel.expand();
                    var tab = new GEOR.ResultsPanel({
                        html: tr("resultspanel.emptytext"),
                        itemId: options.layerId,
                        tabTip: options.tooltip,
                        noDelete: true, // deletion is useless: deactivate control in bbar
                        title: options.title,
                        sfControl: options.ctrl,
                        map: map
                    });
                    tab.populate({
                        features: options.features,
                        addLayerToMap: options.addLayerToMap
                    });
                    southPanel.add(tab);
                    southPanel.setActiveTab(tab);
                },
                "shutdown": function() {
                    southPanel.collapse();
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
            },
            "beforecontextcleared": function() {
                // warn other modules about what's goign on
                // (gfi, selectfeature, querier, editor, styler)
                // so that they can properly shutdown.
                if (GEOR.edit) {
                    GEOR.edit.deactivate();
                }
                GEOR.styler.deactivate();
                GEOR.selectfeature.deactivate();
                GEOR.getfeatureinfo.deactivate();
                southPanel.collapse();
            },
            "searchresults": function(options) {
                southPanel.expand();
                var tab = new GEOR.ResultsPanel({
                    html: tr("resultspanel.emptytext"),
                    tabTip: options.tooltip,
                    itemId: options.layerId, // internally used to link layer with its results tab
                    title: options.title,
                    map: map
                });
                tab.populate({
                    features: options.features,
                    // here we do have a valid model (got from describeFeatureType)
                    model: options.model
                });
                southPanel.add(tab);
                southPanel.setActiveTab(tab);
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
                return GEOR.wmc.read(o.wmcString, !o.noReset, true);
            }
        });
    });
})();
