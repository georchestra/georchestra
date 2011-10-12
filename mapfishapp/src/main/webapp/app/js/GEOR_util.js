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

Ext.namespace("GEOR");

GEOR.util = (function() {

    // isStringType
    var isStringType = function(type) {
        return type == 'xsd:string' || type == 'string'; // geoserver,mapserver
    };
    
    // isNumericType 
    var isNumericType = function(type) {
        return type == 'xsd:double' || type == 'double' || 
            type == 'xsd:int' || type == 'int' || 
            type == 'xsd:integer' || type == 'integer' || 
            // as stated by mapserver doc, real type can exist
            // see for instance http://mapserver.org/ogc/wfs_server.html#reference-section
            // and search for gml_[item name]_type
            type == 'xsd:real' || type == 'real' || 
            type == 'xsd:float' || type == 'float' || 
            type == 'xsd:decimal' || type == 'decimal' || 
            type == 'xsd:long' || type == 'long'; 
    };
    
    // isDateType 
    var isDateType = function(type) {
        return type == 'xsd:date' || type == 'date' || 
            type == 'xsd:dateTime' || type == 'dateTime';
    };
    
    // isBooleanType 
    var isBooleanType = function(type) {
        return type == 'xsd:boolean' || type == 'boolean';
    };
    
    // Template that displays name and type for each attribute (with a qtip)
    var tplAttribute = new Ext.XTemplate(
        '<tpl for=".">',
            '<tpl if="this.isString(type)">',
                '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>Caractères</span></div>',
            '</tpl>',
            '<tpl if="this.isNumeric(type)">',
                '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>Numérique</span></div>',
            '</tpl>',
            '<tpl if="this.isDate(type)">',
                '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>Date</span></div>',
            '</tpl>',
            '<tpl if="this.isBoolean(type)">',
                '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>Booléen</span></div>',
            '</tpl>',
            '<tpl if="this.isAnother(type)">',
                '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>autre</span></div>',
            '</tpl>',
        '</tpl>', {
            isString: isStringType,
            isNumeric: isNumericType,
            isDate: isDateType,
            isBoolean: isBooleanType,
            isAnother: function(type) {
                return !isStringType(type) && !isNumericType(type) && 
                    !isDateType(type) && !isBooleanType(type);
            }
        }
    );

    return {
        
        /**
         * APIMethod: shortenLayerName
         * Returns a shorter string for a layer name (if required).
         *
         * Parameters:
         * layer - {String | GeoExt.data.LayerRecord | OpenLayers.Layer.WMS}  
         *         The layer name or the layer or the layer record.
         */
        shortenLayerName: function(layer) {
            var t;
            if (typeof(layer) == 'string') {
                t = layer;
            } else if (layer instanceof OpenLayers.Layer.WMS) {
                t = layer.name;
            } else if (layer instanceof GeoExt.data.LayerRecord) {
                t = layer.get('title') || layer.get('layer').name || '';
            } else {
                // there's a pb, we silently ignore it
                return '';
            }
            return ((t.length > 40) ? t.substr(0,37) + '...' : t);
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
         * APIMethod: stringDeaccentuate
         * Returns a string without accents
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String}
         */
        stringDeaccentuate: function(str) {
            str = str.replace(/ç/, 'c');
            str = str.replace(/(á|à|ä|â|å|Â|Ä|Á|À|Ã)/, 'a');
            str = str.replace(/(é|è|ë|ê|Ê|Ë|É|È|Ę)/, 'e');
            str = str.replace(/(í|ì|ï|î|Î|Ï|Í|Ì|Į)/, 'i');
            str = str.replace(/(ó|ò|ö|ô|ø|Ô|Ö|Ó|Ò)/, 'o');
            return str.replace(/(ú|ù|ü|û|Û|Ü|Ú|Ù|Ų)/, 'u');
        },

        /**
         * APIMethod: getAppRelativePath
         * Given a URL get its path relative to "mapfishapp". For
         * example 
         * getAppRelativePath("http://foo.org/mapfishapp/bar/foo")
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
            if (path.indexOf("/mapfishapp") === 0) {
                path = path.slice("/mapfishapp".length);
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
            Ext.Msg.show(Ext.apply({
                title: "Confirmation",
                msg: '',
                buttons: Ext.Msg.YESNO,
                closable: false,
                modal: true,
                width: 400,
                fn: function(btnId) {
                    if (btnId == 'yes' && options.yesCallback) {
                        options.yesCallback.call(options.scope);
                    } else if (options.noCallback) {
                        options.noCallback.call(options.scope);
                    }
                },
                icon: Ext.MessageBox.QUESTION
            }, options));
            
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
            Ext.Msg.show(Ext.apply({
                title: "Information",
                msg: '',
                modal: false,
                width: 400,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.INFO
            }, options));
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
            Ext.Msg.show(Ext.apply({
                title: "Erreur",
                msg: '',
                modal: false,
                width: 400,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            }, options));
        },
        
        /**
         * APIMethod: isUrl
         *
         * Parameters:
         * s - {String} test string
         *
         * Returns:
         * {Boolean}
         */
        isUrl: function(s) {
            var regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
            return regexp.test(s);
        },
        
        /**
         * APIMethod: isNumericType
         *
         * Parameters:
         * type - {String} type to test if numeric
         *
         * Returns:
         * {Boolean}
         */
        isNumericType: isNumericType,

        /**
         * APIMethod: isStringType
         *
         * Parameters:
         * type - {String} type to test if string
         *
         * Returns:
         * {Boolean}
         */
        isStringType: isStringType,

        /**
         * APIMethod: getAttributesComboTpl
         *
         * Returns:
         * {Ext.XTemplate} a template for configuring Ext.form.ComboBox
         */
        getAttributesComboTpl: function() {
            return tplAttribute;
        },
         
        /**
         * Method: round
         * Rounds a float with a given number of decimals.
         */
        round: function(input, decimals) {
            var p = Math.pow(10, decimals);
            return Math.round(input*p)/p;
        }
    };
})();
