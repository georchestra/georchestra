Ext.namespace("GEOR");

GEOR.cadastre = (function () {

    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;
	var title = "Cadastre";
	var abstract = "Localiser une parcelle cadastrale en fonction d'une commune et d'une section";
    var parcelLayer = null;
    var animationTimer = null;
    var loop = null;


    /**
     * Property: config
     *{Object} Hash of options,.
     */

    var config = null;

    var mask_loader = null;

    var communes = null;
    
    var communesRequestType = null;

    var sections = null;

    var parcelles = null;



    var requestFailure = function (response) {
            alert(response.responseText);
            mask_loader.hide();
        };

    var getCommunes = function () {
            mask_loader.show();
            if (communesRequestType === "file") {
                OpenLayers.Request.GET({
                    url: 'app/addons/cadastre/communes.json',
                    failure: requestFailure,
                    success: getCommunesSuccess
                });
            } else {
                var postRequest = '<wfs:GetFeature service="WFS" version="1.0.0"' + ' outputFormat="json"' + ' xmlns:topp="http://www.openplans.org/topp"' + ' xmlns:wfs="http://www.opengis.net/wfs"' + ' xmlns:ogc="http://www.opengis.net/ogc"' + ' xmlns:gml="http://www.opengis.net/gml"' + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' + ' xsi:schemaLocation="http://www.opengis.net/wfs' + ' http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">' + ' <wfs:Query typeName="' + config.communes.typename + '">' + ' <ogc:PropertyName>' + config.communes.idfield + '</ogc:PropertyName> ' + ' <ogc:PropertyName>' + config.communes.labelfield + '</ogc:PropertyName>' + ' </wfs:Query>' + ' </wfs:GetFeature>';

                var request = OpenLayers.Request.issue({
                    method: 'POST',
                    headers: {
                        "Content-Type": "text/xml"
                    },
                    url: config.communes.wfsurl,
                    data: postRequest,
                    failure: requestFailure,
                    success: getCommunesSuccess
                });
            }
        };
        
    

    var getSections = function () {
            mask_loader.show();
            sections.removeAll();
            Ext.getCmp('cbsection').setValue(null);
            Ext.getCmp('cbparc').setValue(null);
            var recCommune = Ext.getCmp('cbcom').getValue();
            var postRequest = '<wfs:GetFeature service="WFS" version="1.0.0"' + ' outputFormat="json"' + ' xmlns:topp="http://www.openplans.org/topp"' + ' xmlns:wfs="http://www.opengis.net/wfs"' + ' xmlns:ogc="http://www.opengis.net/ogc"' + ' xmlns:gml="http://www.opengis.net/gml"' + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' + ' xsi:schemaLocation="http://www.opengis.net/wfs' + ' http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">' + ' <wfs:Query typeName="' + config.sections.typename + '">' + ' <ogc:PropertyName>' + config.sections.labelfield + '</ogc:PropertyName> ' + ' <ogc:Filter>' + '<ogc:PropertyIsEqualTo>' + '<ogc:PropertyName>' + config.sections.criterefield + '</ogc:PropertyName>' + '			<ogc:Literal>' + recCommune + '</ogc:Literal>' + '		</ogc:PropertyIsEqualTo>' + ' </ogc:Filter>' + ' </wfs:Query>' + ' </wfs:GetFeature>';

            var request = OpenLayers.Request.issue({
                method: 'POST',
                headers: {
                    "Content-Type": "text/xml"
                },
                url: config.sections.wfsurl,
                data: postRequest,
                failure: requestFailure,
                success: getSectionsSuccess
            });
        };


    var getCommunesSuccess = function (response) {
            var sb = Ext.getCmp('my-status');
            var obj = JSON.parse(response.responseText);
            communes.loadData(obj.features);
            mask_loader.hide();
            sb.setStatus({
                text: 'Sélectionnez une commune...',
                iconCls: 'x-status-valid',
                clear: true // auto-clear after a set interval
            });

        };

    var getSectionsSuccess = function (response) {
            var sb = Ext.getCmp('my-status');
            var obj = JSON.parse(response.responseText);
            sections.loadData(obj.features);
            mask_loader.hide();
            if (obj.features.length > 0) {
                sb.setStatus({
                    text: 'Sélectionnez une section...',
                    iconCls: 'x-status-valid',
                    clear: true // auto-clear after a set interval
                });
            }
        };

    var getParcelles = function () {
            mask_loader.show();
            parcelles.removeAll();
            Ext.getCmp('cbparc').setValue(null);
            var recSection = Ext.getCmp('cbsection').getValue();
            var recCommune = Ext.getCmp('cbcom').getValue();
            var postRequest = '<wfs:GetFeature service="WFS" version="1.0.0"' + ' outputFormat="json"' + ' xmlns:topp="http://www.openplans.org/topp"' + ' xmlns:wfs="http://www.opengis.net/wfs"' + ' xmlns:ogc="http://www.opengis.net/ogc"' + ' xmlns:gml="http://www.opengis.net/gml"' + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' + ' xsi:schemaLocation="http://www.opengis.net/wfs' + ' http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">' + ' <wfs:Query typeName="' + config.parcelles.typename + '">' + ' <ogc:PropertyName>' + config.parcelles.labelfield + '</ogc:PropertyName> ' + ' <ogc:Filter>' + '<ogc:And>' + '<ogc:PropertyIsEqualTo>' + '<ogc:PropertyName>' + config.parcelles.criterefield1 + '</ogc:PropertyName>' + '			<ogc:Literal>' + recCommune + '</ogc:Literal>' + '		</ogc:PropertyIsEqualTo>' + '     <ogc:PropertyIsEqualTo>' + '<ogc:PropertyName>' + config.parcelles.criterefield2 + '</ogc:PropertyName>' + '			<ogc:Literal>' + recSection + '</ogc:Literal>' + '		</ogc:PropertyIsEqualTo>' + '</ogc:And>' + '	</ogc:Filter>' + ' </wfs:Query>' + ' </wfs:GetFeature>';

            var request = OpenLayers.Request.issue({
                method: 'POST',
                headers: {
                    "Content-Type": "text/xml"
                },
                url: config.parcelles.wfsurl,
                data: postRequest,
                failure: requestFailure,
                success: getParcellesSuccess
            });
        };

    var getParcellesSuccess = function (response) {
            var sb = Ext.getCmp('my-status');
            var obj = JSON.parse(response.responseText);
            if (obj.features.length > 0) {
                parcelles.loadData(obj.features);
                sb.setStatus({
                    text: 'Sélectionnez une parcelle...',
                    iconCls: 'x-status-valid',
                    clear: true // auto-clear after a set interval
                });
            } else {
                sb.setStatus({
                    text: 'Aucune parcelle trouvée',
                    iconCls: 'x-status-error',
                    clear: true // auto-clear after a set interval
                });
            }
            mask_loader.hide();
        };


    var getParcelle = function () {
            var recParc = Ext.getCmp('cbparc').getValue();
            var sb = Ext.getCmp('my-status');
            if (recParc) {
                sb.setStatus({
                    text: 'Recherche de la parcelle',
                    iconCls: 'x-status-busy',
                    clear: false // auto-clear after a set interval
                });



                var postRequest = '<wfs:GetFeature service="WFS" version="1.0.0"' + ' outputFormat="json"' + ' xmlns:topp="http://www.openplans.org/topp"' + ' xmlns:wfs="http://www.opengis.net/wfs"' + ' xmlns:ogc="http://www.opengis.net/ogc"' + ' xmlns:gml="http://www.opengis.net/gml"' + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' + ' xsi:schemaLocation="http://www.opengis.net/wfs' + ' http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">' + ' <wfs:Query typeName="' + config.parcelles.typename + '">' + ' <ogc:Filter>' + ' <ogc:FeatureId fid="' + recParc + '"/>' + '	</ogc:Filter>' + ' </wfs:Query>' + ' </wfs:GetFeature>';

                var request = OpenLayers.Request.issue({
                    method: 'POST',
                    headers: {
                        "Content-Type": "text/xml"
                    },
                    url: config.parcelles.wfsurl,
                    data: postRequest,
                    failure: requestFailure,
                    success: getParcelleSuccess
                });
            } else {
                sb.setStatus({
                    text: 'requête incomplète',
                    iconCls: 'x-status-error',
                    clear: true // auto-clear after a set interval
                });
            }
        };

    var startAnimation = function () {
            loop = 0;
            animationTimer = window.setInterval(showhide, 0.5 * 1000);
        };

    var showhide = function () {
            loop += 1;
            if (loop < 7) {
                parcelLayer.setVisibility(!parcelLayer.visibility);
            } else {
                parcelLayer.removeAllFeatures();
                parcelLayer.setVisibility(true);
                window.clearInterval(animationTimer);
                animationTimer = null;
            }

        };

    var delParcelles = function () {
            parcelLayer.removeAllFeatures();
        };

    var showParcelle = function () {
            parcelLayer.setVisibility(true);
        };


    var getParcelleSuccess = function (response) {
            var sb = Ext.getCmp('my-status');
            var obj = JSON.parse(response.responseText);

            if (obj.features.length > 0) {
                var myextent = new OpenLayers.Bounds(obj.bbox[0], obj.bbox[1], obj.bbox[2], obj.bbox[3]);
                map.zoomToExtent(myextent);
                var geom = OpenLayers.Format.GeoJSON.prototype.parseGeometry(obj.features[0].geometry);
                var feat = new OpenLayers.Feature.Vector(geom);
                parcelLayer.addFeatures([feat]);
                if (config.animation === true) {
                    startAnimation();
                } else {
                    showParcelle();
                }

                sb.setStatus({
                    text: 'Parcelles localisée',
                    iconCls: 'x-status-valid',
                    clear: true // auto-clear after a set interval
                });
            } else {
                sb.setStatus({
                    text: 'Pas de parcelle trouvée',
                    iconCls: 'x-status-error',
                    clear: true // auto-clear after a set interval
                });
            }


        };

    var createForm = function () {


            var communesCombo = new Ext.form.ComboBox({
                fieldLabel: "Communes",
                id: 'cbcom',
                store: communes,
                valueField: config.communes.idfield,
                displayField: config.communes.labelfield,
                editable: true,
                mode: 'local',
                triggerAction: 'all',
                listeners: {
                    'select': function () {
                        getSections();
                    }
                },
                listWidth: 167
            });

            var sectionsCombo = new Ext.form.ComboBox({
                fieldLabel: "Sections",
                id: 'cbsection',
                store: sections,
                valueField: config.sections.labelfield,
                displayField: config.sections.labelfield,
                editable: true,
                mode: 'local',
                triggerAction: 'all',
                listeners: {
                    'select': function () {
                        getParcelles();
                    }
                },
                listWidth: 167
            });

            var parcellesCombo = new Ext.form.ComboBox({
                fieldLabel: "Parcelles",
                store: parcelles,
                id: 'cbparc',
                valueField: "id",
                //valueField: config.parcelles.labelfield,				
                displayField: config.parcelles.labelfield,
                editable: false,
                mode: 'local',
                triggerAction: 'all',
                listWidth: 167
            });

            var cadastreForm = new Ext.FormPanel({
                labelWidth: 100,
                layout: 'form',
                bodyStyle: 'padding: 10px',
                id: 'cadastreform',
                height: 200,
                items: [communesCombo, sectionsCombo, parcellesCombo]
            });


            return cadastreForm;
        };

    var showForm = function () {
            var form = createForm();
            var win = new Ext.Window({
                closable: true,
                width: 320,
                id: 'winCadastre',
                title: "Recherche de parcelles",
                border: false,
                plain: true,
                region: 'center',
                items: [form],
                bbar: new Ext.ux.StatusBar({
                    id: 'my-status',
                    // defaults to use when the status is cleared:
                    defaultText: '',
                    defaultIconCls: 'x-status-saved',
                    items: [{
                        text: 'Localisation',
                        handler: function () {
                            getParcelle();
                        }
                    }, {
                        text: 'effacer',
                        tooltip: 'effacer les parcelles localisées',
                        hidden: config.animation,
                        handler: function () {
                            delParcelles();
                        }
                    } ]
                })

            });
            win.render(Ext.getBody());
            win.show();
            mask_loader = new Ext.LoadMask('cadastreform', {
                msg: "Chargement..."
            });
            if (communes.data.length === 0) {
                getCommunes();

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
            parcelLayer = new OpenLayers.Layer.Vector("parcel", {
                displayInLayerSwitcher: false
            });
            parcelLayer.setZIndex(1000);
            map.addLayers([parcelLayer]);
            config = addonconfig.options;
            communesRequestType = addonconfig.options.communes.requesttype;
			if (config.title){
				title = config.title;
			}
			if (config.abstract){
				abstract = config.abstract;
			}
            if (config.proxy){
				OpenLayers.ProxyHost = config.proxy;
			}
            communes = new Ext.data.JsonStore({
                fields: [{
                    name: config.communes.idfield,
                    mapping: 'properties.' + config.communes.idfield
                }, {
                    name: config.communes.labelfield,
                    mapping: 'properties.' + config.communes.labelfield
                }],
                sortInfo: {
                    field: config.communes.labelfield,
                    direction: 'ASC'
                }
            });

            parcelles = new Ext.data.JsonStore({
                fields: [{
                    name: config.parcelles.labelfield,
                    mapping: 'properties.' + config.parcelles.labelfield
                }, {
                    name: 'id',
                    mapping: 'id'
                }],
                sortInfo: {
                    field: config.parcelles.labelfield,
                    type: 'int',
                    direction: 'ASC'
                }
            });

            sections = new Ext.data.JsonStore({
                fields: [{
                    name: config.sections.labelfield,
                    mapping: 'properties.' + config.sections.labelfield
                }],
                sortInfo: {
                    field: config.sections.labelfield,
                    direction: 'ASC'
                }
            });

            var menuitems = new Ext.menu.Item({
                text: title,				
                iconCls: 'cadastre-icon',
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
                        text: 'Rechercher une parcelle',
                        handler: function () {
                            showForm();
                        }
                    }]
                })
            });

            return menuitems;
        }
    }
})();
