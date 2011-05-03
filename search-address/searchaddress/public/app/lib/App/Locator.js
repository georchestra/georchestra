/*
 * @requires OpenLayers/Control.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Style.js
 * @include OpenLayers/Rule.js
 * @include GeoExt/widgets/Action.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Projection.js
 */

Ext.namespace('App');

/**
 * Constructor: App.Locator
 * Creates a {GeoExt.Action} configured with a custom crafted OpenLayers 
 * Control for position measurement. Use the "action" property to get a 
 * reference to the action.
 *
 * Parameters:
 * map - {OpenLayers.Map} The map object.
 * actionOptions - {Object} Options for the action.
 */
App.Locator = function(map, actionOptions) {

    // Private
    /**
     * Property: control
     * {OpenLayers.Control} our custom control with a point handler.
     */
    var control = null;
    
    /**
     * Property: tip
     * {Ext.Tip} The tip.
     */
    var tip = null;
    
    /**
     * Property: displayProjection
     * {OpenLayers.Projection} The projection for displaying the position
     * If set, please note that you'll need to include proj4js lib
     */
    var displayProjection = null; //new OpenLayers.Projection("EPSG:4326");
    
    /**
     * Property: template
     * {Ext.XTemplate} The template used to display the measure
     */
    var template = new Ext.XTemplate(
        '<p>X:&nbsp;{[values.measure.x.toFixed(this.decimals)]} {units}</p>',
        '<p>Y:&nbsp;{[values.measure.y.toFixed(this.decimals)]} {units}</p>', {
            compiled: true,
            decimals: 0 // number of decimals for the measurement
        }
    );

    /**
     * Method: cleanup
     * Destroys the tooltip
     */
    var cleanup = function() {
        if (tip) {
            tip.destroy();
            tip = null;
        }
    };

    /**
     * Method: onDeactivate
     * Removes the tooltip and the sketch
     */
    var onDeactivate = function() {
        cleanup();
        control.cancel();
    };

    /**
     * Method: makeString
     * Removes the tooltip and the sketch
     */
    var makeString = function(event) {
        return template.apply(event);
    };
    
    /**
     * Method: onMeasure 
     * Displays the tooltip
     */
    var onMeasure = function(event) {
        cleanup();
        tip = new Ext.Tip({
            html: makeString(event),
            closable: true,
            draggable: false,
            listeners: {
                'hide': onDeactivate,
                scope: this
            }
        });
        Ext.getBody().on("mousemove", function(e) {
            tip.showAt(e.getXY());
        }, this, {single: true});
    };
    
    /**
     * Method: createStyleMap
     * Builds the StyleMap for the sketches.
     */
    var createStyleMap = function() {
        var sketchSymbolizers = {
            "Point": {
                pointRadius: 4,
                graphicName: "square",
                fillColor: "white",
                fillOpacity: 1,
                strokeWidth: 1,
                strokeOpacity: 1,
                strokeColor: "#333333"
            }
        };
        return new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(null, {
                rules: [new OpenLayers.Rule({symbolizer: sketchSymbolizers})]
            })
        });
    };

    // Public
    Ext.apply(this, {

        /**
         * APIProperty: action
         * {GeoExt.Action} The action instance. Read-only.
         */
        action: null
    });

    // Main
    control = new App.Locator.Control({
        displayProjection: displayProjection,
        handlerOptions: {
            layerOptions: {
                styleMap: createStyleMap()
            }
        },
        eventListeners: {
            "measure": onMeasure,
            "deactivate": onDeactivate,
            scope: this
        }
    });
    
    this.action = new GeoExt.Action(
        Ext.apply({
            map: map,
            control: control
        }, actionOptions)
    );
};

/**
 * Class: App.Locator.Control
 * Allows for drawing of point features for position measurements.
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
App.Locator.Control = OpenLayers.Class(OpenLayers.Control, { 
    
    /**
     * APIProperty: displayProjection
     * {<OpenLayers.Projection>} The projection in which the 
     * position is displayed
     */
    displayProjection: null,
    
    /**
     * APIProperty: handlerOptions
     * {Object} ReadOnly options for point handler
     */
    handlerOptions: null,
    
    /**
     * Constant: EVENT_TYPES
     *
     * Supported event types:
     * measure - Triggered when a point is drawn
     */
    EVENT_TYPES: ['measure'],

    /**
     * Constructor: App.Locator.Control
     * Create a new locator control to get point position
     * 
     * Parameters:
     * options - {Object} An optional object whose properties will be used
     *     to extend the control.
     */
    initialize: function(options) {
        this.EVENT_TYPES =
            App.Locator.Control.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        this.handler = new OpenLayers.Handler.Point(this, {
            'done': this.onPoint
        }, OpenLayers.Util.extend({
            persist: true
        }, this.handlerOptions));
    },

    /**
     * Method: onPoint
     * Callback executed on sketch done.
     */
    onPoint: function(geometry) {
        var units, displayProjection = this.displayProjection;
        if(displayProjection) {
            var mapProjection = this.map.getProjectionObject();
            geometry.transform(mapProjection, displayProjection);
            units = this.displayProjection.getUnits();
        } else {
            units = this.map.getUnits();
        }
        this.events.triggerEvent('measure', {
            measure: {x: geometry.x, y: geometry.y},
            units: units
        });
    },
    
    /**
     * APIMethod: cancel
     * Stop the control from measuring. The temporary sketch will be erased.
     */
    cancel: function() {
        this.handler.cancel();
    },
    
    /**
     * Method: destroy
     * The destroy method is used to perform any clean up before the control
     * is dereferenced. 
     */
    destroy: function() {
        this.handler = null;
        OpenLayers.Control.prototype.destroy.apply(this, arguments);
    },
    
    CLASS_NAME: "App.Locator.Control"
});
