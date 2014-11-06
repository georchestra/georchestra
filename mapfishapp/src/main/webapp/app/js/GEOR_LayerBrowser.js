Ext.namespace("GEOR");

GEOR.LayerBrowser = Ext.extend(Ext.Panel, {

    /* public */
    store: null,

    servers: null,
    
    columns: null,

    /* private */
    
    layout: 'border',
    
    combo: null,
    
    urlField: null,
    
    mapSRS: null,

    /**
     * Property: cbxSm
     * {Ext.grid.CheckboxSelectionModel} the selection model
     */
    cbxSm: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {

        this.cbxSm =  new Ext.grid.CheckboxSelectionModel({
            width: 20,
            // for check all/none behaviour:
            header: '<div class="x-grid3-hd-checker">&#160;</div>',
            listeners: {
                "selectionchange": this.onSelectionchange,
                scope: this
            }
        });

        var tr = OpenLayers.i18n;
        // create a grid to display records from the store
        var grid = new Ext.grid.GridPanel({
            region: 'center',
            border: false,
            store: this.store,
            loadMask: {
                msg: tr("Loading...")
            },
            columns: [this.cbxSm].concat(this.columns),
            sm: this.cbxSm,
            enableHdMenu: false,
            autoExpandColumn: "description"
        });

        this.combo = new Ext.form.ComboBox({
            editable: false,
            triggerAction: 'all',
            height: 30,
            width: 400,
            fieldLabel: this.fieldLabel,
            loadingText: tr("Loading..."),
            mode: 'local',
            store: new Ext.data.Store({
                data: this.servers,
                reader: new Ext.data.JsonReader({
                    fields: ['name', 'url']
                })
            }),
            listeners: {
                "select": this.onComboSelect,
                scope: this
            },
            valueField: 'url',
            displayField: 'name',
            tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
        });
        this.urlField = new Ext.app.OWSUrlField({
            labelSeparator: tr("labelSeparator"),
            fieldLabel: tr("... or enter its address"),
            callback: function(r, options, success) {
                // We don't want to display layers
                // which cannot be served in map's native SRS
                if (this.mapSRS) {
                    // TODO: find a way to disable records whose SRS does not match the map SRS 
                    // (make them greyed out and unselectable)
                    // using cbxSm event beforerowselect returning false for instance
                    var t = this.store.getCount(), 
                        srs = this.mapSRS;
                    this.store.filterBy(function(record, id) {
                        return record.get('srs')[srs] === true;
                    });
                    var notDisplayed = t - this.store.getCount();
                    if (notDisplayed > 0) {
                        var msg = (notDisplayed > 1) ?
                            tr("The server is publishing NB layers with an incompatible projection", {'NB': notDisplayed})
                            : tr("The server is publishing one layer with an incompatible projection");
                        GEOR.util.infoDialog({
                           msg: msg
                        });
                    }
                }
            },
            scope: this,
            store: this.store,
            height: 30,
            width: 400
        });

        this.items = [{
            region: 'north',
            layout: 'form',
            border: false,
            labelSeparator: tr("labelSeparator"),
            labelWidth: 170,
            bodyStyle: 'padding: 5px;',
            height: 90,
            items: [{
                xtype: 'radiogroup',
                fieldLabel: tr("Service type"),
                items: [{
                    boxLabel: 'WMS', 
                    name: 'svtype',
                    inputValue: 'wms',
                    checked: true
                },{
                    boxLabel: 'WMTS',
                    inputValue: 'wmts',
                    name: 'svtype'
                },{
                    boxLabel: 'WFS',
                    inputValue: 'wfs',
                    name: 'svtype'
                }]
            }, this.combo, this.urlField]
        }, grid];

        GEOR.LayerBrowser.superclass.initComponent.call(this);
    },

    onSelectionchange: function(sm) {
        this.fireEvent("selectionchanged", sm.getSelections());
    },

    onComboSelect: function(cmb, rec, idx) {
        if (GEOR.config.DISPLAY_SELECTED_OWS_URL) {
            this.urlField.setValue(rec.get('url'));
        }
        this.urlField.onTrigger2Click(rec.get('url'));
    },

    /**
     * APIMethod: clearSelection
     * Clears the current selection
     */
    clearSelection: function() {
        this.cbxSm.clearSelections();
    },

    /** private: method[destroy]
     */
    destroy: function() {
        this.combo.un("select", this.onComboSelect, this);
        this.cbxSm.un("selectionchange", this.onSelectionchange, this);
        GEOR.LayerBrowser.superclass.destroy.call(this);
    }
});