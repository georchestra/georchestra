/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler");

/**
 * Class: Styler.ScaleSlider
 */
Styler.ScaleSlider = Ext.extend(Ext.Slider, {
    
    /**
     * Property: map
     * {OpenLayers.Map} The map that this slider is connected to
     */
    map: null,
    
    // private overrides
    minValue: null,
    maxValue: null,
    
    /**
     * Property: updating
     * {Boolean} The slider position is being updated by itself (based on
     *     map zoomend).
     */
    updating: false,
    
    // private override
    initComponent: function() {
        this.minValue = this.minValue || this.map.getZoomForResolution(
            this.map.maxResolution || this.map.baseLayer.maxResolution);
        this.maxValue = this.maxValue || this.map.getZoomForResolution(
            this.map.minResolution || this.map.baseLayer.minResolution);
        Styler.ScaleSlider.superclass.initComponent.call(this);
        
        this.on({
            "changecomplete": this.changeHandler,
            scope: this
        });
        
        this.map.events.register("zoomend", this, this.update);
    },
    
    /**
     * Method: getZoom
     * Get the zoom level for the associated map based on the slider value.
     *
     * Returns:
     * {Number} The map zoom level.
     */
    getZoom: function() {
        return this.getValue();
    },
    
    /**
     * Method: getScale
     * Get the scale denominator for the associated map based on the slider value.
     *
     * Returns:
     * {Number} The map scale denominator.
     */
    getScale: function() {
        return OpenLayers.Util.getScaleFromResolution(
            this.map.getResolutionForZoom(this.getValue()),
            this.map.getUnits()
        );
    },
    
    /**
     * Method: getResolution
     * Get the resolution for the associated map based on the slider value.
     *
     * Returns:
     * {Number} The map resolution.
     */
    getResolution: function() {
        return this.map.getResolutionForZoom(this.getValue());
    },
    
    /**
     * Method: changeHandler
     * Registered as a listener for slider changecomplete.  Zooms the map.
     */
    changeHandler: function() {
        if(!this.updating) {
            this.map.zoomTo(this.getValue());
        }
    },
    
    /**
     * Method: update
     * Registered as a listener for map zoomend.  Updates the value of the slider.
     */
    update: function() {
        this.updating = true;
        this.setValue(this.map.getZoom());
        this.updating = false;
    },
    
    /**
     * Method: addToMap
     * Adds the slider to the map.
     * 
     * Parameters:
     * options - {Object} options for this method
     * 
     * Supported options:
     * - *cls* {String} CSS class to add. Default is "gx-scaleslider".
     */
    addToMap: function(options) {
        options = options || {};
        this.addClass(options.cls || "gx-scaleslider");
        this.render(this.map.viewPortDiv);
        var stopEvent = function(e) {
            e.stopEvent();
        }
        this.getEl().on({
            "mousedown": {fn: stopEvent},
            "click": {fn: stopEvent}
        });
    }
    
});
Ext.reg('gx_scaleslider', Styler.ScaleSlider);
