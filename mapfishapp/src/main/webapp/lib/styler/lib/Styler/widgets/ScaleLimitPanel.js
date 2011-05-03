/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @include Styler/widgets/MultiSlider.js
 * @include Styler/widgets/tips/MultiSliderTip.js
 */

Ext.namespace("Styler");

Styler.ScaleLimitPanel = Ext.extend(Ext.Panel, {
    
    /**
     * Property: maxScaleLimit
     * {Number} Lower limit for scale denominators.  Default is what you get
     *     when you project the world in Spherical Mercator onto a single
     *     256 x 256 pixel tile and assume OpenLayers.DOTS_PER_INCH (this
     *     corresponds to zoom level 0 in Google Maps).
     */
    maxScaleLimit: 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,
    
    /**
     * Property: limitMaxScale
     * {Boolean} Limit the maximum scale denominator.  If false, no upper
     *     limit will be imposed.
     */
    limitMaxScale: true,

    /**
     * Property: maxScaleDenominator
     * {Number} The initial maximum scale denominator.  If <limitMaxScale> is
     *     true and no minScaleDenominator is provided, <maxScaleLimit> will
     *     be used.
     */
    maxScaleDenominator: undefined,

    /**
     * Property: minScaleLimit
     * {Number} Lower limit for scale denominators.  Default is what you get when
     *     you assume 20 zoom levels starting with the world in Spherical
     *     Mercator on a single 256 x 256 tile at zoom 0 where the zoom factor
     *     is 2.
     */
    minScaleLimit: Math.pow(0.5, 19) * 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,

    /**
     * Property: limitMinScale
     * {Boolean} Limit the minimum scale denominator.  If false, no lower
     *     limit will be imposed.
     */
    limitMinScale: true,

    /**
     * Property: minScaleDenominator
     * {Number} The initial minimum scale denominator.  If <limitMinScale> is
     *     true and no minScaleDenominator is provided, <minScaleLimit> will
     *     be used.
     */
    minScaleDenominator: undefined,
    
    /**
     * Property: scaleLevels
     * {Number} Number of scale levels to assume.  This is only for scaling
     *     values exponentially along the slider.  Scale values are not
     *     required to one of the discrete levels.  Default is 20.
     */
    scaleLevels: 20,
    
    /**
     * Property: scaleSliderTemplate
     * {String} Template for the tip displayed by the scale threshold slider.
     *
     * Can be customized using the following keywords in curly braces:
     * zoom - the zoom level
     * scale - the scale denominator
     * type - "Min" or "Max"
     * zoomType - "Max" or "Min" (sense is opposite type)
     *
     * Default is "{type} Scale 1:{scale}".
     */
    scaleSliderTemplate: "Echelle {type} 1:{scale}",
    
    /**
     * Method: modifyScaleTipContext
     * Called from the multi-slider tip's getText function.  The function
     *     will receive two arguments - a reference to the panel and a data
     *     object.  The data object will have scale, zoom, and type properties
     *     already calculated.  Other properties added to the data object
     *     are available to the <scaleSliderTemplate>.
     */
    modifyScaleTipContext: Ext.emptyFn,

    /**
     * Property: scaleFactor
     * {Number} Calculated base for determining exponential scaling of values
     *     for the slider.
     */
    scaleFactor: null,
    
    /**
     * Property: changing
     * {Boolean} The panel is updating itself.
     */
    changing: false,
    
    border: false,
    
    initComponent: function() {
        
        this.layout = "column";
        
        this.defaults = {
            border: false,
            bodyStyle: "margin: 0 5px;"
        };
        this.bodyStyle = {
            padding: "5px"
        };
        
        this.scaleSliderTemplate = new Ext.Template(this.scaleSliderTemplate);
        
        Ext.applyIf(this, {
            minScaleDenominator: this.minScaleLimit,
            maxScaleDenominator: this.maxScaleLimit
        });
        
        this.scaleFactor = Math.pow(
            this.maxScaleLimit / this.minScaleLimit,
            1 / (this.scaleLevels - 1)
        );
        
        this.scaleSlider = new Styler.MultiSlider({
            vertical: true,
            height: 100,
            listeners: {
                changecomplete: this.updateScaleValues,
                render: function(slider) {
                    slider.thumbs[0].setVisible(this.limitMaxScale);
                    slider.thumbs[1].setVisible(this.limitMinScale);
                    slider.setDisabled(!this.limitMinScale && !this.limitMaxScale);
                },
                scope: this
            },
            plugins: [new Styler.MultiSliderTip({
                getText: (function(slider, index) {
                    var values = slider.getValues();
                    var scales = this.sliderValuesToScale(values);
                    var data = {
                        scale: String(scales[index]),
                        zoom: (values[index] * (this.scaleLevels / 100)).toFixed(1),
                        type: (index === 0) ? "Max" : "Min",
                        zoomType: (index === 0) ? "Min" : "Max"
                    };
                    this.modifyScaleTipContext(this, data);
                    return this.scaleSliderTemplate.apply(data);
                }).createDelegate(this)
            })]
        });
        
        this.maxScaleInput = new Ext.form.TextField({
            width: 100,
            fieldLabel: "1",
            value: Math.round(this.maxScaleDenominator),
            disabled: !this.limitMaxScale,
            validationDelay: 1500, // 1.5 sec
            listeners: {
                valid: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.maxScaleLimit);
                    if(value > limit) {
                        field.setValue(limit);
                    } else if(value < this.minScaleDenominator) {
                        field.setValue(this.minScaleDenominator);
                    } else {
                        this.maxScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                scope: this
            }
        });

        this.minScaleInput = new Ext.form.TextField({
            width: 100,
            fieldLabel: "1",
            value: Math.round(this.minScaleDenominator),
            disabled: !this.limitMinScale,
            validationDelay: 1500, // 1.5 sec
            listeners: {
                valid: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.minScaleLimit);
                    if(value < limit) {
                        field.setValue(limit);
                    } else if(value > this.maxScaleDenominator) {
                        field.setValue(this.maxScaleDenominator);
                    } else {
                        this.minScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                scope: this
            }
        });
        
        this.items = [this.scaleSlider, {
            xtype: "panel",
            layout: "form",
            defaults: {border: false},
            items: [{
                labelWidth: 90,
                layout: "form",
                width: 150,
                items: [{
                    xtype: "checkbox",
                    checked: !!this.limitMinScale,
                    fieldLabel: "Échelle min",
                    listeners: {
                        check: function(box, checked) {
                            this.limitMinScale = checked;
                            var slider = this.scaleSlider;
                            var values = slider.getValues();
                            values[1] = 100;
                            slider.setValues(values);
                            slider.thumbs[1].setVisible(checked);
                            this.minScaleInput.setDisabled(!checked);
                            this.updateScaleValues(slider, values);
                            slider.setDisabled(!this.limitMinScale && !this.limitMaxScale);
                        },
                        scope: this
                    }
                }]
            }, {
                labelWidth: 10,
                layout: "form",
                items: [this.minScaleInput]
            }, {
                labelWidth: 90,
                layout: "form",
                items: [{
                    xtype: "checkbox",
                    checked: !!this.limitMaxScale,
                    fieldLabel: "Échelle max",
                    listeners: {
                        check: function(box, checked) {
                            this.limitMaxScale = checked;
                            var slider = this.scaleSlider;
                            var values = slider.getValues();
                            values[0] = 0;
                            slider.setValues(values);
                            slider.thumbs[0].setVisible(checked);
                            this.maxScaleInput.setDisabled(!checked);
                            this.updateScaleValues(slider, values);
                            slider.setDisabled(!this.limitMinScale && !this.limitMaxScale);
                        },
                        scope: this
                    }
                }]
            }, {
                labelWidth: 10,
                layout: "form",
                items: [this.maxScaleInput]
            }]
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with fill related properties
             *     updated.
             */
            "change"
        ); 

        Styler.ScaleLimitPanel.superclass.initComponent.call(this);
        
    },
    
    /**
     * Method: updateScaleValues
     */
    updateScaleValues: function(slider, values) {
        if(!this.changing) {
            var resetSlider = false;
            if(!this.limitMaxScale) {
                if(values[0] > 0) {
                    values[0] = 0;
                    resetSlider = true;
                }
            }
            if(!this.limitMinScale) {
                if(values[1] < 100) {
                    values[1] = 100;
                    resetSlider = true;
                }
            }
            if(resetSlider) {
                slider.setValues(values);
            } else {
                var scales = this.sliderValuesToScale(values);
                var max = scales[0];
                var min = scales[1];
                this.changing = true;
                this.minScaleInput.setValue(min);
                this.maxScaleInput.setValue(max);
                this.changing = false;
                this.fireEvent(
                    "change", this,
                    (this.limitMinScale) ? min : undefined,
                    (this.limitMaxScale) ? max : undefined
                );
            }
        }
    },
    
    /**
     * Method: updateSliderValues
     */
    updateSliderValues: function() {
        if(!this.changing) {
            var min = this.minScaleDenominator;
            var max = this.maxScaleDenominator;
            var values = this.scaleToSliderValues([max, min]);
            this.changing = true;
            this.scaleSlider.setValues(values);
            this.changing = false;
            this.fireEvent(
                "change", this,
                (this.limitMinScale) ? min : undefined,
                (this.limitMaxScale) ? max : undefined
            );
        }
    },

    /**
     * Method: sliderValuesToScale
     * Given two values between 0 and 100, generate the min and max scale
     *     denominators.  Assuming exponential scaling with <scaleFactor>.
     *
     * Parameters:
     * values - {Array} Values from the scale slider.
     *
     * Returns:
     * {Array} A two item array of min and max scale denominators.
     */
    sliderValuesToScale: function(values) {
        var interval = 100 / (this.scaleLevels - 1);
        return [Math.round(Math.pow(this.scaleFactor, (100 - values[0]) / interval) * this.minScaleLimit),
                Math.round(Math.pow(this.scaleFactor, (100 - values[1]) / interval) * this.minScaleLimit)];
    },
    
    /**
     * Method: scaleToSliderValues
     */
    scaleToSliderValues: function(scales) {
        var interval = 100 / (this.scaleLevels - 1);
        return [100 - (interval * Math.log(scales[0] / this.minScaleLimit) / Math.log(this.scaleFactor)),
                100 - (interval * Math.log(scales[1] / this.minScaleLimit) / Math.log(this.scaleFactor))];
    }
    
});

Ext.reg('gx_scalelimitpanel', Styler.ScaleLimitPanel); 
