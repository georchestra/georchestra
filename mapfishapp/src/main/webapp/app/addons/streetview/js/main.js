Ext.namespace("GEOR.Addons");

GEOR.Addons.Streetview = Ext.extend(GEOR.Addons.Base, {
    _layer: null,
    _fovLayer: null,
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
        var style = {
            cursor: "pointer",
            fillOpacity: 0.3, 
            fillColor: "#fff",
            strokeColor: "#67b6f9",
            strokeOpacity: 0.8,
            strokeWidth: 3,
            pointRadius: 3
        };
        this._layer = new OpenLayers.Layer.Vector("__georchestra_streetview", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": Ext.applyIf({}, style),
                "select": Ext.applyIf({}, style)
            }),
            eventListeners: {
                "featuremodified": function(o) {
                    this._updateView();
                    this._updateFOV();
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
        this._fovLayer = new OpenLayers.Layer.Vector("__georchestra_streetview_fov", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": Ext.applyIf({
                    strokeColor: "#f00", 
                    strokeWidth: 2
                }, style),
                "select": Ext.applyIf({}, style)
            })
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
            y: 120,
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
                id: 'plus',
                qtip: OpenLayers.i18n("Fullscreen in a new window"),
                handler: function(){
                    var pos = this._getMarkerPosition();
                    if (!pos.x) {
                        return;
                    }
                    // cbll = position
                    // cbp = window size, bearing, tilt, zoom, pitch
                    window.open("http://maps.google.com/maps?q=&layer=c&cbll="+pos.y+","+pos.x+"&cbp=12,"+pos.angle+",0,0,0");
                },
                scope: this
            },{
                id: 'pin',
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
                    this.item && this.item.setChecked(false);
                    this.components && this.components.toggle(false);
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

        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                enableToggle: true,
                tooltip: this.getTooltip(record),
                iconCls: "addon-streetview",
                listeners: {
                    "toggle": this._onCheckchange,
                    scope: this
                }
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item =  new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-streetview",
                checked: false,
                listeners: {
                    "checkchange": this._onCheckchange,
                    scope: this
                }
            });
        }
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
        // remove previous FOV:
        this._fovLayer.removeAllFeatures();
        // will update both features in both layers:
        this._createRegularPolygonFromPoint(f.geometry.getCentroid(), f.geometry.angle);
        // select again the new feature:
        this._modifyControl.selectFeature(this._layer.features[0]);
    },

    /**
     * Method: _getMarkerPosition
     * 
     */
    _getMarkerPosition: function() {
        var feature = this._layer.features[0];
        if (!feature) {
            return;
        }
        var geom = feature.geometry.getCentroid();
        geom.transform(this.map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
        return {
            x: geom.x,
            y: geom.y,
            angle: feature.geometry.angle
        };
    },

    /**
     * Method: _updateFOV
     * updates the second feature in the layer (the one which represents the FOV)
     */
    _updateFOV: function() {
        if (!this._fovLayer.features[0]) {
            return;
        }
        var origin = this._layer.features[0].geometry.getCentroid();
        var angle = this._layer.features[0].geometry.angle;
        var geom = this._fovLayer.features[0].geometry;
        if ((origin.x != geom.x) &&  (origin.y != geom.y)) {
            geom.move(origin.x - geom.x, origin.y - geom.y);
            geom.x = origin.x;
            geom.y = origin.y;
        }
        var new_angle = 90 - angle;
        geom.rotate(new_angle - geom.angle, origin); 
        geom.angle = new_angle;
        this._fovLayer.redraw();
    },

    /**
     * Method: _createFOV
     * Creates the FOV geometry (= a portion of a circle)
     * 
     * Parameters:
     * origin - {<OpenLayers.Geometry.Point>}
     * radius - {Float} the FOV radius value in meters
     * angle - {Float} the view heading in degrees
     * fov - {Integer} the field of view in degrees (around the "angle" direction)
     *
     * Returns: 
     * {geom} The FOV geometry
     */
    _createFOV: function(origin, radius, angle, fov) {
        var points = [];
        points.push(origin.clone());
        for(var i=0; i<=10; ++i) {
            var x = origin.x + (radius * Math.sin((angle+fov/2-i*(fov/10))/180*Math.PI));
            var y = origin.y + (radius * Math.cos((angle+fov/2-i*(fov/10))/180*Math.PI));
            points.push(new OpenLayers.Geometry.Point(x, y));
        }
        var ring = new OpenLayers.Geometry.LinearRing(points);
        var geom = new OpenLayers.Geometry.Polygon([ring]);
        geom.x = origin.x;
        geom.y = origin.y;
        geom.angle = 90; // the first direction we show is North (counted counterclockwise from the positive x axis)
        return geom;
    },

    /**
     * Method: _createRegularPolygonFromPoint
     * 
     */
    _createRegularPolygonFromPoint: function(geometry, angle) {
        // radius small enough to make the circular geom disappear
        // behind the central (=drag) point handler:
        var radius = 1 * this.map.getResolution();
        var geom = OpenLayers.Geometry.Polygon.createRegularPolygon(geometry, radius, 20, 0);
        // we're assuming an initial heading of 0 degrees (North):
        geom.angle = angle || 0;
        geom.x = geometry.getCentroid().x;
        geom.y = geometry.getCentroid().y;
        // draw FOV
        var geom2 = this._createFOV(geometry, 15*radius, angle, this.options.initial_fov);
        // get back fov inner angle: (fov angle is measured counterclockwise form the positive x axis)
        geom2.angle = 90 - geom.angle;
        this._layer.addFeatures([new OpenLayers.Feature.Vector(geom)]);
        this._fovLayer.addFeatures([new OpenLayers.Feature.Vector(geom2)]);
    },

    /**
     * Method: _updateView
     * 
     */
    _updateView: function() {
        var pos = this._getMarkerPosition();
        if (!pos.x) {
            return;
        }
        this._window.show();
        this._window.items.get(0).update('<img src="'+this._buildURL(pos.x, pos.y, pos.angle)+'" />');
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
            this.map.addLayer(this._fovLayer);
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
            this._layer.removeAllFeatures(); // reset marker position
            this._fovLayer.removeAllFeatures();
            this.map.removeLayer(this._layer);
            this.map.removeLayer(this._fovLayer);
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
        this._layer.map && this.map.removeLayer(this._layer);
        this._fovLayer.map && this.map.removeLayer(this._fovLayer);
        this._window.destroy();
        this._window = null;
        this._layer = null;
        this._fovLayer = null;
        
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});