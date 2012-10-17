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
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include Ext.ux/form/TwinTriggerComboBox.js
 * @include GeoExt.ux/widgets/form/BoundingBoxPanel.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Handler/RegularPolygon.js
 */

Ext.namespace("GEOR");

GEOR.layeroptions = (function() {
    /*
     * Private
     */

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

    /**
     * Property: map
     * {OpenLayers.Map} The map
     */
    var map;

    /**
     * Property: vectorLayer
     * {OpenLayers.Layer.Vector} layer vector used to display
     * the bbox to be extracted.
     */
    var vectorLayer = null;

    /**
     * Property: layerOptionsPanel
     * {Ext.Panel} main panel containing the layer options.
     */
    var layerOptionsPanel = null;

    /**
     * Property: combos
     * {Object}
     */
    var combos = {};

    /**
     * Property: fieldsets
     * {Object}
     */
    var fieldsets = {};

    /**
     * Property: numberfields
     * {Object}
     */
    var numberfields = {};

    /**
     * Method: getCombo
     * Return the combo corresponding to ref
     * {Ext.form.ComboBox}
     */
    var getCombo = function(ref, options) {
        if(!combos[ref]) {
            var opts = {
                anchor: '-20',
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                fieldLabel: 'Combo label',
                mode: 'local',
                valueField: 'value',
                displayField: 'text',
                store: new Ext.data.SimpleStore({
                    fields: ['value', 'text'],
                    data: options && options.store_data || []
                })
            };
            if(options && options.store_data) { delete(options.store_data); }
            if(options && options.twin) {
                delete(options.twin);
                opts.trigger3Class = 'x-form-trigger-no-width x-hidden';
                combos[ref] = new Ext.ux.form.TwinTriggerComboBox(
                    Ext.apply(opts, options));
            }
            else {
                combos[ref] = new Ext.form.ComboBox(
                    Ext.apply(opts, options));
            }
        }
        return combos[ref];
    };

    /**
     * Method: getNumberField
     * Return the numberfield corresponding to ref
     * {Ext.form.NumberField}
     */
    var getNumberField = function(ref, options) {
        if(!numberfields[ref]) {
            var opts = {
                anchor: '-20',
                decimalPrecision: 2
            };
            numberfields[ref] = new Ext.form.NumberField(
                Ext.apply(opts, options));
            numberfields[ref].showAll = function() {
                numberfields[ref].getEl().up('div.x-form-item').show();
            };
            numberfields[ref].hideAll = function() {
                numberfields[ref].getEl().up('div.x-form-item').hide();
            };
            numberfields[ref].isAllVisible = function() {
                numberfields[ref].getEl().up('div.x-form-item').isVisible();
            };
        }
        return numberfields[ref];
    };

    /**
     * Method: getFieldSet
     * Return the fieldset corresponding to ref
     * {Ext.form.FieldSet}
     */
    var getFieldSet = function(ref, options) {
        if(!fieldsets[ref]) {
            fieldsets[ref] = new Ext.form.FieldSet(
                Ext.apply({
                    cls: 'bbox-fieldset',
                    autoHeight: true,
                    items: [{
                        xtype: 'gxux_bboxpanel',
                        border: false,
                        map: map,
                        vectorLayer: vectorLayer
                    }]
                }, options)
            );
        }
        return fieldsets[ref];
    };

    /**
     * Method: getFieldsetTitle
     * Return the fieldset title, based on current map projection
     */
    var getFieldsetTitle = function() {
        var crs = map.getProjection();
        return tr("layeroptions.boundingbox",{
            "UNIT": tr(GEOR.util.unitsTranslations[GEOR.util.getUnitsForCRS(crs)]),
            "NUMBER": crs.split(':')[1],
            "CRS": crs});
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: getOptions
         * Get layer export options.
         *
         * Returns:
         * {Object} object filled with layer export options values.
         */
        getOptions: function() {
            // for each widget, get its value
            var options = {};
            if(layerOptionsPanel.getLayout().activeItem.id == 'globalLayerOptions') {
                // global properties
                options.projection = getCombo('globalProjections').getValue();
                options.resolution = getNumberField('globalResolution').getValue();
                options.globalRasterFormat = getCombo('globalRasterFormats').getValue();
                options.globalVectorFormat = getCombo('globalVectorFormats').getValue();
                options.bbox = getFieldSet('globalBbox').items.itemAt(0).getBbox();
            }
            else if(layerOptionsPanel.getLayout().activeItem.id == 'customLayerOptions') {
                options.projection = getCombo('customProjections').getValue();
                options.resolution = getNumberField('customResolution').getValue();
                options.format = getCombo('customFormats').getValue();
                options.bbox = getFieldSet('customBbox').items.itemAt(0).getBbox();
                options.bboxFromGlobal = getFieldSet('customBbox').collapsed;
            }
            return options;
        },

        /**
         * APIMethod: setBbox
         * Set the bbox given the bounds
         *
         * Parameters:
         * bounds - {OpenLayers.Bounds} bounds to be set
         */
        setBbox: function(bounds) {
            if (bounds && (bounds.getWidth() + bounds.getHeight() > 0)) {
                var fieldset = (layerOptionsPanel.getLayout().activeItem.id == "globalLayerOptions") ?
                    getFieldSet('globalBbox') : getFieldSet('customBbox');
                !fieldset.collapsed && fieldset.items.itemAt(0).setBbox(bounds);
            }
        },

        /**
         * APIMethod: setOptions
         * Load layer options in the panel.
         *
         * Parameters:
         * options - {Object} object from which layer options values
         * are read.
         * global - {Boolean} true if options refer to global options
         */
        setOptions: function(options, global) {
            layerOptionsPanel.getLayout().setActiveItem(global ? 0 : 1);
            if(!options) {
                return;
            }
            if(global) {
                // global properties

                // restore BBox in form fields & map
                options.bbox && getFieldSet('globalBbox').items.itemAt(0).setBbox(options.bbox);

                // restore combo values
                options.projection && getCombo('globalProjections').setValue(options.projection);
                options.resolution && getNumberField('globalResolution').setValue(options.resolution);
                options.globalRasterFormats && getCombo('globalRasterFormats').setValue(options.globalRasterFormat);
                options.globalVectorFormats && getCombo('globalVectorFormats').setValue(options.globalVectorFormat);
                vectorLayer.setVisibility(true);
                getFieldSet('globalBbox').setTitle(getFieldsetTitle());
            } else {
                // custom properties
                // reload store for projections combo
                var formats, projections = GEOR.config.SUPPORTED_REPROJECTIONS;
                if(options.owsType == "WFS") {
                    formats = GEOR.config.SUPPORTED_VECTOR_FORMATS;
                    getNumberField('customResolution').hideAll();
                } else {
                    formats = GEOR.config.SUPPORTED_RASTER_FORMATS;
                    getNumberField('customResolution').showAll();
                }
                getCombo('customFormats').getStore().loadData(formats);
                getCombo('customFormats').clearValue();
                getCombo('customProjections').getStore().loadData(projections);
                getCombo('customProjections').clearValue();

                // restore values
                getCombo('customProjections').setValue(options.projection);
                getNumberField('customResolution').setValue(options.resolution);
                getCombo('customFormats').setValue(options.format);
                getFieldSet('customBbox').items.itemAt(0).setBbox(options.bbox);
                var collapsedState = (options.bboxFromGlobal == undefined) ?
                    true : options.bboxFromGlobal;
                if (collapsedState == getFieldSet('customBbox').collapsed) {
                    vectorLayer.setVisibility(!collapsedState);
                }
                else {
                    getFieldSet('customBbox').toggleCollapse();
                }
                getFieldSet('customBbox').setTitle(getFieldsetTitle());
            }
        },

        /**
         * APIMethod: create
         * Returns the layer options panel.
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         * options - {Object} options to be applied to panel constructor.
         *
         * Returns:
         * {Ext.Panel} The layer options panel.
         */
        create: function(m, options) {
            if (options.vectorLayer) {
                vectorLayer = options.vectorLayer;
                delete options.vectorLayer;
                vectorLayer.events.on({
                    "beforefeatureadded": function(o) {
                        var feature = o.feature;
                        if (!feature) {
                            return;
                        }
                        var area = feature.geometry.getGeodesicArea(map.getProjectionObject())/1E6;
                        var str = (area > 10) ?
                            Math.round(area) + tr(' km²'):
                            (area < 0.1) ?
                                Math.round(area*1E6) + tr(' m²') :
                                Math.round(area*10)/10  + tr(' km²');
                        o.feature.attributes['area'] = str;
                    },
                    scope: this
                });
            }
            map = m;
            layerOptionsPanel = new Ext.Panel(
                Ext.apply(options, {
                    layout: 'card',
                    activeItem: 0,
                    defaults: {
                        defaults: {
                            border: false
                        },
                        bodyStyle: 'padding: 5px; border-color: #fff #fff #d0d0d0 #fff;'
                    },
                    items: [{
                        id: 'globalLayerOptions',
                        layout: 'column',
                        items: [
                            {
                                columnWidth: 0.25,
                                layout: 'form',
                                labelAlign: 'top',
                                items: [
                                    getCombo('globalProjections', {
                                        fieldLabel: tr("Output projection"),
                                        store_data: GEOR.config.SUPPORTED_REPROJECTIONS,
                                        value: GEOR.config.GLOBAL_EPSG
                                    }),
                                    getNumberField('globalResolution', {
                                        fieldLabel: tr('Raster resolution (m/pixel)'),
                                        //value: GEOR.config.GLOBAL_MAX_EXTENT.getWidth() / GEOR.config.DEFAULT_WCS_EXTRACTION_WIDTH
                                        value: 0.5,
                                        allowBlank: false,
                                        listeners: {
                                            // TODO: remove following direct validation, and create a
                                            // global validation for extractor params to check if
                                            // extraction is allowed.
                                            "valid": function() {
                                                Ext.getCmp("geor-btn-extract-id").enable();
                                            },
                                            "invalid": function() {
                                                Ext.getCmp("geor-btn-extract-id").disable();
                                            },
                                            scope: this
                                        }
                                    })
                                ]
                            }, {
                                columnWidth: 0.25,
                                layout: 'form',
                                labelAlign: 'top',
                                items: [
                                    getCombo('globalRasterFormats', {
                                        fieldLabel: tr('Raster output format'),
                                        store_data: GEOR.config.SUPPORTED_RASTER_FORMATS,
                                        value: GEOR.config.SUPPORTED_RASTER_FORMATS[0][0]
                                    }),
                                    getCombo('globalVectorFormats', {
                                        fieldLabel: tr('Vector output format'),
                                        store_data: GEOR.config.SUPPORTED_VECTOR_FORMATS,
                                        value: GEOR.config.SUPPORTED_VECTOR_FORMATS[0][0]
                                    })
                                ]
                            }, {
                                columnWidth: 0.5,
                                layout: 'form',
                                items: [
                                    getFieldSet('globalBbox', {
                                        title: 'fake' // will be appropriately set
                                                      // when the default layer
                                                      // is selected. We cannot
                                                      // initially set it to ''
                                                      // or it will never be
                                                      // displayed
                                    })
                                ]
                            }
                        ]
                    }, {
                        id: 'customLayerOptions',
                        layout: 'column',
                        items: [
                            {
                                columnWidth: 0.25,
                                layout: 'form',
                                labelAlign: 'top',
                                items: [
                                    getCombo('customProjections', {
                                        twin: true,
                                        fieldLabel: tr('Output projection')
                                    }),
                                    getNumberField('customResolution', {
                                        fieldLabel: tr('Raster resolution (m/pixel)'),
                                        value: null
                                    })
                                ]
                            }, {
                                columnWidth: 0.25,
                                layout: 'form',
                                labelAlign: 'top',
                                items: [
                                    getCombo('customFormats', {
                                        twin: true,
                                        fieldLabel: tr('Output format')
                                    })
                                ]
                            }, {
                                columnWidth: 0.5,
                                layout: 'form',
                                items: [
                                    getFieldSet('customBbox', {
                                        title: tr('Bounding box'),
                                        checkboxToggle: true,
                                        listeners: {
                                            'collapse': function() {
                                                vectorLayer.setVisibility(false);
                                            },
                                            'expand': function() {
                                                vectorLayer.setVisibility(true);
                                            }
                                        }
                                    })
                                ]
                            }
                        ]
                    }]
                }));
            return layerOptionsPanel;
        }
    };


})();
