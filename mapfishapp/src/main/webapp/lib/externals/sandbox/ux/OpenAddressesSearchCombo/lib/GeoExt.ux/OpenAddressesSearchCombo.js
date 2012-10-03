/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt.ux
 *  class = OpenAddressesSearchCombo
 *  base_link = `Ext.form.ComboBox <http://extjs.com/deploy/dev/docs/?class=Ext.form.ComboBox>`_
 */

Ext.namespace("GeoExt.ux");

GeoExt.ux.OpenAddressesSearchCombo = Ext.extend(Ext.form.ComboBox, {
    /** api: config[map]
     *  ``OpenLayers.Map or Object``  A configured map or a configuration object
     *  for the map constructor, required only if :attr:`zoom` is set to
     *  value greater than or equal to 0.
     */
    /** private: property[map]
     *  ``OpenLayers.Map``  The map object.
     */
    map: null,

    /** api: config[width]
     *  See http://www.extjs.com/deploy/dev/docs/source/BoxComponent.html#cfg-Ext.BoxComponent-width,
     *  default value is 350.
     */
    width: 350,

    /** api: config[listWidth]
     *  See http://www.extjs.com/deploy/dev/docs/source/Combo.html#cfg-Ext.form.ComboBox-listWidth,
     *  default value is 350.
     */
    listWidth: 350,

    /** api: config[zoom]
     *  ``Number`` Zoom level for recentering the map after search, if set to
     *  a negative number the map isn't recentered, defaults to 8.
     */
    /** private: property[zoom]
     *  ``Number``
     */
    zoom: 18,

    /** api: config[minChars]
     *  ``Number`` Minimum number of characters to be typed before
     *  search occurs, defaults to 1.
     */
    minChars: 1,

    /** api: config[queryDelay]
     *  ``Number`` Delay before the search occurs, defaults to 50 ms.
     */
    queryDelay: 50,

    /** api: config[maxRows]
     *  `String` The maximum number of rows in the responses, defaults to 20,
     *  maximum allowed value is 1000.
     *  See: http://www.geonames.org/export/geonames-search.html
     */
    /** private: property[maxRows]
     *  ``String``
     */
    maxRows: '20',

    /** api: config[tpl]
     *  ``Ext.XTemplate or String`` Template for presenting the result in the
     *  list (see http://www.extjs.com/deploy/dev/docs/output/Ext.XTemplate.html),
     *  if not set a default value is provided.
     */
    tpl: '<tpl for="."><div class="x-combo-list-item">{street} {housenumber}, {city}</div></tpl>',

    /** api: config[lang]
     *  ``String`` Place name and country name will be returned in the specified
     *  language. Default is English (en).
     */
    /** private: property[lang]
     *  ``String``
     */
    lang: 'en',

    /** private: property[hideTrigger]
     *  Hide trigger of the combo.
     */
    hideTrigger: true,

    /** private: property[url]
     *  Url of the OpenAddresses service
     */
    url: 'http://www.openaddresses.org/addresses/',

    /** private: constructor
     */
    initComponent: function() {
        this.emptyText =  OpenLayers.i18n('Search address in OpenAddresses');
        this.loadingText = OpenLayers.i18n('Search in OpenAddresses...');
        GeoExt.ux.OpenAddressesSearchCombo.superclass.initComponent.apply(this, arguments);

        this.store = new Ext.data.Store({
            proxy: new Ext.data.ScriptTagProxy({
                url: this.url,
                method: 'GET'
            }),
            baseParams: {
                limit: this.maxRows,
                lang: this.lang,
                attrs: "street,housenumber,city"
            },
            reader: new Ext.data.JsonReader({
                root: 'features',
                fields: [
                   {name: 'geometry'},
                   {name: 'properties'},
                   {name: 'street', mapping: 'properties.street'},
                   {name: 'city', mapping: 'properties.city'},
                   {name: 'housenumber', mapping: 'properties.housenumber'}
                ]
            })
        });

        if(this.zoom > 0) {
            this.on("select", function(combo, record, index) {
                var position = new OpenLayers.LonLat(
                    record.data.geometry.coordinates[0], record.data.geometry.coordinates[1]
                );
                position.transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    this.map.getProjectionObject()
                );
                this.setValue(record.data.street + ' ' + record.data.housenumber + ' ' + record.data.city);
                this.map.setCenter(position, this.zoom);
            }, this);
        }
    }
});

/** api: xtype = gxux_openaddressessearchcombo */
Ext.reg('gxux_openaddressessearchcombo', GeoExt.ux.OpenAddressesSearchCombo);
