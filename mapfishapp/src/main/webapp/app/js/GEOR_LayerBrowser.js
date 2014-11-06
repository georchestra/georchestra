Ext.namespace("GEOR");

GEOR.LayerBrowser = Ext.extend(Ext.Panel, {

    /* public */
    store: null,

    /* private */
    
    layout: 'border',
    
    combo: null,
    
    urlField: null,
    
    mapSRS: null,

    /**
     * Property: dataview
     * {Ext.DataView}
     */
    dataview: null,

    serverStore: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {
        var tr = OpenLayers.i18n,
        default_svt = GEOR.config.DEFAULT_SERVICE_TYPE;

        this.dataview = new Ext.DataView({
            store: this.store,
            region: 'center',
            multiSelect: true,
            selectedClass: 'x-view-selected',
            simpleSelect: true,
            cls: 'x-list',
            overClass:'x-view-over',
            itemSelector: 'div.x-view-item',
            autoScroll: true,
            autoWidth: true,
            trackOver: true,
            autoHeight: true,
            tpl: this.getTemplate(),
            listeners: {
                "click": function(dv) {
                    // TODO
                    //observable.fireEvent("selectionchanged", dv.getSelectedRecords());
                }
            }
        });
        this.serverStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: GEOR.util.getValidURI(
                    GEOR.config.OGC_SERVERS_URL[default_svt]
                ),
                method: 'GET',
                disableCaching: false
            }),
            autoLoad: true,
            reader: new Ext.data.JsonReader({
                fields: ['name', 'url'],
                root: 'servers'
            })
        });
        this.combo = new Ext.form.ComboBox({
            editable: false,
            triggerAction: 'all',
            height: 30,
            width: 400,
            fieldLabel: this.fieldLabel,
            loadingText: tr("Loading..."),
            mode: 'local',
            store: this.serverStore,
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
                    checked: default_svt == "WMS"
                },{
                    boxLabel: 'WMTS',
                    name: 'svtype',
                    inputValue: 'wmts',
                    checked: default_svt == "WMTS"
                },{
                    boxLabel: 'WFS',
                    name: 'svtype',
                    inputValue: 'wfs',
                    checked: default_svt == "WFS"
                }],
                listeners: {
                    "change": this.onServiceTypeChange,
                    scope: this
                }
            }, this.combo, this.urlField]
        }, this.dataview];

        GEOR.LayerBrowser.superclass.initComponent.call(this);
    },

    /**
     * Method: onServiceTypeChange
     * 
     */
    onServiceTypeChange: function(rg, checked) {
        // clear twintriggerfield:
        this.urlField.onTrigger1Click();
        // clear results
        this.dataview.clearSelections();
        // clear combo
        this.combo.clearValue();
        // load servers list
        this.serverStore.proxy.setUrl(
            GEOR.util.getValidURI(
                GEOR.config.OGC_SERVERS_URL[
                    checked.inputValue.toUpperCase()
                ]
            )
        );
        this.serverStore.load();
        // try to focus combo
        this.combo.focus();
    },

    /**
     * Method: onSelectionchange
     * 
     */
    onSelectionchange: function(sm) {
        this.fireEvent("selectionchanged", sm.getSelections());
    },

    /**
     * Method: onComboSelect
     * 
     */
    onComboSelect: function(cmb, rec, idx) {
        if (GEOR.config.DISPLAY_SELECTED_OWS_URL) {
            this.urlField.setValue(rec.get('url'));
        }
        this.urlField.onTrigger2Click(rec.get('url'));
    },

    /**
     * Method: getTemplate
     * Creates the Ext.Dataview item template
     *
     * Returns:
     * {Ext.XTemplate}
     */
    getTemplate: function() {
        var tr = OpenLayers.i18n,
        tpl = [
            '<tpl for=".">',
                '<div class="x-view-item">',
                    '<table style="width:100%;"><tr><td style="vertical-align:text-top;">', // TODO: queryable label top right corner
                        '<p><b>{[this.title(values.title)]}</b></p>',
                        '<p>{[this.abstract(values.abstract)]}&nbsp;',
                        '<a href="{[this.metadataURL(values)]}" ext:qtip="' +
                            tr("Show metadata sheet in a new window") + '" ',
                        'target="_blank" onclick="window.open(this.href);return false;">' +
                            tr('more') + '</a></p>',
                    '</td></tr></table>',
                '</div>',
            '</tpl>'
        ].join('');

        var context = {
            "title": function(t) {
                return GEOR.util.shorten(t, 200);
            },
            "metadataURL": function(values) {
                return ""; // FIXME
            },
            "abstract": function(t) {
                // two things here:
                // 1) shorten text
                // 2) replace url links with <a href="XXX">lien</a>
                //    (long links can break the dataview layout)
                t = GEOR.util.shorten(t, 400);
                var regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/gi;
                return t.replace(regexp,
                    '[<a href="$&" ext:qtip="'+
                        tr("Open the URL url in a new window", {'URL': '$&'})
                        +'"' +
                    ' target="_blank" onclick="window.open(this.href);return false;">lien</a>]'
                );
            }
        };

        return new Ext.XTemplate(tpl, context);
    },

    /**
     * APIMethod: clearSelection
     * Clears the current selection
     */
    clearSelection: function() {
        this.dataview.clearSelections();
    },

    /** private: method[destroy]
     */
    destroy: function() {
        this.combo.un("select", this.onComboSelect, this);
        //this.cbxSm.un("selectionchange", this.onSelectionchange, this);
        GEOR.LayerBrowser.superclass.destroy.call(this);
    }
});