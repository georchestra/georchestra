/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux.data");

/*
 * @requires GeoExt.ux/widgets/WMSBrowser.js
 */

/** api: (define)
 *  module = GeoExt.ux.data
 *  class = WMSBrowserWMSCapabilitiesStore
 */

/** api: constructor
 *  .. class:: WMSBrowserWMSCapabilitiesStore
 */
GeoExt.ux.data.WMSBrowserWMSCapabilitiesStore = Ext.extend(GeoExt.data.WMSCapabilitiesStore, {

    /** api: config[url]
     * ``String`` The url of the WMSGetCapabilities request
     */
    url: null,

    /** api: config[layerOptions]
     * ``Object`` Optional object passed as default options to the
     * :class:`OpenLayers.Layer.WMS` constructor.
     */
    layerOptions: null,

    /** api: config[wmsbrowser]
     * :class:`GeoExt.ux.data.WMSBrowser` A reference to the main browser object
     */
    wmsbrowser: null,

    /** api: config[gridPanel]
     * :class:`Ext.grid.GridPanel` A reference to the grid panel containing the
     * layer records
     */
    gridPanel: null,

    /** private: property[map]
     * :class:`OpenLayers.Map` A reference to the map object.  Taken from the
     * wmsbrowser object.
     */
    map: null,

    /** private: method[constructor]
     */
    constructor: function(config) {
        Ext.apply(this, config);
        this.map = this.wmsbrowser.layerStore.map;
        arguments.callee.superclass.constructor.call(this, config);
        // event registration
        this.on('load', this.onCapabilitiesLoad, this);
        this.on('loadexception', this.onCapabilitiesLoadException, this);
    },

    /** private: method[onCapabilitiesLoad]
     *  :param store: :class:`GeoExt.data.WMSGetCapabilitiesStore`
     *  :param records: ``Array(GeoExt.data.LayerRecord)``
     *  :param options: ``Object``
     *
     *  Called after a GetCapabilities request.  If the request returned
     *  records, that means it was a valid server so add it in the dropdown
     *  list.
     *
     *  For each returned layer records, validate that it supports the map's
     *  projection and intersects the map's extent.
     */
    onCapabilitiesLoad: function(store, records, options) {
        var srs = this.map.getProjection();

        var mapMaxExtent = this.map.getMaxExtent().clone().transform(
            new OpenLayers.Projection(this.map.getProjection()),
            new OpenLayers.Projection('EPSG:4326')
        );

        this.wmsbrowser.resetLayerPreview();

        // loop through all records (layers) to see if they contain the current
        // map projection and intersects the map extent.
        for(var i=0; i<records.length; i++) {
            var record = records[i];

            // Check if the 'srs' contains a 'key' named by the srs OR
            // Check if the 'srs' is an array and contains the srs
            if(record.get('srs')[srs] === true ||
               OpenLayers.Util.indexOf(record.get('srs'), srs) >= 0) {
                record.set("srsCompatible", true);
            } else {
                if(srs == 'EPSG:900913' && record.get('srs')['EPSG:3857'] === true) {
                    srs = 'EPSG:3857';
                    record.set("srsCompatible", true);
                }
                else {
                    record.set("srsCompatible", false);
                }
            }

            // Check if the llbbox 
            var layerExtent = record.get("llbbox");
            var extent;
            if (layerExtent) 
            {
                if(typeof layerExtent == "string") {
                    extent = OpenLayers.Bounds.fromString(layerExtent);
                } else if(layerExtent instanceof Array) {
                    extent = OpenLayers.Bounds.fromArray(layerExtent);
                }
            }

            if (!extent || mapMaxExtent.intersectsBounds(extent, false)) {
                record.set("extentCompatible", true);
            } else {
                record.set("extentCompatible", false);
            }
        }

        if(this.getCount() > 0) {
            this.wmsbrowser.fireEvent('getcapabilitiessuccess');

            // select the first element of the list on load end
            if (this.gridPanel && this.wmsbrowser.selectFirstRecordOnStoreLoad){
                this.gridPanel.getSelectionModel().selectRow(0);
            }

            // the url that was used was a valid WMS server, keep it if the
            // url field is a combobox and if it's not already added
            var xtype = this.wmsbrowser.serverComboBox.getXType();
            if(xtype == Ext.form.ComboBox.xtype) {
                var aszUrls = 
                    this.wmsbrowser.serverComboBox.store.getValueArray('url');
                var index = OpenLayers.Util.indexOf(
                    aszUrls, this.wmsbrowser.currentUrl
                );
                if(index == -1) {
                    var record = new Ext.data.Record({
                        'url': this.wmsbrowser.currentUrl
                    });
                    this.wmsbrowser.serverComboBox.store.add([record]);
                }
            }
        } else {
            this.wmsbrowser.fireEvent(
                'genericerror', this.wmsbrowser.noLayerReturnedText
            );
        }
    },

    /** private: method[onCapabilitiesLoadException]
     *
     *  Called after a GetCapabilities request failure.  Fires the according
     *  failure event.
     */
    onCapabilitiesLoadException: function() {
        this.wmsbrowser.fireEvent('getcapabilitiesfail');
    }

});
