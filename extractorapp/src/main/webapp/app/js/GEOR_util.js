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
 * @include OpenLayers/Projection.js
 */

Ext.namespace("GEOR");

GEOR.util = (function() {

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

    return {

        /**
         * APIMethod: shortenLayerName
         * Returns a shorter string for a layer name (if required).
         *
         * Parameters:
         * layer - {String | GeoExt.data.LayerRecord | OpenLayers.Layer.WMS}
         *         The layer name or the layer or the layer record.
         * limit - {Integer} The number of chars before cutting
         *         Defaults to 25.
         */
        shortenLayerName: function(layer, limit) {
            var t, l;
            if (typeof(layer) == 'string') {
                t = layer;
            } else if (layer instanceof OpenLayers.Layer.WMS) {
                t = layer.name;
            } else if (layer instanceof GeoExt.data.LayerRecord) {
                t = layer.get('title') || '';
            } else {
                // there's a pb, we silently ignore it
                return '';
            }
            l = (limit) ? limit : 25;
            return ((t.length > l) ? t.substr(0,l-3) + '...' : t);
        },

        /**
         * APIMethod: stringUpperCase
         * Returns a string with first letter uppercased
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String} input string with first letter uppercased
         */
        stringUpperCase: function(str) {
            return str.substr(0,1).toUpperCase() +
                str.substr(1).toLowerCase();
        },

        /**
         * APIMethod: getAppRelativePath
         * Given a URL get its path relative to "extractorapp". For
         * example
         * getAppRelativePath("http://foo.org/extractorapp/bar/foo")
         * returns "bar/foo".
         *
         * Parameters:
         * url - {String} The URL.
         *
         * Returns:
         * {String} The path.
         */
        getAppRelativePath: function(url) {
            url = url || window.location.href;
            var urlObject = OpenLayers.Util.createUrlObject(url,
                {ignorePort80: true}
            );
            var path = urlObject.pathname;
            if (path.indexOf("/extractorapp") === 0) {
                path = path.slice("/extractorapp".length);
                if (path.indexOf("/") === 0) {
                    path = path.slice(1);
                }
                return path;
            }
        },

        /**
         * APIMethod: confirmDialog
         * Shows a confirm dialog box
         *
         * Parameters:
         * options - {Object} Hash with keys: (starred ones are mandatory)
         *      title, msg*, width, yesCallback*, noCallback, scope*
         */
        confirmDialog: function(options) {
            Ext.Msg.show({
                title: options.title || tr('Confirm'),
                msg: options.msg,
                buttons: Ext.Msg.YESNO,
                closable: false,
                width: options.width || 400,
                fn: function(btnId) {
                    if (btnId == 'yes' && options.yesCallback) {
                        options.yesCallback.call(options.scope);
                    } else if (options.noCallback) {
                        options.noCallback.call(options.scope);
                    }
                },
                icon: Ext.MessageBox.QUESTION
            });

        },

        /**
         * APIMethod: infoDialog
         * Shows an informative dialog box
         *
         * Parameters:
         * options - {Object} Hash with keys: (starred ones are mandatory)
         *      title, msg*, width
         */
        infoDialog: function(options) {
            Ext.Msg.show({
                title: options.title || tr('Information'),
                msg: options.msg,
                width: options.width || 400,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.INFO,
                modal: false
            });
        },

        /**
         * APIMethod: errorDialog
         * Shows an error dialog box
         *
         * Parameters:
         * options - {Object} Hash with keys: (starred ones are mandatory)
         *      title, msg*, width
         */
        errorDialog: function(options) {
            Ext.Msg.show({
                title: options.title || tr('Error'),
                msg: options.msg,
                width: options.width || 400,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        },

        /**
         * APIMethod: isUrl
         *
         * Parameters:
         * s - {String} test string
         * strict - {Boolean} If true, strict URL matching.
         *  Else, check the string begins with an URL
         *
         * Returns:
         * {Boolean}
         */
        isUrl: function(s, strict) {
            if (strict) {
                return new RegExp(/^(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?$/i).test(s);
            }
            return new RegExp(/^(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/i).test(s);
        },

        /**
         * APIMethod: getUnitsForCRS
         *
         * Parameters:
         * crs - {String|OpenLayers.Projection} crs string or projection object
         *
         * Returns:
         * {String} either 'm' for meters or 'degrees'
         */
        getUnitsForCRS: function(crs) {
            return (typeof crs == "string") ?
                new OpenLayers.Projection(crs).getUnits() :
                crs.getUnits();
        },

        unitsTranslations: {
            'degrees': tr('degrees'),
            'm': tr('meters')
        },

        // see http://www.w3schools.com/js/js_cookies.asp
        getCookie: function(name) {
            var i, x, y, ARRcookies = document.cookie.split(";");
            for (i=0;i<ARRcookies.length;i++) {
                x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
                y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
                x = x.replace(/^\s+|\s+$/g, "");
                if (x == name) {
                    return unescape(y);
                }
            }
        }
    };
})();
