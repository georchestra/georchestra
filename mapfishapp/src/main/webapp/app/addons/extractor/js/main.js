Ext.namespace("GEOR.Addons");

/*
 * TODO: 
 * - handle ACLs
 * - wizard (1 choose layers (NOK report here) 2 choose extent 3 choose formats 4 enter email )
 * - modifyFeature control improved: non symetrical mode when OpenLayers.Control.ModifyFeature.RESIZE
 */
GEOR.Addons.Extractor = Ext.extend(GEOR.Addons.Base, {
    win: null,
    jsonFormat: null,
    layer: null,
    modifyControl: null,
    item: null,
    srsField: null,
    vectorFormatField: null,
    rasterFormatField: null,
    resField: null,
    emailField: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        this.jsonFormat = new OpenLayers.Format.JSON();
        var style = {
            externalGraphic: GEOR.config.PATHNAME + "/app/addons/extractor/img/shading.png",
            graphicWidth: 16,
            graphicHeight: 16,
            graphicOpacity: 1,
            graphicXOffset: -8,
            graphicYOffset: -8,
            graphicZIndex: 10000,
            strokeColor: "fuchsia",
            strokeWidth: 2,
            fillOpacity: 0,
            cursor: "pointer"
        };
        this.layer = new OpenLayers.Layer.Vector("__georchestra_extractor", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": Ext.applyIf({}, style),
                "select": Ext.applyIf({}, style)
            })
        });
        this.modifyControl = new OpenLayers.Control.ModifyFeature(this.layer, {
            standalone: true,
            mode: OpenLayers.Control.ModifyFeature.RESHAPE | 
                OpenLayers.Control.ModifyFeature.RESIZE | 
                OpenLayers.Control.ModifyFeature.DRAG,
            autoActivate: true
        });

        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record),
                iconCls: 'extractor-icon',
                handler: this.showWindow,
                scope: this
            });
            this.target.doLayout();
        } else {
            // create a menu item for the "tools" menu:
            this.item =  new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: 'extractor-icon',
                handler: this.showWindow,
                scope: this
            });
        }
        if (this.options.showWindowOnStartup == true) {
            this.showWindow();
        }
    },
    
    createWindow: function() {
        var FIELD_WIDTH = 170,
            base = {
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                mode: 'local',
                width: FIELD_WIDTH,
                labelSeparator: OpenLayers.i18n("labelSeparator"),
                valueField: 'value',
                displayField: 'text'
        };
        this.srsField = new Ext.form.ComboBox(Ext.apply({
            name: "srs",
            fieldLabel: OpenLayers.i18n("SRS"),
            value: this.options.defaultSRS,
            store: new Ext.data.SimpleStore({
                fields: ['value', 'text'],
                data: this.options.srsData
            })
        }, base));
        this.vectorFormatField = new Ext.form.ComboBox(Ext.apply({
            name: "vectorFormat",
            fieldLabel: OpenLayers.i18n("Format for vectors"),
            value: this.options.defaultVectorFormat,
            store: new Ext.data.SimpleStore({
                fields: ['value', 'text'],
                data: this.options.vectorFormatData
            })
        }, base));
        this.rasterFormatField = new Ext.form.ComboBox(Ext.apply({
            name: "rasterFormat",
            fieldLabel: OpenLayers.i18n("Format for rasters"),
            value: this.options.defaultRasterFormat,
            store: new Ext.data.SimpleStore({
                fields: ['value', 'text'],
                data: this.options.rasterFormatData
            })
        }, base));
        this.resField = new Ext.form.NumberField({
            fieldLabel: OpenLayers.i18n("Resolution for rasters (cm)"),
            name: "resolution",
            width: FIELD_WIDTH,
            labelSeparator: OpenLayers.i18n("labelSeparator"),
            value: this.options.defaultRasterResolution,
            decimalPrecision: 0
        });
        this.emailField = new Ext.form.TextField({
            name: "email",
            vtype: "email",
            vtypeText: OpenLayers.i18n('This field should be an e-mail address in the format user@domain.com'),
            allowBlank: false,
            width: FIELD_WIDTH,
            labelSeparator: OpenLayers.i18n("labelSeparator"),
            value: GEOR.config.USEREMAIL || "",
            fieldLabel: OpenLayers.i18n("Email")
        });
        return new Ext.Window({
            closable: true,
            closeAction: 'hide',
            width: 330,
            height: 270,
            title: OpenLayers.i18n("addon_extractor_popup_title"),
            border: false,
            buttonAlign: 'left',
            layout: 'fit',
            items: [{
                xtype: 'form',
                labelWidth: 120,
                bodyStyle: "padding:5px;",
                items: [
                    this.srsField, 
                    this.vectorFormatField, 
                    this.rasterFormatField, 
                    this.resField, 
                    this.emailField
                ]
            }],
            fbar: ['->', {
                text: OpenLayers.i18n("Close"),
                handler: function() {
                    this.win.hide();
                },
                scope: this
            }, {
                text: OpenLayers.i18n("Extract"),
                handler: this.extract,
                scope: this
            }],
            listeners: {
                "show": function() {
                    if (!this.layer.features.length) {
                        this.layer.addFeatures([
                            new OpenLayers.Feature.Vector(
                                this.map.getExtent().scale(0.83).toGeometry()
                            )
                        ]);
                    }
                    this.map.addLayer(this.layer);
                    this.map.zoomToExtent(
                        this.layer.features[0].geometry.getBounds().scale(1.2)
                    );
                    this.map.addControl(this.modifyControl);
                    this.modifyControl.selectFeature(this.layer.features[0]);
                },
                "hide": function() {
                    this.map.removeLayer(this.layer);
                    this.modifyControl.unselectFeature(this.layer.features[0]);
                    this.map.removeControl(this.modifyControl);
                    this.item && this.item.setChecked(false);
                    this.components && this.components.toggle(false);
                },
                scope: this
            }
        });
    },

    showWindow: function() {
        if (!this.win) {
            this.win = this.createWindow();
        }
        this.win.show();
    },

    doExtract: function(okLayers) {
        var bbox = this.layer.features[0].geometry.getBounds();
        var spec = {
            "emails": [this.emailField.getValue()],
            "globalProperties": {
                "projection": this.srsField.getValue(),
                "resolution": parseInt(this.resField.getValue())/100,
                "rasterFormat": this.rasterFormatField.getValue(),
                "vectorFormat": this.vectorFormatField.getValue(),
                "bbox": {
                    "srs": this.map.getProjection(),
                    "value": bbox.toArray()
                }
            },
            "layers": okLayers
        };
        GEOR.waiter.show();
        Ext.Ajax.request({
            url: this.options.serviceURL,
            success: function(response) {
                if (response.responseText &&
                    response.responseText.indexOf('<success>true</success>') > 0) {
                    this.win.hide();
                    GEOR.util.infoDialog({
                        msg: OpenLayers.i18n('The extraction request succeeded, check your email.')
                    });
                } else {
                    GEOR.util.errorDialog({
                        msg: OpenLayers.i18n('The extraction request failed.')
                    });
                }
            },
            failure: function(response) {
                GEOR.util.errorDialog({
                    msg: OpenLayers.i18n('The extraction request failed.')
                });
            },
            jsonData: this.jsonFormat.write(spec),
            scope: this
        });
    },

    extract: function() {
        if (!this.emailField.isValid()) {
            return;
        }
        var okLayers = [];
        this.mapPanel.layers.each(function(r) {
            var layer = r.getLayer();
            if (!layer.getVisibility() || !layer.url) {
                return;
            }
            if (r.get("WCS_typeName") && r.get("WCS_URL")) {
                okLayers.push({
                    "owsUrl": r.get("WCS_URL"),
                    "owsType": "WCS",
                    "layerName": r.get("WCS_typeName")
                });
            }
            if (r.get("WFS_typeName") && r.get("WFS_URL")) {
                okLayers.push({
                    "owsUrl": r.get("WFS_URL"),
                    "owsType": "WFS",
                    "layerName": r.get("WFS_typeName")
                });
            }
        });
        if (okLayers.length > 0) {
            this.doExtract(okLayers);
        } else {
            GEOR.util.infoDialog({
                msg: OpenLayers.i18n('No layer is available for extraction.')
            });
        }
    },

    destroy: function() {
        this.win && this.win.hide();
        this.layer = null;
        this.jsonFormat = null;
        this.modifyControl = null;
        
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});