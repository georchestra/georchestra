/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/** api: (define)
 *  module = GeoExt.ux
 *  class = FeatureBrowser
 *  base_link = `Ext.Panel <http://dev.sencha.com/deploy/dev/docs/?class=Ext.Panel>`_
 */

/** api: constructor
 *  .. class:: FeatureBrowser(config)
 *      
 *      Creates a Panel to browse in a features list,
 *  show attributes for each using templates.
 */
GeoExt.ux.FeatureBrowser = Ext.extend(Ext.Panel, {

    /* begin i18n */
    /** api: config[counterText]
     *  ``String`` i18n, The counter message to display (defaults to "{0} of
     *  {1}). Note that this string is formatted using {0} as a token for index
     *  and {1} as a token for total. These tokens should be preserved when
     *  overriding this string if showing those values is desired.
     */
    counterText: "{0} of {1} features",

    /** api: config[elseTpl]
     *  ``Ext.Template`` | ``Ext.XTemplate`` Ext.Template or Ext.XTemplate
     *  to be used for features which don't match any of the tpl keys.
     *  Will be taken into account only if tpl is an Object. Optional.
     */
    elseTpl: null, 

    /** api: config[tpl]
     * ``Ext.Template`` | ``Ext.XTemplate`` | ``Object`` | ``Function``
     *  Ext.Template or Ext.XTemplate to be applied for each feature with
     *  feature.attributes as context. If provided as an Object, each key
     *  may correspond to the value for the tplFeatureAttribute key in the
     *  feature attributes. The corresponding value has to be a valid template.
     *  Optional.
     */
    tpl: undefined,

    /** api: config[tplFeatureAttribute]
     *  ``String`` | ``Number``
     *  The attribute which value is to be compared with one of the tpl members
     *  when provided as an Object. Mandatory is tpl is a Object. Optional.
     */
    tplFeatureAttribute: null,

    /** api: config[skippedFeatureAttributes]
     *  ``Array(String)``
     *  Specifies the feature attributes to skip in the default Ext.Template
     *  created by this component. Only applies when the ``tpl`` option is
     *  not set. Optional.
     */
    skippedFeatureAttributes: null,
    
    /** api: config[features]
     *  ``Array`` Array of ``OpenLayers.Feature.Vector`` to build the
     *  FeatureBrowser with. Required.
     */
    features: null,

    /** private: method[initComponent]
     */
    initComponent: function() {

        this.layout = "card";

        var nbFeatures = this.features.length;

        this.items = [];
        var feature, tpl;
        for (var i = 0; i < nbFeatures; i++) {
            feature = this.features[i];
            tpl = this.getTemplateForFeature(feature);
            
            this.items.push(new Ext.BoxComponent({
                id: 'card-'+ this.id + i,
                html: tpl.apply(feature.attributes)
            }));
        }

        if (nbFeatures > 1) {
            this.bbar = [
                {xtype: 'tbtext', id: 'counter-' + this.id},
                '->',
                {
                    id: 'move-prev' + this.id,
                    iconCls: "x-tbar-page-prev",
                    handler: this.navHandler.createDelegate(this, 
                        [-1, nbFeatures, this.features]),
                    disabled: true,
                    listeners: {
                        click: function(button, e) {
                            e.stopEvent();
                        }
                    }
                },
                {
                    id: 'move-next' + this.id,
                    iconCls: "x-tbar-page-next",
                    handler: this.navHandler.createDelegate(this, 
                        [1, nbFeatures, this.features]),
                    listeners: {
                        click: function(button, e) {
                            e.stopEvent();
                        }
                    }
                }
            ];
        }
        this.activeItem = 0;

        GeoExt.ux.FeatureBrowser.superclass.initComponent.apply(this, arguments);

        // add custom events
        this.addEvents(
        
            /** api: events[featureselected]
             *  Fires when a feature is displayed in the FeatureBrowser.
             *  Application may use this to highlight it on the map, for
             *  example.
             *
             *  Listener arguments:
             *  * panel - :class:`GeoExt.ux.FeatureBrowser` This panel.
             *  * feature - ``OpenLayers.Feature.Vector`` The selected feature
             */
            'featureselected'
        );
        this.fireEvent('featureselected', this, this.features[0]);

        var counter = Ext.getCmp('counter-' + this.id);
        counter && counter.setText(
            String.format(this.counterText, 1, nbFeatures)
        );
    },

    /** private: method[navHandler]
     *  The navigation handler method. Called when navigation buttons 
     *  (next or previous) are clicked
     */
    navHandler: function(direction, total, features) {
        var lay = this.getLayout();
        var i = lay.activeItem.id.split('card-' + this.id)[1];
        var next = parseInt(i, 10) + direction;
        lay.setActiveItem(next);
        this.fireEvent('featureselected', this, features[next]);
        Ext.getCmp('move-prev' + this.id).setDisabled(next === 0);
        Ext.getCmp('move-next' + this.id).setDisabled(next == total - 1);

        var counter = Ext.getCmp('counter-' + this.id);
        counter && counter.setText(
            String.format(this.counterText, next + 1, total)
        );
    },

    /** private: method[getTemplateForFeature]
     *  Returns a template for the given feature.
     *
     *  :param feature: ``OpenLayers.Feature.Vector`` The feature to create
     *      a template with.
     *
     *  :return: ``Ext.Template`` | ``Ext.XTemplate`` The created template.
     */
    getTemplateForFeature: function(feature) {
        var tpl,
            attributes = feature.attributes;

        if (this.tpl instanceof Ext.Template) {
            tpl = this.tpl;
        } else if (typeof this.tpl === 'object') {
            tpl = this.tpl[attributes[this.tplFeatureAttribute]] ||
                  this.elseTpl;
        } else if (typeof this.tpl === 'function') {
            // currently unsupported
        }

        // create a default template with key/value pairs
        if (!tpl) {
            var templateString = '';
            for (var k in attributes) {
                if (attributes.hasOwnProperty(k) &&
                    (this.skippedFeatureAttributes == null ||
                     this.skippedFeatureAttributes.indexOf(k) === -1)) {

                    templateString += '<div>' +
                                      '<b>' + k + ': </b>' +
                                      '{' + k + '}' +
                                      '</div>';
                }
            }
            tpl = new Ext.Template(templateString);
        }

        return tpl;
    }
});
