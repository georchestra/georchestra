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
        this.measuresReset = new Ext.Action({
            handler: function () {
                if (this.layer) {
                    this.layer.removeAllFeatures();
                }
                var removeAllFeaturesCallback = function () {
                    this.removeAllFeatures();
                }
                this._loopOnMatchingLayers(this.map, this.dynamicMesurePattern,
                    removeAllFeaturesCallback);
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
                var format = new OpenLayers.Format.KML({
                        'extractAttributes': true,
                        'foldersName': OpenLayers.i18n("measurements.tools"),
                        'internalProjection': this.map.getProjectionObject(),
                        'externalProjection': new OpenLayers.Projection("EPSG:4326")
                    });
                var kmlFeatures = this.layer.features;
                for (i = 0; i < kmlFeatures.length; i++) {
                    var geometry = kmlFeatures[i].geometry;
                    var aLength = this.lengthAction.control.getBestLength(geometry);
                    sLength = this.formatMeasure(aLength);
                    kmlFeatures[i].data.length_measure = sLength[0];
                    kmlFeatures[i].data.length_units = sLength[1];


                    if (geometry instanceof OpenLayers.Geometry.Polygon) {
                        var area = this.areaAction.control.getBestArea(geometry);
                        sArea = this.formatMeasure(area);
                        kmlFeatures[i].data.area_measure = sArea[0];
                        kmlFeatures[i].data.area_units = sArea[1];
                        var label = kmlFeatures[i].data.area_measure + ' ' +
                                kmlFeatures[i].data.area_units + '² (' +
                                kmlFeatures[i].data.length_measure + ' ' +
                                kmlFeatures[i].data.length_units + ')';
                    } else {
                        var label = kmlFeatures[i].data.length_measure + ' ' +
                                    kmlFeatures[i].data.length_units;
                    }

                    kmlFeatures[i].attributes.name = label;
                    kmlFeatures[i].attributes.description = label;
               }
               var setNameDescriptionKmlCallback = function(argsObj) {
                   var feature = null, measure = null,
                       areaPattern = /(^OpenLayers.Control.DynamicMeasure)(.)*(AreaKeep$)/
                   for (j = 0; j < this.features.length; j++) {
                        feature = this.features[j];
                        //Square unit for area
                        if (areaPattern.test(this.name)) {
                            measure = feature.data.measure + ' ' +
                                feature.data.units + '²';
                        } else {
                            measure = feature.data.measure + ' ' +
                                feature.data.units;
                        }
                        feature.attributes.name = measure;
                        feature.attributes.description = measure;
                    }
                    argsObj.kmlFeatures = argsObj.kmlFeatures.concat(this.features);
               }
               var kmlObj = {kmlFeatures: kmlFeatures};
               this._loopOnMatchingLayers(this.map, this.dynamicMesurePattern,
                    setNameDescriptionKmlCallback, kmlObj);

                var olKML = format.write(kmlObj.kmlFeatures),
                    kmlStyle = "<Style id='measureFeatureStyle'><LineStyle><width>2</width><color>ff6666636</color></LineStyle><PolyStyle><fill>0</fill></PolyStyle><LabelStyle><color>ff170580</color></LabelStyle><IconStyle><color>00ffffff</color><Icon><href>http:/maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href></Icon></IconStyle></Style>",
                    styleInHeadKML = olKML.replace(/<Folder>/g, '<Folder>' + kmlStyle),
                    styleInHeadAndPlacemarksKML = styleInHeadKML.replace(/<Placemark><name>/g,'<Placemark><styleUrl>#measureFeatureStyle</styleUrl><name>');
                OpenLayers.Request.POST({
                    url: GEOR.config.PATHNAME + "/ws/kml/",
                    data: styleInHeadAndPlacemarksKML,
                    success: function(response) {
                        var o = Ext.decode(response.responseText);
                        window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
                    }
                });
                this.exportAsKml.items[0].toggle();
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
                    // Show annotation layers
                    var setVisibilityCallback = function() {
                        this.setVisibility(true);
                    }
                    this._loopOnMatchingLayers(this.map, this.dynamicMesurePattern,
                        setVisibilityCallback);
                },
                'hide': function() {
                    // Hide draw layer
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) > 0) {
                        this.map.removeLayer(this.layer);
                    }
                    // Hide annotation layers
                    var setVisibilityCallback = function() {
                        this.setVisibility(false);
                    }
                    this._loopOnMatchingLayers(this.map, this.dynamicMesurePattern,
                        setVisibilityCallback);
                    this.areaAction.control.deactivate();
                    this.lengthAction.control.deactivate();
                    //Deactivate measurements tool in menus
                    this.item.setChecked(false);
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
     * Method: _loopOnMatchingLayers
     *
     * Private use.
     * fun is a function that will be called with a matching layer and
     *     the argsObj as parameter
     */
    _loopOnMatchingLayers: function (map, pattern, fun, argsObj) {
        argsObj = argsObj || null;
        var layerName = null;
        for (i = 0; i < map.layers.length; i++) {
            layerName = map.layers[i].name
            if (pattern.test(layerName)) {
                fun.call(map.layers[i], argsObj);
            }
        }
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
        measure[0] = OpenLayers.Number.format(
            Number(measure[0].toPrecision(this.options.accuracy)), null);

        return measure;
    },

    /**
     * Property: dynamicMesurePattern
     *
     * Regular expression associated with layer.name which contains measurement
     * annotations managed by OpenLayers.Control.DynamicMeasure
     */
    dynamicMesurePattern: /(^OpenLayers.Control.DynamicMeasure)(.)*(Keep$)/,

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
