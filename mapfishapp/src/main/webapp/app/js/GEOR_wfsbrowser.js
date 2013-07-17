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
 * @include GEOR_ows.js
 * @include GEOR_util.js
 * @include GEOR_config.js
 * @include OpenLayers/Strategy/BBOX.js
 */

Ext.namespace("GEOR");

GEOR.wfsbrowser = (function() {

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
     * Property: cbxSm
     * {Ext.grid.CheckboxSelectionModel} the selection model
     */
    var cbxSm = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

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
            var srs = options.srs;
            delete options.srs;
            tr = OpenLayers.i18n;

            var store = new GEOR.ows.WFSCapabilities({
                storeOptions: {
                    // url should not be empty unless we want the following
                    // exception to occur:
                    // "uncaught exception: Ext.data.DataProxy: DataProxy attempted
                    // to execute an API-action but found an undefined url /
                    // function. Please review your Proxy url/api-configuration."
                    url: "/dummy",
                    sortInfo: {
                        field: 'title',
                        direction: 'ASC'
                    },
                    layerOptions: function() {
                        return {
                            visibility: true,
                            displayInLayerSwitcher: true,
                            styleMap: GEOR.util.getStyleMap(),
                            rendererOptions: {
                                zIndexing: true
                            },
                            strategies: [
                                new OpenLayers.Strategy.Fixed()
                            ]
                            //,renderers: ["Canvas"] // in order to load many features
                        };
                    },
                    protocolOptions: {
                        //autoDestroy: false, // TEST (seems not to work as expected)

                        // we need to set the srsName in the WFS query,
                        // so that features are returned in the correct SRS.
                        // Please note that, with WFS 1.0.0, the trick should
                        // only work with GeoServer:
                        srsNameInQuery: true,
                        srsName: srs
                        // Note: the geometry name will be set later on:
                        // See http://applis-bretagne.fr/redmine/issues/2145
                        // and describeFeaturetypeSuccess() in GEOR_layerfinder.js

                        // TODO: MapServer >= 5.6 requires that all propertyNames
                        // are listed here, if we want to get the geometry.
                        // This requires that we do a WFS DescribeFeatureType
                        // and amend the protocol once we get the response.
                        // see http://applis-bretagne.fr/redmine/issues/1996

                        // I think this will be done as a consequence of
                        // http://applis-bretagne.fr/redmine/issues/1984 :
                        // geometryName and propertyNames should be cached
                        // in the layerStore for future use, after GetCap &
                        // DescribeFeatureType responses are parsed.
                    }
                }
            });

            cbxSm =  new Ext.grid.CheckboxSelectionModel({
                width: 20,
                // for check all/none behaviour:
                header: '<div class="x-grid3-hd-checker">&#160;</div>',
                listeners: {
                    "selectionchange": function(sm) {
                        observable.fireEvent("selectionchanged", sm.getSelections());
                    }
                }
            });

            // create a grid to display records from the store
            var grid = new Ext.grid.GridPanel({
                region: 'center',
                border: false,
                store: store,
                loadMask: {
                    msg: tr("Loading...")
                },
                columns: [
                    cbxSm,
                    {header: tr("Layer"), dataIndex: "title", sortable: true, width: 200},
                    {id: "description", header: tr("Description"), dataIndex: "abstract"}
                ],
                sm: cbxSm,
                enableHdMenu: false,
                autoExpandColumn: "description"
            });

            var comboField = new Ext.form.ComboBox({
                editable: false,
                triggerAction: 'all',
                height: 30,
                width: 400,
                fieldLabel: tr("Choose a WFS server: "),
                loadingText: tr('Loading...'),
                mode: 'local',
                store: new Ext.data.Store({
                    data: GEOR.config.WFS_SERVERS,
                    reader: new Ext.data.JsonReader({
                        fields: ['name', 'url']
                    })
                }),
                listeners: {
                    "select": function(cmb, rec, idx) {
                        if (GEOR.config.DISPLAY_SELECTED_OWS_URL) {
                            urlField.setValue(rec.get('url'));
                        }
                        urlField.onTrigger2Click(rec.get('url'));
                    }
                },
                valueField: 'url',
                displayField: 'name',
                tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
            });

            var urlField = new Ext.app.OWSUrlField({
                fieldLabel: tr("... or enter its address: "),
                store: store,
                callback: function(r, options, success) {
                    if (!success) {
                        GEOR.util.errorDialog({
                            msg: "Unreachable server or insufficient rights"
                        });
                        return;
                    }
                    // available store fields : layer, title, name, namespace, abstract
                },
                height: 30,
                width: 400
            });

            return new Ext.Panel(Ext.apply({
                title: tr("WFS server"),
                layout: 'border',
                items: [
                    {
                        region: 'north',
                        layout: 'form',
                        border: false,
                        labelSeparator: '',
                        labelWidth: 170,
                        bodyStyle: 'padding: 5px;',
                        height: 65,
                        items: [comboField, urlField]
                    },
                    grid
                ]
            }, options));
        },

        /**
         * APIMethod: clearSelection
         * Clears the current selection
         */
        clearSelection: function() {
            cbxSm.clearSelections();
        }
    };
})();
