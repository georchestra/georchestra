Ext.namespace("GEOR");

GEOR.LayerBrowser = Ext.extend(Ext.Panel, {

    layout: 'border',
    mapSRS: null,
    defaults: {
        border: false
    },

    // the servers list combo
    combo: null,
    // the store behind the combo
    serverStore: null,

    // the twin trigger field
    urlField: null,
    // the layer store
    store: null,

    dataview: null,

    mask: null,

    filterPanel: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {
        var tr = OpenLayers.i18n;
        this.fieldLabel = tr("Choose a server");
        this.store.on("datachanged", this.onStoreDatachanged, this);
        this.store.on("clear", this.onStoreClear, this); // triggered by store.removeAll(), not catched by "datachanged"
        this.store.on("beforeload", this.onStoreBeforeload, this);
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
                "selectionchange": this.onSelectionchange,
                scope: this
            }
        });
        this.serverStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: GEOR.util.getValidURI(
                    GEOR.config.OGC_SERVERS_URL[this.id]
                ),
                method: 'GET',
                disableCaching: false
            }),
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
                // clear filter:
                this.filterPanel.items.get(0).reset();
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

        this.filterPanel = new Ext.FormPanel({
            region: "south",
            layout: 'column',
            disabled: true,
            hideLabels: true,
            bodyStyle: 'padding: 5px;',
            border: false,
            height: 31,
            items: [new Ext.ux.form.SearchField({
                store: this.store,
                emptyText: tr("I'm looking for ..."),
                width: 150
            }), {
                xtype: "displayfield",
                value: "",
                style: {
                    textAlign: "right",
                    margin: "5px"
                },
                columnWidth: 1
            }]
        });

        this.items = [{
            region: 'north',
            layout: 'form',
            labelSeparator: tr("labelSeparator"),
            labelWidth: 170,
            bodyStyle: 'padding: 5px;',
            height: 65,
            items: [
                this.combo, 
                this.urlField
            ]
        }, {
            region: 'center',
            autoScroll: true,
            layout: 'fit',
            items: this.dataview,
            listeners: {
                "afterlayout": function() {
                    if (!this.mask) {
                        // async load servers list:
                        this.serverStore.load();
                        // build mask
                        (function() {
                            this.mask = new Ext.LoadMask(this.dataview.ownerCt.getEl(), {
                                msg: tr("Loading...")
                            });
                        }).defer(1000, this);
                        // defer is required to get correct mask position
                    }
                },
                scope: this
            }
        }, this.filterPanel]; // TODO: instead of a new panel, try with a bottom bar attached to the dataview ?

        GEOR.LayerBrowser.superclass.initComponent.call(this);

        this.addEvents(
            /**
             * @event selectionchanged
             */
            "selectionchanged"
        );
    },

    /**
     * Method: onStoreBeforeload
     * 
     */
    onStoreBeforeload: function() {
        this.filterPanel.disable();
        this.mask && this.mask.show();
    },

    /**
     * Method: onStoreDatachanged 
     * ... equivalent to "load" or "filtered"
     */
    onStoreDatachanged: function(store) {
        this.filterPanel.enable();
        // hide mask
        this.mask && this.mask.hide();
        // focus search field:
        this.filterPanel.items.get(0).focus();
        // update store count:
        this.filterPanel.items.get(1).setRawValue(
            store.getCount() + " " + OpenLayers.i18n("layers")
        );
        // scroll dataview to top:
        var el = this.dataview.getEl();
        var f = el && el.first();
        f && f.scrollIntoView(this.dataview.container);
    },

    /**
     * Method: onStoreClear
     * 
     */
    onStoreClear: function(store) {
        // update store count:
        this.filterPanel.items.get(1).setRawValue(
            store.getCount() + " " + OpenLayers.i18n("layers")
        );
        this.filterPanel.disable();
    },

    /**
     * Method: onSelectionchange
     * 
     */
    onSelectionchange: function(dv, selections) {
        this.fireEvent("selectionchanged", dv.getRecords(selections));
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
                    '<table style="width:100%;"><tr><td style="vertical-align:text-top;">',
                        '{[this.queryable(values)]}',
                        '<p><b>{[this.title(values.title)]}</b></p>',
                        '<p>{[this.abstract(values.abstract)]}&nbsp;',
                        '{[this.mdlink(values)]}',
                    '</td></tr></table>',
                '</div>',
            '</tpl>'
        ].join('');
        // Ideas to make it lighter :
        // - border-botttom: 1px thin line
        // - white background
        // - metadata link in a new p at the end
        // - metadata link lighter color : not blue, but grey.

        var context = {
            "title": function(t) {
                // shorten to 65 to take into account uppercased layer titles
                return GEOR.util.shorten(t, 65);
            },
            "queryable": function(values) {
                return values.queryable ?
                    '<div style="float:right;"><img src="'+GEOR.config.PATHNAME+'/app/img/famfamfam/information.png" /></div>' :
                    "";
            },
            "mdlink": function(values) {
                var url = GEOR.util.setMetadataURL(values.layer, values.metadataURLs);
                return url ? [
                    '&nbsp;-&nbsp;<a href="', url, '" ext:qtip="',
                        tr("Show metadata sheet in a new window"), '" ',
                        'target="_blank" onclick="window.open(this.href);return false;">',
                        tr('metadata'), '</a></p>'
                ].join('') : "";
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
        this.store.un("datachanged", this.onStoreDatachanged, this);
        this.store.un("clear", this.onStoreClear, this);
        this.store.un("beforeload", this.onStoreBeforeload, this);
        GEOR.LayerBrowser.superclass.destroy.call(this);
    }
});



Ext.ns('Ext.ux.form');
Ext.ux.form.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function(){
        Ext.ux.form.SearchField.superclass.initComponent.call(this);
        this.on('keyup', function(f, e){
            this.filter();
            if (this.getRawValue() == "") {
                this.triggers[0].hide();
            }
        }, this);
    },
    enableKeyEvents: true,
    trigger1Class: 'x-form-clear-trigger',
    hideTrigger1: true,
    hideTrigger2: true,
    width:180,
    hasSearch: false,

    // cancel
    onTrigger1Click: function(){
        if(this.hasSearch){
            this.reset();
            this.store.clearFilter();
            this.triggers[0].hide();
            this.hasSearch = false;
        }
    },

    // search
    filter: function(){
        var v = this.getRawValue().toUpperCase();
        this.store.filter([{
            fn: function(r) {
                var t = r.get('title'),
                    a = r.get('abstract');
                // TODO: improve matching ;-) (accents, spaces, special chars ...)
                return (t && t.toUpperCase().indexOf(v) > -1) ||
                    (a && a.toUpperCase().indexOf(v) > -1);
            }
        }]);
        this.hasSearch = true;
        this.triggers[0].show();
    }
});
