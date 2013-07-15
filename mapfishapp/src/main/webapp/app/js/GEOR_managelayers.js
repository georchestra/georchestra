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
 * @requires GeoExt/widgets/tree/LayerNode.js
 * @include GeoExt/plugins/TreeNodeActions.js
 * @include GeoExt/plugins/TreeNodeComponent.js
 * @include GeoExt/widgets/tips/LayerOpacitySliderTip.js
 * @include GeoExt/widgets/LayerOpacitySlider.js
 * @include GeoExt/widgets/tree/LayerContainer.js
 * @include GeoExt/widgets/tree/TreeNodeUIEventMixin.js
 * @include OpenLayers/Format/JSON.js
 * @include GEOR_layerfinder.js
 * @include GEOR_util.js
 * Note: GEOR_querier.js not included here since it's not required for edit app
 */

Ext.namespace("GEOR");

GEOR.managelayers = (function() {
    /*
     * Private
     */

    /**
     * Class: LayerNode
     * Our own LayerNode class.
     */
    var LayerNode = Ext.extend(GeoExt.tree.LayerNode, {
        constructor: function(config) {
            config.text = GEOR.util.shortenLayerName(config.layer);
            config.qtip = config.layer.name;
            LayerNode.superclass.constructor.apply(this, [config]);
        }
    });
    Ext.tree.TreePanel.nodeTypes.geor_layer = LayerNode;

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: selectstyle
         * Fires when a new wms layer style has been selected
         */
        "selectstyle"
    );

    /**
     * Property: layerFinder
     */
    var layerFinder;

    /**
     * Property: layerContainer
     */
    var layerContainer;

    /**
     * Property: stylesMenu
     */
    var stylesMenu;

    /**
     * Property: querierRecord
     */
    var querierRecord;

    /**
     * Property: form
     */
    var form;

    /**
     * Property: jsonFormat
     */
    var jsonFormat;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: actionHandler
     * The action listener.
     */
    var actionHandler = function(node, action, evt) {
        var layer = node.layer;
        var layerRecord = node.layerStore.getById(layer.id);
        switch(action) {
        case "down":
            var map = layer.map;
            if (map.getLayerIndex(layer) == 1) {
                return;
            }
            map.raiseLayer(layer, -1);
            break;
        case "up":
            layer.map.raiseLayer(layer, +1);
            break;
        case "delete":
            if (GEOR.config.CONFIRM_LAYER_REMOVAL) {
                GEOR.util.confirmDialog({
                    msg: tr(
                        "Confirm NAME layer deletion ?",
                        {'NAME': layerRecord.get('title')}
                    ),
                    width: 360,
                    yesCallback: function() {
                        layer.destroy();
                    },
                    scope: this
                });
            } else {
                layer.destroy();
            }
            break;
        }
    };

    /**
     * Method: onStyleItemCheck
     * Is called when user clicked on a predefined styles item
     *
     * Parameters:
     * item {Ext.menu.CheckItem}
     * checked {Boolean}
     */
    var onStyleItemCheck = function(item, checked){
        if (checked === true) {
            observable.fireEvent("selectstyle", this, item.value);
        }
    };

    /**
     * Method: onFormatItemCheck
     * Is called when user clicked on a image format item
     *
     * Parameters:
     * item {Ext.menu.CheckItem}
     * checked {Boolean}
     */
    var onFormatItemCheck = function(item, checked){
        if (checked === true && item.value) {
            var layer = this.get('layer'),
                t = this.get('type');
            if (t == "WMS") {
                layer.mergeNewParams({
                    FORMAT: item.value
                });
            } else if (t == "WMTS") {
                layer.format = item.value;
                layer.redraw();
            }
        }
    };

    /**
     * Method: formatAttribution
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Object} The configured object (xtype: box)
     *          for inclusion in layer manager item
     */
    var formatAttribution = function(layerRecord) {
        var attr = layerRecord.get('attribution');
        var titleForDisplay = attr.title || '-';
        if (titleForDisplay.length > 16) {
            titleForDisplay = titleForDisplay.substr(0, 13) + '...';
        }

        // logo displayed in qtip if set
        var tip = tr('source: ')+ (attr.title || '-') +
            ((attr.logo && GEOR.util.isUrl(attr.logo.href, true)) ? '<br /><img src=\''+attr.logo.href+'\' />' : '');

        var attrDisplay = (attr.href) ?
            '<a href="'+attr.href+'" target="_blank" ext:qtip="'+tip+'">'+titleForDisplay+'</a>' :
            '<span ext:qtip="'+tip+'">'+titleForDisplay+'</span>';

        return {
            xtype: 'box',
            cls: "geor-layers-form-text",
            autoEl: {
                tag: 'span',
                html: tr('source: ')+attrDisplay
            }
        };
    };

    /**
     * Method: formatVisibility
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Object} The configured object (xtype: box)
     *          for inclusion in layer manager item
     */
    var formatVisibility = function(layerRecord) {
        var layer = layerRecord.get('layer');
        var visibilityText = tr("1:MAXSCALE to 1:MINSCALE", {
            'MAXSCALE': OpenLayers.Number.format(layer.maxScale, 0),
            'MINSCALE': OpenLayers.Number.format(layer.minScale, 0)
        });
        return {
            xtype: 'box',
            cls: "geor-layers-form-text",
            autoEl: {
                tag: 'span',
                'ext:qtip': tr("Visibility range (indicative):<br />from TEXT", {
                    'TEXT': visibilityText
                }),
                html: visibilityText
            }
        };
    };

    /**
     * Method: createGfiButton
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Object} The configured object (xtype: button)
     *          for inclusion in layer manager item toolbar
     */
    var createGfiButton = function(layerRecord) {
        return {
            xtype: 'button',
            disabled: !(layerRecord.get("queryable")),
            iconCls: 'geor-btn-info',
            allowDepress: true,
            enableToggle: true,
            toggleGroup: 'map',
            tooltip: tr("Information on objects of this layer"),
            listeners: {
                "toggle": function(btn, pressed) {
                    GEOR.getfeatureinfo.toggle(layerRecord, pressed);
                }
            }
        };
    };



    /**
     * Method: createFormatMenu
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Ext.menu.Menu} The configured formats menu
     */
    var createFormatMenu = function(layerRecord) {
        var formats = layerRecord.get("formats"),
            layer = layerRecord.get("layer"),
            type = layerRecord.get("type"),
            isWMS = type === "WMS",
            isWMTS = type === "WMTS",
            formatMenuItems = [];

        if (formats && formats.length) {
            for (var i=0, len=formats.length; i<len; i++) {
                var value = formats[i].value || formats[i];
                if (GEOR.config.ACCEPTED_MIME_TYPES.indexOf(value) > -1) {
                    formatMenuItems.push(new Ext.menu.CheckItem({
                        text: value,
                        value: value,
                        checked:
                            (isWMS && (
                                (formats[i].current && formats[i].current === true) ||
                                layer.params.FORMAT === formats[i])
                            ) ||
                            (isWMTS && layer.format === formats[i]),
                        group: 'format_' + layer.id,
                        checkHandler: onFormatItemCheck,
                        scope: layerRecord
                    }));
                }
            }
        } else {
            // we display the only one we know : the current one
            var v = (isWMS && layer.params.FORMAT) || (isWMTS && layer.format);
            formatMenuItems.push(new Ext.menu.CheckItem({
                text: v,
                value: v,
                checked: true,
                group: 'format_' + layer.id
            }));
        }

        return new Ext.menu.Menu({
            items: formatMenuItems
        });
    };

    /**
     * Method: createStylesMenu
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Ext.menu.Menu} The configured styles menu
     */
    var createStylesMenu = function(layerRecord) {
        var styles = layerRecord.get("styles"),
            layer = layerRecord.get("layer"),
            type = layerRecord.get("type"),
            isWMS = type === "WMS",
            isWMTS = type === "WMTS",
            stylesMenuItems = [],
            onStyleItemCheck;
        
        if (isWMS) {
            onStyleItemCheck = function(item, checked){
                if (checked === true) {
                    observable.fireEvent("selectstyle", this, item.value);
                }
            };
            var default_style = {
                // TODO: add style name in ()
                text: tr("Default style"),
                value: '',
                checked: true,
                group: 'style_' + layer.id,
                checkHandler: onStyleItemCheck,
                scope: layerRecord
            };
            // build object config for predefined styles
            stylesMenuItems.push(default_style);
            if (styles && styles.length > 0) {
                var checked, style;
                for (var i=0, len=styles.length; i<len; i++) {
                    style = styles[i];
                    if (style.href) {
                        if (style.current) {
                            // if the style has an href and is the current
                            // style we don't want any named style to be
                            // checked in the list of styles
                            default_style.checked = false;
                        }
                    } else {
                        checked = false;
                        if(style.current === true) {
                            default_style.checked = false;
                            checked = true;
                        }
                        stylesMenuItems.push(new Ext.menu.CheckItem({
                            text: style.name || style.title, // title is a human readable string
                            // but it is not often relevant (eg: may store "AtlasStyler v1.8")
                            // moreover, GeoServer 2 displays style name rather than style title.
                            value: style.name, // name is used in the map request STYLE parameter
                            checked: checked,
                            group: 'style_' + layer.id,
                            checkHandler: onStyleItemCheck,
                            scope: layerRecord
                        }));
                    }
                }
            }
        } else if (isWMTS) {
            var identifier;
            onStyleItemCheck = function(item, checked){
                if (checked === true) {
                    // correct way of doing:
                    layer.style = item.value;
                    //layer.redraw(); (will be required once the hack below is removed)
                    
                    // incorrect way of doing, but mandatory because of 
                    // https://github.com/GeoWebCache/geowebcache/issues/194
                    observable.fireEvent("selectstyle", this, item.value);
                }
            };
            for (var i=0, len=styles.length; i<len; i++) {
                identifier = styles[i].identifier;
                stylesMenuItems.push(new Ext.menu.CheckItem({
                    text: identifier,
                    value: identifier,
                    checked: layer.style === identifier,
                    group: 'style_' + layer.id,
                    checkHandler: onStyleItemCheck,
                    scope: layerRecord
                }));
            }
        }
        return new Ext.menu.Menu({
            items: stylesMenuItems
        });
    };

    /**
     * Method: submitData
     * If required, this method creates the form and uses it to
     * submit the objet passed as a parameter
     *
     * Parameters:
     * o - {Object} JS object which should be serialized + submitted
     */
    var submitData = function(o) {
        form = form || Ext.DomHelper.append(Ext.getBody(), {
            tag: "form",
            action: "/extractorapp/",
            target: "_blank",
            method: "post"
        });
        var input = form[0] || Ext.DomHelper.append(form, {
            tag: "input",
            type: "hidden",
            name: "data"
        });
        jsonFormat = jsonFormat || new OpenLayers.Format.JSON();
        input.value = jsonFormat.write(o);
        form.submit();
    };

    /**
     * Method: createMenu
     *
     * Parameters:
     * layerRecord - {GeoExt.data.LayerRecord}
     *
     * Returns:
     * {Ext.menu.Menu} The configured global menu
     */
    var createMenu = function(layerRecord) {
        var queryable = !!(layerRecord.get("queryable")),
            layer = layerRecord.get('layer'),
            type = layerRecord.get("type"),
            isWMS = type === "WMS",
            isWMTS = type === "WMTS",
            isWFS = type === "WFS";

        var menuItems = [], url, sepInserted;

        /**
         * Method: zoomToLayerRecordExtent
         *
         * Parameters:
         * r - {GeoExt.data.LayerRecord}
         */
        var zoomToLayerRecordExtent = function(r) {
            var map = r.get('layer').map,
                mapSRS = map.getProjection(),
                zoomed = false,
                bb = r.get('bbox');

            for (var key in bb) {
                if (!bb.hasOwnProperty(key)) {
                    continue;
                }
                if (key === mapSRS) {
                    map.zoomToExtent(
                        OpenLayers.Bounds.fromArray(bb[key].bbox)
                    );
                    zoomed = true;
                    break;
                }
            }
            if (!zoomed) {
                // use llbbox
                var llbbox = OpenLayers.Bounds.fromArray(
                    r.get('llbbox')
                );
                llbbox.transform(
                    new OpenLayers.Projection('EPSG:4326'),
                    map.getProjectionObject()
                );
                map.zoomToExtent(llbbox);
            }
        };

        // recenter action
        menuItems.push({
            iconCls: 'geor-btn-zoom',
            text: tr("Recenter on the layer"),
            listeners: {
                "click": function(btn, pressed) {
                    var layer = layerRecord.get('layer'),
                        map = layer.map;
                    // TODO: layer.getDataExtent() can be null if layer strategy is bbox
                    // and there's no feature currently in layer.
                    // It seems WFS capabilities has a llbbox field in record => parse it
                    if (isWFS) {
                        var b = layer.getDataExtent();
                        if (b && b.getWidth() * b.getHeight()) {
                            map.zoomToExtent(b);
                        }
                    } else {
                        if (!layerRecord.get('bbox') && !layerRecord.get('llbbox')) {
                            // Get it from the WMS GetCapabilities document
                            GEOR.ows.hydrateLayerRecord(layerRecord, {
                                success: function(){
                                    zoomToLayerRecordExtent(layerRecord);
                                },
                                failure: function() {
                                    GEOR.util.errorDialog({
                                        msg: tr("Impossible to get layer extent")
                                    });
                                },
                            scope: this
                            });
                        } else {
                            zoomToLayerRecordExtent(layerRecord);
                        }
                    }
                }
            }
        });

        // redraw action (aka "do not used client-cached layer")
        if (!isWFS) {
            menuItems.push({
                iconCls: 'geor-btn-refresh',
                text: tr("Refresh layer"),
                listeners: {
                    "click": function(btn, pressed) {
                        layerRecord.get('layer').mergeNewParams({
                            nocache: new Date().valueOf()
                        });
                    }
                }
            });
        }

        var insertSep = function() {
            if (!sepInserted) {
                menuItems.push("-");
                sepInserted = true;
            }
        };
        
        // metadata action
        if (layerRecord.get("metadataURLs") &&
            layerRecord.get("metadataURLs")[0]) {
            url = layerRecord.get("metadataURLs")[0];
            url = (url.href) ? url.href : url;
            insertSep();
            menuItems.push({
                iconCls: 'geor-btn-metadata',
                text: tr("Show metadata"),
                listeners: {
                    "click": function(btn, pressed) {
                        window.open(url);
                    }
                }
            });
        }
        if (GEOR.styler && isWMS && queryable) {
            insertSep();
            menuItems.push({
                iconCls: 'geor-btn-style',
                text: tr("Edit symbology"),
                listeners: {
                    "click": function(btn, pressed) {
                        GEOR.styler.create(layerRecord, this.el);
                    }
                }
            });
        }

        // TODO: queryable is not the correct boolean here to decide whether
        // we can have the querier or not.
        // The availability of a WFS equivalent layer is.
        // This depends on http://applis-bretagne.fr/redmine/issues/1984

        if (GEOR.querier && ((isWMS && queryable) || isWFS)) {
            insertSep();
            menuItems.push({
                iconCls: 'geor-btn-query',
                text: tr("Build a query"),
                listeners: {
                    "click": function(btn, pressed) {
                        if (layerRecord == querierRecord) {
                            // FIXME (later) : this is not how it should be.
                            // (only the module should fire its own events)
                            GEOR.querier.events.fireEvent("showrequest");
                            return;
                        }
                        var layer = layerRecord.get('layer');
                        var name = layerRecord.get('title') || layer.name || '';
                        if (isWFS) {
                            var recordType = GeoExt.data.LayerRecord.create([
                                {name: "featureNS", type: "string"},
                                {name: "owsURL", type: "string"},
                                {name: "typeName", type: "string"}
                            ]);
                            GEOR.querier.create(name, new recordType({
                                "featureNS": layerRecord.get('namespace'),
                                "owsURL": layer.protocol.url,
                                "typeName": layerRecord.get('name')
                            }, layer.id));
                        } else { // WMS layer
                            querierRecord = layerRecord;
                            // all this code should be moved elsewhere, see http://applis-bretagne.fr/redmine/issues/1984 (later)
                            GEOR.waiter.show();
                            GEOR.ows.WMSDescribeLayer(layerRecord, {
                                success: function(store, records) {
                                    var r = GEOR.ows.getWfsInfo(records);
                                    if (!r) {
                                        GEOR.util.errorDialog({
                                            msg: tr("Failed to get WFS layer address." +
                                                 "<br />The query module will be disabled")
                                        });
                                        return;
                                    }
                                    GEOR.querier.create(name, r);
                                },
                                failure: function() {
                                    GEOR.util.errorDialog({
                                        msg: tr("DescribeLayer WMS query failed." +
                                                 "<br />The query module will be disabled")
                                    });
                                },
                                storeOptions: {
                                    fields: [
                                        {name: "owsType", type: "string"},
                                        {name: "owsURL", type: "string"},
                                        {name: "typeName", type: "string"},
                                        // and we need to add a special featureNS field
                                        // which will be filled by WFSDescribeFeatureType:
                                        {name: "featureNS", type: "string"}
                                    ]
                                },
                                scope: this
                            });
                        }
                    }
                }
            });
        }

        if (!isWMTS) {
            insertSep();
            menuItems.push({
                iconCls: 'geor-btn-download',
                text: tr("Download data"),
                handler: function() {
                    submitData({
                        layers: [{
                            layername: layerRecord.get('name'),
                            metadataURL: url || "",
                            owstype: isWMS ? "WMS" : "WFS",
                            owsurl: isWMS ? layer.url : layer.protocol.url
                        }]
                    })
                }
            });
        }

        if (!isWFS) {
            menuItems.push("-");
            stylesMenu = createStylesMenu(layerRecord);
            menuItems.push({
                text: tr("Choose a style"),
                menu: stylesMenu
            });
            menuItems.push({
                text: tr("Modify format"),
                menu: createFormatMenu(layerRecord)
            });
        }

        return new Ext.menu.Menu({
            ignoreParentClicks: true,
            items: menuItems
        });
    };

    /**
     * Method: createLayerNodePanel
     *
     * Parameters:
     * node - {GeoExt.tree.LayerNode} The layer node.
     * ct - {Ext.Element} The container element.
     *
     * Returns:
     * {Ext.Panel} The panel to add in the node.
     */
    var createLayerNodePanel = function(node, ct) {
        var layer = node.layer;
        var layerRecord = node.layerStore.getById(layer.id);

        // buttons in the toolbar
        var buttons = [];
        if (GEOR.getfeatureinfo) {
            buttons.push(createGfiButton(layerRecord));
        }
        buttons = buttons.concat([
        {
            text: tr("Actions"),
            menu: createMenu(layerRecord)
        }, '-', {
            xtype: "gx_opacityslider",
            width: 100,
            // hack for http://applis-bretagne.fr/redmine/issues/2026 :
            topThumbZIndex: 1000,
            // and this is because GeoExt.LayerOpacitySlider defaults
            // are too much for me :
            delay: 50,
            changeVisibilityDelay: 50,
            // we're also monitoring the layer visibility:
            changeVisibility: true,
            aggressive: true,
            layer: layer,
            plugins: new GeoExt.LayerOpacitySliderTip()
        }]);

        var panelItems = [{
            xtype: "toolbar",
            cls: "geor-toolbar",
            buttons: buttons
        }];
        if (GEOR.config.DISPLAY_VISIBILITY_RANGE) {
            panelItems.push(formatVisibility(layerRecord), {
                xtype: 'box',
                autoEl: {
                    tag: 'span',
                    style: 'padding: 0 5px;',
                    html: '|'
                }
            });
        }
        panelItems.push(formatAttribution(layerRecord));

        // return the panel
        return {
            xtype: "panel",
            border: false,
            cls: "gx-tree-layer-panel",
            // we add a class to the bwrap element
            // to avoid the default overflow:hidden
            // behavior which prevents the visibility
            // range from being fully displayed
            bwrapCssClass: "gx-tree-layer-panel-bwrap",
            bodyCfg: {
                // we use our own class for the panel
                // body, this is to avoid the white
                // background of .x-panel-body
                cls: "geor-tree-layer-panel-body"
            },
            items: panelItems,
            // add a method to unselect all predefined which
            // keeps a references on the styles menu
            unselectStyles: function() {
                stylesMenu.items.each(function(item) {
                    if (item instanceof Ext.menu.CheckItem) {
                        item.setChecked(false);
                    }
                });
            }
        };
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
         * Return the layer view config.
         *
         * Parameters:
         * layerStore - {GeoExt.data.LayerStore} The application layer
         *     store.
         */
        create: function(layerStore) {
            tr = OpenLayers.i18n;
            Ext.QuickTips.init();
            // create the layer container
            layerContainer = new GeoExt.tree.LayerContainer({
                layerStore: layerStore,
                loader: {
                    baseAttrs: {
                        nodeType: "geor_layer",
                        cls: "geor-tree-node",
                        uiProvider: "ui",
                        actions: [{
                            action: "delete",
                            qtip: tr("Delete this layer")
                        }, {
                            action: "up",
                            qtip: tr("Push up this layer"),
                            update: function(el) {
                                if (this.isFirst()) {
                                    if (!this._updating &&
                                        this.nextSibling &&
                                        this.nextSibling.hidden === false) {

                                        this._updating = true; // avoid recursion
                                        this.getOwnerTree().plugins[0].updateActions(this.nextSibling);
                                        delete this._updating;
                                    }
                                    el.setVisibilityMode(Ext.Element.DISPLAY);
                                    el.hide();
                                } else {
                                    el.show();
                                }
                            }
                        }, {
                            action: "down",
                            qtip: tr("Push down this layer"),
                            update: function(el) {
                                var isLast = this.isLast();
                                if (isLast) {
                                    if (!this._updating &&
                                        this.previousSibling &&
                                        this.previousSibling.hidden === false) {

                                        this._updating = true; // avoid recursion
                                        this.getOwnerTree().plugins[0].updateActions(this.previousSibling);
                                        delete this._updating;
                                    }
                                    el.setVisibilityMode(Ext.Element.DISPLAY);
                                    el.hide();
                                } else {
                                    el.show();
                                }
                                return isLast;
                            }
                        }],
                        component: createLayerNodePanel
                    }
                }
            });

            return {
                xtype: "treepanel",
                autoScroll: true,
                enableDD: true,
                loader: {
                    applyLoader: false,
                    uiProviders: {
                        "ui": Ext.extend(
                            GeoExt.tree.LayerNodeUI,
                            new GeoExt.tree.TreeNodeUIEventMixin())
                    }
                },
                // apply the tree node actions plugin to layer nodes
                plugins: [{
                    ptype: "gx_treenodeactions",
                    listeners: {
                        action: actionHandler
                    }
                }, {
                    ptype: "gx_treenodecomponent"
                }],
                lines: false,
                rootVisible: false,
                root: layerContainer,
                buttons: [{
                    text: tr("Add layers"),
                    iconCls: 'btn-add',
                    handler: function() {
                        if (!layerFinder) {
                            layerFinder = GEOR.layerfinder.create(layerStore, this.el);
                        }
                        layerFinder.show();
                    }
                }]
            };
        },

        /**
         * APIMethod: unselectStyles
         * Unselect predefined styles associated to a layer record
         *
         * Parameters:
         * layerRecord - {GeoExt.data.LayerRecord} A layer record.
         */
        unselectStyles: function(layerRecord) {
            var nodes = layerContainer.childNodes;
            for (var i=0, len=nodes.length; i<len; i++) {
                if (nodes[i].layer.id === layerRecord.id) {
                    nodes[i].component.unselectStyles();
                    break;
                }
            }
        }
    };
})();
