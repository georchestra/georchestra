/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler");

/**
 * Class: Styler.Slider
 * Slider which supports vertical or horizontal orientation, keyboard adjustments, 
 *     configurable snapping, axis clicking and animation. Can be added as an
 *     item to any container.
 *     
 * Extends:
 *  - Ext.BoxComponent
 */
Styler.MultiSlider = Ext.extend(Ext.BoxComponent, {
    /**
     * Property: values
     * {Array(Number)} The values to initialize the slider with. By default
     *     the slider is initialized with values distributed between minValue
     *     and maxValue (inclusive).
     */
    
    /**
     * Property: count
     * {Number} The number of thumbs for the slider.  If <values> is provided
     *     count will equal the number of items in the values array.  Default
     *     is 2.
     */
    count: 2,

    /**
     * Property: vertical
     * {Boolean} Orient the Slider vertically rather than horizontally.  Default
     *     is false.
     */
    vertical: false,

    /**
     * Property: minValue
     * {Number} The minimum value for the Slider. Defaults to 0.
     */
    minValue: 0,

    /**
     * Property: maxValue
     * {Number} The maximum value for the Slider. Defaults to 100.
     */    
    maxValue: 100,

    /**
     * Property: increment
     * {Number} How many units to change the slider when adjusting by drag and
     *     drop. Use this option to enable 'snapping'.
     */
    increment: 0,

    // private
    clickRange: [5, 15],

    /**
     * Property: clickToChange
     * {Boolean} Determines whether or not clicking on the Slider axis will
     *     change the slider. Defaults to true
     */
    clickToChange: true,

    /**
     * Property: animage
     * {Boolean} Turn on or off animation. Defaults to true
     */
    animate: true,

    /**
     * Property: dragging
     * {Boolean} True while the thumb is in a drag operation
     */
    dragging: false,

    // private override
    initComponent : function() {
        if(this.values === undefined) {
            this.values = new Array(this.count);
            this.values[0] = this.minValue;
            if(this.count > 1) {
                var delta = (this.maxValue - this.minValue) / (this.count - 1);
                for(var i=1; i<this.count; ++i) {
                    this.values[i] = this.minValue + (i * delta);
                }
            }
        } else {
            this.count = this.values.length;
        }
        Styler.MultiSlider.superclass.initComponent.call(this);
        this.addEvents(
            /**
             * @event beforechange
             * Fires before the slider value is changed. By returning false from an event handler, 
             * you can cancel the event and prevent the slider from changing.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Number} newValues The new values which the slider is being changed to.
             * @param {Number} oldValues The old values which the slider was previously.
             */        
            'beforechange', 
 
            /**
             * @event change
             * Fires when the slider values are changed.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Number} newValues The new values which the slider has been changed to.
             */
            'change',
 
            /**
             * @event changecomplete
             * Fires when the slider value is changed by the user and any drag operations have completed.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Number} newValues The new value which the slider has been changed to.
             */
            'changecomplete',
 
            /**
             * @event dragstart
             * Fires after a drag operation has started.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Ext.EventObject} e The event fired from Ext.dd.DragTracker
             */
            'dragstart', 
 
            /**
             * @event drag
             * Fires continuously during the drag operation while the mouse is moving.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Ext.EventObject} e The event fired from Ext.dd.DragTracker
             */
            'drag', 

            /**
             * @event dragend
             * Fires after the drag operation has completed.
             * @param {Styler.MultiSlider} slider The slider
             * @param {Ext.EventObject} e The event fired from Ext.dd.DragTracker
             */
            'dragend'
        );

        if(this.vertical) {
            Ext.apply(this, Styler.MultiSlider.Vertical);
        }
    },

    // private override
    onRender : function() {
        var autoThumbs = new Array(this.count);
        for(var i=0; i<this.count; ++i) {
            autoThumbs[i] = {cls: "x-slider-thumb " + "x-slider-thumb" + i};
        }
        this.autoEl = {
            cls: 'x-slider ' + (this.vertical ? 'x-slider-vert' : 'x-slider-horz'),
            cn: {
                cls:'x-slider-end',
                cn: {
                    cls:'x-slider-inner',
                    cn: autoThumbs
                }
            }
        };
        Styler.MultiSlider.superclass.onRender.apply(this, arguments);
        this.endEl = this.el.first();
        this.innerEl = this.endEl.first();
        // this must be easier
        this.thumbs = new Array(this.count);
        for(var i=0; i<this.count; ++i) {
            this.thumbs[i] = new Ext.Element(this.innerEl.dom.childNodes[i]);
        }
        this.halfThumb = (this.vertical ? this.thumbs[0].getHeight() : this.thumbs[0].getWidth())/2;
        this.initEvents();
    },

    // private override
    initEvents: function() {
        for(var i=0, len=this.thumbs.length; i<len; ++i) {
            this.thumbs[i].addClassOnOver('x-slider-thumb-over');
        }
        this.mon(this.el, 'mousedown', this.onMouseDown, this);
        
        this.trackers = new Array(this.count);
        for(var i=0; i<this.count; ++i) {
            this.trackers[i] = this.initTracker(i);
        }
    },
    
    initTracker: function(index) {
        var tracker = new Ext.dd.DragTracker({
            onBeforeStart: this.onBeforeDragStart.createDelegate(this, [index], true),
            onStart: this.onDragStart.createDelegate(this, [index], true),
            onDrag: this.onDrag.createDelegate(this, [index], true),
            onEnd: this.onDragEnd.createDelegate(this, [index], true),
            tolerance: 3,
            autoStart: 300
        });
        tracker.initEl(this.thumbs[index]);
        this.on('beforedestroy', tracker.destroy, tracker);
        return tracker;
    },

    // private override
    onMouseDown : function(e) {
        if(this.disabled || !this.clickToChange) {return;}
        var over = false;
        for(var i=0; i<this.count; ++i) {
            over = over || e.target == this.thumbs[i].dom;
        }
        if(!over) {
            var local = this.innerEl.translatePoints(e.getXY());
            this.onClickChange(local);
        }
    },
    
    // private
    onClickChange: function(local) {
        if(local.top > this.clickRange[0] && local.top < this.clickRange[1]) {
            var target = Math.round(this.reverseValue(local.left));
            // modify the value for the closest thumb
            var index = this.getClosestIndex(target);
            var values = this.values.slice();
            values[index] = target;
            this.setValues(values, undefined, true);
        }
    },
    
    getClosestIndex: function(target) {
        // determine which value is closest - keep looking if target is bigger
        var index = 0;
        var value = this.values[0];
        var minDiff = Math.abs(target - value);
        var diff;
        if(this.count > 1 && (target >= value)) {
            for(var i=1; i<this.count; ++i) {
                value = this.values[i];
                diff = Math.abs(target - value);
                if(diff <= minDiff) {
                    if(diff == minDiff) {
                        if(target > value) {
                            index = i;
                        }
                    } else {
                        index = i;
                        minDiff = diff;
                    }
                } else {
                    break;
                }
            }
        }
        return index;
    },

    // private
    doSnap: function(value) {
        if(!this.increment || this.increment == 1 || !value) {
            return value;
        }
        var newValue = value, inc = this.increment;
        var m = value % inc;
        if(m > 0) {
            if(m > (inc/2)) {
                newValue = value + (inc-m);
            } else {
                newValue = value - m;
            }
        }
        return newValue.constrain(this.minValue,  this.maxValue);
    },
    
    // private
    afterRender: function() {
        Styler.MultiSlider.superclass.afterRender.apply(this, arguments);
        if(this.values !== undefined) {
            var v, value, changed;
            var newValues = this.values.slice();
            for(var i=0; i<this.count; ++i) {
                value = newValues[i];
                v = this.normalizeValue(value);
                if(v !== value) {
                    newValues[i] = v;
                    changed = true;
                }
            }
            if(changed) {
                this.setValues(newValues, false);
            } else {
                this.moveThumbs(this.translateValues(this.values), false);
            }
        }
    },

    // private
    getRatio: function(){
        var w = this.innerEl.getWidth();
        var v = this.maxValue - this.minValue;
        return v == 0 ? w : (w/v);
    },

    // private
    normalizeValue: function(v) {
        if(typeof v != 'number') {
            v = parseInt(v);
        }
        v = Math.round(v);
        v = this.doSnap(v);
        v = v.constrain(this.minValue, this.maxValue);
        return v;
    },

    /**
     * Programmatically sets the values of the Slider.  Ensures that the values
     *     are constrained within the minValue and maxValue.  In addition, it
     *     ensures that values are ordered.  If one value exceeds any subsequent
     *     values, all subsequent values will be increased to meet the first
     *     value.
     * 
     * Parameters:
     * values - {Array(Number)} The values to set the slider to.
     * animate - {Boolean} Turn on or off animation, defaults to true
     */
    setValues: function(values, animate, changeComplete) {
        // normalize all values
        var changed = false;
        for(var i=0; i<this.count; ++i) {
            values[i] = this.normalizeValue(values[i]);
            if(values[i] !== this.values[i]) {
                changed = true;
            }
        }
        var changed = (values[this.count-1] !== this.values[this.count-1]);
        // force ascending order
        // allow thumbs to slide others to lower values
        if(this.count > 1) {
            var next, current;
            for(var i=this.count-2; i>=0; --i) {
                next = values[i+1];
                current = values[i];
                if(current > next) {
                    values[i] = next;
                }
                changed = changed || (values[i] !== this.values);
            }
        }
        
        if(changed && this.fireEvent('beforechange', this, values, this.values) !== false) {
            this.values = values;
            this.moveThumbs(this.translateValues(values), animate !== false);
            this.fireEvent('change', this, values);
            if(changeComplete){
                this.fireEvent('changecomplete', this, values);
            }
        }
    },

    // private
    translateValues: function(values) {
        var ratio = this.getRatio();
        var len = values.length;
        var newValues = new Array(len);
        for(var i=0; i<len; ++i) {
            newValues[i] = (values[i] * ratio) - (this.minValue * ratio) - this.halfThumb;
        }
        return newValues;
    },

    reverseValue: function(pos) {
        var ratio = this.getRatio();
        return (pos + this.halfThumb + (this.minValue * ratio)) / ratio;
    },

    // private
    moveThumbs: function(values, animate) {
        if(!animate || this.animate === false) {
            for(var i=0; i<this.count; ++i) {
                this.thumbs[i].setLeft(values[i]);
            }
        } else {
            for(var i=0; i<this.count; ++i) {
                this.thumbs[i].shift({left: values[i], stopFx: true, duration:.35});
            }
        }
    },

    // private
    onBeforeDragStart: function(e, index) {
        return !this.disabled;
    },

    // private
    onDragStart: function(e, index) {
        this.thumbs[index].addClass('x-slider-thumb-drag');
        this.dragging = true;
        this.dragStartValue = this.values[index];
        this.fireEvent('dragstart', this, index, e);
    },

    // private
    onDrag: function(e, index) {
        var pos = this.innerEl.translatePoints(this.trackers[index].getXY());
        var newValues = this.values.slice();
        newValues[index] = Math.round(this.reverseValue(pos.left));
        this.setValues(newValues, false);
        this.fireEvent('drag', this, index, e);
    },
    
    // private
    onDragEnd: function(e, index) {
        this.thumbs[index].removeClass('x-slider-thumb-drag');
        this.dragging = false;
        this.fireEvent('dragend', this, index, e);
        if(this.dragStartValue != this.values[index]) {
            this.fireEvent('changecomplete', this, this.values);
        }
    },

    // private
    onResize: function(w, h) {
        this.innerEl.setWidth(w - (this.el.getPadding('l') + this.endEl.getPadding('r')));
        this.syncThumbs();
    },
    
    /**
     * Synchronizes the thumb position to the proper proportion of the total component width based
     * on the current slider {@link #value}.  This will be called automatically when the Slider
     * is resized by a layout, but if it is rendered auto width, this method can be called from
     * another resize handler to sync the Slider if necessary.
     */
    syncThumbs: function() {
        if(this.rendered) {
            this.moveThumbs(this.translateValues(this.values));
        }
    },
    
    /**
     * Returns the current value of the slider
     * @return {Number} The current value of the slider
     */
    getValues: function() {
        return this.values;
    }
});
Ext.reg('gx_multislider', Styler.MultiSlider);

// private class to support vertical sliders
Styler.MultiSlider.Vertical = {
    onResize : function(w, h){
        this.innerEl.setHeight(h - (this.el.getPadding('t') + this.endEl.getPadding('b')));
        this.syncThumbs();
    },

    getRatio : function(){
        var h = this.innerEl.getHeight();
        var v = this.maxValue - this.minValue;
        return h/v;
    },

    moveThumbs: function(values, animate) {
        if(!animate || this.animate === false){
            for(var i=0; i<this.count; ++i) {
                this.thumbs[i].setBottom(values[i]);
            }
        } else {
            for(var i=0; i<this.count; ++i) {            
                this.thumbs[i].shift({bottom: values[i], stopFx: true, duration:.35});
            }
        }
    },

    onDrag: function(e, index) {
        var pos = this.innerEl.translatePoints(this.trackers[index].getXY());
        var bottom = this.innerEl.getHeight()-pos.top;
        var newValues = this.values.slice();
        newValues[index] = Math.round(bottom/this.getRatio());
        this.setValues(newValues, false);
        this.fireEvent('drag', this, index, e);
    },

    onClickChange: function(local) {
        if(local.left > this.clickRange[0] && local.left < this.clickRange[1]) {
            var bottom = this.innerEl.getHeight()-local.top;
            var target = Math.round(bottom/this.getRatio());
            // modify the value for the closest thumb
            var index = this.getClosestIndex(target);
            var values = this.values.slice();
            values[index] = target;
            this.setValues(values, undefined, true);
        }
    }
};

