/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
 * @include GEOB_config.js
 * @include GEOB_util.js
 * @include GeoExt/data/PrintProvider.js
 * @include GeoExt/data/PrintPage.js
 * @include GeoExt/plugins/PrintPageField.js
 * @include GeoExt/plugins/PrintProviderField.js
 */

Ext.namespace("GEOB");

GEOB.print = (function() {

    /*
     * Private
     */

    /**
     * Property: cmpId
     * {String} The component id.
     */
    var cmpId = 'GEOB_print';

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
     * property: defaultCustomParams
     * {Object} Default custom params for printPage.
     */
    var defaultCustomParams = {
        mapTitle: "",
        copyright: "",
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
        var attr = [], defaultAttr = false;
        layerStore.each(function(r) {
            if (!r.get('attribution')) {
                return;
            }
            if (r.get('attribution').title && attr.indexOf(r.get('attribution').title) < 0) {
                attr.push(r.get('attribution').title);
            } else if (!r.get('attribution').title && !defaultAttr) {
                attr.push(GEOB.config.DEFAULT_ATTRIBUTION);
                defaultAttr = true;
            }
        });
        return 'Source'+ ((attr.length > 1)?'s':'') +' : '+attr.join(', ');
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

        // The printProvider that connects us to the print service
        printProvider = new GeoExt.data.PrintProvider({
            url: "pdf",
            baseParams: {
                url: GEOB.config.MAPFISHAPP_URL + "pdf"
            },
            listeners: {
                "loadcapabilities": function() {
                    printPage = new GeoExt.data.PrintPage({
                        printProvider: printProvider,
                        customParams: defaultCustomParams
                    });
                },
                "beforeencodelayer": function(layer) {
                    if ((layer.CLASS_NAME == "OpenLayers.Layer.Vector") ||
                        (layer.CLASS_NAME == "OpenLayers.Layer.Vector.RootContainer")) {
                        return false;
                    }
                },
                "beforeprint": function() {
                    mask.show();
                },
                "print": function() {
                    mask.hide();
                },
                "printexception": function() {
                    mask.hide();
                    GEOB.util.errorDialog({
                        title: 'Impression impossible',
                        msg: [
                            'Le service d\'impression a signalé une erreur.',
                            'Contactez l\'administrateur de la plateforme.'
                        ].join('<br/>')
                    });
                },
                "encodelayer": function(pp, layer, encLayer) {
                    //console.log(layer.url); // debug
                    if (GEOB.config.WMSC2WMS.hasOwnProperty(layer.url)) {
                        if (GEOB.config.WMSC2WMS[layer.url] !== undefined) {
                            //console.log(layer.name + ' - tuilée avec WMS référencé'); // debug
                            encLayer.baseURL = GEOB.config.WMSC2WMS[layer.url];
                        } else {
                            //console.log(layer.name + ' - tuilée sans WMS référencé'); // debug
                            GEOB.util.infoDialog({
                                title: 'Couche non disponible pour impression',
                                msg: [
                                    'La couche ' + layer.name + ' ne peut pas encore être imprimée.',
                                    'Contactez l\'administrateur de la plateforme.'
                                ].join('<br/>')
                            });
                        }
                    }
                }
            }
        });
    };

    var showWindow = function() {
        if (!printPage) {
            GEOB.util.errorDialog({
                title: 'Impression non disponible',
                msg: [
                    'Le service d\'impression est actuellement inaccessible.',
                    'Contactez l\'administrateur de la plateforme.'
                ].join('<br/>')
            });
        }
        if (win === null) {
            // The form with fields controlling the print output
            var formPanel = new Ext.form.FormPanel({
                bodyStyle: "padding:5px",
                items: [{
                        xtype: 'textfield',
                        fieldLabel: 'Titre',
                        width: 200,
                        name: 'mapTitle',
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
                        xtype: 'checkbox',
                        fieldLabel: 'Mini-carte',
                        name: 'showOverview',
                        checked: defaultCustomParams.showOverview,
                        plugins: new GeoExt.plugins.PrintPageField({
                            printPage: printPage
                        })

                    }, {
                        xtype: 'checkbox',
                        fieldLabel: 'Nord',
                        name: 'showNorth',
                        checked: defaultCustomParams.showNorth,
                        plugins: new GeoExt.plugins.PrintPageField({
                            printPage: printPage
                        })

                    }, {
                        xtype: 'checkbox',
                        fieldLabel: 'Echelle',
                        name: 'showScalebar',
                        checked: defaultCustomParams.showScalebar,
                        plugins: new GeoExt.plugins.PrintPageField({
                            printPage: printPage
                        })

                    },{
                        xtype: 'checkbox',
                        fieldLabel: 'Date',
                        name: 'showDate',
                        checked: defaultCustomParams.showDate,
                        plugins: new GeoExt.plugins.PrintPageField({
                            printPage: printPage
                        })

                    }, {
                        xtype: 'checkbox',
                        fieldLabel: 'Légende',
                        name: 'showLegend',
                        checked: defaultCustomParams.showLegend,
                        plugins: new GeoExt.plugins.PrintPageField({
                            printPage: printPage
                        })

                    }, {
                        xtype: "combo",
                        store: printProvider.layouts,
                        displayField: "name",
                        fieldLabel: "Format",
                        typeAhead: true,
                        mode: "local",
                        triggerAction: "all",
                        plugins: new GeoExt.plugins.PrintProviderField({
                            printProvider: printProvider
                        })
                    }, {
                        xtype: "combo",
                        store: printProvider.dpis,
                        displayField: "name",
                        fieldLabel: "Résolution",
                        tpl: '<tpl for="."><div class="x-combo-list-item">{name} dpi</div></tpl>',
                        typeAhead: true,
                        mode: "local",
                        triggerAction: "all",
                        plugins: new GeoExt.plugins.PrintProviderField({
                            printProvider: printProvider
                        }),
                        // the plugin will work even if we modify a combo value
                        setValue: function(v) {
                            v = parseInt(v) + " dpi";
                            Ext.form.ComboBox.prototype.setValue.apply(this, arguments);
                        }
                    }
                ],
                buttons: [{
                    text: "Imprimer",
                    handler: function() {
                        printPage.customParams.copyright = getLayerSources();
                        printPage.fit(layerStore.map, false);
                        printProvider.print(layerStore.map, printPage, {
                            legend: legendPanel
                        });
                    }
                }, {
                    text: "Fermer",
                    handler: function() {
                        Ext.getCmp(cmpId+'_print_window').hide();
                    }
                }]
            });

            win = new Ext.Window({
                title: 'Impression',
                id: cmpId+'_print_window',
                resizable: false,
                width: 350,
                autoHeight: true,
                //modal: true,
                closeAction: 'hide',
                items: [formPanel]
            });
        }
        
        win.show();
        
        if (!mask) {
            mask = new Ext.LoadMask(win.bwrap.dom, {
                msg:"impression en cours..."
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
                    tooltip: "imprimer la carte courante",
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
