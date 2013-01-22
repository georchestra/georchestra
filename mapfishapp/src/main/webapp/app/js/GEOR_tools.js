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
 * @include GeoExt/widgets/Popup.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.tools = (function() {
    /*
     * Private
     */

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: contextselected
         * Fires when a new tools selection is available
         */
        "selectionchanged"
    );

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr;

    /**
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map;

    /**
     * Property: popup
     * {GeoExt.Popup}
     */
    var popup;

    /**
     * Property: menu
     * {Ext.menu.Menu}
     */
    var menu;

    /**
     * Property: win
     * {Ext.Window}
     */
    var win;

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
    var createMeasureControl = function(handlerType, map) {
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

        var measureControl = new OpenLayers.Control.Measure(handlerType, {
            persist: true,
            handlerOptions: {
                layerOptions: {styleMap: styleMap}
            }
        });
        
        var showPopup = function(event) {
            popup && popup.destroy();
            popup = new GeoExt.Popup({
                map: map,
                title: tr("Measure"),
                bodyStyle: "padding:5px;",
                unpinnable: true,
                closeAction: 'close',
                location: map.getCenter(),
                tpl: new Ext.Template("{measure} {units}"),
                listeners: {
                    "close": function() {
                        measureControl.deactivate();
                        popup.destroy();
                        popup = null;
                    }
                }
            });
            var points = event.geometry.components;
            if (points[0] instanceof OpenLayers.Geometry.LinearRing) {
                points = points[0].components;
            }
            if (event.measure > 0) {
                popup.location = points[points.length-1].getBounds().getCenterLonLat();
                popup.position();
                popup.show();
                popup.update({
                    measure: event.order == 2 ?
                        (event.units == tr("m") ?
                            (event.measure/10000).toFixed(2) :
                            (event.measure*100).toFixed(2)) :
                        event.measure.toFixed(2),
                    units: event.order == 2 ? tr("hectares") : event.units
                });
            }
        }
        measureControl.events.on({
            measurepartial: showPopup,
            measure: showPopup
        });
        return measureControl;
    };


    /**
     * Method: addTools
     * Creates/shows the tools selection window
     */
    var addTools = function() {
        var target = (GEOR.config.ANIMATE_WINDOWS) ? 
            this.el : undefined;

        if (win) {
            win.show();
            return;
        }

        var store = new Ext.data.JsonStore({
            fields: ["name","title","thumbnail","description", "options"],
            data: GEOR.config.ADDONS.slice(0)
        });

        var dataview = new Ext.DataView({
            store: store,
            multiSelect: true,
            selectedClass: 'x-view-selected',
            simpleSelect: true,
            cls: 'x-list',
            overClass:'x-view-over',
            itemSelector: 'div.x-view-item',
            autoScroll: true,
            autoWidth: true,
            trackOver: true,
            autoHeight: true,
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="x-view-item">',
                        '<table><tr><td style="vertical-align:text-top;">',
                            '<p><b>{title}</b></p>',
                            '<p>{description}</p>',
                        '</td><td width="190" style="text-align:center;" ext:qtip="'+tr("Clic to select or deselect the tool")+'">',
                            '<img src="app/addons/{name}/{thumbnail}" class="thumb" onerror="this.src=\'app/img/broken.png\';"/>',
                        '</td></tr></table>',
                    '</div>',
                '</tpl>'
            ),
            listeners: {
                "click": function(dv) {
                    var selectedRecords = dv.getSelectedRecords();
                    observable.fireEvent("selectionchanged", selectedRecords);
                    win.getFooterToolbar().getComponent('load').setDisabled(selectedRecords.length === 0);
                }
            }
        });

        win = new Ext.Window({
            title: tr("Tools"),
            layout: 'vbox',
            layoutConfig: {
                align: 'stretch'
            },
            defaults: {
                border: false
            },
            modal: false,
            constrainHeader: true,
            animateTarget: target,
            width: 4 * 130 + 2 * 10 + 15, // 15 for scrollbar
            height: 450,
            closeAction: 'hide',
            plain: true,
            buttonAlign: 'left',
            fbar: [{
                xtype: 'checkbox',
                itemId: 'cbx',
                boxLabel: tr("remember the selection"),
                listeners: {
                    //"check": onCbxCheckChange
                }
            },'->', {
                text: tr("Close"),
                handler: function() {
                    win.hide();
                }
            }, {
                text: tr("Load"),
                disabled: true,
                itemId: 'load',
                minWidth: 90,
                iconCls: 'geor-load-tools',
                //handler: loadBtnHandler,
                listeners: {
                    "enable": function(btn) {
                        btn.focus();
                    }
                }
            }],
            items: [{
                xtype: 'box',
                height: 30,
                autoEl: {
                    tag: 'div',
                    cls: 'box-as-panel',
                    html: tr("Available tools:"),
                }
            }, dataview]
        });
        win.show(target);
    };

    /*
     * Public
     */
    return {
        /*
         * Observable object
         */
        events: observable,

        /**
         * APIMethod: create
         * Return the menu config
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application's layer store.
         *
         * Returns:
         * {Object}
         */
        create: function(layerStore) {
            tr = OpenLayers.i18n;
            map = layerStore.map;
            menu = new Ext.menu.Menu({
                defaultAlign: "tr-br",
                items: [
                    new Ext.menu.CheckItem(
                        new GeoExt.Action({
                            text: tr("distance measure"),
                            control: createMeasureControl(OpenLayers.Handler.Path, map),
                            map: map,
                            group: "measure",
                            iconCls: "measure_path"
                        })
                    ), new Ext.menu.CheckItem(
                        new GeoExt.Action({
                            text: tr("area measure"),
                            control: createMeasureControl(OpenLayers.Handler.Polygon, map),
                            map: map,
                            group: "measure",
                            iconCls: "measure_area"
                        })
                    ), '-', {
                        text: tr("Add more tools"),
                        iconCls: "add",
                        handler: addTools
                }]
            });

            return {
                text: tr("Tools"),
                menu: menu
            };
        }
        
    };
})();
