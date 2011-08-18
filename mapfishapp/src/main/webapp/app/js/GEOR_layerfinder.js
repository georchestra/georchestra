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
 * @requires GeoExt/data/LayerRecord.js
 * @include OpenLayers/Request.js
 * @include OpenLayers/Format/CSWGetDomain/v2_0_2.js
 * @include OpenLayers/Format/CSWGetRecords/v2_0_2.js
 * @include OpenLayers/Filter/Comparison.js
 * @include Ext.ux/widgets/tree/TreeStoreNode.js
 * @include Ext.ux/widgets/tree/XmlTreeLoader.js
 * @include GEOR_ows.js
 * @include GEOR_util.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.layerfinder = (function() {

    /*
     * Private
     */

    var tabPanel = null;

    var addButton = null;

    /**
     * Property: mask
     * {Ext.LoadMask} the catalogue's keywords panel mask
     */
    var mask = null;

    /**
     * Property: xmlTreeLoader
     * {Ext.ux.tree.XmlTreeLoader} the treeLoader
     */
    var xmlTreeLoader = null;

    /**
     * Property: treeSorter
     * {Ext.tree.TreeSorter} the tree sorter
     */
    var treeSorter = null;

    /**
     * Method: filterCswRecord
     * Keep only WMS-1.1.1 records with a correct layer name and server URL
     *
     * Parameters:
     * records - {Array} array of records
     *
     * Returns:
     * {Array} array of filtered records
     */
    var filterCswRecord = function(records) {
        var filtered = [], name, rights = [], metadataURL;
        Ext.each(records, function (record) {
            if(record.URI) {
                // multiple wms can be found in one csw:Record
                Ext.each(record.URI, function (item) {
                    if((item.protocol == "OGC:WMS-1.1.1-http-get-map") &&
                        item.name && item.value) {

                        name = (record.title && record.title[0]) ?
                                record.title[0].value : "undefined";

                        metadataURL = null;
                        if (record.identifier && record.identifier[0]) {
                            metadataURL = GEOR.config.GEONETWORK_URL+
                                '/metadata.show?uuid='+ record.identifier[0].value
                            name += ' - <a href="'+metadataURL +
                                '" target="_blank" onclick="window.open(this.href);return false;">métadonnées</a>';
                        }

                        if (record.rights && record.rights[0]) {
                            Ext.each(record.rights, function(r) {
                                rights.push(r.value);
                            });
                        }
                        this.push({
                            name: name,
                            layername: item.name,
                            wmsurl: item.value,
                            metadataURL: metadataURL,
                            rights: rights.join(' - ') || "",
                            abstract: record.abstract ?
                                record.abstract[0] : "",
                            source: (record.source && record.source[0]) ?
                                record.source[0].value : ""
                        });
                    }
                }, this);
            }
        }, filtered);
        return filtered;
    };

    /**
     * Method: cleanTree
     * Removes all children nodes of a given tree
     *
     * Parameters:
     * tree - {Ext.tree.TreePanel} the treePanel to empty
     */
    var cleanTree = function(tree) {
        while (tree.root.item(0)) {
            tree.root.item(0).remove();
        }
    };

    /**
     * Method: appendKeyword
     * Create and append treenode to tree with given keyword
     *
     * Parameters:
     * tree - {Ext.tree.TreePanel} the treePanel to append the node to
     * keyword - {String} the keyword string to display
     */
    var appendKeyword = function(tree, keyword) {
        if (!xmlTreeLoader) {
            xmlTreeLoader = new Ext.ux.tree.XmlTreeLoader({
                url: GEOR.config.CSW_URL,
                parseInput: function(treeLoader, treeNode) {
                    var getRecordsFormat = new OpenLayers.Format.CSWGetRecords({
                        maxRecords: 100
                    });
                    return getRecordsFormat.write({
                        resultType: "results",
                        Query: {
                            // TODO here : add Sort info
                            // (see http://trac.osgeo.org/openlayers/ticket/2952 & http://csm-bretagne.fr/redmine/issues/1724)
                            Constraint: {
                                version: "1.1.0",
                                Filter: new OpenLayers.Filter.Comparison({
                                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                                    property: GEOR.config.CSW_GETDOMAIN_PROPERTY,
                                    value: treeNode.text
                                })
                            },
                            ElementSetName: {
                                value: "full"
                            }
                        }
                    });
                },
                parseOutput: function(treeLoader, xml) {
                    var getRecordsFormat = new OpenLayers.Format.CSWGetRecords();
                    var response = getRecordsFormat.read(xml);
                    var filteredRecords = filterCswRecord(response.records);
                    return filteredRecords;
                }
            });
        }

        tree.root.appendChild(new Ext.ux.tree.TreeStoreNode({
            text: keyword,
            store: new Ext.data.JsonStore({
                fields: [
                    {name: 'text', mapping: 'name'},
                    {name: 'name', mapping: 'layername'},
                    {name: 'description', mapping: 'abstract'},
                    {name: 'wmsurl'},
                    {name: 'metadataURL'},
                    {name: 'rights'},
                    {name: 'qtip', mapping: 'abstract'},
                    {name: 'displayInTree', defaultValue: true}
                ]
            }),
            expandable: true,
            expanded: false,
            loader: xmlTreeLoader,
            leaf: false,
            defaults: {
                checked: false
            }
        }));

        if (!treeSorter && GEOR.config.CSW_GETDOMAIN_SORTING) {
            treeSorter = new Ext.tree.TreeSorter(tree, {
                folderSort: false,
                caseSensitive: false,
                dir: "asc",
                sortType: function(node) {
                    var translate = {
                        "ä": "a", "á": "a", "à": "a", "â": "a",
                        "Ä": "A", "Á": "A", "À": "A", "Â": "A",
                        "ë": "e", "é": "e", "è": "e", "ê": "e",
                        "Ë": "E", "É": "E", "È": "E", "Ê": "E",
                        "ï": "i", "î": "i",
                        "Ï": "I", "Î": "I",
                        "ö": "o", "ó": "o", "ò": "o", "ô": "o",
                        "Ö": "O", "Ó": "O", "Ò": "O", "Ô": "O",
                        "ü": "u", "ú": "u", "ù": "u", "û": "u",
                        "Ü": "U", "Ú": "U", "Ù": "U", "Û": "U"
                    };
                    var translate_re = /[äáàâÄÁÀÂëéèêËÉÈÊïîÏÎöóòôÖÓÒÔüúùûÜÚÙÛ]/g;

                    var text = (node.text) ? node.text.replace(translate_re, function(match) {
                        return translate[match];
                    }) : "";
                    return text.toLowerCase();
                }
            });
        }
    };

    /**
     * Method: buildGeorTree
     * Creates geOrchestra keyword list in tree
     *
     * Parameters:
     * tree - {Ext.tree.TreePanel} the treePanel to append the nodes to
     */
    var buildGeorTree = function(tree) {
        mask.show();
        cleanTree(tree);
        var getDomainFormat = new OpenLayers.Format.CSWGetDomain();
        OpenLayers.Request.POST({
            url: GEOR.config.CSW_URL,
            data: getDomainFormat.write({PropertyName: GEOR.config.CSW_GETDOMAIN_PROPERTY}),
            success: function(response) {
                var r = getDomainFormat.read(response.responseText);
                Ext.each(r.DomainValues[0].ListOfValues, function (item) {
                    appendKeyword(tree, item.Value.value);
                }, this);
                mask.hide();
            },
            failure: function() {
                mask.hide();
                GEOR.util.errorDialog({
                    msg: "La requête CSW getDomain a échoué"
                });
            }
        });
    };

    /**
     * Method: buildInspireTree
     * Creates Inspire keyword list in tree
     *
     * Parameters:
     * tree - {Ext.tree.TreePanel} the treePanel to append the nodes to
     */
    var buildInspireTree = function(tree, key) {
        mask.show();
        cleanTree(tree);
        if (!key) {
            GEOR.util.errorDialog({
                title: "Erreur sur le thésaurus",
                msg: "Absence de clé pour accéder à ce thésaurus"
            });
        }
        OpenLayers.Request.GET({
            url: GEOR.config.GEONETWORK_URL + '/xml.search.keywords',
            params: {
                pNewSearch: 'true',
                pKeyword: '*',
                //maxResults: '100', // Should it be changed ?
                pTypeSearch: 1,
                // pTypeSearch: 0 = starts with / 1 = contains / 2 = exact match
                pMode: 'consult',
                pThesauri: key
            },
            success: function(response) {
                var keywordType = Ext.data.Record.create([
                    {name: 'selected', mapping: 'selected'},
                    {name: 'name', mapping: 'value'},
                    {name: 'description', mapping: 'definition'},
                    {name: 'uri', mapping: 'uri'}
                ]);
                var myReader = new Ext.data.XmlReader({
                   record: "keyword",
                   id: "id"
                }, keywordType);
                var r = myReader.read(response);
                if (r.records) {
                    Ext.each(r.records, function(record) {
                        appendKeyword(tree, record.get('name'));
                    });
                }
                mask.hide();
            },
            failure: function() {
                mask.hide();
                GEOR.util.errorDialog({
                    msg: "La requête des mots clés a échoué"
                });
            }
        });
    };

    /**
     * Method: createCataloguePanel
     * Return the panel for the geocatalogue tab.
     *
     * Returns:
     * {Ext.Panel}
     */
    var createCataloguePanel = function() {

        var tree = new Ext.tree.TreePanel({
            region: 'center',
            useArrows:true,
            autoScroll:true,
            animate:true,
            rootVisible: false,
            border: false,
            root: {
                nodeType: 'async'
            }
        });
        var registerCheckbox = function(node){
            if(!node.hasListener("checkchange")) {
                node.on("checkchange", function(node, checked){
                    if (tree.getChecked().length > 0) {
                        addButton.enable();
                    } else {
                        addButton.disable();
                    }
                });
            }
        };
        tree.on({
            "insert": registerCheckbox,
            "append": registerCheckbox
        });

        var recordType = Ext.data.Record.create([
            {name: 'name', type: 'string', mapping: 'filename'},
            {name: 'key', type: 'string', mapping: 'key'}
        ]);
        var thesauriStore = new Ext.data.Store({
            autoLoad: true,
            proxy: new Ext.data.HttpProxy({
                url: GEOR.config.GEONETWORK_URL + '/xml.thesaurus.getList',
                method: 'GET',
                disableCaching: false
            }),
            reader: new Ext.data.XmlReader({
                record: 'thesaurus',
                id: 'key'
            }, recordType),
            listeners: {
                "load": function(store) {
                    var regexp = new RegExp('.rdf');
                    store.each(function(r) {
                        r.set('name', r.get('name').replace(regexp, ''));
                    });
                    // ajout du record GEOR.config.THESAURUS_NAME
                    var v = [];
                    v['name'] = GEOR.config.THESAURUS_NAME;
                    v['key'] = GEOR.config.THESAURUS_NAME;
                    store.add([new recordType(v)]);

                    var defKey = GEOR.config.DEFAULT_THESAURUS_KEY;
                    combo.setValue(defKey);
                    var r = store.query('key', defKey).first();
                    combo.fireEvent('select', combo, r);
                },
                scope: this
            }
        });

        var combo = new Ext.form.ComboBox({
            xtype: 'combo',
            id: 'mycombo',
            fieldLabel: 'Nomenclature',
            store: thesauriStore,
            loadingText: 'chargement...',
            mode: 'local',
            triggerAction: 'all',
            editable: false,
            valueField: 'key',
            displayField: 'name',
            listeners: {
                "select": function(combo, record){
                    if (record.get('key') == GEOR.config.THESAURUS_NAME) {
                        buildGeorTree(tree);
                    } else {
                        buildInspireTree(tree, record.get('key'));
                    }
                }
            }
        });

        return {
            title: 'Géocatalogue',
            layout: 'border',
            items: [{
                region: 'north',
                xtype: 'form',
                autoHeight: true,
                bodyStyle: 'padding: 5px;',
                items: [combo]
            }, tree],
            getCount: function() {
                return tree.getChecked().length;
            },
            getSelectedRecords: function() {
                return tree.getChecked('record');
            },
            clearSelections: function() {
                Ext.each(tree.getChecked(), function (item) {
                    item.getUI().toggleCheck(false);
                });
            },
            listeners: {
                "afterlayout": function() {
                    if (!mask) {
                        mask = new Ext.LoadMask(tree.getEl(), {
                            msg: "chargement en cours"
                        });
                    }
                }
            }
        };
    };


    /**
     * Method: createOgcPanel
     * Return the panel for the OGC web services.
     *
     * Parameters:
     * options - {Object} Hash with key: srs (the map srs).
     *
     * Returns:
     * {Ext.Panel}
     */
    var createOgcPanel = function(options) {
        var store = new GEOR.ows.WMSCapabilities();

        /**
         * Property: cbxSm
         * {Ext.grid.CheckboxSelectionModel} The selection model
         */
        var cbxSm =  new Ext.grid.CheckboxSelectionModel({
            width: 20,
            // for check all/none behaviour:
            header: '<div class="x-grid3-hd-checker">&#160;</div>',
            listeners: {
                "selectionchange": function(sm) {
                    if (sm.getCount()>0) {
                        addButton.enable();
                    } else {
                        addButton.disable();
                    }
                }
            }
        });

        var r = function (val){
            if (val) {
                return '<img src="app/img/famfamfam/tick.gif" alt="oui">';
            } else {
                return '<img src="app/img/famfamfam/cross.gif" alt="non">';
            }
        };

        // create a grid to display records from the store
        var grid = new Ext.grid.GridPanel({
            region: 'center',
            border: false,
            store: store,
            loadMask: {
                msg: "Chargement ..."
            },
            columns: [
                cbxSm,
                {header: "Couche", dataIndex: "title", sortable: true, width: 200},
                {id: "queryable", header: "Interrogeable", dataIndex: "queryable", sortable: true, width: 75, renderer: r},
                {id: "opaque", header: "Opaque", dataIndex: "opaque", sortable: true, width: 50, renderer: r},
                {id: "description", header: "Description", dataIndex: "abstract"}
            ],
            sm: cbxSm,
            enableHdMenu: false,
            autoExpandColumn: "description"
        });

        var comboField = new Ext.form.ComboBox({
            editable: false,
            triggerAction: 'all',
            height: 30,
            fieldLabel: "Choisissez un serveur WMS",
            loadingText: 'Chargement...',
            mode: 'remote',
            store: new Ext.data.Store({
                proxy : new Ext.data.HttpProxy({
                    method: 'GET',
                    disableCaching: false,
                    url: GEOR.config.WMS_LIST_URL
                }),
                reader: new Ext.data.JsonReader({
                    root: 'servers',
                    fields: ['name', 'url']
                }),
                sortInfo: {
                    field: 'name',
                    direction: 'ASC'
                }
            }),
            listeners: {
                "select": function(cmb, rec, idx) {
                    urlField.setValue(rec.get('url'));
                    urlField.onTrigger2Click();
                }
            },
            valueField: 'url',
            displayField: 'name',
            tpl: '<tpl for="."><div ext:qtip="<b>{name}</b><br/>{url}" class="x-combo-list-item">{name}</div></tpl>'
        });

        var urlField = new Ext.app.WMSUrlField({
            fieldLabel: "... ou saisissez son adresse",
            srs: options.srs,
            store: store,
            height: 30,
            width: 400
        });

        return {
            title: 'Serveurs OGC',
            layout: 'border',
            items: [
                {
                    region: 'north',
                    layout: 'form',
                    labelSeparator: ' : ',
                    labelWidth: 170,
                    bodyStyle: 'padding: 5px;',
                    height: 65,
                    items: [comboField, urlField]
                },
                grid
            ],
            getCount: function() {
                return cbxSm.getCount();
            },
            getSelectedRecords: function() {
                return cbxSm.getSelections();
            },
            clearSelections: function() {
                cbxSm.clearSelections();
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
    var addSelectedLayers = function(layerStore) {
        var p = tabPanel.getActiveTab();
        var records = p.getSelectedRecords();
        var recordsToAdd = [];
        // we need to clone the layers because they should be added twice
        for(var i=0, len=records.length; i<len; i++) {
            var record = records[i];
            if(record instanceof GeoExt.data.LayerRecord) {
                // we're coming from the OGC tab
                recordsToAdd.push(record.clone());
            } else if(record.get("name")) {
                // we're coming from the geocatalog
                // convert records to layer records
                var data = record.data;
                var store = new GEOR.ows.WMSCapabilities({
                    storeOptions: {
                        url: data.wmsurl
                    },
                    success: function(store, records) {
                        var index = store.find("name", this.layerName);
                        if(index < 0) {
                            GEOR.util.errorDialog({
                                msg: "La couche n'a pas été trouvée dans le service WMS. " +
                                     "Peut-être n'avez-vous pas les droits d'accès suffisants pour accéder à cette couche."
                            });
                            return;
                        }
                        var r = records[index];
                        var srs = this.layerStore.map.getProjection();
                        if(!r.get('srs') || (r.get('srs')[srs] !== true)) {
                            GEOR.util.errorDialog({
                                msg: "La projection de la couche n'est pas compatible."
                            });
                            return;
                        }
                        // here: add the copyright information to the "attribution" field
                        if (data.rights && !r.get("attribution")) {
                            r.set("attribution", {title: data.rights});
                        }
                        // if we have a metadataURL coming from the catalog,
                        // we use it instead of the one we get from the capabilities
                        // (as asked by Lydie - see http://csm-bretagne.fr/redmine/issues/1599#note-5)
                        if (data.metadataURL) {
                            r.set("metadataURLs", [data.metadataURL]);
                        }
                        this.layerStore.addSorted(r);
                    },
                    scope: {
                        layerStore: layerStore,
                        layerName: record.get("name")
                    }
                });
            }
        }
        Ext.each(recordsToAdd, function(r) {
            layerStore.addSorted(r);
        });
        p.clearSelections();
    };

    /**
     * Method: createTabPanel
     * Return the main tab panel.
     *
     * Parameters:
     * layerStore - {GeoExt.data.LayerStore} The application layer store.
     *
     * Returns:
     * {Ext.TabPanel}
     */
    var createTabPanel = function(layerStore) {
        tabPanel = new Ext.TabPanel({
            border: false,
            activeTab: 0,
            deferredRender: true,
            items: [
                createCataloguePanel(),
                createOgcPanel({
                    srs: layerStore.map.getProjection()
                })
            ],
            listeners: {
                'tabchange': function (tp, p) {
                    if (p.getCount() > 0) {
                        addButton.enable();
                    } else {
                        addButton.disable();
                    }
                }
            }
        });
        return tabPanel;
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
         * layerStore - {GeoExt.data.LayerStore} The application layer store.
         *
         * Returns:
         * {Ext.Window}
         */
        create: function(layerStore) {
            addButton = new Ext.Button({
                text: 'Ajouter',
                disabled: true,
                handler: function() {
                    addSelectedLayers(layerStore);
                    win.hide();
                },
                scope: this
            });
            var win = new Ext.Window({
                title: 'Ajouter des couches',
                layout: 'fit',
                width: 650,
                height: 450,
                closeAction: 'hide',
                modal: false,
                items: createTabPanel(layerStore),
                buttons: [
                    addButton,
                    {
                        text: 'Annuler',
                        handler: function() {
                            win.hide();
                        }
                    }
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

Ext.app.WMSUrlField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function() {
        Ext.app.WMSUrlField.superclass.initComponent.call(this);
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

    onTrigger1Click: function() {
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
            // conf
            var conf = Ext.get('conf');
            if (conf) {
                conf.enableDisplayMode().show();
            }
        }
    },

    onTrigger2Click: function() {
        // trim raw value:
        var v = this.getRawValue().replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (v.length < 1) {
            this.onTrigger1Click();
            return;
        }
        if (!GEOR.util.isUrl(v)) {
            GEOR.util.errorDialog({
                msg: "URL non conforme."
            });
            return;
        }
        var srs = this.srs;
        // update url for WMSCap request
        this.store.proxy.conn.url = v;
        this.store.load({
            callback: function(r, options, success) {
                if (success) {
                    var store = this.store;
                    var t = store.getCount();
                    // but we don't want to display layers
                    // which cannot be served in map's native SRS
                    store.filterBy(function(record, id) {
                        return record.get('srs') && (record.get('srs')[srs] === true);
                    });
                    var notDisplayed = t - store.getCount();
                    if (notDisplayed > 0) {
                        var plural = (notDisplayed > 1) ? 's' : '';
                        GEOR.util.infoDialog({
                           msg: "Le serveur publie "+notDisplayed+
                            " couche"+plural+" dont la projection n'est pas compatible"
                        });
                    }
                }
            },
            scope: this,
            add: false
        });

        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
        // conf
        var conf = Ext.get('conf');
        if (conf) {
            conf.enableDisplayMode().hide();
        }
    }
});
