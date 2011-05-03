Ext.onReady(function(){
    Ext.QuickTips.init();

    /**
     * Constant: LAYER_RULES_URL
     * {String} URL to the "layers" service.
     */
//    var LAYER_RULES_URL = 'tests/layer_rules.json';
    var LAYER_RULES_URL = '/gssec/ws/rules/layers';

    /**
     * Constant: LAYERS_URL
     * {String} URL to the "layers" service.
     */
//    var LAYERS_URL = 'tests/layers.json';
   var LAYERS_URL = '/gssec/ws/layers';

    /**
     * Constant: ROLES_URL
     * {String} URL to the "roles" service.
     */
//    var ROLES_URL = 'tests/roles.json';
    var ROLES_URL = '/gssec/ws/roles';

    /**
     * Constant: REGIONS_URL
     * {String} URL to the "regions" service.
     */
//    var REGIONS_URL = 'tests/regions.json';
    var REGIONS_URL = '/gssec/ws/regions';

    /**
     * Property: genId
     * {Number} The data generation identifier.
     */
    var genId = null;

    /**
     * Property: currentRegion
     * {String} The name of the selected region.
     */
    var currentRegion = null;

    /**
     * Property: LayerRule
     * {Ext.data.Record} The record type of "layer rule" records.
     */
    var LayerRule = Ext.data.Record.create([{
        name: 'name',
        type: 'string'
    }, {
        name: 'permission',
        type: 'string'
    },{
        name: 'role',
        type: 'string'
    }]);

    /**
     * Method: pull
     * Pull "layer rule" records from the "layer rules" web service.
     * 
     * Parameters:
     * store - {Ext.data.Store} The store to insert layer rule records
     *                          into.
     */
    function pull(store) {
        Ext.Ajax.request({
            url: LAYER_RULES_URL + '?region=' + currentRegion,
            method: 'GET',
            callback: function(o, s, r) {
                if(r.status === 200) {
                    var data = Ext.util.JSON.decode(r.responseText);
                    genId = data["gen_id"];
                    store.loadData(data);
                    // these records come from the server, they aren't
                    // phantoms
                    store.each(function(r) {
                        r.phantom = false;
                    });
                } else {
                    Ext.Msg.alert(
                        'Erreur',
                        'La requête a échoué avec le code d\'erreur ' + r.status + '.'
                    );
                }
            }
        });
    }

    /**
     * Method: push
     * Push the records to the "layer rules" web service.
     *
     * Parameters:
     * store - {Ext.data.Store} The store.
     */
    function push(store) {
        var layers = [];
        store.each(function(r) {
            layers.push({
                'name': r.get('name'),
                'permission': r.get('permission'),
                'role': r.get('role')
            });
        });
        Ext.Ajax.request({
            url: LAYER_RULES_URL+"?region="+currentRegion,
            method: 'POST',
            jsonData: Ext.util.JSON.encode({'rules': layers, 'gen_id': genId}),
            callback: function(o, s, r) {
                var st = r.status;
                if(st < 200 || st >= 300) {
                    var title, msg;
                    if(st === 409) {
                        title = 'Conflit';
                        msg = 'Les données ont été modifiées par un autre utilisateur, ' +
                              'cette édition ne peut pas être conservée.';
                    } else {
                        title = 'Erreur';
                        msg = 'La requête a échoué avec le code d\'erreur ' + st + '.';
                    }
                    Ext.Msg.alert(title, msg, function() { pull(store); });
                }
            }
        });
    }

    // Main

    // for DEBUG
    /*
    Ext.Ajax.defaultHeaders = {
        'Sec-Roles': 'ROLE_GS_ADMIN_District1,ROLE_GS_ADMIN_District2'
    };
    */

    // create the "layers" store
    var store = new Ext.data.GroupingStore({
        reader: new Ext.data.JsonReader({
            fields: LayerRule,
            root: 'rules'
        }),
        listeners: {
            'remove': function(s) {
                push(s);
            }
        }
    });

    // create the row editor plugin
    var editor = new Ext.ux.grid.RowEditor({
        saveText: 'Sauver',
        cancelText: 'Annuler',
        errorText: 'Erreurs',
        listeners: {
            'afteredit': function(e) {
                push(e.grid.getStore());
            },
            'canceledit': function(e) {
                if(e.record.phantom) {
                    e.grid.getStore().remove(e.record);
                }
            }
        }
    });

    // create the grid, rendering it in the page body
    var grid = new Ext.grid.GridPanel({
        renderTo: "layer_rules",
        width: 550,
        height: 500,
        store: store,
        margins: '0 5 5 5',
        plugins: [editor],
        view: new Ext.grid.GroupingView({
            markDirty: false
        }),
        tbar: [{
            ref: '../addBtn',
            iconCls: 'icon-user-add',
            text: 'Ajouter',
            disabled: true,
            handler: function(){
                var e = new LayerRule({
                    name: '',
                    permission: 'read',
                    role: ''
                });
                editor.stopEditing();
                store.insert(0, e);
                grid.getView().refresh();
                grid.getSelectionModel().selectRow(0);
                editor.startEditing(0);
            }
        }, {
            ref: '../removeBtn',
            iconCls: 'icon-user-delete',
            text: 'Supprimer',
            disabled: true,
            handler: function(){
                editor.stopEditing();
                var s = grid.getSelectionModel().getSelections();
                for(var i = 0, r; r = s[i]; i++){
                    store.remove(r);
                }
            }
        }, {
            ref: '../copyBtn',
            iconCls: 'icon-user-copy',
            text: 'Copier',
            disabled: true,
            handler: function() {
                var records = grid.getSelectionModel().getSelections();
                if(records.length === 1) {
                    var r = records[0], store = r.store, idx = store.indexOf(r) + 1;
                    var copy = r.copy();
                    copy.id = Ext.data.Record.id(copy);
                    editor.stopEditing();
                    store.insert(idx, [copy]);
                    grid.getView().refresh();
                    grid.getSelectionModel().selectRow(idx);
                    editor.startEditing(idx);
                }
            }
        }, '->', {
            xtype: 'label',
            html: 'Région :',
            cls: 'label-region'
        }, {
            xtype: 'combo',
            store: new Ext.data.Store({
                reader: new Ext.data.JsonReader({
                    root: 'regions',
                    fields: [{name: 'cn', type: 'string'}]
                }),
                proxy: new Ext.data.HttpProxy({
                    url: REGIONS_URL,
                    method: 'GET'
                }),
                autoDestroy: true
            }),
            displayField: 'cn',
            valueField: 'cn',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            editable: false,
            listeners: {
                'select': function(c, r, i) {
                    currentRegion = r.get('cn');
                    pull(store);
                    grid.addBtn.setDisabled(false);
                }
            }
        }],
        columns: [
        new Ext.grid.RowNumberer(),
        {
            header: 'Nom',
            dataIndex: 'name',
            width: 200,
            sortable: true,
            editor: {
                xtype: 'combo',
                store: new Ext.data.Store({
                    reader: new Ext.data.JsonReader({
                        root: 'layers',
                        fields: [{name: 'cn', type: 'string'}]
                    }),
                    proxy: new Ext.data.HttpProxy({
                        url: LAYERS_URL,
                        method: 'GET'
                    }),
                    autoDestroy: true,
                    listeners: {
                        beforeload: function (store, opts) {
            				store.baseParams.region = currentRegion;
            			}
            		}
                }),
                displayField: 'cn',
                valueField: 'cn',
                mode: 'remote',
                forceSelection: true,
                triggerAction: 'all',
                selectOnFocus: true,
                allowBlank: false
            }
        }, {
            header: 'Permission',
            dataIndex: 'permission',
            width: 100,
            sortable: true,
            editor: {
                xtype: 'combo',
                store: new Ext.data.ArrayStore({
                    fields: ['permission'],
                    data: [['read'], ['write']]
                }),
                displayField: 'permission',
                valueField: 'permission',
                mode: 'local',
                forceSelection: true,
                triggerAction: 'all',
                selectOnFocus: true,
                allowBlank: false
            }
        }, {
            header: 'Rôle',
            dataIndex: 'role',
            width: 200,
            sortable: true,
            editor: {
                xtype: 'combo',
                store: new Ext.data.Store({
                    reader: new Ext.data.JsonReader({
                        root: 'roles',
                        fields: [{name: 'cn', type: 'string'}]
                    }),
                    proxy: new Ext.data.HttpProxy({
                        url: ROLES_URL,
                        method: 'GET'
                    }),
                    autoDestroy: true
                }),
                displayField: 'cn',
                valueField: 'cn',
                mode: 'remote',
                forceSelection: true,
                triggerAction: 'all',
                selectOnFocus: true,
                allowBlank: false
            }
        }]
    });

    // register a selectionchange listener to disable the remove
    // button when necessary
    grid.getSelectionModel().on('selectionchange', function(sm){
        grid.removeBtn.setDisabled(sm.getCount() < 1);
        grid.copyBtn.setDisabled(sm.getCount() !== 1);
    });
});
