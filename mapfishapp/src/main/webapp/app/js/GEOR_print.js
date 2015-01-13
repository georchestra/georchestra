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
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include GeoExt/widgets/MapPanel.js
 * @requires GeoExt/data/PrintProvider.js
 * @include GeoExt/data/PrintPage.js
 * @include GeoExt/plugins/PrintPageField.js
 * @include GeoExt/plugins/PrintProviderField.js
 * @include GeoExt/plugins/PrintExtent.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Style.js
 */

Ext.namespace("GEOR");

GEOR.print = (function() {

    /*
     * Private
     */

    /**
     * Property: mask
     * {Ext.LoadMask} The treePanel loadMask
     */
    var mask = null;

    /**
     * Property: win
     * {Ext.Window} The Ext window opened when the print
     * action triggers.
     */
    var win = null;

    /**
     * Property: action
     * {Ext.Action} The action.
     */
    var action = null;

    /**
     * Property: layerStore
     * {GeoExt.data.LayerStore} The layer store.
     */
    var layerStore = null;

    /**
     * Property: printProvider
     * {GeoExt.data.PrintProvider} The print provider.
     */
    var printProvider = null;

    /**
     * Property: printExtent
     * {GeoExt.plugins.PrintExtent} The print extent.
     */
    var printExtent;

    /**
     * property: printpage
     * {geoext.data.printpage} The print page.
     */
    var printPage = null;

    /**
     * property: legendPanel
     * {GeoExt.LegendPanel} The legend panel.
     */
    var legendPanel = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: boundsLayer
     * {OpenLayers.Layer.Vector} for print bounds
     */
    var boundsLayer;

    /**
     * Constant: VECTOR_LAYER_NAME
     * {String} The vector layer name, as used across this module
     */
    var VECTOR_LAYER_NAME = '__georchestra_print_bounds_';

    /**
     * property: defaultCustomParams
     * {Object} Default custom params for printPage.
     */
    var defaultCustomParams = {
        mapTitle: "",
        mapComments: "",
        copyright: "",
        scaleLbl: "",
        dateLbl: "",
        showOverview: true,
        showNorth: true,
        showScalebar: true,
        showDate: true,
        showLegend: true
    };

    /**
     * Method: getLayerSources
     * Creates an attribution string from map layers
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application's layer store.
     *
     * Returns:
     * {String} The attribution string
     */
    var getLayerSources = function() {
        var attr = [];
        layerStore.each(function(r) {
            if (!r.get('attribution')) {
                return;
            }
            if (r.get('attribution').title && attr.indexOf(r.get('attribution').title) < 0) {
                attr.push(r.get('attribution').title);
            }
        });
        return ((attr.length > 1)?tr("Sources: "):tr("Source: ")) +attr.join(', ');
    };

    /**
     * Method: getProjection
     * Creates a string with the projection name
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application's layer store.
     *
     * Returns:
     * {String} The projection string
     */
    var getProjection = function() {
        var s = '', epsg = layerStore.map.getProjection();
        proj = Proj4js.defs[epsg];
        if (!proj) {
            return '';
        }
        Ext.each(proj.split('+'), function(r) {
            var c = r.split("title=");
            if (c.length > 1) {
                s = c[1].replace(/,/g,'').trim();
            }
        });
        if (!s) {
            return '';
        }
        return tr("Projection: PROJ", {'PROJ': s});
    };

    /**
     * Method: initialize
     *
     * Initialize the print module.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application's layer store.
     */
    var initialize = function(ls) {

        layerStore = ls;
        tr = OpenLayers.i18n;
        boundsLayer = new OpenLayers.Layer.Vector(VECTOR_LAYER_NAME, {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    //fillColor: '#ee9900',
                    //fillOpacity: 0.25,
                    fillOpacity: 0,
                    strokeColor: "#000000",
                    strokeOpacity: 1,
                    strokeWidth: 2
                }),
                "temporary": new OpenLayers.Style({
                    fillColor: "#ffffff",
                    fillOpacity: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.6,
                    strokeWidth: 1,
                    pointRadius: 5,
                    cursor: "${role}"
                }),
                "rotate": new OpenLayers.Style({
                    externalGraphic: GEOR.config.PATHNAME + "/app/img/print-rotate.png",
                    fillOpacity: 1.0,
                    graphicXOffset: 8,
                    graphicYOffset: 8,
                    graphicWidth: 20,
                    graphicHeight: 20,
                    cursor: "pointer",
                    display: "${display}",
                    rotation: "${rotation}"
                }, {
                    context: {
                        display: function(f) {
                            return f.attributes.role == "se-rotate" ? "" : "none";
                        },
                        rotation: function(f) {
                            return printPage.rotation;
                        }
                    }
                })
            })
        });

        // The printProvider that connects us to the print service
        var serviceUrl = GEOR.config.PATHNAME + '/pdf';
        GEOR.waiter.show(); // an XHR is required here for the print capabilities
        printProvider = new GeoExt.data.PrintProvider({
            url: serviceUrl,
            autoLoad: true,
            outputFormatsEnabled: true,
            baseParams: {
                url: serviceUrl
            },
            listeners: {
                "loadcapabilities": function(provider, caps) {
                    // Filter out layouts from the provider.layouts store
                    // that the current user does not have the right to use:
                    // see http://applis-bretagne.fr/redmine/issues/4497
                    provider.layouts.filterBy(function(record) {
                        var layout = record.get('name'),
                            acl = GEOR.config.PRINT_LAYOUTS_ACL[layout];
                        // empty or not specified means "layout allowed for everyone"
                        if (!acl || acl.length === 0) {
                            return true;
                        }
                        for (var i=0, l=GEOR.config.ROLES.length; i<l; i++) {
                            // check current role is allowed to use current layout:
                            if (acl.indexOf(GEOR.config.ROLES[i]) >= 0) {
                                return true;
                            }
                        }
                        return false;
                    });
                    // create printPage & printExtent
                    printPage = new GeoExt.data.PrintPage({
                        printProvider: printProvider,
                        customParams: defaultCustomParams
                    });
                    printExtent = new GeoExt.plugins.PrintExtent({
                        layer: boundsLayer,
                        printProvider: printProvider,
                        transformFeatureOptions: {
                            rotationHandleSymbolizer: "rotate"
                        }
                    });
                    printExtent.init(GeoExt.MapPanel.guess());
                },
                "beforeencodelayer": function(printProvider, layer) {
                    if ((layer.CLASS_NAME === "OpenLayers.Layer.Vector") &&
                        layer.name === VECTOR_LAYER_NAME) {
                        // do not print bounds layer
                        return false;
                    }
                },
                "beforeprint": function(provider, map, pages, o) {
                    mask.show();
                    pages[0].customParams.copyright = getLayerSources();
                    pages[0].customParams.projection = getProjection();
                    pages[0].customParams.scaleLbl = tr("Scale: ");
                    pages[0].customParams.dateLbl = tr("Date: ");
                    provider.customParams.showLegend = pages[0].customParams.showLegend;
                    // set a custom PDF file name:
                    provider.customParams.outputFilename = GEOR.config.PDF_FILENAME;
                },
                "print": function() {
                    mask.hide();
                },
                "printexception": function() {
                    mask.hide();
                    GEOR.util.errorDialog({
                        title: tr("Print error"),
                        msg: [
                            tr("Print server returned an error"),
                            tr("Contact platform administrator")
                        ].join('<br/>')
                    });
                },
                "encodelayer": function(pp, layer, encLayer) {
                    if (encLayer && encLayer.type === "WMTS") {
                        // FIXME: these values are incorrect and prevent printing of WMTS layers
                        delete encLayer['minScaleDenominator'];
                        delete encLayer['maxScaleDenominator'];
                    }
                    if (GEOR.config.WMSC2WMS.hasOwnProperty(layer.url)) {
                        if (GEOR.config.WMSC2WMS[layer.url] !== undefined) {
                            //console.log(layer.name + ' - tuilée avec WMS référencé'); // debug
                            encLayer.baseURL = GEOR.config.WMSC2WMS[layer.url];
                        } else {
                            //console.log(layer.name + ' - tuilée sans WMS référencé'); // debug
                            GEOR.util.infoDialog({
                                title: tr("Layer unavailable for printing"),
                                msg: [
                                    tr("The NAME layer cannot be printed.", {'NAME': layer.name}),
                                    tr("Contact platform administrator")
                                ].join('<br/>')
                            });
                        }
                    }
                }
            }
        });
    };

    /**
     * Method: formatHandler
     * Callback for checkHandler
     *
     * Parameters:
     * format - {String} The output format (eg: "png" or "pdf")
     */
    var formatHandler = function(format) {
        var r = printProvider.outputFormats.find("name", format);
        if (r >= 0) {
            printProvider.setOutputFormat(printProvider.outputFormats.getAt(r));
        } else {
            alert(tr("print.unknown.format",
                {'FORMAT': format}));
        }
    };

    /**
     * Method: showWindow
     *
     */
    var showWindow = function() {
        if (!printPage) {
            GEOR.util.errorDialog({
                title: tr("Unable to print"),
                msg: [
                    tr("The print server is currently unreachable"),
                    tr("Contact platform administrator")
                ].join('<br/>')
            });
            return;
        }
        if (win === null) {
            // default values from config:
            var r = printProvider.layouts.find("name",
                GEOR.config.DEFAULT_PRINT_LAYOUT);
            if (r >= 0) {
                printProvider.setLayout(printProvider.layouts.getAt(r));
            } else {
                alert(tr("print.unknown.layout",
                    {'LAYOUT': GEOR.config.DEFAULT_PRINT_LAYOUT}));
            }
            r = printProvider.dpis.find("value",
                GEOR.config.DEFAULT_PRINT_RESOLUTION);
            if (r >= 0) {
                printProvider.setDpi(printProvider.dpis.getAt(r));
            } else {
                alert(tr("print.unknown.resolution",
                    {'RESOLUTION': GEOR.config.DEFAULT_PRINT_RESOLUTION}));
            }
            // The form with fields controlling the print output
            var formPanel = new Ext.form.FormPanel({
                bodyStyle: "padding:5px",
                hideLabels: true,
                items: [{
                    xtype: 'textfield',
                    emptyText: tr("Title"),
                    width: 420,
                    name: 'mapTitle',
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    }),
                    listeners: {
                        "keypress": function(f, e) {
                            // transfer focus on Print button on ENTER
                            if (e.getKey() === e.ENTER) {
                                win.getFooterToolbar().getComponent('print').focus();
                            }
                        }
                    }
                }, {
                    xtype: 'textarea',
                    emptyText: tr("Comments"),
                    width: 420,
                    name: 'mapComments',
                    grow: false,
                    enableKeyEvents: false,
                    selectOnFocus: true,
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    })
                }, {
                    xtype: 'hidden',
                    name: 'copyright',
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    })
                }, {
                    xtype: 'hidden',
                    name: 'projection',
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    })
                }, {
                    layout: 'column',
                    bodyStyle: 'padding:5px',
                    border: false,
                    items: [{
                        columnWidth: .5,
                        layout: 'form',
                        border: false,
                        labelAlign: 'left',
                        labelSeparator: tr("labelSeparator"),
                        labelWidth: 70,
                        items: [{
                            xtype: 'checkbox',
                            fieldLabel: tr("Minimap"),
                            name: 'showOverview',
                            checked: defaultCustomParams.showOverview,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, /*{
                            xtype: 'checkbox',
                            fieldLabel: tr("North"),
                            name: 'showNorth',
                            checked: defaultCustomParams.showNorth,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, */{
                            xtype: 'checkbox',
                            fieldLabel: tr("Scale"),
                            name: 'showScalebar',
                            checked: defaultCustomParams.showScalebar,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, {
                            xtype: "combo",
                            store: printProvider.layouts,
                            lastQuery: '', // required to apply rights filter
                            displayField: "name",
                            valueField: "name",
                            fieldLabel: tr("Format"),
                            width: 110,
                            forceSelection: true,
                            editable: false,
                            mode: "local",
                            triggerAction: "all",
                            plugins: new GeoExt.plugins.PrintProviderField({
                                printProvider: printProvider
                            })
                        }, {
                            xtype: "combo",
                            store: printProvider.dpis,
                            displayField: "name",
                            valueField: "value",
                            fieldLabel: tr("Resolution"),
                            width: 110,
                            forceSelection: true,
                            editable: false,
                            tpl: '<tpl for="."><div class="x-combo-list-item">{name} dpi</div></tpl>',
                            mode: "local",
                            triggerAction: "all",
                            plugins: new GeoExt.plugins.PrintProviderField({
                                printProvider: printProvider
                            }),
                            // the plugin will work even if we modify a combo value
                            setValue: function(v) {
                                var text = v;
                                if (this.valueField) {
                                    var r = this.findRecord(this.valueField, v);
                                    if (r) {
                                        text = r.data[this.displayField];
                                    }
                                }
                                text = parseInt(v) + " dpi";
                                this.lastSelectionText = text;
                                Ext.form.ComboBox.superclass.setValue.call(this, text);
                                this.value = v;
                                return this;
                            }
                        }]
                    }, {
                        columnWidth: .5,
                        layout: 'form',
                        border: false,
                        labelAlign: 'left',
                        defaultType: 'textfield',
                        labelSeparator: tr("labelSeparator"),
                        labelWidth: 70,
                        items: [{
                            xtype: 'checkbox',
                            fieldLabel: tr("Date"),
                            name: 'showDate',
                            checked: defaultCustomParams.showDate,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, {
                            xtype: 'checkbox',
                            fieldLabel: tr("Legend"),
                            name: 'showLegend',
                            checked: defaultCustomParams.showLegend,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, {
                            xtype: "combo",
                            fieldLabel: tr("Scale"),
                            store: printProvider.scales,
                            forceSelection: true,
                            editable: false,
                            width: 110,
                            displayField: "name",
                            mode: "local",
                            triggerAction: "all",
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }, {
                            xtype: "numberfield",
                            fieldLabel: tr("Rotation"),
                            width: 110,
                            name: "rotation",
                            enableKeyEvents: true,
                            plugins: new GeoExt.plugins.PrintPageField({
                                printPage: printPage
                            })
                        }]
                    }]
                }]
            });

            win = new Ext.Window({
                title: tr("Print the map"),
                resizable: false,
                constrainHeader: true,
                animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el,
                border: false,
                width: 450,
                x: 0,
                y: Ext.get(layerStore.map.div).getTop() + 1,
                autoHeight: true,
                closeAction: 'hide',
                items: [formPanel],
                listeners: {
                    "show": function() {
                        // show print extent:
                        printExtent.addPage(printPage);
                        printExtent.show();
                        /*
                        // focus first field on show
                        var field = formPanel.getForm().findField('mapTitle');
                        field.focus('', 50);
                        */
                        var btn = win.getFooterToolbar().getComponent('print');
                        (function(){
                            btn.focus();
                        }).defer(50);
                    },
                    "hide": function() {
                        printExtent.removePage(printPage);
                        printExtent.hide();
                    }
                },
                buttons: [{
                    text: tr("Close"),
                    handler: function() {
                        win.hide();
                    }
                }, {
                    xtype: "splitbutton",
                    text: tr("Print"),
                    arrowTooltip: tr("Pick an output format"),
                    minWidth: 90,
                    itemId: 'print',
                    iconCls: 'mf-print-action',
                    handler: function() {
                        printProvider.print(layerStore.map, printPage, {
                            legend: legendPanel
                        });
                    },
                    menuAlign: "tr-br",
                    menu: new Ext.menu.Menu({
                        items: [{
                            checked: true,
                            group: 'print-format',
                            text: tr("PDF"),
                            checkHandler: formatHandler.createCallback("pdf")
                        }, {
                            checked: false,
                            group: 'print-format',
                            text: tr("PNG"),
                            checkHandler: formatHandler.createCallback("png")
                        }]
                    })
                }]
            });
        }
        win.show();

        if (!mask) {
            mask = new Ext.LoadMask(win.bwrap.dom, {
                msg: tr("Printing...")
            });
        }
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: init
         * Initialize the print module
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application's layer store.
         */
        init: function(layerStore) {
            initialize(layerStore);
        },

        /**
         * APIMethod: getAction
         * Get the print action (for inclusion in a toolbar).
         *
         * Returns:
         * {Ext.Action} The action.
         */
        getAction: function() {
            if (action === null) {
                action = new Ext.Action({
                    iconCls: 'mf-print-action',
                    text: '',
                    tooltip: OpenLayers.i18n("Print current map"),
                    handler: showWindow
                });
            }
            return action;
        },

        /**
         * APIMethod: setLegend
         * Set the legend panel
         *
         * Parameters:
         * l - {GeoExt.LegendPanel} the legend panel
         */
        setLegend: function(l) {
            legendPanel = l;
        }
    };
})();


GeoExt.data.PrintProvider.prototype.encoders.legends["gx_vectorlegend"] = function(legend) {
    var enc = this.encoders.legends.base.call(this, legend);
    enc[0].classes.push({
        name: ""
    });
    return enc;
};
