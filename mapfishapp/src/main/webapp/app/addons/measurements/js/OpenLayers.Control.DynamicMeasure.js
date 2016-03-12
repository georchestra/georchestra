/* Copyright 2011-2016 Xavier Mamano, http://github.com/jorix/OL-DynamicMeasure
 * Published under MIT license. */

/**
 * @requires OpenLayers/Control/Measure.js
 * @requires OpenLayers/Rule.js
 * @requires OpenLayers/StyleMap.js
 */

/**
 * Class: OpenLayers.Control.DynamicMeasure
 * Allows for drawing of features for measurements.
 *
 * Inherits from:
 *  - <OpenLayers.Control.Measure>
 */
OpenLayers.Control.DynamicMeasure = OpenLayers.Class(
                                                   OpenLayers.Control.Measure, {

    /**
     * APIProperty: accuracy
     * {Integer} Digits measurement accuracy, default is 5.
     */
    accuracy: 5,

    /**
     * APIProperty: persist
     * {Boolean} Keep the temporary measurement after the
     *     measurement is complete.  The measurement will persist until a new
     *     measurement is started, the control is deactivated, or <cancel> is
     *     called. Default is true.
     */
    persist: true,

    /**
     * APIProperty: styles
     * {Object} Alterations of the default styles of the points lines poligons
     *     and labels text, could use keys: "Point", "Line",
     *     "Polygon", "labelSegments", "labelHeading", "labelLength" and
     *     "labelArea". Default is <OpenLayers.Control.DynamicMeasure.styles>.
     */
    styles: null,

    /**
     * APIProperty: positions
     * {Object} Alterations of the default position of the labels, could use
     *     keys: "labelSegments" & "labelHeading", with values "start" "middle"
     *     and "end" refered of the current segment; and keys: "labelLength" &
     *     "labelArea" with additional values "center" (of the feature) and
     *     "initial" (initial point of the feature) and also mentioned previus
     *     values. Default is
     *     <OpenLayers.Control.DynamicMeasure.positions>.
     */
    positions: null,

    /**
     * APIProperty: maxSegments
     * {Integer|Null} Maximum number of visible segments measures, default is 1.
     *
     * To avoid soiling the track is desirable to reduce the number of visible
     *     segments.
     */
    maxSegments: 1,

    /**
     * APIProperty: maxHeadings
     * {Integer|Null} Maximum number of visible headings measures, default is 1.
     *
     * To avoid soiling the track is desirable to reduce the number of visible
     *     segments.
     */
    maxHeadings: 1,

    /**
     * APIProperty: layerSegmentsOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerSegments> of the lengths of the segments. If set to
     *     null the layer does not act.
     *
     *     If `styleMap` options is set then the key "labelSegments" of the
     *     `styles` option is ignored.
     */
    layerSegmentsOptions: undefined,

    /**
     * APIProperty: layerHeadingOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerHeading> of the angle of the segments. If set to
     *     null the layer does not act.  Default is null, set to {} to use a
     *     <layerHeading> to show headings.
     *
     *     If `styleMap` options is set then the key "labelHeading" of the
     *     `styles` option is ignored.
     */
    layerHeadingOptions: null,

    /**
     * APIProperty: layerLengthOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerLength> of the total length. If set to null the layer
     *     does not act.
     *
     *     If `styleMap` option is set then the key "labelLength" of the
     *     `styles` option is ignored.
     */
    layerLengthOptions: undefined,

    /**
     * APIProperty: layerAreaOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerArea> of the total area. If set to null the layer does
     *     not act.
     *
     *     If `styleMap` is set then the key "labelArea" of the `styles` option
     *     is ignored.
     */
    layerAreaOptions: undefined,

    /**
     * APIProperty: drawingLayer
     * {<OpenLayers.Layer.Vector>} Drawing layer to store the drawing when
     *     finished.
     */
    drawingLayer: null,

    /**
     * APIProperty: multi
     * {Boolean} Cast features to multi-part geometries before passing to the
     *     drawing layer, only used if declared a <drawingLayer>.
     * Default is false.
     */
    multi: false,

    /**
     * APIProperty: keep
     * {Boolean} Keep annotations for every measures.
     */
    keep: false,

    /**
     * Property: layerSegments
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show the
     *     length of the segments.
     */
    layerSegments: null,

    /**
     * Property: layerLength
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show total
     *     length.
     */
    layerLength: null,

    /**
     * Property: layerArea
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show total
     *     area.
     */
    layerArea: null,

    /**
     * Property: layerSegmentsKeep
     * {<OpenLayers.Layer.Vector>} The layer keep a copy of the length of
     *     every segments measured since tool activation.
     */
    layerSegmentsKeep: null,

    /**
     * Property: layersLengthKeep
     * {<OpenLayers.Layer.Vector>} The layer keep a copy of the length of
     *     every polyline/poly measured since tool activation.
     */
    layerLengthKeep: null,

    /**
     * Property: layerAreaKeep
     * {<OpenLayers.Layer.Vector>} The layer keep a copy of the area of every
     *     polygon
     */
    layerAreaKeep: null,
    /**
     * Property: dynamicObj
     * {Object} Internal use.
     */
    dynamicObj: null,

    /**
     * Property: isArea
     * {Boolean} Internal use.
     */
    isArea: null,

    /**
     * Constructor: OpenLayers.Control.Measure
     *
     * Parameters:
     * handler - {<OpenLayers.Handler>}
     * options - {Object}
     *
     * Valid options:
     * accuracy - {Integer} Digits measurement accuracy, default is 5.
     * styles - {Object} Alterations of the default styles of the points lines
     *     poligons and labels text, could use keys: "Point",
     *     "Line", "Polygon", "labelSegments", "labelLength", "labelArea".
     * positions - {Object} Alterations of the default position of the labels.
     * handlerOptions - {Object} Used to set non-default properties on the
     *     control's handler. If `layerOptions["styleMap"]` is set then the
     *     keys: "Point", "Line" and "Polygon" of the `styles` option
     *     are ignored.
     * layerSegmentsOptions - {Object} Any optional properties to be set on the
     *     layer of <layerSegments> of the lengths of the segments. If
     *     `styleMap` is set then the key "labelSegments" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerLengthOptions - {Object} Any optional properties to be set on the
     *     layer of <layerLength> of the total length. If
     *     `styleMap` is set then the key "labelLength" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerAreaOptions - {Object} Any optional properties to be set on the
     *     layer of <layerArea> of the total area. If
     *     `styleMap` is set then the key "labelArea" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerHeadingOptions - {Object} Any optional properties to be set on the
     *     layer of <layerHeading> of the angle of the segments. If
     *     `styleMap` is set then the key "labelHeading" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * drawingLayer - {<OpenLayers.Layer.Vector>} Optional drawing layer to
     *     store the drawing when finished.
     * multi - {Boolean} Cast features to multi-part geometries before passing
     *     to the drawing layer
     * keep - {Boolean} Keep annotations for every measures.
     */
    initialize: function(handler, options) {

        // Manage options
        options = options || {};

        // handlerOptions: persist & multi
        options.handlerOptions = OpenLayers.Util.extend(
            {persist: !options.drawingLayer}, options.handlerOptions
        );
        if (options.drawingLayer && !('multi' in options.handlerOptions)) {
            options.handlerOptions.multi = options.multi;
        }

        // * styles option
        if (options.drawingLayer) {
            var sketchStyle = options.drawingLayer.styleMap &&
                                 options.drawingLayer.styleMap.styles.temporary;
            if (sketchStyle) {
                options.handlerOptions
                                  .layerOptions = OpenLayers.Util.applyDefaults(
                    options.handlerOptions.layerOptions, {
                        styleMap: new OpenLayers.StyleMap({
                            'default': sketchStyle
                        })
                    }
                );
            }
        }
        var optionsStyles = options.styles || {};
        options.styles = optionsStyles;
        var defaultStyles = OpenLayers.Control.DynamicMeasure.styles;
        // * * styles for handler layer.
        if (!options.handlerOptions.layerOptions ||
            !options.handlerOptions.layerOptions.styleMap) {
            // use the style option for layerOptions of the handler.
            var style = new OpenLayers.Style(null, {rules: [
                new OpenLayers.Rule({symbolizer: {
                    'Point': OpenLayers.Util.applyDefaults(
                                optionsStyles.Point, defaultStyles.Point),
                    'Line': OpenLayers.Util.applyDefaults(
                                optionsStyles.Line, defaultStyles.Line),
                    'Polygon': OpenLayers.Util.applyDefaults(
                                optionsStyles.Polygon, defaultStyles.Polygon)
                }})
            ]});
            options.handlerOptions = options.handlerOptions || {};
            options.handlerOptions.layerOptions =
                                      options.handlerOptions.layerOptions || {};
            options.handlerOptions.layerOptions.styleMap =
                                    new OpenLayers.StyleMap({'default': style});
        }

        // * positions option
        options.positions = OpenLayers.Util.applyDefaults(
            options.positions,
            OpenLayers.Control.DynamicMeasure.positions
        );

        // force some handler options
        options.callbacks = options.callbacks || {};
        if (options.drawingLayer) {
            OpenLayers.Util.applyDefaults(options.callbacks, {
                create: function(vertex, feature) {
                    this.callbackCreate(vertex, feature);
                    this.drawingLayer.events.triggerEvent(
                        'sketchstarted', {vertex: vertex, feature: feature}
                    );
                },
                modify: function(vertex, feature) {
                    this.callbackModify(vertex, feature);
                    this.drawingLayer.events.triggerEvent(
                        'sketchmodified', {vertex: vertex, feature: feature}
                    );
                },
                done: function(geometry) {
                    if (this.keep) {
                        this.copyAnnotations();
                    }
                    this.callbackDone(geometry);
                    this.drawFeature(geometry);
                }
            });
        }
        OpenLayers.Util.applyDefaults(options.callbacks, {
            create: this.callbackCreate,
            point: this.callbackPoint,
            cancel: this.callbackCancel,
            done: this.callbackDone,
            modify: this.callbackModify,
            redo: this.callbackRedo,
            undo: this.callbackUndo
        });

        // do a trick with the handler to avoid blue background in freehand.
        var _self = this;
        var oldOnselectstart = document.onselectstart ?
                              document.onselectstart : OpenLayers.Function.True;
        var handlerTuned = OpenLayers.Class(handler, {
            down: function(evt) {
                document.onselectstart = OpenLayers.Function.False;
                return handler.prototype.down.apply(this, arguments);
            },
            up: function(evt) {
                document.onselectstart = oldOnselectstart;
                return handler.prototype.up.apply(this, arguments);
            },
            move: function(evt) {
                if (!this.mouseDown) {
                    document.onselectstart = oldOnselectstart;
                }
                return handler.prototype.move.apply(this, arguments);
            },
            mouseout: function(evt) {
                if (OpenLayers.Util.mouseLeft(evt, this.map.viewPortDiv)) {
                    if (this.mouseDown) {
                        document.onselectstart = oldOnselectstart;
                    }
                }
                return handler.prototype.mouseout.apply(this, arguments);
            },
            finalize: function() {
                document.onselectstart = oldOnselectstart;
                handler.prototype.finalize.apply(this, arguments);
            }
        }, {
            undo: function() {
                var undone = handler.prototype.undo.call(this);
                if (undone) {
                    this.callback('undo',
                                 [this.point.geometry, this.getSketch(), true]);
                }
                return undone;
            },
            redo: function() {
                var redone = handler.prototype.redo.call(this);
                if (redone) {
                    this.callback('redo',
                                 [this.point.geometry, this.getSketch(), true]);
                }
                return redone;
            }
        });
        // ... and call the constructor
        OpenLayers.Control.Measure.prototype.initialize.call(
                                                   this, handlerTuned, options);

        this.isArea = handler.prototype.polygon !== undefined; // duck typing
    },

    /**
     * APIMethod: destroy
     */
    destroy: function() {
        this.deactivate();
        OpenLayers.Control.Measure.prototype.destroy.apply(this, arguments);
    },

    /**
     * Method: draw
     * This control does not have HTML component, so this method should
     *     be empty.
     */
    draw: function() {},

    /**
     * APIMethod: activate
     */
    activate: function() {
        var response = OpenLayers.Control.Measure.prototype.activate.apply(
                                                               this, arguments);
        if (response) {
            // Create dynamicObj
            this.dynamicObj = {};
            // Create layers
            var _optionsStyles = this.styles || {},
                _defaultStyles = OpenLayers.Control.DynamicMeasure.styles,
                _self = this;
            var _create = function(styleName, initialOptions, nameSuffix='') {
                if (initialOptions === null) {
                    return null;
                }
                var options = OpenLayers.Util.extend({
                    displayInLayerSwitcher: false,
                    calculateInRange: OpenLayers.Function.True
                    // ?? ,wrapDateLine: this.citeCompliant
                }, initialOptions);
                if (!options.styleMap) {
                    var style = _optionsStyles[styleName];

                    options.styleMap = new OpenLayers.StyleMap({
                        'default': OpenLayers.Util.applyDefaults(style,
                                                      _defaultStyles[styleName])
                    });
                }
                var layer = new OpenLayers.Layer.Vector(
                    _self.CLASS_NAME + ' ' + styleName + nameSuffix,
                    options);
                _self.map.addLayer(layer);
                return layer;
            };
            this.layerSegments =
                            _create('labelSegments', this.layerSegmentsOptions);
            this.layerHeading =
                            _create('labelHeading', this.layerHeadingOptions);
            this.layerLength = _create('labelLength', this.layerLengthOptions);
            if (this.isArea) {
                this.layerArea = _create('labelArea', this.layerAreaOptions);
            }
            if (this.keep) {
                this.layerSegmentsKeep =
                            _create('labelSegments', this.layerSegmentsOptions,
                            'Keep');
                this.layerLengthKeep =
                            _create('labelLength', this.layerLengthOptions,
                            'Keep');
                if (this.isArea) {
                    this.layerAreaKeep =
                                _create('labelArea', this.layerAreaOptions,
                                'Keep');
                }
            }
        }
        return response;
    },

    /**
     * APIMethod: deactivate
     */
    deactivate: function() {
        var response = OpenLayers.Control.Measure.prototype.deactivate.apply(
                                                               this, arguments);
        if (response) {
            if (this.layerSegments) {
                this.layerSegments.destroy();
            }
            if (this.layerLength) {
                this.layerLength.destroy();
            }
            if (this.layerHeading) {
                this.layerHeading.destroy();
            }
            if (this.layerArea) {
                this.layerArea.destroy();
            }
            this.dynamicObj = null;
            this.layerSegments = null;
            this.layerLength = null;
            this.layerHeading = null;
            this.layerArea = null;
        }
        return response;
    },

    /**
     * APIMethod: emptyKeeped
     * Remove annotation from layers layerSegementsKeep, layerLengthKeep,
     * layerAreaKeep
     */
     emptyKeeped: function () {
        if (this.layerSegmentsKeep) {
            this.layerSegmentsKeep.removeAllFeatures();
        }
        if (this.layerLengthKeep) {
            this.layerLengthKeep.removeAllFeatures();
        }
        if (this.layerAreaKeep) {
            this.layerAreaKeep.removeAllFeatures();
        }
    },

    /**
     * APIMethod: setImmediate
     * Sets the <immediate> property. Changes the activity of immediate
     * measurement.
     */
    setImmediate: function(immediate) {
        this.immediate = immediate;
    },

    /**
     * Method: callbackCreate
     */
    callbackCreate: function() {
        var dynamicObj = this.dynamicObj;
        dynamicObj.drawing = false;
        dynamicObj.freehand = false;
        dynamicObj.fromIndex = 0;
        dynamicObj.countSegments = 0;
    },

    /**
     * Method: callbackCancel
     */
    callbackCancel: function() {
        this.destroyLabels();
    },

    /**
     * Method: callbackDone
     * Called when the measurement sketch is done.
     *
     * Parameters:
     * geometry - {<OpenLayers.Geometry>}
     */
    callbackDone: function(geometry) {
        this.measureComplete(geometry);
        if (!this.persist) {
            this.destroyLabels();
        }
    },

    /**
     * Method: drawFeature
     */
    drawFeature: function(geometry) {
        var feature = new OpenLayers.Feature.Vector(geometry);
        var proceed = this.drawingLayer.events.triggerEvent(
            'sketchcomplete', {feature: feature}
        );
        if (proceed !== false) {
            feature.state = OpenLayers.State.INSERT;
            this.drawingLayer.addFeatures([feature]);
            if (this.featureAdded) {
                // for compatibility
                this.featureAdded(feature);
            }
            this.events.triggerEvent('featureadded', {feature: feature});
        }
    },

    /**
     * Method: copyAnnotations
     */
    copyAnnotations: function() {
        var _insertable = function(feat) {
            feat.state = OpenLayers.State.INSERT;
        };
        // Segments measures
        var segments = this.layerSegments.clone();
        var segmentsFeatures = segments.features;
        for(i = 0; i > segmentsFeatures.length; i++) {
            segmentsFeatures[i].state = OpenLayers.State.INSERT;
        }
        this.layerSegmentsKeep.addFeatures(segmentsFeatures);
        // Length measures
        var lengths = this.layerLength.clone();
        var lengthsFeatures = lengths.features;
        for(i = 0; i > lengthsFeatures.length; i++) {
            lengthsFeatures[i].state = OpenLayers.State.INSERT;
        }
        this.layerLengthKeep.addFeatures(lengthsFeatures);
        // Area measures
        if (this.isArea) {
            var areas = this.layerArea.clone();
            var areasFeatures = areas.features;
            for(i = 0; i > areasFeatures.length; i++) {
                areasFeatures[i].state = OpenLayers.State.INSERT;
            }
            this.layerAreaKeep.addFeatures(areasFeatures);
        }
    },

    /**
     * Method: callbackCancel
     */
    destroyLabels: function() {
        if (this.layerSegments) {
            this.layerSegments.destroyFeatures(null, {silent: true});
        }
        if (this.layerLength) {
            this.layerLength.destroyFeatures(null, {silent: true});
        }
        if (this.layerHeading) {
            this.layerHeading.destroyFeatures(null, {silent: true});
        }
        if (this.layerArea) {
            this.layerArea.destroyFeatures(null, {silent: true});
        }
    },

    /**
     * Method: callbackPoint
     */
    callbackPoint: function(point, geometry) {
        var dynamicObj = this.dynamicObj;
        if (!dynamicObj.drawing) {
            this.destroyLabels();
        }
        if (!this.handler.freehandMode(this.handler.evt)) {
            dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
            dynamicObj.freehand = false;
            dynamicObj.countSegments++;
        } else if (!dynamicObj.freehand) {
            // freehand has started
            dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
            dynamicObj.freehand = true;
            dynamicObj.countSegments++;
        }

        this.measurePartial(point, geometry);
        dynamicObj.drawing = true;
    },

    /**
     * Method: callbackUndo
     */
    callbackUndo: function(point, feature) {
        var _self = this,
            undoLabel = function(layer) {
                if (layer) {
                    var features = layer.features,
                        lastSegmentIndex = features.length - 1,
                        lastSegment = features[lastSegmentIndex],
                        lastSegmentFromIndex = lastSegment.attributes.from,
                        lastPointIndex = _self.handler.getCurrentPointIndex();
                    if (lastSegmentFromIndex >= lastPointIndex) {
                        var dynamicObj = _self.dynamicObj;
                        layer.destroyFeatures(lastSegment);
                        lastSegment = features[lastSegmentIndex - 1];
                        dynamicObj.fromIndex = lastSegment.attributes.from;
                        dynamicObj.countSegments = features.length;
                    }
                }
            };
        undoLabel(this.layerSegments);
        undoLabel(this.layerHeading);
        this.callbackModify(point, feature, true);
    },

    /**
     * Method: callbackRedo
     */
    callbackRedo: function(point, feature) {
        var line = this.handler.line.geometry,
            currIndex = this.handler.getCurrentPointIndex();
        var dynamicObj = this.dynamicObj;
        this.showLabelSegment(
            dynamicObj.countSegments,
            dynamicObj.fromIndex,
            line.components.slice(dynamicObj.fromIndex, currIndex)
        );
        dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
        dynamicObj.countSegments++;
        this.callbackModify(point, feature, true);
    },

    /**
     * Method: callbackModify
     */
    callbackModify: function(point, feature, drawing) {
        if (this.immediate) {
            this.measureImmediate(point, feature, drawing);
        }

        var dynamicObj = this.dynamicObj;
        if (dynamicObj.drawing === false) {
           return;
        }

        var line = this.handler.line.geometry,
            currIndex = this.handler.getCurrentPointIndex();
        if (!this.handler.freehandMode(this.handler.evt) &&
                                                          dynamicObj.freehand) {
            // freehand has stopped
            dynamicObj.fromIndex = currIndex - 1;
            dynamicObj.freehand = false;
            dynamicObj.countSegments++;
        }

        // total measure
        var totalLength = this.getBestLength(line);
        if (!totalLength[0]) {
           return;
        }
        var positions = this.positions,
            positionGet = {
            center: function() {
                var center = feature.geometry.getBounds().clone();
                center.extend(point);
                center = center.getCenterLonLat();
                return [center.lon, center.lat];
            },
            initial: function() {
                var initial = line.components[0];
                return [initial.x, initial.y];
            },
            start: function() {
                var start = line.components[dynamicObj.fromIndex];
                return [start.x, start.y];
            },
            middle: function() {
                var start = line.components[dynamicObj.fromIndex];
                return [(start.x + point.x) / 2, (start.y + point.y) / 2];
            },
            end: function() {
                return [point.x, point.y];
            }
        };
        if (this.layerLength) {
            this.showLabel(
                        this.layerLength, 1, 0, totalLength,
                        positionGet[positions.labelLength](), 1);
        }
        if (this.isArea) {
            if (this.layerArea) {
                var totalArea = this.getBestArea(feature.geometry);
                if (totalArea[0] || this.layerArea.features.length) {
                    this.showLabel(this.layerArea, 1, 0,
                              totalArea, positionGet[positions.labelArea](), 1);
                }
            }
            if (this.showLabelSegment(
                      1, 0, [line.components[currIndex], line.components[0]])) {
                dynamicObj.countSegments++;
            }
        }
        this.showLabelSegment(
            dynamicObj.countSegments,
            dynamicObj.fromIndex,
            line.components.slice(dynamicObj.fromIndex, currIndex + 1)
        );
    },

    /**
     * Function: showLabelSegment
     *
     * Parameters:
     * labelsNumber- {Integer} Number of the labels to be on the label layer.
     * fromIndex - {Integer} Index of the last point on the measured feature.
     * points - Array({<OpenLayers.Geometry.Point>})
     *
     * Returns:
     * {Boolean}
     */
    showLabelSegment: function(labelsNumber, fromIndex, _points) {
        var layerSegments = this.layerSegments,
            layerHeading = this.layerHeading;
        if (!layerSegments && !layerHeading) {
            return false;
        }
        // clone points
        var points = [],
            pointsLen = _points.length;
        for (var i = 0; i < pointsLen; i++) {
            points.push(_points[i].clone());
        }
        var segmentLength =
                 this.getBestLength(new OpenLayers.Geometry.LineString(points));
        if (segmentLength[0] == 0) {
            return false;
        }
        var positions = this.positions,
            from = points[0],
            to = points[pointsLen - 1],
            positionGet = {
                start: function() {
                    return [from.x, from.y];
                },
                middle: function() {
                    return [(from.x + to.x) / 2, (from.y + to.y) / 2];
                },
                end: function() {
                    return [to.x, to.y];
                }
            },
            created = false;
        if (layerSegments) {
            created = this.showLabel(layerSegments, labelsNumber, fromIndex,
                            segmentLength,
                            positionGet[positions.labelSegments](),
                            this.maxSegments);
        }
        if (layerHeading) {
            var heading = Math.atan2(to.y - from.y, to.x - from.x),
                bearing = 90 - heading * 180 / Math.PI;
            if (bearing < 0) {
                bearing += 360;
            }
            created = this.showLabel(layerHeading,
                            labelsNumber, fromIndex,
                            [bearing, '°'],
                            positionGet[positions.labelHeading](),
                            this.maxHeadings) || created;
        }
        return created;
    },

    /**
     * Function: showLabel
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.Vector>} Layer of the labels.
     * labelsNumber- {Integer} Number of the labels to be on the label layer.
     * fromIndex - {Integer} Index of the last point on the measured feature.
     * measure - Array({Float|String}) Measure provided by OL Measure control.
     * points - Array({Fload}) Array of x and y of the point to draw the label.
     * maxSegments - {Integer|Null} Maximum number of visible segments measures
     *
     * Returns:
     * {Boolean}
     */
    showLabel: function(
                     layer, labelsNumber, fromIndex, measure, xy, maxSegments) {
        var featureLabel, featureAux,
            features = layer.features;
        if (features.length < labelsNumber) {
        // add a label
            featureLabel = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(xy[0], xy[1]),
                {from: fromIndex}
            );
            this.setMesureAttributes(featureLabel.attributes, measure);
            layer.addFeatures([featureLabel]);
            if (maxSegments !== null) {
                var hide = (features.length - maxSegments) - 1;
                if (hide >= 0) {
                    featureAux = features[hide];
                    featureAux.style = {display: 'none'};
                    layer.drawFeature(featureAux);
                }
            }
            return true;
        } else {
        // update a label
            featureLabel = features[labelsNumber - 1];
            var geometry = featureLabel.geometry;
            geometry.x = xy[0];
            geometry.y = xy[1];
            geometry.clearBounds();
            this.setMesureAttributes(featureLabel.attributes, measure);
            layer.drawFeature(featureLabel);
            if (maxSegments !== null) {
                var show = (features.length - maxSegments);
                if (show >= 0) {
                    featureAux = features[show];
                    if (featureAux.style) {
                        delete featureAux.style;
                        layer.drawFeature(featureAux);
                    }
                }
            }
            return false;
        }
    },

    /**
     * Method: setMesureAttributes
     * Format measure[0] with digits of <accuracy>. Could internationalize the
     *     format customizing <OpenLayers.Number.thousandsSeparator> and
     *     <OpenLayers.Number.decimalSeparator>
     *
     * Parameters:
     * attributes - {object} Target attributes.
     * measure - Array({*})
     */
    setMesureAttributes: function(attributes, measure) {
        attributes.measure = OpenLayers.Number.format(
                           Number(measure[0].toPrecision(this.accuracy)), null);
        attributes.units = measure[1];
    },

    CLASS_NAME: 'OpenLayers.Control.DynamicMeasure'
});

/**
 * Constant: OpenLayers.Control.DynamicMeasure.styles
 * Contains the keys: "Point", "Line", "Polygon",
 *     "labelSegments", "labelHeading", "labelLength" and
 *     "labelArea" as a objects with style keys.
 */
OpenLayers.Control.DynamicMeasure.styles = {
    'Point': {
        pointRadius: 4,
        graphicName: 'square',
        fillColor: 'white',
        fillOpacity: 1,
        strokeWidth: 1,
        strokeOpacity: 1,
        strokeColor: '#333333'
    },
    'Line': {
        strokeWidth: 2,
        strokeOpacity: 1,
        strokeColor: '#666666',
        strokeDashstyle: 'dash'
    },
    'Polygon': {
        strokeWidth: 2,
        strokeOpacity: 1,
        strokeColor: '#666666',
        strokeDashstyle: 'solid',
        fillColor: 'white',
        fillOpacity: 0.3
    },
    labelSegments: {
        label: '${measure} ${units}',
        fontSize: '11px',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 2
    },
    labelLength: {
        label: '${measure} ${units}\n',
        fontSize: '11px',
        fontWeight: 'bold',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'lb',
        labelOutlineWidth: 3
    },
    labelArea: {
        label: '${measure}\n${units}²\n',
        fontSize: '11px',
        fontWeight: 'bold',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 3
    },
    labelHeading: {
        label: '${measure} ${units}',
        fontSize: '11px',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 3
    }
};

/**
 * Constant: OpenLayers.Control.DynamicMeasure.positions
 * Contains the keys: "labelSegments", "labelHeading",
 *     "labelLength" and "labelArea" as a strings with values 'start',
 *     'middle' and 'end' allowed for all keys (refered of last segment) and
 *     'center' and 'initial' (refered of the measured feature and only allowed
 *     for "labelLength" and "labelArea" keys)
 */
OpenLayers.Control.DynamicMeasure.positions = {
    labelSegments: 'middle',
    labelLength: 'end',
    labelArea: 'center',
    labelHeading: 'start'
};
