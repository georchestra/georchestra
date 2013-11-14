/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/*
 * @requires GeoExt.ux/Measure.js
 */

/** api: (define)
 *  module = GeoExt.ux.MeasureLength
 *  class = MeasureLength
 */

/** api: constructor
 *  .. class:: MeasureLength
 * 
 *      Creates a GeoExt.Action for length measurements.
 *
 *  JSBuild: OpenLayers/Handler/Path.js must be included.
 */
GeoExt.ux.MeasureLength = Ext.extend(GeoExt.ux.Measure, {

    /** api: config[handlerClass]
     *  ``Function`` The handler class to pass to the measure control,
     *  Defaults to ``OpenLayers.Handler.Path``. 
     */

    /** api: config[iconCls]
     *  ``String`` The CSS class selector that specifies a background image 
     *  to be used as the header icon for all components using this action 
     *  Defaults to 'gx-map-measurelength'. 
     */
    
    /** api: config[template]
     *  ``String`` | ``Ext.XTemplate`` HTML template, or Ext.XTemplate used
     *  to display the measure. Optional.
     */

    /** api: config[tooltip]
     *  ``String`` The tooltip for the button. Defaults to "Length measurement".
     */
    tooltip: 'Length measurement',
     
    /** private: method[constructor]
     */
    constructor: function(config) {
        config = Ext.apply({
            handlerClass: OpenLayers.Handler.Path,
            iconCls: 'gx-map-measurelength',
            tooltip: this.tooltip,
            template: '<p>{[values.measure.toFixed(this.decimals)]}&nbsp;'+
                '{units}</p>'
        }, config);
        arguments.callee.superclass.constructor.call(this, config);
    }
});
