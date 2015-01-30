Ext.namespace("GEOR.Addons");

GEOR.Addons.Quicksearch = Ext.extend(GEOR.Addons.Base, {

    searches: null,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        if (!this.target) {
            alert("QuickSearch addon config error: requires a target property !");
            return;
        }
        // addon always placed in a toolbar
        
        // create all possible protocols.
        this.searches = [];
        var srs = this.map.getProjection();
        Ext.each(this.options.searches, function(search) {
            this.searches.push({
                filter: search.filter,
                pattern: search.pattern,
                template: search.template, // TO BE IMPROVED !!!
                protocol: new OpenLayers.Protocol.WFS({
                    url: search.service,
                    version: "1.1.0", // maximize compatibility but keep ability to reproject on the server
                    srsName: srs,
                    srsNameInQuery: true,
                    featureType: search.featureType,
                    featureNS: search.featureNS,
                    maxFeatures: 10,
                    defaultFilter: new OpenLayers.Filter.Comparison({
                        type: '~', // LIKE
                        property: search.field,
                        value: "",
                        // TODO: to be live updated:
                        //value: search.filter., //'*' + queryString.toUpperCase() + '*',
                        matchCase: false
                    })
                })
            });
        }, this);

        this.combo = new Ext.ux.form.TwinTriggerComboBox({
            xtype: 'combo',
            typeAhead: true,
            hideTrigger: true,
            selectOnFocus: true,
            mode: 'remote',
            minChars: 3,
            queryDelay: 50,
            tpl: new Ext.XTemplate(
                '<tpl for="."><div class="x-combo-list-item">',
                '{label}',
                '</div></tpl>'
            ),
            store: this.createStore(),
            displayField: 'label',
            emptyText: "Quick search...",
            triggerAction: 'all',
            trigger2Class: 'x-form-trigger-no-width x-hidden',
            trigger3Class: 'x-form-trigger-no-width x-hidden',
            listeners: {
                "render": function(c) {
                    new Ext.ToolTip({
                        target: c.getEl(),
                        trackMouse: true,
                        //dismissDelay: 
                        html: OpenLayers.i18n("addon_qs_qtip")
                    });
                }
            }
        });
        this.components = this.target.insertButton(this.position, ['-', this.combo, '-']);
        this.target.doLayout();
        
    },

    /**
     * Method: createStore
     *
     */
    createStore: function() {
        var store = new GeoExt.data.FeatureStore({
            
            proxy: new GeoExt.data.ProtocolProxy({
                protocol: this.searches[0].protocol
            })/*,
            
            reader: new cgxp.data.FeatureReader({
                format: new OpenLayers.Format.GeoJSON()
            }, ['label', 'layer_name']),
            sortInfo: {field: 'label', direction: 'ASC'} 
            */
        });

        store.on('beforeload', function(store, options) {
            // /([\d\.']+)[\s,]+([\d\.']+)/ 
            // coords = store.baseParams.query.match(
            
            var doQuery = false;
            Ext.each(this.searches, function(search) {
                var r = store.baseParams.query.match(search.pattern);
                if (r && r[1]) {
                    // we've got a match
                    var value = r[1];
                    doQuery = true;
                    search.protocol.defaultFilter.value = value + "*"; // FIXME: approximation grossière
                    store.proxy.protocol = search.protocol;
                    return false; // breaks the loop
                }
            });
            delete store.baseParams["query"];
            return doQuery;
        }, this);
        return store;
    }

});