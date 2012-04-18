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

Ext.namespace("GEOR");

GEOR.wmsbrowser = (function() {

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
            var store = new GEOR.ows.WMSCapabilities({
                storeOptions: {
                    // url should not be empty unless we want the following
                    // exception to occur:
                    // uncaught exception: Ext.data.DataProxy: DataProxy attempted
                    // to execute an API-action but found an undefined url /
                    // function. Please review your Proxy url/api-configuration.
                    url: "/dummy"
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

            var r = function(val) {
                return (val ? '<img src="app/img/famfamfam/tick.gif" alt="oui">' :
                    '<img src="app/img/famfamfam/cross.gif" alt="non">');
            };

            // create a grid to display records from the store
            var grid = new Ext.grid.GridPanel({
                region: 'center',
                border: false,
                store: store,
                loadMask: {
                    msg: "Chargement ..."
                },
                columns: [
                    cbxSm,
                    {header: "Couche", dataIndex: "title", sortable: true, width: 200},
                    {id: "queryable", header: "Interrogeable", dataIndex: "queryable", sortable: true, width: 75, renderer: r},
                    {id: "opaque", header: "Opaque", dataIndex: "opaque", sortable: true, width: 50, renderer: r},
                    {id: "description", header: "Description", dataIndex: "abstract"}
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
                fieldLabel: "Choisissez un serveur WMS",
                loadingText: 'Chargement...',
                mode: 'local',
                store: new Ext.data.Store({
                    data: GEOR.config.WMS_SERVERS,
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
                fieldLabel: "... ou saisissez son adresse",
                callback: function(r, options, success) {
                    if (!success) {
                        GEOR.util.errorDialog({
                            msg: "Serveur non joignable ou droits insuffisants"
                        });
                        return;
                    }
                    var t = store.getCount();
                    // but we don't want to display layers
                    // which cannot be served in map's native SRS
                    store.filterBy(function(record, id) {
                        return record.get('srs') && 
                            (record.get('srs')[srs] === true);
                    });
                    var notDisplayed = t - store.getCount();
                    if (notDisplayed > 0) {
                        var plural = (notDisplayed > 1) ? 's' : '';
                        GEOR.util.infoDialog({
                           msg: "Le serveur publie "+notDisplayed+
                            " couche"+plural+" dont la projection n'est pas compatible"
                        });
                    }
                },
                store: store,
                height: 30,
                width: 400
            });

            return new Ext.Panel(Ext.apply({
                title: 'Serveur WMS',
                layout: 'border',
                items: [
                    {
                        region: 'north',
                        layout: 'form',
                        border: false,
                        labelSeparator: ' : ',
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
