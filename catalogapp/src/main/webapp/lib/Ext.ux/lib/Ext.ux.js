/*
 * The code in this file is based on code taken from OpenLayers.
 *
 * Copyright (c) 2006-2007 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license.
 */

(function() {

    /**
     * Check to see if Ext.ux.singleFile is true. It is true if the
     * Ext.ux/SingleFile.js is included before this one, as it is
     * the case in single file builds.
     */
    var singleFile = (typeof Ext.ux == "object" && Ext.ux.singleFile);

    /**
     * The relative path of this script.
     */
    var scriptName = singleFile ? "Ext.ux.js" : "lib/Ext.ux.js";

    /**
     * Function returning the path of this script.
     */
    var getScriptLocation = function() {
        var scriptLocation = "";
        var scripts = document.getElementsByTagName('script');
        for(var i=0, len=scripts.length; i<len; i++) {
            var src = scripts[i].getAttribute('src');
            if(src) {
                var index = src.lastIndexOf(scriptName);
                // set path length for src up to a query string
                var pathLength = src.lastIndexOf('?');
                if(pathLength < 0) {
                    pathLength = src.length;
                }
                // is it found, at the end of the URL?
                if((index > -1) && (index + scriptName.length == pathLength)) {
                    scriptLocation = src.slice(0, pathLength - scriptName.length);
                    break;
                }
            }
        }
        return scriptLocation;
    };

    /**
     * If Ext.ux.singleFile is false then the JavaScript files in the jsfiles
     * array are autoloaded.
     */
    if(!singleFile) {
        var jsfiles = new Array(
            "Ext.ux/widgets/tree/TreeRecordNode.js",
            "Ext.ux/widgets/tree/TreeStoreNode.js",
            "Ext.ux/widgets/tree/XmlTreeLoader.js",
            "Ext.ux/widgets/spinner/Spinner.js",
            "Ext.ux/widgets/spinner/SpinnerStrategy.js",
            "Ext.ux/widgets/spinner/NumberSpinner.js",
            "Ext.ux/widgets/colorpicker/ColorPicker.js",
            "Ext.ux/widgets/colorpicker/ColorMenu.js",
            "Ext.ux/widgets/colorpicker/ColorPickerField.js",
            "Ext.ux/widgets/palettecombobox/PaletteComboBox.js"
        );

        var agent = navigator.userAgent;
        var allScriptTags = new Array(jsfiles.length);
        var host = getScriptLocation() + "lib/";
        for (var i=0, len=jsfiles.length; i<len; i++) {
            allScriptTags[i] = "<script src='" + host + jsfiles[i] +
                "'></script>";
        }
        document.write(allScriptTags.join(""));
    }
})();
