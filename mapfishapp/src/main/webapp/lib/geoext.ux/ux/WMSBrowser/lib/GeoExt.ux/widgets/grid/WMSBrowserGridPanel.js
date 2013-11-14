/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux.grid");

/*
 * @requires GeoExt.ux/widgets/WMSBrowser.js
 */

/** api: (define)
 *  module = GeoExt.ux.grid
 *  class = WMSBrowserWMSCapabilitiesStore
 */

/** api: constructor
 *  .. class:: WMSBrowserWMSCapabilitiesStore
 */
GeoExt.ux.grid.WMSBrowserGridPanel = Ext.extend(Ext.grid.GridPanel, {

    layout: 'absolute',

    x: 0,

    y: 0,

    region: 'center',

    anchor: '50% 100%',

    store: null,

    columns: null,

    sm: null,

    autoExpandColumn: "title",

    width: 'auto',

    autoWidth: true,

    border: true,

    /** api: config[wmsbrowser]
     * :class:`GeoExt.ux.data.WMSBrowser` A reference to the main browser object
     */
    wmsbrowser: null,

    /** private: property[map]
     * :class:`OpenLayers.Map` A reference to the map object.  Taken from the
     * wmsbrowser object.
     */
    map: null,

    /** private: property[layerStore]
     * :class:`GeoExt.data.LayerStore` A reference to the layer store object.
     * Taken from the wmsbrowser object.
     */
    layerStore: null,

    /** private: method[constructor]
     */
    constructor: function(config) {
        Ext.apply(this, config);

        this.layerStore = this.wmsbrowser.layerStore;
        this.map = this.layerStore.map;

        var checkboxSelectionModel = new Ext.grid.CheckboxSelectionModel({
            singleSelect: false,
            renderer: function(value, metaData, record) {
                // Hide checkbox for certain records
                if(record.get('srsCompatible') == false ||
                   record.get('extentCompatible' == false)) {
                    return;
                }
                return Ext.grid.CheckboxSelectionModel.prototype.renderer.apply(
                    this, arguments
                );
            },
            listeners: {
                beforerowselect: function(sm, row, keep, rec) {
                    return sm.scope.isRecordSelectable(rec);
                },
                rowselect: function(sm, row, rec) {
                    sm.scope.wmsbrowser.centerPanel.getForm().loadRecord(rec);
                    sm.scope.addLayerToPreview(rec);
                    sm.scope.setLayerNameFromSelectedRecords();
                },
                rowdeselect: function(sm, row, rec) {
                    sm.scope.wmsbrowser.centerPanel.getForm().reset();
                    sm.scope.removeLayerFromPreview(rec);
                    sm.scope.setLayerNameFromSelectedRecords();
                }
            },
            scope: this
        });

        var columns = [
            checkboxSelectionModel,
            { header: this.wmsbrowser.srsCompatibleText, scope: this,
              dataIndex: "srsCompatible", hidden: false,
              renderer: this.boolRenderer, width: 30, hidden: true},
            { header: this.wmsbrowser.extentCompatibleText, scope: this,
              dataIndex: "extentCompatible", hidden: false,
              renderer: this.boolRenderer, width: 30, hidden: true},
            { header: this.wmsbrowser.titleText, scope: this,
              dataIndex: "title", id: "title", sortable: true},
            { header: this.wmsbrowser.nameText, scope: this,
              dataIndex: "name", sortable: true},
            { header: this.wmsbrowser.queryableText, scope: this,
              dataIndex: "queryable", sortable: true, hidden: true, 
              renderer: this.boolRenderer, width: 30},
            { header: this.wmsbrowser.descriptionText, scope: this,
              dataIndex: "abstract", hidden: true}
        ];

        Ext.apply(this, {columns: columns, sm: checkboxSelectionModel});

        arguments.callee.superclass.constructor.call(this, config);
    },

    /** private: method[boolRenderer]
     *  :param bool: ``Boolean``
     *
     *  Renders boolean values in color and with alternate text value.
     */
    boolRenderer: function(bool) {
        return (bool)
            ? '<span style="color:green;">'+this.wmsbrowser.yesText+'</span>'
            : '<span style="color:red;">'+this.wmsbrowser.noText+'</span>';
    },

    /** private: method[addLayerToPreview]
     *  :param record: ``GeoExt.data.LayerRecord``  The selected layer record
     *
     *  Add selected layer records to the exiting
     *  :class:`GeoExt.data.LayerRecord` object if it exists else create a new
     *  one, then recenter the map preview on the newly added data.
     */
    addLayerToPreview: function(record) {
        if (!this.wmsbrowser.layerPreview) {
            this.wmsbrowser.layerPreview = record.clone();
            this.wmsbrowser.layerPreview.data.layer = record.data.layer.clone();
            this.wmsbrowser.layerPreview.get("layer").mergeNewParams({
                format: "image/png",
                transparent: "true"
            });

            this.wmsbrowser.layerPreview.get("layer").mergeNewParams(
                {'LAYERS': [this.wmsbrowser.layerPreview.get("layer").params.LAYERS]}
            );

            this.wmsbrowser.mapPanelPreview.layers.add(this.wmsbrowser.layerPreview);

            if (this.wmsbrowser.mapPanelPreview.collapsed) {
                this.wmsbrowser.hideLayerPreview();
            }

            this.wmsbrowser.zoomToRecordLLBBox(this.wmsbrowser.layerPreview);
        } else {
            this.wmsbrowser.layerPreview.get("layer").params.LAYERS.push(
                record.get("layer").params.LAYERS
            );
            this.wmsbrowser.layerPreview.get("layer").mergeNewParams(
                {'LAYERS': this.wmsbrowser.layerPreview.get("layer").params.LAYERS}
            );
            this.wmsbrowser.zoomToRecordLLBBox(record, false);
        }
    },

    /** private: method[removeLayerFromPreview]
     *  :param record: ``GeoExt.data.LayerRecord``  The unselected layer record
     *
     *  Remove the unselected layer from the :class:`GeoExt.data.LayerRecord`
     *  params.
     */
    removeLayerFromPreview: function(record) {
        if (!this.wmsbrowser.layerPreview) {
            return;
        }

        var layers = this.wmsbrowser.layerPreview.get("layer").params.LAYERS;
        var index = OpenLayers.Util.indexOf(
            layers, record.get("layer").params.LAYERS
        );

        if (index != -1) {
            layers.splice(index, 1);
        }

        this.wmsbrowser.layerPreview.get("layer").mergeNewParams({
            'LAYERS': layers
        });
    },

    /** private: method[getLayerNameFromSelectedRecords]
     *  :return:  ``String`` The string of all selected layer
     *
     *  Get all currently selected layer record 'title' or 'name', merge them
     *  together in a single string separated by ',' and return it.
     */
    getLayerNameFromSelectedRecords: function() {
        var layerName = [];
        var records = this.getSelectionModel().getSelections();

        for (var i=0, len=records.length; i<len; i++) {
            var record = records[i];
            if (record.get('title') != "") {
                layerName.push(record.get('title'));
            } else if (record.get('name') != "") {
                layerName.push(record.get('name'));
            }
        }

        return layerName.join(', ');
    },

    /** private: method[setLayerNameFromSelectedRecords]
     *  Set the layerName field value to all the selected layer record 'title'
     *  or 'name'. 
     */
    setLayerNameFromSelectedRecords: function() {
        this.wmsbrowser.layerNameField.setValue(
            this.getLayerNameFromSelectedRecords()
        );
    },

    /** private: method[]
     *  :param record: ``GeoExt.data.LayerRecord``  The layer record to check
     *
     *  :return:  ``Boolean`` Wheter the record is selectable or not.
     *
     *  Check if a layer can be selected by checking its 'srsCompatible' and
     *  'extentCompatible' properties.  Throw errors if it's not.
     */
    isRecordSelectable: function(record) {
        var compatible = true;
        var reasons = [];

        // check if srs is valid
        if (!record.get("srsCompatible")) {
            compatible = false;
            reasons.push(
                this.wmsbrowser.srsNotSupportedShortText +
                " (" + this.map.getProjection() + ")"
            );
        }

        // check if exent is valid
        if (!record.get("extentCompatible")) {
            compatible = false;
            reasons.push(
                this.wmsbrowser.extentNotSupportedShortText +
                " (" + this.map.getExtent().toBBOX() + ")"
            );
        }

        // output a message if not valid
        if (!compatible) {
            var layerName = "";
            if (record.get('title') != "") {
                layerName = record.get('title') + " : ";
            } else if (record.get('name') != "") {
                layerName = record.get('name') + " : ";
            }
            var message = layerName + this.wmsbrowser.layerCantBeAddedText + reasons.join(', ');
            this.wmsbrowser.fireEvent('genericerror', message);
        }

        return compatible;
    },

    /** private: method[addLayer]
     *
     *  From all currently selected layer records, create a single
     *  :class:`GeoExt.data.LayerRecord` object and add it to the
     *  :class:`GeoExt.data.LayerStore` object.  Called when the user clicks the
     *  'addLayer' button.
     */
    addLayer: function() {
        var records = this.getSelectionModel().getSelections();

        // VALIDATION : record selection or connection established
        if (records.length == 0) {
            // if no record was selected
            if(this.store.getTotalCount() > 0) {
                this.wmsbrowser.fireEvent(
                    'genericerror', this.wmsbrowser.pleaseSelectALayerText
                );
            } else {
                this.wmsbrowser.fireEvent(
                    'genericerror', this.wmsbrowser.pleaseInputURLText
                );
            }

            return;
        }

        if (!this.wmsbrowser.isLayerNameValid()) {
            return;
        }

        var newlayerRecord, layersParam = [];

        for (var i=0, len=records.length; i<len; i++) {
            var record = records[i];

            // check the projection of the map is supported by the layer
            if (record.get("srsCompatible") === false) {
                alert( this.wmsbrowser.srsNotSupportedText);
                continue;
            }

            if (!newLayerRecord) {
                var newLayerRecord = record.clone();

                // the following line gives a "too much recursion" error.
                //newLayerRecord.set("layer", record.get("layer"));
                newLayerRecord.data.layer = record.data.layer.clone();

                newLayerRecord.get("layer").mergeNewParams({
                    format: "image/png",
                    transparent: "true"
                });

                layersParam.push(newLayerRecord.get("layer").params.LAYERS);
            } else {
                layersParam.push(record.get("layer").params.LAYERS);
            }

        }

        if (newLayerRecord) {
            newLayerRecord.get("layer").mergeNewParams(
                {'LAYERS': layersParam}
            );

            newLayerRecord.get("layer").name = 
                this.wmsbrowser.layerNameField.getValue();

            var addLayer = this.wmsbrowser.fireEvent(
                'beforelayeradded', {'layerRecord': newLayerRecord}
            );

            if (addLayer !== false) {
                this.layerStore.add(newLayerRecord);
                this.wmsbrowser.fireEvent(
                    'layeradded', {'layerRecord': newLayerRecord}
                );
                if(this.wmsbrowser.zoomOnLayerAdded) {
                    // zoom to added layer extent (in the current map projection)
                    this.map.zoomToExtent(
                        OpenLayers.Bounds.fromArray(newLayerRecord.get("llbbox")).transform(
                            new OpenLayers.Projection("EPSG:4326"),
                            new OpenLayers.Projection(
                                this.map.getProjection()))
                    );
                }
            }
        }
    }
});
