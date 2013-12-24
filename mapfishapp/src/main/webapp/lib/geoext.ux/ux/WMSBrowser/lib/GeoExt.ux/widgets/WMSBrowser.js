/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/*
 * @requires GeoExt/widgets/Action.js
 */

/** api: (define)
 *  module = GeoExt.ux
 *  class = WMSBrowser
 */

/** api: constructor
 *  .. class:: WMSBrowser
 */
GeoExt.ux.WMSBrowser = Ext.extend(Ext.Panel, {

    /** private: property[CUSTOM_EVENTS]
     *  ``Array(String)`` Array of custom events used by this widget
     */
    CUSTOM_EVENTS: [
        "beforegetcapabilities",
        "getcapabilitiessuccess",
        "getcapabilitiesfail",
        "genericerror",
        "beforelayeradded",
        "layeradded"
    ],

    /** private: property[DEFAULT_CAPABILITIES_PARAMS]
     *  ``Array(String)`` Array of default GetCapabilities request parameters
     */
    DEFAULT_CAPABILITIES_PARAMS: {
       'service': "WMS",
       'request': "GetCapabilities",
       'version': '1.1.1'
    },

    /** private: property[DEFAULT_LAYER_BROWSER_XTYPE]
     *  ``String`` Default layer browser xtype to use if not defined
     */
    DEFAULT_LAYER_BROWSER_XTYPE: Ext.tree.TreePanel.xtype,

    /* begin i18n */
    /** api: config[inputURLText] ``String`` i18n */
    inputURLText: "Select or input a server address (URL)",

    /** api: config[connectText] ``String`` i18n */
    connectText: "Connect",

    /** api: config[pleaseInputURLText] ``String`` i18n */
    pleaseInputURLText: "Please, select or input a server adress (URL) in the" +
                        " dropdown list first.",

    /** api: config[srsCompatibleText] ``String`` i18n */
    srsCompatibleText: "SRS compatible",

    /** api: config[extentCompatibleText] ``String`` i18n */
    extentCompatibleText: "Extent compatible",

    /** api: config[titleText] ``String`` i18n */
    titleText: "Title",

    /** api: config[nameText] ``String`` i18n */
    nameText: "Name",

    /** api: config[queryableText] ``String`` i18n */
    queryableText: "Queryable",

    /** api: config[descriptionText] ``String`` i18n */
    descriptionText: "Description",

    /** api: config[yesText] ``String`` i18n */
    yesText: "Yes",

    /** api: config[noText] ``String`` i18n */
    noText: "No",

    /** api: config[addLayerText] ``String`` i18n */
    addLayerText: "Add layer",

    /** api: config[addSelectedLayersText] ``String`` i18n */
    addSelectedLayersText: "Add currently selected layers as one single layer.",

    /** api: config[mapPanelPreviewTitleText] ``String`` i18n */
    mapPanelPreviewTitleText: "Map preview",

    /** api: config[layerCantBeAddedText] ``String`` i18n */
    layerCantBeAddedText: "This layer can't be added : ",

    /** api: config[srsNotSupportedText] ``String`` i18n */
    srsNotSupportedText: "This layer can't be added to the current map" + 
                         " because it doesn't support its projection.",

    /** api: config[srsNotSupportedShortText] ``String`` i18n */
    srsNotSupportedShortText: "it doesn't support the map current projection",

    /** api: config[extentNotSupportedShortText] ``String`` i18n */
    extentNotSupportedShortText: "it is out of the map bounds",

    /** api: config[pleaseSelectALayerText] ``String`` i18n */
    pleaseSelectALayerText: "Please, select one or more layers from the grid first.",

    /** api: config[pleaseSelectALayerText] ``String`` i18n */
    pleaseCheckALayerInTreeText: "Please, check one or more layers from the tree first.",

    /** api: config[closeWindowText] ``String`` i18n */
    closeWindowText: "Close this window",

    /** api: config[closeText] ``String`` i18n */
    closeText: "Close",

    /** api: config[inputURLInvalidText] ``String`` i18n */
    inputURLInvalidText: "The url address entered is not valid.",

    /** api: config[layerNameText] ``String`` i18n */
    layerNameText: "Layer name :",

    /** api: config[noLayerReturnedText] ``String`` i18n */
    noLayerReturnedText: "The url address is valid but returned no layers.",

    /** api: config[layersSuccessfullyLoadedText] ``String`` i18n */
    layersSuccessfullyLoadedText: "Layers successfully loaded.",

    /** api: config[layerAddedText] ``String`` i18n */
    layerAddedText: "Layer(s) successfully added to the map.",

    /** api: config[urlInvalidText] ``String`` i18n */
    urlInvalidText: "The server address (url) is invalid or is not a valid WMS server.",

    /** api: config[pleaseInputLayerNameText] ``String`` i18n */
    pleaseInputLayerNameText: "Please, input a layer name in the textfield below.",

    /** api: config[warningText] ``String`` i18n */
    warningText: "Warning",

    /** api: config[errorText] ``String`` i18n */
    errorText: "Error",
    /* end i18n */

    /** api: config[layerStore]
     *  ``GeoExt.data.LayerStore`` A store holding records.  Mandatory.
     */

    /** api: property[layerStore]
     *  :class:`GeoExt.data.LayerStore`  A store containing
     *  :class:`GeoExt.data.LayerRecord` objects.
     */
    layerStore: null,

    /** api: config[serverStore]
     *  ``Ext.data.SimpleStore``
     *  A store holding records.  If not provided, an empty
     *  :class:`Ext.data.SimpleStore` will be created.
     */

    /** api: property[serverStore]
     *  :class:`Ext.data.SimpleStore`  A store containing
     *  :class:`Ext.data.Record` objects for server urls.
     */
    serverStore: null,

    /** api: config[serverStoreDisplayField]
     *  ``String``  The field to display in the dropdown list using the
     *              serverStore.  Defaults to 'url'.
     */
    serverStoreDisplayField: 'url',

    /** api: config[layout]
     *  ``String``  The default 'layout' value.
     */
    layout: 'border',

    /** api: config[defaultType]
     *  ``String``  The default 'defaultType' value.
     */
    defaultType: 'textfield',

    /** api: config[defaults]
     *  ``Object``  The default 'defaults' value.
     */
    defaults: {
        style:'padding:2px;margin:0px;'
    },

    /** api: config[capabilitiesParams]
     *  ``Object``  Hash of parameters for the GetCapabilities requests.  Each
     *              defined parameter will overwrites the 
     *              DEFAULT_CAPABILITIES_PARAMS
     */
    capabilitiesParams: {},

    /** api: config[mapPanelPreviewOptions]
     *  ``Object``  Hash of options for the mapPanelPreview object.  Each
     *              defined option will overwrites the default according option
     *              of the object when creating it.
     */
    mapPanelPreviewOptions: {},

    /** api: config[layerNameFieldOptions]
     *  ``Object``  Hash of options for the layerNameField object.  Each
     *              defined option will overwrites the default according option
     *              of the object when creating it.
     */
    layerNameFieldOptions: {},

    /** api: config[layerOptions]
     * ``Object`` optional object passed as default options
     * ``OpenLayers.Layer.WMS`` constructor
     */
    layerOptions: null,

    /** api: config[useIcons]
     * ``Boolean`` Whether the buttons should use icons or not.  Defaults to
     *             false.
     */
    useIcons: false,

    /** api: config[zoomOnLayerAdded]
     * ``Boolean`` Whether the map should zoom to the newly added layer's extent
     *             or not.  Defaults to false.
     */
    zoomOnLayerAdded: false,

    /** api: config[closeOnLayerAdded]
     * ``Boolean`` Whether the window containing the widget should automatically
     *             closes after a layer has been added to the map or not.
     *             Defaults to false.  Only working of widget is inside a
     *             :class:`Ext.Window` object.
     */
    closeOnLayerAdded: false,

    /** api: config[allowInvalidUrl]
     * ``Boolean`` Whether invalid urls should be allowed to be inputed inside
     *             the server dropdown list or not.  If set to false, then
     *             if an invalid url is inputed, no query will be made.
     *             Defaults to false.
     */
    allowInvalidUrl: false,

    /** api: config[selectFirstRecordOnStoreLoad]
     * ``Boolean`` Whether the first record in the grid should be automatically
     *             selected after a GetCapabilities request or not.  Defaults to
     *             false.
     */
    selectFirstRecordOnStoreLoad: false,

    /** api: config[alertPopupAutoHide]
     * ``Boolean`` Whether the alert popups should be automatically hidden
     *             after a certain period of time or not.  Defaults to false.è
     *             Only used if no :class:`GeoExt.ux.WMSBrowserStatusBar` is
     *             set.
     */
    alertPopupAutoHide: false,

    /** api: config[proxyHost]
     * ``String`` The url to a proxy to use for this widget only.  If not set,
     *            it uses OpenLayers.ProxyHost instead (if set).
     *            Example of value : "./proxy?url="
     */
    proxyHost: null,

    /** api: config[layerBrowserXtype]
     * ``String`` The xtype of layer browser to use (component on the left).
     *  Supported value are : 
     *  - Ext.grid.GridPanel.xtype
     *  - Ext.tree.TreePanel.xtype
     */
    layerBrowserXtype: null,

    /** api: config[alertPopupTimeout]
     * ``Integer`` The 'popupTimeout' property to set the 
     * :class:`GeoExt.ux.plugins.WMSBrowserAlerts` plugin.  Only used if no
     * :class:`GeoExt.ux.WMSBrowserStatusBar` is set.
     */
    alertPopupTimeout: 4000,

    /** private: property[currentUrl]
     *  ``String`` Used to keep track of the url while a query is processing.
     */
    currentUrl: null,
 
    /** private: property[layerPreview]
     *  :class:`GeoExt.data.LayerRecord`  Used to create the layer preview
     */
    layerPreview: null,

    /** private: property[mapPanelPreview]
     *  :class:`GeoExt.MapPanel`  Used to create the map preview (containing the
     *                            layer preview)
     */
    mapPanelPreview: null,

    /** private: prperty[gridPanel]
     *  :class:`Ext.grid.GridPanel`  The grid used to display the 
     *  :class:`GeoExt.data.LayerRecord` created inside the
     *  :class:`GeoExt.data.WMSCapabilitiesStore`.  Not used if treePanel is
     *  defined (see below).
     */
    gridPanel: null,

    /** private: prperty[infoPanel]
     *  :class:`Ext.Panel`  The panel where the description and map are
     *                      displayed
     */
    infoPanel: null,

    /** private: property[centerPanel]
     *  :class:`Ext.form.FormPanel`  The panel used as a 'center' container
     */
    centerPanel: null,

    /** private: property[serverComboBox]
     *  :class:`Ext.form.ComboBox`  The dropdown list of WMS servers
     */
    serverComboBox: null,

    /** private: property[layerNameField]
     *  :class:`Ext.form.TextField`  The field used to contain the layer's name
     */
    layerNameField: null,

    /** private: property[statusBar]
     *  :class:`GeoExt.ux.WMSBrowserStatusBar`  If Ext.ux.StatusBar class is
     *  present, it will automatically added to this widget.
     */
    statusBar: null,

    /** private: property[treePanel]
     *  :class:`GeoExt.ux.WMSBrowserTreePanel`  Only set if using when using
     * 'layerBrowserXtype' == Ext.tree.TreePanel.xtype
     */
    treePanel: null,

    /** private: method[constructor]
     */
    constructor: function(config) {
        Ext.apply(this, config);

        if (!this.serverStore) {
            this.serverStore = 
                new Ext.data.SimpleStore({fields: ['url'], data : []});
        }
        
        Ext.applyIf(this.capabilitiesParams, this.DEFAULT_CAPABILITIES_PARAMS);
        this.layerBrowserXtype =
            this.layerBrowserXtype || this.DEFAULT_LAYER_BROWSER_XTYPE;

        this.addEvents(this.CUSTOM_EVENTS);

        this.initMyItems();
        this.initMyToolbar();

        arguments.callee.superclass.constructor.call(this, config);

        this.on("afterrender", this.onAfterRender, this);
    },

    /** private: method[initMyItems]
     *
     *  Initializes the widget items.  Create the north and center panels in
     *  which there are :
     *  - a dropdown list of server urls
     *  - a grid panel containing layer records
     *  - a form panel containing layer info when one layer record is selected
     *  - a mappanel used for the preview
     */
    initMyItems: function() {
        // north (connection informations)
        this.serverComboBox = new Ext.form.ComboBox({
            style:'padding:0px;margin:0px;',
            columnWidth: 0.85,
            'name': 'wms_url',
            xtype: 'combo',
            store: this.serverStore,
            displayField: this.serverStoreDisplayField,
            typeAhead: true,
            mode: 'local',
            forceSelection: false,
            triggerAction: 'all',
            allowBlank: false,
            validator:this.urlValidator,
            invalidText: this.inputURLInvalidText,
            emptyText: this.inputURLText,
            selectOnFocus:true
        });

        var northPanel = new Ext.form.FormPanel({
            height: 'auto',
            autoHeight: true,
            border: false,
            region: 'north',
            layout: 'column',
            items: [this.serverComboBox, {
                columnWidth: 0.15,
                width: '100%',
                style:'padding:0px;margin:0px;',
                xtype: 'button',
                text: this.connectText,
                scope: this,
                handler: function(b, e){this.triggerGetCapabilities();}
            }]
        });

        // center (capabilities grid and info, mapPanel)
        this.mapPanelPreview = new GeoExt.MapPanel(
            Ext.applyIf(this.mapPanelPreviewOptions, {
                xtype: "gx_mappanel",
                title: this.mapPanelPreviewTitleText,
                region: 'south',
                collapsible: true,
                collapsed: true,
                border: false,
                height: 200,
                floatable: false,
                minSize: 100,
                split: true,
                layers: [new OpenLayers.Layer("dummy")]
            })
        );
        this.mapPanelPreview.on("collapse", this.hideLayerPreview, this);
        this.mapPanelPreview.on("expand", this.showLayerPreview, this);

        switch (this.layerBrowserXtype)
        {
          case Ext.grid.GridPanel.xtype:
            this.capStore = this.createCapStore();
            this.gridPanel = this.createGridPanel();
            this.capStore.gridPanel = this.gridPanel;
            this.layerBrowser = this.gridPanel;
            break;
          case Ext.tree.TreePanel.xtype:
            this.treePanel = this.createTreePanel();
            this.layerBrowser = this.treePanel;
            break;
        }

        this.statusBar = (GeoExt.ux.WMSBrowserStatusBar)
            ? new GeoExt.ux.WMSBrowserStatusBar({'wmsbrowser': this})
            : null;

        if (!this.statusBar) {
            Ext.apply(this, {plugins: [new GeoExt.ux.plugins.WMSBrowserAlerts()]});
        }

        this.infoPanel = new Ext.Panel({
            anchor: '50% 100%',
            x: '50%',
            y: '0',
                    region: 'east',
                    width: '50%',
            layout: 'border',
            border: true,
            items: [
                this.createFormPanel(), 
                this.mapPanelPreview
            ]
        });

        var centerPanel = new Ext.form.FormPanel({
            tbar: this.statusBar,
            region: 'center',
            layout: 'border',
            border: false,
            items: [this.treePanel || this.gridPanel, this.infoPanel]
        });

        centerPanel.on('bodyresize', function(){
            this.infoPanel.setWidth('50%');
        }, this);

        this.centerPanel = centerPanel;

        Ext.apply(this, {items: [northPanel, centerPanel]});
    },

    /** private: method[createCapStore]
     *  :return:  :class:`GeoExt.ux.data.WMSBrowserWMSCapabilitiesStore`
     *
     *  Creates and returns a
     *  :class:`GeoExt.ux.data.WMSBrowserWMSCapabilitiesStore` binded to this
     *  widget.  Used to query and hold the layer records.
     */
    createCapStore: function() {
        return new GeoExt.ux.data.WMSBrowserWMSCapabilitiesStore({
            'url': "",
            layerOptions: this.layerOptions,
            wmsbrowser: this
        });
    },

    /** private: method[createGridPanel]
     *  :return:  ``GeoExt.ux.grid.WMSBrowserGridPanel``
     *
     * Creates and returns :class:`GeoExt.ux.grid.WMSBrowserGridPanel` binded
     * to this widget to display and select the layer records.
     */
    createGridPanel: function() {
        return new GeoExt.ux.grid.WMSBrowserGridPanel({
            store: this.capStore,
            wmsbrowser: this
        });
    },

   /** private: method[createTreePanel]
     *  :return:  ``GeoExt.ux.tree.WMSBrowserTreePanel``
     *
     * Creates and returns :class:`GeoExt.ux.tree.WMSBrowserTreePanel` binded
     * to this widget to display and select the layers from nodes in a tree.
     */
    createTreePanel: function() {
        var options = {'wmsbrowser': this};
        return new GeoExt.ux.tree.WMSBrowserTreePanel(options);
    },

    /** private: method[createFormPanel]
     *  :return:  ``Object``
     *
     * Creates and returns the form panel options used to display some
     * informations when a layer is selected in the grid.
     */
    createFormPanel: function() {
        this.descriptionField = new Ext.form.TextArea({
            name: 'abstract',
            x: 0,
            y: 15,
            anchor: '100% 100%'
        });

        var options = {
            xtype: 'fieldset',
            layout: 'absolute',
            region: 'center',
            labelWidth: 80,
            anchor: '100% 100%',
            defaultType: 'textfield',
            border: false,
            items: [{
                x: 0,
                y: 0,
                xtype:'label',
                text: this.descriptionText
            }, this.descriptionField]
        };

        return options;
    },

    /** private: method[resetCenterFormPanel]
     *  Reset the center form panel.
     */
    resetCenterFormPanel: function() {
        this.centerPanel.getForm().reset();
    },

    /** private: method[initMyToolbar]
     *
     *  Initializes the widget toolbars.  The bottom toolbars contains :
     *  - a textfield used to hold the layer's name
     *  - 'add' and 'close' buttons.
     */
    initMyToolbar: function() {
        var items = [];

        items.push('->');

        // LayerName textfield
        items.push({
            xtype: 'label',
            text: this.layerNameText
        });
        this.layerNameField = new Ext.form.TextField(
            Ext.applyIf(
                this.layerNameFieldOptions, {
                    width: 275,
                    xtype: 'textfield'
            })
        );
        items.push(this.layerNameField);

        items.push('-');

        // AddLayer action
        var actionOptions = {
            handler: this.addLayer,
            scope: this,
            tooltip: this.addSelectedLayersText
        };

        if (this.useIcons === true) {
            actionOptions.iconCls = "gx-wmsbrowser-addlayer";
        } else {
            actionOptions.text = this.addLayerText;
        }

        var action = new Ext.Action(actionOptions);
        items.push(action);

        Ext.apply(this, {bbar: new Ext.Toolbar(items)});
    },

    /** private: method[triggerGetCapabilities]
     *
     *  Called when the user clicks the 'connect' button.  Validate that the
     *  url is valid and trigger a GetCapabilities request.
     */
    triggerGetCapabilities: function() {
        var url = this.serverComboBox.getValue();
        this.resetAll();

        // if url in not valid
        if(!this.serverComboBox.isValid()) {
            // if url is blank, throw error
            if(!url) {
                this.fireEvent('genericerror', this.pleaseInputURLText);
                return;
            }
            // if url is not blank and the widget don't allow invalid urls, 
            // throw error
            else if (!this.allowInvalidUrl){
                this.fireEvent('genericerror', this.inputURLInvalidText);
                return;
            }
        }

        this.fireEvent('beforegetcapabilities');

        // keep the inputed url in order to add it to the url store later if
        // it was valid
        this.currentUrl = url;

        // add the GetCapabilities parameters to the url
        var params = OpenLayers.Util.getParameterString(this.capabilitiesParams);        
        url = OpenLayers.Util.urlAppend(url, params);

        // check for local proxyHost setting, then OpenLayers.ProxyHost
        if (this.proxyHost && OpenLayers.String.startsWith(url, "http")) {
            url = this.proxyHost + encodeURIComponent(url);
        } else if (OpenLayers.ProxyHost &&
                   OpenLayers.String.startsWith(url, "http")) {
            url = OpenLayers.ProxyHost + encodeURIComponent(url);
        }

        if (this.gridPanel) {
            // change the url of the capability store proxy
            this.capStore.proxy.setUrl(url);
            this.capStore.proxy.setApi(Ext.data.Api.actions.read, url);

            this.capStore.load();
        } else if (this.treePanel) {
            this.treePanel.loadURL(url);
        }
    },

    /** private: method[addLayer]
     *  Called when the user clicks the 'add layer' button.  Call the
     *  according layerBrowser 'addLayer' method.  If a layer was added, close
     *  the parent :class: `Ext.Window` if closeOnLayerAdded is set to true.
     */
    addLayer: function() {
        if(this.layerBrowser.addLayer()) {
            if(this.closeOnLayerAdded && 
               this.ownerCt.getXType() == Ext.Window.xtype) {
                this.closeWindow();
            }
        }        
    },

    /** private: method[onAfterRender]
     *  Called after this element was rendered.
     *  If the owner is a window, add a 'close' button.
     */
    onAfterRender : function() {
        if(this.ownerCt.getXType() == Ext.Window.xtype) {
            this.addCloseButton();
        }
    },

    /** private: method[addCloseButton]
     *  Adds a 'close' button to the bottom toolbar.
     */
    addCloseButton : function() {
        var actionOptions = {
            handler: this.closeWindow,
            scope: this,
            tooltip: this.closeWindowText
        };

        if (this.useIcons === true) {
            actionOptions.iconCls = "gx-wmsbrowser-close";
        } else {
            actionOptions.text = this.closeText;
        }

        var action = new Ext.Action(actionOptions);

        this.getBottomToolbar().add(action);
    },

    /** private: method[closeWindow]
     *  Hides the :class:`Ext.Window`.  Only used if the widget was rendered
     *  inside one.
     */
    closeWindow: function() {
        this.ownerCt.hide();
    },

    /** private: method[urlValidator]
     *  :param url: ``String``  The url inputed or selected
     *  :return: ``Boolean``  Whether the url is valid or not.
     */
    urlValidator: function(url) {
        return Ext.form.VTypes.url(url);
    },

    /** private: method[zoomToRecordLLBBox]
     *  :param record: ``GeoExt.data.LayerRecord``  The layer record to zoom to
     *
     *  Get the 'llbbox' value of the record layer and zoom to its location.
     */
    zoomToRecordLLBBox: function(record, zoomToMaxExtent) {
        if (zoomToMaxExtent == null) {
            zoomToMaxExtent = true;
        }
        var zoomed = false;

        var layerExtent;
        if (record instanceof OpenLayers.Layer) {
            layerExtent = record.metadata.llbbox;
        } else {
            layerExtent = record.get("llbbox");
        }

        if (layerExtent) 
        {
            var extent;
            if(typeof layerExtent == "string") {
                extent = OpenLayers.Bounds.fromString(layerExtent);
            } else if(layerExtent instanceof Array) {
                extent = OpenLayers.Bounds.fromArray(layerExtent);
            }
            
            if (extent) {
                this.mapPanelPreview.map.zoomToExtent(extent);
                zoomed = true;
            }
        }

        if (zoomToMaxExtent && !zoomed) {
            this.mapPanelPreview.map.zoomToMaxExtent();
        }
    },

    /** private: method[hideLayerPreview]
     *  Hide the layer preview.
     */
    hideLayerPreview: function() {
        if (this.layerPreview) {
            if (this.layerPreview instanceof OpenLayers.Layer) {
                this.layerPreview.setVisibility(false);
            } else {
                this.layerPreview.get('layer').setVisibility(false);
            }
        }
    },

    /** private: method[showLayerPreview]
     *  Show the layer preview.
     */
    showLayerPreview: function() {
        if (this.layerPreview) {
            if (this.layerPreview instanceof OpenLayers.Layer) {
                this.layerPreview.setVisibility(true);
            } else {
                this.layerPreview.get('layer').setVisibility(true);
            }
        }
    },

    /** private: method[resetLayerPreview]
     *  Remove all layers from the mapPanelPreview and reset the layer preview.
     */
    resetLayerPreview: function() {
        if (this.layerPreview) {
            this.mapPanelPreview.layers.removeAll();
            this.mapPanelPreview.map.addLayer(new OpenLayers.Layer("dummy"));
            this.layerPreview = null;
        }
    },

    /** private: method[isLayerNameValid]
     * :return:  ``Boolean`` Whether the layer name is valid or not
     *  Checks the current value of the layer name in textbox.
     */
    isLayerNameValid: function() {
        var validLayerName = true;
 
        var layerName = this.layerNameField.getValue();
        if (!layerName || layerName == "") {
            this.fireEvent('genericerror', this.pleaseInputLayerNameText);
            validLayerName = false;
        }

        return validLayerName;
    },

    /** private: method[resetLayerName]
     *  Reset the layer name;
     */
    resetLayerName: function() {
        this.layerNameField.reset();
    },

    /** private: method[resetAll]
     *  Reset the layer preview, layer name and center form panel.
     */
    resetAll: function() {
        this.resetLayerPreview();
        this.resetLayerName();
        this.resetCenterFormPanel();
    }
});
