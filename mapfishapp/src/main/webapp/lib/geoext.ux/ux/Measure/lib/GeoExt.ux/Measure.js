/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/*
 * @requires GeoExt/widgets/Action.js
 */

/** api: (define)
 *  module = GeoExt.ux
 *  class = Measure
 */

/** api: constructor
 *  .. class:: Measure(config)
 *
 *      Creates a GeoExt.Action for measurements.
 * 
 *  JSBuild: OpenLayers/Control/Measure.js, OpenLayers/StyleMap.js,
 *  OpenLayers/Style.js, OpenLayers/Rule.js, OpenLayers/Handler.js
 *  must be included.
 */
GeoExt.ux.Measure = Ext.extend(GeoExt.Action, {

    /** api: config[handlerClass]
     *  ``Function`` The handler class to pass to the measure control,
     *  ``OpenLayers.Handler.Polygon`` for area measurements,
     *  ``OpenLayers.Handler.Path`` for distance measurements, and
     *  ``OpenLayers.Handler.Point`` for position measurements.
     *  Required.
     */

    /** api: config[styleMap]
     *  ``OpenLayers.StyleMap`` A style map for the sketch layer. Optional.
     */

    /** api: config[controlOptions]
     *  ``Object`` Options to pass to the Measure control. Optional.
     */

    /** api: config[autoDeactivate]
     *  ``Boolean`` Should the measure control be deactivated when closing
     *      the measurement tip. Optional. Defaults to false.
     */
    
    /** private: property[tip]
     *  ``Ext.Tip`` The displayed tip.
     */
    tip: null,

    /** api: config[template]
     *  ``String`` | ``Ext.XTemplate`` HTML template, or Ext.XTemplate used
     *      to display the measure. Required.
     */
    /** private: property[template]
     *  ``Ext.XTemplate`` The template used for the display of measures.
     */
    template: null,
     
    /** api: config[decimals]
     *  ``Integer`` The number of decimals for the displayed values.
     *    Defaults to 2.
     */
    decimals: 2,

    /** private: method[constructor]
     */
    constructor: function(config) {
        config = config || {};
        config.control = this.createControl(
            config.handlerClass,
            config.styleMap || this.createStyleMap(),
            config.controlOptions);
        delete config.handlerClass;
        delete config.styleMap;
        delete config.controlOptions;
        if(typeof(config.template) == "string") {
            this.template = new Ext.XTemplate(config.template, {
                decimals: config.decimals === undefined ?
                    this.decimals : config.decimals,
                compiled: true
            });
        } else if(config.template instanceof Ext.XTemplate) {
            this.template = config.template;
        }
        delete config.template;
        delete config.decimals;
        this.autoDeactivate = config.autoDeactivate || false;
        delete config.autoDeactivate;
        arguments.callee.superclass.constructor.call(this, config);
    },

    /** private: method[createControl]
     *  Creates the measure control.
     *
     *  :param handlerClass: ``Function`` The handler class the measure
     *      control is configured with.
     *  :param styleMap: ``OpenLayers.StyleMap`` The style map used for
     *      the sketch layer.
     *  :param controlOptions: ``Object`` Extra options to set in the
     *      measure control.
     *
     *  :return: ``OpenLayers.Control.Measure`` The mesure control.
     */
    createControl: function(handlerClass, styleMap, controlOptions) {
        controlOptions = Ext.apply({
            persist: true,
            eventListeners: {
                "measure": this.display,
                "deactivate": this.cleanup,
                "measurepartial": this.cleanup,
                scope: this
            },
            handlerOptions: {
                layerOptions: {
                    styleMap: styleMap
                }
            }
        }, controlOptions);
        return new OpenLayers.Control.Measure(handlerClass, controlOptions);
    },
    
    /** private: method[cleanup]
     *  Destroys the tip.
     */
    cleanup: function() {
        if(this.tip) {
            this.tip.destroy();
            this.tip = null;
        }
    },
    
    /** private: method[makeString]
     *  Builds the HTML string for the tip.
     *
     *  :param event ``Object`` The event object.
     *
     *  :return: ``String`` The HTML string.
     */
    makeString: function(event) {
        return this.template.apply(event);
    },

    /** private: method[display]
     *  Creates and displays the tip.
     *
     *  :param event ``Object`` The event object.
     */
    display: function(event) {
        this.cleanup();
        this.tip = new Ext.Tip({
            html: this.makeString(event),
            closable: true,
            draggable: false,
            listeners: {
                hide: function() {
                    this.control.cancel();
                    if (this.autoDeactivate === true) {
                        this.control.deactivate();
                    }
                    this.cleanup();
                },
                scope: this
            }
        });
        Ext.getBody().on("mousemove", function(e) {
            this.tip.showAt(e.getXY());
        }, this, {single: true});
    },

    /** private: method[createStyleMap]
     *  Creates the default style map.
     *
     *  :return: ``OpenLayers.StyleMap`` The style map.
     */
    createStyleMap: function() {
        var sketchSymbolizers = {
            "Point": {
                pointRadius: 4,
                graphicName: "square",
                fillColor: "white",
                fillOpacity: 1,
                strokeWidth: 1,
                strokeOpacity: 1,
                strokeColor: "#333333"
            },
            "Line": {
                strokeWidth: 2,
                strokeOpacity: 1,
                strokeColor: "#666666",
                strokeDashstyle: "dash"
            },
            "Polygon": {
                strokeWidth: 2,
                strokeOpacity: 1,
                strokeColor: "#666666",
                fillColor: "white",
                fillOpacity: 0.3
            }
        };
        return new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(null, {
                rules: [new OpenLayers.Rule({symbolizer: sketchSymbolizers})]
            })
        });
    }
});
