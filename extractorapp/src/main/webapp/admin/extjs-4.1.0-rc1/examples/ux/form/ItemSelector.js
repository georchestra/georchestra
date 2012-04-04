/*
 * Note that this control will most likely remain as an example, and not as a core Ext form
 * control.  However, the API will be changing in a future release and so should not yet be
 * treated as a final, stable API at this time.
 */

/**
 * A control that allows selection of between two Ext.ux.form.MultiSelect controls.
 */
Ext.define('Ext.ux.form.ItemSelector', {
    extend: 'Ext.ux.form.MultiSelect',
    alias: ['widget.itemselectorfield', 'widget.itemselector'],
    alternateClassName: ['Ext.ux.ItemSelector'],
    requires: [
        'Ext.button.Button',
        'Ext.ux.form.MultiSelect'
    ],
    
    /**
     * @cfg {Boolean} [hideNavIcons=false] True to hide the navigation icons
     */
    hideNavIcons:false,

    /**
     * @cfg {Array} buttons Defines the set of buttons that should be displayed in between the ItemSelector
     * fields. Defaults to <tt>['top', 'up', 'add', 'remove', 'down', 'bottom']</tt>. These names are used
     * to build the button CSS class names, and to look up the button text labels in {@link #buttonsText}.
     * This can be overridden with a custom Array to change which buttons are displayed or their order.
     */
    buttons: ['top', 'up', 'add', 'remove', 'down', 'bottom'],

    /**
     * @cfg {Object} buttonsText The tooltips for the {@link #buttons}.
     * Labels for buttons.
     */
    buttonsText: {
        top: "Move to Top",
        up: "Move Up",
        add: "Add to Selected",
        remove: "Remove from Selected",
        down: "Move Down",
        bottom: "Move to Bottom"
    },
    
    initComponent: function(){
        this.ddGroup = this.id + '-dd';
        this.callParent();
    },
    
    createList: function(){
        var me = this;
        
        return Ext.create('Ext.ux.form.MultiSelect', {
            submitValue: false,
            flex: 1,
            dragGroup: me.ddGroup,
            dropGroup: me.ddGroup,
            store: {
                model: me.store.model,
                data: []
            },
            displayField: me.displayField,
            disabled: me.disabled,
            listeners: {
                boundList: {
                    scope: me,
                    itemdblclick: me.onItemDblClick,
                    drop: me.syncValue
                }
            }
        });
    },

    setupItems: function() {
        var me = this;
        
        me.fromField = me.createList();
        me.toField = me.createList();
        
        // add everything to the from field at the start
        me.fromField.store.add(me.store.getRange());
        
        return {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                me.fromField,
                {
                    xtype: 'container',
                    margins: '0 4',
                    width: 22,
                    layout: {
                        type: 'vbox',
                        pack: 'center'
                    },
                    items: me.createButtons()
                },
                me.toField
            ]
        };
    },
    
    createButtons: function(){
        var me = this,
            buttons = [];
            
        if (!me.hideNavIcons) {
            Ext.Array.forEach(me.buttons, function(name) {
                buttons.push({
                    xtype: 'button',
                    tooltip: me.buttonsText[name],
                    handler: me['on' + Ext.String.capitalize(name) + 'BtnClick'],
                    cls: Ext.baseCSSPrefix + 'form-itemselector-btn',
                    iconCls: Ext.baseCSSPrefix + 'form-itemselector-' + name,
                    navBtn: true,
                    scope: me,
                    margin: '4 0 0 0'
                });
            });
        }
        return buttons;
    },
    
    getSelections: function(list){
        var store = list.getStore(),
            selections = list.getSelectionModel().getSelection(),
            i = 0,
            len = selections.length;
            
        return Ext.Array.sort(selections, function(a, b){
            a = store.indexOf(a);
            b = store.indexOf(b);
            
            if (a < b) {
                return -1;
            } else if (a > b) {
                return 1;
            }
            return 0;
        });
    },

    onTopBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            i = selected.length - 1,
            selection;
        
        
        store.suspendEvents();
        for (; i > -1; --i) {
            selection = selected[i];
            store.remove(selected);
            store.insert(0, selected);
        }
        store.resumeEvents();
        list.refresh();   
        this.syncValue(); 
    },

    onBottomBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            i = 0,
            len = selected.length,
            selection;
            
        store.suspendEvents();
        for (; i < len; ++i) {
            selection = selected[i];
            store.remove(selection);
            store.add(selection);
        }
        store.resumeEvents();
        list.refresh();
        this.syncValue();
    },

    onUpBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            i = 0,
            len = selected.length,
            selection,
            index;
            
        store.suspendEvents();
        for (; i < len; ++i) {
            selection = selected[i];
            index = Math.max(0, store.indexOf(selection) - 1);
            store.remove(selection);
            store.insert(index, selection);
        }
        store.resumeEvents();
        list.refresh();
        this.syncValue();
    },

    onDownBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            i = 0,
            len = selected.length,
            max = store.getCount(),
            selection,
            index;
            
        store.suspendEvents();
        for (; i < len; ++i) {
            selection = selected[i];
            index = Math.min(max, store.indexOf(selection) + 1);
            store.remove(selection);
            store.insert(index, selection);
        }
        store.resumeEvents();
        list.refresh();
        this.syncValue();
    },

    onAddBtnClick : function() {
        var me = this,
            fromList = me.fromField.boundList,
            selected = this.getSelections(fromList);
            
        fromList.getStore().remove(selected);
        this.toField.boundList.getStore().add(selected);
        this.syncValue();
    },

    onRemoveBtnClick : function() {
        var me = this,
            toList = me.toField.boundList,
            selected = this.getSelections(toList);
            
        toList.getStore().remove(selected);
        this.fromField.boundList.getStore().add(selected);
        this.syncValue();
    },
    
    syncValue: function(){
        this.setValue(this.toField.store.getRange()); 
    },
    
    onItemDblClick: function(view, rec){
        var me = this,
            from = me.fromField.store,
            to = me.toField.store,
            current,
            destination;
            
        if (from.indexOf(rec) > -1) {
            current = from;
            destination = to;
        } else {
            current = to;
            destination = from;
        }
        current.remove(rec);
        destination.add(rec);
        me.syncValue();
    },
    
    setValue: function(value){
        var me = this,
            fromStore = me.fromField.store,
            toStore = me.toField.store,
            selected;
        
        value = me.setupValue(value);
        me.mixins.field.setValue.call(me, value);
        
        selected = me.getRecordsForValue(value);
        
        Ext.Array.forEach(toStore.getRange(), function(rec){
            if (!Ext.Array.contains(selected, rec)) {
                // not in the selected group, remove it from the toStore
                toStore.remove(rec);
                fromStore.add(rec);
            }
        });
        toStore.removeAll();
        
        Ext.Array.forEach(selected, function(rec){
            // In the from store, move it over
            if (fromStore.indexOf(rec) > -1) {
                fromStore.remove(rec);     
            }
            toStore.add(rec);
        });
    },
    
    onBindStore: Ext.emptyFn,
    
    onEnable: function(){
        var me = this;
        
        me.callParent();
        me.fromField.enable();
        me.toField.enable();
        
        Ext.Array.forEach(me.query('[navBtn]'), function(btn){
            btn.enable();
        });
    },
    
    onDisable: function(){
        var me = this;
        
        me.callParent();
        me.fromField.disable();
        me.toField.disable();
        
        Ext.Array.forEach(me.query('[navBtn]'), function(btn){
            btn.disable();
        });
    },
    
    onDestroy: function(){
        this.bindStore(null);
        this.callParent();
    }
});
