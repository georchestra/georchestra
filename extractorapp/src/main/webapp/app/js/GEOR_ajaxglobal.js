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
 * @requires OpenLayers/Request.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include GEOR_waiter.js
 * @include GEOR_util.js
 * @include GEOR_config.js
 */

Ext.namespace("GEOR");

GEOR.ajaxglobal = (function() {

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

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
        var text;
        switch(options.request.status) {
            case 0:
                text = tr("The server did not return nothing.");
                break;
            case 403:
                text = tr("The server did not allow access.");
                break;
            case 406:
                text = tr("ajaxglobal.error.406");
                break;
            case HTTP_STATUS_TOO_BIG:
                break;
            case HTTP_STATUS_EXCEPTION_REPORT:
                //text = "Le service OGC a renvoyé une exception.";
                break;
            default:
                text = tr("ajaxglobal.error.default");
                break;
        }
        if (text) {
            GEOR.util.errorDialog({
                title: tr("ajaxglobal.error.title",
                          {ERROR: options.request.status}),
                msg: tr("ajaxglobal.error.body", {"TEXT": text})
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
                    title: tr("Warning: the browser may freeze"),
                    msg: tr("ajaxglobal.toobig", {
                        "WEIGHT": Math.round(request.responseText.length/1024),
                        "LIMIT": Math.round(GEOR.config.MAX_LENGTH/1024)}),
                    width: 420,
                    yesCallback: function() {
                        OpenLayers.Request.runCallbacks.call(
                            OpenLayers.Request, options);
                    },
                    noCallback: function() {
                        request.status = HTTP_STATUS_TOO_BIG;
                        OpenLayers.Request.runCallbacks.call(
                            OpenLayers.Request, options);
                    }
                });
                runCallbacks = false;
            } else {
                // deal with Service Exception Report
                var data = request.responseXML;
                if (!data || !data.documentElement) {
                    data = request.responseText;
                }
                if (typeof data == "string" &&
                    data.substr(0, 5) == "<?xml") {
                    data = (new OpenLayers.Format.XML()).read(data);
                }
                if (data && data.nodeType == 9 && data.documentElement) {
                    var node = data.documentElement;
                    var local = node.localName || node.nodeName.split(":").pop();
                    if (local == "ServiceExceptionReport" ||
                        local == "ExceptionReport") {
                        request.status = HTTP_STATUS_EXCEPTION_REPORT;
                    }
                }
            }
        }
        if (runCallbacks) {
            OpenLayers.Request.runCallbacks.call(
                OpenLayers.Request, options);
        }
        // we ourself run the callbacks
        return false;
    };

    return {

        /**
         * APIMethod: init
         * Initialize GEOR.ajaxglobal
         */
        init: function() {
            OpenLayers.Request.events.on({
                "failure": handleFailure,
                "complete": handleComplete,
                scope: this
            });
        }

    };
})();
