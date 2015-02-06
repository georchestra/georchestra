Ext.namespace("GEOR.Addons");

GEOR.Addons.Quicksearch = Ext.extend(GEOR.Addons.Base, {

    _searches: null,

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
        // addon is always placed in a toolbar
        var tr = OpenLayers.i18n;
        // create all possible protocols:
        this._searches = [];
        Ext.each(this.options.searches, function(search) {
            this._searches.push({
                pattern: search.pattern,
                protocol: GEOR.ows.WFSProtocol({
                    typeName: search.featureType,
                    featureNS: search.featureNS,
                    owsURL: search.service,
                    maxFeatures: this.options.maxResults,
                    WFSversion: "1.1.0"
                }, this.map)
            });
        }, this);

        this.layer = new OpenLayers.Layer.Vector("__georchestra_qs", {
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                "default": this.options.graphicStyle
            })
        });
        this.map.addLayer(this.layer);

        this.combo = new Ext.form.ComboBox({
            hideTrigger: true,
            selectOnFocus: true,
            mode: 'remote',
            loadingText: tr('Loading...'),
            minChars: this.options.minChars,
            queryDelay: 100,
            tpl: new Ext.XTemplate(
                '<tpl for="."><div class="x-combo-list-item" ext:qtip="{values.feature.attributes.'+this.options.field+'}">',
                '{values.feature.attributes.'+this.options.field+'}', // it is not possible to dynamically switch template ...
                '</div></tpl>'
            ),
            store: this._createStore(),
            emptyText: tr("addon_qs_emptyText"),
            triggerAction: 'all',
            listeners: {
                "render": function(c) {
                    new Ext.ToolTip({
                        target: c.getEl(),
                        trackMouse: true,
                        html: this.options.tip[this.lang]
                    });
                },
                "select": this._onComboSelect,
                scope: this
            }
        });
        this.components = this.target.insertButton(this.position, [
            '-', this.combo, '-'
        ]);
        this.target.doLayout();
    },

    /**
     * Method: _createStore
     *
     */
    _createStore: function() {
        return new GeoExt.data.FeatureStore({
            proxy: new GeoExt.data.ProtocolProxy({
                protocol: this._searches[0].protocol
            }),
            listeners: {
                'beforeload': this._onBeforeLoad,
                scope: this
            }
        });
    },

    /**
     * Method: _onBeforeLoad
     *
     */
    _onBeforeLoad: function(store, options) {
        var doQuery = false;
        Ext.each(this._searches, function(search) {
            if (!search.pattern) {
                return;
            }
            var r = store.baseParams.query.match(search.pattern);
            if (r && r[1]) {
                if (!search.protocol.url) {
                    alert("Your query for "+r[1]+" matched a rule with no configured service.");
                    return false;
                }
                // we've got a match
                doQuery = true;
                search.protocol.defaultFilter = this._createFilter(r[1]);
                store.proxy.protocol = search.protocol;
                return false; // breaks the loop
            }
        }, this);
        var elseSearch = this._searches[this._searches.length-1]; // TODO: improve so that no need for it to be the last filter
        if (doQuery == false && !elseSearch.pattern) {
            // means we have tested the input against all rules
            // and that the last rule has no pattern
            // => use the last rule
            doQuery = true;
            elseSearch.protocol.defaultFilter = this._createFilter(store.baseParams.query);
            store.proxy.protocol = elseSearch.protocol;
        }
        delete store.baseParams["query"];
        return doQuery;
    },

    /**
     * Method: _createFilter
     *
     */
    _createFilter: function(value) {
        return new OpenLayers.Filter.Comparison({
            type: '~', // = LIKE
            property: this.options.field, // COMMON to all queried layers :-(
            value: value + "*",
            matchCase: false
        });
    },

    /**
     * Method: _onComboSelect
     * Callback on combo selected
     */
    _onComboSelect: function(combo, record) {
        this.layer.destroyFeatures();
        this.popup && this.popup.destroy();
        var feature = record.getFeature().clone();
        if (!feature.geometry) {
            return;
        }
        this.map.setCenter(feature.geometry.getBounds().getCenterLonLat());
        this.layer.addFeatures([feature]);
        this.popup = new GeoExt.Popup({
            location: feature,
            width: 300,
            html: feature.attributes[this.options.field],
            anchorPosition: "top-left",
            bodyStyle: "padding: 5px;",
            collapsible: false,
            closable: true,
            closeAction: "hide",
            unpinnable: true,
            listeners: {
                "hide": function() {
                    this.layer.destroyFeatures();
                },
                scope: this
            },
            buttons: [{
                text: tr("zoom"),
                handler: function() {
                    this.map.zoomToExtent(this.layer.getDataExtent());
                },
                scope: this
            }]
        });
        this.popup.show();
    },

    /**
     * Method: destroy
     * 
     */
    destroy: function() {
        this.popup && this.popup.destroy();
        this.popup = null;
        this.map.removeLayer(this.layer);
        this.layer = null;

        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});