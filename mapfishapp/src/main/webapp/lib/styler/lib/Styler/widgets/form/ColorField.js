/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler.form");

/**
 * A text field that colors its own background based on the input value.  The
 *     value may be any one of the 16 W3C supported CSS color names
 *     (http://www.w3.org/TR/css3-color/).  The value can also be an arbitrary
 *     RGB hex value prefixed by a '#' (e.g. '#FFCC66').
 */
Styler.form.ColorField = Ext.extend(Ext.form.TextField,  {

    /**
     * Property: cssColors
     * {Object} Properties are supported CSS color names.  Values are RGB hex
     *     strings (prefixed with '#').
     */
    cssColors: {
        aqua: "#00FFFF",
        black: "#000000",
        blue: "#0000FF",
        fuchsia: "#FF00FF",
        gray: "#808080",
        green: "#008000",
        lime: "#00FF00",
        maroon: "#800000",
        navy: "#000080",
        olive: "#808000",
        purple: "#800080",
        red: "#FF0000",
        silver: "#C0C0C0",
        teal: "#008080",
        white: "#FFFFFF",
        yellow: "#FFFF00"
    },    

    initComponent: function() {
        Styler.form.ColorField.superclass.initComponent.call(this);
        
        // Add the colorField listener to color the field.
        this.on({
            valid: this.colorField,
            scope: this
        });
        
    },
    
    /**
     * Method: isDark
     * Determine if a color is dark by avaluating brightness according to the
     *     W3C suggested algorithm for calculating brightness of screen colors.
     *     http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
     *
     * Parameters:
     * hex - {String} A RGB hex color string (prefixed by '#').
     *
     * Returns:
     * {Boolean} The color is dark.
     */
    isDark: function(hex) {
        var dark = false;
        if(hex) {
            // convert hex color values to decimal
            var r = parseInt(hex.substring(1, 3), 16) / 255;
            var g = parseInt(hex.substring(3, 5), 16) / 255;
            var b = parseInt(hex.substring(5, 7), 16) / 255;
            // use w3C brightness measure
            var brightness = (r * 0.299) + (g * 0.587) + (b * 0.144);
            dark = brightness < 0.5;
        }
        return dark;
    },
    
    colorField: function() {
        var color = this.getValue();
        var hex = this.colorToHex(color) || "#ffffff";
        this.getEl().setStyle({
            "background": hex,
            "color": this.isDark(hex) ? "#ffffff" : "#000000"
        });
    },
    
    /**
     * Method: getHexValue
     * As a compliment to the field's getValue method, this method always
     *     returns the RGB hex string representation of the current value
     *     in the field (given a named color or a hex string).
     *
     * Returns:
     * {String} The RGB hex string for the field's value (prefixed with '#').
     */
    getHexValue: function() {
        return this.colorToHex(this.getValue());
    },
    
    /**
     * Method: colorToHex
     * Return the RGB hex representation of a color string.  If a CSS supported
     *     named color is supplied, the hex representation will be returned.
     *     If a non-CSS supported named color is supplied, null will be
     *     returned.  If a RGB hex string is supplied, the same will be
     *     returned.
     *
     * Returns:
     * {String} A RGB hex color string or null if none found.
     */
    colorToHex: function(color) {
        var hex;
        if(color.match(/^#[0-9a-f]{6}$/i)) {
            hex = color;
        } else {
            hex = this.cssColors[color.toLowerCase()] || null;
        }
        return hex;
    }
    
});

Ext.reg("gx_colorfield", Styler.form.ColorField);