/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @requires GeoExt/widgets/tree/LayerNode.js
 * @include GeoExt/widgets/tips/LayerOpacitySliderTip.js
 * @include GeoExt/widgets/LayerOpacitySlider.js
 * @include GeoExt/widgets/tree/LayerContainer.js
 * @include GEOB_layerfinder.js
 * @include GEOB_util.js
 */

Ext.namespace("GEOB");

GEOB.managelayers = (function() {
    /*
     * Private
     */

    /**
     * Class: LayerNode
     * Our own LayerNode class.
     */
    var LayerNode = Ext.extend(GeoExt.tree.LayerNode, {
        constructor: function(config) {
            config.text = GEOB.util.shortenLayerName(config.layer);
            config.qtip = config.layer.name;
            LayerNode.superclass.constructor.apply(this, [config]);
        }
    });
    Ext.tree.TreePanel.nodeTypes.geob_layer = LayerNode;

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
            GEOB.util.confirmDialog({
                msg: "Voulez-vous réellement supprimer la couche "+
                    layerRecord.get('title')+" ?",
                width: 360,
                yesCallback: function() {
                    layer.destroy();
                },
                scope: this
            });
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
            this.get('layer').mergeNewParams({
                FORMAT: item.value
            });
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
        var attrDisplay = (attr.href) ? 
            '<a href="'+attr.href+'" target="_blank" ext:qtip="'+attr.title+'">'+titleForDisplay+'</a>' :
            '<span ext:qtip="source : '+attr.title+'">'+titleForDisplay+'</span>';
        
        return {
            xtype: 'box',
            cls: "geob-layers-form-text",
            autoEl: {
                tag: 'span',
                html: ' | source : '+attrDisplay
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
        var visibilityText = "1:" + OpenLayers.Number.format(layer.maxScale, 0) +
                          " à 1:" + OpenLayers.Number.format(layer.minScale, 0);
        return {
            xtype: 'box',
            cls: "geob-layers-form-text",
            autoEl: {
                tag: 'span',
                'ext:qtip': "Plage de visibilité (indicative) :<br />de "+visibilityText,
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
            iconCls: 'geob-btn-info',
            allowDepress: false, // false is required here because
                                 // we do not have a default button
                                 // in toggleGroup
            enableToggle: true,
            toggleGroup: 'map',
            tooltip: "Interroger les objets de cette couche",
            listeners: {
                "toggle": function(btn, pressed) {
                    GEOB.getfeatureinfo.toggle(layerRecord, pressed);
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
        var formats = layerRecord.get("formats");
        var layer = layerRecord.get("layer");
        
        var formatMenuItems = [];
        if (formats && formats.length) {
            for (var i=0, len=formats.length; i<len; i++) {
                var value = formats[i].value || formats[i];
                if (GEOB.config.ACCEPTED_MIME_TYPES.indexOf(value) > -1) {
                    formatMenuItems.push(new Ext.menu.CheckItem({
                        text: value,
                        value: value,
                        checked: (formats[i].current && 
                            formats[i].current === true) || 
                            (layer.params.FORMAT === formats[i]),
                        group: 'format_' + layer.id,
                        checkHandler: onFormatItemCheck,
                        scope: layerRecord
                    }));
                }
            }
        } else {
            // we display the only one we know : the current one
            formatMenuItems.push(new Ext.menu.CheckItem({
                text: layer.params.FORMAT,
                value: layer.params.FORMAT,
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
        var styles = layerRecord.get("styles");
        var layer = layerRecord.get("layer");
        
        var default_style = {
            text: 'Style par défaut',
            value: null,
            checked: true,
            group: 'style_' + layer.id,
            checkHandler: onStyleItemCheck,
            scope: layerRecord
        };
        // build object config for predefined styles
        var stylesMenuItems = [
            '<b class="menu-title">Choisissez un style</b>',
            default_style
        ];
        if (styles && styles.length > 0) {
            var style, checked;
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
                        text: styles[i].title, // title is a human readable string
                        value: styles[i].name, // name is used in the map request STYLE parameter
                        checked: checked,
                        group: 'style_' + layer.id,
                        checkHandler: onStyleItemCheck,
                        scope: layerRecord
                    }));
                }
            }
        }
        return new Ext.menu.Menu({
            items: stylesMenuItems
        });
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
        var queryable = !!(layerRecord.get("queryable"));
        
        var menuItems = [], url;
        if (layerRecord.get("metadataURLs") && 
            layerRecord.get("metadataURLs")[0]) {
            url = layerRecord.get("metadataURLs")[0];
            url = (url.href) ? url.href : url;
            menuItems.push({
                iconCls: 'geob-btn-metadata',
                text: "Fiche de métadonnées",
                listeners: {
                    "click": function(btn, pressed) {
                        window.open(url);
                    }
                }
            });
        }
        if (GEOB.styler && queryable) {
            menuItems.push({
                iconCls: 'geob-btn-style',
                text: "Editer la symbologie",
                listeners: {
                    "click": function(btn, pressed) {
                        GEOB.styler.create(layerRecord);
                    }
                }
            });
        }
        if (GEOB.querier && queryable) {
            menuItems.push({
                iconCls: 'geob-btn-query',
                text: "Construire une requête",
                listeners: {
                    "click": function(btn, pressed) {
                        GEOB.querier.create(layerRecord);
                    }
                }
            });
        }
        if (menuItems.length > 0) {
            menuItems.push("-");
        }
        
        stylesMenu = createStylesMenu(layerRecord);
        menuItems.push({
            text: 'Styles prédéfinis',
            disabled: !queryable,
            menu: stylesMenu
        });
        menuItems.push({
            text: "Format d'image",
            menu: createFormatMenu(layerRecord)
        });
        
        return new Ext.menu.Menu({
            disabled: !queryable,
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
        if (GEOB.getfeatureinfo) {
            buttons.push(createGfiButton(layerRecord));
        }
        buttons = buttons.concat([{
            text:'Actions',
            menu: createMenu(layerRecord)
        }, '-',{
            xtype: "gx_opacityslider",
            width: 100,
            aggressive: true,
            layer: layer,
            plugins: new GeoExt.LayerOpacitySliderTip()
        }]);
        
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
                cls: "geob-tree-layer-panel-body"
            },
            items: [
                {
                    xtype: "toolbar",
                    cls: "geob-toolbar",
                    buttons: buttons
                }, 
                formatVisibility(layerRecord), 
                formatAttribution(layerRecord)
            ],
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
            Ext.QuickTips.init();
            // create the layer container
            layerContainer = new GeoExt.tree.LayerContainer({
                layerStore: layerStore,
                loader: {
                    baseAttrs: {
                        nodeType: "geob_layer",
                        cls: "geob-tree-node",
                        actions: [{
                            action: "delete",
                            qtip: "supprimer cette couche"
                        }, {
                            action: "up",
                            qtip: "monter cette couche",
                            update: function(el) {
                                var isFirst = this.isFirst();
                                if (isFirst) {
                                    if (!this._updating &&
                                        this.nextSibling &&
                                        this.nextSibling.hidden === false) {

                                        this._updating = true; // avoid recursion
                                        this.nextSibling.getUI().updateActions();
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
                            qtip: "descendre cette couche",
                            update: function(el) {
                                var isLast = this.isLast();
                                if (isLast) {
                                    if (!this._updating &&
                                        this.previousSibling &&
                                        this.previousSibling.hidden === false) {

                                        this._updating = true; // avoid recursion
                                        this.previousSibling.getUI().updateActions();
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
                loader: {
                    applyLoader: false
                },
                lines: false,
                rootVisible: false,
                root: layerContainer,
                buttons: [{
                    text: 'Ajouter des couches',
                    handler: function() {
                        if (!layerFinder) {
                            layerFinder = GEOB.layerfinder.create(layerStore);
                        }
                        layerFinder.show();
                    }
                }],
                listeners: {
                    action: actionHandler
                }
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
