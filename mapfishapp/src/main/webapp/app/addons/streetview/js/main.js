Ext.namespace("GEOR.Addons");

GEOR.Addons.Streetview = function(map, options) {
    this.map = map;
    this.options = options;
};

GEOR.Addons.Streetview.prototype = {
    _layer: null,
    _modifyControl: null,
    _drawControl: null,
    _window: null,
    _helpShown: false,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        var style = { // TODO: let the admin configure this style
            graphicWidth: 10,
            graphicHeight: 10,
            graphicOpacity: 1,
            graphicZIndex: 10000,
            cursor: "pointer",
            fillOpacity: 0, 
            strokeColor: "#67b6f9",
            strokeOpacity: 0.8,
            strokeWidth: 3,
            pointRadius: 3
        };
        this._layer = new OpenLayers.Layer.Vector("addon_streetview_vectors", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": Ext.applyIf({}, style),
                "select": Ext.applyIf({}, style)
            }),
            eventListeners: {
                "featuremodified": function(o) {
                    this._updateView();
                    this._window.show();
                },
                "sketchcomplete": function(o) {
                    // Handling initial point feature.
                    // change point into regular polygon:
                    this._createRegularPolygonFromPoint(o.feature.geometry, 0);
                    // will cause a new "featureadded" event, 
                    // this time with a regular polygon instead of a point
                    return false;
                    // the point feature won't get added to the layer
                },
                "featureadded": function(o) {
                    // Handling regular polygon feature
                    this._drawControl.deactivate();
                    this._modifyControl.selectFeature(this._layer.features[0]);
                    this._updateView();
                    this._window.show();
                },
                scope: this
            }
        });
        this._drawControl = new OpenLayers.Control.DrawFeature(this._layer, OpenLayers.Handler.Point, {});
        this.map.addControl(this._drawControl);
        this._modifyControl = new OpenLayers.Control.StreetviewModifyFeature(this._layer, {
            standalone: true,
            mode: OpenLayers.Control.ModifyFeature.ROTATE | 
                OpenLayers.Control.ModifyFeature.DRAG
        });
        this.map.addControl(this._modifyControl);
        this._window = new Ext.Window({
            title: OpenLayers.i18n("StreetView"),
            closable: true,
            x: 0,
            y: 0,
            minHeight: 200,
            minWidth: 200,
            boxMaxHeight: 640,
            boxMaxWidth: 640,
            closeAction: "hide",
            resizable: true,
            layout: 'fit',
            bufferResize: 200,
            constrainHeader: true,
            tools:[{
                id:'pin',
                qtip: OpenLayers.i18n("Recenter on the marker"),
                handler: function(){
                    this.map.setCenter(this._layer.features[0].geometry.getBounds().getCenterLonLat());
                },
                scope: this
            }],
            items: [{
                width: this.options.initial_window_size,
                height: this.options.initial_window_size,
                xtype: 'box'
            }],
            listeners: {
                "hide": function() {
                    item.setChecked(false);
                },
                "resize": this._updateView,
                "show": {
                    fn: function() {
                        GEOR.helper.msg("StreetView", 
                            OpenLayers.i18n("Move the marker to update the view"));
                    },
                    single: true
                },
                scope: this
            }
        });
        var lang = OpenLayers.Lang.getCode(),
            item = new Ext.menu.CheckItem({
                text: record.get("title")[lang] || record.get("title")["en"],
                qtip: record.get("description")[lang] || record.get("description")["en"],
                iconCls: "addon-streetview",
                checked: false,
                listeners: {
                    "checkchange": this._onCheckchange,
                    scope: this
                }
            });
        this.item = item;
        return item;
    },

    /**
     * Method: _onMapZoomend
     * 
     */
    _onMapZoomend: function() {
        var f = this._layer.features[0];
        if (!f) {
            return;
        }
        // unselect
        if (this._modifyControl.feature) {
            this._modifyControl.unselectFeature(f);
        }
        // keep feature geometry apparent diameter
        this._layer.removeAllFeatures();
        this._createRegularPolygonFromPoint(f.geometry.getCentroid(), f.geometry.angle);
        // select again the new feature:
        this._modifyControl.selectFeature(this._layer.features[0]);
    },

    /**
     * Method: _createRegularPolygonFromPoint
     * 
     */
    _createRegularPolygonFromPoint: function(geometry, angle) {
        // radius for 20 px diameter icon size:
        var radius = 10 * this.map.getResolution();
        var geom = OpenLayers.Geometry.Polygon.createRegularPolygon(geometry, radius, 20, 0);
        // we're assuming an initial heading of 0 degrees (North):
        geom.angle = angle || 0;
        this._layer.addFeatures([new OpenLayers.Feature.Vector(geom)]);
    },

    /**
     * Method: _updateView
     * 
     */
    _updateView: function() {
        var feature = this._layer.features[0];
        if (!feature) {
            return;
        }
        var geom = feature.geometry.getCentroid();
        geom.transform(this.map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
        this._window.show();
        this._window.items.get(0).update('<img src="'+this._buildURL(geom.x, geom.y, feature.geometry.angle)+'" />');
    },

    /**
     * Method: _buildURL
     * 
     */
    _buildURL: function(lon, lat, angle) {
        var base = "http://maps.googleapis.com/maps/api/streetview?",
            o = this.options,
            cmp = this._window,
            p = 1e6;
        if (angle < 0) {
            angle += 360;
        }
        return base + OpenLayers.Util.getParameterString({
            key: o.api_key,
            size: cmp.getInnerWidth() + "x" + cmp.getInnerHeight(),
            location: Math.round(lat*p)/p + "," + Math.round(lon*p)/p,
            fov: o.initial_fov,
            heading: Math.round(angle),
            pitch: o.initial_pitch
        });
    },

    /**
     * Method: onCheckchange
     * Callback on checkbox state changed
     */
    _onCheckchange: function(item, checked) {
        if (checked) {
            this.map.addLayer(this._layer);
            this._modifyControl.activate();
            if (!this._layer.features.length) {
                this._drawControl.activate();
            } else {
                // update feature geom size, and select again feature with:
                this._onMapZoomend();
                this._window.show();
            }
            if (!this._helpShown) {
                this._helpShown = true;
                GEOR.helper.msg("StreetView", 
                    OpenLayers.i18n("Click on the map to get a glimpse of the surroundings"));
            }
            this.map.events.on({
                "zoomend": this._onMapZoomend,
                scope: this
            });
        } else {
            this._window.hide();
            if (OpenLayers.Util.indexOf(this.map.controls, this._drawControl) > -1) {
                this._drawControl.deactivate();
            }
            this._modifyControl.deactivate();
            this.map.removeLayer(this._layer);
            this.map.events.un({
                "zoomend": this._onMapZoomend,
                scope: this
            });
        }
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.map.events.un({
            "zoomend": this._onMapZoomend,
            scope: this
        });
        this._drawControl && this._drawControl.destroy();
        this._drawControl = null;
        this._modifyControl && this._modifyControl.destroy();
        this._modifyControl = null;
        this.map.removeLayer(this._layer);
        this._window.destroy();
        this._window = null;
        this._layer = null;
        this.map = null;
    }
};