/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt.ux
 *  class = BoundingBoxPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */

Ext.namespace("GeoExt.ux.form");

GeoExt.ux.form.BoundingBoxPanel = Ext.extend(Ext.Panel, {
    /** api: config[vectorLayer]
     *  ``OpenLayers.Layer.Vector`` A reference to a vector layer.
     */

    /** private: property[bbox]
     *  ``OpenLayers.Bounds`` Bounds of the current bbox.
     */
    bbox: null,

    /** private: property[top]
     *  ``NumberField`` The numberfield tha stores top coordinate.
     */
    top: null,

    /** private: property[bottom]
     *  ``NumberField`` The numberfield tha stores bottom coordinate.
     */
    bottom: null,

    /** private: property[right]
     *  ``NumberField`` The numberfield tha stores right coordinate.
     */
    right: null,

    /** private: property[left]
     *  ``NumberField`` The numberfield tha stores left coordinate.
     */
    left: null,

    /** api: config[defaultsOptions]
     *  ``Object`` Default options to be applied to all fields.
     */
    defaultsOptions:  {
        allowBlank: false,
        decimalPrecision: 6
    },

    /** api: config[northOptions]
     *  ``Object`` Options to be applied to top field.
     */
    northOptions: null,

    /** api: config[southOptions]
     *  ``Object`` Options to be applied to bottom field.
     */
    southOptions: null,

    /** api: config[eastOptions]
     *  ``Object`` Options to be applied to right field.
     */
    eastOptions: null,

    /** api: config[westOptions]
     *  ``Object`` Options to be applied to left field.
     */
    westOptions: null,

    /** api: config[vectorLayer]
     *  ``OpenLayers.Layer.Vector`` A reference to a vector layer.
     */
    vectorLayer: null,

    /** api: config[handleAction]
     *  ``Boolean`` Wether to generate a GeoExt.Action.
     */
    handleAction: true,

    layout: 'table',

    layoutConfig: { columns: 3 },

    defaults: {
        border: false,
        cellCls: 'bbox-panel-cell',
        width: '100%'
    },

    cls: 'bbox-panel',

    autoHeight: true,

    /** private: constructor
     */
    initComponent: function() {
        var map = null;
        var vectorLayer = null;
        if (this.map) {
            map = this.map;
            this.map = null;
        }
        if (this.vectorLayer) {
            vectorLayer = this.vectorLayer;
            this.vectorLayer = null;
        }

        this.top = new Ext.form.NumberField(
            Ext.apply({}, this.northOptions, this.defaultsOptions)
        );
        this.bottom = new Ext.form.NumberField(
            Ext.apply({}, this.southOptions, this.defaultsOptions)
        );
        this.right = new Ext.form.NumberField(
            Ext.apply({}, this.eastOptions, this.defaultsOptions)
        );
        this.left = new Ext.form.NumberField(
            Ext.apply({}, this.westOptions, this.defaultsOptions)
        );

        if (vectorLayer) {
            if (!map) {
                map = vectorLayer.map;
            }
            this.bind(vectorLayer, map);
        }
        this.top.on('change', this.onChange, this);
        this.bottom.on('change', this.onChange, this);
        this.right.on('change', this.onChange, this);
        this.left.on('change', this.onChange, this);

        this.top.on('specialkey', this.specialKeyPressed, this);
        this.bottom.on('specialkey', this.specialKeyPressed, this);
        this.right.on('specialkey', this.specialKeyPressed, this);
        this.left.on('specialkey', this.specialKeyPressed, this);
        
        if (this.handleAction) {
            this.control = new OpenLayers.Control.DrawFeature(
                this.vectorLayer,
                OpenLayers.Handler.RegularPolygon,
                {
                    handlerOptions: {
                        sides: 4,
                        irregular: true
                    }
                }
            );
            this.action = new GeoExt.Action({
                control: this.control,
                map: this.map,
                text: "Modifier cette emprise",
                tooltip: "Modifier l'emprise en en dessinant une nouvelle sur la carte",
                enableToggle: true,
                allowDepress: true
            });
            this.button = new Ext.Button(this.action);
        }

        this.items = [
            {}, this.top, {},
            this.left, this.button || {}, this.right,
            {}, this.bottom, {}
        ];

        GeoExt.ux.form.BoundingBoxPanel.superclass.initComponent.apply(this, arguments);
    },
    
    /**
     * APIMethod: getBbox
     * Get the bbox values
     */
    getBbox: function() {
        return this.bbox;
    },
    
    /**
     * APIMethod: setBbox
     * Set the bbox values
     */
    setBbox: function(bbox) {
        this.left.setValue(bbox.left),
        this.bottom.setValue(bbox.bottom),
        this.right.setValue(bbox.right),
        this.top.setValue(bbox.top)
        this.updateVectorLayer();
    },

    /**
     * Private method: bind
     * Bind this layer options panel with a vector layer.
     *
     * Parameters:
     * vectorLayer - {OpenLayers.Layer.Vector} The vector layer.
     * map - {OpenLayers.Map} The map.
     */
    bind: function(vectorLayer, map) {
        // if already bound or bad params, then abort
        if((this.vectorLayer && this.map) || !vectorLayer || !map) {
            return;
        }
        this.vectorLayer = vectorLayer;
        this.map = map;
        // initialize vectorLayer bbox with values from numberfields
        this.updateVectorLayer();
        this.vectorLayer.events.register("sketchstarted", this, this.onSketchStarted);
        this.vectorLayer.events.register("featureadded", this, this.onFeatureAdded);
        if (this.handleAction) {
            this.vectorLayer.events.register("visibilitychanged", this, this.onVisibilityChanged);
            this.map.events.register("removelayer", this, this.onRemoveLayer);
        }

    },

    /**
     * Private method: unbind
     * Unbind this layer options panel from the vector layer.
     */
    unbind: function() {
        if(this.vectorLayer) {
            this.vectorLayer.events.unregister("sketchstarted", this, this.onSketchStarted);
            this.vectorLayer.events.unregister("featureadded", this, this.onFeatureAdded);
            if (this.handleAction) {
                this.vectorLayer.events.unregister("visibilitychanged", this, this.onVisibilityChanged);
                this.map.events.unregister("removelayer", this, this.onRemoveLayer);
            }
            this.vectorLayer = null;
            this.map = null;
        }
    },

    /**
     * Private method
     * Update vector layer bbox.
     */
    updateVectorLayer: function() {
        var bounds = this.getBboxFromFields();
        if (!bounds.equals(this.bbox)) {
            if (this.vectorLayer) {
                // destroy all features
                this.vectorLayer.destroyFeatures();
                this.vectorLayer.addFeatures(
                    new OpenLayers.Feature.Vector(
                        bounds.toGeometry()
                    )
                );
            }
            this.bbox = bounds;
        }
    },

    /**
     * Private method
     * Handler of specialKey event.
     */
    specialKeyPressed: function(field, e) {
        if (e.getKey() == e.ENTER) {
            this.onChange();
        }
    },
    
    /**
     * Private method
     * Handler of change event.
     */
    onChange: function() {
        if (this.vectorLayer && !this._updating) {
            this._updating = true;
            this.updateVectorLayer();
            delete this._updating;
        }
    },

    /**
     * Private method
     * Handler of sketchstarted event.
     */
    onSketchStarted: function() {
        if (this.vectorLayer) {
            // remove all features
            this.vectorLayer.removeFeatures(this.vectorLayer.features);
        }
    },

    /**
     * Private method
     * Handler of featureadded event.
     */
    onFeatureAdded: function(evt) {
        if (!this._updating) {
            var feature = evt.feature;
            var bounds = this.getBboxFromFeature(feature);
            if (!bounds.equals(this.bbox)) {
                this._updating = true;
                this.left.setValue(bounds.left),
                this.bottom.setValue(bounds.bottom),
                this.right.setValue(bounds.right),
                this.top.setValue(bounds.top)
                this.bbox = bounds;
                delete this._updating;
            }
        }
    },

    /**
     * Private method
     */
    updateActionState: function() {
        if (this.vectorLayer.visibility == false ||
            this.vectorLayer.map != this.map) {
            this.control && this.control.deactivate();
        }
    },

    /**
     * Private method
     * Handler of visibilitychanged event.
     */
    onVisibilityChanged: function() {
        this.updateActionState();
    },

    /**
     * Private method
     * Handler of removelayer event.
     */
    onRemoveLayer: function(evt) {
        if (evt.layer == this.vectorLayer) {
            this.updateActionState();
        }
    },

    /**
     * Private method
     * Get the current bbox bounds from the numberfields.
     */
    getBboxFromFields: function() {
        var bounds = new OpenLayers.Bounds(
            this.left.getValue(),
            this.bottom.getValue(),
            this.right.getValue(),
            this.top.getValue()
        );
        return bounds;
    },

    /**
     * Private method
     * Get the current bbox bounds from the feature vector.
     */
    getBboxFromFeature: function() {
        if (this.vectorLayer &&  this.vectorLayer.features.length) {
            return this.vectorLayer.features[0].geometry.getBounds();
        }
    }

});

/** api: xtype = gxux_bboxpanel */
Ext.reg('gxux_bboxpanel', GeoExt.ux.form.BoundingBoxPanel);

