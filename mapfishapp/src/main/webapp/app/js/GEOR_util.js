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
 * @include GEOR_config.js
 * @include OpenLayers/Util.js
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
    var tplAttribute = null;

    return {

        /**
         * ISO639 correspondance hash
         */
        ISO639: {
            'de': 'ger', 
            'en': 'eng', 
            'es': 'spa', 
            'fr': 'fre', 
            'ru': 'rus'
        },

        /**
         * APIMethod: getProtocol
         * Returns the protocol name for a layer
         *
         * Parameters:
         * layer - {GeoExt.data.LayerRecord | OpenLayers.Layer}
         */
        getProtocol: function(layer) {
            if (layer instanceof OpenLayers.Layer.WMS) {
                return {
                    protocol: "WMS", 
                    version: layer.params.VERSION,
                    service: layer.url,
                    layer: layer.params.LAYERS
                };
            } else if (layer instanceof OpenLayers.Layer.WMTS) {
                return {
                    protocol: "WMTS", 
                    version: layer.version,
                    service: layer.url,
                    layer: layer.layer
                };
            } else if (layer instanceof OpenLayers.Layer.Vector &&
                layer.protocol &&
                /OpenLayers\.Protocol\.WFS/.test(layer.protocol.CLASS_NAME)) {
                    return {
                        protocol: "WFS", 
                        version: layer.protocol.version,
                        service: layer.protocol.url,
                        layer: layer.protocol.featureType
                    };
            } else if (layer instanceof GeoExt.data.LayerRecord) {
                return GEOR.util.getProtocol(layer.get('layer'));
            }
        },

        /**
         * APIMethod: sortFn
         * Function used to sort alphabetically
         */
        sortFn: function(a, b) {
            var aa = a.toLowerCase(),
                bb = b.toLowerCase();
            if (aa > bb) return 1;
            if (aa < bb) return -1;
            return 0;
        },

        /**
         * APIMethod: shorten
         * Returns a shorter string to a given length
         *
         * Parameters:
         * t - {String}
         * length - {Integer}
         */
        shorten: function(t, length) {
            return ((t.length > length) ? t.substr(0, length-3) + '...' : t);
        },

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
            return GEOR.util.shorten(t, 40);
        },

        /**
         * APIMethod: stringUpperCase
         * Returns a string with first letter uppercased and all others lowercased
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String} input string with first letter uppercased
         */
        stringUpperCase: function(str) {
            return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        },

        /**
         * APIMethod: stringReplaceCharCode
         * Replace char codes in string
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String} input string with char codes replaced by the actual char
         */
        stringReplaceCharCodes: function(str) {
            return str.replace(/&#(\d+);?/g, function() {
                return String.fromCharCode(arguments[1])
            });
        },

        /**
         * APIMethod: Capitalize
         * Returns a string with first letter uppercased
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String} input string with first letter uppercased
         */
        Capitalize: function(str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
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
            str = str.replace(/ç/g, 'c');
            str = str.replace(/(á|à|ä|â|å|Â|Ä|Á|À|Ã)/g, 'a');
            str = str.replace(/(é|è|ë|ê|Ê|Ë|É|È|Ę)/g, 'e');
            str = str.replace(/(í|ì|ï|î|Î|Ï|Í|Ì|Į)/g, 'i');
            str = str.replace(/(ó|ò|ö|ô|ø|Ô|Ö|Ó|Ò)/g, 'o');
            return str.replace(/(ú|ù|ü|û|Û|Ü|Ú|Ù|Ų)/g, 'u');
        },

        /**
         * APIProperty: specialCharsRegExp
         */
        specialCharsRegExp: new RegExp("[,;:/%()!*.\\[\\]~&=-]","g"),

        /**
         * APIMethod: prepareString
         * Returns a string without accents or special chars, uppercased
         *
         * Parameters:
         * str - {String}
         *
         * Returns:
         * {String}
         */
        prepareString: function(str) {
            // remove special chars and spaces
            var t = str.replace(GEOR.util.specialCharsRegExp, '').replace(/ /g, '');
            // substitue accents & uppercase:
            return GEOR.util.stringDeaccentuate(t).toUpperCase();
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
            if (path.indexOf(GEOR.config.PATHNAME) === 0) {
                path = path.slice(GEOR.config.PATHNAME.length);
                if (path.indexOf("/") === 0) {
                    path = path.slice(1);
                }
                return path;
            }
        },

        /**
         * APIMethod: getValidURI
         * Given an URI or a path relative to /mapfishapp,
         * Outputs the valid, full, URI.
         *
         * Parameters:
         * input - {String} URI or relative path
         *
         * Returns:
         * {String} a valid URI
         */
        getValidURI: function(input) {
            if (GEOR.util.isUrl(input)) {
                return input;
            }
            return [
                window.location.protocol, '//', window.location.host,
                GEOR.config.PATHNAME, '/', input
            ].join('');
        },
        
        /**
         * APIMethod: getMetadataURLs
         * Given a record, or record data, returns metadata urls
         *
         * Parameters:
         * record - {Ext.data.Record}
         *
         * Returns:
         * {Object} metadataURLs keyed with htmlurls and xmlurls
         */
        getMetadataURLs: function(record) {
            if (record instanceof GeoExt.data.LayerRecord) {
                record = record.data;
            }
            var out = {
                "htmlurls": [],
                "xmlurls": []
            };
            Ext.each(record.metadataURLs, function(murl) {
                var f = murl.format;
                if (murl && !f) {
                    // considering that WMCs only store the HTML metadata URL
                    out.htmlurls.push(murl);
                    return;
                }
                if (/^text\/html|application\/xhtml(\+xml)?$/.test(f)) {
                    out.htmlurls.push(murl.href);
                }
                if (/^text\/xml|application\/xml$/.test(f)) {
                    out.xmlurls.push(murl.href);
                }
            });
            return out;
        },

        /**
         * APIMethod: mdwindow
         * Given an XML MD url, fetch it and display MD essentials in a popup
         *
         * Parameters:
         * xmlMetadataURL - {String}
         * htmlMetadataURL - {String} (optional)
         *
         * Returns:
         * {Boolean} false
         */
        mdwindow: function(xmlMetadataURL, htmlMetadataURL) {
            if (!xmlMetadataURL) {
                return;
            }
            var tr = OpenLayers.i18n;
            GEOR.waiter.show();
            OpenLayers.Request.GET({
                url: xmlMetadataURL,
                success: function(response) {
                    var f = new OpenLayers.Format.CSWGetRecords();
                    try {
                        var o = f.read(response.responseXML || response.responseText);
                    } catch(e) {
                        GEOR.util.errorDialog({
                            msg: tr("Could not parse metadata.")
                        });
                    }
                    if (o && o.records && o.records[0]) {
                        GEOR.util.urlDialog({
                            title: GEOR.util.getMDtitle(o.records[0]),
                            msg: GEOR.util.makeMD(o.records[0]),
                            buttons: [{
                                text: tr('More'),
                                disabled: !htmlMetadataURL,
                                handler: function() {
                                    window.open(htmlMetadataURL);
                                }
                            }, {
                                text: tr('OK'),
                                handler: function() {
                                    this.ownerCt.ownerCt.close();
                                }
                            }]
                        });
                    } else {
                        GEOR.util.errorDialog({
                            msg: tr("Could not parse metadata.")
                        });
                    }
                },
                failure: function() {
                    GEOR.util.errorDialog({
                        msg: tr("Could not get metadata.")
                    });
                }
            });
            return false;
        },

        /**
         * APIMethod: getMDtitle
         * Given a MD object, returns its title
         *
         * Parameters:
         * metadata - {Object}
         *
         * Returns:
         * {String} metadata title
         */
        getMDtitle: function(metadata) {
            var o = '';
            try {
                o = metadata.identificationInfo[0].citation.title.characterString;
            } catch (e) {}
            return o;
        },

        /**
         * APIMethod: makeMD
         * Given a MD object, creates some markup to render it
         *
         * Parameters:
         * metadata - {Object}
         *
         * Returns:
         * {String} html markup
         */
        makeMD: function(metadata) {
            // TODO: let the platform admin specify his own custom MD template:
            var tpl = [
                '{[this.abstract(values)]}',
                '{[this.lineage(values)]}',
                '{[this.dates(values)]}',
                '{[this.contacts(values)]}'
            ].join('');
            var ctx = {
                "abstract": function(v) {
                    var o = '';
                    try {
                        o = v.identificationInfo[0]['abstract'].characterString + '<br/><br/>';
                    } catch (e) {}
                    return o;
                },
                "lineage": function(v) {
                    var o = '';
                    try {
                        o = v.dataQualityInfo[0].lineage.statement.characterString + '<br/><br/>';
                    } catch (e) {}
                    return o;
                },
                "dates":  function(v) {
                    var a = v.identificationInfo[0].citation.date, 
                        o = [];
                    if (!a[0]) {
                        return '';
                    }
                    Ext.each(a, function(date) {
                        try {
                            var type = OpenLayers.i18n(date.dateType.codeListValue),
                                datetime = date.date[0].dateTime.split("T")[0];
                            o.push(type+OpenLayers.i18n('labelSeparator')+datetime);
                        } catch (e) {}
                    });
                    return o.join('<br/>')+'<br/><br/>';
                },
                "contacts": function(v) {
                    var a = v.contact,
                        o = [];
                    if (!a[0]) {
                        return '';
                    }
                    Ext.each(a, function(contact) {
                        try {
                            var role = OpenLayers.i18n(contact.role.codeListValue),
                                email = contact.contactInfo.address.electronicMailAddress[0].characterString;
                            o.push(role+OpenLayers.i18n('labelSeparator')+'<a href="mailto:'+email+'" target="_blank">'+email+'</a>');
                        } catch (e) {}
                    });
                    return o.join('<br/>');
                }
            };
            return new Ext.XTemplate(tpl, ctx).apply(metadata);
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
                title: OpenLayers.i18n("Confirmation"),
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
                title: OpenLayers.i18n("Information"),
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
                title: OpenLayers.i18n("Error"),
                msg: '',
                modal: false,
                width: 400,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            }, options));
        },
        
        /**
         * APIMethod: urlDialog
         * Shows a dialog box suitable for a message with an URL
         *
         * Parameters:
         * options - {Object} Hash with keys:
         *      title, msg
         */
        urlDialog: function(options) {
            var win = new Ext.Window({
                title: options.title,
                layout: "fit",
                width: options.width || 400,
                maxHeight: 400,
                closeAction: 'close',
                constrainHeader: true,
                modal: false,
                defaultButton: 0,
                items: [{
                    bodyStyle: 'padding:5px',
                    autoScroll: true,
                    html: options.msg,
                    maxHeight: 400,
                    border: false
                }],
                buttons: options.buttons || [{
                    text: OpenLayers.i18n("Thanks!"),
                    handler: function() {
                        win.close();
                    }
                }],
                listeners: {
                    'afterrender': function(w) {
                        if (w.getHeight() > w.maxHeight) {
                            w.setHeight(w.maxHeight);
                        }
                    }
                }
            });
            win.show();
        },

        /**
         * APIMethod: extButton
         *
         * Parameters:
         * options - {Object} Button config
         *
         * Returns:
         * {String} the HTML string to generate a button
         */
        extButton: function(options) {
            return [
                '<table cellspacing="0" class="x-btn x-btn-text-icon" style="width: auto;">',
                   '<tbody class="x-btn-small x-btn-icon-small-left">',
                      '<tr>',
                         '<td class="x-btn-tl"><i>&nbsp;</i></td>',
                         '<td class="x-btn-tc"></td>',
                         '<td class="x-btn-tr"><i>&nbsp;</i></td>',
                      '</tr>',
                      '<tr>',
                         '<td class="x-btn-ml"><i>&nbsp;</i></td>',
                         '<td class="x-btn-mc"><em class=" x-unselectable" unselectable="on">',
                            '<button ext:qtip="', options.tooltip, '" type="button" class=" x-btn-text ', options.iconCls, '" onclick="', options.handler,'">',
                            options.text,
                            '</button></em></td>',
                         '<td class="x-btn-mr"><i>&nbsp;</i></td>',
                      '</tr>',
                      '<tr>',
                         '<td class="x-btn-bl"><i>&nbsp;</i></td>',
                         '<td class="x-btn-bc"></td>',
                         '<td class="x-btn-br"><i>&nbsp;</i></td>',
                      '</tr>',
                   '</tbody>',
                '</table>'].join('');
        },

        /**
         * APIMethod: extFbar
         *
         * Parameters:
         * options - {Object} config
         *
         * Returns:
         * {String} the HTML string to generate a footer bar with buttons
         */
        extFbar: function(options) {
            var tds = '';
            Ext.each(options.buttons, function(btn) {
                tds += '<td class="x-toolbar-cell">'+btn+'</td>'
            });
            return [
'<div class="x-panel-fbar x-small-editor x-toolbar-layout-ct" style="width: auto;">',
   '<table cellspacing="0" class="x-toolbar-ct">',
      '<tbody>',
         '<tr>',
            '<td class="x-toolbar-left" align="left">',
               '<table cellspacing="0">',
                  '<tbody>',
                     '<tr class="x-toolbar-left-row"></tr>',
                  '</tbody>',
               '</table>',
            '</td>',
            '<td class="x-toolbar-right" align="right">',
               '<table cellspacing="0" class="x-toolbar-right-ct">',
                  '<tbody>',
                     '<tr>',
                        '<td>',
                           '<table cellspacing="0">',
                              '<tbody>',
                                 '<tr class="x-toolbar-right-row">',
                                    tds,
                                 '</tr>',
                              '</tbody>',
                           '</table>',
                        '</td>',
                        '<td>',
                           '<table cellspacing="0">',
                              '<tbody>',
                                 '<tr class="x-toolbar-extras-row"></tr>',
                              '</tbody>',
                           '</table>',
                        '</td>',
                     '</tr>',
                  '</tbody>',
               '</table>',
            '</td>',
         '</tr>',
      '</tbody>',
   '</table>',
'</div>'].join('');
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
         * APIMethod: splitURL
         *
         * Parameters:
         * url - {String} full url string
         *
         * Returns:
         * {Object} with keys: 
         *    - serviceURL - the full service URL without any parameter
         *    - params - an object with uppercased keys for each GET parameter
         */
        splitURL: function(url) {
            var o = OpenLayers.Util.createUrlObject(url,
                {ignorePort80: true}
            );
            var u = o.protocol + "//" +  o.host;
            // https forces port 443, no need to add it:
            if (o.port && o.protocol != "https") {
                u += ":" + o.port;
            }
            u += o.pathname;
            return {
                serviceURL: u,
                params: OpenLayers.Util.upperCaseObject(o.args)
            };
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
            if (!tplAttribute) {
                tplAttribute = new Ext.XTemplate(
                    '<tpl for=".">',
                        '<tpl if="this.isString(type)">',
                            '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>'+OpenLayers.i18n("Characters")+'</span></div>',
                        '</tpl>',
                        '<tpl if="this.isNumeric(type)">',
                            '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>'+OpenLayers.i18n("Digital")+'</span></div>',
                        '</tpl>',
                        '<tpl if="this.isDate(type)">',
                            '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>'+OpenLayers.i18n("Date")+'</span></div>',
                        '</tpl>',
                        '<tpl if="this.isBoolean(type)">',
                            '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>'+OpenLayers.i18n("Boolean")+'</span></div>',
                        '</tpl>',
                        '<tpl if="this.isAnother(type)">',
                            '<div ext:qtip="{name}" class="x-combo-list-item">{name} <span>'+OpenLayers.i18n("Other")+'</span></div>',
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
            }
            return tplAttribute;
        },

        /**
         * Method: getStyleMap
         * Returns the standardized StyleMap
         *
         * Parameters:
         * override - {Object} optional hash with properties "default", "select"
         *
         * Returns:
         * {OpenLayers.StyleMap}
         */
        getStyleMap: function(override) {
            override = override || {};
            var defStyle = OpenLayers.Util.extend({},
                OpenLayers.Feature.Vector.style['default']);
            var selStyle = OpenLayers.Util.extend({},
                OpenLayers.Feature.Vector.style['select']);
            var c = {
                "_name": function(feature) {
                    return feature.attributes.name ? feature.attributes.name : "";
                }
            };
            return new OpenLayers.StyleMap({
                "default": new OpenLayers.Style(
                    Ext.apply(defStyle, Ext.apply({
                        cursor: "pointer",
                        fillOpacity: 0.1,
                        strokeWidth: 3
                    }, override["default"] || {}))
                , {context: c}),
                "select": new OpenLayers.Style(
                    Ext.apply(selStyle, Ext.apply({
                        cursor: "pointer",
                        strokeWidth: 3,
                        fillOpacity: 0.1,
                        graphicZIndex: 1000
                    }, override["select"] || {}))
                , {context: c})
            });
        },

        /**
         * Method: createRecordType
         * Returns a layer record type with additional methods.
         *
         * Parameters:
         * fields - {Array} an array of field config objects
         *
         * Returns:
         * {Function} A specialized `GeoExt.data.LayerRecord` constructor.
         */
        createRecordType: function(fields) {
            var recordType = GeoExt.data.LayerRecord.create(fields);
            return Ext.extend(recordType, {
                hasEquivalentWFS: function() {
                    return !!this.get("WFS_URL") && !!this.get("WFS_typeName");
                },
                hasEquivalentWCS: function() {
                    return !!this.get("WCS_URL") && !!this.get("WCS_typeName");
                }
            });
        },

        /**
         * Method: round
         * Rounds a float with a given number of decimals.
         */
        round: function(input, decimals) {
            var p = Math.pow(10, decimals);
            return Math.round(input*p)/p;
        },

        /**
         * Method: isSuitableDCProtocol
         *
         * Returns:
         * {Boolean} true if the WMS protocol matches and the value is a valid
         *  URL and the layer name is set
         */
        isSuitableDCProtocol: function(item) {
            if (!item.protocol) {
                return false;
            }
            var c = {
                'OGC:WMS': true,
                'OGC:WMS-1.0.0-http-get-map': true,
                'OGC:WMS-1.1.0-http-get-map': true,
                'OGC:WMS-1.1.1-http-get-map': true,
                'OGC:WMS-1.3.0-http-get-map': true
            };
            return !!c[item.protocol] && !!item.name && GEOR.util.isUrl(item.value);
        },

        /**
         * APIMethod: registerTip
         * Registers quick tip on menuitem
         *
         * Parameters:
         * menuItem - {Ext.menu.BaseItem}
         */
        registerTip: function (menuItem) {
            var qtip = typeof (menuItem.qtip) == 'string'
                        ? {text: menuItem.qtip}
                        : menuItem.qtip;
            qtip = Ext.apply(qtip, {
                target: menuItem.getEl().getAttribute('id')
            });
            Ext.QuickTips.register(qtip);
        },

        /**
         * APIMethod: isInvalidRing
         *
         */
        isInvalidRing: function(ring) {
            // Linear ring must have 0 or more than 2 points
            if (!((ring.components.length == 0) ||
                (ring.components.length > 2))) {
                return false;
            }
        },

        /**
         * APIMethod: hasInvalidGeometry
         * Will return id if layer's geometry is invalid
         */
        hasInvalidGeometry: function(feature) {
            var geometry = feature.geometry;
            if (geometry.CLASS_NAME == "OpenLayers.Geometry.Polygon") {
                var invalid = Ext.each(geometry.components, GEOR.util.isInvalidRing);
                if (invalid >= 0) {
                    return false;
                }
            } else if (geometry.CLASS_NAME == "OpenLayers.Geometry.LineString") {
                // LineString must have 0 or more than 1 points
                if (!((geometry.components.length == 0) ||
                    (geometry.components.length > 1))) {
                    return false;
                }
            }
        }

    };
})();


/**
 * Creates a menu that supports tooltip specs for it's items. Just add "tooltip: {text: 'txt', title: 'ssss'}" to
 * the menu item config, "title" value is optional.
 * @class Ext.ux.MenuQuickTips
 * see http://www.sencha.com/forum/showthread.php?77312-Is-it-possible-to-add-tooltip-to-menu-item
 */
Ext.ux.MenuQuickTips = Ext.extend(Object, {
    init: function (c) {
        c.menu.items.each(function (item) {
            if (typeof (item.qtip) != 'undefined') {
                item.on('afterrender', GEOR.util.registerTip);
            }
        });
    }
});
Ext.preg('menuqtips', Ext.ux.MenuQuickTips);