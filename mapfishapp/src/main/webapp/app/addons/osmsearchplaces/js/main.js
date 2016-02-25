/*
 * @include OpenLayers/Format/WKT.js
*/

Ext.namespace("GEOR.Addons");

GEOR.Addons.OSMSearchPlaces = Ext.extend(GEOR.Addons.Base, {
    win: null,
    addressField: null,
    layer: null,
    popup: null,
    _requestCount: 0,

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        if (!this.target) {
            alert("Osmsearchplaces addon config error: requires a target property !");
            return;
        }
        	
        this.layer = new OpenLayers.Layer.Vector("__georchestra_osmsearchplaces", {
            displayInLayerSwitcher: false
        });
        this.map.addLayer(this.layer);
        this.addressField = this._createCbSearch();
        
        // create a button to be inserted in toolbar:
        this.components = this.target.insertButton(this.position, [
            '-', this.addressField, '-'
        ]);
        this.target.doLayout();
    },
	
    _computeQuery: function(query) {
        var computedQuery = '';
        for (var i = 0, len = query.length; i < len; i++) {
            var letter = query[i];
            computedQuery += '[';
            computedQuery += query[i].toUpperCase();
            computedQuery += query[i];
            if( query[i] == 'a' || query[i] == 'A' ) {
                computedQuery += 'ÂÃÄÀÁÅàáâãäå';
            }
            if( query[i] == 'e' || query[i] == 'E' ) {
                computedQuery += 'ÈÉÊËèéêë';
            }
            if( query[i] == 'i' || query[i] == 'I' ) {
                computedQuery += 'ÌÍÎÏìíîï';
            }
            if( query[i] == 'o' || query[i] == 'O' ) {
                computedQuery += 'ÒÓÔÕÖØòóôõöø';
            }
            if( query[i] == 'u' || query[i] == 'U' ) {
                computedQuery += 'ÙÚÛÜùúûü';
            }
            if( query[i] == 'y' || query[i] == 'Y' ) {
                computedQuery += 'Ýý';
            }
            if( query[i] == 'n' || query[i] == 'N' ) {
                computedQuery += 'Ññ';
            }
            if( query[i] == ' ' ) {
                computedQuery += ' -';
            }
            computedQuery += ']';
        }
        return computedQuery;
    },
    
    _refreshCbValues: function(event) {
        var comboBox = this.addressField;
        comboBox.collapse();
        
        if( event.query.length < event.combo.minChars ) {
            return false;
        }
        
        var overpassQuery = 'way["name"~"'+this._computeQuery(event.query)+'"]';
        overpassQuery += '('+this.options.boundingbox.minlat+','+this.options.boundingbox.minlon+','
            +this.options.boundingbox.maxlat+','+this.options.boundingbox.maxlon+');';
        overpassQuery += 'out tags '+this.options.limit+';';
        
        var store = new Ext.data.ArrayStore({fields: ['display', 'geometry'], idIndex: 0, autoLoad: false});
        comboBox.bindStore(store);
        comboBox.expand();
        comboBox.onBeforeLoad();
        
        Ext.Ajax.request({
            url: this.options.overpassURL,
            params: {
                data: overpassQuery
            },
            scope: this,
            success: function(respon,opt){
                var waysList = respon.responseXML.getElementsByTagName("way"), searchTerms = [];
                for (var i = 0; i < waysList.length; i++) {
                    var nameTags = waysList[i].querySelectorAll("tag[k=name]");
                    if( nameTags.length > 0 ) {
                        var name = nameTags[0].getAttribute("v");
                        if( searchTerms.indexOf(name) == -1 ) {
                            searchTerms.push(name);
                        }
                    }
                }
                
                var requestCount = searchTerms.length, requestComplete = 0, searchResults = [];
                var onRequestComplete = function(scope) {
                    requestComplete++;
                    if (requestComplete >= requestCount) {
                        var POIRecord = Ext.data.Record.create([
                            {name: 'display', mapping: 'display', allowBlank: false},
                            {name: 'geometry', mapping: 'geometry', allowBlank: false}
                        ]);
                        			
                        for(var i = 0; i < searchResults.length; i++) {
                            var poi = new POIRecord({
                                display: searchResults[i].getAttribute("display_name").split(scope.options.cutFrom)[0],
                                geometry: searchResults[i].getAttribute("geotext")
                            });
                            store.add(poi);
                        }
                        store.commitChanges();
                        store.fireEvent('load',store);
                    }
                };
                
                for (var i  = 0; i < searchTerms.length; i++) {
                    Ext.Ajax.request({
                        url: this.options.nominatimURL,
                        params: {
                            addressdetails: 1,
                            polygon_text: 1,
                            format: 'xml',
                            q: searchTerms[i],
                            bounded: 1,
                            viewbox: this.options.boundingbox.minlon+','+this.options.boundingbox.maxlat+
                                ','+this.options.boundingbox.maxlon+','+this.options.boundingbox.minlat
                        },
                        method: 'GET',
                        scope: this,
                        success: function(respon,opt){
                            for(var i = 0; i < respon.responseXML.getElementsByTagName("place").length; i++) {
                                searchResults.push(respon.responseXML.getElementsByTagName("place")[i]);
                            }
                            onRequestComplete(this);
                        },
                        failure: function(resp,opt) {
                            comboBox.collapse();
                        }
                    });
                }
            },
            failure: function(resp,opt) {
                comboBox.collapse();
            },
            method: 'POST'
        });
        
        return false;
    },

    /*
     * Method: _createCbSearch
     * Returns: {Ext.form.ComboBox}
     */
    _createCbSearch: function() {
        tplResult = new Ext.XTemplate(
            '<tpl for="."><div class="x-combo-list-item" ext:qtip="'+this.options.comboTemplate+'">',
            this.options.comboTemplate,
            '</div></tpl>'
        );
        
        return new Ext.form.ComboBox({
            name: "address",
            width: 350,
            emptyText: OpenLayers.i18n('osmsearchplaces.field_emptytext'),
            fieldLabel: OpenLayers.i18n('osmsearchplaces.field_label'),
            loadingText: OpenLayers.i18n('Loading...'),
            queryDelay: 100,
            hideTrigger: true,
            selectOnFocus: true,
            tpl: tplResult,
            queryParam: 'query',
            minChars: 3,
            pageSize: 0,
            autoScroll: true,
            listeners: {
                "select": this._onComboSelect,
                "beforequery": this._refreshCbValues,
                scope: this
            }
        });
    },
	
    _onComboSelect: function(combo, record) {
        this.layer.destroyFeatures();
        this.popup && this.popup.destroy();
        var bbox, srcFeature, destGeom, destFeature,
            from = new OpenLayers.Projection("EPSG:4326"),
            to = new OpenLayers.Projection(this.map.getProjection());
        
        if (!record.get("geometry")) {
            return;
        }
		
        srcFeature = new OpenLayers.Format.WKT().read(record.get("geometry"));
        destGeom = srcFeature.geometry.transform(from,to);
        destFeature = new OpenLayers.Feature.Vector(destGeom);
        
        this.map.zoomToExtent(destGeom.getBounds());
        if( this.map.getZoomForExtent(destGeom.getBounds()) > 22 ) {
            this.map.zoomTo(22);
        }
        
        this.layer.addFeatures([destFeature]);
        this.popup = new GeoExt.Popup({
            location: destFeature,
            width: 300,
            map: this.map,
            html: new Ext.XTemplate(
                    '<div class="x-combo-list-item">',
                    this.options.comboTemplate,
                    '</div>'
                ).apply(record.data),
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
     * Method: showWindow
     */
    showWindow: function() {
        this.win.show();
        this.win.alignTo(
            Ext.get(this.map.div),
            "t-t",
            [0, 5],
            true
        );
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        this.win.hide();
        this.popup && this.popup.destroy();
        this.popup = null;
        this.layer = null;
        
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
