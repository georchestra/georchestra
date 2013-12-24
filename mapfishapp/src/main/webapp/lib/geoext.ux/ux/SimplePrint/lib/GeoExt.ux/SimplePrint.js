/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/*
 * @include GeoExt/data/PrintPage.js
 * @include GeoExt/plugins/PrintExtent.js
 * @include GeoExt/plugins/PrintProviderField.js
 * @include GeoExt/plugins/PrintPageField.js
 */

/** api: (define)
 *  module = GeoExt.form
 *  class = SimplePrint
 *  base_link = `Ext.form.FormPanel <http://dev.sencha.com/deploy/dev/docs/?class=Ext.form.FormPanel>`_
 */

/** api: constructor
 *  .. class:: SimplePrint
 * 
 *  An instance of this form creates a single print page. Layout, DPI, scale
 *  and rotation are configurable in the form. A Print button is also added to
 *  the form.
 */
GeoExt.ux.SimplePrint = Ext.extend(Ext.form.FormPanel, {
    
    /* begin i18n */
    /** api: config[layoutText] ``String`` i18n */
    layoutText: "Layout",
    /** api: config[dpiText] ``String`` i18n */
    dpiText: "DPI",
    /** api: config[scaleText] ``String`` i18n */
    scaleText: "Scale",
    /** api: config[rotationText] ``String`` i18n */
    rotationText: "Rotation",
    /** api: config[printText] ``String`` i18n */
    printText: "Print",
    /** api: config[creatingPdfText] ``String`` i18n */
    creatingPdfText: "Creating PDF...",
    /* end i18n */
   
    /** api: config[printProvider]
     *  :class:`GeoExt.data.PrintProvider` The print provider this form
     *  is connected to.
     */
    
    /** api: config[mapPanel]
     *  :class:`GeoExt.MapPanel` The map panel that this form should be
     *  connected to.
     */
    
    /** api: config[layer]
     *  ``OpenLayers.Layer`` Layer to render page extents and handles
     *  to. Useful e.g. for setting a StyleMap. Optional, will be auto-created
     *  if not provided.
     */

    /** api: config[printOptions]
     *  ``Object`` Optional options for the printProvider's print command.
     */

    /** api: property[printOptions]
     *  ``Object`` Optional options for the printProvider's print command.
     */
    printOptions: null,
    
    /** api: config[hideUnique]
     *  ``Boolean`` If set to false, combo boxes for stores with just one value
     *  will be rendered. Default is true.
     */
    
    /** api: config[hideRotation]
     *  ``Boolean`` If set to true, the Rotation field will not be rendered.
     *  Default is false.
     */
    
    /** api: config[busyMask]
     *  ``Ext.LoadMask`` A LoadMask to use while the print document is
     *  prepared. Optional, will be auto-created with ``creatingPdfText` if
     *  not provided.
     */
    
    /** private: property[busyMask]
     *  ``Ext.LoadMask``
     */
    busyMask: null,
   
    /** api: config[printExtentOptions]
     *  ``Object`` Optional options for the `GeoExt.plugins.Print` plugin.
     */
    printExtentOptions: null,

    /** private: property[printExtent]
     *  :class:`GeoExt.plugins.PrintExtent`
     */
    printExtent: null,
    
    /** api: property[printPage]
     *  :class:`GeoExt.data.PrintPage` The print page for this form. Useful
     *  e.g. for rotating handles when used in a style map context. Read-only.
     */
    printPage: null,

    /** api: config[comboOptions]
     *  ``Object`` Optional options for the comboboxes. If not provided, the
     *  following will be used:
     *
     *  .. code-block:: javascript
     *
     *      {
     *          typeAhead: true,
     *          selectOnFocus: true
     *      }
     */
    comboOptions: null,
    
    /** private: method[initComponent]
     */
    initComponent: function() {

        // This is a workaround for an Ext issue. When the SimplePrint
        // is an accordion's item an error occurs on expand if
        // the fbar is created later, i.e. outside initComponent. So the
        // problem triggers when the capabilities are loaded using
        // XHR. The workaround involves forcing the creation of
        // the fbar as part of initComponent.
        this.fbar = this.fbar || [];

        GeoExt.ux.SimplePrint.superclass.initComponent.call(this);

        this.printPage = new GeoExt.data.PrintPage({
            printProvider: this.initialConfig.printProvider
        });
        
        this.printExtent = new GeoExt.plugins.PrintExtent(Ext.applyIf({
            pages: [this.printPage],
            layer: this.initialConfig.layer
        }, this.printExtentOptions));

        if (!this.busyMask) {
            this.busyMask = new Ext.LoadMask(Ext.getBody(), {
                msg: this.creatingPdfText
            });
        }

        this.printExtent.printProvider.on({
            "beforeprint": this.busyMask.show,
            "print": this.busyMask.hide,
            scope: this.busyMask
        });

        if(this.printExtent.printProvider.capabilities) {
            this.initForm();
        } else {
            this.printExtent.printProvider.on({
                "loadcapabilities": this.initForm,
                scope: this,
                single: true
            });
        }        

        //for accordion
        this.on('expand', this.showExtent, this);
        this.on('collapse', this.hideExtent, this);

        //for tabs
        this.on('activate', this.showExtent, this);
        this.on('deactivate', this.hideExtent, this);

        //for manual enable/disable
        this.on('enable', this.showExtent, this);
        this.on('disable', this.hideExtent, this);

        //for use in an Ext.Window with closeAction close
        this.on('destroy', this.hideExtent, this);
    },
    
    /** private: method[initForm]
     *  Creates and adds items to the form.
     */
    initForm: function() {
        this.mapPanel.initPlugin(this.printExtent);
        var p = this.printExtent.printProvider;
        var hideUnique = this.initialConfig.hideUnique !== false;
        var cbOptions = this.comboOptions || {
            typeAhead: true,
            selectOnFocus: true
        };
        
        !(hideUnique && p.layouts.getCount() <= 1) && this.add(Ext.apply({
            xtype: "combo",
            fieldLabel: this.layoutText,
            store: p.layouts,
            forceSelection: true,
            displayField: "name",
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintProviderField({
                printProvider: p
            })
        }, cbOptions));
        !(hideUnique && p.dpis.getCount() <= 1) && this.add(Ext.apply({
            xtype: "combo",
            fieldLabel: this.dpiText,
            store: p.dpis,
            forceSelection: true,
            displayField: "name",
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintProviderField({
                printProvider: p
            })
        }, cbOptions));
        !(hideUnique && p.scales.getCount() <= 1) && this.add(Ext.apply({
            xtype: "combo",
            fieldLabel: this.scaleText,
            store: p.scales,
            forceSelection: true,
            displayField: "name",
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintPageField({
                printPage: this.printPage
            })
        }, cbOptions));
        this.initialConfig.hideRotation !== true && this.add({
            xtype: "numberfield",
            fieldLabel: this.rotationText,
            name: "rotation",
            enableKeyEvents: true,
            plugins: new GeoExt.plugins.PrintPageField({
                printPage: this.printPage
            })
        });

        this.addButton({
            text: this.printText,
            handler: function() {
                this.printExtent.print(this.printOptions);
            },
            scope: this
        });

        this.doLayout();

        if(this.autoFit === true) {
            this.onMoveend();
            this.mapPanel.map.events.on({
                "moveend": this.onMoveend,
                scope: this
            });
        }
    },
    
    /** private: method[onMoveend]
     *  Handler for the map's moveend event
     */
    onMoveend: function() {
        this.printPage.fit(this.mapPanel.map, {mode: "screen"});
    },
    
    /** private: method[beforeDestroy]
     */
    beforeDestroy: function() {
        var p = this.printExtent.printProvider;
        p.un("beforePrint", this.busyMask.show, this.busyMask);
        p.un("print", this.busyMask.hide, this.busyMask);
        if(this.autoFit === true) {
            this.mapPanel.map.events.un({
                "moveend": this.onMoveend,
                scope: this
            });
        }
        GeoExt.ux.SimplePrint.superclass.beforeDestroy.apply(this, arguments);
    },

    /** private: method[showExtent]
     * Handler for the panel's expand/activate/enable event
     */
    showExtent: function() {
        this.printExtent.show();
    },

    /** private: method[hideExtent]
     * Handler for the panel's collapse/deactivate/disable/destroy event
     */
    hideExtent: function() {
        this.printExtent.hide();
    }
});

/** api: xtype = gxux_simpleprint */
Ext.reg("gxux_simpleprint", GeoExt.ux.SimplePrint);
