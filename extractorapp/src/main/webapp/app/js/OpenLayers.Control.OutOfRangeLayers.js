
/**
 * @requires OpenLayers/Control.js
 */

/**
 * Class: OpenLayers.Control.OutOfRangeLayers
 * This control displays a list of layers out of range
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
OpenLayers.Control.OutOfRangeLayers = 
  OpenLayers.Class(OpenLayers.Control, {
    
    /**
     * APIProperty: seperator
     * {String} String used to seperate layers.
     */
    separator: ", ",
    
    /**
     * APIProperty: prefix
     * {String} String used to prefix layers list.
     */
    prefix: "List of layers out of range: ",
    
    /**
     * APIProperty: suffix
     * {String} String used to suffix layers list.
     */
    suffix: "",
    
    /**
     * Constructor: OpenLayers.Control.Attribution 
     * 
     * Parameters:
     * options - {Object} Options for control.
     */

    /** 
     * Method: destroy
     * Destroy control.
     */
    destroy: function() {
        this.map.events.un({
            "removelayer": this.updateText,
            "addlayer": this.updateText,
            "zoomend": this.updateText,
            scope: this
        });
        
        OpenLayers.Control.prototype.destroy.apply(this, arguments);
    },    
    
    /**
     * Method: draw
     * Initialize control.
     * 
     * Returns: 
     * {DOMElement} A reference to the DIV DOMElement containing the control
     */    
    draw: function() {
        OpenLayers.Control.prototype.draw.apply(this, arguments);
        
        this.map.events.on({
            'addlayer': this.updateText,
            'removelayer': this.updateText,
            'zoomend': this.updateText,
            scope: this
        });
        this.updateText();
        
        return this.div;    
    },

    /**
     * Method: updateText
     * Update OOR string.
     */
    updateText: function() {
        var oorLayers = [];
        if (this.map && this.map.layers) {
            var mapScale = this.map.getScale();
            for(var i=0, len=this.map.layers.length; i<len; i++) {
                var layer = this.map.layers[i];
                if (layer.getVisibility() && 
                    ((layer.minScale && mapScale > layer.minScale) || 
                    (layer.maxScale && mapScale < layer.maxScale))) {
                        
                    oorLayers.push(layer.name);
                }
            }
            this.div.innerHTML = (oorLayers.length) ?
                this.prefix + oorLayers.join(this.separator) + this.suffix :
                "";
        }
    },

    CLASS_NAME: "OpenLayers.Control.OutOfRangeLayers"
});
