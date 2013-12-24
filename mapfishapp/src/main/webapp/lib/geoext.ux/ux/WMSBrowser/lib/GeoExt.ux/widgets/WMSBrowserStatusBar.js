/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux");

/**
 * @include GeoExt.ux/widgets/WMSBrowser.js
 */

/** api: (define)
 *  module = GeoExt.ux
 *  class = WMSBrowserStatusBar
 */

if (Ext.ux.StatusBar) {

    /** api: constructor
     *  .. class:: WMSBrowserStatusBar
     */
    GeoExt.ux.WMSBrowserStatusBar = Ext.extend(Ext.ux.StatusBar, {

        /* begin i18n */
        /** api: config[text] ``String`` i18n */
        text: 'Ready',

        /** api: config[busyText] ``String`` i18n */
        busyText: 'Loading layers...',

        /** api: config[defaultText] ``String`` i18n */
        defaultText: 'Ready',
        /* end i18n */

        /** api: config[iconCls]
         *  ``String``  The default 'iconCls' value.
         */
        iconCls: 'x-status-valid',

        /** api: config[defaultIconCls]
         *  ``String``  The default 'defaultIconCls' value.
         */
        defaultIconCls: 'x-status-valid',

        /** private: property[wmsbrowser]
         *  :class:`GeoExt.ux.WMSBrowser`  The widget in which to listen
         *  custom events to display the messages and status.
         */
        wmsbrowser: null,

        /** private: method[constructor]
         */
        constructor: function(config) {
            Ext.apply(this, config);
            arguments.callee.superclass.constructor.call(this, config);

            // event registrations
            this.wmsbrowser.on(
                'beforegetcapabilities',
                this.onBeforeGetCapabilities,
                this
            );

            this.wmsbrowser.on(
                'getcapabilitiessuccess',
                this.onGetCapabilitiesSuccess,
                this
            );

            this.wmsbrowser.on(
                'getcapabilitiesfail',
                this.onGetCapabilitiesFail,
                this
            );

            this.wmsbrowser.on(
                'genericerror',
                this.onGenericError,
                this
            );

            this.wmsbrowser.on(
                'layeradded',
                this.onLayerAdded,
                this
            );
        },

        /** private: method[onBeforeGetCapabilities]
         *  Called when a "beforegetcapabilities" event is fired by the 
         *  :class:`GeoExt.ux.WMSBrowser` widget.  Set the status bar to "busy".
         */
        onBeforeGetCapabilities: function() {
            this.showBusy();
        },

        /** private: method[onGetCapabilitiesSuccess]
         *  Called when a "getcapabilitiessuccess" event is fired by the 
         *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the according success
         *  message.
         */
        onGetCapabilitiesSuccess: function() {
            this.setStatus({
                text: this.wmsbrowser.layersSuccessfullyLoadedText,
                iconCls: 'x-status-valid',
                clear: true
            });
        },

        /** private: method[onGetCapabilitiesFail]
         *  Called when a "getcapabilitiessuccess" event is fired by the 
         *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the according failure
         *  message.
         */
        onGetCapabilitiesFail: function() {
            this.setStatus({
                text: this.wmsbrowser.urlInvalidText,
                iconCls: 'x-status-error',
                clear: true
            });
        },

        /** private: method[onGenericError]
         *  :param message: ``String``  The error message sent by the event.
         *
         *  Called when a "genericerror" event is fired by the 
         *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the message sent by
         *  the event.
         */
        onGenericError: function(message) {
            this.setStatus({
                text: message,
                iconCls: 'x-status-error',
                clear: true
            });
        },

        /** private: method[onLayerAdded]
         *  Called when a "layeradded" event is fired by the 
         *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the according success
         *  message.
         */
        onLayerAdded: function() {
            this.setStatus({
                text: this.wmsbrowser.layerAddedText,
                iconCls: 'x-status-valid',
                clear: true
            });
        }
    });
}
