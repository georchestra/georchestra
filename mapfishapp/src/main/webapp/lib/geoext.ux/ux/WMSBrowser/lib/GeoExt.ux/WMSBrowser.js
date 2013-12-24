/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/*
 * The code in this file is based on code taken from OpenLayers.
 *
 * Copyright (c) 2006-2007 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license.
 */
 
(function() {

    /**
     * The relative path of this script.
     */
    var scriptName = "lib/GeoExt.ux/WMSBrowser.js";

    /**
     * Function returning the path of this script.
     */
    var getScriptLocation = function() {
        var scriptLocation = "";
        // If we load other scripts right before GeoExt using the same
        // mechanism to add script resources dynamically (e.g. OpenLayers), 
        // document.getElementsByTagName will not find the GeoExt script tag
        // in FF2. Using document.documentElement.getElementsByTagName instead
        // works around this issue.
        var scripts = document.documentElement.getElementsByTagName('script');
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

    var jsFiles = new Array(
        "data/Store.js",
        "data/WMSBrowserWMSCapabilitiesStore.js",
        "plugins/WMSBrowserAlerts.js",
        "widgets/WMSBrowser.js",
        "widgets/WMSBrowserStatusBar.js",
        "widgets/grid/WMSBrowserGridPanel.js",
        "widgets/tree/WMSBrowserRootNode.js",
        "widgets/tree/WMSBrowserTreePanel.js"
    );

    // use "parser-inserted scripts" for guaranteed execution order
    // http://hsivonen.iki.fi/script-execution/
    var scriptTags = new Array(jsFiles.length);
    var host = getScriptLocation() + "lib/GeoExt.ux/";
    for (var i=0, len=jsFiles.length; i<len; i++) {
        scriptTags[i] = "<script src='" + host + jsFiles[i] +
                               "'></script>"; 
    }
    if (scriptTags.length > 0) {
        document.write(scriptTags.join(""));
    }

})();
