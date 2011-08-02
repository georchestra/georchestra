/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include OpenLayers/Strategy/Save.js
 * @include OpenLayers/Strategy/BBOX.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Control/SelectFeature.js
 * @include OpenLayers/Control/ModifyFeature.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Control/Snapping.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Handler/Point.js
 * @include OpenLayers/Handler/Path.js
 * @include OpenLayers/Handler/Polygon.js
 * @include OpenLayers/Style.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Geometry/MultiPoint.js
 * @include OpenLayers/Geometry/MultiLineString.js
 * @include OpenLayers/Geometry/MultiPolygon.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include GeoExt/widgets/Action.js
 * @include GeoExt/plugins/AttributeForm.js
 */

Ext.namespace('GEOR.Editing');

GeoExt.form.recordToField.TIPTRANSLATIONS = {
    "required": "obligatoire",
    "text": "Texte",
    "string": "Chaine de caractères",
    "number": "Nombre",
    "float": "Flottant",
    "decimal": "Décimal",
    "double": "Entier double",
    "int": "Entier",
    "long": "Entier long",
    "integer": "Entier",
    "short": "Entier court",
    "byte": "Entier sur 8 bits",
    "unsignedLong": "Entier long non signé",
    "unsignedInt": "Entier non signé",
    "unsignedShort": "Entier court non signé",
    "unsignedByte": "Entier sur 8 bits non signé",
    "nonNegativeInteger": "Entier non négatif",
    "positiveInteger": "Entier positif",
    "boolean": "Booléen",
    "date": "Date",
    "dateTime": "Date avec heure"
};

/**
 */
GEOR.Editing.LayerEditingPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: layer
     * {OpenLayers.Layer.Vector}
     */
    layer: null,

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    map: null,

    /**
     * Property: attributeStore
     * {GeoExt.data.AttributeStore}
     */
    attributeStore: null,

    /**
     * Property: selectFeature
     * {OpenLayers.Control.SelectFeature}
     */
    selectFeature: null,

    /**
     * Property: drawFeature
     * {OpenLayers.Control.DrawFeature}
     */
    drawFeature: null,

    /**
     * Property: snap
     * {OpenLayers.Control.Snapping}
     */
    snap: null,

    /**
     * Property: modifyFeature
     * {OpenLayers.Control.ModifyFeature}
     */
    modifyFeature: null,

    /**
     * Property: nextSelectedFeature
     * {OpenLayers.Feature.Vector}
     */
    nextSelectedFeature: null,

    /**
     * Property: lastFeature
     * {OpenLayers.Feature.Vector} The feature being unselected.
     */
    lastFeature: null,

    /**
     * Property: initialSelectedGeometry
     * {OpenLayers.Geometry}
     */
    initialSelectedGeometry: null,

    /**
     * Property: strategy
     * {OpenLayers.Strategy.Save}
     */
    strategy: null,

    /**
     * Property: saveBtn
     * {Ext.Button}
     */
    saveBtn: null,

    /**
     * Property: deleteBtn
     * {Ext.Button}
     */
    deleteBtn: null,

    /**
     * Property: cancelBtn
     * {Ext.Button}
     */
    cancelBtn: null,

    /**
     */
    initComponent: function() {

        this.strategy = new OpenLayers.Strategy.Save();

        // prevent the layer from destroying the protocol, as
        // it isn't handled by us
        this.protocol.autoDestroy = false;

        // create vector layer based on the passed protocol
        // and display it
        this.layer = new OpenLayers.Layer.Vector(
            "GEOR.Editing.LayerEditingPanel", {
            strategies: [
                new OpenLayers.Strategy.BBOX(), 
                this.strategy
            ],
            protocol: this.protocol,
            styleMap: this.createStyleMap(),
            displayInLayerSwitcher: false,
            alwaysInRange: true
        });
        this.map.addLayer(this.layer);
        this.layer.refresh();

        // add editing controls
        var type = this.addLayerControls();

        this.tbar = [
            new GeoExt.Action({
                map: this.map,
                control: this.drawFeature, 
                enableToggle: true,
                toggleGroup: 'edit',
                tooltip: "Dessiner " + type.text,
                iconCls: type.iconCls
            }), 
            new GeoExt.Action({
                map: this.map,
                control: this.selectFeature,
                enableToggle: true,
                toggleGroup: 'edit',
                text: "Sélectionner",
                pressed: true
            })
        ];

        this.bbar = [
            '->',
            {
                text: 'Tout annuler',
                iconCls: 'geor-btn-cancel',
                handler: function() {
                    this.layer.refresh({ force: true });
                    this.lastFeature = null;
                },
                scope: this
            },
            {
                text: 'Synchroniser',
                iconCls: 'geor-btn-sync',
                handler: function() {
                    if (!this.isFeatureDirty()) {
                        if (this.layer.selectedFeatures.length > 0) {
                            var f = this.layer.selectedFeatures[0];
                            this.modifyFeature.unselectFeature(f);
                            this.layer.drawFeature(f, "default");
                            this.silentUnselect();
                            this.cleanForm();
                            this.lastFeature = null;
                        }
                        this.strategy.save();
                        // we want to force activation of select feature:
                        this.selectFeature.activate();
                    } else {
                        Ext.MessageBox.alert(
                            "Attention",
                            "Veuillez confirmer ou annuler " +
                            "les modifications en cours avant " +
                            "de synchroniser avec le serveur"
                        );
                    }
                },
                scope: this
            }
        ];


        this.cancelBtn = new Ext.Button({
            text: 'Annuler',
            handler: function() { 
                var feature = this.layer.selectedFeatures[0];
                if (feature) {
                    this.modifyFeature.unselectFeature(feature);
                    this.silentUnselect();
                    this.layer.removeFeatures([feature]);
                    feature.geometry = this.initialSelectedGeometry.clone();
                    feature.state = OpenLayers.State.UNKNOWN;
                    this.layer.addFeatures([feature]);
                    this.silentSelect(feature);
                    this.modifyFeature.selectFeature(feature);
                }
                this.formPanel.getForm().reset();
            },
            scope: this
        });

        this.deleteBtn = new Ext.Button({
            text: 'Supprimer',
            disabled: true,
            handler: function() {
                var feature = this.layer.selectedFeatures[0];
                if (!feature) {
                    Ext.Msg.alert('Attention', 'Veuillez sélectionner un objet en premier lieu');
                    return;
                }
                if (feature.fid === null) {
                    this.silentUnselect();
                    this.modifyFeature.unselectFeature(feature);
                    this.layer.destroyFeatures([feature]);
                } else {
                    if (feature.state==OpenLayers.State.DELETE) {
                       feature.state = OpenLayers.State.UPDATE; 
                    } else {
                        feature.state = OpenLayers.State.DELETE;
                    }
                    this.layer.drawFeature(feature, this.selectFeature.renderIntent);
                    this.modifyFeature.unselectFeature(feature);
                    this.silentUnselect();
                }
                this.cleanForm();
                this.lastFeature = null;
            },
            scope: this
        });

        this.saveBtn = new Ext.Button({
            text: 'Confirmer',
            formBind: true,
            handler: this.confirmHandler,
            scope: this
        });
         
        this.formPanel = new Ext.form.FormPanel({
            plugins: [
                new GeoExt.plugins.AttributeForm({
                    attributeStore: this.attributeStore,
                    displayFieldType: true,
                    markRequiredFields: true
                })
            ],
            defaults: {
                maxLengthText: "Texte trop long",
                minLengthText: "Texte trop court",
                maxText: "Valeur maximale dépassée",
                minText: "Valeur minimale non atteinte",
                nanText: "Nombre non valide"
            },
            trackResetOnLoad: true,
            monitorValid: true,
            autoScroll: true,
            labelWidth: 100,
            bodyStyle: 'padding: 5px;',
            bodyCssClass: 'layer-editing-panel',
            labelSeparator: ' :',
            border: false,
            disabled: true,
            bbar: [this.cancelBtn, this.deleteBtn, this.saveBtn],
            labelStyle: 'font-size:11px;text-transform:lowercase;'
        });


        // build layout
        Ext.apply(this, {
            layout: 'form',
            layout: 'fit',
            items: [ this.formPanel ]
        });

        // manage events
        this.layer.events.register('beforefeatureselected', this, this.checkSelect);
        this.layer.events.register('featureunselected', this, this.unSelect);
        
        this.drawFeature.events.register('featureadded', this, function(e) {
            this.selectFeature.unselectAll();
            this.selectFeature.select(e.feature);
            // HACK: fixes selected feature can't be unselected
            this.selectFeature.handlers.feature.lastFeature = e.feature;
        });
        this.strategy.events.register('success', this, function() {
            Ext.Msg.alert(
                'Information', 
                'Les données de cette couche ont été transférées sur le serveur avec succès.'
            );
            this.lastFeature = null;
            this.layer.redraw();
        });
        this.strategy.events.register('fail', this, function() {
            Ext.Msg.alert(
                'Attention', 
                'Il y eu une erreur lors de l\'enregistrement des données sur le serveur'
            );
        });
        
        this.formPanel.on('clientvalidation', function(formPanel, valid){
            if (this.isFeatureDirty()) {
                this.saveBtn.enable();
                this.cancelBtn.enable();
            } else {
                this.cancelBtn.disable();
                this.saveBtn.disable();
            }
            var feature = this.layer.selectedFeatures[0];
            if (feature) {
                if (feature.state==OpenLayers.State.DELETE) {
                    this.deleteBtn.setText('Restaurer');
                } else {
                    this.deleteBtn.setText('Supprimer');
               }
            }
        }, this);

        GEOR.Editing.LayerEditingPanel.superclass.initComponent.apply(this, arguments);
    },

    /**
     * Method: confirmHandler
     *   confirm the modifications on the current selected feature
     *
     */
    confirmHandler: function(feature) {
        feature = (feature.CLASS_NAME == "OpenLayers.Feature.Vector" ? feature : null) || this.layer.selectedFeatures[0];
        if (!feature) {
            // should not happen
            Ext.Msg.alert('Attention', 'Aucun objet sélectionné !');
            return;
        }
        
        var values = {};
        this.formPanel.form.items.each(function(field){
            values[field.getName()] = field.getValue();
        });
        Ext.apply(
            feature.attributes,
            values //instead of this.formPanel.getForm().getValues() 
            // HTML form values -> does not suit our needs
            // eg: unchecked checkboxes are not submitted if unchecked => 
            // impossible to have a "false" value with formPanel.getForm().getValues()
        );
        
        if (feature.state != OpenLayers.State.INSERT) {
            feature.state = OpenLayers.State.UPDATE;
            this.layer.drawFeature(feature);
        }
        this.lastFeature = null; 
        this.modifyFeature.unselectFeature(feature);
        this.silentUnselect();
        this.cleanForm();
    },

    /**
     * Method: addLayerControls
     *   add select & draw control according to layer symbol type
     *
     */
    addLayerControls: function() {

        var labels = {
            Point: {
                text: 'un point',
                iconCls: 'drawpoint'
            },
            Line: {
                text: 'une ligne',
                iconCls: 'drawline'
            },
            Polygon: {
                text: 'un polygone',
                iconCls: 'drawpolygon'
            }
        };

        this.selectFeature = new OpenLayers.Control.SelectFeature(
            this.layer
        );

        this.modifyFeature = new OpenLayers.Control.ModifyFeature(
            this.layer, {
                standalone: true
            }
        );
        this.map.addControl(this.modifyFeature);
        this.modifyFeature.activate();

        var type = GEOR.ows.getSymbolTypeFromAttributeStore(this.attributeStore);
        var handlerOptions = {};
        var typeName = type.type;
        if (type.multi=='Multi') {
            handlerOptions.multi = true;
        }
        if (type.type=='Line') {
            type.type = 'Path';
        }

        this.drawFeature = new OpenLayers.Control.DrawFeature(
            this.layer,
            OpenLayers.Handler[type.type],
            {
                handlerOptions: handlerOptions
            }
        );

        this.snap = new OpenLayers.Control.Snapping({layer: this.layer});
        this.map.addControl(this.snap);
        
        return labels[typeName];
    },

    /**
     * Method: isFeatureDirty
     *
     * Check if the feature's geometry or its attributes has been modified
     */
    isFeatureDirty: function(feature) {
        if (!feature) {
            var feature = this.layer.selectedFeatures.length > 0 ?
                          this.layer.selectedFeatures[0] : undefined;
        }
        return this.formPanel.getForm().isDirty() ||
               (feature &&
                !this.initialSelectedGeometry.equals(feature.geometry));
    },

    /**
     * Method: checkSelect
     *   cancel select if the form is in a dirty state
     */
    checkSelect: function(e) {
        this.formPanel.enable();
        this.deleteBtn.enable();
        var feature = e.feature;
        if (this.isFeatureDirty(this.lastFeature)) {
            this.nextSelectedFeature = feature;
            return false;
        }
        this.loadFeature(feature);
        return false;
    },

    /**
     * Method: loadFeature
     *   load feature's data for editing
     */
    loadFeature: function(feature) {
        this.silentSelect(feature);
        this.initialSelectedGeometry = feature.geometry.clone();
        this.formPanel.getForm().setValues(feature.attributes);
    },

    /**
     * Method: silentSelect
     *  select feature without trigerring any event
     */
    silentSelect: function(feature) {
        // temporary unregister callback to avoid recursion
        this.selectFeature.unselectAll();
        this.layer.events.unregister('beforefeatureselected', this, this.checkSelect);
        this.selectFeature.select(feature); 
        this.modifyFeature.selectFeature(feature);
        this.layer.events.register('beforefeatureselected', this, this.checkSelect);
        this.formPanel.enable();
    },

    /**
     * Method: silentUnselect
     *  unselect feature without trigerring any event
     */
    silentUnselect: function(feature) {
        // temporary unregister callback to avoid recursion
        this.layer.events.unregister('featureunselected', this, this.unSelect);
        this.selectFeature.unselectAll(); 
        this.formPanel.disable();
        this.layer.events.register('featureunselected', this, this.unSelect);
    },

    /**
     * Method: unSelect
     *  callback executed on feature unselected.
     *  checks for unsaved data, and displays confirm dialog in this case
     */
    unSelect: function(e) {
        var feature = e.feature;
        this.lastFeature = feature;
        this.modifyFeature.unselectFeature(feature);
        this.layer.drawFeature(feature, "default");
        if (this.isFeatureDirty(feature)) {
            Ext.MessageBox.confirm(
                'Attention : modifications non confirmées', 
                'Voulez-vous continuer et annuler les modifications ?',
                function(btn) {
                    if (btn=='yes') {
                        // restore unmodified geometry
                        this.layer.removeFeatures([feature]);
                        feature.geometry = this.initialSelectedGeometry.clone();
                        feature.state = OpenLayers.State.UNKNOWN;
                        this.layer.addFeatures([feature]);

                        if (this.nextSelectedFeature) {
                            this.loadFeature(this.nextSelectedFeature);
                            this.nextSelectedFeature = null;
                        } else {
                            this.cleanForm();
                            this.formPanel.disable();
                        }
                    } else {
                        // we do as it the "confirm" button had been pressed here 
                        this.confirmHandler(feature);
                    }
                },
                this
            );
            return false;
        }
        this.cleanForm();
    },

    /**
     * Method: cleanForm
     * Set empty values in form in a non-dirty state 
     */
    cleanForm: function() {
        var form = this.formPanel.getForm();
        var values = {};
        Ext.each(form.items.items, function(field){
            values[field.getName()] = null; // or '' ?
        });
        form.setValues(values);
    },

    /**
     * Method: createStyleMap
     * Create a style map for the vector layer.
     *
     * Returns:
     * {<OpenLayer.StyleMap>} The style map.
     */
    createStyleMap: function() {
        
        var style = OpenLayers.Util.extend({}, 
                        OpenLayers.Feature.Vector.style['default']);
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(
                OpenLayers.Util.extend(style, {
                    strokeWidth: 2
                })
            )
        });
            
        // create a styleMap for the vector layer so that features
        // have different styles depending on their states, also
        // use the "select" render intent for styling vertices
        // displayed when modifying a feature (such vertices
        // have the _sketch property set).
        var context = function(feature) {
            var state = feature._sketch ? "vertex" : undefined;
            return {
                state: state || feature.state || OpenLayers.State.UNKNOWN
            };
        };
        var lookup = {};
        lookup[OpenLayers.State.UNKNOWN] = {};
        lookup[OpenLayers.State.UPDATE] = {
            fillColor: "green",
            strokeColor: "green"
        };
        lookup[OpenLayers.State.DELETE] = {
            fillColor: "red",
            strokeColor: "red",
            fillOpacity: 0.1,
            strokeOpacity: 0.6,
            display: ""
        };
        lookup[OpenLayers.State.INSERT] = {
            fillColor: "violet",
            strokeColor: "violet"
        };
        lookup["vertex"] = OpenLayers.Feature.Vector.style.select;
        styleMap.addUniqueValueRules("default", "state", lookup, context);
        return styleMap;
    },
    
    /**
     * Method: tearDown
     * Hide vector layer
     */
    tearDown: function() {
        // deactivate controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.deactivate();
        });
        // remove & destroy vector layer
        this.layer.setVisibility(false);
    },
    
    /**
     * Method: setUp
     * Show vector layer
     */
    setUp: function() {
        // activate controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.activate();
        });
        // remove & destroy vector layer
        this.layer.setVisibility(true);
    },
    
    /**
     * Method: destroy
     * Remove vector layer on panel destroy
     */
    destroy: function() {
        // deactivate & remove controls
        Ext.each([this.drawFeature, this.selectFeature, this.modifyFeature, this.snap], function(control) {
            control.deactivate();
            this.map.removeControl(control);
        });
        // remove & destroy vector layer
        this.layer.destroy();
        GEOR.Editing.LayerEditingPanel.superclass.destroy.apply(this, arguments);
    }

});

Ext.reg("geor_layereditingpanel", GEOR.Editing.LayerEditingPanel);
