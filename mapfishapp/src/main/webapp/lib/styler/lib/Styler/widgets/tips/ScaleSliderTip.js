/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/tips/SliderTip.js
 */

Ext.namespace("Styler");

/**
 * Class: Styler.ScaleSliderTip
 * A slider to control and show the current scale of a map.
 */
Styler.ScaleSliderTip = Ext.extend(Styler.SliderTip, {
    
    /**
     * Property: template
     * {String} Template for the tip. Can be customized using the following
     * keywords in curly braces:
     * - *zoom* the zoom level
     * - *resolution* the resolution
     * - *scale* the scale denominator
     */
    template: '<div>' + OpenLayers.i18n("Zoom Level: {zoom}") + '</div>' +
        '<div>' + OpenLayers.i18n("Resolution: {resolution}") + '</div>' +
        '<div>' + OpenLayers.i18n("Scale: 1 : {scale}") + '</div>',
    
    /**
     * Property: compiledTemplate
     * {Ext.Template} The template compiled from the <template> string on init.
     */
    compiledTemplate: null,
    
    /**
     * Method: init
     */
    init: function(slider) {
        this.compiledTemplate = new Ext.Template(this.template);
        Styler.ScaleSliderTip.superclass.init.call(this, slider);
    },
    
    /**
     * Method: getText
     * 
     * Parameters:
     * slider - {Ext.Slider} the slider this tip is attached to.
     */
    getText : function(slider) {
        var data = {
            zoom: slider.getZoom(),
            resolution: slider.getResolution(),
            scale: Math.round(slider.getScale()) 
        };
        return this.compiledTemplate.apply(data);
    }
});
