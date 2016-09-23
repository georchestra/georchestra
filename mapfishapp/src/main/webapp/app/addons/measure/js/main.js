Ext.namespace("GEOR.Addons");

GEOR.Addons.Measure = Ext.extend(GEOR.Addons.Base, {

    popup: null,
    measureAreaControl: null,
    measureDistanceControl: null,
    measurePerimeterControl: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        var tr = OpenLayers.i18n;
        this.measureAreaControl = 
            this.createMeasureControl(OpenLayers.Handler.Polygon);
        this.measurePerimeterControl = 
            this.createMeasureControl(OpenLayers.Handler.Polygon);
        this.measureDistanceControl = 
            this.createMeasureControl(OpenLayers.Handler.Path);
        var items = [
            new Ext.menu.CheckItem(
                new GeoExt.Action({
                    text: tr("measure_area"),
                    qtip: tr("measure_area"),
                    control: this.measureAreaControl,
                    map: this.map,
                    group: "_measure",
                    iconCls: "measure-area"
                })
            ), new Ext.menu.CheckItem(
                new GeoExt.Action({
                    text: tr("measure_distance"),
                    qtip: tr("measure_distance"),
                    control: this.measureDistanceControl,
                    map: this.map,
                    group: "_measure",
                    iconCls: "measure-path"
                })
            ), new Ext.menu.CheckItem(
                    new GeoExt.Action({
                        text: tr("measure_perimeter"),
                        qtip: tr("measure_perimeter"),
                        control: this.measurePerimeterControl,
                        map: this.map,
                        group: "_measure",
                        iconCls: "measure-area"
                    })
              )
        ];
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, items);
            this.target.doLayout();
        } else {
            // addon outputs placed in "tools menu"
            this.items = items;
        }
    },


    /**
     * Method: createMeasureControl.
     * Create a measure control.
     *
     * Parameters:
     * handlerType - {OpenLayers.Handler.Path} or {OpenLayers.Handler.Polygon}
     *     The handler the measure control will use, depending whether
     *     measuring distances or areas.
     *
     * Returns:
     * {OpenLayers.Control.Measure} The control.
     */
    createMeasureControl: function(handlerType) {
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(null, {
                rules: [new OpenLayers.Rule({
                    symbolizer: this.options.graphicStyle
                })]
            })
        });
        var measureControl = new OpenLayers.Control.Measure(handlerType, {
            persist: true,
            geodesic: true,
            handlerOptions: {
                layerOptions: {
                    styleMap: styleMap
                }
            }
        });
        measureControl.events.on({
            "measurepartial": this.showPopup,
            "measure": this.showPopup,
            scope: this
        });
        return measureControl;
    },

    /**
     * Method: showPopup
     * 
     */
    showPopup: function(event) {
        this.popup && this.popup.destroy();
        if (this.measurePerimeterControl.active) {
        	this.popup = new GeoExt.Popup({
                map: this.map,
                title: OpenLayers.i18n("measure_popuptitle"),
                bodyStyle: "padding:5px;",
                unpinnable: true,
                closeAction: 'close',
                location: this.map.getCenter(),
                tpl: new Ext.Template('<div>{measureP} {unitsPM}<br>{measurePKM} {unitsPKM}</div>'),
                listeners: {
                    "close": function() {
                        this.measureAreaControl.deactivate();
                        this.measureDistanceControl.deactivate();
                        this.popup.destroy();
                        this.popup = null;
                    },
                    scope: this
                }
            })
        } else {
	        this.popup = new GeoExt.Popup({
	            map: this.map,
	            title: OpenLayers.i18n("measure_popuptitle"),
	            bodyStyle: "padding:5px;",
	            unpinnable: true,
	            closeAction: 'close',
	            location: this.map.getCenter(),
	            tpl: (event.order === 2) ? new Ext.Template("{measureM} {unitsM}<br>{measureKM} {unitsKM}<br>{measureH} {unitsH}") : new Ext.Template("{measureM} {unitsM}<br>{measureKM} {unitsKM}"),
	            listeners: {
	                "close": function() {
	                    this.measureAreaControl.deactivate();
	                    this.measureDistanceControl.deactivate();
	                    this.popup.destroy();
	                    this.popup = null;
	                },
	                scope: this
	            }
	        })
    	};
    	
    	var points = event.geometry.components;      
        if (points[0] instanceof OpenLayers.Geometry.LinearRing) {
            points = points[0].components;
        }
        
        // Get the perimeter from the feature points of the handler's geometry
        var verticesGeom = [];
        if (points.length != 0){
        	for(i=0; i < points.length; i++){
        		var point = new OpenLayers.Geometry.Point(points[i].x, points[i].y);
        		verticesGeom.push(point);
        	};        	
        	var ring = new OpenLayers.Geometry.LinearRing(verticesGeom);
            distGeom = Math.round(ring.getGeodesicLength(this.map.getProjectionObject()));
            distGeomUnits = "m";
        	console.log(distGeom + ' ' + distGeomUnits); 			
        	} 
        	if (event.measure > 0) {
                this.popup.location = points[points.length-1].getBounds().getCenterLonLat();
                this.popup.position();
                this.popup.show();
                this.popup.update({
                    measureM: (event.order === 2) ? ((event.units === 'km') ? (event.measure*1000000).toFixed(2) : event.measure.toFixed(2)) : ((event.units === 'km') ? (event.measure*1000).toFixed(2) : event.measure.toFixed(2)),
                    unitsM: (event.order === 2) ? tr("m2") : tr('m'),
                    measureKM: (event.order === 2) ? ((event.units === 'km') ? event.measure.toFixed(2) : (event.measure/1000000).toFixed(2)) : ((event.units === 'km') ? event.measure.toFixed(2) : (event.measure/1000).toFixed(2)),
                    unitsKM: (event.order === 2) ? tr("km2") : tr('km'),
                    measureH: (event.units === 'km') ? (event.measure*100).toFixed(2) : (event.measure/10000).toFixed(2),
                    unitsH: tr("hectare"),
                    measureP: distGeom,
                    unitsPM:tr('m'),
                    measurePKM:(distGeom/1000).toFixed(2),
                    unitsPKM: tr('km')
                });
            }
        },
    
  
    
    /**
     * Method: destroy
     * 
     */
    destroy: function() {
        this.measureAreaControl.deactivate();
        this.measureDistanceControl.deactivate();
        this.measurePerimeterControl.deactivate();
        this.popup && this.popup.destroy();
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});