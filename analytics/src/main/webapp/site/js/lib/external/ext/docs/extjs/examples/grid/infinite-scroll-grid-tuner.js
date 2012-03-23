Ext.Loader.setConfig({enabled: true});

Ext.Loader.setPath('Ext.ux', '../ux/');

Ext.require([
    '*',
    'Ext.ux.ajax.JsonSimlet',
    'Ext.ux.ajax.SimManager'
]);

Ext.onReady(function() {

var remoteData = [];
for (var r = 0; r < 50*1000; r++) {
    var rowData = {};
    for (var f = 1; f < 11; f++) {
        rowData['f' + f] = 'row' + (r + 1) + '/col' + f;
    }
    remoteData.push(rowData);
}

    Ext.data.Store.prototype.prefetch = Ext.Function.createInterceptor(Ext.data.Store.prototype.prefetch, function(options) {
        logPanel.log('Prefetch rows ' + options.start + '-' + (options.start + options.limit));
    });

    Ext.data.Store.prototype.onProxyPrefetch = Ext.Function.createSequence(Ext.data.Store.prototype.onProxyPrefetch, function(operation) {
        logPanel.log('Prefetch returned ' + operation.start + '-' + (operation.start + operation.limit));
    });

    Ext.grid.PagingScroller.prototype.onViewRefresh = Ext.Function.createSequence(Ext.grid.PagingScroller.prototype.onViewRefresh, function() {
        var me = this,
            table = me.view.el.child('table', true);

        logPanel.log('Table moved to top: ' + table.style.top);
    });

    Ext.ux.ajax.SimManager.init({
        delay: 300
    }).register({'localAjaxSimulator': {
        stype: 'json',  // use JsonSimlet (stype is like xtype for components)
        data: remoteData
    }});

    Ext.define('Record', {
        extend: 'Ext.data.Model',
        fields: ['f1', 'f2', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10']
    });

    // create the Data Store
    var store = Ext.create('Ext.data.Store', {
        id: 'store',
        model: 'Record',
        remoteSort: true,
        // allow the grid to interact with the paging scroller by buffering
        buffered: true,
        proxy: {
            // load using script tags for cross domain, if the data in on the same domain as
            // this page, an HttpProxy would be better
            type: 'ajax',
            url: 'localAjaxSimulator',
            reader: {
                root: 'topics',
                totalProperty: 'totalCount'
            }
        }
    });

    var columns = [{
        xtype: 'rownumberer',
        width: 50,
        sortable: false
    }];
    for (var f = 1; f < 11; f++) {
        columns.push({
            text: 'F' + f,
            dataIndex: 'f' + f
        })
    }

    var grid = Ext.create('Ext.grid.Panel', {
        region: 'center',
        title: 'Random data (' + remoteData.length + ' records)',
        store: store,
        loadMask: true,
        selModel: {
            pruneRemoved: false
        },
        multiSelect: true,
        columns: columns
    });

    function makeLabel (ns, cls, name) {
        var docs = '../..';
        docs = '../../../.build/sdk'
        return '<a href="'+docs+'/docs/#!/api/'+ns+'.'+cls+'-cfg-'+name+'" target="docs">' + cls + ' ' + name + '</a>';
    }

    var logPanel = new Ext.Panel({
        title: 'Log',
        region: 'center',
        autoScroll: true,
        log: function(m) {
            logPanel.body.createChild({
                html: m
            });
            logPanel.body.dom.scrollTop = 1000000;
        },
        tbar: [{
            text: 'Clear',
            handler: function() {
                logPanel.body.update('');
            }
        }]
    });

    var controls = Ext.create('Ext.form.Panel', {
        region: 'north',
        split: true,
        height: 310,
        minHeight: 310,
        bodyPadding: 5,
        layout: 'form',
        defaults: {
            labelWidth: 150
        },
        items: [{
            xtype: 'numberfield',
            fieldLabel: 'Ajax latency (ms)',
            itemId: 'latency',
            value: 1000
        }, {
            xtype: 'numberfield',
            fieldLabel: 'Initial prefetch',
            itemId: 'primeSize',
            value: 150
        }, {
            xtype: 'numberfield',
            fieldLabel: 'Table size',
            itemId: 'tableSize',
            value: 100
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.data', 'Store', 'numFromEdge'),
            itemId: 'storeNumFromEdge',
            value: Ext.data.Store.prototype.numFromEdge
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.data', 'Store', 'trailingBufferZone'),
            itemId: 'storeTrailingBufferZone',
            value: Ext.data.Store.prototype.trailingBufferZone
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.data', 'Store', 'leadingBufferZone'),
            itemId: 'storeLeadingBufferZone',
            value: Ext.data.Store.prototype.leadingBufferZone
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.grid', 'PagingScroller', 'numFromEdge'),
            itemId: 'scrollerNumFromEdge',
            value: Ext.grid.PagingScroller.prototype.numFromEdge
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.grid', 'PagingScroller', 'trailingBufferZone'),
            itemId: 'scrollerTrailingBufferZone',
            value: Ext.grid.PagingScroller.prototype.trailingBufferZone
        }, {
            xtype: 'numberfield',
            fieldLabel: makeLabel('Ext.grid', 'PagingScroller', 'leadingBufferZone'),
            itemId: 'scrollerLeadingBufferZone',
            value: Ext.grid.PagingScroller.prototype.leadingBufferZone
        }],
        tbar: [{
            text: 'Reload',
            handler: initializeGrid
        }]
    });

    function initializeGrid() {

        store.removeAll();
        store.prefetchData.clear();
        delete store.requestStart;
        delete store.requestEnd;

        store.numFromEdge = controls.down('#storeNumFromEdge').getValue();
        store.trailingBufferZone = controls.down('#storeTrailingBufferZone').getValue();
        store.leadingBufferZone = controls.down('#storeLeadingBufferZone').getValue();

        grid.verticalScroller.numFromEdge = controls.down('#scrollerNumFromEdge').getValue();
        grid.verticalScroller.trailingBufferZone = controls.down('#scrollerTrailingBufferZone').getValue();
        grid.verticalScroller.leadingBufferZone = controls.down('#scrollerLeadingBufferZone').getValue();

        Ext.ux.ajax.SimManager.delay = controls.down('#latency').getValue();

        // Load a maximum of 100 records into the prefetch buffer (which is NOT mapped to the UI)
        // When that has completed, instruct the Store to load the first page from prefetch into the live, mapped record cache
        store.prefetch({
            start: 0,
            limit: controls.down('#primeSize').getValue(),
            callback: function() {
                store.guaranteeRange(0, controls.down('#tableSize').getValue() - 1);
            }
        });
    }

    new Ext.Viewport({
        layout: 'border',
        items: [
            {
                title: 'Configuration',
                collapsible: true,
                layout: 'border',
                region: 'west',
                bodyBorder: false,
                width: 230,
                split: true,
                minWidth: 230,
                items: [ controls, logPanel ]
            },
            grid
        ]
    })
});
