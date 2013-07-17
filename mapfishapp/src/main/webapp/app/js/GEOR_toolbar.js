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
 * @include GEOR_tools.js
 * @include GEOR_localStorage.js
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
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

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
            tooltip: tr("zoom to global extent of the map"),
            iconCls: "zoomfull"
        }));

        // default control is a fake, so that Navigation control
        // is used by default to pan.
        ctrl = new OpenLayers.Control();
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "pan",
            tooltip: tr("pan"),
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
            tooltip: tr("zoom in"),
            toggleGroup: "map",
            allowDepress: false
        }));

        ctrl = new OpenLayers.Control.ZoomBox({
          out: true
        });
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "zoomout",
            tooltip: tr("zoom out"),
            toggleGroup: "map",
            allowDepress: false
        }));

        items.push("-");

        ctrl = new OpenLayers.Control.NavigationHistory();
        map.addControl(ctrl);
        items.push(new GeoExt.Action({
            control: ctrl.previous,
            iconCls: "back",
            tooltip: tr("back to previous zoom"),
            disabled: true
        }));
        items.push(new GeoExt.Action({
            control: ctrl.next,
            iconCls: "next",
            tooltip: tr("go to next zoom"),
            disabled: true
        }));

        items.push("-");
        
        items.push({
            xtype: 'button',
            iconCls: 'geor-btn-info',
            allowDepress: true,
            enableToggle: true,
            toggleGroup: 'map',
            tooltip: tr("Information on objects of all superposed layers pointed"),
            listeners: {
                "toggle": function(btn, pressed) {
                    GEOR.getfeatureinfo.toggleX(pressed);
                }
            }
        });

        // create a legend panel, it is used both for displaying
        // the legend in the interface and for inclusion if PDFs
        // created by the print module
        var legendPanel = new GeoExt.LegendPanel({
            layerStore: layerStore,
            border: false,
            defaults: {
                labelCls: 'bold-text',
                showTitle: true,
                baseParams: {
                    FORMAT: 'image/png'
                }
            },
            autoScroll: true
        });

        if (GEOR.print) {
            items.push("-");
            GEOR.print.setLegend(legendPanel);
            items.push(GEOR.print.getAction());
        }

        items.push('->');

        if (GEOR.header === false) {
            // insert a login or logout link in the toolbar
            var login_html = '<div style="margin-right:1em;font:11px tahoma,verdana,helvetica;"><a href="' + GEOR.config.LOGIN_URL +
                '" style="text-decoration:none;" onclick="return GEOR.toolbar.confirmLogin()">'+tr("Login")+'</a></div>';
            if(!GEOR.config.ANONYMOUS) {
                login_html = '<div style="margin-right:1em;font:11px tahoma,verdana,helvetica;">'+GEOR.config.USERNAME + '&nbsp;<a href="' + GEOR.config.LOGOUT_URL +
                    '" style="text-decoration:none;">'+tr("Logout")+'</a></div>';
            }
            items.push(Ext.DomHelper.append(Ext.getBody(), login_html));
            items.push('-');
        }

        items.push({
            text: tr("Help"),
            tooltip: tr("Show help"),
            handler: function() {
                if(Ext.isIE) {
                    window.open(GEOR.config.HELP_URL);
                } else {
                    window.open(GEOR.config.HELP_URL, tr("Help"), "menubar=no,status=no,scrollbars=yes");
                }
            }
        });

        items.push("-");
        items.push(GEOR.tools.create());

        items.push('-');
        items.push({
            text: tr("Legend"),
            tooltip: tr("Show legend"),
            enableToggle: true,
            handler: function(btn) {
                if (!legendWin) {
                    legendWin = new Ext.Window({
                        width: 340,
                        bodyStyle: 'padding: 5px',
                        constrainHeader: true,
                        title: tr("Legend"),
                        border: false,
                        animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el,
                        layout: 'fit',
                        bodyCssClass: 'white-bg',
                        items: [ legendPanel ],
                        autoHeight: false,
                        height: 350,
                        closeAction: 'hide',
                        listeners: {
                            "hide": function() {
                                btn.toggle(false);
                            },
                            "show": function() {
                                btn.toggle(true);
                            }
                        },
                        autoScroll: true
                    });
                }
                if (!legendWin.isVisible()) {
                    legendWin.show();
                } else {
                    legendWin.hide();
                }
            }
        });

        items.push("-");
        items.push(GEOR.workspace.create(map));

        // the toolbar items are added afterwards the creation of the toolbar
        // because we need a reference to the toolbar when creating the
        // legend item
        tbar.add.apply(tbar, items);
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
            tr = OpenLayers.i18n;

            return createTbar(layerStore);
        },

        /**
         * Method: confirmLogin
         * Displays a confirm dialog before leaving the app for CAS login
         */
        confirmLogin: function() {
            return GEOR.ls.available ? true :
                confirm(tr("Leave this page ? You will lose the current cartographic context."));
        }
    };


})();
