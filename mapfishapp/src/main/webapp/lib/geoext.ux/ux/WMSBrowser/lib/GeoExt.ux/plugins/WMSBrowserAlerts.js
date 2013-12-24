/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.ux.plugins");

/*
 * @requires GeoExt.ux/widgets/WMSBrowser.js
 */

/** api: (define)
 *  module = GeoExt.ux.plugins
 *  class = WMSBrowserAlerts
 */

/** api: constructor
 *  .. class:: WMSBrowserAlerts
 */
GeoExt.ux.plugins.WMSBrowserAlerts = Ext.extend(Ext.util.Observable, {

    /** private: property[wmsbrowser]
     *  :class:`GeoExt.ux.WMSBrowser`  The widget in which to listen
     *  custom events to display the messages and status.
     */
    wmsbrowser: null,

    /** api: config[autoHidePopup]
     * ``Boolean`` Whether the popup should be automatically hidden after a
     *             certain period of time or not.  Defaults to false.
     */
    autoHidePopup: false,

    /** api: config[popupTimeout]
     * ``Integer`` The time in milliseconds the popup should be visible.  Only
     *             used if 'autoHidePopup' property is set to true.
     */
    popupTimeout: 4000,

    /** private: method[init]
     *  :param wmsbrowser: ``GeoExt.ux.WMSBrowser``
     */
    init: function(wmsbrowser, config) {
        this.wmsbrowser = wmsbrowser;
        this.popupTimeout = this.wmsbrowser.alertPopupTimeout;
        this.autoHidePopup = this.wmsbrowser.alertPopupAutoHide;

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
    },

    /** private: method[showPopup]
     *  Display a popup with given title, message and icon.  Automatically hides
     *  after a certain period of time (set by the 'popupTimeout' property).
     */
    showPopup: function(title, message, extIcon) {
        if(!extIcon){
            extIcon = Ext.MessageBox.WARNING
        }

        // this fix makes sure that the Ext.MessageBow window used to display
        // the alert is always shown on top of any other windows...
        if (Ext.MessageBox.getDialog().manager.zseed <= Ext.WindowMgr.zseed) {
            Ext.MessageBox.getDialog().manager = new Ext.WindowGroup();
            Ext.MessageBox.getDialog().manager.zseed = Ext.WindowMgr.zseed + 1000;
        }

        Ext.MessageBox.show({
            title: title,
            msg: message,
            modal: false,
            width: 300,
            buttons: Ext.MessageBox.OK,
            icon: extIcon
        });

        if (this.autoHidePopup) {
            setTimeout(function(){
                Ext.MessageBox.hide();
            }, this.popupTimeout);
        }
    },

    /** private: method[onGetCapabilitiesFail]
     *  Called when a "getcapabilitiessuccess" event is fired by the 
     *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the according failure
     *  message.
     */
    onGetCapabilitiesFail: function() {
        this.showPopup(
            this.wmsbrowser.errorText,
            this.wmsbrowser.urlInvalidText
        );
    },

    /** private: method[onGenericError]
     *  :param message: ``String``  The error message sent by the event.
     *
     *  Called when a "genericerror" event is fired by the 
     *  :class:`GeoExt.ux.WMSBrowser` widget.  Shows the message sent by
     *  the event.
     */
    onGenericError: function(message) {
        this.showPopup(
            this.wmsbrowser.warningText,
            message
        );
    }
});
