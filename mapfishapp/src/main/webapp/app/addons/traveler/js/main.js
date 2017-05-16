Ext.namespace("GEOR.Addons");

GEOR.Addons.Traveler = Ext.extend(GEOR.Addons.Base, {
    /**
     * Method: map
     * get current map     
     */
    map: this.map,

    /**
     * Method: loader
     * create loader     
     */
    loader: function() {
        return new Ext.LoadMask(Ext.getBody(), {
            msg: OpenLayers.i18n("Traveler.isochrone.msg.loader")
        })
    },

    /**
     * Method: isoControl
     * isochrone control to draw isochrone start points     
     */
    isoControl: null,

    /**
     * Method: isoControl
     * isochrone control to draw route start points     
     */
    routeControl: null,
    /**
     * Method: featureRouteArray
     * create object containing way point
     */
    featureArray: new Object(),

    /**
     * Method: featureIsoArray
     * create object containing start point
     */
    isoStart: new Object(),

    /**
     * Method: isoResult
     * create object to link result to line
     */
    isoResult: new Object(),

    /**
     * Method: isoLayer
     * start point layer     
     */
    isoLayer: null,

    /**
     * Method: isoResLayer
     * isochrones polygon layer  
     */
    isoResLayer: null,

    /**
     * Method: lastFieldUse
     * get last field use to create link between point and field id to destroy point when field is remove
     */
    lastFieldUse: null,

    /**
     * Method: isoWindow
     * get main tool window
     */
    isoWindow: null,

    /**
     * Method: routeWindow
     * get main tool window
     */
    routeWindow: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
    	var tr = OpenLayers.i18n;
        // do not load addon if user is not connect to map viewer
        if (GEOR.config.ANONYMOUS) {
            return Ext.Msg.alert(
            		tr("traveler.title.noright"),
                tr("traveler.msg.noright"));
        }
    	
        var addon = this;
        if (!addon.map) { // init map addon if not exist
            addon.map = GeoExt.MapPanel.guess().map;
        }

        var items = [
            new Ext.menu.CheckItem( // manage isochrone tool
                new Ext.Action({
                    text: tr("isochrone"),
                    qtip: tr("isochrone"),
                    map: this.map,
                    group: "_travel",
                    iconCls: "addon-isochrone-icon",
                    id: "iso_tool",
                    listeners: {
                        "click": function(box) {
                            if (!Ext.getCmp("iso_win")) {
                                // create items
                                addon.isoLayer = GEOR.Addons.Traveler.isochrone.layer(addon);
                                addon.isoResLayer = GEOR.Addons.Traveler.isochrone.resultLayer(addon);
                                var isoMode = GEOR.Addons.Traveler.isochrone.mode();
                                var isoExclud = GEOR.Addons.Traveler.isochrone.exclusions();
                                var banCb = GEOR.Addons.Traveler.isochrone.ban(addon);
                                addon.isoControl = GEOR.Addons.Traveler.isochrone.drawControl(addon, banCb.id);
                                var isoBan = GEOR.Addons.Traveler.isochrone.banField(addon, banCb);
                                var isoFielSet = GEOR.Addons.Traveler.isochrone.pointFset(addon, isoBan);
                                var isoTime = GEOR.Addons.Traveler.isochrone.time(addon);
                                // create window to finalize isochrone init
                                var isoWin = GEOR.Addons.Traveler.isochrone.window(isoMode, isoFielSet, isoExclud, addon, isoTime);
                                isoWin.show();
                            } else {
                                var window = Ext.getCmp("iso_win");
                                addon.isoWindow = window;
                                if (!window.isVisible()) {
                                    window.show();
                                } else {
                                	window.destroy();
                                	addon.isoWindow = null;
                                }
                            }
                        }
                    }
                })
            ), new Ext.menu.CheckItem( // manage route tool
                new Ext.Action({
                    text: tr("route"),
                    qtip: tr("route"),
                    map: this.map,
                    group: "_travel",
                    iconCls: "addon-route-icon",
                    listeners: {
                        "click": function(box) {
                            if (!addon.routeWindow) {
                                addon.routePoints = GEOR.Addons.Traveler.route.pointsLayer(addon); // start points layer
                                addon.routeLines = GEOR.Addons.Traveler.route.linesLayer(addon); // result layer
                                addon.routeControl = GEOR.Addons.Traveler.route.routeControl(addon);
                                addon.routeWindow = GEOR.Addons.Traveler.route.routeWindow(addon);
                                addon.routeWindow.show();
                            } else {
                                if (!addon.routeWindow.isVisible()) {
                                    addon.routeWindow.show();
                                } else {
                                    addon.routeWindow.destroy();
                                    addon.routeWindow = null;
                                }
                            }

                        }
                    }
                })
            )
        ];
        this.items = items;
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        var addon = this;
        var map = GeoExt.MapPanel.guess().map;
        if (addon.routeControl) {
            addon.routeControl.destroy();
        }
        if (addon.isoControl) {
            addon.isoControl.destroy();
        }
        if (addon.routeWindow) {
            addon.routeWindow.destroy();
            addon.routeWindow = null;
        }
        if (addon.isoWindow) {
            addon.isoWindow.destroy();
            addon.isoWindow = null;
        }
        if (addon.isoLayer) {
            addon.isoLayer.destroy();
        }
        if (addon.isoResLayer) {
            addon.isoResLayer.destroy();
        }
        if (map && map.getLayersByName("route_lines").length > 0) {
            map.getLayersByName("route_lines")[0].destroy();
        }
        if (map && map.getLayersByName("route_points").length > 0) {
            map.getLayersByName("route_points")[0].destroy();
        }
        addon.featureArray = null;
        addon.isoStart = null;
        addon.loader = null;
        addon.isoResult = null;
        addon.lastFieldUse = null;
        GEOR.Addons.Base.prototype.destroy.call(addon);
    }
});