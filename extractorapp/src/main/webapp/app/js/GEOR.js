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
 * @include GEOR_proj4jsdefs.js
 * @include GEOR_data.js
 * @include GEOR_map.js
 * @include GEOR_dlform.js
 * @include GEOR_config.js
 * @include GEOR_ajaxglobal.js
 * @include GEOR_waiter.js
 * @include GEOR_layerstree.js
 * @include GEOR_mappanel.js
 * @include GEOR_layeroptions.js
 * @include GEOR_referentials.js
 */

Ext.namespace("GEOR");

(function() {

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = OpenLayers.i18n;

    /**
     * Handler which decides whether to show the DL Form.
     */
    var handleDlForm = function(email, b) {
        if (GEOR.config.DOWNLOAD_FORM) {
            // show popup with form
            GEOR.dlform.show({
                // callback once submitted :
                callback: function() {
                    GEOR.layerstree.extract(email, b);
                }
            });
        } else {
            GEOR.layerstree.extract(email, b);
        }
    };

    /**
     * Handler for extract all checked layers button.
     */
    var extractHandler = function(b) {
        var emailRegexp = /^([\w\-\'\-]+)(\.[\w-\'\-]+)*@([\w\-]+\.){1,5}([A-Za-z]){2,4}$/;
        var email = GEOR.data.email || (localStorage && localStorage.getItem('email'));
        if (emailRegexp.test(email)) {
            handleDlForm(email, b);
        } else {
            // prompt for valid email and process the extraction using a callback
            Ext.Msg.prompt(tr('Email'), tr('Enter a valid email address: '), function(btn, text){
                if (btn == 'ok') {
                    if (emailRegexp.test(text)) {
                        if (localStorage) {
                            localStorage.setItem('email', text);
                        }
                        GEOR.data.email = text;
                        handleDlForm(text, b);
                    } else {
                        GEOR.util.errorDialog({
                            msg: tr("The email address is not valid. " +
                                    "Stopping extraction.")
                        });
                    }
                }
            });
        }
    };

    /**
     * Handler for extract button.
     */
    var extractBtnHandler = function() {
        if (GEOR.layerstree.getSelectedLayersCount() > 0) {
            extractHandler(this);
        } else {
            var dialog = Ext.Msg.confirm(tr("Not any layer in the cart"),
            tr("You did not select any layer for extracting. Extract all ?"), function(btn, text){
                if (btn == 'yes'){
                    GEOR.layerstree.selectAllLayers();
                    extractHandler(this);
                } else {
                    dialog.hide();
                }
            }, this);
        }
    };

    /* 
     * In IE, document.namespaces is not loaded when Ext.onReady is triggered, that causes 
     * an error in OpenLayers VML Loading. Need to use window.onload instead  
     * http://stackoverflow.com/questions/1081812/javascript-unspecified-error-in-open-layers
     */
    window.onload = function() {
        
        /*
         * Setting of OpenLayers global vars.
         */

        OpenLayers.Lang.setCode(GEOR.config.LANG);
        OpenLayers.Number.thousandsSeparator = " ";
        OpenLayers.ImgPath = 'resources/app/img/openlayers/';
        OpenLayers.DOTS_PER_INCH = GEOR.config.MAP_DOTS_PER_INCH;
        OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

        /*
         * Setting of proj4js global vars.
         */

        Proj4js.libPath = 'resources/lib/proj4js/lib/';

        /*
         * Setting of Ext global vars.
         */

        Ext.BLANK_IMAGE_URL = "resources/lib/externals/ext/resources/images/default/s.gif";
        Ext.apply(Ext.MessageBox.buttonText, {
            yes: tr("Yes"),
            no: tr("No"),
            ok: tr("OK"),
            cancel: tr("Cancel")
        });

        /*
         * Initialize the application.
         */

        GEOR.waiter.init();
        var map = GEOR.map.create();
        var vectorLayer = GEOR.map.createVectorLayer();

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

        vpItems.push({
            region: "center",
            layout: "border",
            id: 'layerconfig',
            title: tr("Extraction parameters applied by default to all " +
                      "cart layers"),
            iconCls: 'config-layers',
            defaults: {
                defaults: {
                    border: false
                },
                border: false
            },
            items: [
                GEOR.layeroptions.create(map, {
                    region: "north",
                    vectorLayer: vectorLayer,
                    height: 105
                }),
                GEOR.mappanel.create(map, {
                    region: "center"
                })
            ]
        }, {
            region: "west",
            border: false,
            width: 300,
            minWidth: 300,
            maxWidth: 500,
            split: true,
            collapseMode: 'mini',
            layout: "border",
            items: [{
                region: "north",
                html: [
                    tr("paneltext1"),
                    tr("paneltext2"),
                    tr("paneltext3")
                    ].join('<br/><br/>'),
                bodyCssClass: 'paneltext',
                height: 170,
                autoScroll: true,
                title: tr("Extractor"),
                iconCls: 'home',
                collapsible: true,
                split: true
            }, {
                xtype: 'tabpanel',
                activeTab: 0,
                region: 'center',
                defaults: {
                    defaults: {
                        border: false
                    },
                    border: false
                },
                items: [{
                    layout: "fit",
                    title: tr("Configuration"),
                    items: GEOR.layerstree.create()
                }, {
                    layout:"border",
                    title: tr("Recenter"),
                    defaults: {
                        border: false
                    },
                    items: [
                        Ext.apply({
                            height: 150,
                            region: 'north'
                        }, GEOR.referentials.create(map)),
                        {
                            xtype: 'container',
                            autoEl: 'div',
                            cls: 'x-panel-body x-panel-body-noborder',
                            html: ' ',
                            region: 'center'
                        }
                    ]
                }]
            }, {
                layout: "form",
                region: 'south',
                footerCssClass: "geor-primary",
                buttons: [{
                    xtype: 'button',
                    id: "geor-btn-extract-id",
                    text: tr("Extract the selected layers"),
                    iconCls: "geor-btn-extract",
                    handler: extractBtnHandler
                }]
            }]
        });

        // the viewport
        new Ext.Viewport({
            layout: "border",
            items: vpItems,
            listeners: {
                "afterrender": function() {
                    if (!GEOR.config.SPLASH_SCREEN) {
                        return;
                    }
                    GEOR.util.infoDialog({
                        title: tr("Use limits"),
                        msg: GEOR.config.SPLASH_SCREEN
                    });
                }
            }
        });

        var saveLayerOptions = function() {
            GEOR.layerstree.saveExportOptions(GEOR.layeroptions.getOptions());
        };

        GEOR.layerstree.events.on({
            "beforelayerchange": saveLayerOptions,
            "beforeextract": saveLayerOptions,
            "layerchange": function(options, global) {
                GEOR.layeroptions.setOptions(options, global);
                var layerOptionsPanel = Ext.getCmp('layerconfig');
                if (global) {
                    layerOptionsPanel.setTitle(
                        tr("Extraction parameters applied by default to all " +
                           "cart layers"),
                        'config-layers'
                    );
                } else {
                    var isRaster = (options.owsType == 'WCS');
                    layerOptionsPanel.setTitle(
                        (isRaster ?
                            tr("Extraction parameters only for the " +
                               "NAME layer (raster)",
                               {'NAME': options.layerName}
                            ) :
                            tr("Extraction parameters only for the " +
                               "NAME layer (vector)",
                               {'NAME': options.layerName}
                            )
                        ),
                        isRaster ?
                        'raster-layer' : 'vector-layer'
                    );
                }
            }
        });
        GEOR.referentials.events.on({
            "recenter": GEOR.layeroptions.setBbox
        });

        // we monitor ajax requests only when the layer tree has finished loading
        // so that the user is not bothered with useless popups
        GEOR.layerstree.init(map, vectorLayer, GEOR.ajaxglobal.init);
    };
})();
