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
         *
         * Returns:
         * {Ext.Panel}
         */
        getPanel: function(options) {
            var store = new GEOR.ows.WFSCapabilities();
            // when we use geoext r>2697 
            // see http://trac.geoext.org/ticket/412 
            // in order to use a custom strategy for vector layers (rather than fixed)

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
                    msg: "Chargement ..."
                },
                columns: [
                    cbxSm,
                    {header: "Couche", dataIndex: "title", sortable: true, width: 200},
                    {id: "description", header: "Description", dataIndex: "abstract"}
                ],
                sm: cbxSm,
                enableHdMenu: false,
                autoExpandColumn: "description"
            });

            var comboField = new Ext.form.ComboBox({
                editable: false,
                triggerAction: 'all',
                // TODO: prevent addition of "?query=" to requested URL
                //queryParam: null, // does not work 
                height: 30,
                fieldLabel: "Choisissez un serveur WFS",
                loadingText: 'Chargement...',
                mode: 'remote',
                store: new Ext.data.Store({
                    proxy : new Ext.data.HttpProxy({
                        method: 'GET',
                        disableCaching: false,
                        url: GEOR.config.OWS_LIST_URL
                    }),
                    reader: new Ext.data.JsonReader({
                        root: 'wfs_servers',
                        fields: ['name', 'url']
                    }),
                    sortInfo: {
                        field: 'name',
                        direction: 'ASC'
                    }
                }),
                listeners: {
                    "select": function(cmb, rec, idx) {
                        urlField.setValue(rec.get('url'));
                        urlField.onTrigger2Click();
                    }
                },
                valueField: 'url',
                displayField: 'name',
                tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
            });
            
            var urlField = new Ext.app.OWSUrlField({
                fieldLabel: "... ou saisissez son adresse",
                store: store,
                callback: function(r, options, success) {
                    if (!success) {
                        GEOR.util.errorDialog({
                            msg: "Serveur non joignable"
                        });
                        return;
                    }
                    
                    //console.log(store); // available fields : layer, title, name, namespace, abstract
                },
                height: 30,
                width: 400
            });

            return new Ext.Panel({
                title: 'Serveurs WFS',
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
            });
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