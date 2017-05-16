Ext.namespace("GEOR.Addons.Traveler.isochrone");

/**
 * Create layer to contains start points
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns  {OpenLayers.Layer.Vector}
 */
GEOR.Addons.Traveler.isochrone.layer = function(addon) {
	var map = addon.map;
	var style = addon.options.POINT_STYLE ? addon.options.POINT_STYLE : false; 
	
    var layer, olStyle;
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
    if (map) { // get layer if exist        	
        if (map.getLayersByName("iso_points").length > 0 && map.getLayersByName("iso_points")[0]) {
            layer = map.getLayersByName("iso_points")[0];
        } else { // or create layer
            var layerOptions = OpenLayers.Util.applyDefaults(
                this.layerOptions, {
                    displayInLayerSwitcher: false,
                    projection: map.getProjectionObject(),
                    styleMap: olStyle,
                    preFeatureInsert: function(feature){
                    	map.setCenter([feature.geometry.x, feature.geometry.y], 12, false); // adapt zoom extent
                    	if(addon.isoLayer){ // remove other points
                    		addon.isoLayer.removeAllFeatures();
                    	}
                    },
                    onFeatureInsert: function(feature) {
                        var z = 0;
                        map.layers.forEach(function(el) { // change index
                            z = el.getZIndex() > z ? el.getZIndex() : z;
                        });
                        if(feature.layer.getZIndex() < z){
                        	feature.layer.setZIndex(z+1);
                        }
                    },
                }
            );
            layer = new OpenLayers.Layer.Vector("iso_points", layerOptions);
            map.addLayer(layer);
        }
    }
    return layer;
};

/**
 * Create layer to contains isochrones polygon results
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns  {OpenLayers.Layer.Vector}
 */
GEOR.Addons.Traveler.isochrone.resultLayer = function(addon) {
	var map = addon.map;
	var name = addon.options.ISO_LAYER_NAME;
    var from = new OpenLayers.Projection("EPSG:4326");
    var layer;

    if (map) { // get layer returnif exist        	
        if (map.getLayersByName(name).length > 0 && map.getLayersByName(name)[0]) {
            layer = map.getLayersByName(name)[0];
        } else { // or create layer
            var layerOptions = OpenLayers.Util.applyDefaults(
                this.layerOptions, {
                    displayInLayerSwitcher: true,
                    projection: map.getProjectionObject(),
                    preFeatureInsert: function(feature) {
                        feature.geometry.transform(from, map.getProjectionObject());
                    },
                    onFeatureInsert: function() { // display start point layer to front
                        if (addon.isoLayer) {
                            var z = 0;
                            map.layers.forEach(function(el) {
                                z = el.getZIndex() > z ? el.getZIndex() : z;

                            });
                            addon.isoLayer.setZIndex(z + 1);
                        }
                    }
                }
            );
            layer = new OpenLayers.Layer.Vector(name, layerOptions);
            layer.events.register("removed",this,function(){
            	if(addon.isoResLayer.displayInLayerSwitcher && Ext.getCmp("geor-layerManager")){
            		var layerInTree = Ext.getCmp("geor-layerManager").root.childNodes;
                	layerInTree.forEach(function(el){
                		if(el.text == addon.isoResLayer.name){
                			el.remove();
                		}
                	});
            	}            	
            	if(Ext.getCmp("iso_win")){
            		Ext.getCmp("iso_win").close();
            	}
            });
            map.addLayer(layer);
        }
    }
    return layer;
};

/**
 * Create control to create points by freehand drawing
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @param {String} fId - id of ban combo use to geocode adress 
 * @returns {OpenLayers.Control.DrawFeature}
 */
GEOR.Addons.Traveler.isochrone.drawControl = function(addon, fId) {
	var map = addon.map ? addon.map : "";
	var layer = addon.isoLayer ? addon.isoLayer : "";
	var obj = addon.isoStart ? addon.isoStart : "";
    var epsg4326 = new OpenLayers.Projection("EPSG:4326");
    var control;
    if (map && layer) {    	
        if(map.getControlsBy("id", "iso_draw")){
        	map.removeControl(map.getControlsBy("id", "iso_draw")[0]);
        }
        // if not exist add control
        var controlOptions = OpenLayers.Util.applyDefaults(
            this.pointControlOptions, {
                id: "iso_draw"
            }
        );
        control = new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Point, controlOptions);
        control.events.on({
            "featureadded": function(event) {
                control.deactivate();
                var feature = event.feature;
                // limit decimals
                var x = Math.round(feature.geometry.x * 10000) / 10000;
                var y = Math.round(feature.geometry.y * 10000) / 10000;
                if (obj && feature.geometry) {
                    // transform geom to 4326 and set combo value in 3857
                    var locGeom = new OpenLayers.Geometry.Point(feature.geometry.x, feature.geometry.y).transform(map.getProjection(), epsg4326);
                    obj["location"] = [locGeom.x + "," + locGeom.y];
                    // display text field
                    Ext.getCmp(fId).setValue(x + "/" + y);
                }
            },
            scope: this
        });
        map.addControl(control);        
        return control;
    }
};

/**
 * Create or replace compositefield to contains time text fields and time number fields
 * @param {Object} addon - Get attributes, objects from GEOR.Addon 
 * @returns {Ext.form.CompositeField}
 */
GEOR.Addons.Traveler.isochrone.time = function(addon) {
    if (Ext.getCmp("iso_time")) {
        Ext.getCmp("iso_time").destroy();
    }
    var config = addon.options;
    return new Ext.form.CompositeField({
        id: "iso_time",
        hideLabel: true,
        items: [{
            xtype: "numberfield",
            value: config.TIME[0],
            width: 30,
            emptyText: "0",
            height: 15
        }, {
            xtype: "textfield",
            value: "min",
            width: 25,
            cls: "isochrone-textfield-time",
            readOnly: true,
            height: 15
        }, {
            xtype: "numberfield",
            value: config.TIME[1],
            emptyText: "0",
            width: 30,
            height: 15
        }, {
            xtype: "textfield",
            width: 25,
            cls: "isochrone-textfield-time",
            value: "min",
            readOnly: true,
            height: 15
        }, {
            xtype: "numberfield",
            value: config.TIME[2],
            emptyText: "0",
            width: 30,
            height: 15
            
        }, {
            xtype: "textfield",
            width: 25,
            cls: "isochrone-textfield-time",
            value: "min",
            readOnly: true,
            height: 15
        }]
    });
};

/**
 * Create or replace Ext.Form.CompositeField that contains buttons use to select travel method
 * return {Ext.form.CompositeField}
 */
GEOR.Addons.Traveler.isochrone.mode = function() {
    var tr = OpenLayers.i18n;
	if (Ext.getCmp("iso_modeCp")) {
        Ext.getCmp("iso_modeCp").destroy();
    }
    return new Ext.form.CompositeField({
        id: "iso_modeCp",
        cls: "isochrone-compositefield-mode",
        items: [{
            xtype: "button",
            tooltip: tr("isochrone.button.pedestrian"),
            id: "iso_pedestrian",
            cls: "isochrone-mode-button",
            iconCls: "pedestrian",
            enableToggle: true,
            allowDepress: false,
            pressed: false,
            toggleGroup: "iso_mode",
            listeners: {
                toggle: function(b) {
                    if (b.pressed) {
                        b.setIconClass("pedestrian-pressed");
                    } else {
                        b.setIconClass("pedestrian");
                    }
                }
            }
        }, {
            xtype: "button",
            tooltip: tr("isochrone.button.vehicle"),
            id: "iso_vehicle",
            iconCls: "vehicle-pressed",
            cls: "isochrone-mode-button",
            allowDepress: false,
            enableToggle: true,
            pressed: true,
            toggleGroup: "iso_mode",
            listeners: {
                toggle: function(b) {
                    if (b.pressed) {
                        b.setIconClass("vehicle-pressed");
                    } else {
                        b.setIconClass("vehicle");
                    }
                }
            }
        }]
    });
};

/**
 * Create or replace compositefields that contains exclusions checkbox
 * @returns {Ext.form.CompositeField} 
 */
GEOR.Addons.Traveler.isochrone.exclusions = function() {
    if (Ext.getCmp("iso_exclusions")) {
        Ext.getCmp("iso_exclusions").destroy();
    }
    return new Ext.form.CompositeField({
        id: "iso_exclusions",
        hideLabel: true,
        items: [{
            xtype: "checkbox",
            boxLabel: OpenLayers.i18n("traveler.isochrone.boxtooltip.toll"),
            id: "iso_toll",
            labelWidth: 20,
            hideLabel: true,
            value: "Toll"
        }, {
            xtype: "checkbox",
            boxLabel: OpenLayers.i18n("traveler.isochrone.boxlabel.bridge"),
            id: "iso_bridge",
            hideLabel: true,
            value: "Bridge"
        }, {
            xtype: "checkbox",
            boxLabel: OpenLayers.i18n("traveler.isochrone.boxlabel.tunels"),
            id: "iso_tunnel",
            hideLabel: true,
            value: "Tunnel"
        }]
    });
};

/**
 * Create or replace ban field to search string adress and draw point to locate result
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns {Ext.form.Combobox}
 */
GEOR.Addons.Traveler.isochrone.ban = function(addon) {
    var epsg4326 = new OpenLayers.Projection("EPSG:4326");
    var layer = addon.isoLayer;
    var map = addon.map;
    var config = addon.options;
    var startPoints =  addon.isoStart ? addon.isoStart : false;
    var banStore = new Ext.data.JsonStore({ // create store that call service
        proxy: new Ext.data.HttpProxy({
            url: addon.options.BAN_URL, // URL
            method: "GET",
            autoLoad: true
        }),
        root: "features",
        fields: [{
                name: "typeGeometry",
                convert: function(v, rec) {
                    return rec.geometry.type;
                }
            },
            {
                name: "coordinates",
                convert: function(v, rec) {
                    return rec.geometry.coordinates;
                }
            },
            {
                name: "id",
                convert: function(v, rec) {
                    return rec.properties.id;
                }
            },
            {
                name: "label",
                convert: function(v, rec) {
                    return rec.properties.label;
                }
            }
        ],
        totalProperty: "limit",
        listeners: {
            "beforeload": function(q) { // analyzes the input to obtain a result
                banStore.baseParams.q = banStore.baseParams["query"];
                banStore.baseParams.limit = config.BAN_RESULT; // limit replies number 
                delete banStore.baseParams["query"];
            }
        }
    });
    if (Ext.getCmp("iso_ban")) {
        Ext.getCmp("iso_ban").destroy();
    }
    return new Ext.form.ComboBox({ // create comboBox
        anchor: 200,
        id: "iso_ban",
        emptyText: OpenLayers.i18n("isochron.ban.emptytext"),
        tooltip: OpenLayers.i18n("isochron.ban.tooltip"),
        hideLabel: true,
        hideTrigger: true,
        cls: "isochrone-fieldset-ban",
        store: banStore,
        displayField: "label",
        width: 160,
        hideTrigger: true,
        pageSize: 0,
        minChars: 5,
        listeners: {
            "select": function(combo, record) { // get geometry from result and draw point on map
                if (layer && map) {
                    var lon = record.json.geometry.coordinates[0];
                    var lat = record.json.geometry.coordinates[1];
                    if (startPoints) {
                        startPoints["location"] = [lon + "," + lat];
                    }
                    var geom = new OpenLayers.Geometry.Point(lon, lat).transform(epsg4326, map.getProjection());
                    var point = new OpenLayers.Geometry.Point(geom.x, geom.y);
                    var feature = new OpenLayers.Feature.Vector(point);
                    layer.addFeatures(feature);
                }
            },
            scope: this
        }
    });
};

/**
 * Create or replace ban Ext.form.CompositeField that will be inserted in main window
 * @param {Object} addon - GEOR.Addon to get attributes and scope as layer to draw result 
 * @param {Object} banEl - {Ext.form.Combobox} create before to write input and fire research
 * @returns {Ext.form.CompositeField}
 */
GEOR.Addons.Traveler.isochrone.banField = function(addon, banEl) {
    return new Ext.form.CompositeField({
        hideLabel: true,
        anchor: "100%",
        items: [banEl, {
            xtype: "button",
            iconCls: "gpsIcon",
            tooltip: tr("isochrone.draw.tooltip"),
            cls: "actionBtn",
            handler: function(button) {
            	var control = addon.isoControl ? addon.isoControl : false;
            	var layer = addon.isoLayer ? addon.isoLayer : false;
                // manage draw control
                if (control && layer) {
                    if (!control.active) {
                        control.activate();
                        layer.removeAllFeatures();
                    } else {
                        control.deactivate();
                    }
                }
            }
        }]
    });
};

/**
 * Create or replace checkbox to display or hide referential combobox. 
 * If user check this, ban combo will be hidden.
 * If user check this, geometry checkbox will be unchecked.
 * @param {Object} banField - {Ext.form.CompositeField} create before to contains ban combobox
 * @param {Object} comboRef - {Ext.form.Combobox} create to select feature from GeoServer among referential layers 
 * @returns {Ext.form.Checkbox}
 */
GEOR.Addons.Traveler.isochrone.refentialBox = function(banField, comboRef) {
    var tr = OpenLayers.i18n;
    return new Ext.form.Checkbox({
        hideLabel: true,
        hidden: false,
        boxLabel: tr("traveler.isochrone.rerential.boxlabel"),
        listeners: {
            "check": function(cb, checked) {
                if (checked) {
                    // uncheck other checkbox
                    cb.findParentByType("compositefield").items.items.forEach(function(el){
                    	if(el !== cb){
                    		el.reset();
                    	}  
                    });
                    banField.hide();
                    comboRef.show();
                } else {
                    comboRef.hide();
                    banField.show();
                }
            }
        }
    });
};

/**
 * Create or replace checkbox to select geometry from local {Ext.state.Provider} if exist. 
 * If user check this, ban combo will be hidden.
 * If user check this, referential checkbox will be uncheck.
 * @param {Object} addon - Get attributes, objects from GEOR.Addon 
 * @param {Object} banField - {Ext.form.CompositeField} create before to contains ban combobox
 * @param {Object} comboRef - {Ext.form.Combobox} create to select feature from GeoServer among referential layers 
 * @returns {Ext.form.Checkbox}
 */
GEOR.Addons.Traveler.isochrone.geometryBox = function(addon, banField, comboRef) {
    var tr = OpenLayers.i18n;
    if (Ext.getCmp("iso_geom")) {
        Ext.getCmp("iso_geom").destroy();
    }
    return new Ext.form.Checkbox({
        hideLabel: true,
        id: "iso_geom",
        hidden: false,
        cls:"",
        boxLabel: tr("traveler.isochrone.searchgeometry"),
        listeners: {
            "check": function(cb, checked) {
            	// manage others ext composents
            	if (addon.isoControl.active) {
                    addon.isoControl.deactivate();
                }
            	var inputFset;
            	if(Ext.getCmp("iso_input")){
                	inputFest = Ext.getCmp("iso_input");
                }
                if (checked) {
                    var to = new OpenLayers.Projection("epsg:4326");
                    // get geometry from localstorage - epsg 3857
                    if (Ext.state.Manager.getProvider() &&  Ext.state.Manager.getProvider().get("geometry")) {
                        addon.isoLayer.removeAllFeatures();
                        var geomStr = Ext.state.Manager.getProvider().decodeValue(Ext.state.Manager.getProvider().get("geometry"));
                        var feature = (new OpenLayers.Format.WKT()).read(geomStr);
                        // authorize only point geometry to create isochrone
                        var countPoints = feature.geometry.components.length;
                        var typeGeom = feature.geometry.CLASS_NAME;
                        if(countPoints > addon.options.LIMIT_GEOM){
                        	cb.reset();
                        	return GEOR.util.infoDialog({
                                msg: tr("Traveler.isochrone.msg.outoflimit")
                            });
                        }
                        if (typeGeom.indexOf("Point") > -1 && countPoints <= addon.options.LIMIT_GEOM) {
                            addon.isoLayer.addFeatures(feature);
                            var coord = [];
                            feature.geometry.components.forEach(function(el) {
                                var trans = new OpenLayers.Geometry.Point(el.x, el.y).transform(addon.map.getProjection(), to);
                                coord.push(trans)
                            });
                            addon.isoStart["location"] = coord; // array                            
                            // uncheck other checkbox
                            cb.findParentByType("compositefield").items.items.forEach(function(el){
                            	if(el !== cb){
                            		el.reset();
                            	}  
                            });
                            // hide all combo
                            banField.hide();
                            comboRef.hide();
                        } else {
                            GEOR.util.infoDialog({
                                msg: tr("Wrong geometry !")
                            });
                            // uncheck checkbox
                            cb.reset();
                        }
                    } else {
                    	GEOR.util.infoDialog({
                            msg: tr("traveler.isochrone.msg.nogeom")
                        });
                    	cb.reset();
                    }
                } else {
                	if(inputFest){
            			banField.show();
                    }
                    addon.isoLayer.removeAllFeatures();
                }
            }
        }
    });
};

/**
 * Create or replace fieldset that contains ban combobox, referential combobox
 * ban combo and referential combo need to be contained in a compositefield.
 * @param {Object} addon - Get attributes, objects from GEOR.Addon 
 * @param {Object} ban - {Ext.form.CompositeField} contain ban combobox
 * @returns {Ext.form.FieldSet}
 */
GEOR.Addons.Traveler.isochrone.pointFset = function(addon, ban) {
    var items = [];
    if (Ext.getCmp("iso_input")) {
        Ext.getCmp("iso_input").destroy();
    }
    var fields = new Ext.form.FieldSet({
        autoWidht: true,
        hideLabel: true,
        cls: "isochrone-fieldset",
        id: "iso_input",
        items: [ban]
    });
    
    var comboRef = GEOR.Addons.Traveler.referential.create(addon);
    if (comboRef) {
        fields.add(comboRef);
    }
    // create refenretial checkBox
    if (comboRef.getStore() && (comboRef.getStore().url || !comboRef.getStore().url == "")) {
        var checkRef = GEOR.Addons.Traveler.isochrone.refentialBox(ban, comboRef);
        items.push(checkRef) 
    }    
    // insert geometry check box
    var geomBox = GEOR.Addons.Traveler.isochrone.geometryBox() ? GEOR.Addons.Traveler.isochrone.geometryBox(addon, ban, comboRef) : false;
    items.push(geomBox);         
    
    if (Ext.getCmp("iso_cpfCheck")) {
        Ext.getCmp("iso_cpfCheck").destroy();
    }
    var cpField = new Ext.form.CompositeField({
        hideLabel: true,
        id: "iso_cpfCheck",
        items: items
    });
    fields.insert(0, cpField);

    return fields;
};

/**
 * Create request to search isochrone with IGN service
 * @param {String} service - url to call GET service
 * @settings {Object} settings - javascript object that contains all parameters to submit search criteria inside url
 * @returns {OpenLayers.Feature.Vector}
 */
GEOR.Addons.Traveler.isochrone.fireRequest = function (service,settings){
	var feature;
	OpenLayers.Request.GET({
        url: service,
        params: settings,
        async:false,
        callback: function(request) {        	
            if (request.responseText) {
                // get geom from JSON decode
                var decode = JSON.parse(request.responseText);
                if(decode.status && decode.status == "ERROR"){
                	feature = decode;
                } else {
                	var geom = decode.wktGeometry;
                    var wkt = new OpenLayers.Format.WKT();
                    feature = wkt.read(geom);                	
                }                               
            }
        }
	});		
	return feature;	
};

/**
 * Create request to search isochrone with IGN service
 * @param {Object} addon - Get attributes, objects from GEOR.Addon
 * @returns isochrone and display new line in Result fieldset
 */
GEOR.Addons.Traveler.isochrone.createIsochrone = function(addon) {
	GEOR.waiter.show();
    var settings = {};
    var check = [];
    var times = [];    
    var tr = OpenLayers.i18n; // need some attributes from addon
    var obj = addon.isoStart;
    var layer = addon.isoResLayer;
    var config = addon.options;
    settings.srs = config.ISO_SRS;     // get request params from manifest.json 
    settings.smoothing = config.SMOOTHING;
    settings.holes = config.HOLES;
    var service = config.ISOCHRONE_SERVICE;
    var checkItems = Ext.getCmp("iso_exclusions") ? Ext.getCmp("iso_exclusions").items.items : false;     // get exclusions params
    if (checkItems && checkItems.length > 0) {
        checkItems.forEach(function(el) {
            if (Ext.getCmp(el.id) && Ext.getCmp(el.id).checked) {
                check.push(Ext.getCmp(el.id).value); 
            };
        });
        settings.exclusions = check.join(";");
    }
    if (Ext.getCmp("iso_pedestrian") && Ext.getCmp("iso_pedestrian").pressed) {     //get graphName 
        settings.graphName = "Pieton";
    } else {
        settings.graphName = "Voiture";
    }

    if (obj) { // get geom from ban or referential tools
        var startGeom = obj["location"];
    }
    if (Ext.getCmp("iso_time")) {         // get times values
        var li = Ext.getCmp("iso_time").items.items;
        li.forEach(function(el) {
            if (el.xtype == "numberfield" && (el.getValue() > 0 ||  el.getValue() !== "")) {
                var seconds = el.getValue() * 60;
                times.push(seconds);
            }
        });
        if(!times.length > 0){
        	return GEOR.util.infoDialog({
                msg: tr("traveler.isochrone.msg.notimes")
            });
        }
        startGeom.forEach(function(p,index) {    // parse start geometry
            var isochrones = [];
            if (p.x && p.y) {
                settings.location = p.x + "," + p.y;
            } else {
                settings.location = p;
            }

            // parse input time to fire request by times
            var area = [];
            var order = [];
            times.forEach(function(el, index) {
                settings.time = el;                               
                if (settings.time && settings.location) {
                	// fire request
                	var feature = GEOR.Addons.Traveler.isochrone.fireRequest(service, settings);
                    if(feature && !feature.status){
                        // set style
                        feature.style = new OpenLayers.Style();
                        feature.style.strokeColor = config.ISOCHRONE_STROKE ? config.ISOCHRONE_STROKE : "rgba(255,255,255,0.4)";
                        // compare area to display feature in good order                        
                        isochrones.push(feature);
                        area.push(feature.geometry.getArea());
                        if (isochrones.length == times.length) {
                            // get area to compare
                            var areaMax = Math.max.apply(Math, area);
                            var areaMin = Math.min.apply(Math, area);
                            // insert isochrone to array in order with good style
                            function setPos(feature, pos, color) {
                                feature.style.fillColor = config.ISOCHRONES_COLOR[color];
                                feature.style.graphicZIndex = pos;
                                order[pos] = feature;
                            }
                            // Order geometry 
                            isochrones.forEach(function(feature) {
                                if(isochrones.length == 1){
                                	setPos(feature,2,0);
                                } else {
                                	// color and order depend on surface
                                	var measure = feature.geometry.getArea();                                    
                                    if (measure == areaMax) {
                                        setPos(feature, 0, 2);
                                    } else if (measure == areaMin) {
                                    	setPos(feature, 2, 0);
                                    } else {
                                        setPos(feature, 1, 1);
                                    }
                                }                                
                            });
                            // add to layer and zoom on layer extent
                            order.forEach(function(el, index) {
                                if (el && el.geometry) {
                                    addon.isoResLayer.addFeatures(el);
                                }
                            });
                            // insert research in result fieldset
                            var resultZone = Ext.getCmp("iso_result") ? Ext.getCmp("iso_result") : false;
                            if (resultZone) {
                                var pos = resultZone.items.length ? resultZone.items.length : 0;
                                var resCpf = new Ext.form.CompositeField({
                                    hideLabel: true,
                                    items: [{
                                        xtype: "checkbox",
                                        hideLabel: true,
                                        checked: true,
                                        listeners: {
                                            "check": function(el, checked) {
                                            	// get feature from result table corresponding to compositefield id
                                                var array = addon.isoResult[resCpf.id];
                                                // hide or show feature by style opacity
                                                function changeOpacity(array, val) {
                                                    array.forEach(function(el) {
                                                        el.style.fillOpacity = val;
                                                        el.style.strokeOpacity = val;
                                                        el.layer.redraw();
                                                    });
                                                }
                                                if (checked) {
                                                    changeOpacity(array, 1)
                                                } else {
                                                    changeOpacity(array, 0)
                                                }
                                            }
                                        }
                                    }, {
                                        xtype: "textfield", // create title of search
                                        width: 80,
                                        cls: "isochrone-textfield-time",
                                        value: tr("isochrone.resulttextfield.value") + (pos + 1)
                                    }, {
                                        xtype: "button", // create button to erase line and remove corresponding isochrones
                                        cls:"actionBtn",
                                        iconCls:"isochrone-button-clear",
                                        tooltip: tr("traveler.isochrone.button.clearoneresult.tooltip"),
                                        handler: function(button) {
                                            // thanks to parent id, we find geom to be erase  in isoResult object 
                                            var parent = button.findParentByType("compositefield");
                                            var isoFeatures = addon.isoResult[parent.id] && addon.isoResult[parent.id].length > 0 ? addon.isoResult[parent.id] : false;
                                            // delete complete line in window, key in feature object and layer features 
                                            if (isoFeatures) {
                                                addon.isoResLayer.removeFeatures(isoFeatures);
                                            }
                                            parent.destroy();
                                            delete addon.isoResult[parent.id]; // update table need to find line and corresponding isochrones
                                        }
                                    }, {
                                        xtype: "button", // button to save only one largest isochrones geometry corresponding to line
                                        iconCls: "isochrone-button-saveone",
                                        tooltip: tr("traveler.isochrone.button.saveonegeom.tooltip"),
                                        cls:"actionBtn",
                                        handler: function(button){
                                        	var parent = button.findParentByType("compositefield");
                                        	var isoFeature = addon.isoResult[parent.id] && addon.isoResult[parent.id].length > 0 ? addon.isoResult[parent.id] : false;
                                        	if(isoFeature){
                                        		var area = 0;
                                        		var largestGeom;
                                        		isoFeature.forEach(function(feature){
                                        			var geomArea = feature.geometry.getArea();
                                        			if(geomArea > area){
                                        				area = geomArea;
                                        				largestFeature = feature;
                                        			}                                                			
                                        		});
                                        		GEOR.Addons.Traveler.isochrone.storeGeometry([feature]); // store geometry in local Ext.state.Provider
                                        	}
                                        }
                                    }],

                                });
                                // use to delete geometry according to compositefield in isoResult object
                                // exemple : isoResult = {"id123":[feature1, feature2]}
                                addon.isoResult[resCpf.id] = isochrones;                                                                        
                                addon.map.zoomToExtent(addon.isoResLayer.getDataExtent()); // zoom to result layer extent 
                                resultZone.insert(pos, resCpf); // insert new result in window
                                Ext.getCmp("iso_win").doLayout(); // update window to reload layout
                            }
                        }                                       	
                    }                    
                } else {
                    Ext.Msg.alert(tr("isochrone.messagetitle.failrequest"), tr("isochrone.messagetext.failrequest"));
                	if(addon.loader()){
                		addon.loader().hide();
                	}
                }
                
            });            
            if(index == (startGeom.length-1)){ // hide waiter if last geom is calculate
            	if(addon.loader()){
            		addon.loader().hide();
            	}            	
            }
        }); 
    }        
};

/**
 * Create method to save geometry to local {Ext.state.provider}
 * @param {Array} features - List feature that will be encode to WKT
 * @returns error message or add geometry to local provider
 */

GEOR.Addons.Traveler.isochrone.storeGeometry = function(features) {
    // compute aggregation of geometries
    if (features ||  features.length > 0) {
        var components = [];
        var type;
        Ext.each(features, function(feature) {
            if (/OpenLayers\.Geometry\.Multi.*/.test(feature.geometry.CLASS_NAME)) {
                // multi-geometry
                Ext.each(feature.geometry.components, function(cmp) {
                    // check that we are not adding pears with bananas
                    if (!type) {
                        type = cmp.CLASS_NAME;
                        components.push(cmp.clone());
                    } else if (cmp.CLASS_NAME == type) {
                        components.push(cmp.clone());
                    }
                });
            } else {
                // simple geometry
                if (!type) {
                    type = feature.geometry.CLASS_NAME;
                    components.push(feature.geometry.clone());
                } else if (feature.geometry.CLASS_NAME == type) {
                    components.push(feature.geometry.clone());
                }
            }
        });
        // store the geometry for later use
        var singleType = type.substr(type.lastIndexOf('.') + 1),
            geometry = new OpenLayers.Geometry["Multi" + singleType](components);
        if (Ext.state.Manager.getProvider()) {
            var provider = Ext.state.Manager.getProvider();
            provider.set('geometry',
                provider.encodeValue(geometry.toString())
            );
            GEOR.util.infoDialog({
                msg: OpenLayers.i18n("Geometry successfully stored in this browser")
            });
        }
    }
};

/**
 *  Create or replace isochrone main window
 *  @param mode -{Ext.form.CompositeField} that contain travel mode
 *  @param exclusion -{Ext.form.CompositeField} that contain exclusions checkbox
 *  @param fSet - {Ext.form.FieldSet} fieldset that contains ban combobox, referential combobox
 *  @param addon  - Get attributes, objects from GEOR.Addon
 *  @param timeFields -{Ext.form.CompositeField} that contain time textfield and time number field
 *  @returns  {Ext.Window}
 */
GEOR.Addons.Traveler.isochrone.window = function(mode, fSet, exclusion, addon, timeFields) {
    var tr = OpenLayers.i18n;
    var win = new Ext.Window({
        id: "iso_win",
        title: tr("isochrone.window.title"),
        constrainHeader: true,
        shadow: false,
        autoHeight: true,
        width: 290,
        autoScroll: true,
        closable: true,
        closeAction: "close",
        resizable: true,
        collapsible: true,
        buttonAlign: "center",
        listeners: {
            "show": function(win) {
                if (addon.map) { // window location
                    win.alignTo(addon.map.div, "tl", [0, 100], false);
                }
            },
            "close": function() { // manage close event
                if (addon.isoLayer) {
                    addon.isoLayer.destroy();
                }
                if (addon.isoResLayer) {
                    addon.isoResLayer.destroy();
                }
            }
        },
        items: [{
            xtype: "panel",
            id: "iso_panel",
            items: [mode, fSet, {
                xtype: "fieldset",
                fieldLabel: "isochrones",
                cls: "isochrone-fieldset",
                id: "iso_timeFset",
                items: [timeFields]
            }, {
                xtype: "spacer",
                height: "3"
            }, {
                xtype: "fieldset",
                collapsible: true,
                collapsed: true,
                cls: "isochrone-fieldset-params",
                id: "iso_options",
                title: tr("Traveler.isochrone.options.title"),
                items: [timeFields, exclusion]
            }, {
                xtype: "spacer",
                height: "3"
            }, {
                xtype: "fieldset",
                collapsible: true,
                hidden: false,
                collapsed: true,
                cls: "isochrone-fieldset-params",
                id: "iso_result",
                title: tr("traveler.isochron.result.title"),
                listeners: {
                    "remove": function(f) {
                        if (f.items.length < 1) {
                            f.collapse();
                        }
                    },
                    "add": function(f) {
                        f.expand();
                    }
                }
            }]
        }],
        buttons: [{
            text: tr("traveler.isochrone.button.firerequest.text"), // button to fire isochrone calcul
            tooltip: tr("traveler.isochrone.button.firerequest.tooltip"),
            listeners: {
                "click": function(b) {
                	if(addon.isoStart && addon.isoStart["location"] && addon.isoStart["location"].length > 0){
                		addon.loader().show();
                		GEOR.Addons.Traveler.isochrone.createIsochrone(addon);                		
                	} else {
                		GEOR.util.infoDialog({
                            msg: tr("traveler.isochrone.msg.nogeom")
                        });
                	}         	                    
                }
            }
        }, {
            text: tr("traveler.isochrone.button.saveallgeom.text"), // button to save all largest geom
            tooltip: tr("traveler.isochrone.button.saveallgeom.tooltip"),
            listeners: {
                click: function(b) {
                    // get isochrones geom and store
                    var largestFeatures = [];
                    var o = addon.isoResult;
                    for (var key in o) {
                        if (o.hasOwnProperty(key)) {
                            var items = o[key];
                            var largest = items[0];
                            items.forEach(function(el) {
                                if (largest && largest.geometry && el.geometry.getArea() > largest.geometry.getArea()) {
                                    largest = el;
                                }
                            });
                            largestFeatures.push(largest);
                        }
                    };
                    GEOR.Addons.Traveler.isochrone.storeGeometry(largestFeatures); // fire function to load geom to local provider
                }
            }
        }]
    });

    return win;
};