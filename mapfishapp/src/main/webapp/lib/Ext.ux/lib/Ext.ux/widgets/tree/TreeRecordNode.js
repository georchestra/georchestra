Ext.namespace("Ext.ux.tree");

/** api: (define)
 *  module = Ext.ux.tree
 *  class = TreeRecordNode
 *  base_link = `Ext.tree.TreeNode <http://extjs.com/deploy/dev/docs/?class=Ext.tree.TreeNode>`_
 */

/** api: constructor
 *  .. class:: TreeRecordNode(config)
 * 
 *      A subclass of ``Ext.tree.TreeNode`` that is connected to an
 *      ``Ext.data.Record`` by setting the node's record property.
 * 
 *      The node's text property defaults to the record 'text' attribute.
 * 
 *      To use this node type in a ``TreePanel`` config, set ``nodeType`` to
 *      "ux_treerecordnode".
 */
Ext.ux.tree.TreeRecordNode = Ext.extend(Ext.tree.TreeNode, {
    
    /** api: config[store]
     *  :class:`Ext.data.Store`
     *  The store containing the record that this node represents.
     */
    store: null,

    /** api: config[record]
     *  :class:``Ext.data.Record``
     *  The record this node is bound to.
     */
    record: null,
    
    /** api: config[childNodeType]
     *  ``Ext.tree.Node or String``
     *  Node class or nodeType of childnodes for this node. A node type provided
     *  here needs to have an add method, with a scope argument. This method
     *  will be run by this node in the context of this node, to create child nodes.
     */
    childNodeType: null,
    
    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        config.leaf = config.leaf || !config.children;
        config.qtip = config.record.get('qtip') || undefined;
        Ext.apply(this, {
            record: config.record,
            store: config.store,
            childNodeType: config.childNodeType
        });
        Ext.ux.tree.TreeRecordNode.superclass.constructor.apply(this, arguments);
    },

    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        if (!this.rendered) {
            var ui = this.getUI();
            
            if(this.record) {
                if(!this.text) {
                    this.text = this.record.get('text'); // which member to display?
                }
                if(this.childNodeType) {
                    this.addChildNodes();
                }
                ui.show();
            } else {
                ui.hide();
            }
            
            if(this.store instanceof Ext.data.Store) {
                this.addStoreEventHandlers();
            }            
        }
        Ext.ux.tree.TreeRecordNode.superclass.render.call(this, bulkRender);
    },
    
    /** private: method[addStoreEventHandlers]
     *  Adds handlers that make sure the node disappeares when the record is
     *  removed from the store, and appears when it is re-added.
     */
    addStoreEventHandlers: function() {
        this.store.on({
            "add": this.onStoreAdd,
            "remove": this.onStoreRemove,
            scope: this
        });
    },

    /** private: method[onStoreAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Nmber``
     *  handler for add events on the store 
     */
    onStoreAdd: function(store, records, index) {
        for(var i=0; i<records.length; ++i) {
            if(this.record === records[i]) {
                this.getUI().show();
            }
        }
    },

    /** private: method[onStoreRemove]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param index: ``Nmber``
     *  handler for remove events on the store 
     */
    onStoreRemove: function(store, record, index) {
        if(this.record === record) {
            this.getUI().hide();
        }
    },
    
    /** private: method[addChildNodes]
     *  Calls the add method of a node type configured as ``childNodeType``
     *  to add children.
     */
    addChildNodes: function() {
        if(typeof this.childNodeType == "string") {
            Ext.tree.TreePanel.nodeTypes[this.childNodeType].add(this);
        } else if(typeof this.childNodeType.add === "function") {
            this.childNodeType.add(this);
        }
    },
    
    /** private: method[destroy]
     */
    destroy: function() {
        delete this.record;
        this.store.un("add", this.onStoreAdd, this);
        this.store.un("remove", this.onStoreRemove, this);
        delete this.store;

        Ext.ux.tree.TreeRecordNode.superclass.destroy.call(this);
    }
});

/**
 * NodeType: ux_treerecordnode
 */
Ext.tree.TreePanel.nodeTypes.ux_treerecordnode = Ext.ux.tree.TreeRecordNode;
