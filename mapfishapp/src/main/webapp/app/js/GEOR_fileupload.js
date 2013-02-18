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
 * @include GEOR_util.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.fileupload = (function() {

    /*
     * Private
     */

    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: selectionchanged
         * Fires when the selection has changed
         *
         * Listener arguments:
         * records - {Array} array of selected records
         */
        "selectionchanged"
    );

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: newFile
     * {Boolean}
     */
    var newFile = true;

    /**
     * Property: store
     * {Boolean}
     */
    var store;

    /**
     * Property: centerPanel
     * {Ext.Panel}
     */
    var centerPanel;

    var formSuccess = function(form, action) {
        var features,
            fc = (new OpenLayers.Format.JSON()).read(action.response.responseText);
        if (!fc) {
            alert("Incorrect server response");
            return;
        }
        features = (new OpenLayers.Format.GeoJSON()).read(fc.geojson);
        model = new GEOR.FeatureDataModel({
            features: features
        });
        if (!features || features.length == 0) {
            alert("No features found");
            return;
        }
        store = new GeoExt.data.FeatureStore({
            features: features,
            fields: model.toStoreFields()
        });

        // create grid
        var columnModel = model.toColumnModel({
            sortable: true
        });

        var gridPanel = new Ext.grid.GridPanel({
            viewConfig: {
                // we add an horizontal scroll bar in case
                // there are too many attributes to display:
                forceFit: (columnModel.length < 10)
            },
            store: store,
            columns: columnModel,
            /*
            sm: new GeoExt.grid.FeatureSelectionModel({
                singleSelect: false,
                selectControl: sfControl
            }),
            */
            frame: false,
            border: false
        });
        // insert grid in centerPanel

        centerPanel.removeAll();
        centerPanel.add(gridPanel);
        centerPanel.doLayout();
        form.reset();
        newFile = true;
    };

    /*
     * Public
     */
    return {
        /*
         * Observable object
         */
        events: observable,

        /**
         * APIMethod: getPanel
         * Return the panel for the WMS browser
         *
         * Parameters:
         * options - {Object} Hash with key: srs (the map srs).
         * The other options will be applied to panel
         *
         * Returns:
         * {Ext.Panel}
         */
        getPanel: function(options) {
            tr = OpenLayers.i18n;
            var srs = options.srs;
            delete options.srs;

            if (!store) {
                store = new GeoExt.data.FeatureStore();
                centerPanel = new Ext.Panel({
                    flex: 1,
                    layout: 'fit',
                    items: [{
                        html: 'feature grid in here'
                    }]
                });
            }

            return new Ext.Panel(Ext.apply({
                title: tr("Local file"),
                layout: 'vbox',
                layoutConfig: {
                    align: 'stretch'
                },
                defaults: {border: false},
                items: [{
                    xtype: 'form',
                    region: "north",
                    fileUpload: true,
                    bodyStyle: 'padding:10px',
                    labelWidth: 100,
                    height: 80,
                    monitorValid: true,
                    buttonAlign: 'right',
                    items: [{
                        xtype: 'textfield',
                        inputType: 'file',
                        name: 'geofile',
                        labelSeparator: tr("labelSeparator"),
                        fieldLabel: tr("File"),
                        allowBlank: false,
                        blankText: tr("The file is required.")
                    }, {
                        xtype: 'hidden',
                        name: 'srs',
                        value: srs
                    }],
                    listeners: {
                        "clientvalidation": function(fp, isValid) {
                            var form = fp.getForm();
                            if (isValid && newFile) {
                                newFile = false;
                                form.submit({
                                    url: "ws/togeojson/",
                                    // Beware: form submission requires a *success* parameter in json response
                                    // As said in http://extjs.com/learn/Manual:RESTful_Web_Services
                                    // "Ext.form.BasicForm hopefully becomes HTTP Status Code aware!"
                                    success: formSuccess,
                                    failure: function(form, action) {
                                        alert("Error : " + action.result.msg);
                                        form.reset();
                                        newFile = true;
                                    },
                                    scope: this
                                });
                            }
                        }
                    }
                }, centerPanel]
            }, options));
        },

        /**
         * APIMethod: clearSelection
         * Clears the current selection
         */
        clearSelection: function() {
            //cbxSm.clearSelections();
        }
    };
})();
