Ext.namespace("GEOR.Addons");

GEOR.Addons.Measurements = Ext.extend(GEOR.Addons.Base, {

    toggleGroup: 'measurements',

    window: null,
    layer: null,
    lengthAction: null,
    areaAction: null,
    resetAction: null,
    exportAction: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.layer = new OpenLayers.Layer.Vector('__georchestra_measurements', {
            displayInLayerSwitcher: false,
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
                accuracy: this.options.accuracy,
                maxSegments: null,
                persist: true,
                geodesic: true,
                drawingLayer: this.layer,
                keep: true,
                styles: this.options.graphicStyle
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
                accuracy: this.options.accuracy,
                maxSegments: null,
                persist: true,
                geodesic: true,
                drawingLayer: this.layer,
                keep: true,
                styles: this.options.graphicStyle
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
            iconAlign: 'top'
        });

        this.resetAction = new Ext.Action({
            handler: this.resetHandler,
            scope: this,
            map: this.map,
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.reset.tooltip"),
            iconCls: 'measurements-delete',
            text: OpenLayers.i18n("measurements.reset"),
            iconAlign: 'top'
        });

        this.exportAction = new Ext.Action({
            handler: this.exportHandler,
            scope: this,
            map: this.map,
            minWidth: 50,
            tooltip: this.tr("measurements.export.tooltip"),
            iconCls: 'measurements-export',
            text: OpenLayers.i18n("measurements.export"),
            iconAlign: 'top'
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
                items: [
                    this.lengthAction,
                    this.areaAction,
                    this.resetAction,
                    this.exportAction
                ]
            }],
            listeners: {
                'show': this.onShow,
                'hide': this.onHide,
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
     * Method: onShow
     *
     */
    onShow: function() {
        if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
            this.map.addLayer(this.layer);
        }
        // Show annotation layers
        this.loopOnMeasureLayers(function(layer) {
            layer.setVisibility(true);
        });
    },


    /**
     * Method: onHide
     *
     */
    onHide: function() {
        // Hide draw layer
        if (OpenLayers.Util.indexOf(this.map.layers, this.layer) > 0) {
            this.map.removeLayer(this.layer);
        }
        // Hide annotation layers
        this.loopOnMeasureLayers(function(layer) {
            layer.setVisibility(false);
        });
        this.areaAction.control.deactivate();
        this.lengthAction.control.deactivate();
        // Deactivate measurements tool in menus
        this.item.setChecked(false);
    },


    /**
     * Method: resetHandler
     *
     */
    resetHandler: function() {
        if (this.layer) {
            this.layer.removeAllFeatures();
        }
        this.loopOnMeasureLayers(function(layer) {
            layer.removeAllFeatures();
        });
        this.resetAction.items[0].toggle();
    },


    /**
     * Method: exportHandler
     *
     */
    exportHandler: function() {
        // code from: src/main/webapp/app/addons/annotation/js/Annotation.js
        GEOR.waiter.show();
        var format = new OpenLayers.Format.KML({
            'extractAttributes': true,
            'foldersName': OpenLayers.i18n("measurements.tools"),
            'internalProjection': this.map.getProjectionObject(),
            'externalProjection': new OpenLayers.Projection("EPSG:4326")
        });

        Ext.each(this.layer.features, function(feature) {
            var label = '', geometry = feature.geometry,
                data = feature.data,
                length = this.formatMeasure(
                    this.lengthAction.control.getBestLength(geometry)
                );

            data.length_measure = length[0];
            data.length_units = length[1];

            if (geometry instanceof OpenLayers.Geometry.Polygon) {
                var area = this.formatMeasure(
                    this.areaAction.control.getBestArea(geometry)
                );
                data.area_measure = area[0];
                data.area_units = area[1];

                label = [data.area_measure, ' ',
                    data.area_units, '² (', 
                    data.length_measure, ' ', 
                    data.length_units, ')'
                ].join('');

            } else {
                label = data.length_measure + ' ' + data.length_units;
            }

            feature.attributes.name = label;
            feature.attributes.description = label;
        }, this);

        var kmlFeatures = [];
        this.loopOnMeasureLayers(function(layer) {
            var areaPattern = /(^OpenLayers.Control.DynamicMeasure)(.)*(AreaKeep$)/;

            Ext.each(layer.features, function(feature) {
                var measure,
                    data = feature.data;
                // Square unit for area
                if (areaPattern.test(this.name)) {
                    measure = data.measure + ' ' + data.units + '²';
                } else {
                    measure = data.measure + ' ' + data.units;
                }
                feature.attributes.name = measure;
                feature.attributes.description = measure;
            }, this);

            kmlFeatures = kmlFeatures.concat(layer.features);
        });

        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/kml/",
            data: format.write(kmlFeatures)
                .replace(/<Folder>/g, '<Folder>' + this.options.KMLStyle)
                .replace(/<Placemark><name>/g,'<Placemark><styleUrl>#measureFeatureStyle</styleUrl><name>'),
            success: function(response) {
                var o = Ext.decode(response.responseText);
                window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
            }
        });

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
     * Method: loopOnMeasureLayers
     *
     * Private use.
     * fun is a function that will be called with a matching layer
     */
    loopOnMeasureLayers: function(fun) {
        // Regular expression associated with layer.name 
        // which contains measurement annotations 
        // managed by OpenLayers.Control.DynamicMeasure
        var pattern = /(^OpenLayers.Control.DynamicMeasure)(.)*(Keep$)/;

        Ext.each(this.map.layers, function(layer) {
            if (pattern.test(layer.name)) {
                fun.call(this, layer);
            }
        }, this);
    },


    /**
     * Method : formatMeasure
     *
     * Format a measure in the add-on format
     *
     * Parameter:
     * - Array({Float|String}) Measure provided by OL Measure control.
     *
     * Return:
     * - Array({String|String}) Formatted measure the first item is value
     *   and the second is the unit.
     *
     */
    formatMeasure: function(measure) {
        // FIXME: NUMBER !
        measure[0] = OpenLayers.Number.format(
            Number(measure[0].toPrecision(this.options.accuracy)), null);

        return measure;
    },


    /**
     * Method: destroy
     *
     */
    destroy: function() {
        this.window.hide();
        this.layer.destroy();
        this.layer = null;
        this.lengthAction.control.emptyKeeped();
        this.areaAction.control.emptyKeeped();
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
