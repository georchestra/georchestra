/**
 * @include Ext.ux/widgets/tree/TreeRecordNode.js
 */
Ext.namespace("Ext.ux.tree");

/** api: (define)
 *  module = Ext.ux.tree
 *  class = TreeStoreNode
 *  base_link = `Ext.tree.TreeNode <http://extjs.com/deploy/dev/docs/?class=Ext.tree.TreeNode>`_
 */

/** api: constructor
 *  .. class:: TreeStoreNode
 *
 *      A subclass of ``Ext.tree.TreeNode`` that will collect all records
 *      from a store. Only records that have displayInTree set to true
 *      will be included.
 *
 *      To use this node type in ``TreePanel`` config, set nodeType to
 *      "ux_treestorenode".
 */
Ext.ux.tree.TreeStoreNode = Ext.extend(Ext.tree.AsyncTreeNode, {

    /** api: config[store]
     *  :class:`Ext.data.Store`
     *  The store containing record nodes to be displayed in the container.
     */
    store: null,

    /** api: config[defaults]
     *  ``Object``
     *  A configuration object passed to all nodes that this container creates.
     */
    defaults: null,

    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        this.store = config.store;
        this.defaults = config.defaults || {};
        Ext.ux.tree.TreeStoreNode.superclass.constructor.apply(this, arguments);
    },

    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        if (!this.rendered) {
            this.store.each(function(record) {
                this.addRecordNode(record);
            }, this);
            this.store.on({
                "load": this.onStoreLoad,
                "add": this.onStoreAdd,
                "remove": this.onStoreRemove,
                scope: this
            });
        }
        Ext.ux.tree.TreeStoreNode.superclass.render.call(this, bulkRender);
    },

    /** private: method[onStoreLoad]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param options: ``Object``
     *
     *  Listener for the store's load event.
     */
    onStoreLoad: function(store, records, options) {
        // if options.add: nodes have already been added to tree on "add" event
        if (options && !options.add) {
            // options.add is false: let's remove every child from the tree
            this.eachChild(function(node) { node.remove(); });
            if (!Ext.isArray(records)) {
                records = [records];
            }
            for (var i = 0; i < records.length; i++) {
                this.addRecordNode(records[i]);
            }
        }
    },

    /** private: method[onStoreAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     *
     *  Listener for the store's add event.
     */
    onStoreAdd: function(store, records, index) {
        if(!this._reordering) {
            var nodeIndex = this.recordIndexToNodeIndex(index+records.length-1);
            for(var i=0; i<records.length; ++i) {
                this.addRecordNode(records[i], nodeIndex);
            }
        }
    },

    /** private: method[onStoreRemove]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param index: ``Number``
     *
     *  Listener for the store's remove event.
     */
    onStoreRemove: function(store, record, index) {
        if(!this._reordering) {
            this.removeRecordNode(record);
        }
    },

    /** private: method[destroy]
     */
    destroy: function() {
        if(this.store) {
            this.store.un("load", this.onStoreLoad, this);
            this.store.un("add", this.onStoreAdd, this);
            this.store.un("remove", this.onStoreRemove, this);
        }
        Ext.ux.tree.TreeStoreNode.superclass.destroy.apply(this, arguments);
    },

    /** private: method[recordIndexToNodeIndex]
     *  :param index: ``Number`` The record index in the store.
     *  :return: ``Number`` The appropriate child node index for the record.
     */
    recordIndexToNodeIndex: function(index) {
        var store = this.store;
        var count = store.getCount();
        var nodeCount = this.childNodes.length;
        var nodeIndex = -1;
        for(var i=count-1; i>=0; --i) {
            if(store.getAt(i).get('displayInTree') === true) {
                ++nodeIndex;
                if(index === i || nodeIndex > nodeCount-1) {
                    break;
                }
            }
        };
        return nodeIndex;
    },

    /** private: method[nodeIndexToRecordIndex]
     *  :param index: ``Number`` The child node index.
     *  :return: ``Number`` The appropriate record index for the node.
     *
     *  Convert a child node index to a record index.
     */
    nodeIndexToRecordIndex: function(index) {
        var store = this.store;
        var count = store.getCount();
        var nodeIndex = -1;
        for(var i=count-1; i>=0; --i) {
            if(store.getAt(i).get('displayInTree') === true) {
                ++nodeIndex;
                if(index === nodeIndex) {
                    break;
                }
            }
        }
        return i;
    },

    /** private: method[addRecordNode]
     *  :param record: ``Ext.data.Record`` The record to be added.
     *  :param index: ``Number`` Optional index for the new record. Default is 0.
     *
     *  Adds a child node representing a record
     */
    addRecordNode: function(record, index) {
        index = index || 0;
        if (record.get('displayInTree') === true) {
            var node = new Ext.ux.tree.TreeRecordNode(Ext.applyIf({
                record: record,
                store: this.store
            }, this.defaults));
            var sibling = this.item(index);
            if(sibling) {
                this.insertBefore(node, sibling);
            } else {
                this.appendChild(node);
            }
            node.on("move", this.onChildMove, this);
        }
    },

    /** private: method[removeRecordNode]
     *  :param record: ``Ext.data.Record`` The record to be removed.
     *
     *  Removes a child node representing a record
     */
    removeRecordNode: function(record) {
        if (record.get('displayInTree') === true) {
            var node = this.findChildBy(function(node) {
                return node.record === record;
            });
            if(node) {
                node.un("move", this.onChildMove, this);
                node.remove();
            }
    	}
    },

    /** private: method[onChildMove]
     *  :param tree: ``Ext.data.Tree``
     *  :param node: ``Ext.tree.TreeNode``
     *  :param oldParent: ``Ext.tree.TreeNode``
     *  :param newParent: ``Ext.tree.TreeNode``
     *  :param index: ``Number``
     *
     *  Listener for child node "move" events.  This updates the order of
     *  records in the store based on new node order if the node has not
     *  changed parents.
     */
    onChildMove: function(tree, node, oldParent, newParent, index) {
        if(oldParent === newParent) {
            var newRecordIndex = this.nodeIndexToRecordIndex(index);
            var oldRecordIndex = this.store.findBy(function(record) {
                return record === node.record;
            });
            // remove the record and re-insert it at the correct index
            var record = this.store.getAt(oldRecordIndex);
            this._reordering = true;
            this.store.remove(record);
            this.store.insert(newRecordIndex, [record]);
            delete this._reordering;
        }
    }

});

/**
 * NodeType: ux_treestorenode
 */
Ext.tree.TreePanel.nodeTypes.ux_treestorenode = Ext.ux.tree.TreeStoreNode;
