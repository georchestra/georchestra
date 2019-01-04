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
 * @include OpenLayers/Format/JSON.js
 * @include GEOR_waiter.js
 * @include GEOR_config.js
 * @include GEOR_localStorage.js
 * @include GEOR_addons.js
 * @include GEOR_util.js
 */

// Note that we keep a dependency to GeoExt Popup here because several addons are using it:
/*
 * @include GeoExt/widgets/Popup.js
 */

Ext.namespace("GEOR");

GEOR.tools = (function() {
    /*
     * Private
     */

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
     * Property: map
     * {GeoExt.MapPanel} The map panel object
     */
    var mapPanel;

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
     * Property: store
     * {Ext.data.JsonStore}
     */
    var store;

    /**
     * Property: addonsCache
     * {Object} Hash storing references to loaded addons
     */
    var addonsCache = {};

    /**
     * Property: state
     * {Object} Hash storing the current state of each addons (loaded or not)
     */
    var state;

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
    };

    /**
     * Method: fetchAndLoadTools
     * Fetch the tools and load them in the menu.
     *
     * Parameters:
     * records - {Array} an array of tool records
     * silent - {Boolean} silence mode (no help info) - defaults to false
     */
    var fetchAndLoadTools = function(records, silent) {
        var newState = {};
        silent = silent || false;
        store.each(function(r) {
            newState[r.id] = (records.indexOf(r) > -1);
        });
        // compute diff with previous selection state:
        var incoming = [], outgoing = [];
        Ext.iterate(newState, function(k, v) {
            if (newState[k] === true && state[k] === false) {
                incoming.push(store.getById(k));
            }
            if (newState[k] === false && state[k] === true) {
                outgoing.push(store.getById(k));
            }
        });
        state = newState;
        // remove unwanted addons:
        Ext.each(outgoing, function(r) {
            var addon = addonsCache[r.id],
                item = addon.item;
            if (item) {
                menu.remove(item, true);
            }
            Ext.each(addon.items, function(item) {
                menu.remove(item, true);
            });
            addon.destroy(); // will be used by the addon to remove its component
            delete addonsCache[r.id];
            r.set("_loaded", false);
        });
        // load new addons:
        var count = incoming.length;
        GEOR.waiter.show(count);
        Ext.each(incoming, function(r) {
            var addonName = r.get("name"),
                addonPath = GEOR.config.PATHNAME + "/ws/addons/" +
                    addonName.toLowerCase() + "/",
                failure = function() {
                    count -= 1;
                    // if an addon fails to load properly, update state accordingly
                    state[r.id] = false;
                    // unselect node corresponding to record in dataview:
                    dataview && dataview.deselect(r);
                    r.set("_loaded", false);
                    // warn user:
                    GEOR.util.errorDialog({
                        msg: tr("Could not load addon ADDONNAME",
                            {'ADDONNAME': addonName}
                        )
                    });
                    if (count == 0 && !silent) {
                        GEOR.helper.msg(tr("Tools"),
                            tr("New tools are now available"));
                    }
                };
            // get corresponding manifest.json
            OpenLayers.Request.GET({
                url: addonPath + "manifest.json",
                success: function(response) {
                    if (!response || !response.responseText) {
                        failure.call(this);
                        return;
                    }
                    count -= 1;
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
                    var urlparams = GEOR.util.splitURL(window.location.href);
                    if (urlparams.params.hasOwnProperty("DEBUG") && o.debugjs && o.debugjs.length) {
                        o.js = o.debugjs;
                    }
                    // load JS
                    if (o.js && o.js.length) {
                        Ext.each(o.js, function(f) {
                            js.push(addonPath + f);
                        });
                        Ext.Loader.load(js, function() {
                            if (!GEOR.Addons[addonName]) {
                                alert("GEOR.Addons."+addonName+" namespace should be defined !");
                                return;
                            }
                            // init addon
                            var addon = new GEOR.Addons[addonName](mapPanel, Ext.apply({},
                                r.get("options") || {},
                                o.default_options || {}
                            ));
                            addonsCache[r.id] = addon;
                            // we're passing the record to the init method
                            // so that the addon has access to the administrator's strings
                            addon.init(r);
                            r.set("_loaded", true);
                            // keep the original order (the one defined by the admin)
                            var records = store.query("_loaded", true);
                            for (var i=0,l=records.getCount(); i<l;i++) {
                                if (records.get(i) === r) {
                                    break;
                                }
                            }
                            if (addon.item) {
                                // "one addon hidden in the tools menu"
                                // handle menuitem qtip:
                                addon.item.on('afterrender', GEOR.util.registerTip);
                                // here we know it should be inserted at position i from the beginning
                                menu.insert(i + 2, addon.item);
                            } else if (addon.items) {
                                // the addon publishes an array of menu items
                                Ext.each(addon.items, function(item) {
                                    item.on('afterrender', GEOR.util.registerTip);
                                    menu.insert(i + 2, item);
                                });
                            } else {
                                // if there is no addon.item, it means the addon takes care of
                                // inserting his own component into the viewport
                                // and calling doLayout on the parent component.
                            }
                        }, this, true); // preserveOrder set to true is important to be able to extend classes
                    }
                    // inform user:
                    if (count == 0 && !silent) {
                        GEOR.helper.msg(tr("Tools"),
                            tr("New tools are now available"));
                    }
                },
                failure: failure
            });
        });
    };


    /**
     * Method: loadBtnHandler
     * Handler for the button triggering the tools loading
     */
    var loadBtnHandler = function() {
        win && win.hide();
        fetchAndLoadTools(dataview.getSelectedRecords(), false);
    };


    /**
     * Method: storeToolsSelection
     * Utility method to make the selection persist in localStorage
     */
    var storeToolsSelection = function() {
        var ids = Ext.pluck(dataview.getSelectedRecords(), "id");
        GEOR.ls.set("default_tools", ids.join(','));
    };


    /**
     * Method: onCbxCheckChange
     * Callback on remember checkbox check event
     *
     * Parameters:
     * cbx - {Ext.form.Checkbox}
     * checked - {Boolean}
     */
    var onCbxCheckChange = function(cbx, checked) {
        if (checked) {
            storeToolsSelection();
        } else {
            // clear localstorage item
            GEOR.ls.remove("default_tools");
        }
    };


    /**
     * Method: showToolSelection
     * Creates/shows the tools selection window
     */
    var showToolSelection = function() {
        var target = (GEOR.config.ANIMATE_WINDOWS) ?
            this.el : undefined;

        if (win) {
            win.show();
            return;
        }

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
                        '</td><td width="50" style="text-align:center;" ext:qtip="'+tr("Click to select or deselect the tool")+'">',
                            '<img src="{[this.thumb(values)]}" class="thumb" />',
                        '</td></tr></table>',
                    '</div>',
                '</tpl>',
            {
                compiled: true,
                disableFormats: true,
                tr: function(v, key) {
                    var lang = OpenLayers.Lang.getCode();
                    return v[key].hasOwnProperty(lang) ? v[key][lang] : v[key]["en"];
                },
                thumb: function(v) {
                    var base = GEOR.config.PATHNAME+"/ws/addons/"+v.name.toLowerCase()+"/";
                    return base + ((v.thumbnail) ? v.thumbnail : "img/thumbnail.png");
                }
            }),
            listeners: {
                "afterrender": {
                    // restore tools selection
                    fn: function(dv) {
                        store.each(function(r) {
                            if (r.get("_loaded") === true) {
                                dv.select(r, true, true);
                            }
                        });
                    },
                    single: true
                },
                "selectionchange": function(dv) {
                    var selectedRecords = dv.getSelectedRecords(),
                        fbar = win.getFooterToolbar(),
                        btn = fbar.getComponent('load'),
                        cbx = fbar.getComponent('cbx'),
                        nb = selectedRecords.length;
                    if (cbx.getValue() === true) {
                        storeToolsSelection();
                    }
                    btn.setText(
                        ((nb == 0) ? tr("No tool") : '') +
                        ((nb == 1) ? selectedRecords.length + " " + tr("tool") : '') +
                        ((nb > 1) ? selectedRecords.length + " " + tr("tools") : '')
                    );
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
            closable: false, // waiting for the TODO below to be done...
            closeAction: 'hide',
            plain: true,
            buttonAlign: 'left',
            fbar: [{
                xtype: 'checkbox',
                itemId: 'cbx',
                boxLabel: tr("remember the selection"),
                checked: GEOR.ls.get("default_tools") !== null,
                disabled: !GEOR.ls.available,
                listeners: {
                    "check": onCbxCheckChange
                }
            }, '->',
            // TODO: add a "cancel" button restoring previous tool selection state.
            // + close button (top right corner) has same effect
            {
                text: tr("OK"),
                itemId: 'load',
                minWidth: 90,
                //iconCls: 'geor-load-tools', // TODO: change icon
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
                    html: tr("Available tools:")
                }
            }, {
                flex: 1,
                layout: 'fit',
                items: [dataview]
            }]
        });
        win.show();
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: init
         * Initialize this module
         *
         * Parameters:
         * mp - {GeoExt.MapPanel} The application's map panel.
         *
         */
        init: function(mp) {
            tr = OpenLayers.i18n;
            mapPanel = mp;
            map = mp.map;
            // filter out restricted addons:
            var allowedAddons = [];
            Ext.each(GEOR.config.ADDONS, function(addon) {
                var okRoles = addon.roles;
                if (okRoles === undefined || okRoles.length === 0) {
                    // no restriction specified
                    allowedAddons.push(addon);
                } else {
                    // check user has at least one of the required roles
                    for (var i = 0; i < okRoles.length; i++) {
                        if (GEOR.config.ROLES.indexOf(okRoles[i]) >= 0) {
                            allowedAddons.push(addon);
                            break;
                        }
                    }
                }
            });
            store = new Ext.data.JsonStore({
                fields: ["id", "name", "title", "thumbnail", "description", "group", "options", {
                    name: "_loaded", defaultValue: false, type: "boolean"
                }, {
                    name: "preloaded", defaultValue: false, type: "boolean"
                }],
                data: allowedAddons
            });
            state = {};
            store.each(function(r) {
                state[r.id] = false;
            });
        },

        /**
         * APIMethod: create
         *
         * Returns:
         * {Ext.Button} the toolbar button holding the menu
         */
        create: function() {
            menu = new Ext.menu.Menu({
                defaultAlign: "tr-br",
                items: [
                    {
                        text: OpenLayers.i18n("Manage tools"),
                        hideOnClick: false,
                        iconCls: "add",
                        handler: showToolSelection
                    }, '-'
                ]
            });
            return new Ext.Button({
                text: OpenLayers.i18n("Tools"),
                menu: menu
            });
        },

        /**
         * APIMethod: restore
         * Restores the tool selection stored in localStorage
         *
         */
        restore: function() {
            var o = GEOR.util.splitURL(window.location.href);
            if (o.params.hasOwnProperty("ADDONS")) {
                fetchAndLoadTools(store.queryBy(function(r) {
                    return (o.params.ADDONS.indexOf(r.id) > -1);
                }), true);
                return;
            }
            if (!GEOR.ls.available) {
                return;
            }
            var str = GEOR.ls.get("default_tools");
            if (!str) {
                fetchAndLoadTools(store.queryBy(function(r) {
                    return (r.get('preloaded') == true);
                }), true);
            }
            else {
                var ids = str.split(',');
                fetchAndLoadTools(store.queryBy(function(r) {
                    return (ids.indexOf(r.id) > -1);
                }), true);
            }
        },

        /**
         * APIMethod: getAddonsState
         * Retrieve informations about addons
         */
        getAddonsState: function() {
            return state;
        },

        /**
         * APIMethod: getAddon
         * @param: addonId - Unique identifier of addon (id in config.json)
         */
        getAddon: function(addonId) {
            return addonsCache[addonId];
        }
    };
})();
