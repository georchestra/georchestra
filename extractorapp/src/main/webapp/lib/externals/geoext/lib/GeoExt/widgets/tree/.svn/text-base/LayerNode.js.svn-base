/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.tree");

/** private: constructor
 *  .. class:: LayerNodeUI
 *
 *      Place in a separate file if this should be documented.
 */
GeoExt.tree.LayerNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {

    /** private: constant[actionsCls]
     */
    actionsCls: "gx-tree-layer-actions",

    /** private: constant[actionCls]
     */
    actionCls: "gx-tree-layer-action",
    
    /** private: method[constructor]
     */
    constructor: function(config) {
        GeoExt.tree.LayerNodeUI.superclass.constructor.apply(this, arguments);
    },
    
    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        var attr = this.node.attributes;
        if (attr.checked === undefined) {
            attr.checked = this.node.layer.getVisibility();
        }
        var rendered = this.rendered;
        GeoExt.tree.LayerNodeUI.superclass.render.apply(this, arguments);

        if (!rendered) {
            var cb = this.checkbox;
            if(attr.checkedGroup) {
                // replace the checkbox with a radio button
                var radio = Ext.DomHelper.insertAfter(cb,
                    ['<input type="radio" name="', attr.checkedGroup,
                    '_checkbox" class="', cb.className,
                    cb.checked ? '" checked="checked"' : '',
                    '"></input>'].join(""));
                radio.defaultChecked = cb.defaultChecked;
                Ext.get(cb).remove();
                this.checkbox = radio;
            }

            this.enforceOneVisible();

            var actions = attr.actions || this.actions;
            if(actions && actions.length > 0) {
                var html = ['<div class="', this.actionsCls, '">'];
                for(var i=0,len=actions.length; i<len; i++) {
                    var a = actions[i];
                    html = html.concat([
                        '<img id="'+this.node.id+'_'+a.action,
                        '" ext:qtip="'+a.qtip,
                        '" src="'+this.emptyIcon,
                        '" class="'+this.actionCls+' '+a.action+'" />'
                    ]);
                }
                html.concat(['</div>']);
                Ext.DomHelper.insertFirst(this.elNode, html.join(""));
            }
            this.updateActions();

            var component = attr.component || this.component;
            if(component) {
                var elt = Ext.DomHelper.append(this.elNode, [
                    {"tag": "div"}
                ]);
                if(typeof component == "function") {
                    component = component(this.node, elt);
                } else if (typeof component == "object" &&
                           typeof component.fn == "function") {
                    component = component.fn.apply(
                        component.scope, [this.node, elt]
                    );
                }
                if(typeof component == "object" &&
                   typeof component.xtype == "string") {
                    component = Ext.ComponentMgr.create(component);
                }
                if(component instanceof Ext.Component) {
                    component.render(elt);
                    this.node.component = component;
                }
            }
        }
    },
    
    /** private: method[onClick]
     *  :param e: ``Object``
     */
    onClick: function(e) {
        if(e.getTarget('.x-tree-node-cb', 1)) {
            this.toggleCheck(this.isChecked());
        } else if(e.getTarget('.' + this.actionCls, 1)) {
            var t = e.getTarget('.' + this.actionCls, 1);
            var action = t.className.replace(this.actionCls + ' ', '');
            if (this.fireEvent("action", this.node, action, e) === false) {
                return;
            }
        } else {
            GeoExt.tree.LayerNodeUI.superclass.onClick.apply(this, arguments);
        }
    },

    /** private: method[updateActions]
     *
     *  Update all the actions.
     */
    updateActions: function() {
        var n = this.node;
        var actions = n.attributes.actions || this.actions || [];
        Ext.each(actions, function(a, index) {
            var el = Ext.get(n.id + '_' + a.action);
            if (el && typeof a.update == "function") {
                a.update.call(n, el);
            }
        });
    },
    
    /** private: method[toggleCheck]
     * :param value: ``Boolean``
     */
    toggleCheck: function(value) {
        value = (value === undefined ? !this.isChecked() : value);
        GeoExt.tree.LayerNodeUI.superclass.toggleCheck.call(this, value);
        
        this.enforceOneVisible();
    },
    
    /** private: method[enforceOneVisible]
     * 
     *  Makes sure that only one layer is visible if checkedGroup is set.
     */
    enforceOneVisible: function() {
        var attributes = this.node.attributes;
        var group = attributes.checkedGroup;
        if(group) {
            var layer = this.node.layer;
            var checkedNodes = this.node.getOwnerTree().getChecked();
            var checkedCount = 0;
            // enforce "not more than one visible"
            Ext.each(checkedNodes, function(n){
                var l = n.layer;
                if(!n.hidden && n.attributes.checkedGroup === group) {
                    checkedCount++;
                    if(l != layer && attributes.checked) {
                        l.setVisibility(false);
                    }
                }
            });
            // enforce "at least one visible"
            if(checkedCount === 0 && attributes.checked == false) {
                layer.setVisibility(true);
            }
        }
    },
    
    /** private: method[appendDDGhost]
     *  :param ghostNode ``DOMElement``
     *  
     *  For radio buttons, makes sure that we do not use the option group of
     *  the original, otherwise only the original or the clone can be checked 
     */
    appendDDGhost : function(ghostNode){
        var n = this.elNode.cloneNode(true);
        var radio = Ext.DomQuery.select("input[type='radio']", n);
        Ext.each(radio, function(r) {
            r.name = r.name + "_clone";
        });
        ghostNode.appendChild(n);
    }
});


/** api: (define)
 *  module = GeoExt.tree
 *  class = LayerNode
 *  base_link = `Ext.tree.TreeNode <http://extjs.com/deploy/dev/docs/?class=Ext.tree.TreeNode>`_
 */

/** api: constructor
 *  .. class:: LayerNode(config)
 * 
 *      A subclass of ``Ext.tree.TreeNode`` that is connected to an
 *      ``OpenLayers.Layer`` by setting the node's layer property. Checking or
 *      unchecking the checkbox of this node will directly affect the layer and
 *      vice versa. The default iconCls for this node's icon is
 *      "gx-tree-layer-icon", unless it has children.
 * 
 *      Setting the node's layer property to a layer name instead of an object
 *      will also work. As soon as a layer is found, it will be stored as layer
 *      property in the attributes hash.
 * 
 *      The node's text property defaults to the layer name.
 *      
 *      If the node has a checkedGroup attribute configured, it will be
 *      rendered with a radio button instead of the checkbox. The value of
 *      the checkedGroup attribute is a string, identifying the options group
 *      for the node.
 * 
 *      To use this node type in a ``TreePanel`` config, set ``nodeType`` to
 *      "gx_layer".
 */
GeoExt.tree.LayerNode = Ext.extend(Ext.tree.AsyncTreeNode, {
    
    /** api: config[layer]
     *  ``OpenLayers.Layer or String``
     *  The layer that this layer node will
     *  be bound to, or the name of the layer (has to match the layer's
     *  name property). If a layer name is provided, ``layerStore`` also has
     *  to be provided.
     */

    /** api: property[layer]
     *  ``OpenLayers.Layer``
     *  The layer this node is bound to.
     */
    layer: null,
    
    /** api: config[layerStore]
     *  :class:`GeoExt.data.LayerStore` ``or "auto"``
     *  The layer store containing the layer that this node represents.  If set
     *  to "auto", the node will query the ComponentManager for a
     *  :class:`GeoExt.MapPanel`, take the first one it finds and take its layer
     *  store. This property is only required if ``layer`` is provided as a
     *  string.
     */
    layerStore: null,
    
    /** api: config[loader]
     *  ``Ext.tree.TreeLoader|Object`` If provided, subnodes will be added to
     *  this LayerNode. Obviously, only loaders that process an
     *  ``OpenLayers.Layer`` or :class:`GeoExt.data.LayerRecord` (like
     *  :class:`GeoExt.tree.LayerParamsLoader`) will actually generate child
     *  nodes here. If provided as ``Object``, a
     *  :class:`GeoExt.tree.LayerParamLoader` instance will be created, with
     *  the provided object as configuration.
     */

    /** api: config[actions]
     *  ``Array(Object)`` An array of objects defining actions. An action is a
     *  clickable image in the node, it is defined with two properties:
     *  "action" and "qtip".
     *  * the "action" property provides the name of the action. It is used as
     *    the name of the ``img`` tag's class. The ``img`` tag being placed in a
     *    div whose class is "gx-tree-layer-actions" a CSS selector for the
     *    action is ``.gx-tree-layer-actions .action-name``. The name of the
     *    action is also set in "action" events for "action" listeners to know
     *    which action got clicked.
     *  * the "qtip" property references the tooltip displayed when the action
     *    image is hovered.
     *  This property applies only if the node is configured with a
     *  :class:`GeoExt.tree.LayerNodeUI` UI instance (which is the default).
     */

    /** api: config[component]
     *  ``Ext.Component or Object or Function`` This property is to be used
     *  when an Ext component is to be inserted in the node. This property can
     *  be used in several ways, it can reference
     *  * ``Ext.Component`` a component instance. In this case the provided
     *    component is just rendered in the node.
     *  * ``Object`` a component config (using ``xtype``). In this case the
     *    component is instantiated and then rendered in the node.
     *  * ``Function`` a function returning a component instance or config.
     *    This function is passed a reference to the layer node and to the Ext
     *    element (``Ext.Element``) into which the component is to be rendered,
     *    it must returned a component instance or config.
     *  * ``Object`` an object with a ``fn`` and ``scope`` properties. ``fn``
     *    references a function returning a component instance or config (like
     *    previously), ``scope`` is its execution scope.
     *  This property applies only if the node is configured with a
     *  :class:`GeoExt.tree.LayerNodeUI` UI instance (which is the default).
     */
    
    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        config.leaf = config.leaf || !(config.children || config.loader);
        
        if(!config.iconCls && !config.children) {
            config.iconCls = "gx-tree-layer-icon";
        }
        if(config.loader && !(config.loader instanceof Ext.tree.TreeLoader)) {
            config.loader = new GeoExt.tree.LayerParamLoader(config.loader);
        }
        
        this.defaultUI = this.defaultUI || GeoExt.tree.LayerNodeUI;
        this.addEvents(
            /** api: event[action]
             *  Notifies listeners when an action is clicked, listeners are
             *  called with the following arguments:
             *  * :class:`GeoExt.tree.LayerNode` the layer node
             *  * ``String`` the action name
             *  * ``Ext.EventObject`` the event object
             */
            "action"
        );

        
        Ext.apply(this, {
            layer: config.layer,
            layerStore: config.layerStore
        });
        if (config.text) {
            this.fixedText = true;
        }
        GeoExt.tree.LayerNode.superclass.constructor.apply(this, arguments);
    },

    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        var layer = this.layer instanceof OpenLayers.Layer && this.layer;
        if(!layer) {
            // guess the store if not provided
            if(!this.layerStore || this.layerStore == "auto") {
                this.layerStore = GeoExt.MapPanel.guess().layers;
            }
            // now we try to find the layer by its name in the layer store
            var i = this.layerStore.findBy(function(o) {
                return o.get("title") == this.layer;
            }, this);
            if(i != -1) {
                // if we found the layer, we can assign it and everything
                // will be fine
                layer = this.layerStore.getAt(i).get("layer");
            }
        }
        if (!this.rendered || !layer) {
            var ui = this.getUI();
            
            if(layer) {
                this.layer = layer;
                // no DD and radio buttons for base layers
                if(layer.isBaseLayer) {
                    this.draggable = false;
                    Ext.applyIf(this.attributes, {
                        checkedGroup: "gx_baselayer"
                    });
                }
                if(!this.text) {
                    this.text = layer.name;
                }
                
                ui.show();
                this.addVisibilityEventHandlers();
            } else {
                ui.hide();
            }
            
            if(this.layerStore instanceof GeoExt.data.LayerStore) {
                this.addStoreEventHandlers(layer);
            }            
        }
        GeoExt.tree.LayerNode.superclass.render.apply(this, arguments);
    },
    
    /** private: method[addVisibilityHandlers]
     *  Adds handlers that sync the checkbox state with the layer's visibility
     *  state
     */
    addVisibilityEventHandlers: function() {
        this.layer.events.on({
            "visibilitychanged": this.onLayerVisibilityChanged,
            scope: this
        }); 
        this.on({
            "checkchange": this.onCheckChange,
            scope: this
        });
    },
    
    /** private: method[onLayerVisiilityChanged
     *  handler for visibilitychanged events on the layer
     */
    onLayerVisibilityChanged: function() {
        if(!this._visibilityChanging) {
            this.getUI().toggleCheck(this.layer.getVisibility());
        }
    },
    
    /** private: method[onCheckChange]
     *  :param node: ``GeoExt.tree.LayerNode``
     *  :param checked: ``Boolean``
     *
     *  handler for checkchange events 
     */
    onCheckChange: function(node, checked) {
        if(checked != this.layer.getVisibility()) {
            this._visibilityChanging = true;
            var layer = this.layer;
            if(checked && layer.isBaseLayer && layer.map) {
                layer.map.setBaseLayer(layer);
            } else {
                layer.setVisibility(checked);
            }
            delete this._visibilityChanging;
        }
    },
    
    /** private: method[addStoreEventHandlers]
     *  Adds handlers that make sure the node disappeares when the layer is
     *  removed from the store, and appears when it is re-added.
     */
    addStoreEventHandlers: function() {
        this.layerStore.on({
            "add": this.onStoreAdd,
            "remove": this.onStoreRemove,
            "update": this.onStoreUpdate,
            scope: this
        });
    },
    
    /** private: method[onStoreAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     *
     *  handler for add events on the store 
     */
    onStoreAdd: function(store, records, index) {
        var l;
        for(var i=0; i<records.length; ++i) {
            l = records[i].get("layer");
            if(this.layer == l) {
                this.getUI().show();
                break;
            } else if (this.layer == l.name) {
                // layer is a string, which means the node has not yet
                // been rendered because the layer was not found. But
                // now we have the layer and can render.
                this.render();
                break;
            }
        }
    },
    
    /** private: method[onStoreRemove]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param index: ``Number``
     *
     *  handler for remove events on the store 
     */
    onStoreRemove: function(store, record, index) {
        if(this.layer == record.get("layer")) {
            this.getUI().hide();
        }
    },

    /** private: method[onStoreUpdate]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param operation: ``String``
     *  
     *  Listener for the store's update event.
     */
    onStoreUpdate: function(store, record, operation) {
        var layer = record.get("layer");
        if(!this.fixedText && (this.layer == layer && this.text !== layer.name)) {
            this.setText(layer.name);
        }
    },

    /** private: method[destroy]
     */
    destroy: function() {
        var layer = this.layer;
        if (layer instanceof OpenLayers.Layer) {
            layer.events.un({
                "visibilitychanged": this.onLayerVisibilityChanged,
                scope: this
            });
        }
        delete this.layer;
        var layerStore = this.layerStore;
        if(layerStore) {
            layerStore.un("add", this.onStoreAdd, this);
            layerStore.un("remove", this.onStoreRemove, this);
            layerStore.un("update", this.onStoreUpdate, this);
        }
        delete this.layerStore;
        this.un("checkchange", this.onCheckChange, this);

        GeoExt.tree.LayerNode.superclass.destroy.apply(this, arguments);
    }
});

/**
 * NodeType: gx_layer
 */
Ext.tree.TreePanel.nodeTypes.gx_layer = GeoExt.tree.LayerNode;
