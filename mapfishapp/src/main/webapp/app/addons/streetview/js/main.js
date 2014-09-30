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
        var style = {
            externalGraphic: GEOR.config.PATHNAME + "/app/addons/streetview/img/dir_north.png",
            graphicWidth: 16,
            graphicHeight: 16,
            graphicOpacity: 1,
            graphicZIndex: 10000,
            cursor: "pointer"
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
                scope: this
            }
        });
        this._drawControl = new OpenLayers.Control.DrawFeature(this._layer, OpenLayers.Handler.Point, {
            eventListeners: {
                "featureadded": function(o) {
                    this._drawControl.deactivate();
                    this.map.removeControl(this._drawControl);
                    this._modifyControl.selectFeature(o.feature);
                    this._updateView();
                    this._window.show();
                },
                scope: this
            }
        });
        this._modifyControl = new OpenLayers.Control.StreetviewModifyFeature(this._layer, {
            standalone: true,
            mode: OpenLayers.Control.ModifyFeature.ROTATE | 
                OpenLayers.Control.ModifyFeature.DRAG
        });
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
     * Method: _updateView
     * 
     */
    _updateView: function() {
        var feature = this._layer.features[0];
        if (!feature) { 
            return;
        }
        var geom = feature.geometry.clone();
        geom.transform(this.map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
        this._window.show();
        this._window.items.get(0).update('<img src="'+this._buildURL(geom.x, geom.y)+'" />');
    },

    /**
     * Method: _buildURL
     * 
     */
    _buildURL: function(lon, lat) {
        var base = "http://maps.googleapis.com/maps/api/streetview?",
            o = this.options,
            cmp = this._window;
        return base + OpenLayers.Util.getParameterString({
            key: o.api_key,
            size: cmp.getInnerWidth()+"x"+cmp.getInnerHeight(),
            location: lat+","+lon,
            fov: o.initial_fov,
            heading: 0,
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
            this.map.addControl(this._modifyControl);
            this._modifyControl.activate();
            if (!this._layer.features.length) {
                this.map.addControl(this._drawControl);
                this._drawControl.activate();
            } else {
                this._modifyControl.selectFeature(this._layer.features[0]);
                this._window.show();
            }
            if (!this._helpShown) {
                this._helpShown = true;
                GEOR.helper.msg("StreetView", 
                    OpenLayers.i18n("Click on the map to get a glimpse of the surroundings"));
            }
        } else {
            this._window.hide();
            if (OpenLayers.Util.indexOf(this.map.controls, this._drawControl) > -1) {
                this._drawControl.deactivate();
                this.map.removeControl(this._drawControl);
            }
            this._modifyControl.deactivate();
            this.map.removeControl(this._modifyControl);
            this.map.removeLayer(this._layer);
        }
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
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