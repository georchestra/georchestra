/*global
 Ext, OpenLayers, GeoExt, GEOR
 */
Ext.namespace("GEOR.Addons");

GEOR.Addons.Atlas = Ext.extend(GEOR.Addons.Base, {
    window: null,
    layer: null,
    title: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        // create a menu item for the "tools" menu:
        this.item = new Ext.menu.CheckItem({
            text: this.getText(record),
            qtip: this.getQtip(record),
            iconCls: "atlas-icon",
            checked: false,
            listeners: {
                "click": {
                    fn: this.menuAction,
                    scope: this
                }
            }
        });
        this.title = this.getText(record);
    },

    /**
     * Method menuAction
     */

    menuAction: function(atlas, e) {
        var atlasLayersStore = new GeoExt.data.LayerStore({
            fields: this.mapPanel.layers.fields.items
        });
        this.mapPanel.layers.each(function(layerRecord) {
            if (layerRecord.get("queryable")) {
                atlasLayersStore.add(layerRecord);
            }
        });
        this.window = new Ext.Window({
            title: this.title,
            width: 420,
            height: 360,
            closable: true,
            items: [{
                xtype: "form",
                items: [
                    {
                        xtype: "combo",
                        name: "layer",
                        fieldLabel: this.tr("atlas_selectlayer"),
                        mode: "local",
                        triggerAction: "all",
                        store: atlasLayersStore,
                        valueField: "name",
                        displayField: "title",
                        allowBlank: false
                    },
                    {
                        xtype: "combo",
                        name: "outputFormat",
                        //TODO tr
                        fieldLabel: "Format",
                        value: "pdf",
                        mode: "local",
                        triggerAction: "all",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["formatId", "formatDescription"],
                            data: [
                                ["pdf", "PDF"],
                                ["zip", "zip"]
                            ]
                        },
                        valueField: "formatId",
                        displayField: "formatDescription",
                        allowBlank: false
                    },
                    {
                        xtype: "combo",
                        name: "layout",
                        //TODO tr
                        fieldLabel: "Layout",
                        value: "A4 portrait",
                        mode: "local",
                        triggerAction: "all",
                        store: {
                            xtype: "arraystore",
                            id: 0,
                            fields: ["layoutId", "layoutDescription"],
                            //TODO tr
                            data: [
                                ["A4 portrait", "A4 portrait"],
                                ["A4 landscape", "A4 landscape"],
                                ["A3 portrait", "A3 portrait"],
                                ["A3 landscape", "A3 landscape"]
                            ]
                        },
                        valueField: "layoutId",
                        displayField: "layoutDescription"
                    },
                    {
                        xtype: "hidden",
                        name: "dpi",
                        //TODO improve management of dpi
                        value: "216"
                    },
                    {
                        xtype: "hidden",
                        name: "projection",
                        value: this.map.projection

                    }

                ],
                buttons: [
                    {
                        //TODO tr
                        text: "Submit",
                        handler: function(b) {
                            var formValues, layersRelatedValues,
                                json = new OpenLayers.Format.JSON();
                            if (b.findParentByType("form").getForm().isValid()) {
                                formValues = b.findParentByType("form").getForm().getFieldValues();
                                layersRelatedValues = this.createPagesSpecs(formValues["layer"]);
                                formValues.featureLayer = layersRelatedValues.featureLayer;
                                formValues.baseLayers = layersRelatedValues.baseLayers;
                                //We do not use form.submit to keep json object
                                OpenLayers.Request.POST({
                                    url: GEOR.config.PATHNAME + "/ws/atlas",
                                    data: json.write(formValues)
                                });

                            }
                        },
                        scope: this
                    },
                    {
                        //TODO tr
                        text: "Cancel"
                    }
                ],
                scope: this
            }],
            scope: this
        });
        this.window.show();
    },

    /**
     * Method createPagesSpecs
     * TODO Check MFP v3 layers/type specification (order is important)
     */
    createPagesSpecs: function(layerName) {
        var layer = null;
        var extraConfig = {};
        var baseLayers = [];
        this.mapPanel.layers.each(function(layerStore) {
            layer = layerStore.get("layer");
            if (layerStore.get("name") == layerName) {
                extraConfig.featureLayer = {};
                extraConfig.featureLayer.type = layerStore.get("type");
                extraConfig.featureLayer.baseURL = layer.url;
                extraConfig.featureLayer.layer = layer.params.LAYERS;
                extraConfig.featureLayer.version = layer.params.VERSION;
                if (layer.params.TRANSPARENT) {
                    extraConfig.featureLayer.customParams = {
                        transparent: true
                    };
                }

            } else if (layer.isBaseLayer) {
                var baseLayersItem = {},
                    extension;
                baseLayersItem.type = layerStore.get("type");
                baseLayersItem.baseURL = layer.url;
                switch (layer.params.FORMAT) {
                    case "image/png":
                        extension = ".png";
                        break;
                    case "image/gif":
                        extension = ".gif";
                        break;
                    case "image/jpeg":
                        //could be .jpg
                        extension = ".jpeg";
                        break;
                    case "image/tiff":
                        //could be .tif
                        extension = ".tiff";
                        break;
                    case "image/svg+xml":
                        extension = ".svg";
                        break;
                }
                baseLayersItem.imageExtension = extension;
                baseLayersItem.opacity = layer.opacity;

                baseLayers.splice(0, -1, baseLayersItem);
            }
        });

        extraConfig.baseLayers = baseLayers;

        return extraConfig;
    },

    /**
     * Method: tr
     *
     */
    tr: function(a) {
        return OpenLayers.i18n(a);
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});