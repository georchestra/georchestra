/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[FeatureSelectionModel]
 *  Feature Selection Model
 *  ---------------------
 *  A row selection model which enables automatic selection and hovering 
 *      of features in the map when rows are selected or hovered in the grid 
 *      and vice-versa.
 */


Ext.ns('GeoExt.ux');

/**
 * @requires GeoExt/widgets/grid/FeatureSelectionModel.js
 */

/** api: constructor
 *  .. class:: FeatureSelectionModel
 *
 *      A row selection model which enables automatic selection and hovering 
 *      of features in the map when rows are selected or hovered in the grid 
 *      and vice-versa.
 */
GeoExt.ux.FeatureSelectionModel = Ext.extend(GeoExt.grid.FeatureSelectionModel, {
    hoverControl: null,
    boundHover: false,
    controlMode: 'select',
    initComponent: function(){
        var config = {
            id: 'terrestris_featselmod'
        };
        
        Ext.apply(this, Ext.apply(this.initialConfig, config));
        
        Terrestris.FeatureSelectionModel.superclass.initComponent.apply(this, arguments);
    },
    /** private */
    constructor: function(config){
        config = config || {};
        if (config.selectControl instanceof OpenLayers.Control.SelectFeature) {
            if (!config.singleSelect) {
                var ctrl = config.selectControl;
                config.singleSelect = !(ctrl.multiple || !!ctrl.multipleKey);
            }
        }
        else 
            if (config.layer instanceof OpenLayers.Layer.Vector) {
                this.selectControl = this.createSelectControl(config.layer, config.selectControl);
                delete config.layer;
                delete config.selectControl;
            }
        
        if (config.hoverControl instanceof OpenLayers.Control.SelectFeature) {
            if (!config.singleSelect) {
                var ctrl = config.hoverControl;
                config.singleSelect = !(ctrl.multiple || !!ctrl.multipleKey);
            }
        }
        else 
            if (config.layer instanceof OpenLayers.Layer.Vector) {
                this.hoverControl = this.createHoverControl(config.layer, config.hoverControl);
                delete config.layer;
                delete config.hoverControl;
            }
        this.superclass = arguments.callee.superclass;
        this.superclass.constructor.call(this, config);
    },
        
    /** private: method[initEvents]
     *
     *  Called after this.grid is defined
     */
    initEvents: function(){
        this.superclass.initEvents.call(this);
        if (this.layerFromStore) {
            var layer = this.grid.getStore() && this.grid.getStore().layer;
            if (layer &&
            !(this.selectControl instanceof OpenLayers.Control.SelectFeature)) {
                this.selectControl = this.createSelectControl(layer, this.selectControl);
            }
            if (layer &&
            !(this.hoverControl instanceof OpenLayers.Control.SelectFeature)) {
                this.hoverControl = this.createHoverControl(layer, this.hoverControl);
            }
        }
        if (this.controlMode === 'select' || this.controlMode === 'both') {
            if (this.selectControl) {
                this.bind(this.selectControl);
            }
        }
        if (this.controlMode === 'hover' || this.controlMode === 'both') {
            if (this.hoverControl) {
                this.bindHover(this.hoverControl);
            }
        }
        
    },
    
    /** private: createHoverControl
     *  :param layer: ``OpenLayers.Layer.Vector`` The vector layer.
     *  :param config: ``Object`` The select feature control config.
     *
     *  Create the select feature control for hovering.
     */
    createHoverControl: function(layer, config){
        config = config || {};
        var singleSelect = config.singleSelect !== undefined ? config.singleSelect : this.singleSelect;
        config = OpenLayers.Util.extend({
            toggle: true,
            multipleKey: singleSelect ? null : (Ext.isMac ? "metaKey" : "ctrlKey"),
            hover: true,
            highlightOnly: true
        }, config);
        var hoverControl = new OpenLayers.Control.SelectFeature(layer, config);
        layer.map.addControl(hoverControl);
        return hoverControl;
    },
    
    /** api: method[bindHover]
     *
     *  :param obj: ``OpenLayers.Layer.Vector`` or
     *      ``OpenLayers.Control.SelectFeature`` The object this selection model
     *      should be bound to, either a vector layer or a select feature
     *      control.
     *  :param options: ``Object`` An object with a "controlConfig"
     *      property referencing the configuration object to pass to the
     *      ``OpenLayers.Control.SelectFeature`` constructor.
     *  :return: ``OpenLayers.Control.SelectFeature`` The select feature
     *      control this selection model uses.
     *
     *  Bind the hover model to a layer or a SelectFeature control (hover).
     */
    bindHover: function(obj, options){
        if (!this.boundHover) {
            options = options || {};
            this.hoverControl = obj;
            if (obj instanceof OpenLayers.Layer.Vector) {
                this.hoverControl = this.createHoverControl(obj, options.controlConfig);
            }
            if (this.autoActivateControl) {
                this.hoverControl.activate();
            }
            this.hoverControl.events.on({
                featurehighlighted: this.featureHighlighted,
                featureunhighlighted: this.featureUnhighlighted,
                scope: this
            });
            this.grid.on("rowmouseenter", this.rowHighlighted, this);
            this.grid.on("rowmouseleave", this.rowUnhighlighted, this);
            this.boundHover = true;
        }
        return this.hoverControl;
    },
    
    /** api: method[unbindHover]
     *  :return: ``OpenLayers.Control.SelectFeature`` The hover feature
     *      control this hover model used.
     *
     *  Unbind the selection model from the layer or SelectFeature control.
     */
    unbindHover: function(){
        var hoverControl = this.hoverControl;
        if (this.boundHover) {
            var layers = this.getLayers();
            for (var i = 0, len = layers.length; i < len; i++) {
                layers[i].events.un({
                    featurehighlighted: this.featureHighlighted,
                    featureunhighlighted: this.featureUnhighlighted,
                    scope: this
                });
            }
            this.un("rowmouseenter", this.rowHighlighted, this);
            this.un("rowmouseleave", this.rowUnhighlighted, this);
            if (this.autoActivateControl) {
                hoverControl.deactivate();
            }
            this.hoverControl = null;
            this.boundHover = false;
        }
        return hoverControl;
    },
    
    /** private: method[featureHighlighted]
     *  :param evt: ``Object`` An object with a feature property referencing
     *                         the highlighted feature.
     */
    featureHighlighted: function(evt){
    
        if (!this._selecting) {
            var store = this.grid.store;
            var row = store.findBy(function(record, id){
                return record.getFeature() == evt.feature;
            });
            if (row != -1 && !this.isSelected(row)) {
                this._selecting = true;
                this.selectRow(row, !this.singleSelect);
                this._selecting = false;
                // focus the row in the grid to ensure it is visible
                this.grid.getView().focusRow(row);
            }
        }
    },
    
    /** private: method[featureUnhighlighted]
     *  :param evt: ``Object`` An object with a feature property referencing
     *                         the unhighlighted feature.
     */
    featureUnhighlighted: function(evt){
    
        if (!this._selecting) {
            var store = this.grid.store;
            var row = store.findBy(function(record, id){
                return record.getFeature() == evt.feature;
            });
            if (row != -1 && this.isSelected(row)) {
                this._selecting = true;
                this.deselectRow(row);
                this._selecting = false;
                this.grid.getView().focusRow(row);
            }
        }
    },
    /** private: method[rowHighlighted]
     *  :param grid: ``Ext.grid.GridPanel`` The bound grid with the features.
     *  :param row: ``Integer`` The row index.
     */
    rowHighlighted: function(grid, row){
        var feature = grid.getStore().getAt(row).getFeature();
        if (!this._selecting && feature) {
            var layers = this.getLayers();
            for (var i = 0, len = layers.length; i < len; i++) {
                if (layers[i].selectedFeatures.indexOf(feature) == -1) {
                    this._selecting = true;
                    this.hoverControl.highlight(feature);
                    this._selecting = false;
                    break;
                }
            }
        }
    },

    /** private: method[rowUnhighlighted]
     *  :param model: ``Ext.grid.GridPanel`` The bound grid with the features.
     *  :param row: ``Integer`` The row index.
     */
    rowUnhighlighted: function(grid, row){
        var feature = grid.getStore().getAt(row).getFeature();
        if (!this._selecting && feature) {
            var layers = this.getLayers();
            for (var i = 0, len = layers.length; i < len; i++) {
                if (layers[i].selectedFeatures.indexOf(feature) == -1) {
                    this._selecting = true;
                    this.hoverControl.unhighlight(feature);
                    this._selecting = false;
                    break;
                }
            }
        }
    }
    
});

Ext.reg('gxux_featureselectionmodel', GeoExt.ux.FeatureSelectionModel);
