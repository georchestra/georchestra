/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GeoExt.data.CSW.js
 * @include OpenLayers/Format/CSWGetRecords/v2_0_2.js
 * @include OpenLayers/Filter/Comparison.js
 * @include GEOR_util.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

/** api: constructor
 *  .. class:: GEOR.CustomCSWRecordsReader(meta, recordType)
 *  
 *      :arg meta: ``Object`` Reader configuration.
 *      :arg recordType: ``Array or Ext.data.Record`` An array of field
 *          configuration objects or a record object.
 *
 *      Create a new custom reader object, which helps converting CSWRecords 
 *      to custom Ext.data.Records suitable for our custom Ext.Dataview
 *      
 */
GEOR.CustomCSWRecordsReader = function(meta, recordType) {
    meta = meta || {};
    if(!(recordType instanceof Function)) {
        recordType = Ext.data.Record.create([
            {name: "layer_name"},
            {name: "layer_description"},
            {name: "service_url"},
            {name: "md_uuid"},
            {name: "md_title"},
            {name: "md_abstract"},
            {name: "md_thumbnail_url"}
        ]);
    }
    GEOR.CustomCSWRecordsReader.superclass.constructor.call(
        this, meta, recordType);
};

Ext.extend(GEOR.CustomCSWRecordsReader, Ext.data.DataReader, {

    /** api: method[readRecords]
     *  :param rs: ``Array(GeoExt.data.CSWRecord)`` List of (csw)records for creating
     *      records.
     *  :return: ``Object``  An object with ``records`` and ``totalRecords``
     *      properties.
     *  
     *  From an array of ``GeoExt.data.CSWRecords`` objects create a data block
     *  containing :class:`Ext.data.Record` objects.
     */
    readRecords : function(rs) {
        var records = [];
        if(rs) {
            var recordType = this.recordType;
            var i, r, values, thumbnailURL;
            
            for(i = 0, lenI = rs.length; i < lenI; i++) {
                r = rs[i];
                thumbnailURL = null;

                if(r.get('URI')) {
                    // thumbnail URL (common to all layers in this MD):
                    Ext.each(r.get('URI'), function (item) {
                        if((item.name && item.name.toLowerCase() == "thumbnail") && item.value) {
                            thumbnailURL = item.value;
                        }
                    });
                    
                    // multiple WMS can be found in one csw:record
                    Ext.each(r.get('URI'), function (item) {
                        if((item.protocol == "OGC:WMS-1.1.1-http-get-map") &&
                            item.name && item.value) {

                            var tip = 'Couche '+item.name+' sur '+item.value;
                            var description = (item.description) ? 
                                '<span ext:qtip="'+tip+'">'+item.description+'</span>' : 
                                tip;
                                
                            values = {
                                "layer_name": item.name,
                                "layer_description": description,
                                "service_url": item.value,
                                "md_uuid": r.get('identifier'),
                                "md_title": r.get('title'),
                                "md_abstract": r.get('abstract'),
                                "md_thumbnail_url": thumbnailURL
                            };
                            records.push(
                                new recordType(values, r.get('identifier')+'_'+item.value+'_'+item.name)
                            );
                        }
                    }, this);
                }
            }
        }
        return {
            records: records,
            totalRecords: records.length
        };
    }
});


GEOR.cswquerier = (function() {

    /*
     * Private
     */
    
    /*
     * Ext.util.Observable for firing events
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: selectionchanged
         * Fires when the selection has changed
         *
         * Listener arguments:
         * records - {Array} array of selected records
         */
        "selectionchanged"
    );
    
    /**
     * Property: GeoExt.data.CSWRecordsStore
     * {GeoExt.data.CSWRecordsStore} store reading its records from the CSW service
     */
    var CSWRecordsStore;

    /**
     * Property: customStore
     * {Ext.data.Store} A store configured with a custom Reader.
     * This is the one that is bound to the Ext.Dataview
     */
    var customStore;
    
    /**
     * Property: mask
     * {Ext.LoadMask} the dataview mask
     */
    var mask;
    
    /**
     * Property: textField
     * {Ext.app.FreetextField} the freetext field
     */
    var textField;

    /**
     * Method: onCSWRecordsStoreLoad
     * Callback on CSWRecordsStore load event
     *
     * Parameters:
     * s - {GeoExt.data.CSWRecordsStore} the store
     * cswRecords - {Array(GeoExt.data.CSWRecord)} loaded records
     */
    var onCSWRecordsStoreLoad = function(s, cswRecords) { 
        // transfer results to customStore:
        customStore.loadData(cswRecords);
        // scroll dataview to top:
        var el = dataview.getEl();
        var f = el && el.first();
        f && f.scrollIntoView(dataview.container);
        // hide mask
        mask && mask.hide();
    };

    /**
     * Method: getTemplate
     * Creates the Ext.Dataview item template
     *
     * Returns:
     * {Ext.XTemplate}
     */
    var getTemplate = function() {
        var tpl = [
            '<tpl for=".">',
                '<div class="x-view-item">',
                    '<table><tr><td style="vertical-align:text-top;">',
                        '<p><b>{layer_description}</b></p>',
                        '<p>{md_title} - {[this.abstract(values.md_abstract)]}&nbsp;',
                        '<a href="{[this.metadataURL(values)]}" ext:qtip="Afficher la fiche de métadonnées dans une nouvelle fenêtre" ',
                        'target="_blank" onclick="window.open(this.href);return false;">plus</a></p>',
                    '</td><td width="190" style="text-align:center;">',
                        '<img src="{[this.thumbnailURL(values)]}" width="180" alt="lien imagette non valide" ',
                        'class="thumb" ext:qtip="Cliquez pour sélectionner ou désélectionner la couche"></img>',
                    '</td></tr></table>',
                '</div>',
            '</tpl>'
        ].join('');
        
        var context = {
            "metadataURL": function(values) {
                // this part is 100% geonetwork specific:
                var url = CSWRecordsStore.proxy.url;
                return url.replace('/csw', '/metadata.show?uuid='+values.md_uuid);
            },
            "thumbnailURL": function(values) {
                // this part is also 100% geonetwork specific:
                if (values.md_thumbnail_url) {
                    var url = CSWRecordsStore.proxy.url;
                    return url.replace('/csw', '/'+values.md_thumbnail_url);
                }
                return GEOR.config.NO_THUMBNAIL_IMAGE_URL;
            },
            "abstract": function(text) {
                // replace url links with <a href="XXX">lien</a>
                // (long links can break the dataview layout)
                var regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/gi;
                return text.replace(regexp,
                    '[<a href="$&" ext:qtip="Ouvrir l\'url $& dans une nouvelle fenêtre"' +
                    ' target="_blank" onclick="window.open(this.href);return false;">lien</a>]'
                );
            }
        };
        
        return new Ext.XTemplate(tpl, context);
    };
    
    /*
     * Public
     */
    return {
        /*
         * Observable object
         */
        events: observable,
        
        /**
         * APIMethod: getPanel
         * Return the panel for the CSW browser tab.
         *
         * Parameters:
         * options - {Object} options applied to panel
         * 
         * Returns:
         * {Ext.Panel}
         */
        getPanel: function(options) {
            
            if (!CSWRecordsStore) {
                CSWRecordsStore = new GeoExt.data.CSWRecordsStore({
                    url: GEOR.config.DEFAULT_CSW_URL,
                    listeners: {
                        "load": onCSWRecordsStoreLoad,
                        "beforeload": function() {
                            // remove selection:
                            observable.fireEvent("selectionchanged", []);
                            dataview.clearSelections();
                            // show mask:
                            mask && mask.show();
                        }, 
                        "exception": function() {
                            mask && mask.hide();
                        }
                    }
                });
                
                customStore = new Ext.data.Store({
                    // all the job -- converting + filtering records -- 
                    // is done in this custom reader:
                    reader: new GEOR.CustomCSWRecordsReader()
                });
                
                dataview = new Ext.DataView({
                    store: customStore,
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
                    tpl: getTemplate(),
                    listeners: {
                        "click": function(dv) {
                            var selectedRecords = dv.getSelectedRecords();
                            observable.fireEvent("selectionchanged", selectedRecords);
                        }
                    }
                });
                
                textField = new Ext.app.FreetextField({
                    store: CSWRecordsStore,
                    callback: function(r, options, success) {
                        if (!success) {
                            GEOR.util.errorDialog({
                                msg: "Serveur non disponible"
                            });
                            return;
                        }
                    }
                });
            }

            return new Ext.Panel(Ext.apply({
                title: "Catalogue",
                layout: 'border',
                items: [{
                    region: 'north',
                    layout: 'hbox',
                    layoutConfig: {
                        align: 'middle'
                    },
                    border: false,
                    height: 35,
                    bodyStyle: 'padding: 5px;',
                    defaults: {
                        border: false
                    },
                    items: [{
                        html: 'Chercher',
                        bodyStyle: 'padding: 0 10px 0 0;font: 12px tahoma,arial,helvetica,sans-serif;'
                    }, textField, {
                        html: 'dans',
                        bodyStyle: 'padding: 0 10px;font: 12px tahoma,arial,helvetica,sans-serif;'
                    }, {
                        xtype: 'combo',
                        store: new Ext.data.ArrayStore({
                            fields: ['url', 'name'],
                            data: GEOR.config.CATALOGS
                        }),
                        value: GEOR.config.DEFAULT_CSW_URL,
                        mode: 'local',
                        triggerAction: 'all',
                        editable: false,
                        valueField: 'url',
                        displayField: 'name',
                        width: 200,
                        tpl: new Ext.XTemplate(
                            '<tpl for=".">',
                                '<div ext:qtip="{url}" class="x-combo-list-item">{name}</div>',
                            '</tpl>'
                        ),
                        listeners: {
                            "select": function(cb, rec) {
                                CSWRecordsStore.proxy.setUrl(rec.get('url'), true);
                                // then trigger search, if first field has search.
                                if (textField.hasSearch) {
                                    textField.onTrigger2Click.call(textField);
                                }
                            }
                        }
                    }]
                }, {
                    region: 'center',
                    border: false,
                    autoScroll: true,
                    layout: 'fit',
                    items: [dataview],
                    listeners: {
                        "afterlayout": function() {
                            // defer is required to get correct mask position
                            if (!mask) {
                                (function() {
                                    mask = new Ext.LoadMask(this.getEl(), {
                                        msg: "chargement en cours"
                                    });
                                }).defer(1000, this);
                            }
                        }
                    }
                }]
            }, options));
        },
        
        /**
         * APIMethod: clearSelection
         * Clears the current record selection
         */
        clearSelection: function() {
            observable.fireEvent("selectionchanged", []);
            dataview && dataview.clearSelections();
        }
    };
})();


Ext.app.FreetextField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function() {
        Ext.app.OWSUrlField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e) {
            if (e.getKey() == e.ENTER) {
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent: false,
    validateOnBlur: false,
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-search-trigger',
    hideTrigger1: true,
    width: 180,
    hasSearch: false,
    paramName: 'query',
    
    cancelRequest: function() {
        var proxy = this.store.proxy;
        var conn = proxy.getConnection();
        if (conn.isLoading()) {
            conn.abort();
        }
        this.store.fireEvent("exception");
    },

    // reset
    onTrigger1Click: function() {
        this.cancelRequest();
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
        }
    },

    // search
    onTrigger2Click: function() {
        this.cancelRequest();
        
        this.store.load({
            params: {
                xmlData: new OpenLayers.Format.CSWGetRecords().write({
                    resultType: "results_with_summary",
                    Query: {
                        ElementSetName: {
                            value: "full"
                        },
                        Constraint: {
                            version: "1.1.0",
                            Filter: new OpenLayers.Filter.Comparison({
                                type: "~", 
                                property: "AnyText",
                                value: '*'+this.getValue()+'*'
                            })
                        },
                        SortBy: [{
                            property: "Relevance",
                            order: "DESC"
                        }]
                    },
                    startPosition: 1,
                    maxRecords: GEOR.config.MAX_CSW_RECORDS
                })
            },
            callback: this.callback,
            scope: this,
            add: false
        });

        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
    }
});