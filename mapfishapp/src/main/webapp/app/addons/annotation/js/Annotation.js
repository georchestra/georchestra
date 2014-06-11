/** api: (define)
 *  module = GEOR
 *  class = Annotation
 *  base_link = `Ext.util.Observable <http://extjs.com/deploy/dev/docs/?class=Ext.util.Observable>`_
 */
Ext.namespace("GEOR");

/**
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Control/ModifyFeature.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Handler/Polygon.js
 * @include OpenLayers/Handler/RegularPolygon.js
 * @include OpenLayers/Lang.js
 * @include GeoExt/widgets/Action.js
 * @include GeoExt/widgets/MapPanel.js
 * @include GeoExt/widgets/Popup.js
 */

/** api: constructor
 *  .. class:: Annotationj(config)
 *
 *      Create a FeatureEditing main controler.
 */
GEOR.Annotation = Ext.extend(Ext.util.Observable, {

    /** api: property[map]
     *  ``OpenLayers.Map``  A configured map object.
     */
    map: null,

    /** api: config[drawControls]
     *  ``Array(OpenLayers.Control.DrawFeature)``
     *  An array of DrawFeature controls automatically created from the layer.
     */
    drawControls: null,

    /** api: config[lastDrawControl]
     *  ``OpenLayers.Control.DrawFeature``
     *  The last active draw control.
     */
    lastDrawControl: null,

    /** api: config[actions]
     *  ``Array(GeoExt.Action or Ext.Action)``
     *  An array of actions created from various controls or tasks that are to
     *  be added to a toolbar.
     */
    actions: null,

    /** api: config[featureControl]
     *  ``OpenLayers.Control.ModifyFeature or OpenLayers.Control.SelectFeature``
     *  The OpenLayers control responsible of selecting the feature by clicks
     *  on the screen and, optionnaly, edit feature geometry.
     */
    featureControl: null,

    /** api: config[featurePanel]
     *  ``GEOR.FeaturePanel``
     *  A reference to the FeaturePanel object created
     */
    featurePanel: null,

    /** api: config[popup]
     *  ``GeoExt.Popup``
     *  A reference to the Popup object created
     */
    popup: null,

    /** api: config[downloadService]
     *  ``String``  URL used in order to use a server download service. The
     *              attributes "format" and "content" are sent (POST) to this
     *              service.
     */
    /** private: property[downloadService]
     *  ``String``  URL used in order to use a server download service. The
     *              attributes "format" and "content" are sent (POST) to this
     *              service.
     */
    downloadService: null,

    /** private: property[useDefaultAttributes]
     *  ``Boolean``
     *  If set to true, defaultAttributes are set to new features added with
     *  no attributes.
     */
    useDefaultAttributes: true,

    /** api: config[defaultAttributes]
     *  ``Array(String)``
     *  An array of attribute names to used when a blank feature is added
     *  to the map if useDefaultAttributes is set to true.
     */
    defaultAttributes: ['label','description'],

    /** api: config[defaultAttributesValues]
     *  ``Array(String|Number)``
     *  An array of attribute values to used when a blank feature is added
     *  to the map if useDefaultAttributes is set to true. This should match
     *  the defaultAttributes order.
     */
    defaultAttributesValues: [OpenLayers.i18n('annotation.no_title'),''],

    /** private: property[style]
     *  ``Object`` Feature style hash to use when creating a layer.
     *   If none is defined, OpenLayers.Feature.Vector.style['default'] is used
     *   instead.
     */
    style: null,

    /** private: property[defaultStyle]
     *  ``Object`` Feature style hash to apply to the default
     *   OpenLayers.Feature.Vector.style['default'] if no style was specified.
     */
    defaultStyle: {
        fillColor: "#FF0000",
        strokeColor: "#FF0000",
        fontColor: "#000000"
    },

    /** api: config[layerOptions]
     *  ``Object``
     *  Options to be passed to the OpenLayers.Layer.Vector constructor.
     */
    layerOptions: {},

    /** api: config[fadeRatio]
     *  ``Numeric``
     *  The fade ratio to apply when features are not selected.
     */
    fadeRatio: 0.4,

    /** api: config[opacityProperties]
     *  ``Array(String)``
     *  The style properties refering to opacity.
     */
    opacityProperties: [
        "fillOpacity", "hoverFillOpacity",
        "strokeOpacity", "hoverStrokeOpacity"
    ],

    /** api: config[defaultOpacity]
     *  ``Numeric``
     *  Default opacity maximum value
     */
    defaultOpacity: 1,

    /** api: property[toggleGroup]
     *  ``String``
     *  The name of the group used for the buttons created.  If none is
     *  provided, it's set to this.map.id.
     */
    toggleGroup: null,

    /** api: property[popupOptions]
     *  ``Object``
     *  The options hash used when creating GeoExt.Popup objects.
     */
    popupOptions: {},

    /** api: property[styler]
     *  ``Styler``
     *  The styler type to use in the FeaturePanel widget.
     */
    styler: null,

    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        Ext.apply(this, config);

        this.drawControls = [];
        this.actions = [];

        this.initMap();

        // if set, automatically creates a "annotation" layer
        var style = this.style || OpenLayers.Util.applyDefaults(
            this.defaultStyle, OpenLayers.Feature.Vector.style["default"]);
        var styleMap = new OpenLayers.StyleMap({
            'default': style,
            'vertices': new OpenLayers.Style({
                 pointRadius: 5,
                 graphicName: "square",
                 fillColor: "white",
                 fillOpacity: 0.6,
                 strokeWidth: 1,
                 strokeOpacity: 1,
                 strokeColor: "#333333"
             })
        });
        var layerOptions = OpenLayers.Util.applyDefaults(
            this.layerOptions, {
                styleMap: styleMap,
                displayInLayerSwitcher: false
            }
        );
        layer = new OpenLayers.Layer.Vector("Annotation", layerOptions);
        this.layer = layer;
        this.map.addLayer(layer);

        layer.events.on({
            "beforefeatureselected": this.onBeforeFeatureSelect,
            "featureunselected": this.onFeatureUnselect,
            "featureselected": this.onFeatureSelect,
            "beforefeaturemodified": this.onModificationStart,
            "featuremodified": this.onModification,
            "afterfeaturemodified": this.onModificationEnd,
            "beforefeatureadded": this.onBeforeFeatureAdded,
            scope: this
        });

        // 2nd, create new ones from the current active layer
        this.initDrawControls(layer);
        this.actions.push('-');
        this.initFeatureControl(layer);
        this.initDeleteAllAction();
        this.actions.push('-');
        this.initExportAsKmlAction();

        GEOR.Annotation.superclass.constructor.apply(this, arguments);
    },

    /** private: method[initMap]
     *  Convenience method to make sure that the map object is correctly set.
     */
    initMap: function() {
        if (this.map instanceof GeoExt.MapPanel) {
            this.map = this.map.map;
        }

        if (!this.map) {
            this.map = GeoExt.MapPanel.guess().map;
        }

        // if no toggleGroup was defined, set to this.map.id
        if (!this.toggleGroup) {
            this.toggleGroup = this.map.id;
        }
    },

    /** private: method[initFeatureControl]
     *  :param layer: ``OpenLayers.Layer.Vector``
     *  Create a ModifyFeature control linked to the passed layer and
     *  add it to the map.  An GeoExt.Action is also created and pushed to the
     *  actions array.
     */
    initFeatureControl: function(layer) {
        var control, actionOptions;

        var options = {
            selectFeature: function(feature) {
                var MF = OpenLayers.Control.ModifyFeature;
                this.mode = MF.RESHAPE | MF.DRAG;
                if (feature.attributes.isCircle){
                    this.mode = MF.RESIZE | MF.DRAG;
                }
                if (feature.attributes.isBox){
                    this.mode = MF.RESHAPE | MF.RESIZE & ~MF.RESHAPE | MF.DRAG;
                }
                MF.prototype.selectFeature.apply(this, arguments);
            },
            vertexRenderIntent: 'vertices'
        };
        control = new OpenLayers.Control.ModifyFeature(layer, options);

        this.featureControl = control;

        actionOptions = {
            control: control,
            map: this.map,
            // button options
            toggleGroup: this.toggleGroup,
            allowDepress: false,
            pressed: false,
            tooltip: OpenLayers.i18n("annotation.modify"),
            // check item options
            group: this.toggleGroup,
            iconCls: "gx-featureediting-editfeature",
            iconAlign: 'top',
            text: OpenLayers.i18n("annotation.modify"),
            checked: false
        };

        var action = new GeoExt.Action(actionOptions);

        this.actions.push(action);
    },

    /** private: method[initDrawControls]
     *  :param layer: ``OpenLayers.Layer.Vector``
     *  Create DrawFeature controls linked to the passed layer and
     *  depending on its geometryType property and add them to the map.
     *  GeoExt.Action are also created and pushed to the actions array.
     */
    initDrawControls: function(layer) {
        var control, handler, geometryTypes, geometryType,
                options, action, iconCls, actionOptions, tooltip;

        geometryTypes = [
            "Point", "Circle", "LineString", "Polygon", "Box", "Label"
        ];


        for (var i = 0; i < geometryTypes.length; i++) {
            options = {
                handlerOptions: {
                    stopDown: true,
                    stopUp: true
                }
            };
            geometryType = geometryTypes[i];

            switch (geometryType) {
                case "LineString":
                    handler = OpenLayers.Handler.Path;
                    iconCls = "gx-featureediting-draw-line";
                    tooltip = OpenLayers.i18n("annotation.create_line");
                    break;
                case "Point":
                    handler = OpenLayers.Handler.Point;
                    iconCls = "gx-featureediting-draw-point";
                    tooltip = OpenLayers.i18n("annotation.create_point");
                    break;
                case "Circle":
                    handler = OpenLayers.Handler.RegularPolygon;
                    options.handlerOptions.sides = 32;
                    options.handlerOptions.irregular = false;
                    iconCls = "gx-featureediting-draw-circle";
                    tooltip = OpenLayers.i18n("annotation.create_circle");
                    break;
                case "Polygon":
                    handler = OpenLayers.Handler.Polygon;
                    iconCls = "gx-featureediting-draw-polygon";
                    tooltip = OpenLayers.i18n("annotation.create_polygon");
                    break;
                case "Box":
                    handler = OpenLayers.Handler.RegularPolygon;
                    options.handlerOptions.sides = 4;
                    options.handlerOptions.irregular = true;
                    iconCls = "gx-featureediting-draw-box";
                    tooltip = OpenLayers.i18n("annotation.create_box");
                    break;
                case "Label":
                    handler = OpenLayers.Handler.Point;
                    iconCls = "gx-featureediting-draw-label";
                    tooltip = OpenLayers.i18n("annotation.create_label");
                    break;
            }

            control = new OpenLayers.Control.DrawFeature(
                    layer, handler, options);

            this.drawControls.push(control);

            if (geometryType == "Label") {
                control.events.on({
                    "featureadded": this.onLabelAdded,
                    scope: this
                });
            }

            if (geometryType == "Circle") {
                control.events.on({
                    "featureadded": this.onCircleAdded,
                    scope: this
                });
            }

            if (geometryType == "Box") {
                control.events.on({
                    "featureadded": this.onBoxAdded,
                    scope: this
                });
            }

            control.events.on({
                "featureadded": this.onFeatureAdded,
                scope: this
            });

            actionOptions = {
                control: control,
                map: this.map,
                // button options
                toggleGroup: this.toggleGroup,
                allowDepress: false,
                pressed: false,
                tooltip: tooltip,
                iconCls: iconCls,
                text: OpenLayers.i18n("annotation." + geometryType.toLowerCase()),
                iconAlign: 'top',
                // check item options
                group: this.toggleGroup,
                checked: false
            };

            action = new GeoExt.Action(actionOptions);

            this.actions.push(action);
        }
    },

    /** private: method[destroyDrawControls]
     *  Destroy all drawControls and all their related objects.
     */
    destroyDrawControls: function() {
        for (var i = 0; i < this.drawControls.length; i++) {
            this.drawControls[i].destroy();
        }
        this.drawControls = [];
    },

    /** private: method[initDeleteAllAction]
     *  Create a Ext.Action object that is set as the deleteAllAction property
     *  and pushed to te actions array.
     */
    initDeleteAllAction: function() {
        var actionOptions = {
            handler: this.deleteAllFeatures,
            scope: this,
            text: OpenLayers.i18n('annotation.delete_all'),
            iconCls: "gx-featureediting-delete",
            iconAlign: 'top',
            tooltip: OpenLayers.i18n('annotation.delete_all_features')
        };

        var action = new Ext.Action(actionOptions);

        this.actions.push(action);
    },

    /** private: method[deleteAllFeatures]
     *  Called when the deleteAllAction is triggered (button pressed).
     *  Destroy all features from all layers.
     */
    deleteAllFeatures: function() {
        Ext.MessageBox.confirm(OpenLayers.i18n('annotation.delete_all_features'), OpenLayers.i18n('annotation.delete_features_confirm'), function(btn) {
            if (btn == 'yes') {
                if (this.popup) {
                    this.popup.close();
                    this.popup = null;
                }

                this.layer.destroyFeatures();
            }
        },
        this);
    },

    /** private: method[initExportAsKmlAction]
     *  Create a Ext.Action object that is set as the exportAsKml property
     *  and pushed to the actions array.
     */
    initExportAsKmlAction: function() {
        var actionOptions = {
            handler: this.exportAsKml,
            scope: this,
            text: OpenLayers.i18n('annotation.export_as_kml'),
            iconCls: "gx-featureediting-export",
            iconAlign: 'top',
            tooltip: OpenLayers.i18n('annotation.export_as_kml_tip')
        };

        var action = new Ext.Action(actionOptions);

        this.actions.push(action);
    },

    /** private: method[exportAsKml]
     *  Called when the exportAsKml is triggered (button pressed).
     */
    exportAsKml: function() {
        GEOR.waiter.show();
        var urlObj = OpenLayers.Util.createUrlObject(window.location.href),
            format = new OpenLayers.Format.KML({
                'foldersName': urlObj.host, // TODO use instance name instead
                'internalProjection': this.map.getProjectionObject(),
                'externalProjection': new OpenLayers.Projection("EPSG:4326")
            });
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/kml/",
            data: format.write(this.layer.features),
            success: function(response) {
                var o = Ext.decode(response.responseText);
                window.location.href = GEOR.config.PATHNAME + "/" + o.filepath;
            }
        });
    },

    /** private: method[getActiveDrawControl]
     *  :return: ``OpenLayers.Control.DrawFeature or false``
     *  Get the current active DrawFeature control.  If none is active, false
     *  is returned.
     */
    getActiveDrawControl: function() {
        var control = false;

        for (var i = 0; i < this.drawControls.length; i++) {
            if (this.drawControls[i].active) {
                control = this.drawControls[i];
                break;
            }
        }

        return control;
    },

    /** private: method[onLabelAdded]
     *  :param event: ``event``
     *  Called when a new label feature is added to the layer.  Set a flag
     *  to let the controler know it's a label.
     */
    onLabelAdded: function(event) {
        var feature = event.feature;
        feature.style.label = feature.attributes.label;
        feature.style.graphic = false;
        feature.style.labelSelect = true;
        feature.isLabel = true;
    },

    /** private: method[onCircleAdded]
     *  :param event: ``event``
     *  Called when a new circle feature is added to the layer.  Set a flag
     *  to let the controler know it's a circle.
     */
    onCircleAdded: function(event) {
        var feature = event.feature;
        feature.attributes.isCircle = true;
    },

    /** private: method[onBoxAdded]
     *  :param event: ``event``
     *  Called when a new box feature is added to the layer.  Set a flag
     *  to let the controler know it's a box.
     */
    onBoxAdded: function(event) {
        var feature = event.feature;
        feature.attributes.isBox = true;
    },

    /** private: method[onFeatureAdded]
     *  :param event: ``event``
     *  Called when a new feature is added to the layer.  Change the state
     *  of the feature to INSERT and select it.
     */
    onFeatureAdded: function(event) {
        var feature, drawControl;

        feature = event.feature;
        feature.state = OpenLayers.State.INSERT;

        drawControl = this.getActiveDrawControl();
        if (drawControl) {
            drawControl.deactivate();
            this.lastDrawControl = drawControl;
        }

        this.featureControl.activate();

        var control = this.featureControl;
        control.selectFeature.defer(1, control, [feature]);

    },

    /** private: method[onModificationStart]
     *  :param event: ``event``
     *  Called when a feature is selected.  Display a popup that contains the
     *  FeaturePanel.
     */
    onModificationStart: function(event) {
        var feature = (event.geometry) ? event : event.feature;

        // to keep the state before any modification, useful when hitting the
        // 'cancel' button
        /*
         if(feature.state != OpenLayers.State.INSERT){
         feature.myClone = feature.clone();
         feature.myClone.fid = feature.fid;
         }
         */

        // if the user clicked on an other feature while adding a new one,
        // deactivate the draw control.
        var drawControl = this.getActiveDrawControl();
        if (drawControl) {
            drawControl.deactivate();
            this.featureControl.activate();
        }

        var options = {
            features: [feature],
            styler: this.styler
        };

        this.featurePanel = new GEOR.FeaturePanel(options);

        // display the popup
        popupOptions = {
            location: feature,
            // the following line is here for compatibility with
            // GeoExt < 1 (before changeset 2343)
            feature: feature,
            items: [this.featurePanel]
        };
        popupOptions = OpenLayers.Util.applyDefaults(popupOptions,
                                                     this.popupOptions);
        popupOptions = OpenLayers.Util.applyDefaults(popupOptions, {
            layout: 'fit',
            border: false,
            width: 280
        });

        var popup = new GeoExt.Popup(popupOptions);
        feature.popup = popup;
        this.popup = popup;
        popup.on({
            close: function() {
                if (OpenLayers.Util.indexOf(this.layer.selectedFeatures, feature) > -1) {
                    this.featureControl.unselectFeature(feature);
                    this.featureControl.deactivate();
                }
            },
            scope: this
        });
        popup.show();

    },

    /** private: method[onModification]
     *  :param event: ``event``
     */
    onModification: function(event) {
        var feature = (event.geometry) ? event : event.feature;
        //we could execute commits here
    },

    /** private: method[onModificationEnd]
     *  :param event: ``event``
     */
    onModificationEnd: function(event) {
        var feature = (event.geometry) ? event : event.feature;
        // or we could execute commits here also

        if (!feature) {
            return;
        }

        if (feature.popup) {
            feature.popup.close();
            feature.popup = null;
        }

        this.reactivateDrawControl();
    },

    /** private: method[onBeforeFeatureAdded]
     *  :param event: ``event``
     *  Called when a new feature is added to the layer.
     */
    onBeforeFeatureAdded: function(event) {
        var feature = event.feature;
        this.parseFeatureStyle(feature);
        this.parseFeatureDefaultAttributes(feature);
    },

    /** private: method[parseFeatureStyle]
     */
    parseFeatureStyle: function(feature) {
        var symbolizer = this.layer.styleMap.createSymbolizer(feature);
        feature.style = symbolizer;
    },

    /** private: method[parseFeatureDefaultAttributes]
     *  :param event: ``OpenLayers.Feature.Vector``
     *  Check if the feature has any attributes.  If not, add those defined in
     *  this.defaultAttributes.
     */
    parseFeatureDefaultAttributes: function(feature) {
        var hasAttributes;

        if(this.useDefaultAttributes === true) {
            hasAttributes = false;

            for (var key in feature.attributes) {
                hasAttributes = true;
                break;
            }

            if(!hasAttributes) {
                for(var i=0; i<this.defaultAttributes.length; i++) {
                    feature.attributes[this.defaultAttributes[i]] =
                        this.defaultAttributesValues[i];
                }
            }
        }
    },

    /** private: method[reactivateDrawControl]
     */
    reactivateDrawControl: Ext.emptyFn,

    /** private: method[onBeforeFeatureSelect]
     *  :param event: ``event``
     *  Called before a feature is selected
     */
    onBeforeFeatureSelect: function(event) {
        var feature = (event.geometry) ? event : event.feature;

        // if it's the first feature that is selected
        if(feature.layer.selectedFeatures.length === 0) {
            this.applyStyles('faded', {'redraw': true});
        }
    },

    /** private: method[onFeatureUnselect]
     *  :param event: ``event``
     *  Called when a feature is unselected.
     */
    onFeatureUnselect: function(event) {
        var feature = (event.geometry) ? event : event.feature;
        this.applyStyle(feature, 'faded', {'redraw': true});

        // if it's the last feature that is unselected
        if(feature.layer.selectedFeatures.length === 0) {
            this.applyStyles('normal', {'redraw': true});
        }
    },

    /** private: method[onFeatureSelect]
     *  :param event: ``event``
     *  Called when a feature is selected
     */
    onFeatureSelect: function(event) {
        var feature = (event.geometry) ? event : event.feature;
        this.applyStyle(feature, 'normal', {'redraw': true});
    },

    /** private: method[applyStyles]
     *  :param style: ``String`` Mandatory.  Can be "normal" or "faded".
     *  :param options: ``Object`` Object of options.
     *  Apply a specific style to all layers of this controler.  If
     *  'redraw': true was specified in the options, the layer is redrawn after.
     */
    applyStyles: function(style, options) {
        style = style || "normal";
        options = options || {};
        var layer = this.layer;
        for(var j=0; j<layer.features.length; j++) {
            feature = layer.features[j];
            // don't apply any style to features coming from the
            // ModifyFeature control
            if(!feature._sketch) {
                this.applyStyle(feature, style);
            }
        }

        if(options['redraw'] === true) {
            layer.redraw();
        }
    },

    /** private: method[applyStyle]
     *  :param feature: ``OpenLayers.Feature.Vector``
     *  :param style: ``String`` Mandatory.  Can be "normal" or "faded".
     *  :param options: ``Object`` Object of options.
     *  Apply a specific style to a specific feature.  If 'redraw': true was
     *  specified in the options, the layer is redrawn after.
     */
    applyStyle: function(feature, style, options) {
        var fRatio;
        options = options || {};

        switch (style) {
          case "faded":
            fRatio = this.fadeRatio;
            break;
          default:
            fRatio = 1 / this.fadeRatio;
        }

        for(var i=0; i<this.opacityProperties.length; i++) {
            property = this.opacityProperties[i];
            if(feature.style[property]) {
                feature.style[property] *= fRatio;
            }
        }

        if(options['redraw'] === true) {
            feature.layer.drawFeature(feature);
        }
    },

    CLASS_NAME: "Annotation"
});

