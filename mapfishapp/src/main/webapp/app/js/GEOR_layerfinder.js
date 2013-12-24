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
 * @include GEOR_config.js
 * @include GEOR_util.js
 * @include GEOR_cswquerier.js
 * @include GEOR_cswbrowser.js
 * @include GEOR_LayerBrowser.js
 * @include GEOR_ows.js
 * @include OpenLayers/Strategy/Fixed.js
 * @include GEOR_fileupload.js
 */

Ext.namespace("GEOR");

GEOR.layerfinder = (function() {

    /*
     * Private
     */

    /**
     * Property: layerStore
     * {GeoExt.data.LayerStore} a reference to the application layer store
     */
    var layerStore = null;

    /**
     * Property: currentTab
     * {String} a local cache of the currently active tab
     * (one of "cswquerier", "cswbrowser", "wms", "wfs", "file")
     */
    var currentTab = "cswquerier";

    /**
     * Property: panels
     * {Object} referencing the panels in the tabPanel.
     */
    var panels = {
        "cswquerier": null,
        "cswbrowser": null,
        "wmts": null,
        "wms": null,
        "wfs": null,
        "file": null
    };

    /**
     * Property: selectedRecords
     * {Object} referencing the records selected in each panel
     */
    var selectedRecords = {
        "cswquerier": [],
        "cswbrowser": [],
        "wmts": [],
        "wms": [],
        "wfs": [],
        "file": []
    };

    /**
     * Property: addButton
     * {Ext.Button} a reference to the "add" button
     */
    var addButton = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: createTabPanel
     * Return the main tab panel.
     *
     * Returns:
     * {Ext.TabPanel}
     */
    var createTabPanel = function() {

        var selectionChangedListener = function(tab) {
            return function(records) {
                selectedRecords[tab] = records;
                if (records.length) {
                    addButton.enable();
                    //addButton.getEl().parent().highlight();
                    addButton.setText(tr("Add")+ ' ('+records.length+')');
                } else {
                    addButton.setText(tr("Add"));
                    addButton.disable();
                }
            };
        };
        GEOR.cswbrowser.events.on({
            "selectionchanged": selectionChangedListener.call(this, "cswbrowser")
        });
        GEOR.cswquerier.events.on({
            "selectionchanged": selectionChangedListener.call(this, "cswquerier")
        });
        GEOR.fileupload.events.on({
            "selectionchanged": selectionChangedListener.call(this, "file")
        });


        panels["cswquerier"] = GEOR.cswquerier.getPanel({
            tabTip: tr("Find layers searching in metadata")
        });
        panels["cswbrowser"] = GEOR.cswbrowser.getPanel({
            tabTip: tr("Find layers from keywords")
        });

        var r = function(val) {
            return (val ? '<img src="app/img/famfamfam/tick.gif" alt="' + tr("Yes") + '">' : 
                '<img src="app/img/nope.gif" alt="' + tr("No") + '">');
        };

        var mapSRS = layerStore.map.getProjection(),
        commonStoreOptions = {
            // url should not be empty unless we want the following
            // exception to occur:
            // uncaught exception: Ext.data.DataProxy: DataProxy attempted
            // to execute an API-action but found an undefined url /
            // function. Please review your Proxy url/api-configuration.
            url: "/dummy",
            sortInfo: {
                field: 'title',
                direction: 'ASC'
            }
        },
        layerColumn = {
            header: tr("Layer"), 
            dataIndex: "title", 
            sortable: true, 
            width: 200
        },
        descriptionColumn = {
            id: "description", 
            header: tr("Description"), 
            dataIndex: "abstract"
        },
        queryableColumn = {
            id: "queryable", 
            header: tr("Queryable"), 
            dataIndex: "queryable", 
            sortable: true, 
            width: 75,
            align: "center",
            renderer: r
        };

        // WMTS
        panels["wmts"] = new GEOR.LayerBrowser({
            title: tr("WMTS server"),
            tabTip: tr("Find layers querying WMTS servers"),
            fieldLabel: tr("Choose a WMTS server: "),
            store: new GEOR.ows.WMTSCapabilities({
                mapSRS: mapSRS,
                storeOptions: commonStoreOptions
            }),
            servers: GEOR.config.WMTS_SERVERS,
            columns: [
                layerColumn,
                queryableColumn,
                descriptionColumn
            ],
            listeners: {
                "selectionchanged": selectionChangedListener.call(this, "wmts")
            }
        });

        // WMS
        panels["wms"] = new GEOR.LayerBrowser({
            title: tr("WMS server"),
            tabTip: tr("Find layers querying WMS servers"),
            fieldLabel: tr("Choose a WMS server: "),
            store: new GEOR.ows.WMSCapabilities({
                storeOptions: commonStoreOptions
            }),
            servers: GEOR.config.WMS_SERVERS,
            mapSRS: mapSRS,
            columns: [
                layerColumn,
                queryableColumn,
                {id: "opaque", header: tr("Opaque"), dataIndex: "opaque", sortable: true, width: 50, align: "center", renderer: r},
                descriptionColumn
            ],
            listeners: {
                "selectionchanged": selectionChangedListener.call(this, "wms")
            }
        });

        
        // WFS
        panels["wfs"] = new GEOR.LayerBrowser({
            title: tr("WFS server"),
            tabTip: tr("Find layers querying WFS servers"),
            fieldLabel: tr("Choose a WFS server: "),
            store: new GEOR.ows.WFSCapabilities({
                storeOptions: Ext.apply({
                    layerOptions: function() {
	                    return {
	                        visibility: true,
	                        displayInLayerSwitcher: true,
                            styleMap: GEOR.util.getStyleMap(),
                            rendererOptions: {
                                zIndexing: true
                            },
	                        strategies: [
                                new OpenLayers.Strategy.Fixed()
                            ]
                            //,renderers: ["Canvas"] // in order to load many features
	                    };
	                },
                    protocolOptions: {
                        //autoDestroy: false, // TEST (seems not to work as expected)

                        // we need to set the srsName in the WFS query,
                        // so that features are returned in the correct SRS.
                        // Please note that, with WFS 1.0.0, the trick should
                        // only work with GeoServer:
                        srsNameInQuery: true,
                        srsName: mapSRS
                        // Note: the geometry name will be set later on:
                        // See http://applis-bretagne.fr/redmine/issues/2145
                        // and describeFeaturetypeSuccess() in GEOR_layerfinder.js

                        // TODO: MapServer >= 5.6 requires that all propertyNames
                        // are listed here, if we want to get the geometry.
                        // This requires that we do a WFS DescribeFeatureType
                        // and amend the protocol once we get the response.
                        // see http://applis-bretagne.fr/redmine/issues/1996

                        // I think this will be done as a consequence of
                        // http://applis-bretagne.fr/redmine/issues/1984 :
                        // geometryName and propertyNames should be cached
                        // in the layerStore for future use, after GetCap &
                        // DescribeFeatureType responses are parsed.
                    }
                }, commonStoreOptions)
            }),
            servers: GEOR.config.WFS_SERVERS,
            columns: [
                layerColumn,
                descriptionColumn
            ],
            listeners: {
                "selectionchanged": selectionChangedListener.call(this, "wfs")
            }
        });
        panels["file"] = GEOR.fileupload.getPanel({
            srs: mapSRS,
            tabTip: tr("Add layers from local files")
        });

        return new Ext.TabPanel({
            border: false,
            activeTab: 0,
            // required for WMS & WFS panels to have correct layout:
            deferredRender: true,
            items: [
                panels["cswquerier"], panels["cswbrowser"], 
                panels["wmts"], panels["wms"], panels["wfs"], 
                panels["file"]
            ],
            listeners: {
                'tabchange': function (tp, p) {
                    switch (p) {
                    case panels["cswquerier"]:
                        currentTab = "cswquerier";
                        break;
                    case panels["cswbrowser"]:
                        currentTab = "cswbrowser";
                        break;
                    case panels["wmts"]:
                        currentTab = "wmts";
                        break;
                    case panels["wms"]:
                        currentTab = "wms";
                        break;
                    case panels["wfs"]:
                        currentTab = "wfs";
                        break;
                    case panels["file"]:
                        currentTab = "file";
                        break;
                    }
                    if (selectedRecords[currentTab].length>0) {
                        addButton.enable();
                        addButton.setText(tr("Add")+' ('+selectedRecords[currentTab].length+')');
                        //addButton.getEl().parent().highlight();
                    } else {
                        addButton.setText(tr("Add"));
                        addButton.disable();
                    }
                }
            }
        });
    };

    /**
     * Method: capabilitiesSuccess
     * Success callback for the WMS capabilities request issued
     * when adding layers from the catalog tab
     *
     * Parameters:
     * record - {GeoExt.data.LayerRecord}
     */
    // TODO : factorize & centralize this code on layer added in application layerStore
    var capabilitiesSuccess = function(record) {
        var data = record.data;
        var layerName = record.get('layer_name');
        return function(store, records) {
            var index = store.find("name", layerName);
            if (index < 0) {
                GEOR.util.errorDialog({
                    msg: tr("layerfinder.layer.unavailable",
                        {'NAME': layerName}
                    )
                });
                return;
            }
            var r = records[index];
            var srs = layerStore.map.getProjection();
            if (!r.get('srs') || (r.get('srs')[srs] !== true)) {
                GEOR.util.errorDialog({
                    msg: tr("Layer projection is not compatible")
                });
                return;
            }
            // Set the copyright information to the "attribution" field
            if (data.rights && !r.get("attribution")) {
                r.set("attribution", {title: data.rights});
            }
            // If we have a metadataURL coming from the catalog,
            // we use it instead of the one we get from the capabilities
            // (as asked by Lydie - see http://applis-bretagne.fr/redmine/issues/1599#note-5)
            if (data.metadataURL) {
                r.set("metadataURLs", [data.metadataURL]);
            }
            layerStore.addSorted(r);
        };
    };

    /**
     * Method: describeFeaturetypeSuccess
     * Success callback for the WFS DescribeFeaturetype request issued
     * when adding layers from the "WFS layers" tab
     *
     * Parameters:
     * record - {GeoExt.data.LayerRecord}
     */
    var describeFeaturetypeSuccess = function(record) {
        var layer = record.get('layer');
        return function(store, records) {
            // find geometry column name
            var idx = store.find('type', GEOR.ows.matchGeomProperty);
            if (idx > -1) {
                // we have a geometry
                var r = store.getAt(idx);
                //record.set('geometryName', r.get('name')); // later on ?
                layer.protocol.setGeometryName(r.get('name'));
                layerStore.addSorted(record.clone());
            } else {
                GEOR.util.errorDialog({
                    msg: tr("The NAME layer does not contain a valid geometry column", {
                        'NAME': p.featureType
                    })
                });
            }
        };
    };

    /**
     * Method: addSelectedLayers
     * Adds the selected OGC layers to the given layerStore.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application layer store.
     */
    var addSelectedLayers = function() {
        var records = selectedRecords[currentTab], record;
        var recordsToAdd = [];

        // we need to clone the layers
        for (var i=0, len=records.length; i<len; i++) {
            record = records[i];
            if (record instanceof GeoExt.data.LayerRecord) {
                // we're coming from the WMS or WFS tab
                var layer = record.get("layer");
                if (layer instanceof OpenLayers.Layer.WMS || layer instanceof OpenLayers.Layer.WMTS) {
                    // WMS + WMTS layers just need cloning
                    // (well, for the moment - see http://applis-bretagne.fr/redmine/issues/1996)
                    recordsToAdd.push(record.clone());
                } else if (layer.protocol) {
                    // we are coming from the WFS tab

                    // WFS layers need cloning of protocol.format too ?
                    // "this.format is null" sur :
                    // this.format.geometryName = geometryName; (protocol.WFS.v1 L231)
                    // quand on supprime une couche WFS puis quand on l'ajoute à nouveau sans recharger le WFS capabilities store.


                    // For WFS layers, we need to get more information
                    // (typically the geometry name)
                    // from WFS DescribeFeatureType
                    var p = layer.protocol;
                    GEOR.waiter.show(); // increments a counter
                    GEOR.ows.WFSDescribeFeatureType({
                        typeName: p.featureType,
                        owsURL: p.url
                    },{
                        success: describeFeaturetypeSuccess.call(this, record),
                        failure: function() {
                            GEOR.util.errorDialog({
                                msg: tr("Unreachable server or insufficient rights")
                            });
                        },
                        scope: this
                    });
                } else {
                    // we have a vector layer without a protocol 
                    // we are coming from the upload file tab
                    layerStore.addSorted(record);
                }
            } else if (record.get("layer_name")) {
                // we're coming from the CSW tabs
                // convert records to layer records
                GEOR.waiter.show(); // increments a counter
                var store = GEOR.ows.WMSCapabilities({
                    storeOptions: {
                        url: record.get('service_url')
                    },
                    success: capabilitiesSuccess.call(this, record),
                    failure: function() {
                        GEOR.util.errorDialog({
                            msg: tr("Unreachable server or insufficient rights")
                        });
                    }
                });
            }
        }
        Ext.each(recordsToAdd, function(r) {
            layerStore.addSorted(r);
        });
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Return the window for layers adding management.
         *
         * Parameters:
         * ls - {GeoExt.data.LayerStore} The application layer store.
         * animateFrom - {String} Id or element from which the window
         *  should animate while opening
         *
         * Returns:
         * {Ext.Window}
         */
        create: function(ls, animateFrom) {
            layerStore = ls;
            tr = OpenLayers.i18n;
            addButton = new Ext.Button({
                text: tr("Add"),
                minWidth: 90,
                iconCls: 'btn-add',
                disabled: true,
                handler: function() {
                    addSelectedLayers();
                    switch (currentTab) {
                    case "cswbrowser":
                        GEOR.cswbrowser.clearSelection();
                        break;
                    case "cswquerier":
                        GEOR.cswquerier.clearSelection();
                        break;
                    case "wmts":
                        panels["wmts"].clearSelection();
                        break;
                    case "wms":
                        panels["wms"].clearSelection();
                        break;
                    case "wfs":
                        panels["wfs"].clearSelection();
                        break;
                    case "file":
                        GEOR.fileupload.clearSelection();
                        break;
                    default:
                        break;
                    }
                },
                scope: this
            });
            var win = new Ext.Window({
                title: tr("Add layers from a ..."),
                constrainHeader: true,
                layout: 'fit',
                animateTarget: GEOR.config.ANIMATE_WINDOWS && animateFrom,
                width: 650,
                height: 450,
                closeAction: 'hide',
                modal: false,
                items: createTabPanel(),
                buttons: [
                    {
                        text: tr("Close"),
                        handler: function() {
                            win.hide();
                        }
                    }, addButton
                ]
            });
            return win;
        }
    };
})();


/**
 * A customized TwinTriggerField, currently used in keyword xlink search
 * Taken from the Extjs examples and adapted (translations)
 */

Ext.app.OWSUrlField = Ext.extend(Ext.form.TwinTriggerField, {
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
    scope: null,

    cancelRequest: function() {
        var proxy = this.store.proxy;
        var conn = proxy.getConnection();
        if (conn.isLoading()) {
            conn.abort();
        }
        this.store.fireEvent("exception");
    },

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

    onTrigger2Click: function(url) {
        this.cancelRequest();

        // trim raw value:
        url = (typeof(url) === "string") ?
            url : this.getRawValue();

        url = url.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (url.length < 1) {
            this.onTrigger1Click();
            return;
        }
        if (!GEOR.util.isUrl(url, true)) {
            GEOR.util.errorDialog({
                msg: OpenLayers.i18n("Malformed URL")
            });
            return;
        }
        // update url for OWS getCapabilities request
        this.store.proxy.setUrl(url);
        this.store.load({
            callback: Ext.createDelegate(this.callback, this.scope),
            add: false
        });

        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
    }
});
