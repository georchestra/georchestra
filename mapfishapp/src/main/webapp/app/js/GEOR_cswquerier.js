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
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Filter/Spatial.js
 * @include GEOR_util.js
 * @include GEOR_helper.js
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
    if (!(recordType instanceof Function)) {
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
        if (rs) {
            var recordType = this.recordType;
            var i, r, values, thumbnailURL;

            for (i = 0, lenI = rs.length; i < lenI; i++) {
                r = rs[i];
                thumbnailURL = null, secondChoice = null;

                if (r.get('URI')) {
                    // thumbnail URL (common to all layers in this MD):
                    Ext.each(r.get('URI'), function (item) {
                        if (item.protocol && /image\/(png|jpg|jpeg|gif)/i.test(item.protocol) && item.value) {
                            if (item.name && item.name.toLowerCase() === "thumbnail") {
                                thumbnailURL = item.value;
                                return false; // stop looping (we found the best possible thumbnail)
                            } else {
                                secondChoice = item.value;
                            }
                        }
                    });
                    if (!thumbnailURL) {
                        thumbnailURL = secondChoice;
                    }

                    // multiple WMS can be found in one csw:record
                    Ext.each(r.get('URI'), function (item) {
                        if (GEOR.util.isSuitableDCProtocol(item)) {

                            var tip = OpenLayers.i18n("NAME layer on VALUE", {'NAME': item.name, 'VALUE': item.value});
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
     * Property: CSWRecordsStore
     * {GeoExt.data.CSWRecordsStore} store reading its records from the CSW service
     */
    var CSWRecordsStore;

    /**
     * Property: servicesStore
     * {GeoExt.data.CSWRecordsStore} store reading its records from the CSW service
     * dedicated to "plan B" ie: fetching the service metadata.
     */
    var servicesStore;

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
     * Property: southPanel
     * {Ext.Panel} the panel where the results count is displayed
     */
    var southPanel;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: uuidsToDig
     * {Array} a cache of csw records for "plan B"
     */
    var uuidsToDig = null;
    
    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;

    /**
     * Method: onServicesStoreLoad
     * Callback on ServicesStore load event
     *
     * Parameters:
     * s - {GeoExt.data.CSWRecordsStore} the store
     * serviceRecords - {Array(GeoExt.data.CSWRecord)} loaded records
     */
    var onServicesStoreLoad = function(s, serviceRecords) {
        // transfer results to customStore:
        Ext.each(uuidsToDig, function(o) {
            var uuid = o.uuid,
                record = o.record,
                md_title = o.record.get("title");
            Ext.each(serviceRecords, function(serviceRecord) {
                // check that the service has a correct service URL
                var serviceURL,
                    identificationInfo = serviceRecord.get("identificationInfo"),
                    operations = identificationInfo[0].containsOperations;
                Ext.each(operations, function(op) {
                    // get the WMS one, whose operationName is GetCapabilities
                    if (op.operationName.characterString == "GetCapabilities" &&
                        op.connectPoint[0] &&
                        op.connectPoint[0].linkage &&
                        GEOR.util.isUrl(op.connectPoint[0].linkage.URL, true) && // strict URL checking
                        op.connectPoint[0].protocol &&
                        op.connectPoint[0].protocol.characterString == "OGC:WMS") {
                        // get the service URL
                        serviceURL = op.connectPoint[0].linkage.URL;
                    }
                });
                // if no service end point, abort
                if (!serviceURL) {
                    return;
                }
                // get the SV_CoupledResource 
                // whose identifier matches the one found in the first query
                // and whose operationName is GetMap
                var resources = identificationInfo[0].coupledResource,
                    serviceTitle = identificationInfo[0].citation.title.characterString;
                Ext.each(resources, function(resource) {
                    if (resource.identifier.characterString == uuid &&
                        resource.operationName.characterString == "GetMap" &&
                        resource.scopedName) {
                        // enrich the original record with correct server name and layer name:
                        record.set("URI", record.get("URI").concat({
                            name: resource.scopedName, 
                            value: serviceURL,
                            description: md_title || OpenLayers.i18n("NAME layer on VALUE", {
                                'NAME': resource.scopedName,
                                'VALUE': serviceTitle || serviceURL
                            }),
                            protocol: "OGC:WMS"
                        }));
                        // feed the DataView (cumulatively adding the record):
                        customStore.loadData([record], true);
                    }
                });
            });
        });
    };

    /**
     * Method: onCSWRecordsStoreLoad
     * Callback on CSWRecordsStore load event
     *
     * Parameters:
     * s - {GeoExt.data.CSWRecordsStore} the store
     * cswRecords - {Array(GeoExt.data.CSWRecord)} loaded records
     */
    var onCSWRecordsStoreLoad = function(s, cswRecords) {
        uuidsToDig = [];
        // empty store:
        customStore.loadData([]);
        // transfer results to customStore:
        Ext.each(cswRecords, function(r) {
            var c = customStore.getCount();
            customStore.loadData([r], true); // cumulatively adding records
            if (customStore.getCount() == c) {
                // ... means that the record did not contain information about a linked WMS layer.
                // This is the place to hook into for "plan B" 
                // see https://github.com/georchestra/georchestra/issues/756
                uuidsToDig.push({
                    uuid: r.data.identifier,
                    record: r
                });
            }
        });

        if (uuidsToDig.length) {
            var filter, filters = [];
            if (uuidsToDig.length == 1) {
                filter = new OpenLayers.Filter.Comparison({
                    type: "==",
                    property: "operatesOnIdentifier",
                    value: uuidsToDig[0].uuid
                });
            } else {
                Ext.each(uuidsToDig, function(o) {
                    filters.push(new OpenLayers.Filter.Comparison({
                        type: "==",
                        property: "operatesOnIdentifier",
                        value: o.uuid
                    }));
                });
                filter = new OpenLayers.Filter.Logical({
                    type: "||",
                    filters: filters
                });
            }
            // TODO: use a GeoExt.data.ProtocolProxy for this store, 
            // with an OpenLayers.Protocol.CSW protocol
            // and a GeoExt.data.CSWRecordsReader reader
            servicesStore.load({
                params: {
                    xmlData: new OpenLayers.Format.CSWGetRecords().write({
                        resultType: "results",
                        outputSchema: "http://www.isotc211.org/2005/gmd", // to get ISO19139 XML
                        Query: {
                            ElementSetName: {
                                value: "full"
                            },
                            Constraint: {
                                version: "1.1.0",
                                Filter: new OpenLayers.Filter.Logical({
                                    type: "&&",
                                    filters: [
                                        new OpenLayers.Filter.Comparison({
                                            type: "==",
                                            property: "ServiceType",
                                            value: "view"
                                        }),
                                        filter
                                    ]
                                })
                            }
                        },
                        startPosition: 1
                    })
                },
                scope: this,
                add: false
            });
        }
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
                    '<table style="width:100%;"><tr><td style="vertical-align:text-top;">',
                        '<p><b>{layer_description}</b></p>',
                        '<p style="padding-top: 2px">{[this.abstract(values.md_abstract)]}&nbsp;',
                        '<a href="{[this.metadataURL(values)]}" ext:qtip="' +
                            tr("Show metadata sheet in a new window") + '" ',
                        'target="_blank" onclick="window.open(this.href);return false;">' +
                            tr('more') + '</a></p>',

                    '</td><td width="190" style="text-align:center;" ext:qtip="'+tr("Clic to select or deselect the layer")+'">',
                        // tried with the "html only" solution provided on
                        // http://stackoverflow.com/questions/980855/inputting-a-default-image-in-case-the-src-arribute-of-an-html-img-is-not-valid
                        // but the headers sent by GN are incorrect for the image to display as an HTML object tag
                        '<img src="{[this.thumbnailURL(values)]}" class="thumb" onerror="this.src=\'',GEOR.config.PATHNAME,'/app/img/broken.png\';"/>',
                    '</td></tr></table>',
                '</div>',
            '</tpl>'
        ].join('');

        var context = {
            "metadataURL": function(values) {
                // this part is 100% geonetwork specific:
                var url = CSWRecordsStore.proxy.url;
                // replace /srv/*/csw with /?uuid=
                return url.replace(/\/srv\/(\S+)\/csw/, '/?uuid='+values.md_uuid);
            },
            "thumbnailURL": function(values) {
                if (values.md_thumbnail_url) {
                    if (GEOR.util.isUrl(values.md_thumbnail_url)) {
                        // full thumbnail URL, yeah !
                        return values.md_thumbnail_url;
                    }
                    // incomplete thumbnail URL, we're trying to guess it.
                    // this part is 100% geonetwork specific:
                    var url = CSWRecordsStore.proxy.url;
                    return url.replace('/csw', '/'+values.md_thumbnail_url);
                }
                // no thumbnail URL:
                return GEOR.util.getValidURI(GEOR.config.NO_THUMBNAIL_IMAGE_URL);
            },
            "abstract": function(text) {
                // two things here:
                // 1) shorten text
                // 2) replace url links with <a href="XXX">lien</a>
                //    (long links can break the dataview layout)
                text = GEOR.util.shorten(text, 400);
                var regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/gi; // TODO: factorize this regexp in GEOR.util
                return text.replace(regexp,
                    '[<a href="$&" ext:qtip="'+
                        tr("Open the URL url in a new window", {'URL': '$&'})
                        +'"' +
                    ' target="_blank" onclick="window.open(this.href);return false;">lien</a>]'
                );
            }
        };

        return new Ext.XTemplate(tpl, context);
    };

    /**
     * Method: onCustomStoreLoad
     * Callback on CustomStore load event
     *
     * Parameters:
     * s - {Ext.data.Store} the store
     */
    var onCustomStoreLoad = function(s) {
        var text,
            mdCount = CSWRecordsStore.getCount(),
            wmsCount = s.getCount();

        if (mdCount) {
            if (mdCount > 1) {
                text = tr("NB metadata match the query.", {'NB': mdCount});
            } else {
                text = tr("A single metadata matches the query.");
            }
            text += " ";
            if (wmsCount == 0) {
                text += tr("No linked layer.");
            } else if (wmsCount == 1) {
                text += tr("One layer found.");
            } else {
                text += tr("NB layers found.", {'NB': wmsCount});
            }

            // a better indicator would be numberOfRecordsMatched > numberOfRecordsReturned
            // but it is more difficult to obtain than mdCount.
            // For the moment, we'll use this criteria:
            if (mdCount == GEOR.config.MAX_CSW_RECORDS) {
                text += " " + tr("Precise your request.");
            }
        } else {
            text = tr("No metadata matches the query.");
        }
        southPanel.body.dom.innerHTML = "<p>"+text+"</p>";
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
         * APIMethod: init
         * Initialize this module 
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */
        init: function(m) { 
            map = m;
        },

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
                tr = OpenLayers.i18n;

                CSWRecordsStore = new GeoExt.data.CSWRecordsStore({
                    url: GEOR.config.DEFAULT_CSW_URL,
                    fields: [
                        {name: "identifier"},
                        {name: "title"},
                        {name: "URI"},
                        {name: "abstract"},
                        {name: "BoundingBox", mapping: "bounds"}
                    ],
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

                servicesStore = new GeoExt.data.CSWRecordsStore({
                    url: GEOR.config.DEFAULT_CSW_URL,
                    fields: [
                        {name: "identifier", mapping: "fileIdentifier"},
                        {name: "identificationInfo"}
                    ],
                    listeners: {
                        "load": onServicesStoreLoad
                    }
                });

                customStore = new Ext.data.Store({
                    // all the job -- converting + filtering records --
                    // is done in this custom reader:
                    reader: new GEOR.CustomCSWRecordsReader(),
                    listeners: {
                        "load": onCustomStoreLoad
                    }
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
                    map: map,
                    store: CSWRecordsStore,
                    callback: function(r, options, success) {
                        if (!success) {
                            GEOR.util.errorDialog({
                                msg: tr("Unreachable server")
                            });
                            return;
                        }
                    }
                });

                southPanel = new Ext.Panel({
                    region: 'south',
                    border: false,
                    height: 25,
                    bodyStyle: 'padding: 5px;font: 11px tahoma,arial,helvetica,sans-serif;',
                    html: "<p></p>"
                });
            }

            return new Ext.Panel(Ext.apply({
                title: tr("Catalogue"),
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
                        html: tr("Find"),
                        bodyStyle: 'padding: 0 10px 0 0;font: 12px tahoma,arial,helvetica,sans-serif;'
                    }, textField, {
                        html: tr("in"),
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
                                servicesStore.proxy.setUrl(rec.get('url'), true);
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
                                        msg: tr("Loading...")
                                    });
                                }).defer(1000, this);
                            }
                        }
                    }
                }, southPanel]
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
        this.on('focus', function() {
            GEOR.helper.msg(
                OpenLayers.i18n("cswquerier.help.title"), 
                OpenLayers.i18n("cswquerier.help.message"),
                10
            );
        }, this);
    },

    validationEvent: false,
    validateOnBlur: false,
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-search-trigger',
    hideTrigger1: true,
    width: 300,
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

    createFilter: function() {
        // see http://osgeo-org.1560.n6.nabble.com/CSW-GetRecords-problem-with-spaces-tp3862749p3862750.html
        var v = this.getValue(),
            words = v.replace(GEOR.util.specialCharsRegExp, ' ').split(' '),
            // data type filters
            // improve relevance of results: (might not be relevant with other csw servers than geonetwork)
            byTypes = new OpenLayers.Filter.Logical({
                type: "&&",
                filters: [
                    // do not request dc:type = service, just dc:type = dataset OR series
                    new OpenLayers.Filter.Logical({
                        type: "||",
                        filters: [
                            new OpenLayers.Filter.Comparison({
                                type: "~",
                                property: "Type",
                                value: 'dataset'
                            }),
                            new OpenLayers.Filter.Comparison({
                                type: "~",
                                property: "Type",
                                value: 'series'
                            })
                        ]
                    })
                ]
            }),
            // exact match filters
            // to return direct hits on same id, titles
            byIds =  new OpenLayers.Filter.Logical({
                type: "||",
                filters: [
                    new OpenLayers.Filter.Comparison({
                        type: "==",
                        property: "Title",
                        value: v
                    }),
                    new OpenLayers.Filter.Comparison({
                        type: "==",
                        property: "AlternateTitle",
                        value: v
                    }),
                    new OpenLayers.Filter.Comparison({
                        type: "==",
                        property: "Identifier",
                        value: v
                    }),
                    new OpenLayers.Filter.Comparison({
                        type: "==",
                        property: "ResourceIdentifier",
                        value: v
                    })
                ]
            }),
            // word filters
            byWords = [],
            // final filters, and logical operator
            finalFilters = [];

            
        Ext.each(words, function(word) {
            if (word) {
                // #word : search in keywords, use _ for space
                if (/^#.+$/.test(word)) {
                    byWords.push(
                        new OpenLayers.Filter.Comparison({
                            type: "==",
                            property: "Subject",
                            value: word.replace("_", " ").substr(1),
                            matchCase: false
                        })
                    );
                }
                // @word : search for organizations, use _ for space
                else if (/^@.+$/.test(word)) {
                    byWords.push(
                        new OpenLayers.Filter.Comparison({
                            type: "==",
                            property: "OrganisationName",
                            value: word.replace("_", " ").substr(1),
                            matchCase: false
                        })
                    );
                }
                // -word : exclude a specific word
                else if (/^-.+$/.test(word)) {
                    byWords.push(
                        new OpenLayers.Filter.Logical({
                            type: "!",
                            filters: [
                                new OpenLayers.Filter.Comparison({
                                    type: '~',
                                    property: "AnyText",
                                    value: "*" + word.substr(1) + "*",
                                    matchCase: false
                                })
                            ]
                        })
                    );
                }
                // ?word : AnyText search
                else if (/^\?.+$/.test(word)) {
                    byWords.push(
                        new OpenLayers.Filter.Comparison({
                            type: "~",
                            property: "AnyText",
                            value: word.substr(1) + "*",
                            matchCase: false
                        })
                    );
                }
                // word : search for hits on any property defined in CSW_FILTER_PROPERTIES
                else if (word !== "") {
                    var byWordProp = [];
                    Ext.each(GEOR.config.CSW_FILTER_PROPERTIES, function(property) {
                        byWordProp.push(
                            new OpenLayers.Filter.Comparison({
                                type: '~',
                                property: property,
                                value: word + '*',
                                matchCase: false
                            })
                        );
                    });
                    byWords.push(
                        new OpenLayers.Filter.Logical({
                            type: "||",
                            filters: byWordProp
                        })
                    );
                }
            }
        });

        // combine all filters alltogether
        finalFilters.push(byTypes);
        
        // spatial filter
        if (GEOR.config.CSW_FILTER_SPATIAL.length!==4) {
            finalFilters.push(new OpenLayers.Filter.Spatial({
                    type: OpenLayers.Filter.Spatial.BBOX,
                    value: this.map.getExtent().clone()
                        .transform(this.map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"))
                })
            );
        }
        else {
            finalFilters.push(new OpenLayers.Filter.Spatial({
                    type: OpenLayers.Filter.Spatial.BBOX,
                    value: new OpenLayers.Bounds(GEOR.config.CSW_FILTER_SPATIAL)
                })
            );
        }

        if (byWords.length > 0) {
            finalFilters.push(new OpenLayers.Filter.Logical({
                    type: "||",
                    filters: [
                        // exact matches
                        byIds,
                        // word matches
                        new OpenLayers.Filter.Logical({
                            type: "&&",
                            filters: byWords
                        })
                    ]
                })
            );
        }

        return new OpenLayers.Filter.Logical({
            type: "&&",
            filters: finalFilters
        });

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
                            Filter: this.createFilter()
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
