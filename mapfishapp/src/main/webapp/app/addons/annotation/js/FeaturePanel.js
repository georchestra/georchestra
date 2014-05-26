/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */
Ext.namespace("GEOR");

/** api: (define)
 *  module = GEOR
 *  class = FeaturePanel
 *  base_link = `Ext.form.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.form.FormPanel>`_
 */

/**
 * @include OpenLayers/Lang.js
 * @include Ext/examples/ux/Spinner.js
 * @include Ext/examples/ux/SpinnerField.js
 * @include FeatureEditing/ux/widgets/Ext.ux.ColorField.js
 */

/** api: constructor
 *  .. class:: FeaturePanel
 */
GEOR.FeaturePanel = Ext.extend(Ext.form.FormPanel, {

    /** api: config[labelWidth]
     *  ``Number``  Default value.
     */
    labelWidth: 100,

    /** api: config[border]
     *  ``Boolean``  Default value.
     */
    border: false,

    /** api: config[bodyStyle]
     *  ``String``  Default value.
     */
    bodyStyle:'padding:5px 5px 5px 5px',

    /** api: config[width]
     *  ``String``  Default value.
     */
    width: 'auto',

    /** api: config[autoWidth]
     *  ``Boolean``  Default value.
     */
    autoWidth: true,

    /** api: config[height]
     *  ``String``  Default value.
     */
    height: 'auto',

    /** api: config[autoHeight]
     *  ``Boolean``  Default value.
     */
    autoHeight: true,

    /** api: config[defaults]
     *  ``Object``  Default value.
     */
    defaults: {width: 120},

    /** api: config[defaultType]
     *  ``String``  Default value.
     */
    defaultType: 'textfield',

    /** private: property[features]
     *  ``OpenLayers.Feature.Vector`` The feature currently being edited
     */
    features: null,

    /** api: config[layer]
     *  ``OpenLayers.Layer.Vector``
     *  The layer the features are binded to
     */
    layer: null,

    /** api: config[deleteAction]
     *  ``Ext.Action``
     *  The action created to delete the selected feature(s).
     */
    deleteAction: null,

    /** private: method[initComponent]
     */
    initComponent: function() {
        this.initFeatures(this.features);
        this.initToolbar();
        this.initMyItems();

        GEOR.FeaturePanel.superclass.initComponent.call(this);
    },

    /** private: method[initFeatures]
     *  :param features: ``Array(OpenLayers.Feature.Vector)``
     */
    initFeatures: function(features) {
        if (features instanceof Array) {
            this.features = features;
        } else {
            this.features = [features];
        }
    },

    /** private: method[initToolbar]
     *  Initialize the controls of the controler and create a toolbar from the
     *  actions created.
     */
    initToolbar: function() {
        this.initDeleteAction();

        // Add buttons and toolbar
        Ext.apply(this, {bbar: new Ext.Toolbar(this.getActions())});
    },

    /** private: method[initMyItems]
     *  Create field options and link them to the controler controls and actions
     */
    initMyItems: function() {
        var oItems, oGroup, feature, field;

        // todo : for multiple features selection support, remove this...
        if (this.features.length != 1) {
            return;
        } else {
            feature = this.features[0];
        }
        oItems = [];

        if (feature.geometry.CLASS_NAME === "OpenLayers.Geometry.Point" ) {
            if (!feature.isLabel) {
                // point size
                oItems.push({
                    xtype: 'spinnerfield',
                    name: 'pointRadius',
                    fieldLabel: OpenLayers.i18n('annotation.size'),
                    value: feature.style.pointRadius || 10,
                    width: 40,
                    minValue: 6,
                    maxValue: 20,
                    listeners: {
                        spin: function(spinner) {
                            feature.style.pointRadius = spinner.field.getValue();
                            feature.layer.drawFeature(feature);
                        }
                    }
                });
            }
        }

        if (feature.isLabel) {
            oItems.push({
                name: 'label',
                fieldLabel: OpenLayers.i18n('annotation.label'),
                value: feature.attributes.label,
                enableKeyEvents: true,
                listeners: {
                    keyup: function(field) {
                        feature.style.label = field.getValue();
                        feature.layer.drawFeature(feature);
                    }
                }
            });
        }

        if (feature.geometry.CLASS_NAME !== "OpenLayers.Geometry.LineString" &&
            !feature.isLabel) {
            var fillColor = feature.style.fillColor;
            oItems.push({
                xtype: 'compositefield',
                fieldLabel: OpenLayers.i18n('annotation.fillcolor'),
                items: [{
                    xtype: 'displayfield', value: ''
                },{
                    xtype: 'button',
                    text: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',
                    menu: {
                        xtype: 'colormenu',
                        value: fillColor.replace('#', ''),
                        listeners: {
                            select: function(menu, color) {
                                color = "#" + color;
                                menu.ownerCt.ownerCt.btnEl.setStyle("background", color);
                                feature.style.fillColor = color;
                                feature.layer.drawFeature(feature);
                            },
                            scope: this
                        }
                    },
                    listeners: {
                        render: function(button) {
                            button.btnEl.setStyle("background", fillColor);
                        }
                    }
                }]
            });
        }

        var color = feature.style[(feature.isLabel ? 'fontColor' : 'strokeColor')]
            || "#00FF00";
        var label = (
            feature.geometry.CLASS_NAME == "OpenLayers.Geometry.LineString" ||
            feature.isLabel
        ) ? 'annotation.color' : 'annotation.outlinecolor';
        oItems.push({
            xtype: 'compositefield',
            fieldLabel: OpenLayers.i18n(label),
            items: [{
                xtype: 'displayfield', value: ''
            },{
                xtype: 'button',
                text: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',
                menu: {
                    xtype: 'colormenu',
                    value: color.replace('#', ''),
                    listeners: {
                        select: function(menu, color) {
                            color = "#" + color;
                            menu.ownerCt.ownerCt.btnEl.setStyle("background", color);
                            if (feature.isLabel) {
                                feature.style.fontColor = color;
                            } else {
                                feature.style.strokeColor = color;
                            }
                            feature.layer.drawFeature(feature);
                        },
                        scope: this
                    }
                },
                listeners: {
                    render: function(button) {
                        button.btnEl.setStyle("background", color);
                    }
                }
            }]
        });

        if (feature.geometry.CLASS_NAME !== "OpenLayers.Geometry.Point" ||
            feature.isLabel) {
            // font size or stroke width
            var attribute = feature.isLabel ? 'fontSize' : 'strokeWidth';
            oItems.push({
                xtype: 'spinnerfield',
                name: 'stroke',
                fieldLabel: OpenLayers.i18n('annotation.' + attribute.toLowerCase()),
                value: feature.style[attribute] || ((feature.isLabel) ? 16 : 1),
                width: 40,
                minValue: feature.isLabel ? 8 : 1,
                maxValue: feature.isLabel ? 36 : 10,
                listeners: {
                    spin: function(spinner) {
                        var f = feature;
                        var style = {};
                        style[attribute] = spinner.field.getValue() +
                            (f.isLabel ? 'px' : '');
                        f.style = OpenLayers.Util.extend(f.style, style);
                        f.layer.drawFeature(f);
                    },
                    scope: this
                }
            });
        }

        Ext.apply(this, {items: oItems});
    },

    /** private: method[initDeleteAction]
     *  Create a Ext.Action object that is set as the deleteAction property
     *  and pushed to te actions array.
     */
    initDeleteAction: function() {
        var actionOptions = {
            handler: this.deleteFeatures,
            scope: this,
            tooltip: OpenLayers.i18n('annotation.delete_feature'),
            iconCls: "gx-featureediting-delete",
            text: OpenLayers.i18n('annotation.delete')
        };

        this.deleteAction = new Ext.Action(actionOptions);
    },

    /** private: method[deleteFeatures]
     *  Called when the deleteAction is triggered (button pressed).
     *  Destroy all features from all layers.
     */
    deleteFeatures: function() {

        Ext.MessageBox.confirm(OpenLayers.i18n('annotation.delete_feature'), OpenLayers.i18n('annotation.delete_confirm'), function(btn) {
            if (btn == 'yes') {
                for (var i = 0; i < this.features.length; i++) {
                    var feature = this.features[i];
                    if (feature.popup) {
                        feature.popup.close();
                        feature.popup = null;
                    }

                    feature.layer.destroyFeatures([feature]);
                }
            }
        },
                this);
    },

    /** private: method[getActions]
     */
    getActions: function() {
        return [this.deleteAction, '->', {
            text: OpenLayers.i18n('annotation.close'),
            handler: function() {
                this.ownerCt.close();
            },
            scope: this
        }];
    },

    /** private: method[beforeDestroy]
     */
    beforeDestroy: function() {
        delete this.feature;
    }

});
