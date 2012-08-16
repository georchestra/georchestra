/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler");

Styler.FeatureRenderer = Ext.extend(Ext.BoxComponent, {

    /**
     * Property: feature
     * {OpenLayers.Feature.Vector} Optional vector to be drawn.  If a feature
     *     is not provided, <symbolType> should be specified.
     */
    feature: undefined,
    
    /**
     * Property: symbolizer
     * {Object} If no symbolizer is provided, the OpenLayers default will be
     *     used.
     */
    symbolizer: OpenLayers.Feature.Vector.style["default"],

    /**
     * Property: symbolType
     * {String} One of "Point", "Line", or "Polygon".  If <feature> is
     *     provided, it will be preferred.  Default is "Point".
     */
    symbolType: "Point",

    /**
     * Property: resolution
     */
    resolution: 1,
    
    /**
     * Property: minWidth
     */
    minWidth: 20,

    /**
     * Property: minHeight
     */
    minHeight: 20,

    /**
     * Property: renderers
     * {Array(String)} List of supported Renderer classes. Add to this list to
     *     add support for additional renderers. This list is ordered:
     *     the first renderer which returns true for the  'supported()'
     *     method will be used, if not defined in the 'renderer' option.
     */
    renderers: ['SVG', 'VML', 'Canvas'],

    /**
     * Property: rendererOptions
     * {Object} Options for the renderer. See {OpenLayers.Renderer} for
     *     supported options.
     */
    rendererOptions: null,
    
    /**
     * Property: pointFeature
     * {OpenLayers.Feature.Vector} Feature with point geometry.
     */
    pointFeature: undefined,
    
    /**
     * Property: lineFeature
     * {OpenLayers.Feature.Vector} Feature with LineString geometry.  Default
     *     zig-zag is provided.
     */
    lineFeature: undefined,

    /**
     * Property: polygonFeature
     * {OpenLayers.Feature.Vector} Feature with Polygon geometry.  Default is
     *     a soft cornered rectangle.
     */
    polygonFeature: undefined,
    
    /** 
     * Property: renderer
     * {OpenLayers.Renderer}
     */
    renderer: null,

    initComponent: function() {
		var i = 0;
        Styler.FeatureRenderer.superclass.initComponent.call(this);
        Ext.applyIf(this, {
            pointFeature: new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(0, 0)
            ),
            lineFeature: new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.LineString([
                    new OpenLayers.Geometry.Point(-8, -3),
                    new OpenLayers.Geometry.Point(-3, 3),
                    new OpenLayers.Geometry.Point(3, -3),
                    new OpenLayers.Geometry.Point(8, 3)
                ])
            ),
            polygonFeature: new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon([
                    new OpenLayers.Geometry.LinearRing([
                        new OpenLayers.Geometry.Point(-8, -4),
                        new OpenLayers.Geometry.Point(-6, -6),
                        new OpenLayers.Geometry.Point(6, -6),
                        new OpenLayers.Geometry.Point(8, -4),
                        new OpenLayers.Geometry.Point(8, 4),
                        new OpenLayers.Geometry.Point(6, 6),
                        new OpenLayers.Geometry.Point(-6, 6),
                        new OpenLayers.Geometry.Point(-8, 4)
                    ])
                ])
            )
        });
        if(!this.feature) {
            this.setFeature(null, {draw: false});
        }
        this.addEvents(
            /**
             * Event: click
             * Fires when the feature is clicked on.
             *
             * Listener arguments:
             * renderer - {Styler.FeatureRenderer} This feature renderer.
             */
            "click"
        );
    },

    // private
    initClickEvents: function() {
        this.el.removeAllListeners();
        this.el.on("click", this.onClick, this);
    },
    
    onClick: function() {
        this.fireEvent("click", this);
    },

    onRender: function(ct, position) {
        this.drawFeature();
        Styler.FeatureRenderer.superclass.onRender.call(this, ct, position);
    },

    afterRender: function() {
        Styler.FeatureRenderer.superclass.afterRender.call(this);
        this.initClickEvents();
    },

    onResize: function(w, h) {
        this.setRendererDimensions();
        Styler.FeatureRenderer.superclass.onResize.call(this, w, h);
    },
    
    setRendererDimensions: function() {
        var gb = this.feature.geometry.getBounds();
        var gw = gb.getWidth();
        var gh = gb.getHeight();
        /**
         * Determine resolution based on the following rules:
         * 1) always use value specified in config
         * 2) if not specified, use max res based on width or height of element
         * 3) if no width or height, assume a resolution of 1
         */
        var resolution = this.initialConfig.resolution;
        if(!resolution) {
            resolution = Math.max(gw / this.width || 0, gh / this.height || 0) || 1;
        }
        this.resolution = resolution;
        // determine height and width of element
        var width = Math.max(this.width || this.minWidth, gw / resolution);
        var height = Math.max(this.height || this.minHeight, gh / resolution);
        // determine bounds of renderer
        var center = gb.getCenterPixel();
        var bhalfw = width * resolution / 2;
        var bhalfh = height * resolution / 2;
        var bounds = new OpenLayers.Bounds(
            center.x - bhalfw, center.y - bhalfh,
            center.x + bhalfw, center.y + bhalfh
        );
        this.renderer.setSize(new OpenLayers.Size(Math.round(width), Math.round(height)));
        this.renderer.setExtent(bounds, true);
    },

    /** 
     * Method: assignRenderer
     * Iterates through the available renderer implementations and selects 
     *     and assigns the first one whose "supported()" function returns true.
     */    
    assignRenderer: function()  {
        for(i=0, len=this.renderers.length; i<len; ++i) {
            var rendererClass = OpenLayers.Renderer[this.renderers[i]];
            if(rendererClass && rendererClass.prototype.supported()) {
                this.renderer = new rendererClass(
                    this.el, this.rendererOptions
                );
                break;
            }  
        }  
    },
    
    /**
     * APIMethod: setSymbolizer
     * Update the symbolizer used to render the feature.
     *
     * Parameters:
     * symbolizer - {Object} A symbolizer object.
     * options - {Object}
     *
     * Valid options:
     * draw - {Boolean} Draw the feature after setting it.  Default is true.
     */
    setSymbolizer: function(symbolizer, options) {
        this.symbolizer = symbolizer;
        if(!options || options.draw) {
            this.drawFeature();
        }
    },
    
    /**
     * APIMethod: setGeometryType
     * Create a new feature based on the geometry type and render it.
     *
     * Paramters:
     * type - {String} One of the FeatureRenderer constants.
     * options - {Object}
     *
     * Valid options:
     * draw - {Boolean} Draw the feature after setting it.  Default is true.
     */
    setGeometryType: function(type, options) {
        this.symbolType = type;
        this.setFeature(null, options);
    },
    
    /**
     * APIMethod: setFeature
     * Update the feature and redraw.
     *
     * Parameters:
     * feature - {OpenLayers.Feature.Vector} The feature to be rendered.  If
     *     none is provided, one will be created based on <symbolType>.
     * options - {Object}
     *
     * Valid options:
     * draw - {Boolean} Draw the feature after setting it.  Default is true.
     */
    setFeature: function(feature, options) {
        this.feature = feature || this[this.symbolType.toLowerCase() + "Feature"];
        if(!options || options.draw) {
            this.drawFeature();
        }
    },

    /**
     * Method: drawFeature
     * Render the feature with the symbolizer.
     */
    drawFeature: function() {
        if(!this.el) {
            this.el = document.createElement("div");
            this.el.id = this.getId();
        }
        if(!this.renderer || !this.renderer.supported()) {
            this.assignRenderer();
            // monkey-patch renderer so we always get a resolution
            this.renderer.map = {
                getResolution: (function() {
                    return this.resolution;
                }).createDelegate(this)
            };
        }
        this.renderer.clear();
        this.setRendererDimensions();
        this.renderer.drawFeature(this.feature,
            Ext.apply({}, this.symbolizer));
    },
    
    /**
     * Method: update
     * Update the <symbolType> or <feature> and <symbolizer>.
     *
     * Parameters:
     * options - {Object} Object with properties to be updated.  Without options
     *     <drawFeature> will still be called.
     *
     * Valid options:
     * feature - {OpenLayers.Feature.Vector} The new or updated feature.  If
     *     provided, the feature gets precedence over symbolType.
     * symbolType - {String} One of the allowed <symbolType> values.
     * symbolizer - {Object} A symbolizer object.
     */
    update: function(options) {
        options = options || {};
        if(options.feature) {
            this.setFeature(options.feature, {draw: false});
        } else if(options.symbolType) {
            this.setGeometryType(options.symbolType, {draw: false});
        }
        if(options.symbolizer) {
            this.setSymbolizer(options.symbolizer, {draw: false});
        }
        this.drawFeature();
    }
    
});

Ext.reg('gx_renderer', Styler.FeatureRenderer); 
