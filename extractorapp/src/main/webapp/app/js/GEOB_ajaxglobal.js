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
 * @requires OpenLayers/Request.js
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include GEOB_waiter.js
 * @include GEOB_util.js
 * @include GEOB_config.js
 */

Ext.namespace("GEOB");

GEOB.ajaxglobal = (function() {

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
                text = "Le serveur n'a pas répondu.";
                break;
            case 403:
                text = "Le serveur a refusé de répondre.";
                break;
            case 406:
                text = "Le serveur distant a répondu, mais le contenu de la "+
                "réponse n'est pas conforme à ce que nous attendons. "+
                "FireFox s'en sort mieux que Internet Explorer dans certaines "+
                "de ces situations. Ce serait probablement une bonne idée que "+
                "d'essayer avec un autre navigateur !";
                break;
            case HTTP_STATUS_TOO_BIG:
                break;
            case HTTP_STATUS_EXCEPTION_REPORT:
                //text = "Le service OGC a renvoyé une exception.";
                break;
            default:
                text = "Pour plus d'information, nous vous invitons à "+
                "chercher le code de retour sur <a href=\"http://"+
                "en.wikipedia.org/wiki/List_of_HTTP_status_codes\">"+
                "cette page</a>.";
                break;
        }
        if (text) {
            GEOB.util.errorDialog({
                title: "Erreur HTTP "+options.request.status,
                msg: "Une erreur est survenue.<br />" + text
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
    
        GEOB.waiter.hide();
        var request = options.request, runCallbacks = true;

        if (httpSuccess(request)) {
            // deal with too big responses
            if (request.responseText.length > GEOB.config.MAX_LENGTH) {
                GEOB.util.confirmDialog({
                    title: 'Attention : risque de blocage du navigateur',
                    msg: [
                        "Les données provenant du serveur sont trop",
                        "volumineuses.<br />Le serveur a envoyé",
                        "" + Math.round(request.responseText.length/1024) + "KO",
                        "(la limite est à",
                        "" + Math.round(GEOB.config.MAX_LENGTH/1024) + "KO).",
                        "<br />Voulez-vous tout de même continuer ?"
                    ].join(" "),
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
         * Initialize GEOB.ajaxglobal
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
