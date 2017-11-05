/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include Ext.ux/widgets/spinner/NumberSpinner.js
 * @include Ext.ux/widgets/spinner/Spinner.js
 * @include Ext.ux/widgets/palettecombobox/PaletteComboBox.js
 * @include Ext.ux/widgets/colorpicker/ColorPicker.js
 * @include Ext.ux/widgets/colorpicker/ColorMenu.js
 * @include Ext.ux/widgets/colorpicker/ColorPickerField.js
 * @include GEOR_util.js
 * @include GEOR_ows.js
 */

Ext.namespace('GEOR');

/*
   Class: GEOR.ClassificationPanel
   Inherits from:
   - Ext.Panel
*/
GEOR.ClassificationPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} DataStore used to fill the ComboBox
     * comboAttribute.
     * Fields: attribute name and type of the attribute. They corresponds to
     * the namespace of the DescribeFeatureType request on a WFS server.
     * The WFS url is taken from a given layer.	
     */
    attributes: null,

    /**
     * Property: wfsInfo
     * {Object} The symbol type.
     */
    symbolType: null,

    /**
     * Property: wfsInfo
     * {Ext.data.Record} An Ext record with owsURL and typeName fields.
     */
    wfsInfo: null,

    /*
     * Property: cbAttribute
     * {Ext.form.ComboBox} Displays attributes of a specific feature type.
    */
    cbAttribute: null,

    /*
     * Property: dsClassifType
     * {Ext.data.SimpleStore} DataStore used to fill the ComboBox dsClassifType.
     * Fields (type of classification):
       - choropleths
       - proportional symbols
       - unique values
     */
    dsClassifType: null,

    /*
    * Property: cbClassifType
    * {Ext.form.ComboBox} Displays type of classification according to the
    * selected Attribute. Depending on the type of the feature attribute some
    * classification type won't be avalaible. Only numeric values enable
    * choropleths and proportional symbols classifications
    */
    cbClassifType: null,

    /*
     * Property: sbClassCountChoropleth
     * {Ext.ux.NumberSpinner} Number of classes (choropleths classification only)
     */
    sbClassCountChoropleth: null,	

    /*
    * Property: sbClassCountPropSymbol
    * {Ext.ux.NumberSpinner} Number of classes (proportional symbols
    * classification only)
    */
    sbClassCountPropSymbol: null,	

    /*
     * Property: sbMinSize
     * {Ext.ux.NumberSpinner} Minimum symbol size (proportional symbols
     * classification only)
     */
    sbMinSize: null,

    /*
     * Property: sbMaxSize
     * {Ext.ux.NumberSpinner} Maximum symbol size (proportional symbols
     * classification only)
     */
    sbMaxSize: null,

    /*
     * Property: cpFirstColor
     * {Ext.ux.ColorPicker} First color (choropleths classification only)
     */
    cpFirstColor: null,

    /*
     * Property: cpLastColor
     * {Ext.ux.ColorPicker} Last color (choropleths classification only)
     */
    cpLastColor: null,

    /*
     * Property: dsPalette
     * {Ext.data.SimpleStore} DataStore used to fill the ComboBox paletteCombo.
     * Fields: value (palette id) and colors (array of colors)
    */
    dsPalette: null,

    /*
     * Property: cbPalette
     * {Ext.ux.PaletteComboBox} Palette (unique values classification only)
     */
    cbPalette: null,

    /*
     * Property: btClassify
     * {Ext.Button} Launches automatic classification
     */
    btClassify: null,

    /*
     * Property: pnChoropleths
     */
     pnChoropleth: null,

    /*
     * Property: pnPropSymbol
     */
     pnPropSymbol: null,

    /*
     * Property: pnUniqueValue
     */
     pnUniqueValue: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {

        this.cbAttribute = new Ext.form.ComboBox({
            store: this.attributes,
            displayField: 'name',
            valueField: 'name',
            editable: false,
            mode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus:true,
            tpl: GEOR.util.getAttributesComboTpl(),
            fieldLabel: OpenLayers.i18n('Attribute'),
            value: this.attributes.getAt(0).get("name")
        });

        this.dsClassifType = new Ext.data.SimpleStore({
            fields: ['text', 'value']
        });
        this.cbClassifType = new Ext.form.ComboBox({
            store: this.dsClassifType,
            displayField: 'text',
            valueField: 'value',
            editable: false,
            mode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            fieldLabel: OpenLayers.i18n('Type')
        });

        this.sbClassCountPropSymbol = new Ext.ux.NumberSpinner({
            allowNegative: false,
            allowDecimals: false,
            value: 5,
            minValue: 2,
            maxValue: 20,
            fieldLabel: OpenLayers.i18n('Number of classes'),
            width: 30
        });

        // FIXME copy config from sbClassCountPropSymbol
        this.sbClassCountChoropleth = new Ext.ux.NumberSpinner({
            allowNegative: false,
            allowDecimals: false,
            value: 5,
            minValue: 2,
            maxValue: 20,
            fieldLabel: OpenLayers.i18n('Number of classes'),
            width: 30
        });

        this.sbMinSize = new Ext.ux.NumberSpinner({
            allowNegative: false,
            allowDecimals: false,
            value: 2,
            minValue: 1,
            maxValue: 30,
            fieldLabel: OpenLayers.i18n('Minimum size'),
            width: 30
        });

        this.sbMaxSize = new Ext.ux.NumberSpinner({
            allowNegative: false,
            allowDecimals: false,
            value: 10,
            minValue: 1,
            maxValue: 30,
            fieldLabel: OpenLayers.i18n('Maximum size'),
            width: 30
        });

        this.cpFirstColor = new Ext.ux.form.ColorPickerField({
            value: '#FFFFFF',
            fieldLabel: OpenLayers.i18n('First color'),
            width: 100
        });

        this.cpLastColor = new Ext.ux.form.ColorPickerField({
            value: '#497BD1',
            fieldLabel: OpenLayers.i18n('Last color'),
            width: 100
        });

        this.dsPalette = new Ext.data.SimpleStore({
            fields: ['value', 'colors'],
            width: 150
        });

        this.cbPalette = new Ext.ux.PaletteComboBox({
            store: this.dsPalette,
            valueField: 'value',
            editable: false,
            mode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            value: 0,
            selectOnFocus:true,
            fieldLabel: OpenLayers.i18n('Palette'),
            anchor: '90%'  // make the combo resize itself
                           // when its container changes
                           // size
        });

        this.btClassify = new Ext.Button({
        });

        // wrap classification specific widgets in 3 different panels

        this.pnChoropleth = new Ext.FormPanel({
            items: [
                this.sbClassCountChoropleth,
                this.cpFirstColor,
                this.cpLastColor
            ]
        });

        this.pnPropSymbol = new Ext.FormPanel({
            items: [
                this.sbClassCountPropSymbol,
                this.sbMinSize,
                this.sbMaxSize
            ]
        });

        this.pnUniqueValue = new Ext.FormPanel({
            labelWidth: 75,
            items: [
                this.cbPalette
            ],
            // if we don't call doLayout when the panel is
            // shown the combo list isn't as wide as the
            // the combo
            listeners: {
                show: {
                    fn: function(p) {
                        p.doLayout();
                    },
                    single: true
                }
            }
        });

        // connect events to widgets
        this.cbAttribute.on('select', this.on_cbAttribute_selected, this);
        this.cbClassifType.on('select', this.on_cbClassifType_selected, this);
        this.sbMinSize.on('change', this.on_sbMinSize_changed, this);
        this.sbMaxSize.on('change', this.on_sbMaxSize_changed, this);
        this.btClassify.on('click', this.on_btClassify_clicked, this);

        this.filldsPalette(); // fill this combo with predefined palettes	
        this.filldsClassifType(this.attributes.getAt(0).get("type"));

        // build layout
        Ext.apply(this, {
            labelAlign: 'top',
            frame: false,
            border: false,
            bodyStyle: 'padding: 5px;',
            title: OpenLayers.i18n('Auto classification'),
            items: [{
                layout:'column',
                border: false,
                defaults: {
                    border: false
                },
                items: [{
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    items: [
                        Ext.apply(this.cbAttribute, {
                            anchor:'90%'
                        }),
                        Ext.apply(this.cbClassifType, {
                            anchor:'90%'
                        })
                    ]
                },{
                    columnWidth: 0.5,
                    defaults: {
                        border: false
                    },
                    layout: 'card',
                    deferredRender: true,
                    id: '_classif_panel_card',
                    activeItem: 2,
                    items: [
                        this.pnChoropleth,
                        this.pnPropSymbol,
                        this.pnUniqueValue
                    ]
                }]
            }],
            buttons: [
                Ext.apply(this.btClassify, {
                    text: OpenLayers.i18n('Classify')
                })
            ]
        });

        this.addEvents(
            /**
             * Event: change
             * Fires when the apply button is clicked.
             */
            "change"
        );

        // call parent initComponent
        GEOR.ClassificationPanel.superclass.initComponent.call(this);
    },

    /*
     * Method: filldsClassifType
     * Fills the cbClassifType with classification types given the feature's
     * data type. Depending on the selected attribute not all classification
     * type will be available. choropleths and proportional symbols are only
     * available when attribute type is numeric. unique values is always available
     * Parameters:
     * dataType - {String} feature data type
     */
    filldsClassifType: function(dataType) {
        var data = [[OpenLayers.i18n('Unique values'), 'unique_values']];
        if (GEOR.util.isNumericType(dataType)) {
            data.push([OpenLayers.i18n('Color range'), 'choropleths']);
            if ((this.symbolType == 'Point') || (this.symbolType == 'Line')) {
                data.push([OpenLayers.i18n('Proportional symbols'), 'prop_symbols']);
            }
        }
        this.dsClassifType.loadData(data);
        this.cbClassifType.setValue(data[0][1]); // select first value
        this.displayTypeContext(data[0][1]); // show the right widgets given the classification
    },

    /*
     *  Method: filldsPalette
     *  Fills the dsPalette with predefined palettes
     */
    filldsPalette: function() {
        var palettesRaw = [
            [[141,211,199],[255,255,179],[190,186,218],[251,128,114],[128,177,211],[253,180,98],
                [179,222,105],[252,205,229],[217,217,217],[188,128,189],[204,235,197],[255,237,111]],

            [[166,206,227],[31,120,180],[178,223,138],[51,160,44],[251,154,153],[227,26,28],
                [253,191,111],[255,127,0],[202,178,214],[106,61,154],[255,255,153]],

            [[251,180,174],[179,205,227],[204,235,197],[222,203,228],[254,217,166],[255,255,204],
                [229,216,189],[253,218,236],[242,242,242]],

            [[228,26,28],[55,126,184],[77,175,74],[152,78,163],[255,127,0],[255,255,51],[166,86,40],
                [247,129,191],[153,153,153]],

            [[179,226,205],[253,205,172],[203,213,232],[244,202,228],[230,245,201],[255,242,174],
                [241,226,204],[204,204,204]],

            [[102,194,165],[252,194,165],[141,160,203],[231,138,195],[166,216,84],[255,217,47],
                [229,196,148],[179,179,179]],

            [[27,158,119],[217,95,2],[117,112,179],[231,41,138],[102,166,30],[230,171,2],
                [166,118,29],[102,102,102]],

            [[127,201,127],[190,174,212],[253,192,134],[255,255,153],[56,108,176],[240,2,127],
                [191,91,23],[102,102,102]]
         ];

         var palettes = [];
         Ext.each(palettesRaw, function(palette, index) {
             palettes[index] = [];
             palettes[index].push(index);
             var colors = [];
             Ext.each(palette, function(color) {
                 colors.push('rgb(' + color.join(',') + ')');
             });
             palettes[index].push(colors);

         });
         this.dsPalette.loadData(palettes);
    },

    /*
     * Method: displayTypeContext
     * Displays or hides widgets depending on the selected classification type
     * Parameters:
     * classificationType - {String} Can be choropleths, prop_symbols, or unique_values
     */
    displayTypeContext: function(classificationType) {
        var cmp = Ext.getCmp('_classif_panel_card');
        if (!cmp) {
            return;
        }
        if (classificationType === 'choropleths') {
            cmp.layout.setActiveItem(0);
        } else if (classificationType === 'prop_symbols') {
            cmp.layout.setActiveItem(1);
        } else if (classificationType === 'unique_values') {
            cmp.layout.setActiveItem(2);
        }
        cmp.doLayout();
    },

    /*
     * Method: on_sbMinSize_changed
     * Valid rule between the 2 SpinBoxes: minimum size mustn't be ever
     * greater or equal than the maximum size
     * Parameters:
     * spinBox - {Ext.ux.NumberSpinner}
     * newVal - (undefined because {Ext.form.NumberField} lost them when override)
     * oldVal - (undefined because {Ext.form.NumberField} lost them when override)
     */
    on_sbMinSize_changed: function(spinBox, newVal, oldVal) {
        if (this.sbMinSize.getValue() >= this.sbMaxSize.getValue()) {
            spinBox.setValue(spinBox.getValue() - 1);
        }
    },

    /*
     * Method: on_sbMaxSize_changed
     * Valid rule between the 2 SpinBoxes: minimum size mustn't be ever
     * greater or equal than the maximum size
     * Parameters:
     * spinBox - {Ext.ux.NumberSpinner}
     * newVal - (undefined because {Ext.form.NumberField} lost them when override)
     * oldVal - (undefined because {Ext.form.NumberField} lost them when override)
     */
    on_sbMaxSize_changed: function(spinBox, newVal, oldVal) {
        if (this.sbMinSize.getValue() >= this.sbMaxSize.getValue()) {
            spinBox.setValue(spinBox.getValue() + 1);
        }
    },

    /*
     * Method: on_cbAttribute_selected
     * Triggered when cbAttribute is selected
     * Parameters:
     * combo - {Ext.form.ComboBox}
     * record - {Ext.data.Record} contains the type
     * index - {integer}
     */
     on_cbAttribute_selected: function(combo, record, index) {
        this.filldsClassifType(record.get("type"));
     },

    /*
     * Method: on_cbClassifType_selected
     * Triggered when cbClassifType is selected
     * Parameters:
     * combo - {Ext.form.ComboBox}
     * record - {Ext.data.Record}
     * index - {integer}
     */
    on_cbClassifType_selected: function(combo, record, index) {
        this.displayTypeContext(combo.getValue());
    },

    /*
     * Method: getParams
     * Generate JSON request for the SLD Service.
     * Helped with the parametrizable parameters from the form and the selected layer
     * Returns: {Object} json request
     */
    getParams: function() {

        // common parameters
        var request = {
            wfs_url: GEOR.ows.getWFSCapURL(this.wfsInfo),
            layer_name: this.wfsInfo.get("typeName"),
            attribute_name: this.cbAttribute.getValue(),
            type: this.cbClassifType.getValue(),
            symbol_type: this.symbolType
        };

        // classification specific parameters
        var type = this.cbClassifType.getValue();
        if (type === 'choropleths') {
            Ext.apply(request, {
                class_count: this.sbClassCountChoropleth.getValue(),
                first_color: this.cpFirstColor.getValue(),
                last_color: this.cpLastColor.getValue()
            });
        } else if (type === 'prop_symbols') {
            Ext.apply(request, {
                class_count: this.sbClassCountPropSymbol.getValue(),
                min_size: this.sbMinSize.getValue(),
                max_size: this.sbMaxSize.getValue()
            });
        } else if (type === 'unique_values') {
            Ext.apply(request, {
                palette: this.cbPalette.getValue()
            });
        }

        return request;
    },

   /*
    * Method: on_btClassify_clicked
    * Launches the classification process. When done it triggers the callback function
    * by passing the sld relative url
    */
    on_btClassify_clicked: function(button, event) {
        var params = this.getParams();
        this.fireEvent("change", this, params);
    }

});

Ext.reg('geor.classifpanel', GEOR.ClassificationPanel);
