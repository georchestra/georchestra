Ext.onReady(function() {

    var states = [
        ['AL', 'Alabama', 'The Heart of Dixie'],
        ['AK', 'Alaska', 'The Land of the Midnight Sun'],
        ['AZ', 'Arizona', 'The Grand Canyon State'],
        ['AR', 'Arkansas', 'The Natural State'],
        ['CA', 'California', 'The Golden State'],
        ['CO', 'Colorado', 'The Mountain State']
    ];

    // simple array store
    var store = new Ext.data.SimpleStore({
        fields: ['abbr', 'state', 'text', {name: 'displayInTree', defaultValue: true}],
        data : states
    });


    var tree = new Ext.tree.TreePanel({
        width:220,
        height:300,
        rootVisible: false,
        root: new Ext.tree.AsyncTreeNode({
            expanded: true,
            text: 'invisible root node',
            leaf: false,
            children: [{
                    text: 'dummy normal node',
                    expanded: true,
                    children: [{
                        text: 'dummy normal leaf',
                        leaf: true
                    }]
                },
                {
                    nodeType: 'ux_treestorenode',
                    loaded: true,
                    leaf: false,
                    expanded: true,
                    text: 'TreeStoreNode',
                    store: store,
                    defaults: {
                        checked: false
                    }
                }
            ]
        })
    });
        
    // the node's check button with its checkchange event can be used
    // to call a custom handler.
    var registerCheckbox = function(node){
        if(!node.hasListener("checkchange")) {
            node.on("checkchange", function(node, checked){
                alert(node.record.get('text') + " has been " + (checked==true?"":"un") + "checked.");
            });
        }
    }
    tree.on({
        "insert": registerCheckbox,
        "append": registerCheckbox,
        scope: this
    });

    tree.render(Ext.get('tree'));

});
