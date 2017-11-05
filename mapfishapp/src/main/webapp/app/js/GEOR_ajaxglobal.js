/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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
 * @requires OpenLayers/Request.js
 * @include OpenLayers/Format/OGCExceptionReport.js
 * @include OpenLayers/Format/OWSCommon/v1_0_0.js
 * @include GEOR_waiter.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.ajaxglobal = (function() {

    /**
     * FIXME
     */
    var HTTP_STATUS_TOO_BIG = 600;

    /**
     * FIXME
     */
    var HTTP_STATUS_EXCEPTION_REPORT = 601;

    /**
     * Method: httpSuccess
     * FIXME
     */
    var httpSuccess = function(request) {
        return (request.status >= 200 && request.status < 300);
    };

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: handleFailure
     * Handles Ajax errors.
     *
     * Parameters:
     * options - {object} hash with options:
     *    request - {XMLHttpRequest} The XHR object.
     *    config - {Object} The request config.
     *    url - {String} The request URL.
     */
    var handleFailure = function(options) {
        var text, width = 400;
        if (GEOR.ajaxglobal.disableAllErrors ||
            // ignoring all errors when it comes to DescribeLayer
            // see https://github.com/georchestra/georchestra/issues/898
            /DescribeLayer/i.test(options.requestUrl)) {
            return;
        }
        switch(options.request.status) {
            case 0:
                text = tr("Server did not respond.");
                break;
            case 403:
                text = tr("Server access denied.");
                break;
            case 406:
                text = tr("ajax.badresponse");
                break;
            case 503:
                text = tr("Server unavailable.");
                break;
            case HTTP_STATUS_TOO_BIG:
                text = tr("Too much data.");
                break;
            case HTTP_STATUS_EXCEPTION_REPORT:
                text = tr("Server exception.");
                if (options.request.errorText) {
                    var t = options.request.errorText;
                    if (t.length > 1000) {
                        t = t.substring(0,500).replace(/(\r\n|\n|\r)/gm,"<br />") +
                            '<br /><br />... [snip] ...<br /><br />' +
                            t.substring(t.length-500).replace(/(\r\n|\n|\r)/gm,"<br />");
                    }
                    text += '<br /><br />'+t;
                    // adjust window width
                    width = 600;
                }
                break;
            default:
                text = tr("ajax.defaultexception");
                break;
        }
        if (text) {
            GEOR.util.errorDialog({
                title: tr("Error")+ ((options.request.status < 600) ?
                    ' HTTP ' + options.request.status : ''),
                width: width,
                msg: tr("An error occured.<br />") + text
            });
        }
    };

    /**
     * Method: handleComplete
     * Handles completion of Ajax requests
     *
     * Parameters:
     * options - {object} hash with options:
     *    request - {XMLHttpRequest} The XHR object.
     *    config - {Object} The request config.
     *    requestUrl - {String} The request URL.
     *
     * Returns:
     * {Boolean} false : we never automatically run other callbacks (success/failure)
     */
    var handleComplete = function(options) {

        GEOR.waiter.hide();
        var request = options.request, runCallbacks = true;

        if (httpSuccess(request)) {
            // deal with too big responses
            if (request.responseText.length > GEOR.config.MAX_LENGTH) {
                GEOR.util.confirmDialog({
                    title: tr('Warning : browser may freeze'),
                    msg: tr("ajaxglobal.data.too.big", {
                        'SENT': Math.round(request.responseText.length/1024),
                        'LIMIT': Math.round(GEOR.config.MAX_LENGTH/1024)
                        }),
                    width: 420,
                    yesCallback: function() {
                        OpenLayers.Request.runCallbacks.call(
                            OpenLayers.Request, options
                        );
                    },
                    noCallback: function() {
                        request.status = HTTP_STATUS_TOO_BIG;
                        OpenLayers.Request.runCallbacks.call(
                            OpenLayers.Request, options
                        );
                    }
                });
                runCallbacks = false;
            } else {
                // deal with Service Exception Report
                if (request.responseXML && !GEOR.ajaxglobal.disableOGCExceptionReports) {
                    var data = (new OpenLayers.Format.OGCExceptionReport()).read(request.responseXML);
                    if (data.exceptionReport && data.exceptionReport.exceptions) {
                        var exceptions = data.exceptionReport.exceptions;
                        var r = [];
                        for (var i=0, l=exceptions.length; i<l; i++) {
                            exceptions[i].text && r.push(exceptions[i].text);
                        }
                        request.status = HTTP_STATUS_EXCEPTION_REPORT;
                        request.errorText = r.join('<br />');
                    }
                }
            }
        }
        if (runCallbacks) {
            OpenLayers.Request.runCallbacks.call(
                OpenLayers.Request, options
            );
        }
        // we ourself run the callbacks
        return false;
    };

    return {

        /**
         * APIProperty: disableOGCExceptionReports
         * Set to true to disable OGC Exception Report handling
         */
        disableOGCExceptionReports: false,

        /**
         * APIProperty: disableAllErrors
         * Set to true to disable XHR error handling
         */
        disableAllErrors: false,

        /**
         * APIMethod: init
         * Initialize GEOR.ajaxglobal
         */
        init: function() {
            tr = OpenLayers.i18n;
            OpenLayers.Request.events.on({
                "failure": handleFailure,
                "complete": handleComplete,
                scope: this
            });
        }
    };
})();
