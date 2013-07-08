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
 */

// TODO: create a class for this panel (already repeated 3 times: wms, wfs, wmts)

Ext.namespace("GEOR");

GEOR.wmtsbrowser = (function() {

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
            tr = OpenLayers.i18n;

            var store = new GEOR.ows.WMTSCapabilities({
                storeOptions: {
                    // url should not be empty unless we want the following
                    // exception to occur:
                    // uncaught exception: Ext.data.DataProxy: DataProxy attempted
                    // to execute an API-action but found an undefined url /
                    // function. Please review your Proxy url/api-configuration.
                    url: "/dummy",
                    sortInfo: {
                        field: 'title',
                        direction: 'ASC'
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
                fieldLabel: tr("Choose a WMTS server: "),
                loadingText: tr("Loading..."),
                mode: 'local',
                store: new Ext.data.Store({
                    data: [{ // TODO: GEOR.config option for this
                        name: "geopicardie",
                        url: "http://www.geopicardie.fr/geoserver/gwc/service/wmts"
                    },{
                        name: "geoportail",
                        url: "http://wxs.ign.fr/wnmz6nt68k09rw3f5vwaflk4/wmts/"
                    }],
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

            var srs = options.srs;
            delete options.srs;
            var urlField = new Ext.app.OWSUrlField({
                fieldLabel: tr("... or enter its address: "),
                callback: function(r, options, success) {
                    if (!success) {
                        GEOR.util.errorDialog({
                            msg: tr("Unreachable server or insufficient rights")
                        });
                        return;
                    }
                    var t = store.getCount();
                    // but we don't want to display layers
                    // which cannot be served in map's native SRS
                    
                    //FIXME
                    
                    /*
                    store.filterBy(function(record, id) {
                        return record.get('srs') &&
                            (record.get('srs')[srs] === true);
                    });
                    var notDisplayed = t - store.getCount();
                    if (notDisplayed > 0) {
                        var msg = (notDisplayed > 1) ?
                            tr("The server is publishing NB layers with an incompatible projection", {'NB': notDisplayed})
                            : tr("The server is publishing one layer with an incompatible projection");
                        GEOR.util.infoDialog({
                           msg: msg
                        });
                    }
                    */
                },
                store: store,
                height: 30,
                width: 400
            });

            return new Ext.Panel(Ext.apply({
                title: tr("WMTS server"),
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
