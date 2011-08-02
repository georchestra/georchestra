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
 * @include OpenLayers/Control/ZoomToMaxExtent.js
 * @include OpenLayers/Control/ZoomBox.js
 * @include OpenLayers/Control/DragPan.js
 * @include OpenLayers/Control/NavigationHistory.js
 * @include OpenLayers/Control/Measure.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Polygon.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Rule.js
 * @include GeoExt/widgets/Action.js
 * @include GeoExt/widgets/LegendPanel.js
 * @include GeoExt/widgets/WMSLegend.js
 * @include GEOR_workspace.js
 * @include GEOR_print.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.toolbar = (function() {
    /*
     * Private
     */

    /**
     * Property: legendWin
     * {Ext.Tip} The tip containing the legend panel.
     */
    var legendWin = null;

    /**
     * Method: createMeasureControl.
     * Create a measure control.
     *
     * Parameters:
     * handlerType - {OpenLayers.Handler.Path} or {OpenLayers.Handler.Polygon}
     *     The handler the measure control will use, depending whether
     *     measuring distances or areas.
     *
     * Returns:
     * {OpenLayers.Control.Measure} The control.
     */
    var createMeasureControl = function(handlerType) {
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(null, {
                rules: [new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 4,
                            graphicName: "square",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 1,
                            strokeOpacity: 1,
                            strokeColor: "#333333"
                        },
                        "Line": {
                            strokeWidth: 3,
                            strokeOpacity: 1,
                            strokeColor: "#666666",
                            strokeDashstyle: "dash"
                        },
                        "Polygon": {
                            strokeWidth: 2,
                            strokeOpacity: 1,
                            strokeColor: "#666666",
                            fillColor: "white",
                            fillOpacity: 0.3
                        }
                    }
                })]
            })
        });

        var makeString = function(event) {
            var metricUnit = event.units;
            var dim = event.order == 2 ? '<sup>2</sup>' : '';
            return event.measure.toFixed(2) + " " + metricUnit + dim;
        };

        var measureToolTip;
        var cleanup = function() {
            if (measureToolTip) {
                measureToolTip.destroy();
            }
        };
        var measureControl = new OpenLayers.Control.Measure(handlerType, {
            persist: true,
            handlerOptions: {
                layerOptions: {styleMap: styleMap}
            }
        });
        measureControl.events.on({
            measurepartial: function(event) {
                cleanup();
                measureToolTip = new Ext.ToolTip({
                    html: makeString(event),
                    autoHide: false,
                    closable: true,
                    draggable: false,
                    mouseOffset: [0, 0],
                    showDelay: 1,
                    listeners: {hide: cleanup}
                });
                if (event.measure > 0) {
                    var px = measureControl.handler.lastUp;
                    measureToolTip.targetXY = [px.x, px.y];
                    measureToolTip.show();
                }
            },
            measure: function(event) {
                cleanup();
                measureToolTip = new Ext.ToolTip({
                    target: Ext.getBody(),
                    html: makeString(event),
                    autoHide: false,
                    closable: true,
                    draggable: false,
                    mouseOffset: [0, 0],
                    showDelay: 1,
                    listeners: {
                        hide: function() {
                            measureControl.cancel();
                            cleanup();
                        }
                    }
                });
            },
            deactivate: cleanup
        });
        return measureControl;
    };
    
    /**
     * Method: createTbar
     * Create the toolbar.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application's layer store.
     *
     * Returns:
     * {Ext.Toolbar} The toolbar.
     */
    var createTbar = function(layerStore) {
        var map = layerStore.map, tbar = new Ext.Toolbar(), ctrl, items = [];

        ctrl = new OpenLayers.Control.ZoomToMaxExtent();
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            tooltip: "zoom sur l'étendue globale de la carte",
            iconCls: "zoomfull"
        }));

        items.push("-");

        // default control is a fake, so that Navigation control
        // is used by default to pan.
        ctrl = new OpenLayers.Control();
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "pan",
            tooltip: "glisser - déplacer la carte",
            toggleGroup: "map",
            allowDepress: false,
            pressed: true
        }));

        ctrl = new OpenLayers.Control.ZoomBox({
          out: false
        });
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "zoomin",
            tooltip: "zoom en avant",
            toggleGroup: "map",
            allowDepress: false
        }));

        items.push(new GeoExt.Action({
            control: createMeasureControl(OpenLayers.Handler.Path),
            map: map,
            toggleGroup: "map",
            tooltip: "mesurer une distance",
            iconCls: "measure_path",
            allowDepress: false
        }));

        items.push(new GeoExt.Action({
            control: createMeasureControl(OpenLayers.Handler.Polygon),
            map: map,
            toggleGroup: "map",
            tooltip: "mesurer une surface",
            iconCls: "measure_area",
            allowDepress: false
        }));

        items.push("-");

        ctrl = new OpenLayers.Control.NavigationHistory();
        map.addControl(ctrl);
        items.push(new GeoExt.Action({
            control: ctrl.previous,
            iconCls: "back",
            tooltip: "revenir à la précédente emprise",
            disabled: true
        }));
        items.push(new GeoExt.Action({
            control: ctrl.next,
            iconCls: "next",
            tooltip: "aller à l'emprise suivante",
            disabled: true
        }));

        items.push("-");

        // create a legend panel, it is used both for displaying
        // the legend in the interface and for inclusion if PDFs
        // created by the print module
        var legendPanel = new GeoExt.LegendPanel({
            layerStore: layerStore,
            border: false,
            defaults: {
                labelCls: 'bold-text',
                showTitle: true
            },
            autoScroll: true
        });
        GEOR.print.setLegend(legendPanel);

        items.push(GEOR.print.getAction());

        items.push('->');

        // insert a login or logout link in the toolbar
        var login_html = '<div style="margin-right:1em;font:11px tahoma,verdana,helvetica;"><a href="' + GEOR.config.LOGIN_URL +
            '" style="text-decoration:none;" onclick="return GEOR.toolbar.confirmLogin()">Connexion</a></div>';
        if(!GEOR.config.ANONYMOUS) {
            login_html = '<div style="margin-right:1em;font:11px tahoma,verdana,helvetica;">'+GEOR.config.USERNAME + '&nbsp;<a href="' + GEOR.config.LOGOUT_URL +
                '" style="text-decoration:none;">déconnexion</a></div>';
        }
        items.push(Ext.DomHelper.append(Ext.getBody(), login_html));

        items.push('-');

        items.push({
            text: "Aide",
            tooltip: "Afficher l'aide",
            handler: function() {
                if(Ext.isIE) {
                    window.open(GEOR.config.HELP_URL);
                } else {
                    window.open(GEOR.config.HELP_URL, "Aide de l'extracteur", "menubar=no,status=no,scrollbars=yes");
                }
            }
        });

        items.push('-');

        items.push({
            text: "Légende",
            tooltip: "Afficher la légende",
            handler: function(btn) {
                if (!legendWin) {
                    legendWin = new Ext.Window({
                        width: 340,
                        bodyStyle: 'padding: 5px',
                        title: "Légende",
                        border: false,
                        layout: 'fit',
                        bodyCssClass: 'white-bg',
                        items: [ legendPanel ],
                        autoHeight: false,
                        height: 350,
                        closeAction: 'hide',
                        autoScroll: true
                    });
                }
                if (!legendWin.isVisible()) {
                    var mapPanel = tbar.ownerCt;
                    legendWin.show();
                    legendWin.getEl().alignTo(
                        mapPanel.body, "tr-tr", [-10, 10]
                    );
                }
            }
        });

        items.push(GEOR.workspace.create());

        // this is a bit unusual to set the buttons in the
        // toolbar afterwards the creation of the toolbar,
        // but it works, and we need a reference to the
        // toolbar when creating the legend item
        tbar.buttons = items;
        return tbar;
    };



    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Return the toolbar config.
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application's layer store.
         *
         * Returns:
         * {Ext.Toolbar} The toolbar.
         */
        create: function(layerStore) {
            Ext.QuickTips.init();
            return createTbar(layerStore);
        },
        
        /**
         * Method: confirmLogin
         * Displays a confirm dialog before leaving the app for CAS login
         */
        confirmLogin: function() {
            return confirm("Vous allez quitter cette page et perdre le contexte cartographique courant");
            // ou : "Pour vous connecter, nous vous redirigeons vers une autre page web. Vous risquez de perdre le contexte cartographique courant. Vous pouvez le sauvegarder en annulant cette opération, et en cliquant sur Espace de travail > Sauvegarder la carte" ?
        }
    };


})();
