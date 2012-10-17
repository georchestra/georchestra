Ext.namespace("GEOR");

GEOR.magnifier = (function () {

    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;

    var title = "default title";

    var abstract = "default abstract";


    /**
     * Property: config
     *{Object} Hash of options,. */

    var config = null;

    var magnifierControl = null;

    var tr = function (str) {
            return OpenLayers.i18n(str);
        };


    var showHideMagnifier = function (item, checked) {
            if (checked === true) {
                var options = {
                    mode: config.mode,
                    baseLayerConfig: {}
                };
                if (config.wmsurl && config.layer) {
                    options.baseLayerConfig = {
                        wmsurl: config.wmsurl,
                        layer: config.layer,
                        format: (config.format) ? config.format : "image/jpeg"
                    }
                }

                options.baseLayerConfig.buffer = config.buffer;
                magnifierControl = new OpenLayers.Control.Magnifier(options);
                map.addControl(magnifierControl);
                magnifierControl.update();
            } else {
                map.removeControl(magnifierControl);
            }
        };



    return {
        /*
         * Public
         */


        /**
         * APIMethod: create
         * 
         * APIMethod: create
         * Return a  {Ext.menu.Item} for GEOR_addonsmenu.js and initialize this module.
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */

        create: function (m, addonconfig) {
            map = m;
            config = addonconfig.options;
            if (config.title) {
                title = config.title;
            }
            if (config.abstract) {
                abstract = config.abstract;
            }

            return {
                text: title,
                checked: false,
                qtip: abstract,
                checkHandler: showHideMagnifier,
                listeners: {
                    afterrender: function (thisMenuItem) {
                        Ext.QuickTips.register({
                            target: thisMenuItem.getEl().getAttribute("id"),
                            title: thisMenuItem.initialConfig.text,
                            text: thisMenuItem.initialConfig.qtip
                        });
                    }
                }
            };
        }
    }
})();