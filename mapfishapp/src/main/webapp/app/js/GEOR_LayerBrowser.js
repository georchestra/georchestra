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
                /*,
                // problem with this approach is that HTTP errors are not silent ...
                
                "mouseenter": function(dv, idx, node, e) {
                    GEOR.ajaxglobal.disableAllErrors = true; // FIXME: awful hack
                    // idea: preload xml metadata (if any), parse it and enrich record with MD fields ? (cf caching)
                    // get WMS layer record
                    var record = dv.getRecord(node);
                    if (record.get("metadata").type !== "MD_Metadata") {
                        // get iso metadata url
                        // prefered url is the one pointing to the XML document:
                        var url = GEOR.util.getBestMetadataURL(record,
                            /^text\/xml|application\/xml$/, true);
                        console.log(url);
                        if (!url) {
                            return;
                        }
                        // fetch document
                        OpenLayers.Request.GET({
                            // TODO: silently fail
                            url: url,
                            success: function(response) {
                                GEOR.ajaxglobal.disableAllErrors = false;
                                var f = new OpenLayers.Format.CSWGetRecords();
                                // TODO: try / catch :
                                var o = f.read(response.responseXML || response.responseText);
                                console.log(o); // TODO: do not forget to commit fix in CSW 2.0.2 getrecords parser obj.records = obj.records || [];
                                if (o && o.records && o.records[0]) {
                                    record.set("metadata", o.records[0]);
                                }
                            },
                            failure: function() {
                                GEOR.ajaxglobal.disableAllErrors = false;
                            }
                        });
                    }
                },*/
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
            anchor: '95%',
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
            anchor: '95%',
            fieldLabel: tr("... or enter its address"),
            // TODO: improvement ... this callback should be set in GEOR_layerfinder.js rather than here ...
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
                // filter out records from services supporting only GET
                if (this.id == "WFS" && this.store.getCount() > 0) {
                    var supportsPOST = !!this.store.getAt(0).getLayer().protocol.url;
                    // check records do not have an undefined protocol url:
                    this.store.filterBy(function(r) {
                        return supportsPOST;
                    });
                    if (!supportsPOST) {
                        GEOR.util.infoDialog({
                           msg: tr("This server does not support HTTP POST")
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
        }, this.filterPanel];

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
                        '<p><b ext:qtip="{title}">{[this.title(values.title)]}</b></p>',
                        '<p style="padding-top: 2px">{[this.abstract(values.abstract)]}</p>',
                        '<p>{[this.mdlink(values)]}</p>',
                    '</td></tr></table>',
                '</div>',
            '</tpl>'
        ].join('');
        // Ideas to make it lighter :
        // - border-botttom: 1px thin line
        // - white background

        var context = {
            "title": function(t) {
                // shorten to 65 to take into account uppercased layer titles
                return GEOR.util.shorten(t, 65);
            },
            "queryable": function(values) {
                return values.queryable ?
                    '<div style="float:right;"><img src="'+GEOR.config.PATHNAME+'/app/img/famfamfam/information.png" ext:qtip="'+tr("Queryable")+'"/></div>' :
                    "";
            },
            "mdlink": function(values) {
                // prefered url is the one pointing at the XML document:
                var xmlurl = GEOR.util.getBestMetadataURL(values,
                    /^text\/xml|application\/xml$/, true);
                if (xmlurl) {
                    return [
                    '<a href="', xmlurl, '" ext:qtip="',
                        tr("Show metadata essentials in a window"), '" ',
                        'target="_blank" onclick="GEOR.util.mdwindow(this.href)">',
                        tr('metadata'), '</a>'
                    ].join('');
                }
                // if xmlurl is not available, use text/html metadata link:
                var htmlurl = GEOR.util.getBestMetadataURL(values, null, true);
                if (htmlurl) {
                    return [
                    '<a href="', htmlurl, '" ext:qtip="',
                        tr("Show metadata sheet in a new browser tab"), '" ',
                        'target="_blank" onclick="window.open(this.href);return false;">',
                        tr('metadata'), '</a>'
                    ].join('');
                }
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
                var haystack = GEOR.util.prepareString([
                    r.get('title'),
                    r.get('abstract'),
                    Ext.pluck(r.get('keywords'), "value").join('')
                ].join()),
                needle = GEOR.util.prepareString(v);
                return new RegExp(needle).test(haystack);
            }
        }]);
        this.hasSearch = true;
        this.triggers[0].show();
    }
});
