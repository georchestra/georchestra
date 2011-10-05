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
                text = "Données trop volumineuses.";
                break;
            case HTTP_STATUS_EXCEPTION_REPORT:
                text = "Le service OGC a renvoyé une exception.";
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
                text = "Pour plus d'information, nous vous invitons à "+
                "chercher le code de retour sur <a href=\"http://"+
                "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">"+
                "cette page</a>.";
                break;
        }
        if (text) {
            GEOR.util.errorDialog({
                title: "Erreur"+ ((options.request.status < 600) ? 
                    ' HTTP ' + options.request.status : ''),
                width: width,
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
    
        GEOR.waiter.hide();
        var request = options.request, runCallbacks = true;

        if (httpSuccess(request)) {
            // deal with too big responses
            if (request.responseText.length > GEOR.config.MAX_LENGTH) {
                GEOR.util.confirmDialog({
                    title: 'Attention : risque de blocage du navigateur',
                    msg: [
                        "Les données provenant du serveur sont trop",
                        "volumineuses.<br />Le serveur a envoyé",
                        "" + Math.round(request.responseText.length/1024) + "KO",
                        "(la limite est à",
                        "" + Math.round(GEOR.config.MAX_LENGTH/1024) + "KO).",
                        "<br />Voulez-vous tout de même continuer ?"
                    ].join(" "),
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
                if (request.responseXML) {
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
