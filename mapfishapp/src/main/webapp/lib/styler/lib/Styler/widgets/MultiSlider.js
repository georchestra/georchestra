Ext.namespace("Styler");

/**
 * Class: Styler.MultiSlider
 *
 * Extends:
 *  - Ext.slider.MultiSlider
 */
Styler.MultiSlider = Ext.extend(Ext.slider.MultiSlider, {

    /**
     * @private
     * Returns the nearest thumb to a click event, along with its distance
     * @param {Object} local Object containing top and left values from a click event
     * @param {String} prop The property of local to compare on. Use 'left' for horizontal sliders, 'top' for vertical ones
     * @return {Object} The closest thumb object and its distance from the click event
     */
    getNearest: function(local, prop) {
        var localValue = prop === 'top' ? this.innerEl.getHeight() - local[prop] : local[prop],
            clickValue = this.reverseValue(localValue),
            nearestDistance = (this.maxValue - this.minValue) + 5, //add a small fudge for the end of the slider
            nearest = null,
            i = 0;

        for (i=0; i < this.thumbs.length; i++) {
            var thumb = this.thumbs[i],
                value = thumb.value,
                dist  = Math.abs(value - clickValue);

            if (Math.abs(dist <= nearestDistance)) {
                ////////////////////////////////////////////////////
                // the following override aims to avoid having a disabled thumb
                // as the nearest
                if (thumb.disabled && nearest !== null) {
                    continue;
                }
                nearest = thumb;
                // don't save the nearestDistance if thumb is disabled so that
                // it will be overriden by the first non-disabled thumb
                if (!thumb.disabled) {
                    nearestDistance = dist;
                }
                ////////////////////////////////////////////////////
            }
        }
        return nearest;
    }

});

