
/* 
TODO:
 - disable buttons on cancel pressed
 - FIX Uncaught TypeError: Cannot call method 'getId' of undefined
Ext.define.commitRecords ext-all-debug.js:18128
Ext.define.processResponse ext-all-debug.js:38483
(anonymous function) ext-all-debug.js:45977
Ext.apply.callback ext-all-debug.js:6208
Ext.define.onComplete ext-all-debug.js:36476
Ext.define.onStateChange ext-all-debug.js:36427
(anonymous function)

 - ideally, keep active selection on reload

*/

Ext.Loader.setConfig({
    enabled: true
});
Ext.Loader.setPath('Ext.ux', './ux');

Ext.require([
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.ux.RowExpander'
]);

Ext.define('Task', {
    extend: 'Ext.data.Model',
    idProperty: 'uuid',
    fields: [{
        name: 'uuid',
        type: 'string',
        useNull: true
    }, {
        name: 'requestor',
        type: 'string'
    }, {
        name: 'priority',
        defaultValue: 0,
        type: 'int' // signed integer
    }, {
        name: 'status',
        defaultValue: 'WAITING',
        type: 'string' // WAITING/RUNNING/PAUSED/CANCELLED/DONE
    }, {
        name: 'spec' // original json spec
    },{
        name: 'request_ts', // original request timestamp
        type: 'string' 
    },{
        name: 'begin_ts', // task starts being processed timestamp
        type: 'string' 
    },{
        name: 'end_ts', // task ends up timestamp
        type: 'string'
    }],
    proxy: {
        type: 'rest',
        url: '/extractorapp/extractor/tasks',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'tasks'
        }
    }
});

Ext.onReady(function(){
    
    var selectedRecord;

    var store = Ext.create('Ext.data.Store', {
        autoLoad: true,
        model: 'Task'
    });
    
    var grid = Ext.create('Ext.grid.Panel', {
        renderTo: document.body,
        plugins: [{
            ptype: 'rowexpander',
            rowBodyTpl: new Ext.XTemplate(
                '<p><b>Couches :</b> {[this.formatSpec(values.spec)]}</p>',
                '<p><b>Requête :</b> {request_ts}</p>',
                '<p><b>Début de traitement :</b> {begin_ts}</p>',
                '<p><b>Fin de traitement :</b> {end_ts}</p>', 
            {
                formatSpec: function(spec) {
                    var out = [];
                    Ext.each(spec.layers, function(l) {
                        out.push(l.layerName+" en "+
                            (l.projection || spec.globalProperties.projection));
                    });
                    return out.join(', ');
                }
            })
        }],
        width: 800,
        height: 400,
        frame: true,
        store: store,
        sortableColumns: false,
        menuDisabled: true,
        allowDeselect: true,
        columns: [{
            text: 'ID',
            width: 230,
            sortable: true,
            dataIndex: 'uuid'
        }, {
            text: 'Demandeur',
            flex: 1,
            sortable: true,
            dataIndex: 'requestor'
        }, {
            header: 'Priorité',
            width: 80,
            sortable: true,
            dataIndex: 'priority'
            // TODO: need a renderer
        }, {
            text: 'Etat',
            width: 80,
            sortable: true,
            dataIndex: 'status'
        }],
        dockedItems: [{
            xtype: 'toolbar',
            itemId: 'tbar',
            items: [{
                text: 'Priorité',
                itemId: 'up',
                disabled: true,
                iconCls: 'icon-up',
                handler: function(){
                    var p = selectedRecord.get('priority');
                    if (p > 1) return;
                    selectedRecord.set('priority', p+1);
                    selectedRecord.save({
                        scope: this,
                        callback: function() {
                            if (arguments[2] === true) {
                                Ext.example.msg('Priorité du job modifiée', 
                                    'Le job '+selectedRecord.get('uuid')+
                                    ' est passé en priorité '+parseInt(p+1));
                            } else {
                                alert('Oops, problème serveur');
                            }
                            store.load();
                        }
                    });
                }
            },{
                text: 'Priorité',
                itemId: 'down',
                disabled: true,
                iconCls: 'icon-down',
                handler: function(){
                    var p = selectedRecord.get('priority');
                    if (p < 1) return;
                    selectedRecord.set('priority', p-1);
                    selectedRecord.save({
                        scope: this,
                        callback: function() {
                            if (arguments[2] === true) {
                                Ext.example.msg('Priorité du job modifiée', 
                                    'Le job '+selectedRecord.get('uuid')+
                                    ' est passé en priorité '+parseInt(p-1));
                            } else {
                                alert('Oops, problème serveur');
                            }
                            store.load();
                        }
                    });
                }
            },{
                text: 'Pause',
                itemId: 'pause',
                disabled: true,
                iconCls: 'icon-pause',
                handler: function() {
                    selectedRecord.set('status', 
                        (selectedRecord.get('status') == 'PAUSED') ? 'WAITING' : 'PAUSED');
                    selectedRecord.save({
                        scope: this,
                        callback: function() {
                            if (arguments[2] === true) {
                                if (selectedRecord.get('status') == 'PAUSED') {
                                    Ext.example.msg('Job mis en pause', 
                                        'Le job '+selectedRecord.get('uuid')+
                                        ' a été mis en pause');
                                } else {
                                    Ext.example.msg('Job ajouté à la queue', 
                                        'Le job '+selectedRecord.get('uuid')+
                                        ' a été remis dans la queue');
                                }
                            } else {
                                alert('Oops, problème serveur');
                            }
                            store.load();
                        }
                    });
                }
            }, '->', {
                itemId: 'cancel',
                text: 'Annuler',
                iconCls: 'icon-cancel',
                disabled: true,
                handler: function() {
                    selectedRecord.set('status', 'CANCELLED');
                    selectedRecord.save({
                        scope: this,
                        callback: function() {
                            if (arguments[2] === true) {
                                Ext.example.msg('Job annulé', 
                                    'Le job '+selectedRecord.get('uuid')+
                                    ' a été annulé');
                            } else {
                                alert('Oops, problème serveur');
                            }
                            store.load();
                        }
                    });
                }
            }]
        }]
    });

    // CODE which polls the server every X seconds
    var sm = grid.getSelectionModel();
    
    window.setInterval(function() {
        // get selection (one item)
        // refresh store:
        store.load({
            callback: function() {
                // restore selection
                if (selectedRecord) {
                    sm.select(selectedRecord);
                }
                // FIXME: selection is not kept !!!
            }
        });
    }, 10000);
    
    // handle button activation:
    sm.on('selectionchange', function(sm, selections){
        selectedRecord = (selections.length) ? selections[0] : null;
        Ext.each(grid.query('.button'), function(b) {
            if (selections.length === 0) {
                b.setDisabled(true);
            } else {
                var status = selections[0].get('status');
                b.setDisabled(status === 'RUNNING' || status === 'DONE' || status === 'CANCELLED');
            }
        });
    });
    
});
