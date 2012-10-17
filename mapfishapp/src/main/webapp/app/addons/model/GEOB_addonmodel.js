/*
 * @include WPS.js
 * @include OpenLayers/Layer/Markers.js
 * @include OpenLayers/Marker.js
 * @include OpenLayers/Icon.js
 * @include OpenLayers/Request.js
 * @include OpenLayers/Format/OWSCommon/v1_1_0.js 
 * 
 */
Ext.namespace("GEOR");

GEOR.addonmodel = (function () {

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

    var tr = function (str) {
            return OpenLayers.i18n(str);
        };


    var getextent = function () {
            GEOR.util.infoDialog({
                title: tr("addonmodel.extent"),
                //"Etendue de la carte",
                msg: tr("addonmodel.extent") + " : " + map.getExtent() + '\n' + tr("addonmodel.size") + " : " + map.getSize()
            });
        };

    var showoptions = function () {
            var myoptions = "option 1 : " + config.option1 + ", option 2 : " + config.option2;
            GEOR.util.infoDialog({
                title: tr("addonmodel.options"),
                //"Options",
                msg: myoptions
            });
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

        create: function (m, wpsconfig) {
            map = m;
            config = wpsconfig.options;
			if (config.title){
				title = config.title;
			}
			if (config.abstract){
				abstract = config.abstract;
			}
            var menuitems = new Ext.menu.Item({
                text: title,				
                iconCls: 'model-icon',
				qtip: abstract,
				listeners:{afterrender: function( thisMenuItem ) { 
							Ext.QuickTips.register({
								target: thisMenuItem.getEl().getAttribute("id"),
								title: thisMenuItem.initialConfig.text,
								text: thisMenuItem.initialConfig.qtip
							});
						}
				},
                menu: new Ext.menu.Menu({
                    items: [{
                        text: tr("addonmodel.extent"),
                        //'Etendue de la carte',                            
                        handler: function () {
                            getextent();
                        }
                    }, {
                        text: tr("addonmodel.options"),
                        //'Paramètres',
                        handler: function () {
                            showoptions();
                        }
                    }]
                })
            });
            return menuitems;
        }
    }
})();