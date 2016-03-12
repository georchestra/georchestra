Ext.namespace("GEOR.Addons");

GEOR.Addons.Measurements = Ext.extend(GEOR.Addons.Base, {

    window: null,
    toggleGroup: 'measurements',

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.layer = new OpenLayers.Layer.Vector('__georchestra_measurements', {
            styleMap: new OpenLayers.StyleMap(
                new OpenLayers.Style(null, {
                    rules: [
                        new OpenLayers.Rule({
                            symbolizer: OpenLayers.Control.DynamicMeasure.styles
                        })
                    ]
                })
            )
        });
        this.lengthAction =  new GeoExt.Action({
            control: new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Path, {
                maxSegments: null,
                persist: true,
                geodesic: true,
                drawingLayer: this.layer,
                keep: true
            }),
            map: this.map,
            // button options
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.distance.tooltip"),
            iconCls: 'measurements-length',
            text: OpenLayers.i18n("measurements.distance"),
            iconAlign: 'top'
        });
        this.areaAction =  new GeoExt.Action({
            control: new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Polygon, {
                maxSegments: null,
                persist: true,
                geodesic: true,
                drawingLayer: this.layer,
                keep: true
            }),
            map: this.map,
            // button options
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.area.tooltip"),
            iconCls: 'measurements-area',
            text: OpenLayers.i18n("measurements.area"),
            iconAlign: 'top',
            scope: this
        });
        this.measuresReset = new Ext.Action({
            handler: function () {
                if (this.layer) {
                    this.layer.removeAllFeatures();
                }
                for (i = 0; i < this.map.layers.length; i++) {
                    var layerName = this.map.layers[i].name;
                    //DynamicMeasure spefic name
                    var dynamicMesurePattern =
                            /(^OpenLayers.Control.DynamicMeasure)(.)*(Keep$)/
                    if (dynamicMesurePattern.test(layerName)) {
                        this.map.layers[i].removeAllFeatures();
                    }
                }
                this.measuresReset.items[0].toggle();
            },
            map: this.map,
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.reset.tooltip"),
            iconCls: 'measurements-delete',
            text: OpenLayers.i18n("measurements.reset"),
            iconAlign: 'top',
            scope: this
        });
        this.exportAsKml = new Ext.Action({
            //code from: src/main/webapp/app/addons/annotation/js/Annotation.js
            handler: function() {
                GEOR.waiter.show();
                var urlObj = OpenLayers.Util.createUrlObject(window.location.href),
                    format = new OpenLayers.Format.KML({
                        'foldersName': urlObj.host, // TODO use instance name instead
                        'internalProjection': this.map.getProjectionObject(),
                        'externalProjection': new OpenLayers.Projection("EPSG:4326")
                    });
               var kmlFeatures = this.layer.features
               for (i = 0; i < this.map.layers.length; i++) {
                    var layerName = this.map.layers[i].name;
                    //DynamicMeasure spefic name
                    var dynamicMesurePattern =
                            /(^OpenLayers.Control.DynamicMeasure)(.)*(Keep$)/
                    if (dynamicMesurePattern.test(layerName)) {
                        kmlFeatures = kmlFeatures.concat(this.map.layers[i].features);
                    }
                }

               OpenLayers.Request.POST({
                    url: GEOR.config.PATHNAME + "/ws/kml/",
                    data: format.write(kmlFeatures),
                    success: function(response) {
                        var o = Ext.decode(response.responseText);
                        window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
                    }
                });
                this.measuresReset.items[0].toggle();
            },
            map: this.map,
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.export.tooltip"),
            iconCls: 'measurements-export',
            text: OpenLayers.i18n("measurements.export"),
            iconAlign: 'top',
            scope: this
        });
        this.window = new Ext.Window({
            title: OpenLayers.i18n('measurements.tools'),
            width: 240,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: 'measurements',
            items: [{
                xtype: 'toolbar',
                border: false,
                items: [this.lengthAction, this.areaAction, this.measuresReset,
                            this.exportAsKml]
            }],
            listeners: {
                'show': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
                        this.map.addLayer(this.layer);
                    }
                },
                'hide': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) > 0) {
                        this.map.removeLayer(this.layer);
                    }
                    this.areaAction.control.deactivate();
                    this.lengthAction.control.deactivate();

                },
                scope: this
            }
        });
        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: "addon-measurements",
                handler: this._onCheckchange,
                scope: this
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-measurements",
                checked: false,
                listeners: {
                    "checkchange": this._onCheckchange,
                    scope: this
                }
            });
        }
    },

    /**
     * Method: tr
     *
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    },

    /**
     * Method: _onCheckchange
     * Callback on checkbox state changed
     */
    _onCheckchange: function(item, checked) {
        if (checked) {
            this.window.show();
            this.window.alignTo(
                Ext.get(this.map.div),
                "t-t",
                [0, 5],
                true
            );
        } else {
            this.window.hide();
        }
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.window.hide();
        this.layer.destroy();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
