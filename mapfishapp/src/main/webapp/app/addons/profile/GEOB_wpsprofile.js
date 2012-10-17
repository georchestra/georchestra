/*
 * @include WPS.js
 * @include OpenLayers/Layer/Markers.js
 * @include OpenLayers/Marker.js
 * @include OpenLayers/Icon.js
 * @include OpenLayers/Request.js
 * @include OpenLayers/Format/OWSCommon/v1_1_0.js
 * @include OpenLayers/Ajax.js
 * 
 */
Ext.namespace("GEOR");

GEOR.wpsprofile = (function () {

    /*
     * Private
     */
    /**
     * Property: mask_loaders
     * Array of {Ext.LoadMask} .
     */
    var mask_loaders = [];

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;
	
	var title = "Profil en long";
	var abstract = "Boîte à outils profil en long";
    /**
     * Property: initialized
     * occurs when the wps describeProcess returns a response
     * boolean.
     */
    var initialized = false;

    /**
     * Property: url
     * String The WPS MNT instance.
     */
    var wps_url = null;

    var wps_identifier = null;

    /**
     * Property: config
     *{Object} Hash of options, with keys: pas, referentiel.
     */

    var config = null;

    /**
     * Property: colors
     *[Array] Hash of colors.
     */
    var colors = null;

    /**
     * Property: drawLayer
     * {OpenLayers.Layer.Vector}.
     */

    var drawLayer = null;

    /**
     * Property: markersLayer
     * {OpenLayers.Layer.Markers}.
     */
    var markersLayer = null;

    var resultLayer = null;

    var verticesLayer = null;

    var sf = null;

    var tr = function (str) {
            return OpenLayers.i18n(str);
        };

    /**
     * Method: describeProcess
     *
     * Parameters:
     * String url, String identifier du process WPS.
     */
    var describeProcess = function (url, identifier) {
            var wps = new OpenLayers.WPS(url, {
                onDescribedProcess: onDescribeProcess
            });
            wps.describeProcess(identifier);
        };
    /**
     * Method: onDescribeProcess
     * Callback executed when the describeProcess response
     * is received.
     *
     * Parameters:
     * process - {WPS.process}.
     */
    var onDescribeProcess = function (process) {

            var tmp1 = process.getInput("referentiel").allowedValues;
            var data1 = [];
            for (var i = 0; i < tmp1.length; i++) {
                data1.push([tmp1[i]]);
            }
            var tmp2 = process.getInput("distance").allowedValues;
            var data2 = [];
            for (var i = 0; i < tmp2.length; i++) {
                data2.push([tmp2[i]]);
            }
            config = {
                pas: {
                    value: 100,
                    title: process.getInput("distance").title,
                    allowedValues: data2
                },
                referentiel: {
                    value: 'bdalti',
                    title: process.getInput("referentiel").title,
                    allowedValues: data1
                }
            };

            Ext.getCmp("wpstooldraw").enable();
            Ext.getCmp("wpstoolparameters").enable();
            initialized = true;

        };
    /**
     * Method: createParametersForm
     * Return a Form with tool parameters
     *
     *Parameter optional jobid integer, link with a Graphic Window 
     */
    var createParametersForm = function (jobid) {
            var referentielStore = new Ext.data.SimpleStore({
                fields: [{
                    name: 'value',
                    mapping: 0
                }],
                data: config.referentiel.allowedValues
            });
            var pasStore = new Ext.data.SimpleStore({
                fields: [{
                    name: 'value',
                    mapping: 0
                }],
                data: config.pas.allowedValues
            });
            var referentielCombo = new Ext.form.ComboBox({
                name: 'referentiel',
                fieldLabel: config.referentiel.title,
                store: referentielStore,
                valueField: 'value',
                value: config.referentiel.value,
                displayField: 'value',
                editable: false,
                mode: 'local',
                triggerAction: 'all',
                listWidth: 167
            });
            var pasCombo = new Ext.form.ComboBox({
                name: 'pas',
                fieldLabel: config.pas.title,
                store: pasStore,
                valueField: 'value',
                value: config.pas.value,
                displayField: 'value',
                editable: false,
                mode: 'local',
                triggerAction: 'all',
                listWidth: 167
            });
            var configForm = new Ext.FormPanel({
                labelWidth: 100,
                layout: 'form',
                bodyStyle: 'padding: 10px',
                id: 'configform' + (jobid ? jobid : ''),
                height: 200,
                title: (jobid ? tr("addonprofile.parameters") : ''),
                defaults: {
                    width: 200
                },
                defaultType: 'textfield',
                items: [referentielCombo, pasCombo,
                {
                    xtype: 'checkbox',
                    fieldLabel: 'show vertices',
                    id: 'chkshowvertices',
                    listeners: {
                        check: function (checkbox, checked) {
                            (jobid ? showVertices(checked) : showVertices(checked));
                        }
                    }
                }],
                buttons: [{
                    text: tr('addonprofile.refresh'),
                    handler: function () {
                        (jobid ? updateChartConfig(jobid) : updateGlobalConfig());
                    }
                }]
            });

            return configForm;
        };
    /**
     * Method: showLayers
     * Affiche les layers Profil et markers    sur la carte     
     */
    var showLayers = function () {
            if (map.getLayersByName("Profil").length == 0) {
                map.addLayers([drawLayer, resultLayer, verticesLayer, markersLayer]);
            }
        };
    /**
     * Method: getGraphicHandler
     * Retourne le nombre de profils créés dans le layer drawLayer
     * La valeur retournée sert de lien avec le Graphique généré
     */
    var getGraphicHandler = function () {
            return drawLayer.features.length;
        };
    /**
     * Method: getColor
     * Retourne le code couleur à associer au profil         
     *         
     */
    var getColor = function (nprofile) {

            var color;
            switch (nprofile) {
            case 1:
            case 2:
            case 3:
                color = colors[nprofile - 1];
                break;
            default:
                color = 'FF7F50';
                break;
            }
            return color;
        };
    /**
     * Method: onNewLine
     * Callback executed when the a new Line is drawned        
     *
     * Parameters:
     * e - {OpenLayers.Layer.events}
     */
    var onNewLine = function (e) {
            var feature = e.feature;
            showLayers();
            var graphicHandler = getGraphicHandler();
            var profileColor = getColor(graphicHandler);
            feature.style = {
                pointRadius: 10,
                fillColor: "green",
                fillOpacity: 0.5,
                strokeColor: '#' + profileColor
            };
            feature.attributes = {
                profile: graphicHandler,
                color: profileColor
            };
            drawLayer.setZIndex(900);
            drawLayer.redraw();
            getprofile(feature, 'new', config);
        };
    /**
     * Method: addmarksfeatures
     * matérialise le sens de numérisation de la polyligne         *
     * Parameters:
     * graphicHandler - integer Identifiant de la fenêtre Graphique
     */
    var addmarksfeatures = function (infos, jobid) {
            var beginPoint = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(infos.infos.firstpointX, infos.infos.firstpointY));
            beginPoint.attributes = {
                profile: parseInt(jobid, 10)
            };
            beginPoint.style = {
                pointRadius: 7,
                externalGraphic: "app/addons/profile/icon-one.gif",
                graphicZIndex: 1000
            };
            var endPoint = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(infos.infos.lastpointX, infos.infos.lastpointY));
            endPoint.attributes = {
                profile: parseInt(jobid, 10)
            };
            endPoint.style = {
                pointRadius: 7,
                externalGraphic: "app/addons/profile/icon-two.jpg",
                graphicZIndex: 1000
            };
            resultLayer.addFeatures([beginPoint, endPoint]);
            resultLayer.setZIndex(902);
        };
    /**
     * Method: addGeometryResult
     *          *
     * Parameters:
     * graphicHandler - integer Identifiant de la fenêtre Graphique
     */
    var addGeometryResult = function (points, jobid) {
            var pointsCollection = []
            var profileColor = getColor(parseInt(jobid, 10));
            for (var i = 1; i < points.length - 1; i++) {
                var pt = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(points[i][1], points[i][2]));
                pt.attributes = {
                    profile: parseInt(jobid, 10),
                    distance: points[i][0],
                    elevation: points[i][3]
                };
                pt.style = {
                    pointRadius: 3,
                    fillColor: '#' + profileColor,
                    fillOpacity: 0.5,
                    strokeColor: '#' + profileColor
                };
                pointsCollection.push(pt);
            }
            verticesLayer.addFeatures(pointsCollection);
            verticesLayer.setZIndex(9000);
        };
    /**
     * Method: removedrawfeatures
     * Supprime le tracé profil correspondant au Graphique    
     *
     * Parameters:
     * graphicHandler - integer Identifiant de la fenêtre Graphique
     */
    var removedrawfeatures = function (graphicHandler) {
            var feature = drawLayer.getFeaturesByAttribute('profile', parseInt(graphicHandler, 10));
            drawLayer.removeFeatures(feature);
            removemarksfeatures(graphicHandler);
            removeverticesfeatures(graphicHandler);
            //verticesLayer.removeAllFeatures();
        };

    var removemarksfeatures = function (graphicHandler) {
            var features = resultLayer.getFeaturesByAttribute('profile', parseInt(graphicHandler, 10));
            resultLayer.removeFeatures(features);

        };

    var removeverticesfeatures = function (graphicHandler) {
            var features = verticesLayer.getFeaturesByAttribute('profile', parseInt(graphicHandler, 10));
            verticesLayer.removeFeatures(features);

        };



    var onSelectVertice = function (e) {
            alert(e.attributes.elevation);
        };

    var showVertices = function (activate) {
            verticesLayer.setVisibility(activate);
        };
    /**
     * Method: convertToGML
     * Convertit un feature au format GML    
     *
     * Parameters:
     * feature - {OpenLayers.Feature.Vector}
     */
    var convertToGML = function (feature) {
            var gmlP = new OpenLayers.Format.GML();
            var inGML = gmlP.write(feature).replace(/<\?xml.[^>]*>/, "");
            return inGML;
        };
    /**
     * Method: LoadGML
     * Charge une chaine GML dans un layer    
     *
     * Parameters:
     * gmlText - String GML.
     */
    var LoadGML = function (gmlText) {
            var features = new OpenLayers.Format.GML().read(gmlText);
            if (features.length <= 3) {
                drawLayer.addFeatures(features);
            } else {
                GEOR.util.errorDialog({
                    title: tr("addonprofile.error"),
                    msg: tr("addonprofile.error1") + " : " + features.length
                });
            }
        };

    /**
     * Method: getprofile
     * Appelle le service WPS permettant de générer le profil en long
     * Parameters:
     * feature - {OpenLayers.Feature.Vector}, option String --> 'new'|'update', optional parameters.
     */
    var getprofile = function (feature, option, parameters) {
            var url = wps_url;
            if (option == 'new') {
                wps = new OpenLayers.WPS(url, {
                    onSucceeded: onExecuted,
                    onFailed: onError
                });
            } else {
                wps = new OpenLayers.WPS(url, {
                    onSucceeded: onUpdated,
                    onFailed: onErrorUpdated
                });
            }
            var InputGML = new OpenLayers.WPS.ComplexPut({
                identifier: "data",
                value: convertToGML(feature)
            });
            var InputReferentiel = new OpenLayers.WPS.LiteralPut({
                identifier: "referentiel",
                value: parameters.referentiel.value,
                format: "Litteral"
            });
            var InputPas = new OpenLayers.WPS.LiteralPut({
                identifier: "distance",
                value: parameters.pas.value,
                format: "Litteral"
            });
            var InputFormat = new OpenLayers.WPS.LiteralPut({
                identifier: "outputformat",
                value: 'json',
                format: "Litteral"
            });
            var OutputInfos = new OpenLayers.WPS.LiteralPut({
                identifier: "resultinfos"
            });
            var OutputResult = new OpenLayers.WPS.LiteralPut({
                identifier: "result"
            });

            var profilprocess = new OpenLayers.WPS.Process({
                identifier: wps_identifier,
                inputs: [InputGML, InputReferentiel, InputPas, InputFormat],
                outputs: [OutputInfos, OutputResult]
            });

            wps.addProcess(profilprocess);
            wps.execute(wps_identifier);
        };
    /**
     * Method: convert2csv
     * Appelle le service de téléchargement csv
     * Parameters:
     * data - {JSON Data}.
     */
    var convert2csv = function (data) {
            var format = new OpenLayers.Format.JSON();
            OpenLayers.Request.POST({
                url: "ws/csv/",
                data: format.write({
                    columns: ['distance', 'x', 'y', 'altitude', 'pente'],
                    data: data
                }),
                success: function (response) {
                    var o = Ext.decode(response.responseText);
                    window.location.href = o.filepath;
                }
            });
        };
    /**
     * Method: updateupdateGlobalConfig
     * Modifie les valeurs Référentiel et pas
     * 
     */
    var updateGlobalConfig = function () {
            config.pas.value = Ext.getCmp('configform').getForm().findField('pas').getValue();
            config.referentiel.value = Ext.getCmp('configform').getForm().findField('referentiel').getValue();
            Ext.getCmp('winParameters').destroy();
        };
    /**
     * Method: updateChartConfig
     * Modifie les valeurs Référentiel et pas avant un nouvel appel du service WPS
     * Parameters:
     * jobid - integer Handler de la fenêtre.
     */
    var updateChartConfig = function (jobid) {
            //Création des messages de Loading
            var mask_loader = new Ext.LoadMask('configform' + jobid, {
                msg: tr("addonprofile.update")
            });
            mask_loaders.push({
                id: 'loaderprofil' + jobid,
                loader: mask_loader
            });
            //mask_loader.id = 'loaderprofil' + jobid;
            mask_loader.show();
            var parameters = {
                pas: {
                    value: Ext.getCmp('configform' + jobid).getForm().findField('pas').getValue()
                },
                referentiel: {
                    value: Ext.getCmp('configform' + jobid).getForm().findField('referentiel').getValue()
                }
            };
            var feature = drawLayer.getFeaturesByAttribute('profile', parseInt(jobid, 10));
            getprofile(feature, 'update', parameters);
        };
    /**
     * Method: onUpdated
     * Callback executed when the the WPS Execute (update) response is received
     * Parameters:
     * process - {WPS.Process}.
     */
    //removemarksfeatures                                    
    var onUpdated = function (process) {
            var inputfeature = new OpenLayers.Format.GML().read(process.getInput("data").value)[0];
            var jobid = inputfeature.attributes['profile'];
            removeverticesfeatures(jobid);
            removemarksfeatures(jobid);
            var obj = JSON.parse(process.getOutput('result').value);
            var infos = JSON.parse(process.getOutput('resultinfos').value);
            addGeometryResult(obj.profile.points, jobid);
            addmarksfeatures(infos, jobid);
            if (obj.profile.points.length > 0) {
                var store = new Ext.data.JsonStore({
                    fields: [{
                        name: 'd',
                        mapping: 0
                    }, {
                        name: 'x',
                        mapping: 1
                    }, {
                        name: 'y',
                        mapping: 2
                    }, {
                        name: 'z',
                        mapping: 3
                    }, {
                        name: 'pente',
                        mapping: 4
                    }]
                });
                store.loadData(obj.profile.points);
                var lineChart = Ext.getCmp('linechart' + jobid);
                var infosForm = Ext.getCmp('infosform' + jobid).getForm();
                infosForm.findField('referentiel').setValue(infos.infos.referentiel);
                infosForm.findField('distance').setValue(Math.round(infos.infos.distance) + ' m');
                infosForm.findField('denivelepositif').setValue(Math.round(infos.infos.denivelepositif) + ' m');
                infosForm.findField('denivelenegatif').setValue(Math.round(infos.infos.denivelenegatif) + ' m');
                infosForm.findField('processedpoints').setValue(infos.infos["processed points"]);
                lineChart.store = store;
                lineChart.xAxis.title = 'Distance (m)' + ' sources : (' + infos.infos.referentiel + ')';
                for (var i = 0; i < mask_loaders.length; i++) {
                    if (mask_loaders[i].id == 'loaderprofil' + jobid) mask_loaders[i].loader.hide();
                    mask_loaders.remove(mask_loaders[i]);
                    break;
                }
                Ext.getCmp("tabpanel" + jobid).setActiveTab(0);
            } else {
                alert(tr("addonprofile.impossible"));
            }
        };
    /**
     * Method: onError
     * Callback executed when the the WPS Execute Error response is received
     * Parameters:
     * process - {WPS.Process}.
     */
    var onError = function (process) {
            GEOR.util.errorDialog({
                title: tr("addonprofile.error"),
                msg: process.exception.text
            });
            var inputfeature = new OpenLayers.Format.GML().read(process.getInput("data").value)[0];
            var id = inputfeature.attributes['profile'];
            removedrawfeatures(id);
        };

    /**
     * Method: createWPSControl
     * Crée un control drawFeature de type ligne
     * Parameters:
     * handlerType - {OpenLayers.Handler.Path}, map - {OpenLayers.Map} The map instance.
     */

    var createWPSControl = function (handlerType, map) {
            var drawLineCtrl = new OpenLayers.Control.DrawFeature(drawLayer, handlerType, {
                featureAdded: function (e) {
                    drawLineCtrl.deactivate();
                }
            });
            return drawLineCtrl;
        };
    /**
     * Method: enableSelectionTool
     * 
     * Retourne true si une sélection est effectuée dans le Panel Results
     * Parameters:
     * m - {OpenLayers.Map} The map instance.
     */
    var enableSelectionTool = function (m) {
            var response = false;
            var searchLayers = m.getLayersByName("search_results");
            if (searchLayers.length == 1) {
                var selectedFeatures = searchLayers[0].selectedFeatures;
                if (selectedFeatures.length > 0) {
                    response = true;
                }
            }
            return response;
        };
    /**
         * Method: getProfileParameters
         * 
         * Retourne les valeurs des paramètres de l'outil
         
         */
    var getProfileParameters = function () {
            var form = createParametersForm();
            var win = new Ext.Window({
                closable: true,
                //width    : 320,
                id: 'winParameters',
                title: tr("addonprofile.parameterstool"),
                border: false,
                plain: true,
                region: 'center',
                items: [form]
            });
            win.render(Ext.getBody());
            win.show();
        };
    /**
     * Method: getMapFeaturesSelection
     * Créé un profil pour chaque feature sélectionnée dans le Panel Results
     * Parameters:
     * map - {OpenLayers.Map} The map instance.
     */
    var getMapFeaturesSelection = function (map) {
            var searchLayer = map.getLayersByName("search_results");
            var features = searchLayer[0].selectedFeatures;
            if (features.length > 0) {
                for (var i = 0; i < features.length; i++) {
                    if (features[i].geometry.CLASS_NAME == "OpenLayers.Geometry.MultiLineString" || features[i].geometry.CLASS_NAME == "OpenLayers.Geometry.MultiLineString") {
                        var feat = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(features[i].geometry.getVertices()));
                        drawLayer.addFeatures([feat]);
                        // Possibilité de faire un merge des features
                        // pour le moment chaque feature sélectionné génère un profil
                    } else {
                        GEOR.util.errorDialog({
                            title: tr("addonprofile.error"),
                            msg: tr("addonprofile.error2")
                        });
                    }
                }
            } else {
                GEOR.util.errorDialog({
                    title: tr("addonprofile.error"),
                    msg: tr("addonprofile.error2")
                });
            }
        };

    /**
     * Method: selectGMLFile
     * Sélectionne un fichier GML en local
     * 
     */
    var selectGMLFile = function () {
            // Check for the various File API support.
            if (window.File && window.FileReader && window.FileList) {
                //--------------
                var fileWindow;
                var fileLoadForm = new Ext.FormPanel({
                    width: 320,
                    frame: true,
                    bodyStyle: 'padding: 10px 10px 0 10px;',
                    labelWidth: 60,
                    defaults: {
                        anchor: '95%'
                    },
                    items: [{
                        xtype: 'fileuploadfield',
                        emptyText: tr("addonprofile.fileselection"),
                        fieldLabel: tr("addonprofile.file"),
                        buttonText: '...',
                        listeners: {
                            'fileselected': function (fb, v) {
                                file = fb.fileInput.dom.files[0]
                                myfilename = v;
                                var reader = new FileReader();
                                reader.onload = function (e) {
                                    var text = e.target.result;
                                    if (myfilename.search('.gml') != -1) {
                                        LoadGML(text);
                                        fileWindow.hide();
                                    } else {
                                        GEOR.util.errorDialog({
                                            title: tr("addonprofile.error"),
                                            msg: tr("addonprofile.error4")
                                        });
                                    }

                                }
                                reader.readAsText(file, "UTF-8");

                            }
                        }
                    }]
                });

                fileWindow = new Ext.Window({
                    closable: true,
                    width: 320,
                    title: tr("addonprofile.fileselection"),
                    border: false,
                    plain: true,
                    region: 'center',
                    items: [fileLoadForm]
                });

                fileWindow.render(Ext.getBody());
                fileWindow.show();



            } else {
                alert('The File APIs are not fully supported in this browser.');
            }
        };

    /**
     * Method: onErrorUpdated
     * Callback executed when the the WPS Execute Updated Error response is received
     * Parameters:
     * process - {WPS.Process}.
     */
    var onErrorUpdated = function (process) {
            GEOR.util.errorDialog({
                title: tr("addonprofile.error"),
                msg: process.exception.text
            });
            var inputfeature = new OpenLayers.Format.GML().read(process.getInput("data").value)[0];
            var jobid = inputfeature.attributes['profile'];
            for (var i = 0; i < mask_loaders.length; i++) {
                if (mask_loaders[i].id == 'loaderprofil' + jobid) mask_loaders[i].loader.hide();
                mask_loaders.remove(mask_loaders[i]);
                break;
            }

        };
    /**
     * Method: onExecuted
     * Callback executed when the the WPS Execute response is received
     * Parameters:
     * process - {WPS.Process}.
     */
    var onExecuted = function (process) {
            var inputfeature = new OpenLayers.Format.GML().read(process.getInput("data").value)[0];
            var jobid = inputfeature.attributes['profile'];
            var profileColor = parseInt('0x' + inputfeature.attributes['color'], 16);
            var obj = JSON.parse(process.getOutput('result').value);
            var infos = JSON.parse(process.getOutput('resultinfos').value);
            addGeometryResult(obj.profile.points, jobid);
            addmarksfeatures(infos, jobid);
            var longueur = infos.infos.distance;
            if (obj.profile.points.length > 0) {

                var store = new Ext.data.JsonStore({
                    //fields:['d','x','y','z','pente']
                    fields: [{
                        name: 'd',
                        mapping: 0
                    }, {
                        name: 'x',
                        mapping: 1
                    }, {
                        name: 'y',
                        mapping: 2
                    }, {
                        name: 'z',
                        mapping: 3
                    }, {
                        name: 'pente',
                        mapping: 4
                    }]
                });
                store.loadData(obj.profile.points);

                lineChart = new Ext.chart.LineChart({
                    id: 'linechart' + jobid,
                    store: store,
                    title: tr("addonprofile.chart"),
                    xField: 'd',
                    //height:200,                
                    yAxis: new Ext.chart.NumericAxis({
                        /*majorUnit: 25,
                            minimum: 0,
                            maximum: 350,*/
                        //title: 'Altitude (m)',
                        labelRenderer: Ext.util.Format.numberRenderer('0')
                    }),
                    xAxis: new Ext.chart.NumericAxis({
                        title: tr("addonprofile.distance") + " " + tr("addonprofile.sources") + " : " + '(' + infos.infos.referentiel + ')'
                        //labelRenderer : function(value) { return value/1000 ; }
                    }),
                    tipRenderer: function (chart, record) {
                        return tr("addonprofile.elevation") + " : " + record.data.z + ' m' + '\n' + tr("addonprofile.distance") + " : " + record.data.d + ' m' + '\n' + tr("addonprofile.inclination") + " : " + Ext.util.Format.number(record.data.pente, '0.0') + '%';
                    },
                    extraStyle: {
                        padding: 10,
                        animationEnabled: true,
                        yAxis: {
                            color: 0x3366cc,
                            majorTicks: {
                                color: 0x3366cc,
                                length: 4
                            },
                            minorTicks: {
                                color: 0x3366cc,
                                length: 2
                            },
                            majorGridLines: {
                                size: 1,
                                color: 0xdddddd
                            }
                        },
                        xAxis: {
                            //showLabels:false,
                            color: 0x3366cc,
                            majorTicks: {
                                color: 0x3366cc,
                                length: 4
                            },
                            minorTicks: {
                                color: 0x3366cc,
                                length: 2
                            },
                            majorGridLines: {
                                size: 1,
                                color: 0xdddddd
                            }
                        }
                    },
                    series: [{
                        type: 'line',
                        yField: 'z',
                        style: {
                            color: profileColor,
                            // couleur de  la ligne
                            lineSize: 1,
                            //taille de la ligne    
                            fillColor: profileColor,
                            // couleurs des points                                                        
                            fillAlpha: 0.8,
                            // Opacité des points
                            size: 4 // taille des points
                        }
                    }],
                    listeners: {
                        itemmouseover: function (o) {
                            markersLayer.clearMarkers();
                            var ptResult = new OpenLayers.LonLat(o.item.x, o.item.y);
                            var size = new OpenLayers.Size(20, 34);
                            var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
                            var icon = new OpenLayers.Icon('app/addons/profile/googlemarker.png', size, offset);
                            markersLayer.addMarker(new OpenLayers.Marker(ptResult, icon));


                        },
                        itemmouseout: function (o) {
                            markersLayer.clearMarkers();

                        }
                    }
                });


                var infosForm = new Ext.FormPanel({
                    title: tr("addonprofile.properties"),
                    id: 'infosform' + jobid,
                    defaults: {
                        xtype: 'textfield'
                    },
                    height: 200,
                    bodyStyle: 'padding: 10px',
                    items: [{
                        fieldLabel: tr("addonprofile.referential"),
                        name: 'referentiel',
                        value: infos.infos.referentiel
                    },

                    {
                        fieldLabel: tr("addonprofile.totaldistance"),
                        name: 'distance',
                        value: Math.round(infos.infos.distance) + ' m'
                    }, {
                        fieldLabel: tr("addonprofile.positivecumul"),
                        name: 'denivelepositif',
                        value: Math.round(infos.infos.denivelepositif) + ' m'
                    }, {
                        fieldLabel: tr("addonprofile.negativecumul"),
                        name: 'denivelenegatif',
                        value: Math.round(infos.infos.denivelenegatif) + ' m'
                    }, {
                        fieldLabel: tr("addonprofile.processedpoints"),
                        name: 'processedpoints',
                        value: infos.infos["processed points"]
                    }],
                    buttons: [{
                        iconCls: 'wps-csv',
                        tooltip: tr("addonprofile.csvdownload"),
                        handler: function () {
                            convert2csv(obj.profile.points);
                        }
                    }]
                });

                var configForm = createParametersForm(jobid);

                var tabs = new Ext.TabPanel({
                    activeTab: 0,
                    id: 'tabpanel' + jobid,
                    autoHeight: false,
                    height: 224,
                    items: [lineChart, infosForm, configForm]
                });

                var chartWindow = new Ext.Window({
                    closable: true,
                    id: 'profile' + jobid,
                    title: tr("addonprofile.charttitle") + jobid,
                    pageX: 10,
                    pageY: Ext.getBody().getHeight() - (250 + (parseInt(jobid, 10) * 20)),
                    resizable: true,
                    width: 600,
                    //height   : 450,
                    border: false,
                    plain: true,
                    region: 'center',
                    items: [tabs],
                    listeners: {
                        'close': function () {
                            removedrawfeatures(jobid);
                        }
                    }
                });

                chartWindow.render(Ext.getBody());
                chartWindow.show();



            } else {
                GEOR.util.errorDialog({
                    title: tr("addonprofile.error"),
                    msg: tr("addonprofile.error5")
                });
            }


        };

    return {
        /*
         * Public
         */

        /**
         * APIMethod: create
         * Return a  {Ext.menu.Item} for GEOR_addonsmenu.js and initialize this module.16:21 13/06/2012
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance, {wpsconfig} the wps tool options.
         */
        create: function (m, wpsconfig) {
            map = m;
	    if (wpsconfig.options.title){
		title = wpsconfig.options.title;
	    }
	    if (wpsconfig.options.abstract){
	 	abstract = wpsconfig.options.abstract;
	    }
	    if (wpsconfig.options.chart){
                Ext.chart.Chart.CHART_URL= wpsconfig.options.chart;
            }
            markersLayer = new OpenLayers.Layer.Markers("WpsMarker", {
                displayInLayerSwitcher: false
            });
            markersLayer.setZIndex(902);
            resultLayer = new OpenLayers.Layer.Vector("Result", {
                displayInLayerSwitcher: false
            });
            verticesLayer = new OpenLayers.Layer.Vector("Vertices", {
                displayInLayerSwitcher: false
            });
            verticesLayer.setVisibility(false);
            sf = new OpenLayers.Control.SelectFeature(verticesLayer, {
                hover: true,
                onSelect: onSelectVertice
            });
            map.addControl(sf);
            sf.activate();


            drawLayer = new OpenLayers.Layer.Vector("Profil", {
                displayInLayerSwitcher: false
            });
            drawLayer.events.register("featureadded", '', onNewLine);
            colors = wpsconfig.options.colors;
            wps_url = wpsconfig.options.wpsurl;
            wps_identifier = wpsconfig.options.identifier;

            var menuitems = new Ext.menu.Item({
                text: title,								
                id: 'wpsprofiletools',
                iconCls: "wps-linechart",
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
                    listeners: {
                        beforeshow: function () {
                            (enableSelectionTool(map) == true) ? Ext.getCmp('wpstoolselect').enable() : Ext.getCmp('wpstoolselect').disable();
                            if (initialized == false) {
                                describeProcess(wps_url, wps_identifier);
                            }
                        }						
                    },
                    items: [
                    new Ext.menu.CheckItem(new GeoExt.Action({
                        id: "wpstooldraw",
                        iconCls: "drawline",
                        text: tr("addonprofile.drawprofile"),
                        map: map,
                        toggleGroup: "map",
                        allowDepress: false,
                        disabled: true,
                        tooltip: tr("addonprofile.drawprofiletip"),
                        control: createWPSControl(OpenLayers.Handler.Path, map)
                    })), new Ext.Action({
                        id: "wpstoolselect",
                        iconCls: "geor-btn-metadata",
                        text: tr("addonprofile.selecttoprofile"),
                        allowDepress: false,
                        tooltip: tr("addonprofile.selecttoprofiletip"),
                        disabled: true,
                        handler: function () {
                            getMapFeaturesSelection(map);

                        }
                    }), new Ext.Action({
                        iconCls: "wps-uploadfile",
                        id: "wpstoolload",
                        text: tr("addonprofile.loadgml"),
                        allowDepress: false,
                        tooltip: tr("addonprofile.loadgmltip"),
                        disabled: (window.File && window.FileReader && window.FileList) ? false : true,
                        handler: function () {
                            selectGMLFile();
                        }
                    }), new Ext.Action({
                        id: "wpstoolparameters",
                        iconCls: "geor-btn-query",
                        text: tr("addonprofile.parameters"),
                        allowDepress: false,
                        disabled: true,
                        tooltip: tr("addonprofile.parameterstip"),
                        handler: function () {
                            getProfileParameters();
                        }
                    })]

                })
            });

            return menuitems;
        }
    }

})();
