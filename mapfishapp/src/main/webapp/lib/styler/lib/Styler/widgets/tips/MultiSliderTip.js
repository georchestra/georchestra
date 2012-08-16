/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler");

Styler.MultiSliderTip = Ext.extend(Ext.Tip, {

    /**
     * Property: hover
     * {Boolean} Display the tip when hovering over a thumb.  If false, tip
     *     will only be displayed while dragging.  Default is true.
     */
    hover: true,
    
    /**
     * Property: dragging
     * {Boolean} A thumb is currently being dragged.
     */
    dragging: false,

    minWidth: 10,
    offsets: [0, -10],
    init: function(slider) {
        slider.on('dragstart', this.onSlide, this);
        slider.on('drag', this.onSlide, this);
        slider.on('dragend', this.hide, this);
        slider.on('destroy', this.destroy, this);
        if(this.hover) {
            slider.on('render', this.registerThumbListeners, this);
        }
        this.slider = slider;
    },
    
    registerThumbListeners: function() {
		var i = 0;
        for(i=0, len=this.slider.thumbs.length; i<len; ++i) {
            this.slider.thumbs[i].on({
                "mouseover": this.createHoverListener(i),
                "mouseout": function() {
                    if(!this.dragging) {
                        this.hide.apply(this, arguments);
                    }
                },
                scope: this
            });
        }
    },
    
    createHoverListener: function(index) {
        return (function() {
            this.onSlide(this.slider, index);
            this.dragging = false;
        }).createDelegate(this);
    },

    onSlide: function(slider, index) {
        this.dragging = true;
        this.show();
        this.body.update(this.getText(slider, index));
        this.doAutoWidth();
        this.el.alignTo(slider.thumbs[index], 'b-t?', this.offsets);
    },

    getText: function(slider, index) {
        return slider.getValues()[index];
    }
});
