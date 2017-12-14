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
 * @include Ext.ux.RowExpander.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include OpenLayers/Projection.js
 * @include GEOR_wmc.js
 * @include GEOR_wmcbrowser.js
 * @include GEOR_config.js
 * @include GEOR_waiter.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.workspace = (function() {
    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map object
     */
    var map = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: contextManagerWindow
     * {Ext.Window}
     */
    var contextManagerWindow = null;

    /**
     * Method: saveMDBtnHandler
     * Handler for the button triggering the WMC save to catalog
     */
    var saveMDBtnHandler = function() {
        var formPanel = this.findParentByType('form'),
            form = formPanel.getForm(),
            md = buildContextMD(formPanel);
        if (form.findField('title').getValue().length < 3) {
            GEOR.util.errorDialog({
                msg: tr("The context title is mandatory")
            });
            return;
        }
        var wmc_string = GEOR.wmc.write(md);
        GEOR.waiter.show();
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/wmc/",
            data: wmc_string,
            success: function(response) {
                formPanel.ownerCt.close();
                var o = Ext.decode(response.responseText),
                    wmc_url = GEOR.util.getValidURI(o.filepath);
                GEOR.waiter.show();
                OpenLayers.Request.POST({
                    url: [ 
                        GEOR.config.GEONETWORK_BASE_URL,
                        "/srv/",
                        GEOR.util.ISO639[GEOR.config.LANG],
                        "/map.import"
                    ].join(''),
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: OpenLayers.Util.getParameterString({
                        "group_id": this.group_id,
                        "map_string": wmc_string,
                        "map_url": wmc_url,
                        "viewer_url": GEOR.util.getValidURI("?wmc="+encodeURIComponent(wmc_url))
                    }),
                    success: function(resp) {
                        if (resp.responseText) {
                            var r =  /<uuid>(.{36})<\/uuid>/.exec(resp.responseText);
                            if (r && r[1]) {
                                // wait a while for the MD to be made available:
                                new Ext.util.DelayedTask(function(){
                                    window.open(GEOR.config.METADATA_VIEW_BASE_URL+r[1]);
                                }).delay(1000);
                                return;
                            }
                        }
                        GEOR.util.errorDialog({
                            msg: tr("There was an error creating the metadata.")
                        });
                    },
                    scope: this
                });
            },
            scope: this
        });
    };

    /**
     * Method: buildContextMD
     * Extracts WMC title, keywords, abstract from formPanel
     */
    var buildContextMD = function(formPanel) {
        var form = formPanel.getForm(),
            md = {
                "title": form.findField('title').getValue(),
                "abstract": form.findField('abstract').getValue()
            },
            keywords = form.findField('keywords').getValue();
        if (keywords) {
            md.keywords = keywords.trim().split(/, */);
        }
        return md;
    };

    /**
     * Method: saveBtnHandler
     * Handler for the button triggering the WMC save dialog
     */
    var saveBtnHandler = function() {
        var formPanel = this.findParentByType('form'), 
            md = buildContextMD(formPanel);
        GEOR.waiter.show();
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/wmc/",
            data: GEOR.wmc.write(md),
            success: function(response) {
                formPanel.ownerCt.close();
                var o = Ext.decode(response.responseText);
                window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
            },
            scope: this
        });
    };

    /**
     * Method: permalink
     * Handler to display a permalink based on on-the-fly WMC generation
     */
    var permalink = function() {

        var cfg = getWindowCfg({
            title: tr("Permalink creation")
        });
        cfg.items[0].buttons = [{
            text: tr("Cancel"),
            handler: cancelBtnHandler
        }, {
            text: tr("Permalink"),
            minWidth: 100,
            //iconCls: 'geor-btn-download',
            //itemId: 'save',
            handler: function() {
                var formPanel = this.findParentByType('form'), 
                    md = buildContextMD(formPanel);
                GEOR.waiter.show();
                OpenLayers.Request.POST({
                    url: GEOR.config.PATHNAME + "/ws/wmc/",
                    data: GEOR.wmc.write(md),
                    success: function(response) {
                        var o = Ext.decode(response.responseText),
                            params = OpenLayers.Util.getParameters(),
                            id =  /^.+(\w{32}).wmc$/.exec(o.filepath)[1];
                        // we have to unset these params since they have precedence 
                        // over the WMC:
                        Ext.each(["bbox", "wmc", "lon", "lat", "radius"], function(item) {
                            delete params[item];
                        });
                        var qs = OpenLayers.Util.getParameterString(params);
                        if (qs) {
                            qs = "?"+qs;
                        }
                        var url = [
                            window.location.protocol, '//', window.location.host,
                            GEOR.config.PATHNAME, '/map/', id, qs
                        ].join('');
                        popup.close();
                        GEOR.util.urlDialog({
                            title: tr("Permalink"),
                            width: 450,
                            msg: [
                                tr("Share your map with this URL: "),
                                '<br /><a href="'+url+'">'+url+'</a>'
                            ].join('')
                        });
                    },
                    scope: this
                });
            }
        }];
        var popup = new Ext.Window(cfg).show();
    };

    /**
     * Method: cancelBtnHandler
     * Handler for the cancel button
     */
    var cancelBtnHandler = function() {
        this.findParentByType('form').ownerCt.close();
    };

    /**
     * Method: getWindowCfg
     * Returns a config for the create WMC dialog.
     */
    var getWindowCfg = function(options) {
        return Ext.apply({
            layout: 'fit',
            modal: false,
            constrainHeader: true,
            width: 400,
            height: 210,
            closeAction: 'close',
            listeners: {
                "show": function() {
                    // focus first field on show
                    var field = this.items.get(0).getForm().findField('title');
                    field.focus('', 50);
                }
            },
            items: [{
                xtype: 'form',
                bodyStyle: 'padding:5px',
                labelWidth: 80,
                labelSeparator: tr("labelSeparator"),
                monitorValid: true,
                buttonAlign: 'right',
                items: [{
                    xtype: 'textfield',
                    name: 'title',
                    width: 280,
                    fieldLabel: tr("Title"),
                    selectOnFocus: true
                }, {
                    xtype: 'textfield',
                    name: 'keywords',
                    width: 280,
                    emptyText: tr("comma separated keywords"),
                    fieldLabel: tr("Keywords"),
                    selectOnFocus: true
                }, {
                    xtype: 'textarea',
                    name: 'abstract',
                    width: 280,
                    fieldLabel: tr("Abstract"),
                    selectOnFocus: true
                }]
            }]
        }, options);
    };

    /**
     * Method: saveWMC
     * Triggers the save dialog.
     */
    var saveWMC = function() {
        var btns = [{
            text: tr("Cancel"),
            handler: cancelBtnHandler
        }];
        if (GEOR.config.ROLES.indexOf("ROLE_GN_EDITOR") >= 0 || 
            GEOR.config.ROLES.indexOf("ROLE_GN_REVIEWER") >= 0 ||
            GEOR.config.ROLES.indexOf("ROLE_GN_ADMIN") >= 0 ) {

            var menu = new Ext.menu.Menu({
                showSeparator: false,
                items: []
            }),
            isolang = GEOR.util.ISO639[GEOR.config.LANG],
            store = new Ext.data.Store({
                autoLoad: true,
                url: [
                    GEOR.config.GEONETWORK_BASE_URL,
                    "/srv/",
                    isolang,
                    "/xml.info?type=groups&profile=Editor"
                ].join(''),
                reader: new Ext.data.XmlReader({
                    record: 'group',
                    idPath: '@id'
                }, [
                    {name: 'name', mapping: '/label/'+isolang},
                    {name: 'description'}
                ]),
                listeners: {
                    "load": function(s, records) {
                        Ext.each(records, function(r) {
                            menu.addItem({
                                text: tr("in group") + " <b>" + r.get("name") + "</b>",
                                group_id: r.id, // a convenient way to pass the group_id arg ...
                                handler: saveMDBtnHandler
                            });
                        });
                    },
                    "loadexception": function() {
                        alert("Oops, there was an error with the catalog");
                    }
                }
            });
            btns.push({
                text: tr("Save to metadata"),
                minWidth: 100,
                iconCls: 'geor-btn-download',
                itemId: 'save-md',
                //menuAlign: "tr-br",
                menu: menu
            });
        }
        btns.push({
            text: tr("Save"),
            minWidth: 100,
            iconCls: 'geor-btn-download',
            itemId: 'save',
            handler: saveBtnHandler
        });

        var cfg = getWindowCfg({
            title: tr("Context saving"),
            animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el
        });
        cfg.items[0].buttons = btns;
        var popup = new Ext.Window(cfg).show();
    };

    /**
     * Method: shareLink
     * Creates handlers for map link sharing
     */
    var shareLink = function(options) {
        return function() {
            var cfg = getWindowCfg({
                title: tr("Map sharing")
            });
            cfg.items[0].buttons = [{
                text: tr("Cancel"),
                handler: cancelBtnHandler
            }, {
                text: tr("Share"),
                minWidth: 100,
                //iconCls: 'geor-btn-download',
                //itemId: 'save',
                handler: function() {
                    var formPanel = this.findParentByType('form'), 
                        md = buildContextMD(formPanel);
                    GEOR.waiter.show();
                    OpenLayers.Request.POST({
                        url: GEOR.config.PATHNAME + "/ws/wmc/",
                        data: GEOR.wmc.write(md),
                        success: function(response) {
                            popup.close();
                            var o = Ext.decode(response.responseText),
                                id =  /^.+(\w{32}).wmc$/.exec(o.filepath)[1];
                            var url = new Ext.XTemplate(options.url).apply({
                                "context_url": encodeURIComponent(GEOR.util.getValidURI(o.filepath)),
                                "map_url": GEOR.util.getValidURI('map/' + id),
                                "id": id
                            });
                            window.open(url);
                        },
                        scope: this
                    });
                }
            }];
            var popup = new Ext.Window(cfg).show();
        }
    };

    /**
     * Method: getShareMenu
     * Creates the sub menu for map sharing
     */
    var getShareMenu = function() {
        var menu = [], cfg;
        Ext.each(GEOR.config.SEND_MAP_TO, function(item) {
            cfg = {
                text: tr(item.name),
                handler: shareLink.call(this, {
                    url: item.url
                })
            };
            if (item.qtip) {
                cfg.qtip = tr(item.qtip);
            }
            if (item.iconCls) {
                cfg.iconCls = item.iconCls;
            }
            menu.push(cfg);
        });
        return menu;
    };

    /**
     * Method: loadCtx
     * 
     */
    var loadCtx = function(record) {
        GEOR.waiter.show();
        OpenLayers.Request.GET({
            url: GEOR.config.PATHNAME + "/ws/wmc/geodoc" + record.get("hash") + ".wmc",
            success: function(response) {
                try {
                    GEOR.wmc.read(response.responseXML, true, true);
                } catch(e) {}
            }
        });
    };

    /**
     * Method: manageContexts
     * Triggers the "manage contexts" dialog window.
     */
    var manageContexts = function() {
        if (contextManagerWindow && contextManagerWindow.isVisible()) {
            return;
        }
        var expander = new Ext.ux.grid.RowExpander({
            tpl: new Ext.XTemplate(
                '<br/>',
                '<p>{abstract}</p>',
                '<br/>',
                '<p><b>', tr("Created:"),'</b> {created_at:date("Y-m-d H:i:s")}</p>', 
                '<p><b>', tr("Last accessed:"),'</b> {last_access:date("Y-m-d H:i:s")}</p>',
                '<p><b>', tr("Access count:"),'</b> {access_count}</p>',
                '<br/>',
                '<p><b>', tr("Permalink:"),'</b> <a href="', GEOR.config.PATHNAME ,'/map/{hash}">', GEOR.config.PATHNAME ,'/map/{hash}</a></p><br>'
            )
        });
        var arrayRenderer = function(value, p, r) {
            if (value && value[0]) {
                return value.join(", ");
            }
            return "";
        };
        var sm = new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: {
                "selectionchange": function(sm) {
                    var g = sm.grid;
                    if (sm.getCount()) {
                        g.viewButton.enable();
                        g.downloadButton.enable();
                        g.removeButton.enable();
                    } else {
                        g.viewButton.disable();
                        g.downloadButton.disable();
                        g.removeButton.disable();
                    }
                }
            }
        });
        contextManagerWindow = new Ext.Window({
            title: tr("My contexts"),
            layout: 'fit',
            modal: false,
            constrainHeader: true,
            animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el,
            width: 600,
            height: 400,
            closeAction: "close",
            border: false,
            items: [{
                xtype: "grid",
                plugins: expander,
                store: {
                    xtype: "jsonstore",
                    autoLoad: true,
                    url: GEOR.config.PATHNAME + "/ws/wmcs.json",
                    idProperty: "hash",
                    fields: [
                        "hash",
                        {name: "created_at", type: "date"},
                        {name: "last_access", type: "date"},
                        {name: "access_count", type: "int"},
                        "title",
                        "abstract",
                        "keywords",
                        "srs",
                        "bbox"
                    ]
                },
                viewConfig: {
                    forceFit:true
                },
                columnLines: true,
                listeners: {
                    "rowdblclick": function(grid, rowIdx) {
                        // load map context on double click:
                        loadCtx(grid.getStore().getAt(rowIdx));
                    }
                },
                cm: new Ext.grid.ColumnModel({
                    defaults: {
                        sortable: true
                    },
                    columns: [
                        expander,
                        {header: tr("Title"), dataIndex: "title", hideable: false},
                        {header: tr("Keywords"), dataIndex: "keywords", renderer: arrayRenderer},
                        {header: tr("Created"), dataIndex: "created_at", renderer: Ext.util.Format.dateRenderer('Y-m-d'), width: 45},
                        {header: tr("Accessed"), dataIndex: "last_access", renderer: Ext.util.Format.dateRenderer('Y-m-d'), width: 45, hidden: true},
                        {header: tr("Count"), dataIndex: "access_count", width: 35, hidden: true},
                    ]
                }),
                sm: sm,
                tbar:[{
                    text: tr("View"),
                    tooltip: tr("View the selected context"),
                    iconCls: 'geor-load-map',
                    ref: '../viewButton',
                    disabled: true,
                    handler: function() {
                        loadCtx(sm.getSelected());
                    }
                }, {
                    text: tr("Download"),
                    tooltip: tr("Download the selected context"),
                    iconCls: 'geor-btn-download',
                    ref: '../downloadButton',
                    disabled: true,
                    handler: function() {
                        var r = sm.getSelected();
                        window.location.href = GEOR.config.PATHNAME + "/ws/wmc/geodoc" + r.get("hash") + ".wmc";
                    }
                }, "->", {
                    text: tr("Delete"),
                    tooltip: tr("Delete the selected context"),
                    iconCls: 'btn-removeall',
                    ref: '../removeButton',
                    disabled: true,
                    handler: function() {
                        var r = sm.getSelected();
                        GEOR.waiter.show();
                        OpenLayers.Request.DELETE({
                            url: GEOR.config.PATHNAME + "/ws/wmc/geodoc" + r.get("hash"),
                            success: function() {
                                sm.grid.getStore().remove(r);
                            },
                            failure: function(resp) {
                                var e = Ext.decode(resp.responseText);
                                GEOR.util.errorDialog({
                                    title: tr("Failed to delete context"),
                                    msg: [
                                        tr("Failed to delete context"), " ", r.get("hash"),
                                        ":<br/>", e.msg
                                    ].join("")
                                });
                            }
                        });
                    }
                }]
            }],
            buttons: [{
                text: tr("Reload"),
                handler: function() {
                    sm.grid.getStore().reload();
                }
            }, {
                text: tr("Close"),
                handler: function() {
                    contextManagerWindow.close();
                }
            }]
        });
        contextManagerWindow.show();
    };


    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Returns the workspace menu config.
         *
         * Parameters:
         * m {OpenLayers.Map}
         *
         * Returns:
         * {Object} The toolbar config item corresponding to the "workspace" menu.
         */
        create: function(m) {
            map = m;
            tr = OpenLayers.i18n;
            var items = [{
                text: tr("Save the map context"),
                iconCls: "geor-save-map",
                handler: saveWMC
            }, {
                text: tr("Load a map context"),
                iconCls: "geor-load-map",
                handler: GEOR.wmcbrowser.show
            }, '-', {
                text: tr("Get a permalink"),
                iconCls: "geor-permalink",
                handler: permalink
            }, {
                text: tr("Share this map"),
                iconCls: "geor-share",
                plugins: [{
                    ptype: 'menuqtips'
                }],
                menu: getShareMenu()
            }];
            // Display context manager to logged in users
            if (GEOR.config.USERNAME !== null) {
                items.splice(3, 0, {
                    text: tr("Manage my contexts"),
                    iconCls: "geor-manage-contexts",
                    handler: manageContexts
                });
                items.splice(4, 0, '-');
            }
            return {
                text: tr("Workspace"),
                menu: new Ext.menu.Menu({
                    defaultAlign: "tr-br",
                    // does not work as expected, at least with FF3 ... (ExtJS bug ?)
                    // top right corner of menu should be aligned with bottom right corner of its parent
                    items: items
                })
            };
        }
    };
})();
