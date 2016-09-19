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
     * Method: saveMDBtnHandler
     * Handler for the button triggering the WMC save to catalog
     */
    var saveMDBtnHandler = function() {
        var formPanel = this.findParentByType('form'), 
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
        GEOR.waiter.show();
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/wmc/",
            data: GEOR.wmc.write({
                title: ""
            }),
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
    };

    /**
     * Method: cancelBtnHandler
     * Handler for the cancel button
     */
    var cancelBtnHandler = function() {
        this.findParentByType('form').ownerCt.close();
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
        var transferFocus = function(f, e) {
            // transfer focus on button on ENTER
            if (e.getKey() === e.ENTER) {
                popup.items.get(0).getFooterToolbar().getComponent('save').focus();
            }
        };
        var popup = new Ext.Window({
            title: tr("Context saving"),
            layout: 'fit',
            modal: false,
            constrainHeader: true,
            animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el,
            width: 400,
            height: 210,
            closeAction: 'close',
            plain: true,
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
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    listeners: {
                        "keypress": transferFocus
                    }
                }, {
                    xtype: 'textfield',
                    name: 'keywords',
                    width: 280,
                    emptyText: tr("comma separated keywords"),
                    fieldLabel: tr("Keywords"),
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    listeners: {
                        "keypress": transferFocus
                    }
                }, {
                    xtype: 'textarea',
                    name: 'abstract',
                    width: 280,
                    fieldLabel: tr("Abstract"),
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    listeners: {
                        "keypress": transferFocus
                    }
                }],
                buttons: btns
            }]
        });
        popup.show();
    };

    /**
     * Method: shareLink
     * Creates handlers for map link sharing
     */
    var shareLink = function(options) {
        return function() {
            GEOR.waiter.show();
            OpenLayers.Request.POST({
                url: GEOR.config.PATHNAME + "/ws/wmc/",
                data: GEOR.wmc.write({
                    title: ""
                }),
                success: function(response) {
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
            return {
                text: tr("Workspace"),
                menu: new Ext.menu.Menu({
                    defaultAlign: "tr-br",
                    // does not work as expected, at least with FF3 ... (ExtJS bug ?)
                    // top right corner of menu should be aligned with bottom right corner of its parent
                    items: [{
                        text: tr("Save the map context"),
                        iconCls: "geor-save-map",
                        handler: saveWMC
                    },{
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
                    }]
                })
            };
        }
    };
})();
