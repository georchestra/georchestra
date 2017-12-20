/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOR_data.js
 * @include GEOR_ows.js
 * @include GEOR_util.js
 * @include GEOR_waiter.js
 * @include GEOR_config.js
 * @include OpenLayers/Control/SelectFeature.js
 */

Ext.namespace("GEOR");

GEOR.layerstree = (function() {
    /*
     * Private
     */

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

    /**
     * Property: map
     * {OpenLayers.Map} The map
     */
    var map;

    /**
     * Property: vectorLayer
     * {OpenLayers.Layer.Vector} The vector layer.
     */
    var vectorLayer;

    /**
     * Property: sf
     * {OpenLayers.Control.SelectFeature} The control used to hover the features.
     */
    var sf;

    /**
     * Property: mask
     * {Ext.LoadMask} The treePanel loadMask
     */
    var mask;

    /**
     * Property: counter
     * {Integer} An internal ajax request counter
     */
    var counter;

    /**
     * Property: rootNode
     * {Ext.tree.AsyncTreeNode} The root tree node.
     */
    var rootNode;

    /**
     * Property: servicesNode
     * {Ext.tree.AsyncTreeNode} The "OGC services" tree node.
     */
    var servicesNode;

    /**
     * Property: layersNode
     * {Ext.tree.AsyncTreeNode} The "Single layers" tree node.
     */
    var layersNode;

    /**
     * Property: globalPropertiesNode
     * {Ext.tree.TreeNode} The global properties tree node.
     */
    var globalPropertiesNode;

    /**
     * Property: maxLayerNameLength
     * {Integer} maximum number of chars for layer name
     */
    var maxLayerNameLength = 100; //30;

    /**
     * Property: callback
     * {Function} to be executed when all layers have finished loading
     */
    var callback;

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: beforelayerchange
         * Fires when a layer selection is about to change
         */
        "beforelayerchange",
        /**
         * Event: layerchange
         * Fires when a layer selection has changed
         */
        "layerchange",
        /**
         * Event: beforeextract
         * Fires before extraction is performed
         */
        "beforeextract"
    );

    /**
     * Property: selectionModel
     * {Ext.tree.DefaultSelectionModel} The tree selection model.
     */
    var selectionModel = new Ext.tree.DefaultSelectionModel({
        listeners: {
            'beforeselect': function(sm, newnode, oldnode){
                if(!newnode.isLeaf()) {
                    // only leaf nodes are selectable
                    return false;
                }
                if(oldnode) {
                    //save options in oldnode.attributes.owsinfo
                    var owsinfo = oldnode.attributes.owsinfo;
                    owsinfo.extent = map.getExtent(); // strange ...
                    observable.fireEvent('beforelayerchange');
                    if (sf) {
                        map.removeControl(sf);
                    }
                    // remove layers from map
                    removeAllLayers();
                }
                return true;
            },
            'selectionchange': function(sm, node){
                if(node.isLeaf()) {
                    var owsinfo = node.attributes.owsinfo;
                    if(owsinfo.layer == undefined) {
                        alert(tr("ERROR: owsinfo.layer should always be defined"));
                        return;
                    }
                    if(owsinfo.baselayer == undefined) {
                        // baselayer has never been created

                        // FIXME: we might need a better process to find the
                        // suitable SRS
                        var mapCRS = owsinfo.layer.projection;

                        var units = GEOR.util.getUnitsForCRS(mapCRS);
                        var baselayerOptions = {
                            projection: mapCRS,
                            maxExtent: owsinfo.layer.maxExtent.scale(1.5),
                            units: units
                        };
                        // force map scales to configurated scales, if applicable:
                        // see http://applis-bretagne.fr/redmine/issues/2413
                        if (units == 'm' && GEOR.config.METRIC_MAP_SCALES) {
                            baselayerOptions.scales = GEOR.config.METRIC_MAP_SCALES;
                        } else if (units == 'degrees' && GEOR.config.GEOGRAPHIC_MAP_SCALES) {
                            baselayerOptions.scales = GEOR.config.GEOGRAPHIC_MAP_SCALES;
                        }
                        owsinfo.baselayer = GEOR.map.getBaseLayer(baselayerOptions);
                    }
                    if(!owsinfo.extent) {
                        owsinfo.extent = owsinfo.layer.maxExtent;
                    }
                    map.addLayer(owsinfo.baselayer);

                    // HACK: we need to reset the state of the map
                    map.layerContainerOrigin = null;

                    if (owsinfo.layer.CLASS_NAME != "OpenLayers.Layer") {
                        map.addLayer(owsinfo.layer);
                    }

                    // TODO: find a way to override strategy (a box strategy might be better than a fixed one,
                    // since the max number of retrievable features is set)

                    // if layer has a strategy then activate it
                    if(owsinfo.layer.strategies && (owsinfo.layer.strategies.length == 1)) {
                        // TODO: show a mask on the layer tree to prevent selecting
                        // another layer before features have been completely loaded
                        // --> listen to loadend strategy event to remove the mask

                        // strategy activation
                        var strategy = owsinfo.layer.strategies[0];
                        strategy.activate();
                    }
                    map.addLayer(vectorLayer);

                    // This is in order to set the vector layer to the SRS of the map:
                    vectorLayer.addOptions({projection: owsinfo.baselayer.projection});

                    map.zoomToExtent(owsinfo.extent.scale(1.1));

                    // Hovering features :
                    if (owsinfo.layer.CLASS_NAME == "OpenLayers.Layer.Vector") {
                        sf = new OpenLayers.Control.SelectFeature(owsinfo.layer, {
                            autoActivate: true,
                            hover: true
                        });
                        map.addControl(sf);
                    }
                    observable.fireEvent('layerchange', owsinfo.exportinfo, (node==globalPropertiesNode));
                }
            }
        }
    });


    /**
     * Method: removeAllLayers
     * Remove all layers from OpenLayers map.
     */
    var removeAllLayers = function() {
        if(map && map.layers) {
            while(map.layers.length > 0) {
                map.removeLayer(map.layers[0]);
            }
        }
    };

    /**
     * Method: getChecked
     * Retrieve an array of checked nodes, or an array of a specific
     * attribute of checked nodes (e.g. "id")
     *
     * Parameters:
     * startNode - {TreeNode} The node to start from, defaults to the root
     * a - {String} attribute (optional) Defaults to null (return the actual nodes)
     */
    var getChecked = function(startNode, a){
        var r = [];
        var f = function(){
            if(this.attributes.checked && this.isLeaf() &&
                this.attributes.owsinfo && !this.disabled) {
                r.push(!a ? this : (a == 'id' ? this.id : this.attributes[a]));
            }
        };
        startNode.cascade(f);
        return r;
    };

    /**
     * Method: appendNodesFromLayerList
     * Create and append layers nodes to layersNode node given
     * a list of layers information.
     *
     * Parameters:
     * layersInfo - {Array} List of layers information.
     * parentNode - {Ext.tree.AsyncTreeNode} The node from which to
     *      append children nodes.
     */
    var appendNodesFromLayerList = function(layersInfo, parentNode) {
        Ext.each(layersInfo, function(item) {
            if(item.owstype == "WMS") {
                appendNodesFromWMSCap(item, parentNode);
            } else if (item.owstype == "WFS") {
                appendNodesFromWFSCap(item, parentNode);
            }
        });
    };

    var checkNullCounter = function() {
        counter -= 1;
        //console.log('compteur décrémenté de 1 -> '+counter);
        if (counter === 0) {
            mask.hide();
            callback && callback.call();
        }
    };

    /**
     * Method: appendNodesFromServiceList
     * Create and append layers nodes to servicesNode node given
     * a list of services information.
     *
     * Parameters:
     * servicesInfo - {Array} List of services information.
     * parentNode - {Ext.tree.AsyncTreeNode} The node from which to
     *      append children nodes.
     */
    var appendNodesFromServiceList = function(servicesInfo, parentNode) {
        var nodeBaseConfig = {
            checked: GEOR.config.LAYERS_CHECKED,
            expanded: true,
            children: [],
            listeners: {"checkchange": checker}
        };
        Ext.each(servicesInfo, function(item, index, allItems) {
            if(item.owstype == "WMS") {
                appendNodesFromWMSCap(item, nodeBaseConfig);
            } else if (item.owstype == "WFS") {
                appendNodesFromWFSCap(item, nodeBaseConfig);
            }
        });
    };

    /**
     * Method: getIsoMetadataUrl
     * extract the XML ISO19115 metadata URL, or null if there is no
     * such metadata
     *
     * Parameters:
     * record - local record item from {Ext.data.Store.data}
     */
    var getIsoMetadataUrl = function(record) {
        var urls = record.get("metadataURLs");
        if (!urls) {
            return null;
        }
        var href = null;
        for (var i=0,l=urls.length; i<l; i++) {
            if (urls[i].type == 'ISO19115:2003' && urls[i].format == 'text/xml' && GEOR.util.isUrl(urls[i].href, true)) {
                href = urls[i].href;
                break;
            }
        }
        return href;
    };

    var appendNodesFromWFSCap = function(wfsinfo, node) {
        GEOR.ows.WFSCapabilities({
            storeOptions: {
                url: wfsinfo.owsurl,
                protocolOptions: {
                    maxFeatures: GEOR.config.MAX_FEATURES,
                    srsName: GEOR.config.GLOBAL_EPSG,
                    url: wfsinfo.owsurl
                }
            },
            success: function(store, records) {
                if (!(node instanceof Ext.tree.TreeNode)) {
                    var serviceNode = new Ext.tree.AsyncTreeNode(Ext.apply({
                        text: wfsinfo.text,
                        qtip: tr('layerstree.qtip.wfs', {
                            'TEXT': wfsinfo.text,
                            'URL': wfsinfo.owsurl}),
                        iconCls: 'wfs-server'
                    }, node));
                    new Ext.tree.TreeSorter(serviceNode, {
                        leafAttr: "leaf",
                        dir: "asc",
                        property: "text",
                        caseSensitive: false
                    });
                    servicesNode.appendChild(serviceNode);
                }
                var appendRecord = function(record) {
                    var owsinfo = {
                        text: record.get("title") || record.get("name"),
                        owstype: wfsinfo.owstype, // WFS
                        owsurl: wfsinfo.owsurl,
                        layer: record.get("layer"),
                        exportinfo: {
                            srs: GEOR.config.GLOBAL_EPSG,
                            bbox: GEOR.config.GLOBAL_MAX_EXTENT,
                            owsType: wfsinfo.owstype, // WFS
                            owsUrl: wfsinfo.owsurl,
                            layerName: wfsinfo.layername
                        }
                    };
                    // remove autoActivation of the strategy to prevent the refresh
                    // on adding the layer to the map
                    owsinfo.layer.strategies[0].autoActivate = false;
                    owsinfo.layer.addOptions({
                        opacity: 0.7,
                        projection: GEOR.config.GLOBAL_EPSG,
                        maxExtent: GEOR.config.GLOBAL_MAX_EXTENT
                    });
                    owsinfo.layer.events.register("featuresadded", {}, function(evt) {
                        if(evt.features.length == GEOR.config.MAX_FEATURES) {
                            GEOR.util.infoDialog({
                                msg: tr('layerstree.maxfeatures',
                                        {NB: GEOR.config.MAX_FEATURES})
                            });
                        }
                    });

                    if (node instanceof Ext.tree.TreeNode) {
                        appendLayerChild(owsinfo, node);
                    } else {
                        appendLayerChild(owsinfo, serviceNode);
                    }
                };

                if(wfsinfo.layername) {
                    // look for the layername and append it
                    var index = store.findBy(function(record, id) {
                        var splittedName= wfsinfo.layername.split(':');
                        if(record.get("name") == splittedName[splittedName.length-1]) {
                            return true;
                        }
                    });
                    if(index >= 0) {
                        appendRecord(records[index]);
                    } else {
                        layersNode.appendChild(new Ext.tree.TreeNode({
                            text: GEOR.util.shortenLayerName(wfsinfo.layername, maxLayerNameLength),
                            disabled: true,
                            iconCls: 'error-layer',
                            checked: false,
                            qtip: tr('layerstree.qtip.missingwfs', {
                                'NAME': wfsinfo.layername,
                                'URL': wfsinfo.owsurl}),
                            leaf: true
                        }));
                    }
                } else {
                    // append all records
                    store.each(appendRecord);
                }
                checkNullCounter(); // OK
            },
            failure: function() {

                var serviceNode = new Ext.tree.AsyncTreeNode(Ext.applyIf({
                    text: wfsinfo.text,
                    iconCls: 'server-error',
                    checked: false,
                    disabled: true,
                    qtip: tr('layerstree.qtip.unavailablewfs', {
                        'NAME': wfsinfo.text,
                        'URL': wfsinfo.owsurl})
                }, node));
                servicesNode.appendChild(serviceNode);
                checkNullCounter(); // OK
            }
        });
    };

    /**
     * Method: appendNodesFromWMSCap
     */
    var appendNodesFromWMSCap = function(wmsinfo, node) {
        GEOR.ows.WMSCapabilities({
            storeOptions: {
                url: wmsinfo.owsurl
            },
            success: function(store, records) {
                var parentNode;
                if (!(node instanceof Ext.tree.TreeNode)) {
                    // create service node and append it to services node
                    var serviceNode = new Ext.tree.AsyncTreeNode(Ext.apply({
                        text: wmsinfo.text,
                        qtip: tr('layerstree.qtip.wms', {
                            'NAME': wmsinfo.text,
                            'URL': wmsinfo.owsurl}),
                        iconCls: 'wms-server'
                    }, node));
                    new Ext.tree.TreeSorter(serviceNode, {
                        leafAttr: "leaf",
                        dir: "asc",
                        property: "text",
                        caseSensitive: false
                    });
                    servicesNode.appendChild(serviceNode);
                    parentNode = serviceNode;
                } else {
                    parentNode = layersNode;
                }

                var appendRecord = function(record) {
                    var srs, bbox = record.get("bbox");
                    // trying to keep the main SRS, as requested by the administrator:
                    if (bbox.hasOwnProperty(GEOR.config.GLOBAL_EPSG)) {
                        srs = bbox[GEOR.config.GLOBAL_EPSG].srs;
                    }
                    // fallback 1
                    if(!srs) {
                        for(var p in bbox) {
                            srs = bbox[p].srs;
                            break;
                        }
                    }
                    // fallback 2
                    if(!srs) {
                        var srslist = record.get("srs");
                        for (var key in srslist) {
                            if (!srslist.hasOwnProperty(key)) {
                                continue;
                            }
                            // TODO: try to find a better SRS. see http://applis-bretagne.fr/redmine/issues/1949
                            if (key != "EPSG:WGS84(DD)" && srslist[key] === true) {
                                // bug "EPSG:WGS84(DD)" is not a valid srs
                                // http://jira.codehaus.org/browse/GEOS-3223
                                // don't take such a layer in account
                                srs = key;
                                break;
                            }
                        }
                    }
                    // always compute maxExtent from llbox, 
                    // which does not suffer the http://jira.codehaus.org/browse/GEOS-4283 bug
                    var llbbox = record.get("llbbox");
                    var maxExtent = OpenLayers.Bounds.fromArray(llbbox).transform(
                            new OpenLayers.Projection("EPSG:4326"),
                            new OpenLayers.Projection(srs));

                    // we should never end up here, since llbbox is required.
                    if(!(srs && maxExtent)) {
                        // append error node here
                        parentNode.appendChild(new Ext.tree.TreeNode({
                            text: GEOR.util.shortenLayerName(wmsinfo.layername, maxLayerNameLength),
                            disabled: true,
                            iconCls: 'error-layer',
                            checked: false,
                            qtip: tr('layerstree.qtip.badprojection',
                                     {'NAME': wmsinfo.layername}),
                            leaf: true
                        }));
                        return;
                    }

                    var owsinfo = {
                        text: record.get("title"),
                        owstype: wmsinfo.owstype, // WMS
                        owsurl: wmsinfo.owsurl,
                        layer: record.get("layer"),
                        exportinfo: {
                            srs: srs,
                            bbox: maxExtent,
                            isoMetadataUrl: getIsoMetadataUrl(record) // ISO19139 MD URL
                        }
                    };
                    owsinfo.layer.addOptions({
                        opacity: 0.7,
                        projection: srs,
                        maxExtent: maxExtent
                    });
                    if (node instanceof Ext.tree.TreeNode) {
                        appendLayerChild(owsinfo, node);
                    } else {
                        appendLayerChild(owsinfo, serviceNode);
                    }
                };

                // one layer has been given to us, not the whole service
                if(wmsinfo.layername) {
                    // look for the layername and append it
                    var index = store.findBy(function(record, id) {
                        if(record.get("name") == wmsinfo.layername) {
                            return true;
                        }
                    });
                    if(index >= 0) {
                        appendRecord(records[index]);
                    } else {
                        layersNode.appendChild(new Ext.tree.TreeNode({
                            text: GEOR.util.shortenLayerName(wmsinfo.layername, maxLayerNameLength),
                            disabled: true,
                            iconCls: 'error-layer',
                            checked: false,
                            qtip: tr('layerstree.qtip.missingwms', {
                                'NAME': wmsinfo.layername,
                                'URL': wmsinfo.owsurl}),
                            leaf: true
                        }));
                    }
                } else {
                    // append all records
                    store.each(appendRecord);
                }
                checkNullCounter(); // OK
            },
            failure: function() {

                var serviceNode = new Ext.tree.AsyncTreeNode(Ext.applyIf({
                    text: wmsinfo.text,
                    disabled: true,
                    checked: false,
                    iconCls: 'server-error',
                    qtip: tr('layerstree.qtip.unavailablewms', {
                        'NAME': wmsinfo.text,
                        'URL': wmsinfo.owsurl})
                }, node));
                servicesNode.appendChild(serviceNode);

                checkNullCounter(); // OK
            }
        });
    };

    /**
     * Method: appendLayerChild
     * Create and append layer node to the given param node
     * only if WMS DescribeLayer response is not empty.
     *
     * Parameters:
     * owsinfo - {Object} infos from wms.
     * parentNode - {Ext.tree.AsyncTreeNode} The node to append child to.
     */
    var appendLayerChild = function(owsinfo, parentNode) {
        var tip = tr('layerstree.layer.tip');
        if(owsinfo.owstype == "WMS") {
            counter += 1; // une requete XHR (a) en plus est necessaire (WMSDescribeLayer)

            // NOTE for the future: do not query N times the same server with the same request
            // keep a local db of responses :-)
            // see http://applis-bretagne.fr/redmine/issues/1928
            GEOR.ows.WMSDescribeLayer(
                owsinfo.layer.params.LAYERS,
                {
                    storeOptions: {
                        url: owsinfo.owsurl
                    },
                    success: function(store, records) {
                        if(owsinfo.exportinfo == undefined) {
                            owsinfo.exportinfo = {};
                        }
                        if(records.length == 0) {

                            parentNode.appendChild(new Ext.tree.TreeNode({
                                text: GEOR.util.shortenLayerName(owsinfo.text, maxLayerNameLength),
                                disabled: true,
                                iconCls: 'error-layer',
                                checked: false,
                                qtip: tr('layerstree.qtip.noextraction',
                                         {'NAME': owsinfo.text}),
                                leaf: true
                            }));

                            checkNullCounter(); // XHR (a)
                            return;
                        }
                        owsinfo.exportinfo.owsType = records[0].get("owsType");
                        owsinfo.exportinfo.owsUrl = records[0].get("owsURL");
                        owsinfo.exportinfo.layerName = records[0].get("typeName");
                        // typeName is "geor:sdi" while layerName might just be "sdi", 
                        // see https://github.com/georchestra/georchestra/issues/517
                        owsinfo.exportinfo.layerType = records[0].get("layerType");
                        
                        var cfg = {
                            text: GEOR.util.shortenLayerName(owsinfo.text, maxLayerNameLength),
                            leaf: true
                        };
                        
                        if (owsinfo.exportinfo.owsType == 'WCS') { // RASTER
                            // Here, we are trying to find the native raster resolution
                            // from the associated ISO19139 metadata.
                            // see https://github.com/georchestra/georchestra/issues/726
                            if (owsinfo.exportinfo.isoMetadataUrl && 
                                GEOR.util.isUrl(owsinfo.exportinfo.isoMetadataUrl, true)) {
                                counter += 1; // une requete XHR (b) en plus est necessaire ()
                                //console.log('compteur incrémenté de 1 (b) -> '+counter);
                                Ext.Ajax.request({
                                    url: owsinfo.exportinfo.isoMetadataUrl,
                                    disableCaching: false,
                                    success: function(response) {
                                        // use document.evaluate on the XML response to fetch the 
                                        // /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification
                                        // /gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance
                                        if (!response || !response.responseXML) {
                                            checkNullCounter(); // XHR (b)
                                            return;
                                        }
                                        var resTip = '', xmldoc = response.responseXML;
                                        if (Ext.isIE) {
                                            xmldoc.setProperty("SelectionLanguage", "XPath");
                                            xmldoc.setProperty("SelectionNamespaces", 
                                                "xmlns:gmd='http://www.isotc211.org/2005/gmd' xmlns:gco='http://www.isotc211.org/2005/gco' xmlns='http://www.isotc211.org/2005/gmd'");
                                            var node = xmldoc.documentElement.selectSingleNode(GEOR.config.METADATA_RESOLUTION_XPATH);
                                            if (node) {
                                                var res = parseFloat(node.firstChild.nodeValue),
                                                unit = node.getAttribute("uom");
                                            }
                                        } else if (!Ext.isIE11) {
                                            var res = xmldoc.evaluate(
                                                GEOR.config.METADATA_RESOLUTION_XPATH, 
                                                xmldoc, 
                                                GEOR.util.mdNSResolver, 
                                                XPathResult.NUMBER_TYPE, // 1
                                                null
                                            ).numberValue, // typically 0.5
                                            unit = xmldoc.evaluate(
                                                GEOR.config.METADATA_RESOLUTION_XPATH+'/@uom', 
                                                xmldoc, 
                                                GEOR.util.mdNSResolver, 
                                                XPathResult.STRING_TYPE, // 2
                                                null
                                            ).stringValue; // typically "m"
                                        }
                                        if (Ext.isNumber(res) && !!unit && GEOR.util.uomMetricRatio.hasOwnProperty(unit)) {
                                            // normalize resolution into meters
                                            res = GEOR.util.uomMetricRatio[unit] * res;
                                            resTip = '(@'+ res + ' m)';
                                            // force local resolution:
                                            owsinfo.exportinfo.resolution = res;
                                            // force global resolution to the lowest numerical one:
                                            if (res < GEOR.layeroptions.getGlobalResolution()) {
                                                GEOR.layeroptions.setGlobalResolution(res);
                                                globalPropertiesNode.attributes.owsinfo.exportinfo.resolution = res;
                                            }
                                        }
                                        Ext.apply(cfg, {
                                            iconCls: 'raster-layer',
                                            owsinfo: owsinfo,
                                            checked: GEOR.config.LAYERS_CHECKED,
                                            qtip: '<b>'+owsinfo.text+'</b> '+resTip+'<br/>' + tip
                                        });
                                        parentNode.appendChild(new Ext.tree.TreeNode(cfg));
                                        checkNullCounter(); // XHR (b)
                                    },
                                    failure: function(response) {
                                        Ext.apply(cfg, {
                                            iconCls: 'raster-layer',
                                            owsinfo: owsinfo,
                                            checked: GEOR.config.LAYERS_CHECKED,
                                            qtip: '<b>'+owsinfo.text+'</b><br/>' + tip
                                        });
                                        parentNode.appendChild(new Ext.tree.TreeNode(cfg));
                                        checkNullCounter(); // XHR (b)
                                    },
                                    scope: this
                                 });
                            } else {
                                Ext.apply(cfg, {
                                    iconCls: 'raster-layer',
                                    owsinfo: owsinfo,
                                    checked: GEOR.config.LAYERS_CHECKED,
                                    qtip: '<b>'+owsinfo.text+'</b><br/>' + tip
                                });
                                parentNode.appendChild(new Ext.tree.TreeNode(cfg));
                            }
                        } else if (owsinfo.exportinfo.owsType == 'WFS') { // VECTOR
                            Ext.apply(cfg, {
                                iconCls: 'vector-layer',
                                owsinfo: owsinfo,
                                checked: GEOR.config.LAYERS_CHECKED,
                                qtip: '<b>'+owsinfo.text+'</b><br/>' + tip
                            });
                            parentNode.appendChild(new Ext.tree.TreeNode(cfg));
                        } else { // ERROR
                            Ext.apply(cfg, {
                                iconCls: 'error-layer',
                                checked: false,
                                qtip: tr('layerstree.qtip.noextraction', {'NAME': owsinfo.text})
                            });
                            parentNode.appendChild(new Ext.tree.TreeNode(cfg));
                        }

                        checkNullCounter(); // XHR (a)
                    },
                    failure: function() {
                        checkNullCounter(); // XHR (a)

                        var msg = tr('layerstree.describelayer', {
                                'NAME': owsinfo.text,
                                'URL': owsinfo.owsurl});

                        parentNode.appendChild(new Ext.tree.TreeNode({
                            text: GEOR.util.shortenLayerName(owsinfo.text, maxLayerNameLength),
                            disabled: true,
                            iconCls: 'error-layer',
                            checked: false,
                            qtip: msg,
                            leaf: true
                        }));
                    }
                }
            );
        } else if (owsinfo.owstype == "WFS") {
            parentNode.appendChild(new Ext.tree.TreeNode({
                text: GEOR.util.shortenLayerName(owsinfo.text, maxLayerNameLength),
                iconCls: 'vector-layer',
                owsinfo: owsinfo,
                checked: GEOR.config.LAYERS_CHECKED,
                qtip: '<b>'+owsinfo.text+'</b><br/>' + tip,
                leaf: true
            }));
        }
    };

    /**
     * Method: checker
     * Check/uncheck child nodes
     *
     * Parameters:
     * node - {Ext.tree.TreeNode} current tree node
     * checked - {Boolean} new checkbox state
     */
    var checker = function(node, checked) {
        node.cascade(function(n) {
            if (n != node) {
                n.getUI().toggleCheck(checked);
            }
        });
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
         * APIMethod: create
         * Return the layers tree config.
         */
        create: function() {
            rootNode = new Ext.tree.TreeNode({
                text: tr('Your extraction cart'),
                expanded: true,
                iconCls: 'basket',
                children: []
            });
            return {
                xtype: 'treepanel',
                root: rootNode,
                autoScroll: Ext.isIE, // true only for browsers who don't understand CSS3 (~ IE).
                bodyCssClass: 'overflow-x-hidden', // CSS3 only
                rootVisible: true,
                expanded: true, // mandatory
                loader: {
                    preloadChildren: true
                },
                selModel: selectionModel,
                listeners: {
                    "render": function(p) {
                        (function() {
                            mask = new Ext.LoadMask(p.body.dom, {
                                msg: tr("Loading...")
                            });
                            mask.show();
                        }).defer(20);
                        // this 20 ms delay is required here because it seems that
                        // the render event is fired too soon by the TreePanel
                    }
                }
            };
        },

        /**
         * APIMethod: init
         * Initialize the layerstree module.
         *
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         * v - {OpenLayers.Layer.Vector} The vector layer.
         * c - {Function} the callback to execute once all layers have finished loading
         */
        init: function(m, v, c) {
            map = m;
            vectorLayer = v;
            callback = c;

            globalPropertiesNode = new Ext.tree.TreeNode({
                text: tr('Default parameters'),
                iconCls: 'config-layers',
                qtip: tr('layerstree.qtip.defaultparameters'),
                leaf: true,
                owsinfo: {
                    layer: new OpenLayers.Layer("fake_layer", {
                        projection: GEOR.config.GLOBAL_EPSG, // this one is also used as default export SRS
                        maxExtent: GEOR.config.GLOBAL_MAX_EXTENT,
                        maxResolution: "auto",
                        displayInLayerSwitcher: false
                    }),
                    exportinfo: {
                        srs: GEOR.config.GLOBAL_EPSG,
                        bbox: GEOR.config.GLOBAL_MAX_EXTENT
                    }
                }
            });

            rootNode.appendChild([globalPropertiesNode]);

            if (GEOR.data.layers && GEOR.data.layers.length) {
                layersNode = new Ext.tree.AsyncTreeNode({
                    text: tr('OGC Layers'),
                    checked: GEOR.config.LAYERS_CHECKED,
                    expanded: true, // mandatory
                    qtip: tr('OGC layers available for extraction'),
                    children: [],
                    listeners: {
                        "checkchange": checker
                    }
                });

                new Ext.tree.TreeSorter(layersNode, {
                    dir: "asc",
                    leafAttr: 'leaf',
                    property: "text",
                    caseSensitive: false
                });

                rootNode.appendChild([layersNode]);
            }

            if (GEOR.data.services && GEOR.data.services.length) {
                servicesNode = new Ext.tree.AsyncTreeNode({
                    text: tr("OGC services"),
                    checked: GEOR.config.LAYERS_CHECKED,
                    expanded: true, // mandatory
                    qtip: tr('The layers of these OGC services can be extracted'),
                    children: [],
                    listeners: {
                        "checkchange": checker
                    }
                });

                rootNode.appendChild([servicesNode]);
            }


            // we create a counter which will be decreased each time
            // an XHR request is over / increased when one more is required
            // when it's back to 0, we shall hide the load mask.
            counter = GEOR.data.layers.length + GEOR.data.services.length;
            // at the beginning, we only know that one capabilities request is
            // required for each layer and each service.

            //console.log('compteur initial -> '+counter+' ('+
            //  GEOR.data.layers.length+' couches et '+GEOR.data.services.length+' services)');

            // create and append layers nodes to layersNode node
            appendNodesFromLayerList(GEOR.data.layers, layersNode);

            // create and append layers nodes to servicesNode node
            appendNodesFromServiceList(GEOR.data.services, servicesNode);

            // default selection is global properties node
            selectionModel.select(globalPropertiesNode);
        },

        /**
         * APIMethod: saveExportOptions
         * Save extractOptions in the current node.
         */
        saveExportOptions: function(options) {
            var currentNode = selectionModel.getSelectedNode();
            var owsinfo = currentNode.attributes.owsinfo;
            if(owsinfo.exportinfo == undefined) {
                owsinfo.exportinfo = {};
            }
            Ext.apply(owsinfo.exportinfo, options);
        },

        /**
         * APIMethod: getSpec
         * returns the current extraction spec.
         */
        getSpec: function(email) {
            observable.fireEvent('beforeextract');
            var checkedNodes = getChecked(rootNode), node;
            var l = checkedNodes.length;

            var global = globalPropertiesNode.attributes.owsinfo.exportinfo;
            var out = {
                emails: [email],
                globalProperties: {
                    projection: global.projection,
                    resolution: global.resolution,
                    rasterFormat: global.globalRasterFormat,
                    vectorFormat: global.globalVectorFormat,
                    bbox: {
                        srs: global.srs,
                        value: global.bbox.toArray()
                    }
                },
                layers: new Array(l)
            };
            // send the JSESSIONID to the server as a validation token
            // only if user is not connected and if dlform is set to true:
            if (GEOR.config.DOWNLOAD_FORM && GEOR.data.anonymous) {
                // see proposition 2 of http://applis-bretagne.fr/redmine/issues/2194#note-15
                out.sessionid = GEOR.util.getCookie('JSESSIONID');
            };

            var local;
            for (var i=0; i<l; i++) {
                local = checkedNodes[i].attributes.owsinfo.exportinfo;
                out.layers[i] = {
                    projection: (local.projection &&
                        local.projection.length) ?
                            local.projection : null,
                    resolution: (typeof(local.resolution) == "number") ?
                        local.resolution : null,
                    format: (local.format &&
                        local.format.length) ?
                            local.format : null,
                    bbox: (local.bboxFromGlobal !== false) ? null : {
                        srs: local.srs,
                        value: local.bbox.toArray()
                    },
                    owsUrl: local.owsUrl,
                    owsType: local.owsType,
                    layerName: local.layerName
                };
                if (local.isoMetadataUrl !== null) {
                    out.layers[i].isoMetadataURL = local.isoMetadataUrl;
                }
            }

            return out;
        },

        /**
         * APIMethod: extract
         * Extract all checked layers.
         */
        extract: function(email, button) {
            GEOR.waiter.show();
            Ext.Ajax.request({
                url: GEOR.config.EXTRACTOR_BATCH_URL,
                // HTTP success:
                success: function(response) {
                    // since we are not in a REST world, we have to check
                    // the value of the success property in the returned XML
                    if (response.responseText &&
                        response.responseText.indexOf('<success>true</success>') > 0) {
                        // disable button
                        button.disable();
                        window.setTimeout(function(){
                            button.enable();
                        }, GEOR.config.EXTRACT_BTN_DISABLE_TIME*1000);
                        // info window
                        GEOR.util.infoDialog({
                            msg: tr('layerstree.email',
                                    {'EMAIL': GEOR.data.email})
                        });
                    } else {
                        GEOR.util.errorDialog({
                            msg: tr('The extraction request failed.')
                        });
                    }
                },
                // HTTP failure:
                failure: function(response) {
                    GEOR.util.errorDialog({
                        msg: tr('The extraction request failed.')
                    });
                },
                jsonData: GEOR.layerstree.getSpec(email),
                scope: this
            });

        },

        /**
         * APIMethod: getSelectedLayersCount
         * returns the number of selected layers
         */
        getSelectedLayersCount: function() {
            var count = 0;
            rootNode.cascade(function(n) {
                if (n.isLeaf() && n.attributes.checked &&
                    n.parentNode !== rootNode && !this.disabled) {
                    count += 1;
                }
            });
            return count;
        },

        /**
         * APIMethod: selectAllLayers
         * check all leaf layers in tree
         *
         * returns the number of checked nodes
         */
        selectAllLayers: function() {
            var count = 0;
            rootNode.cascade(function(n) {
                if (n.isLeaf() && !n.isSelected() &&
                    n.parentNode !== rootNode && !this.disabled) {
                    count += 1;
                    n.getUI().toggleCheck(true);
                }
            });
            return count;
        }
    };
})();
