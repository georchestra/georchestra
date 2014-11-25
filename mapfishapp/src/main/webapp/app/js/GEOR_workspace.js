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
            form = formPanel.getForm();
        if (form.findField('title').getValue().length < 3) {
            GEOR.util.errorDialog({
                msg: tr("The context title is mandatory")
            });
            return;
        }
        var wmc_string = GEOR.wmc.write({
            "title": form.findField('title').getValue(),
            "abstract": form.findField('abstract').getValue()
        });
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
                        "/wmc.import"
                    ].join(''),
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: OpenLayers.Util.getParameterString({
                        "wmc_string": wmc_string,
                        "wmc_url": wmc_url,
                        "viewer_url": GEOR.util.getValidURI("?wmc="+encodeURIComponent(wmc_url))
                    }),
                    success: function(resp) {
                        if (resp.responseText) {
                            var r =  /<uuid>(.{36})<\/uuid>/.exec(resp.responseText);
                            if (r && r[1]) {
                                // wait a while for the MD to be made available:
                                new Ext.util.DelayedTask(function(){
                                    window.open(GEOR.config.GEONETWORK_BASE_URL+"/?uuid="+r[1]);
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
     * Method: saveBtnHandler
     * Handler for the button triggering the WMC save dialog
     */
    var saveBtnHandler = function() {
        var formPanel = this.findParentByType('form'), 
            form = formPanel.getForm();
        GEOR.waiter.show();
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/wmc/",
            data: GEOR.wmc.write({
                "title": form.findField('title').getValue(),
                "abstract": form.findField('abstract').getValue()
            }),
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
        if (GEOR.config.ROLES.indexOf("ROLE_SV_EDITOR") >= 0 ||
            GEOR.config.ROLES.indexOf("ROLE_SV_ADMIN") >= 0 ) {
            btns.push({
                text: tr("Save to metadata"),
                minWidth: 100,
                iconCls: 'geor-btn-download',
                itemId: 'save-md',
                handler: saveMDBtnHandler,
                formBind: true
            });
        }
        btns.push({
            text: tr("Save"),
            minWidth: 100,
            iconCls: 'geor-btn-download',
            itemId: 'save',
            handler: saveBtnHandler,
            formBind: true
        });

        var popup = new Ext.Window({
            title: tr("Context saving"),
            layout: 'fit',
            modal: false,
            constrainHeader: true,
            animateTarget: GEOR.config.ANIMATE_WINDOWS && this.el,
            width: 400,
            height: 180,
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
                    //allowBlank: false,
                    //blankText: tr("The file is required."),
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    listeners: {
                        "keypress": function(f, e) {
                            // transfer focus on Print button on ENTER
                            if (e.getKey() === e.ENTER) {
                                popup.items.get(0).getFooterToolbar().getComponent('save').focus();
                            }
                        }
                    }
                }, {
                    xtype: 'textarea',
                    name: 'abstract',
                    width: 280,
                    fieldLabel: tr("Abstract"),
                    enableKeyEvents: true,
                    selectOnFocus: true,
                    listeners: {
                        "keypress": function(f, e) {
                            // transfer focus on Print button on ENTER
                            if (e.getKey() === e.ENTER) {
                                popup.items.get(0).getFooterToolbar().getComponent('save').focus();
                            }
                        }
                    }
                }],
                buttons: btns
            }]
        });
        popup.show();
    };

    /**
     * Method: editOSM
     * Creates handlers for OSM edition
     */
    var editOSM = function(options) {
        var round = GEOR.util.round;
        return function() {
            var url, bounds = map.getExtent();
            bounds.transform(
                map.getProjectionObject(),
                new OpenLayers.Projection("EPSG:4326")
            );
            if (options.protocol === 'lbrt') {
                url = options.base + OpenLayers.Util.getParameterString({
                    left: round(bounds.left, 5),
                    bottom: round(bounds.bottom, 5),
                    right: round(bounds.right, 5),
                    top: round(bounds.top, 5)
                });
                frames[0].location.href = url;
            } else if (options.protocol === 'llz') {
                var c = bounds.getCenterLonLat();
                /*
                Zoom level determined based on the idea that, for OSM:
                    maxResolution: 156543 -> zoom level 0
                    numZoomLevels: 19
                */
                var zoom = Math.round((Math.log(156543) - Math.log(map.getResolution()))/Math.log(2));
                url = options.base + OpenLayers.Util.getParameterString({
                    lon: round(c.lon, 5),
                    lat: round(c.lat, 5),
                    zoom: Math.min(19, zoom - 1)
                });
                window.open(url);
            }
        };
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
                    },'-', {
                        text: tr("Edit in OSM"),
                        iconCls: "geor-edit-osm",
                        plugins: [{
                            ptype: 'menuqtips'
                        }],
                        menu: [{
                            text: tr("with JOSM"),
                            qtip: tr("JOSM must be started with the remote control option"),
                            handler: editOSM.call(this, {
                                base: 'http://127.0.0.1:8111/load_and_zoom?',
                                protocol: 'lbrt'
                            })
                        },{
                            text: tr("with Potlatch"),
                            qtip: tr("Recommended scale is 1:10.000"),
                            handler: editOSM.call(this, {
                                base: 'http://www.openstreetmap.org/edit?editor=potlatch&',
                                protocol: 'llz'
                            })
                        },{
                            text: tr("with Potlatch2"),
                            qtip: tr("Recommended scale is 1:10.000"),
                            handler: editOSM.call(this, {
                                base: 'http://www.openstreetmap.org/edit?editor=potlatch2&',
                                protocol: 'llz'
                            })
                        },{
                            text: tr("with Walking Papers"),
                            qtip: tr("Recommended scale is 1:10.000"),
                            handler: editOSM.call(this, {
                                base: 'http://walking-papers.org/?',
                                protocol: 'llz'
                            })
                        }]
                    }]
                })
            };
        }
    };
})();
