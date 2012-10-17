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

GEOR.wpsagrocampus = (function () {

    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;
	
	var title = "Infos MNT";
	var abstract = "R�cup�rer les informations MNT en un point d�sir�";

    /**
     * Property: url
     * String The WPS MNT instance.
     */
    var wps_url = null;

    /**
     * Property: config
     *{Object} Hash of options, with keys: pas, referentiel.
     */

    var config = null;

    var wps_identifier;

    var defControl = function () {
            OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
                defaultHandlerOptions: {
                    'single': true,
                    'double': false,
                    'pixelTolerance': 0,
                    'stopSingle': false,
                    'stopDouble': false
                },

                initialize: function (options) {
                    this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions);
                    OpenLayers.Control.prototype.initialize.apply(
                    this, arguments);
                    this.handler = new OpenLayers.Handler.Click(
                    this, {
                        'click': this.trigger
                    }, this.handlerOptions);
                },

                trigger: function (e) {
                    var lonlat = map.getLonLatFromViewPortPx(e.xy);
                    getinfosmnt(lonlat);
                }

            });
        };

    var getinfosmnt = function (lonlat) {
            click.deactivate();
            var url = wps_url;
            wps = new OpenLayers.WPS(url, {
                onSucceeded: onExecuted,
                onFailed: onError
            });
            var mntin = new OpenLayers.WPS.LiteralPut({
                identifier: "MNT Utilise",
                value: "Bretagne 50m",
                format: "Literal"
            });
            var epsgIn = new OpenLayers.WPS.LiteralPut({
                identifier: "EPSG IN",
                value: "epsg:2154 (Lambert 93)",
                format: "Literal"
            });
            var X = new OpenLayers.WPS.LiteralPut({
                identifier: "x",
                value: parseInt(lonlat.lon,10),
                format: "Literal"
            });
            var Y = new OpenLayers.WPS.LiteralPut({
                identifier: "y",
                value: parseInt(lonlat.lat,10),
                format: "Literal"
            });

            var stdout = new OpenLayers.WPS.LiteralPut({
                identifier: "stdout",
                format: "Complex"
            });

            var infomnt = new OpenLayers.WPS.Process({
                identifier: wps_identifier,
                inputs: [mntin, epsgIn, X, Y],
                outputs: [stdout]
            });
            wps.addProcess(infomnt);
            wps.execute(wps_identifier);

        };

    var onError = function (process) {
            GEOR.util.infoDialog({
                msg: "Echec dans l'execution du processus !<br>\n" + "Raison : " + process.exception.text
            });
        };

    var onExecuted = function (process) {
            alert(process.getOutput('stdout').value[0]);
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
            wps_url = config.wpsurl;
            wps_identifier = config.identifier;
			if (config.title){
				title = config.title;
			}
			if (config.abstract){
				abstract = config.abstract;
			}
            
            defControl();
            click = new OpenLayers.Control.Click();
            map.addControl(click);


            var menuitems = new Ext.menu.Item({
                text: title,
				qtip: abstract,
				listeners:{afterrender: function( thisMenuItem ) { 
							Ext.QuickTips.register({
								target: thisMenuItem.getEl().getAttribute("id"),
								title: thisMenuItem.initialConfig.text,
								text: thisMenuItem.initialConfig.qtip
							});
						}
				},
                iconCls: 'wps-infomnt',
                menu: new Ext.menu.Menu({
                    items: [{
                        iconCls: 'wps-infomnt',
                        text: 'infos MNT',
                        handler: function () {
                            click.activate();
                        }
                    }]
                })
            });
            return menuitems;
        }
    }
})();