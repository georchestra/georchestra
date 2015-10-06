Ext.namespace("GEOR.Addons");

GEOR.Addons.Measurements = Ext.extend(GEOR.Addons.Base, {

    window: null,
    toggleGroup: 'measurements',

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.layer = new OpenLayers.Layer.Vector('__georchestra_measurements', {
            styleMap: new OpenLayers.StyleMap(
                new OpenLayers.Style(null, {
                    rules: [
                        new OpenLayers.Rule({
                            symbolizer: OpenLayers.Control.DynamicMeasure.styles
                        })
                    ]
                })
            )
        });
        this.layer2 = new OpenLayers.Layer.Vector('__georchestra_measurements2', {
            styleMap: new OpenLayers.StyleMap(
                new OpenLayers.Style(null, {
                    rules: [
                        new OpenLayers.Rule({
                            symbolizer: OpenLayers.Control.DynamicMeasure.styles
                        })
                    ]
                })
            )
        });
        this.layer3 = new OpenLayers.Layer.Vector('__georchestra_measurements3', {
            styleMap: new OpenLayers.StyleMap(
                new OpenLayers.Style(null, {
                    rules: [
                        new OpenLayers.Rule({
                            symbolizer: OpenLayers.Control.DynamicMeasure.styles
                        })
                    ]
                })
            )
        });
        this.layer4 = new OpenLayers.Layer.Vector('__georchestra_measurements4', {
            styleMap: new OpenLayers.StyleMap(
                new OpenLayers.Style(null, {
                    rules: [
                        new OpenLayers.Rule({
                            symbolizer: OpenLayers.Control.DynamicMeasure.styles
                        })
                    ]
                })
            )
        });
        this.lengthAction =  new GeoExt.Action({
            control: new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Path, {
                maxSegments: null,
                geodesic: true,
                drawingLayer: this.layer,
                layerSegments: this.layer2,
                layerLength: this.layer3,
                layerArea: this.layer4
            }),
            map: this.map,
            // button options
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.distance.tooltip"),
            iconCls: 'measurements-length',
            text: OpenLayers.i18n("measurements.distance"),
            iconAlign: 'top'
        });
        this.areaAction =  new GeoExt.Action({
            control: new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Polygon, {
                maxSegments: null,
                geodesic: true,
                drawingLayer: this.layer,
                layerSegments: this.layer2,
                layerLength: this.layer3,
                layerArea: this.layer4
            }),
            map: this.map,
            // button options
            toggleGroup: this.toggleGroup,
            allowDepress: true,
            pressed: false,
            minWidth: 50,
            tooltip: this.tr("measurements.area.tooltip"),
            iconCls: 'measurements-area',
            text: OpenLayers.i18n("measurements.area"),
            iconAlign: 'top'
        });
        this.window = new Ext.Window({
            title: OpenLayers.i18n('measurements.tools'),
            width: 200,
            closable: true,
            closeAction: "hide",
            resizable: false,
            border: false,
            cls: 'measurements',
            items: [{
                xtype: 'toolbar',
                border: false,
                items: [this.lengthAction, this.areaAction]
            }],
            listeners: {
                'show': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) < 0) {
                        this.map.addLayer(this.layer);
                    }
                },
                'hide': function() {
                    if (OpenLayers.Util.indexOf(this.map.layers, this.layer) > 0) {
                        this.map.removeLayer(this.layer);
                    }
                },
                scope: this
            }
        });
        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: "addon-measurements",
                handler: this._onCheckchange,
                scope: this
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item =  new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-measurements",
                checked: false,
                listeners: {
                    "checkchange": this._onCheckchange,
                    scope: this
                }
            });
        }
    },

    /**
     * Method: tr
     *
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    },

    /**
     * Method: _onCheckchange
     * Callback on checkbox state changed
     */
    _onCheckchange: function(item, checked) {
        if (checked) {
            this.window.show();
            this.window.alignTo(
                Ext.get(this.map.div),
                "t-t",
                [0, 5],
                true
            );
        } else {
            this.window.hide();
        }
    },

    /**
     * Method: destroy
     * 
     */
    destroy: function() {
        this.window.hide();
        this.layer.destroy();
        this.layer = null;
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});