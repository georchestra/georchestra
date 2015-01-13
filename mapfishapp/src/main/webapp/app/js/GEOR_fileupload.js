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
 * @include OpenLayers/Format/JSON.js
 * @include OpenLayers/Format/GeoJSON.js
 * @include OpenLayers/Layer/Vector.js
 * @include GeoExt/data/LayerRecord.js
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

    var MULTI_FILES_FORMATS = ['shp', 'mif', 'tab'];

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

    /**
     * Method: getFileName
     *
     * Parameters:
     * fieldValue - {String} something like "C:\fakepath\file.zip"
     *
     * Returns:
     * {String} the file name (eg: file.zip)
     */
    var getFileName = function(fieldValue) {
        //http://meta.stackoverflow.com/questions/68471/the-image-uploader-shows-fakepath-as-path-when-using-chrome
        var cmpts = fieldValue.split('\\');
        if (cmpts.length) {
            return cmpts[cmpts.length-1];
        } else {
            return "geofile";
        }
    };

    /**
     * Method: loadPanel
     *
     * Parameters:
     * p - {Object|Ext.Panel}
     */
    var loadPanel = function(p) {
        centerPanel.removeAll();
        centerPanel.add(p);
        centerPanel.doLayout();
    };

    /**
     * Method: errorAndReset
     *
     * Parameters:
     * form - {Ext.form.BasicForm}
     * err - {String}
     */
    var errorAndReset = function(form, err) {
        centerPanel.el.unmask();
        alert(OpenLayers.i18n("server upload error: ERROR", {'ERROR': err}));
        form.reset();
        newFile = true;
    };

    /**
     * Method: formSuccess
     *
     * Parameters:
     * form - {Ext.form.BasicForm}
     * action - {Ext.form.Action}
     */
    var formSuccess = function(form, action) {
        centerPanel.el.unmask();
        var features,
            fc = (new OpenLayers.Format.JSON()).read(action.response.responseText);
        if (!fc) {
            errorAndReset(form, OpenLayers.i18n("Incorrect server response."));
            return;
        } else if (fc.success !== "true") {
            errorAndReset(form, OpenLayers.i18n(fc.error));
            return;
        }
        features = (new OpenLayers.Format.GeoJSON()).read(fc.geojson);
        if (!features || features.length == 0) {
            errorAndReset(form, OpenLayers.i18n("No features found."));
            return;
        }
        model = new GEOR.FeatureDataModel({
            features: features
        });
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
            frame: false,
            border: false
        });

        loadPanel(gridPanel);
        var fieldValue = form.items.get(0).getValue();
        form.reset();
        newFile = true;

        var recordType = GeoExt.data.LayerRecord.create(
            GEOR.ows.getRecordFields()
        );

        var name = GEOR.util.shortenLayerName(getFileName(fieldValue)),
            layer = new OpenLayers.Layer.Vector(name, {
                styleMap: GEOR.util.getStyleMap(),
                rendererOptions: {
                    zIndexing: true
                }
            });

        layer.addFeatures(features);

        observable.fireEvent("selectionchanged", [new recordType({
            layer: layer
        }, layer.id)]);
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
            var srs = options.srs, msg;
            delete options.srs;

            if (!centerPanel) {
                centerPanel = new Ext.Panel({
                    flex: 1,
                    layout: 'fit',
                    defaults: {border: false},
                    items: [{
                        html: ''
                    }]
                });
            }

            if (!GEOR.config.FILE_FORMAT_LIST || 
                GEOR.config.FILE_FORMAT_LIST.length === 0) {

                msg = tr("The service is unavailable.");

            } else {

                var supportedMulti = [];
                Ext.each(MULTI_FILES_FORMATS, function(f) {
                    if (OpenLayers.Util.indexOf(GEOR.config.FILE_FORMAT_LIST, f) > -1) {
                        supportedMulti.push(f);
                    }
                });
                var last = supportedMulti.pop().toUpperCase();
                msg = [
                    '<b>', tr("Upload a vector data file."), '</b><br/><br/>',
                    tr("The allowed formats are the following: "),
                    GEOR.config.FILE_FORMAT_LIST.join(", ").toUpperCase(), '. (',
                    tr("2D only"),') <br/>',
                    tr("Use ZIP compression for multifiles formats, such as"),
                    " ", supportedMulti.join(', ').toUpperCase(), " ", tr("or"), " ", last
                ].join('');

            }

            return new Ext.Panel(Ext.apply({
                title: tr("Local file"),
                layout: 'vbox',
                layoutConfig: {
                    align: 'stretch'
                },
                defaults: {border: false},
                items: [{
                    xtype: 'box',
                    height: 75,
                    autoEl: {
                        tag: 'div',
                        cls: 'box-as-panel',
                        html: msg
                    }
                }, {
                    xtype: 'form',
                    region: "north",
                    fileUpload: true,
                    bodyStyle: 'padding:10px',
                    labelWidth: 60,
                    height: 50,
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
                                centerPanel.el.mask(OpenLayers.i18n("Loading..."));
                                form.submit({
                                    url: GEOR.config.PATHNAME + "/ws/togeojson/",
                                    success: formSuccess,
                                    failure: function(form, action) {
                                        errorAndReset(form, OpenLayers.i18n(action.result.error));
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
            loadPanel({
                html: ''
            });
            observable.fireEvent("selectionchanged", []);
        }
    };
})();
