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
 * @include OpenLayers/Strategy/Save.js
 * @include OpenLayers/Strategy/BBOX.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include OpenLayers/Control/ModifyFeature.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Control/Snapping.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Polygon.js
 * @include OpenLayers/Style.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Geometry/MultiPoint.js
 * @include OpenLayers/Geometry/MultiLineString.js
 * @include OpenLayers/Geometry/MultiPolygon.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include GeoExt/widgets/Action.js
 * @include GeoExt/plugins/AttributeForm.js
 * @include GEOR_util.js
 */

Ext.namespace('GEOR.Editing');

/**
 */
GEOR.Editing.LayerEditingPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: layer
     * {OpenLayers.Layer.Vector}
     */
    layer: null,

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    map: null,

    /**
     * Property: attributeStore
     * {GeoExt.data.AttributeStore}
     */
    attributeStore: null,

    /**
     * Property: selectFeature
     * {OpenLayers.Control.SelectFeature}
     */
    selectFeature: null,

    /**
     * Property: drawFeature
     * {OpenLayers.Control.DrawFeature}
     */
    drawFeature: null,

    /**
     * Property: snap
     * {OpenLayers.Control.Snapping}
     */
    snap: null,

    /**
     * Property: modifyFeature
     * {OpenLayers.Control.ModifyFeature}
     */
    modifyFeature: null,

    /**
     * Property: nextSelectedFeature
     * {OpenLayers.Feature.Vector} the feature which should have been selected
     * on click (but which cannot be selected, since the previous feature has updates)
     */
    nextSelectedFeature: null,

    /**
     * Property: lastFeature
     * {OpenLayers.Feature.Vector} The feature which has just been unselected.
     */
    lastFeature: null,

    /**
     * Property: originalGeometry
     * {OpenLayers.Geometry}
     */
    originalGeometry: null,

    /**
     * Property: strategy
     * {OpenLayers.Strategy.Save}
     */
    strategy: null,

    /**
     * Property: saveBtn
     * {Ext.Button}
     */
    saveBtn: null,

    /**
     * Property: deleteBtn
     * {Ext.Button}
     */
    deleteBtn: null,

    /**
     * Property: cancelBtn
     * {Ext.Button}
     */
    cancelBtn: null,

    /**
     */
    initComponent: function() {

        this.strategy = new OpenLayers.Strategy.Save();

        // prevent the layer from destroying the protocol, as
        // it isn't handled by us
        this.protocol.autoDestroy = false;

        // create vector layer based on the passed protocol
        // and display it
        this.layer = new OpenLayers.Layer.Vector(
            "GEOR.Editing.LayerEditingPanel", {
            strategies: [
                new OpenLayers.Strategy.BBOX(),
                this.strategy
            ],
            protocol: this.protocol,
            styleMap: this.createStyleMap(),
            displayInLayerSwitcher: false,
            alwaysInRange: true
        });
        this.map.addLayer(this.layer);
        // features are downloaded from BBOX strategy at this point:
        this.layer.refresh();

        // add editing controls
        var type = this.addLayerControls();

        this.tbar = [
            new GeoExt.Action({
                map: this.map,
                control: this.drawFeature,
                enableToggle: true,
                toggleGroup: 'edit',
                text: OpenLayers.i18n("Enter ") + type.text,
                iconCls: type.iconCls
            }),
            new GeoExt.Action({
                map: this.map,
                control: this.selectFeature,
                enableToggle: true,
                toggleGroup: 'edit',
                text: OpenLayers.i18n("Modify object"),
                pressed: true
            })
        ];

        this.bbar = [
            '->',
            {
                text: OpenLayers.i18n('Cancel all'),
                iconCls: 'geor-btn-cancel',
                handler: function() {
                    GEOR.util.confirmDialog({
                        title: OpenLayers.i18n('Cancel all'),
                        msg: OpenLayers.i18n('layereditingpanel.cancel.confirm'),
                        yesCallback: function() {
                            this.layer.refresh({force: true});
                            this.lastFeature = null;
                        },
                        scope: this
                    });
                },
                scope: this
            },
            {
                text: OpenLayers.i18n("Synchronize"),
                iconCls: 'geor-btn-sync',
                handler: function() {
                    if (!this.isFeatureDirty()) {
                        if (this.layer.selectedFeatures.length > 0) {
                            var f = this.layer.selectedFeatures[0];
                            this.modifyFeature.unselectFeature(f);
                            this.layer.drawFeature(f, "default");
                            this.silentUnselect();
                            this.cleanForm();
                            this.lastFeature = null;
                        }
                        this.strategy.save();
                        // we want to force activation of select feature control:
                        this.selectFeature.activate();
                    } else {
                        GEOR.util.infoDialog({
                            msg: OpenLayers.i18n("layereditingpanel.changes.confirm")
                        });
                    }
                },
                scope: this
            }
        ];


        this.cancelBtn = new Ext.Button({
            text: OpenLayers.i18n('Cancel'),
            handler: function() {
                var feature = this.layer.selectedFeatures[0];
                if (feature) {
                    this.modifyFeature.unselectFeature(feature);
                    this.silentUnselect();
                    this.layer.removeFeatures([feature]);
                    feature.geometry = this.originalGeometry.clone();
                    feature.toState(OpenLayers.State.UNKNOWN);
                    this.layer.addFeatures([feature]);
                    this.silentSelect(feature);
                    this.modifyFeature.selectFeature(feature);
                }
                this.formPanel.getForm().reset();
            },
            scope: this
        });

        this.deleteBtn = new Ext.Button({
            text: OpenLayers.i18n('Delete'),
            disabled: true,
            handler: function() {
                var feature = this.layer.selectedFeatures[0];
                if (!feature) {
                    GEOR.util.infoDialog({
                        msg: OpenLayers.i18n('Please select one feature.')
                    });
                    return;
                }
                if (feature.fid === null) {
                    // feature has been created in the client, but never synchronised
                    this.silentUnselect();
                    this.modifyFeature.unselectFeature(feature);
                    this.layer.destroyFeatures([feature]);
                } else {
                    if (feature.state == OpenLayers.State.DELETE) {
                        // restoring feature
                        feature.state = OpenLayers.State.UPDATE;
                    } else {
                        // always deleting
                        feature.state = OpenLayers.State.DELETE;
                    }
                    this.layer.drawFeature(feature, this.selectFeature.renderIntent);
                    this.modifyFeature.unselectFeature(feature);
                    this.silentUnselect();
                }
                this.cleanForm();
                this.lastFeature = null;
            },
            scope: this
        });

        this.saveBtn = new Ext.Button({
            text: OpenLayers.i18n('Confirm'),
            formBind: true,
            handler: this.confirmHandler,
            scope: this
        });

        this.formPanel = new Ext.form.FormPanel({
            plugins: [
                new GeoExt.plugins.AttributeForm({
                    attributeStore: this.attributeStore,
                    recordToFieldOptions: {
                        labelTpl: new Ext.XTemplate(
                            '<span ext:qtip="{[this.getTip(values)]}">{name}</span>', {
                                compiled: true,
                                disableFormats: true,
                                getTip: function(v) {
                                    if (!v.type) {
                                        return '';
                                    }
                                    var type = v.type.split(":").pop(); // remove ns prefix
                                    return OpenLayers.i18n(type) +
                                        (v.nillable ? '' : OpenLayers.i18n(' (required)'));
                                }
                            }
                        ),
                        checkboxLabelProperty: 'fieldLabel',
                        mandatoryFieldLabelStyle: 'font-weight:bold;'
                    }
                })
            ],
            defaults: {
                width: 160,
                maxLengthText: OpenLayers.i18n("Text too long"),
                minLengthText: OpenLayers.i18n("Text too short"),
                maxText: OpenLayers.i18n("Maximum value exceeded"),
                minText: OpenLayers.i18n("Minimum value not reached"),
                nanText: OpenLayers.i18n("Invalid number")
            },
            trackResetOnLoad: true,
            monitorValid: true,
            autoScroll: true,
            labelWidth: 100,
            bodyStyle: 'padding: 5px;',
            bodyCssClass: 'layer-editing-panel',
            labelSeparator: ' :',
            border: false,
            disabled: true,
            bbar: [this.deleteBtn, '->', this.cancelBtn, this.saveBtn],
            labelStyle: 'font-size:11px;text-transform:lowercase;'
        });

        // build layout
        Ext.apply(this, {
            layout: 'form',
            layout: 'fit',
            items: [ this.formPanel ]
        });

        // manage events
        this.layer.events.register('beforefeatureselected', this, this.checkSelect);
        this.layer.events.register('featureunselected', this, this.unSelect);

        this.drawFeature.events.register('featureadded', this, function(e) {
            this.selectFeature.unselectAll();
            this.selectFeature.select(e.feature);
            // HACK: fixes selected feature can't be unselected
            this.selectFeature.handlers.feature.lastFeature = e.feature;
        });
        this.strategy.events.register('success', this, function() {
            GEOR.util.infoDialog({
                msg: OpenLayers.i18n('Synchronization successful.')
            });
            this.lastFeature = null;
            this.layer.redraw();
            // reset features original values:
            Ext.each(this.layer.features, function(f) {
                f.modified = null;
            });
        });
        this.strategy.events.register('fail', this, function() {
            GEOR.util.errorDialog({
                msg: OpenLayers.i18n('Synchronization failed.')
            });
        });

        this.formPanel.on('clientvalidation', function(formPanel, valid){
            if (this.isFeatureDirty()) {
                this.saveBtn.enable();
                this.cancelBtn.enable();
            } else {
                this.cancelBtn.disable();
                this.saveBtn.disable();
            }
            var feature = this.layer.selectedFeatures[0];
            if (feature) {
                if (feature.state == OpenLayers.State.DELETE) {
                    this.deleteBtn.setText(OpenLayers.i18n('Recover'));
                } else {
                    this.deleteBtn.setText(OpenLayers.i18n('Delete'));
               }
            }
        }, this);

        GEOR.Editing.LayerEditingPanel.superclass.initComponent.apply(this, arguments);
    },

    /**
     * Method: confirmHandler
     *   confirm the modifications on the current selected feature
     *
     */
    confirmHandler: function(feature) {
        feature = (feature.CLASS_NAME == "OpenLayers.Feature.Vector" ? feature : null) ||
            this.layer.selectedFeatures[0];

        if (!feature) {
            GEOR.util.errorDialog({
                msg: OpenLayers.i18n('No feature selected !')
            });
            return;
        }

        var fieldName, value, fa = feature.attributes;
        this.formPanel.form.items.each(function(field) {
            fieldName = field.getName();
            value = field.getValue();
            // setting value to null if field is empty is *very important* here
            // so that the field value effectively gets deleted:
            fa[fieldName] = (value === '' ? null : value);
        });
        if (feature.state != OpenLayers.State.INSERT) {
            feature.state = OpenLayers.State.UPDATE;
            this.layer.drawFeature(feature);
        }
        this.lastFeature = null;
        this.modifyFeature.unselectFeature(feature);
        this.silentUnselect();
        this.cleanForm();
    },

    /**
     * Method: addLayerControls
     *   add select & draw control according to layer symbol type
     *
     */
    addLayerControls: function() {

        var labels = {
            Point: {
                text: OpenLayers.i18n('a point'),
                iconCls: 'drawpoint'
            },
            Line: {
                text: OpenLayers.i18n('a line'),
                iconCls: 'drawline'
            },
            Polygon: {
                text: OpenLayers.i18n('a polygon'),
                iconCls: 'drawpolygon'
            }
        };

        this.selectFeature = new OpenLayers.Control.SelectFeature(
            this.layer
        );

        this.modifyFeature = new OpenLayers.Control.ModifyFeature(
            this.layer, {
                standalone: true
            }
        );

        // this is necessary for the drag control's feature handler
        // not to stop "click" events when a feature is selected
        var featureHandler = this.modifyFeature.dragControl.handlers.feature;
        featureHandler.stopClick = false;
        featureHandler.stopDown = false;
        featureHandler.stopUp = false;

        this.map.addControl(this.modifyFeature);
        this.modifyFeature.activate();

        var type = GEOR.ows.getSymbolTypeFromAttributeStore(this.attributeStore);
        var handlerOptions = {};
        var typeName = type.type;
        // handle Multi geometries:
        if (type.multi == 'Multi') {
            handlerOptions.multi = true;
        }
        if (type.type == 'Line') {
            type.type = 'Path';
        }

        this.drawFeature = new OpenLayers.Control.DrawFeature(
            this.layer,
            OpenLayers.Handler[type.type],
            {
                handlerOptions: handlerOptions
            }
        );

        this.snap = new OpenLayers.Control.Snapping({layer: this.layer});
        this.map.addControl(this.snap);

        return labels[typeName];
    },

    /**
     * Method: isFeatureDirty
     *
     * Check if the feature's geometry or its attributes has been modified
     * If no feature is given, uses the selected feature
     *
     */
    isFeatureDirty: function(feature) {
        if (!feature) {
            var feature = this.layer.selectedFeatures.length > 0 ?
                          this.layer.selectedFeatures[0] : undefined;
        }

        return this.formPanel.getForm().isDirty() ||
               (feature && this.originalGeometry &&
                !this.originalGeometry.equals(feature.geometry));
    },

    /**
     * Method: checkSelect
     * Validates whether e.feature can be selected or not
     * Cancels select feature operation if the form is in a dirty state
     */
    checkSelect: function(e) {
        var feature = e.feature;
        if (this.isFeatureDirty(this.lastFeature)) {
            this.nextSelectedFeature = feature;
        } else {
            this.formPanel.enable();
            this.deleteBtn.enable();
            this.loadFeature(feature);
        }
        return false;
    },

    /**
     * Method: loadFeature
     *   load feature's data for editing
     */
    loadFeature: function(feature) {
        this.silentSelect(feature);
        this.originalGeometry = feature.geometry.clone();
        // do this only one time for a "checked-out" feature
        if (!feature.modified || !feature.modified.attributes) {
            feature.modified = OpenLayers.Util.extend(feature.modified, {
                attributes: feature.attributes
            });
        }
        this.formPanel.getForm().setValues(feature.attributes);
    },

    /**
     * Method: silentSelect
     *  select feature without triggering any event
     */
    silentSelect: function(feature) {
        // temporary unregister callback to avoid recursion
        this.selectFeature.unselectAll();
        this.layer.events.unregister('beforefeatureselected', this, this.checkSelect);
        this.selectFeature.select(feature);
        this.modifyFeature.selectFeature(feature);
        this.layer.events.register('beforefeatureselected', this, this.checkSelect);
        this.formPanel.enable();
    },

    /**
     * Method: silentUnselect
     *  unselect feature without triggering any event
     */
    silentUnselect: function(feature) {
        // temporary unregister callback to avoid recursion
        this.layer.events.unregister('featureunselected', this, this.unSelect);
        this.selectFeature.unselectAll();
        this.formPanel.disable();
        this.layer.events.register('featureunselected', this, this.unSelect);
    },

    /**
     * Method: unSelect
     *  callback executed on feature unselected.
     *  checks for unsaved data, and displays confirm dialog in this case
     */
    unSelect: function(e) {
        var feature = e.feature;
        this.lastFeature = feature;
        this.modifyFeature.unselectFeature(feature);
        this.layer.drawFeature(feature, "default");
        // TODO: I would propose to auto-save feature on unselect,
        // without bothering the user.
        // Or at least, offer him the choice to switch to a "auto-confirm edits" mode.
        if (this.isFeatureDirty(feature)) {
            GEOR.util.confirmDialog({
                title: OpenLayers.i18n("Pending changes"),
                msg: OpenLayers.i18n("Do you want to confirm changes ?"),
                yesCallback: function() {
                    // we do as if the "confirm" button had been pressed here
                    this.confirmHandler(feature);
                },
                noCallback: function() {
                    // restore unmodified geometry
                    this.layer.removeFeatures([feature]);
                    feature.geometry = this.originalGeometry.clone();
                    feature.toState(OpenLayers.State.UNKNOWN);
                    this.layer.addFeatures([feature]);

                    if (this.nextSelectedFeature) {
                        this.loadFeature(this.nextSelectedFeature);
                        this.nextSelectedFeature = null;
                    } else {
                        this.cleanForm();
                        this.formPanel.disable();
                    }
                },
                scope: this
            });
            return false;
        }
        this.cleanForm();
    },

    /**
     * Method: cleanForm
     * Set empty values in form in a non-dirty state
     */
    cleanForm: function() {
        var form = this.formPanel.getForm();
        Ext.each(form.items.items, function(field){
            field.setValue('');
        });
    },

    /**
     * Method: createStyleMap
     * Create a style map for the vector layer.
     *
     * Returns:
     * {<OpenLayer.StyleMap>} The style map.
     */
    createStyleMap: function() {

        var style = OpenLayers.Util.extend({},
                        OpenLayers.Feature.Vector.style['default']);
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(
                OpenLayers.Util.extend(style, {
                    strokeWidth: 3,
                    cursor: "pointer"
                })
            )
        });

        // create a styleMap for the vector layer so that features
        // have different styles depending on their states, also
        // use the "select" render intent for styling vertices
        // displayed when modifying a feature (such vertices
        // have the _sketch property set).
        var context = function(feature) {
            var state = feature._sketch ? "vertex" : undefined;
            return {
                state: state || feature.state || OpenLayers.State.UNKNOWN
            };
        };
        var lookup = {};
        lookup[OpenLayers.State.UNKNOWN] = {};
        lookup[OpenLayers.State.UPDATE] = {
            fillColor: "green",
            strokeColor: "green"
        };
        lookup[OpenLayers.State.DELETE] = {
            fillColor: "red",
            strokeColor: "red",
            fillOpacity: 0.1,
            strokeOpacity: 0.6,
            display: ""
        };
        lookup[OpenLayers.State.INSERT] = {
            fillColor: "violet",
            strokeColor: "violet"
        };
        lookup["vertex"] = OpenLayers.Feature.Vector.style.select;
        styleMap.addUniqueValueRules("default", "state", lookup, context);
        return styleMap;
    },

    /**
     * Method: tearDown
     * Hide vector layer
     */
    tearDown: function() {
        // deactivate controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.deactivate();
        });
        // remove & destroy vector layer
        this.layer.setVisibility(false);
    },

    /**
     * Method: setUp
     * Show vector layer
     */
    setUp: function() {
        // activate controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.activate();
        });
        // remove & destroy vector layer
        this.layer.setVisibility(true);
    },

    /**
     * Method: destroy
     * Remove vector layer on panel destroy
     */
    destroy: function() {
        // deactivate & remove controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.deactivate();
            this.map.removeControl(control);
        });
        // remove & destroy vector layer
        this.layer.destroy();
        GEOR.Editing.LayerEditingPanel.superclass.destroy.apply(this, arguments);
    }

});

Ext.reg("geor_layereditingpanel", GEOR.Editing.LayerEditingPanel);
