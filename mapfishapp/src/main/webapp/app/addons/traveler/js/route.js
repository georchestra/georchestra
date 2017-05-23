Ext.namespace("GEOR.Addons.Traveler.route");

/**
 * Returns OpenLayers.Layer.Vector that contain start point
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} - OpenLayers.Layer.Vector that contain points
 */
GEOR.Addons.Traveler.route.pointsLayer = function(addon) {
    var resultLayer;
    var olStyle;
    var map = addon.map;
    var from = new OpenLayers.Projection("EPSG:4326");
    var style = addon.options.POINT_STYLE ? addon.options.POINT_STYLE : false;
    if (style) { // get style if exist
        olStyle = new OpenLayers.StyleMap(style);
    } else { // or create style
        olStyle = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style({
                strokeColor: "orange",
                strokeOpacity: 0.5,
                strokeWidth: 1,
                fillColor: "orange",
                fillOpacity: 0.6
            })
        });
    }
    if (map) {
        // manage if exist
        if (map.getLayersByName("route_points").length > 0) {
            resultLayer = GeoExt.MapPanel.guess().map.getLayersByName("route_points")[0];
        } else {
            // layer options
            var layerOptions = OpenLayers.Util.applyDefaults(
                this.layerOptions, {
                    displayInLayerSwitcher: false,
                    projection: map.getProjectionObject(),
                    styleMap: olStyle,
                    preFeatureInsert: function(feature) {
                        map.setCenter([feature.geometry.x, feature.geometry.y], 12, false); // adapt zoom extent
                    },
                    onFeatureInsert: function(feature) {
                        var z = 0;
                        map.layers.forEach(function(el) { // change index
                            z = el.getZIndex() > z ? el.getZIndex() : z;
                        });
                        if (feature.layer.getZIndex() < z) {
                            feature.layer.setZIndex(z + 1);
                        }
                    },
                }
            );
            // add layer
            resultLayer = new OpenLayers.Layer.Vector("route_points", layerOptions);
            map.addLayer(resultLayer);
        }
    }
    return resultLayer;
};


/**
 * Returns OpenLayers.Layer.Vector that contain route results
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} - OpenLayers.Layer.Vector that contains lines geometry
 */

GEOR.Addons.Traveler.route.linesLayer = function(addon) {
    var from = new OpenLayers.Projection("EPSG:4326");
    var resultLayer;
    var options = addon.options;
    var style = addon.options.ROUTE_STYLE ? new OpenLayers.StyleMap(options.ROUTE_STYLE) : false;
    if (!style) {
        style = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style({
                strokeColor: "rgba(0,179,253,0.7)",
                strokeOpacity: 1,
                strokeWidth: 5,
                strokeLinecap: "round",
                strokeDashstyle: "solid"
            })
        });
    }
    var map = addon.map;
    if (map) {
        // create and add layer to map if not exist
        if (map.getLayersByName("route_lines").length == 1) {
            resultLayer = GeoExt.MapPanel.guess().map.getLayersByName("route_lines")[0];
        } else {
            // layer options
            var layerOptions = OpenLayers.Util.applyDefaults(
                this.layerOptions, {
                    displayInLayerSwitcher: false,
                    projection: map.getProjectionObject(),
                    styleMap: style,
                    preFeatureInsert: function(feature) {
                        feature.geometry.transform(from, map.getProjectionObject());
                    }
                }
            );
            // add layer
            resultLayer = new OpenLayers.Layer.Vector("route_lines", layerOptions);
            map.addLayer(resultLayer);
        }
    }
    return resultLayer;
};

/**
 * Returns OpenLayers.Control.DrawFeature need to draw a freehand drawing start points
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} - OpenLayers.Control.DrawFeature to add points on map
 */

GEOR.Addons.Traveler.route.routeControl = function(addon) {
    var controlOptions;
    var map = addon.map;
    var layer = GEOR.Addons.Traveler.route.pointsLayer(addon);
    var control = addon.routeControl && map.getControlsBy("id", addon.routeControl.id).length > 0 ? addon.routeControl : false; // manage if already exist    
    if (map && layer && !control) { // create and add control if not already exist                 	
        controlOptions = OpenLayers.Util.applyDefaults( // options
            this.pointControlOptions, {
                id: "route_pointCtrl"
            }
        );
        control = new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Point, controlOptions);
        control.events.on({
            "featureadded": function() {
                control.deactivate();
                var str = "vec_";
                var indx = layer.features.length - 1;
                var feature = layer.features[indx];
                var lastPid = feature.id;
                var x = Math.round(feature.geometry.x * 10000) / 10000;
                var y = Math.round(feature.geometry.y * 10000) / 10000;
                if (addon.lastFieldUse) {
                    addon.featureArray[addon.lastFieldUse] = lastPid;
                    Ext.getCmp(addon.lastFieldUse).setValue(x + " / " + y);
                }
                GEOR.Addons.Traveler.route.getRoad(addon); // fire routing
            },
            scope: this
        });
        map.addControl(control); // add control to map
        if (!addon.routeControl) {
            addon.routeControl = control;
        }
    }
    return control;
};

/**
 * Returns Ext.form.CompositeField to display the means of travel.
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} - Ext.form.CompositeField that contains two Ext.Buttons
 */

GEOR.Addons.Traveler.route.modeBtn = function(addon) {
    return new Ext.form.CompositeField({
        // first window's panel
        cls: "cpMode",
        items: [{
            xtype: "button",
            height: 35,
            id: "route_walkBtn",
            width: 35,
            tooltip: OpenLayers.i18n("Traveler.route.walk.tooltip"),
            enableToggle: true,
            toggleGroup: "modeBtn",
            cls: "isochrone-mode-button",
            iconCls: "pedestrian",
            fieldLabel: "Pieton",
            hideLabel: true,
            listeners: {
                "toggle": function(b) {
                    if (b.pressed) {
                        b.setIconClass("pedestrian-pressed");
                        GEOR.Addons.Traveler.route.getRoad(addon, b);
                    } else {
                        b.setIconClass("pedestrian");
                    }
                }
            }
        }, {
            xtype: "button",

            id: "route_carBtn",
            tooltip: OpenLayers.i18n("Traveler.route.car.tooltip"),
            enableToggle: true,
            pressed: true,
            fieldLabel: "Voiture",
            hideLabel: true,
            height: 35,
            toggleGroup: "modeBtn",
            width: 35,
            iconCls: "vehicle-pressed",
            cls: "isochrone-mode-button",
            listeners: {
                toggle: function(b) {
                    if (b.pressed) {
                        b.setIconClass("vehicle-pressed");
                        GEOR.Addons.Traveler.route.getRoad(addon, b);
                    } else {
                        b.setIconClass("vehicle");
                    }
                }
            }
        }]
    });
};

/**
 * Returns Ext.form.CompositeField to display the means of travel.
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @param {string} id - field's identifiers to get and remove corresponding point from table  
 * @returns start point destruction
 */
GEOR.Addons.Traveler.route.removeFeature = function(addon, id) {
    var arr = addon.featureArray;
    var layer = GEOR.Addons.Traveler.route.pointsLayer(addon);
    var resultLayer = GEOR.Addons.Traveler.route.linesLayer(addon);
    var addon = addon;
    var extCmp = Ext.getCmp(id) ? Ext.getCmp(id) : false;
    if (extCmp) {
        extCmp.setValue("");
    }
    if (arr[id] && arr[id] != "") {
        var point = layer.getFeatureById(arr[id]);
        layer.removeFeatures(point);
        arr[id] = "";
        if (layer.features.length > 1) {
            GEOR.Addons.Traveler.route.getRoad(addon); // recalculate route
        } else {
            addon.routeLines.removeAllFeatures();
            if (Ext.getCmp("route_btnNav")) {
                Ext.getCmp("route_btnNav").hide();
            }
        }
    }
};

/**
 * Call addsStep function
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns method to insert new step in field set and refresh window layout
 */
GEOR.Addons.Traveler.route.insertFset = function(addon) {
    var panel = addon.routePanel;
    var window = addon.routeWindow;
    if (panel) {
        var idx = panel.items && panel.items.length > 2 ? panel.items.length - 4 : false; // get index
        if (idx) {            
            panel.insert(idx, GEOR.Addons.Traveler.route.addStep(addon, true, false)); // add cross button to delete step            
            panel.doLayout(); // force refresh panel
        }
    }
},

/**
 * Returns the opening state of the window
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns window without settings
 */
GEOR.Addons.Traveler.route.refresh = function(addon) {
    var map = addon.map;
    addon.routeWindow = GEOR.Addons.Traveler.route.routeWindow(addon);
    addon.featureArray = new Object();
    if (GEOR.Addons.Traveler.route.pointsLayer(addon)) {
        GEOR.Addons.Traveler.route.pointsLayer(addon).removeAllFeatures();
    }

    if (GEOR.Addons.Traveler.route.linesLayer(addon)) {
        GEOR.Addons.Traveler.route.pointsLayer(addon).removeAllFeatures();
    }

    if (Ext.getCmp("route_window")) {
        return Ext.getCmp("route_window").show();
    }
};

/**
 * Returns the opening state of the window
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @param {Boolean} - Indicate true to hidden delete button and hidden add step button
 * @param {Boolean} - Indicate to true to display delete step button
 * @param {String} idFset - Ext.forme.FiedlSet identifier to find corresponding Ext element and insert new items   
 * @returns new waypoint in fieldset
 */
GEOR.Addons.Traveler.route.addStep = function(addon, isStart, delBtn, idFset) {
    var tr = OpenLayers.i18n;
    var travelerAddon = this;
    var layer = addon.routePoints;
    var featureArray = addon.featureArray;
    var window = addon.routeWindow ? addon.routeWindow : false;
    var panel = addon.routePanel ? addon.routePanel : false;
    var control = addon.routeControl;

    var map = addon.map;

    var options = addon.options;

    var banStore = new Ext.data.JsonStore({
        proxy: new Ext.data.HttpProxy({
            url: options.BAN_URL,
            method: 'GET',
            autoLoad: true
        }),
        root: 'features',
        fields: [{
                name: 'typeGeometry',
                convert: function(v, rec) {
                    return rec.geometry.type
                }
            },
            {
                name: 'coordinates',
                convert: function(v, rec) {
                    return rec.geometry.coordinates
                }
            },
            {
                name: 'id',
                convert: function(v, rec) {
                    return rec.properties.id
                }
            },
            {
                name: 'label',
                convert: function(v, rec) {
                    return rec.properties.label
                }
            }
        ],
        totalProperty: 'limit',
        listeners: {
            "beforeload": function(q) {
                banStore.baseParams.q = banStore.baseParams["query"];
                banStore.baseParams.limit = options.BAN_RESULT ? options.BAN_RESULT : 5;
                delete banStore.baseParams["query"];
            }
        }
    });

    // create element to contain waypoint's fields
    var fSet = new Ext.form.FieldSet({
        autoWidht: true,
        cls: "fsStep",
        id: idFset
    });

    // create ID from fSet
    var addId = "add_" + fSet.id;
    var gpsId = "gps_" + fSet.id;
    var rmId = "rm_" + fSet.id;
    var inputId = "field_" + fSet.id;

    // create field and buttons
    var banField = new Ext.form.CompositeField({
        hideLabel: true,
        anchor: "100%",
        items: [{
            xtype: "combo",
            id: inputId,
            anchor: 200,
            emptyText: tr("Traveler.route.ban.emptytext"),
            fieldClass: "fBan",
            tooltip: tr("Traveler.route.ban.tooltip"),
            hideLabel: true,
            hideTrigger: true,
            store: banStore,
            displayField: 'label',
            hideTrigger: true,
            pageSize: 0,
            minChars: 5,
            listeners: {
                "select": function(combo, record) {
                    var geom, toCoordX, toCoordY;
                    var from = new OpenLayers.Projection("EPSG:4326"); // default GeoJSON SRS return by the service 
                    var to = map.getProjectionObject();
                    if (layer) {
                        if (featureArray[combo.id] && combo.id) {
                            GEOR.Addons.Traveler.route.removeFeature(addon, combo.id);
                        }
                        var fromCoordX = record.json.geometry.coordinates[0]; // get original coordinates
                        var fromCoordY = record.json.geometry.coordinates[1];
                        var geom = new OpenLayers.Geometry.Point(fromCoordX, fromCoordY).transform(from, to); // transform geom to map srs
                        var point = new OpenLayers.Geometry.Point(geom.x, geom.y); // create point
                        var feature = new OpenLayers.Feature.Vector(point);
                        layer.addFeatures(feature); // add points to map
                        featureArray[combo.id] = feature.id; // update point - id table
                    }
                },
                scope: this
            }
        }, {
            xtype: "button",
            iconCls: "gpsIcon",
            id: gpsId,
            tooltip: tr("Traveler.route.drawpoint.tooltip"),
            cls: "actionBtn",
            handler: function(button) {
                var idBtn = button.id;
                var control = addon.routeControl;
                // add point by click
                if (control && map) {
                    if (!control.active) {
                        // active control
                        control.activate();
                        // use to change value display in field
                        addon.lastFieldUse = inputId;
                        GEOR.Addons.Traveler.route.removeFeature(addon, inputId);
                    } else {
                        control.deactivate();
                    }
                }
            }
        }, {
            xtype: "button", // button to add new waypoint
            iconCls: "addIcon",
            id: addId,
            tooltip: tr("Traveler.route.addpoint.tooltip"),
            cls: "actionBtn",
            hidden: isStart,
            handler: function() {
                if (Ext.getCmp("route_panelWp")) {
                    var panel = Ext.getCmp("route_panelWp");
                    GEOR.Addons.Traveler.route.insertFset(addon, panel, window);
                }
            }
        }, {
            xtype: "button", // button to delete waypoint
            iconCls: "rmIcon",
            id: rmId,
            tooltip: tr("Traveler.route.removepoint.tooltip"),
            hidden: delBtn,
            cls: "actionBtn",
            handler: function(button) {
                if (fSet) {
                    fSet.destroy();
                    // remove associated point if exist
                    GEOR.Addons.Traveler.route.removeFeature(addon, inputId);
                    if (control.active) {
                        control.deactivate();
                    }
                }
            }
        }]
    });

    var comboRef = GEOR.Addons.Traveler.referential.createForRoute(addon, fSet, inputId); // create referential element
    // if no warehouse is set to find layer, deactive checkBox
    var cbDisplay = false;
    // add all items to field set
    fSet.add(banField);
    if (comboRef) {
        fSet.add(comboRef);
    }
    if (comboRef.getStore() && (!comboRef.getStore().url || comboRef.getStore().url == "")) {
        cbDisplay = true;
    }
    var checkItem = new Ext.form.Checkbox({ // create checkbox to display or hidden referential combo
        hideLabel: true,
        hidden: cbDisplay,
        boxLabel: tr("traveler.route.checkbox.referential"),
        listeners: {
            "check": function() {
                if (this.checked) {
                    banField.hide();
                    comboRef.show();
                    if(addon.featureArray[comboRef.id] && Ext.getCmp(addon.featureArray[comboRef.id])){
                    	Ext.getCmp(addon.featureArray[comboRef.id]).show();
                    }
                } else {
                    banField.show();
                    comboRef.hide();
                    if(addon.featureArray[comboRef.id] && Ext.getCmp(addon.featureArray[comboRef.id])){
                    	Ext.getCmp(addon.featureArray[comboRef.id]).hide();
                    }
                }
            }
        }
    });
    // insert checkbox to select data type
    fSet.insert(0, checkItem);

    return fSet;
};

/**
 * Return new request to create route
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @param {Object} - Ext.Button to find travel mode indicate in pressed button's field label
 * @returns new function to create new result
 */
GEOR.Addons.Traveler.route.getRoad = function(addon, modeButton) {
    var wPCoord = "";
    var method = [];
    var exclusions = [];
    var graphName;
    var settings = new Object();
    var url = addon.options.ROUTE_SERVICE;
    settings.origin = ""; // request settings
    settings.destination = "";
    settings.srs = addon.map.getProjectionObject();        
    if(modeButton){ // get pressed mode value
        settings.graphName =  modeButton.fieldLabel;        
    } else { // or get mode by buttons id
        if (Ext.getCmp("route_carBtn").pressed) {
            settings.graphName = "Voiture";
        } else {
            settings.graphName = "Pieton";
        }
    }
    // parse key value table
    for (var key in addon.featureArray) {
        var a = addon.featureArray[key];
        if (addon.featureArray.hasOwnProperty(key) && addon.routePoints.getFeatureById(a)) {
            var geom = addon.routePoints.getFeatureById(a).geometry;
            var c = geom.x + "," + geom.y;
            if (key.indexOf("start") > -1) {
                // get origin param
                settings.origin = c;
            } else if (key.indexOf("end") > -1) {
                // get destination param
                settings.destination = c;
            } else {
                // stock others waypoints
                wPCoord = wPCoord + c + ";";
            }
        }
    };
    settings.waypoints = wPCoord; // waypoints param    
    var checkItems = Ext.getCmp("route_excludeCheck") ? Ext.getCmp("route_excludeCheck").items.items : false; // get exclusions params
    if (checkItems && checkItems.length > 0) {
        checkItems.forEach(function(el) {
            if (Ext.getCmp(el.id) && Ext.getCmp(el.id).checked) {
                exclusions.push(Ext.getCmp(el.id).value);
            };
        });
    }
    // get method param
    if (Ext.getCmp("route_radioTime").checked) {
        settings.method = "TIME";
    } else {
        settings.method = "DISTANCE";
    }
    // parse geom from service to display result and zoom on bound
    function parseWKT(geom, layer, map, json) {
        var road;
        var bounds;
        // to display only one result
        layer.removeAllFeatures();
        if (Ext.getCmp("route_btnNav")) {
            Ext.getCmp("route_btnNav").hide();
        }
        // get geom from WKT and create feature
        var wkt = new OpenLayers.Format.WKT();
        var features = wkt.read(geom);
        layer.addFeatures(features);
        if (layer.getFeatureById(features.id)) {
            road = layer.getFeatureById(features.id);
        }
        if (road) {
            if (road.constructor != Array) {
                road = [road];
            }
            for (var i = 0; i < road.length; ++i) {
                // get feature bound if not exist
                if (!bounds) {
                    bounds = road[i].geometry.getBounds();
                } else {
                    bounds.extend(road[i].geometry.getBounds());
                }
            }
            // set point layer to front
            var zIndex = layer.getZIndex() ? layer.getZIndex() : false;
            if (zIndex) {
                addon.routePoints.setZIndex(zIndex + 1);
            }
            // zoom to result extent
            addon.map.zoomToExtent(bounds);
            // display time and distance informations
            if (json.duration || json.distance) {
                var tCut = json.duration.split(":");
                var tStr = tCut[0] + " h " + tCut[1] + " min";

                if (Ext.getCmp("route_resFset")) {
                    Ext.getCmp("route_resFset").show();
                }

                if (Ext.getCmp("route_distTexF") && json.distance) {
                    Ext.getCmp("route_distTexF").setValue(json.distance);
                }

                if (Ext.getCmp("route_timeTexF") && tStr) {
                    Ext.getCmp("route_timeTexF").setValue(tStr);
                }
            }
        } else {
            alert('Bad WKT');
        }
    }
    if (settings.origin && settings.origin != "" && settings.destination && settings.destination != "") {
        // display load item
        GEOR.waiter.show();
        // fire request
        var request = new OpenLayers.Request.GET({
            url: url,
            params: settings,
            async: true,
            callback: function(request) {
                if (request.responseText) {
                    var decode = JSON.parse(request.responseText);
                    // display result on map and get navigation infos
                    if (decode && decode.geometryWkt) {
                        // get geom from json 
                        var geomWKT = decode.geometryWkt;
                        // create route from WKT 
                        parseWKT(geomWKT, addon.routeLines, addon.map, decode);
                        // get navigation steps
                        if (decode.legs && decode.legs.length > 0) {
                            var steps = [];
                            var section = decode.legs;

                            section.forEach(function(el, index) {
                                el.steps.forEach(function(el, index) {
                                    steps.push(el);
                                })
                            })
                            GEOR.Addons.Traveler.route.navInfos = steps;
                        }
                    }
                    // get navigation details from json
                    if (decode.legs && decode.legs.length > 0) {
                        var steps = [];
                        var section = decode.legs;
                        section.forEach(function(el, index) {
                            el.steps.forEach(function(el, index) {
                                steps.push(el);
                            })
                        })
                        if (steps.length > 0) {
                            if (Ext.getCmp("route_btnNav")) {
                                //Ext.getCmp("route_btnNav").show(); // uncomment to display print button
                            }
                        }
                    }
                } else {
                    console.log("Request fail");
                }
                GEOR.waiter.hide();
            }
        });
    } else {
        GEOR.Addons.Traveler.route.linesLayer(addon).removeAllFeatures();
    }
};

/**
 * Create a new panel that will be added in window 
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} new Ext.Panel that contains all fieldset
 */
GEOR.Addons.Traveler.route.createPanel = function(addon) {
    if (Ext.getCmp("route_panelWp")) {
        Ext.getCmp("route_panelWp").destroy();
    } else {
        return new Ext.Panel({
            autoScroll: true,
            hidden: false,
            id: "route_panelWp",
            items: [GEOR.Addons.Traveler.route.modeBtn(addon), {
                xtype: "fieldset",
                collapsible: true,
                collapsed: true,
                title: OpenLayers.i18n("Traveler.route.options.title"),
                cls: "fsOptions",
                items: [{
                    xtype: "compositefield",
                    hideLabel: true,
                    id: "route_radioMethod",
                    items: [{
                        xtype: "radio",
                        checked: true,
                        hideLabel: true,
                        boxLabel: OpenLayers.i18n("Traveler.route.options.fast"),
                        id: "route_radioTime",
                        value: "TIME",
                        name: "method",
                        listeners: {
                            check: function(c) {
                                GEOR.Addons.Traveler.route.getRoad(addon);
                            },
                            scope: this
                        }
                    }, {
                        xtype: "spacer",
                        width: "5"
                    }, {
                        xtype: "radio",
                        hideLabel: true,
                        id: "route_radioDist",
                        name: "method",
                        value: "DISTANCE",
                        boxLabel: OpenLayers.i18n("Traveler.route.options.distance"),
                        listeners: {
                            check: function(c) {
                                GEOR.Addons.Traveler.route.getRoad(addon);
                            },
                            scope: this
                        }
                    }, {
                        xtype: "spacer",
                        height: "25"
                    }]
                }, {
                    xtype: "compositefield", // exclusions checkbox
                    id: "route_excludeCheck",
                    hideLabel: true,
                    items: [{
                        xtype: "checkbox",
                        tooltip: OpenLayers.i18n("Traveler.route.options.toll.tooltip"),
                        boxLabel: OpenLayers.i18n("Traveler.route.options.checkbox.toll"),
                        id: "route_tollRadio",
                        labelWidth: 20,
                        hideLabel: true,
                        value: "Toll",
                        listeners: {
                            check: function(c) {
                                GEOR.Addons.Traveler.route.getRoad(addon);
                            },
                            scope: this
                        }
                    }, {
                        xtype: "checkbox",
                        boxLabel: OpenLayers.i18n("Traveler.route.checkbox.bridge"),
                        tooltip: OpenLayers.i18n("Traveler.route.bridge.tooltip"),
                        id: "route_bridgeRadio",
                        hideLabel: true,
                        value: "Bridge",
                        listeners: {
                            check: function(c) {
                                GEOR.Addons.Traveler.route.getRoad(addon);
                            },
                            scope: this
                        }
                    }, {
                        xtype: "checkbox",
                        boxLabel: OpenLayers.i18n("Traveler.route.checkbox.tunels"),
                        tooltip: OpenLayers.i18n("Traveler.route.tunnels.tooltip"),
                        id: "route_tunnelRadio",
                        hideLabel: true,
                        value: "Tunnel",
                        listeners: {
                            check: function(c) {
                                GEOR.Addons.Traveler.route.getRoad(addon);
                            },
                            scope: this
                        }
                    }]
                }]
            }, {
                xtype: "spacer",
                height: "10"
            }, {
                xtype: "fieldset", // fieldset to display result's informations
                title: OpenLayers.i18n("Traveler.route.result.title"),
                hidden: true,
                id: "route_resFset",
                collapsible: true,
                collapsed: true,
                cls: "fsInfo",
                items: [{
                    xtype: "textfield",
                    id: "route_distTexF",
                    width: 60,
                    fieldLabel: OpenLayers.i18n("Traveler.route.result.distance"),
                    readOnly: true,
                    style: {
                        borderWidth: "0px"
                    },
                    labelStyle: 'font-size:11px;'
                }, {
                    xtype: "textfield",
                    width: 60,
                    id: "route_timeTexF",
                    fieldLabel: OpenLayers.i18n("Traveler.route.result.time"),
                    readOnly: true,
                    style: {
                        borderWidth: "0px"
                    },
                    labelStyle: 'font-size:11px;'
                }]
            }],
            listeners: {
                "added": function(panel) {
                    addon.routePanel = panel;
                    panel.insert(1, GEOR.Addons.Traveler.route.addStep(addon, true, true, "startPoint")); // add new waypoint
                    panel.insert(2, GEOR.Addons.Traveler.route.addStep(addon, false, true, "endPoint"));
                }
            }
        });
    }
};

/**
 * Create tool window
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Object} main Ext.Window to set and create route calculation 
 */
GEOR.Addons.Traveler.route.routeWindow = function(addon) {
    var tr = OpenLayers.i18n;
    var map = addon.map;
    if (Ext.getCmp("route_window")) {
        Ext.getCmp("route_window").destroy();
    }
    var window = new Ext.Window({
        title: tr("Traveler.route.windowtitle"),
        constrainHeader: true,
        autoHeight: true,
        shadow: false,
        width: 290,
        id: "route_window",
        autoScroll: true,
        closable: true,
        closeAction: "hide",
        resizable: false,
        collapsible: true,
        items: [GEOR.Addons.Traveler.route.createPanel(addon)],
        buttonAlign: 'center',
        fbar: [{
            iconCls: "refresh",
            tooltip: tr("Traveler.route.refresh.tooltip"),
            id: "route_btnRefresh",
            cls: "actionBtn",
            handler: function() {
                GEOR.Addons.Traveler.route.refresh(addon);
            }
        }, {
            iconCls: "navIcon",
            cls: "actionBtn",
            hidden: true,
            id: "route_btnNav",
            tooltip: tr("Traveler.route.print.tooltip"),
            handler: function() {
                if (GEOR.Addons.Traveler.route.navInfos) {
                    // call pdf -> see getDocument method in photos obliques
                }
            }
        }],
        listeners: {
            "show": function(win) {
                if (map) { // window location
                    win.alignTo(map.div, "tl", [0, 100], false);
                }
            },
            "hide": function() {
                if (addon.routePoints) { // remove all features
                    addon.routePoints.removeAllFeatures();
                }
                if (addon.routeLines) {
                    addon.routeLines.removeAllFeatures();
                }

                if (Ext.getCmp("route_btnNav")) {
                    Ext.getCmp("route_btnNav").hide();
                }
            }
        }
    });

    return window;
};