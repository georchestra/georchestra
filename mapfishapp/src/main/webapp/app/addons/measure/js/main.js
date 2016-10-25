Ext.namespace("GEOR.Addons");

GEOR.Addons.Measure = Ext.extend(GEOR.Addons.Base, {

    popup: null,
    measureAreaControl: null,
    measureDistanceControl: null,

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
            geodesic: GEOR.config.MAP_SRS != "EPSG:4326",
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
        this.popup = new GeoExt.Popup({
            map: this.map,
            title: OpenLayers.i18n("measure_popuptitle"),
            bodyStyle: "padding:5px;",
            unpinnable: true,
            closeAction: 'close',
            location: this.map.getCenter(),
            tpl: new Ext.Template("{measure} {units}"),
            listeners: {
                "close": function() {
                    this.measureAreaControl.deactivate();
                    this.measureDistanceControl.deactivate();
                    this.popup.destroy();
                    this.popup = null;
                },
                scope: this
            }
        });
        var points = event.geometry.components;
        if (points[0] instanceof OpenLayers.Geometry.LinearRing) {
            points = points[0].components;
        }
        if (event.measure > 0) {
            this.popup.location = points[points.length-1].getBounds().getCenterLonLat();
            this.popup.position();
            this.popup.show();
            this.popup.update({
                measure: event.order == 2 ?
                    // area measurement, order = 2
                    (event.units == "m" ?
                        (event.measure/10000).toFixed(this.options.decimals) :
                        (event.measure*100).toFixed(this.options.decimals)) :
                    // distance measurement, order = 1
                    event.measure.toFixed(this.options.decimals),
                units: event.order == 2 ?
                    // area measurement
                    tr("hectares") :
                    // distance measurement
                    event.units
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
        this.popup && this.popup.destroy();
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});