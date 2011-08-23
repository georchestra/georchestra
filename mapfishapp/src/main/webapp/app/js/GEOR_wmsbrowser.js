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
         *
         * Returns:
         * {Ext.Panel}
         */
        getPanel: function(options) {
            var store = new GEOR.ows.WMSCapabilities();

            /**
             * Property: cbxSm
             * {Ext.grid.CheckboxSelectionModel} The selection model
             */
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
                // TODO: prevent addition of "?query=" to requested URL
                //queryParam: null, // does not work 
                height: 30,
                fieldLabel: "Choisissez un serveur WMS",
                loadingText: 'Chargement...',
                mode: 'remote',
                store: new Ext.data.Store({
                    proxy : new Ext.data.HttpProxy({
                        method: 'GET',
                        disableCaching: false,
                        url: GEOR.config.WMS_LIST_URL
                    }),
                    reader: new Ext.data.JsonReader({
                        root: 'servers',
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
            
            var urlField = new Ext.app.WMSUrlField({
                fieldLabel: "... ou saisissez son adresse",
                srs: options.srs,
                store: store,
                height: 30,
                width: 400
            });

            return new Ext.Panel({
                title: 'Serveurs WMS',
                layout: 'border',
                items: [
                    {
                        region: 'north',
                        layout: 'form',
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


/**
 * A customized TwinTriggerField, currently used in keyword xlink search
 * Taken from the Extjs examples and adapted (translations)
 */

Ext.app.WMSUrlField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function() {
        Ext.app.WMSUrlField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e) {
            if (e.getKey() == e.ENTER) {
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent: false,
    validateOnBlur: false,
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-search-trigger',
    hideTrigger1: true,
    width: 180,
    hasSearch: false,
    paramName: 'query',

    onTrigger1Click: function() {
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
            // conf
            var conf = Ext.get('conf');
            if (conf) {
                conf.enableDisplayMode().show();
            }
        }
    },

    onTrigger2Click: function() {
        // trim raw value:
        var url = this.getRawValue().replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (url.length < 1) {
            this.onTrigger1Click();
            return;
        }
        if (!GEOR.util.isUrl(url)) {
            GEOR.util.errorDialog({
                msg: "URL non conforme."
            });
            return;
        }
        var srs = this.srs; // FIXME : this is probably not the way it should be done.
        // update url for WMSCap request
        this.store.proxy.conn.url = url;
        this.store.load({
            callback: function(r, options, success) {
                if (success) {
                    var store = this.store;
                    var t = store.getCount();
                    // but we don't want to display layers
                    // which cannot be served in map's native SRS
                    store.filterBy(function(record, id) {
                        return record.get('srs') && (record.get('srs')[srs] === true);
                    });
                    var notDisplayed = t - store.getCount();
                    if (notDisplayed > 0) {
                        var plural = (notDisplayed > 1) ? 's' : '';
                        GEOR.util.infoDialog({
                           msg: "Le serveur publie "+notDisplayed+
                            " couche"+plural+" dont la projection n'est pas compatible"
                        });
                    }
                }
            },
            scope: this,
            add: false
        });

        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
        // conf
        var conf = Ext.get('conf');
        if (conf) {
            conf.enableDisplayMode().hide();
        }
    }
});
