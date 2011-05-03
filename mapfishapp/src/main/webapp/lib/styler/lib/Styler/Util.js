/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler.Util");

Styler.Util = {
    
    /**
     * Function: getSymbolTypeFromRule
     * Determines the symbol type of the first symbolizer of a rule that is
     * not a text symbolizer
     * 
     * Parameters:
     * rule - {OpenLayers.Rule}
     * 
     * Returns:
     * {String} "Point", "Line" or "Polygon" (or undefined if none of the
     *     three)
     */
    getSymbolTypeFromRule: function(rule){
        var symbolizer = rule.symbolizer;
        if (symbolizer["Line"] || symbolizer["Point"] || symbolizer["Polygon"]) {
            for (var type in symbolizer) {
                if (type != "Text") {
                    return type;
                }
            }
        }
    }
};
