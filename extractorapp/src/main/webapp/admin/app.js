/*
 * Copyright (C) 2009 by the geOrchestra PSC
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
        type: 'string' // WAITING/RUNNING/PAUSED/CANCELLED/COMPLETED
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
        model: 'Task',
        sorters: { property: 'priority', direction : 'DESC' },
        listeners: {
        	load : {
        		// put top priority (temporary) to running task
        		fn : function(s) {
        			var i = s.find("status","RUNNING");
                    if(i >= 0) {
                    	s.getAt(i).set("priority", 3);
                    	s.sort();
                    }
        		}
        	}
        }
    });

    var grid = Ext.create('Ext.grid.Panel', {
        renderTo: Ext.Element.get('extraction_table'),
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
            dataIndex: 'priority',
            renderer: function(v) {
                switch (v) {
                    case 0:
                        return '<img src="images/gear--minus.png" alt="priorité basse"></img>';
                        break;
                    case 1:
                        return '<img src="images/gear.png" alt="priorité standard"></img>';
                        break;
                    case 2:
                        return '<img src="images/gear--plus.png" alt="priorité haute"></img>';
                        break;
                    case 3:
                        return '<img src="images/gear--plus.png" alt="priorité haute"></img>';
                        break;
                    default:
                        return 'should not happen';
                }
            }
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
                                //alert('Oops, problème serveur');
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
                                //alert('Oops, problème serveur');
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
                                //alert('Oops, problème serveur');
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
                                //alert('Oops, problème serveur');
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
                b.setDisabled(status === 'RUNNING' || status === 'CANCELLED' || status === 'COMPLETED');
            }
        });
    });

    Ext.create('Ext.form.Panel', {
        renderTo: Ext.Element.get('full_stats_download_form'),
        width: 300,
        bodyPadding: 10,
        title: 'Téléchargement des statistiques détaillées',
        standardSubmit: true,
        items: [{
            xtype: 'datefield',
            anchor: '100%',
            fieldLabel: 'Date de début',
            name: 'startDate',
            format: 'Y-m-d',
            value: Ext.Date.add(new Date(), Ext.Date.YEAR, -1)
        }, {
            xtype: 'datefield',
            anchor: '100%',
            fieldLabel: 'Date de fin',
            name: 'endDate',
            format: 'Y-m-d',
            value: new Date(),
            maxValue: new Date()
        }],
        buttons: [{
            text: 'OK',
            handler: function(){
                var v = this.up('form').getForm().getValues();
                window.open(
                  '/analytics/ws/fullLayersExtraction.csv?startDate=' + v.startDate + '&endDate=' + v.endDate
                );
            }
        }]
    });

});
