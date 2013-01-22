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
     * Property: dataview
     * {Ext.DataView}
     */
    var dataview;

    /**
     * Property: button
     * {Ext.Button} the button to which the menu is attached
     */
    var button;

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
     * Method: loadCssFiles
     * This method loads dynamically the css files passed in parameter
     *
     * Parameters:
     * files - {Array} the css files
     */
    var loadCssFiles = function(prefix, files) {
        Ext.each(files, function(file) {
            var css = document.createElement("link");
            css.setAttribute("rel", "stylesheet");
            css.setAttribute("type", "text/css");
            css.setAttribute("href", prefix + file);
            document.getElementsByTagName("head")[0].appendChild(css);
        });
    }


    /**
     * Method: fetchAndLoadTools
     * Fetch the tools and load them in the menu.
     *
     * Parameters:
     * records - {Array} an array of tool records
     */
    var fetchAndLoadTools = function(records) {
        win.hide();
        GEOR.waiter.show(records.length);
        Ext.each(records, function(r) {
            var addonName = r.get("name"),
                addonPath = "app/addons/" + addonName.toLowerCase() + "/";
            // get corresponding manifest.json 
            OpenLayers.Request.GET({
                url: addonPath + "manifest.json",
                success: function(response) {
                    // TODO: handle repeated files (eg: same addon with different parameters)
                    var js = [], 
                        o = (new OpenLayers.Format.JSON()).read(
                            response.responseText
                        );
                    // handle i18n
                    if (o.i18n) {
                        Ext.iterate(o.i18n, function(k, v) {
                            OpenLayers.Lang[k] = 
                                OpenLayers.Util.extend(OpenLayers.Lang[k], v);
                        });
                    }
                    // load CSS
                    if (o.css && o.css.length) {
                        loadCssFiles(addonPath, o.css);
                    }
                    // load JS
                    if (o.js && o.js.length) {
                        Ext.each(o.js, function(f) {
                            js.push(addonPath + f);
                        });
                        Ext.Loader.load(js, function() {
                            // init addon
                            if (GEOR.Addons[addonName]) {
                                var addon = new GEOR.Addons[addonName](map, Ext.apply({}, 
                                        r.get("options") || {}, 
                                        o.default_options || {}
                                    )),
                                    // we're passing the record to the init method
                                    // so that the addon has access to the administrator's strings
                                    tool = addon.init(r);
                                menu.addItem(tool); // TODO: add menu ? addMenuItem( config )
                            } else {
                                alert("GEOR.Addons."+addonName+" namespace should be defined !");
                            }
                        }, this, true);
                    }
                },
                failure: function() {
                    // TODO
                }
            });
        });
    };


    /**
     * Method: onDblclick
     * Callback on view node double clicked
     *
     * Parameters:
     * view - {Ext.DataView}
     * index - {Integer} not used internally
     * node - {HTMLElement}
     */
    var onDblclick = function(view, index, node) {
        var record = view.getRecord(node);
        if (record) {
            fetchAndLoadTools([record]);
        }
    };


    /**
     * Method: loadBtnHandler
     * Handler for the button triggering the tools loading
     */
    var loadBtnHandler = function() {
        fetchAndLoadTools(dataview.getSelectedRecords());
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
            fields: ["name", "title", "thumbnail", "description", "options"],
            data: GEOR.config.ADDONS.slice(0)
        });

        dataview = new Ext.DataView({
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
                        '<table><tr><td width="100%" style="vertical-align:text-top;">',
                            '<p><b>{[this.tr(values, "title")]}</b></p>',
                            '<p>{[this.tr(values, "description")]}</p>',
                        '</td><td width="50" style="text-align:center;" ext:qtip="'+tr("Clic to select or deselect the tool")+'">',
                            '<img src="app/addons/{[values.name.toLowerCase()]}/{thumbnail}" class="thumb" ',
                            'onerror="this.src=\'app/addons/{[values.name.toLowerCase()]}/img/thumbnail.png\';"/>',
                        '</td></tr></table>',
                    '</div>',
                '</tpl>', 
            {
                compiled: true,
                disableFormats: true,
                tr: function(v, key) {
                    return v[key][OpenLayers.Lang.getCode()];
                }
            }),
            listeners: {
                "selectionchange": function(dv) {
                    var selectedRecords = dv.getSelectedRecords();
                    observable.fireEvent("selectionchanged", selectedRecords);
                    win.getFooterToolbar().getComponent('load').setDisabled(
                        selectedRecords.length === 0
                    );
                },
                "dblclick": onDblclick
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
            fbar: [/*{
                xtype: 'checkbox',
                itemId: 'cbx',
                boxLabel: tr("remember the selection"),
                listeners: {
                    //"check": onCbxCheckChange
                }
            },*/'->', {
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
                handler: loadBtnHandler,
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
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application's layer store.
         *
         * Returns:
         * {Ext.Button} the toolbar button holding the menu
         */
        create: function(layerStore) {
            tr = OpenLayers.i18n;
            map = layerStore.map;
            menu = new Ext.menu.Menu({
                defaultAlign: "tr-br",
                items: [{ // TODO: put this at the bottom of the list (but how to insert items at a given position ?)
                    text: tr("Add more tools"),
                    hideOnClick: false,
                    iconCls: "add",
                    handler: addTools
                }, '-',
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
                ), '-']
            });

            button = new Ext.Button({
                text: tr("Tools"),
                plugins: [{
                    ptype: 'menuqtips' // TODO: plugin in its own file as a dependency
                }],
                menu: menu
            });
            return button;
        }
        
    };
})();