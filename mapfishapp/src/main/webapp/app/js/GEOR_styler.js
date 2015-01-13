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
 * @include OpenLayers/Request.js
 * @include OpenLayers/Rule.js
 * @include OpenLayers/Filter/Comparison.js
 * @include OpenLayers/Filter/Logical.js
 * @include OpenLayers/Format/SLD/v1_0_0.js
 * @include Styler/widgets/RulePanel.js
 * @include Styler/widgets/LegendPanel.js
 * @include Styler/Util.js
 * @include Ext.ux/widgets/colorpicker/ColorPicker.js
 * @include Ext.ux/widgets/colorpicker/ColorPickerField.js
 * @include GEOR_ClassificationPanel.js
 * @include GEOR_ows.js
 * @include GEOR_config.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.styler = (function() {
    /*
     * Private
     */

    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: sldready
         * Fires when a new sld is ready on the server.
         */
        "sldready"
    );

	/**
     * Property: win
     * {Ext.Window} The styler window.
     */
    var win = null;

	/**
	 * Property: wmsLayerRecord
	 * {GeoExt.data.LayerRecord} The record representing the WMS layer
     * to style.
	 */
	var wmsLayerRecord = null;

    /**
     * Property: wfsInfo
     * {Ext.data.Record} A record with WFS information (data fields
     * are "owsType", "owsURL" and "typeName").
     */
    var wfsInfo = null;

	/**
     * Property: mask
     * {Ext.LoadingMask} The window's loading mask.
     */
    var mask = null;

    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} The attribute store associated
     * with the layer being styled.
     */
    var attributes = null;

    /**
     * Property: sldURL
     * {String} The URL to the SLD document.
     */
    var sldURL = null;

    /**
     * Property: dirty
     * {Boolean} Is true when the SLD pointed to by sldURL
     * does not match the set of rules in the legend panel.
     */
    var dirty = false;

    /**
     * Property: legendContainer
     * {Ext.Panel} This panel includes the legend panel, it uses
     * a fit layout.
     */
    var legendContainer = null;

    /**
     * Property: classifContainer
     * {Ext.Panel} This panel includes the classif panel, it uses
     * a fit layout.
     */
    var classifContainer = null;

    /**
     * Property: ruleContainer
     * {Ext.Panel} This panel includes the rule panel, it uses
     * a fit layout.
     */
    var ruleContainer = null;

    /**
     * Property: symbolType
     * The symbol type of the layer being styled, act as a
     * cache to avoid requesting the server.
     */
    var symbolType = null;

    /**
     * Property: geometryName
     * {String} The name of the geometry column
     */
    var geometryName = null;

    /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Method: getLegendPanel
     * Get a reference to the legend panel.
     */
    var getLegendPanel = function() {
        var legendPanel = null;
        if (legendContainer.items.getCount() > 1) {
            legendPanel = legendContainer.getComponent(1);
        }
        return legendPanel;
    };

    /**
     * Method: getHelpCmp
     * Get a reference to the component containing the help
     * string.
     */
    var getHelpCmp = function() {
        return legendContainer.getComponent(0);
    };

    /**
     * Method: getDeleteButton
     * Get a reference to the "delete rule" button.
     */
    var getDeleteButton = function() {
        return legendContainer.getBottomToolbar().items.get(3);
    };

    /**
     * Method: applyStyling
     * Apply the new style to the layer if we have
     * a new style.
     *
     * Parameters:
     * callback - {Function}
     * scope - {Object}
     */
    var applyStyling = function(callback, scope) {
        if (dirty) {
            // refreshing the layer will be done
            // once the SLD is saved
            saveSLD(callback, scope);
        } else {
            if (sldURL) {
                // sldURL matches our set of rules, we
                // fire the "sldready" event.
                observable.fireEvent(
                    "sldready",
                    wmsLayerRecord,
                    sldURL
                );
            }
            callback.apply(scope, [true]);
        }
    };

    /**
     * Method: dlStyle
     * Download a SLD file from created styling
     *
     * Parameters:
     * callback - {Function}
     * scope - {Object}
     */
    var dlStyle = function() {
        var callback = function(ok, sldURL) {
            if (!sldURL) {
                return;
            }
            GEOR.util.urlDialog({
                title: tr("Download style"),
                msg: tr("You can download your SLD style at ") +
                        '<br /><a href="'+sldURL+'">'+sldURL+'</a>'
            });
        };
        var scope = this;
        if (dirty) {
            // refreshing the layer will not be done
            // in this case (cf third arg)
            saveSLD(callback, scope, false);
        } else {
            sldURL && callback.apply(scope, [true, sldURL]);
        }
    };

    /**
     * Method: saveSLD
     * Build a SLD string from the set of rules and send it to
     * the "ws/sld" web service.
     *
     * Parameters:
     * callback - {Function}
     * scope - {Object}
     * applySLD  - {Boolean} should we apply the style when done - defaults to true
     */
    var saveSLD = function(callback, scope, applySLD) {
        var ok = true, rules = getLegendPanel().rules;
        applySLD = (applySLD !== false) ? true : false;
        if (rules && rules.length > 0) {
            var data = createSLD(rules);

            if (data === null) {
                ok = false;
            } else {
                // define the callbacks
                var success = function(response) {
                    sldURL = [
                        window.location.protocol, '//', window.location.host,
                        GEOR.config.PATHNAME, '/',
                        Ext.decode(response.responseText).filepath
                    ].join('');
                    applySLD && observable.fireEvent(
                        "sldready",
                        wmsLayerRecord,
                        sldURL
                    );
                    // indicate that the SLD at sldURL matches
                    // our set of rules
                    dirty = false;

                    mask.hide();
                    callback.apply(scope, [true, sldURL]);
                };
                var failure = function(response) {
                    mask.hide();
                    callback.apply(scope, [false]);
                };
                mask.msg = tr("Saving SLD");
                mask.show();
                Ext.Ajax.request({
                    url: GEOR.config.PATHNAME + "/ws/sld/",
                    method: "POST",
                    headers: {
                        "Content-Type": "application/vnd.ogc.sld+xml; charset=UTF-8"
                    },
                    xmlData: data,
                    success: success,
                    failure: failure
                });
            }
        }
        callback.apply(scope,  [ok]);
    };

    /**
     * Method: createSLD
     * Create a SLD from a set of rules.
     *
     * Parameters:
     * {Array({OpenLayers.Rule})} The set of rules.
     *
     * Returns:
     * {String} The SLD string.
     */
    var createSLD = function(rules) {
        if (!validateRules(rules)) {
            GEOR.util.errorDialog({
                msg: tr("Some classes are invalid, verify that all fields " +
                    "are correct")
            });
            return null;
        }
        return new OpenLayers.Format.SLD().write({
            "namedLayers": [{
                "name": wfsInfo.get("typeName"),
                "userStyles": [
                    new OpenLayers.Style(undefined, {
                        rules: rules
                    })
                ]
            }]
        });
    };

    /**
     * Method: getSLD
     * Get a SLD from the "ws/sld" web service.
     *
     * Parameters:
     * url - {String} The URL to the SLD doc.
     */
    var getSLD = function(url) {
        if (url) {
            mask.msg = tr("Get SLD");
            mask.show();
            // define the callbacks
            var success = function(request) {
                var doc = request.responseXML;
                if (!doc || !doc.documentElement) {
                    doc = request.responseText;
                }
                var sld = new OpenLayers.Format.SLD().read(doc, {
                    namedLayersAsArray: true
                });
                var rules =
                    sld &&
                    sld.namedLayers &&
                    sld.namedLayers.length > 0 &&
                    sld.namedLayers[0].name == wfsInfo.get("typeName") &&
                    sld.namedLayers[0].userStyles &&
                    sld.namedLayers[0].userStyles.length > 0 &&
                    sld.namedLayers[0].userStyles[0].rules;
                if (rules) {
                    // we're about to add a rules in the legend panel,
                    // hide the help message
                    getHelpCmp().hide();
                    newLegendPanel(rules);
                    mask.hide();
                    dirty = false;
                    sldURL = url;
                } else {
                    mask.hide();
                    Ext.Msg.alert(
                        tr("Error"),
                        tr("Malformed SLD")
                    );
                }
            };
            var failure = function(request) {
                mask.hide();
            };
            Ext.Ajax.request({
                method: "GET",
                url: url,
                success: success,
                failure: failure
            });
        }
    };

    /**
     * Method: newLegendPanel
     * Create a legend panel and add it to the legend
     * container.
     *
     * Parameters:
     * rule - {Array({OpenLayers.Rule})} The array of rules.
     */
    var newLegendPanel = function(rules) {
        var legendPanel = getLegendPanel();
        if (legendPanel) {
            legendContainer.remove(legendPanel);
        }
        legendPanel = legendContainer.add(new Styler.LegendPanel({
            rules: rules,
            border: false,
            style: {padding: "10px"},
            selectOnClick: true,
            listeners: {
                "ruleselected": function(panel, rule) {
                    showRule(rule);
                    getDeleteButton().enable();
                },
                "ruleunselected": function(panel, rule) {
                    getDeleteButton().disable();
                },
                "rulemoved": function(panel, rule) {
                    dirty = true;
                }
            }
        }));
        legendContainer.doLayout();
        return legendPanel;
    };

    /**
     * Method: validateRules
     * Validate a set of rules.
     *
     * Parameters:
     * rules - {Array({OpenLayers.Rule})}
     *
     * Returns:
     * {Boolean}
     */
    var validateRules = function(rules) {
        var i, l, valid = true;
        for (i=0,l=rules.length; i<l; i++) {
            valid = valid && validateRule(rules[i]);
        }
        return valid;
    };

    /**
     * Method: validateRule
     * Return true if all the leaf filters of the rule
     * have a type, and false otherwise.
     *
     * Parameters:
     * rule - {OpenLayers.Rule}
     *
     * Returns:
     * {Boolean}
     */
    var validateRule = function(rule) {
        var validateFilter = function(f) {
            var v = true;
            if (f.filters) {
                for (var i=0,l=f.filters.length; i<l; i++) {
                    v = v && validateFilter(f.filters[i]);
                }
            } else {
                v = !!f.type;
            }
            return v;
        };
        var valid = true;
        if (rule.filter) {
            valid = validateFilter(rule.filter);
        }
        return valid;
    };

    /**
     * Method: fixFilter
     * Cast the values based on the layer schema. This is necessary
     * because the SLD format converts strings to numeric values.
     *
     * Parameters:
     * f - {OpenLayers.Filter} The filter.
     */
    var fixFilter = function(f) {
        if (f instanceof OpenLayers.Filter.Comparison) {
            var idx = attributes.find("name", f.property);
            if (idx > -1) {
                var rec = attributes.getAt(idx), type = rec.get("type");
                if (GEOR.util.isStringType(type)) {
                    f.value += "";
                }
            }
        } else if (f.filters) {
            for (var i=0, l=f.filters.length; i<l; i++) {
                fixFilter(f.filters[i]);
            }
        }
    };

    /**
     * Method: showRule
     * Add a rule panel for that rule.
     *
     * Parameters:
     * rule - {OpenLayers.Rule} The rule.
     */
    var showRule = function(rule) {

        // before anything, if the rule includes a filter, fix
        // that filter
        if (rule.filter) {
            fixFilter(rule.filter);
        }

        var layer = wmsLayerRecord.get("layer");

        ruleContainer.removeAll();
        var rulePanel = ruleContainer.add({
            xtype: 'gx_rulepanel',
            border: false,
            attributes: attributes,
            attributesComboConfig: {
                tpl: GEOR.util.getAttributesComboTpl()
            },
            rule: rule,
            nestedFilters: false,
            scaleLevels: layer.numZoomLevels,
            minScaleLimit: OpenLayers.Util.getScaleFromResolution(
                layer.resolutions[layer.numZoomLevels-1],
                layer.map.units
            ),
            maxScaleLimit: OpenLayers.Util.getScaleFromResolution(
                layer.resolutions[0],
                layer.map.units
            ),
            pointGraphics: [
                {display: tr("circle"), value: "circle", mark: true,
                 preview: "lib/externals/styler/theme/img/circle.gif"},
                {display: tr("square"), value: "square", mark: true,
                 preview: "lib/externals/styler/theme/img/square.gif"},
                {display: tr("triangle"), value: "triangle", mark: true,
                 preview: "lib/externals/styler/theme/img/triangle.gif"},
                {display: tr("star"), value: "star", mark: true,
                 preview: "lib/externals/styler/theme/img/star.gif"},
                {display: tr("cross"), value: "cross", mark: true,
                 preview: "lib/externals/styler/theme/img/cross.gif"},
                {display: tr("x"), value: "x", mark: true,
                 preview: "lib/externals/styler/theme/img/x.gif"},
                {display: tr("customized...")}
            ],
            symbolType: Styler.Util.getSymbolTypeFromRule(rule),
            listeners: {
                change: function(c, r) {
                    getLegendPanel().updateRuleEntry(r);
                    // indicate that some rule has changed
                    dirty = true;
                }
            }
        });

        stylerContainer.getLayout().setActiveItem(0);
        stylerContainer.doLayout();
    };

    /**
     * Method: getSymbolTypeFromFeature
     * Trigger a WFS GetFeature request with maxFeatures set to
     * 1 and get the symbol type from the feature received.
     *
     * Parameters:
     * callback - {Function} The function called once the symbol type is
     *     determined.
     * scope - {Object} The callback execution context.
     */
    var getSymbolTypeFromFeature = function(callback, scope) {
        var map = wmsLayerRecord.get("layer").map;
        var protocol = GEOR.ows.WFSProtocol(wfsInfo, map);
        protocol.read({
            maxFeatures: 1,
            // we need to specifically ask for the geometry here
            // some mapservers won't give it
            // see http://applis-bretagne.fr/redmine/issues/1996
            propertyNames: [geometryName],
            callback: function(response) {
                var type;
                if (response.success() &&
                    response.features &&
                    response.features.length > 0) {
                    var feature = response.features[0];
                    type = feature.geometry.CLASS_NAME.replace(
                        /OpenLayers\.Geometry\.(Multi)?|String/g, ""
                    );
                }
                callback.apply(scope, [type]);
            }
        });
    };

    /**
     * Method: getSymbolTypeFromSchema
     * Determine the symbol type (Point, Line, or Polygon) of the layer. This
     * is based on the first geometry type field in the schema. If the type
     * is gml:GeometryType, the symbol will be undefined.
     *
     * Parameters:
     * callback - {Function} The function called once the symbol type is
     *     determined.
     * scope - {Object} The callback execution context.
     */
    var getSymbolTypeFromSchema = function(callback, scope) {
        var cb = function(st, recs, opts) {
            var type = GEOR.ows.getSymbolTypeFromAttributeStore(st).type;
            callback.apply(scope, [type]);
        };
        GEOR.ows.WFSDescribeFeatureType(wfsInfo, {
            extractFeatureNS: true,
            success: cb
        });
    };

    /**
     * Method: getSymbolType
     * Determine the symbol type (Point, Line, or Polygon) given a layer.
     *
     * Parameters:
     * callback - {Function} The function called once the symbol type is
     *     determined.
     * scope - {Object} The callback execution context.
     */
    var getSymbolType = function(callback, scope) {
        if (symbolType) {
            // cache hit
            callback.apply(scope, [symbolType]);
        } else {
            // cache miss
            var n = 0;
            var cb = function(type) {
                if (type || ++n > 2) {
                    symbolType = type;
                    callback.apply(scope, [type]);
                } else {
                    getSymbolTypeFromFeature(cb);
                }
            };
            getSymbolTypeFromSchema(cb);
        }
    };

    /**
     * Method: classify
     * Launch the classification process.
     *
     * Parameters:
     * params - {Object} The classification parameters.
     */
    var classify = function(params) {
        mask.msg = tr("Classification ...<br/>(this operation can take " +
            "some time)");
        mask.show();
        
        // HACK: since GeoTools WFSDatasource doesn't support WFS 2.0.0, 
        // we must use an earlier WFS version to make Styler work.
        params.wfs_url = OpenLayers.Util.urlAppend(params.wfs_url, 
            "VERSION=1.0.0");
        
        OpenLayers.Request.POST({
            url: GEOR.config.PATHNAME + "/ws/sld/",
            data: Ext.util.JSON.encode(params),
            headers: {
                "Content-Type": "application/json"
            },
            success: function(response) {
                // response contains the URL to the
                // SLD stored on the server
                var url = [
                    window.location.protocol, '//', window.location.host,
                    GEOR.config.PATHNAME, '/', 
                    Ext.decode(response.responseText).filepath
                ].join('');
                getSLD(url);
                // store the path to SLD , we'll need it when
                // applying the new style to the layer
                sldURL = url;

                // indicate that the SLD at sldURL matches
                // our set of rules
                dirty = false;

            },
            failure: function(response) {
                mask.hide();
            },
            scope: this
        });
    };

    /**
     * Method: addRule
     * Add a rule to the legend panel, and show it in the rule
     * container for editing.
     *
     * Parameters:
     * type - {String} The symbol type.
     */
    var addRule = function(type) {
        var symbolizer = {};
        symbolizer[type] = Ext.apply(
            {},
            OpenLayers.Format.SLD.v1.prototype.defaultSymbolizer
        );
        var id = OpenLayers.Util.createUniqueID("");
        var rule = new OpenLayers.Rule({
            name: tr("Class") + "_" + id,
            title: tr("Untitled") + " " + id,
            symbolizer: symbolizer
        });
        // we're about to add a rule entry in the legend panel,
        // hide the help message
        getHelpCmp().hide();
        // create a legend panel if none exists
        var legendPanel = getLegendPanel();
        if (!legendPanel) {
            legendPanel = newLegendPanel();
        }
        legendPanel.rules.push(rule);
        legendPanel.addRuleEntry(rule);
        showRule(rule);
        // indicate a new SLD must be posted to the
        // SLD web service
        dirty = true;
    };

    /**
     * Method: removeRule
     * Remove the rule selected in the legend panel.
     */
    var removeRule = function() {
        var legendPanel = getLegendPanel();
        if (legendPanel && legendPanel.selectedRule) {
            var selectedRule = legendPanel.selectedRule;
            ruleContainer.removeAll();
            legendPanel.unselect();
            legendPanel.removeRuleEntry(selectedRule);
            legendPanel.rules.remove(selectedRule);
            // indicate a new SLD must be posted to the
            // SLD web service
            dirty = true;
            // if there's no longer rule entries in the legend panel
            // we put the help message back
            if (legendPanel.rulesContainer.items.getCount() === 0) {
                getHelpCmp().show();
            }
        }
    };

    /**
     * Method: initStyler.
     * This method is executed once the WMSDescribeLayerStore
     * is loaded, it is responsible for initializing the styler.
     *
     * Parameters:
     * sType - {Object} The symbol type
     */
    var initStyler = function(sType) {

        /*
         * create the rule container
         */
        ruleContainer = new Ext.Panel({
            layout: "fit",
            border: false
        });

        /*
         * create the classif container, with a classification
         * panel as its child
         */
        classifContainer = new Ext.Panel({
            layout: "fit",
            border: false,
            items: {
                xtype: 'geor.classifpanel',
                attributes: attributes,
                symbolType: sType,
                wfsInfo: wfsInfo,
                // launch the classification process when the
                // classification panel asks us to do so
                listeners: {
                    change: function(panel, params) {
                        classify(params);
                    }
                }
            }
        });

        /*
         * create the styler container
         */
        stylerContainer = new Ext.Panel({
            region: "center",
            layout: "card",
            activeItem: 0,
            items: [
                ruleContainer,
                classifContainer
            ]
        });

        /*
         * create the legend container
         */
        legendContainer = new Ext.Panel({
            region: 'west',
            width: 250,
            split: true,
            autoScroll: true,
            items: [{
                xtype: "box",
                border: false,
                autoEl: {
                    tag: "blockquote",
                    style: "padding:5px;",
                    html: tr("styler.guidelines")
                }
            }],
            bbar: [{
                text: tr("Analyze"),
                handler: function(btn, evt) {
                    stylerContainer.getLayout().setActiveItem(1);
                    stylerContainer.doLayout();
                }
            }, '->', {
                iconCls: "add",
                tooltip: tr("Add a class"),
                handler: function(btn, evt) {
                    addRule(sType);
                }
            }, {
                iconCls: "delete",
                tooltip: tr("Delete the selected class"),
                disabled: true,
                handler: function(btn, evt) {
                    removeRule();
                }
            }]
        });

        /*
         * populate the legend container if the layer
         * has an SLD param
         */
        var url = wmsLayerRecord.get("layer").params.SLD;
        if (url) {
            getSLD(url);
        }

        /*
         * add the legend and styler containers to the styler
         * window and enable it
         */
        win.add({
            layout: "border",
            border: false,
            defaults: {border: false},
            items: [
                legendContainer,
                stylerContainer
            ]
        });
        win.doLayout();
        win.enable();
        // if url is defined getURL takes care
        // of hiding the mask
        if (!url) {
            mask && mask.hide();
        }
    };

    var giveup = function(msg) {
        win.add({
            html: "<p>"+msg+"</p>",
            border: false,
            bodyStyle: 'padding:1em;'
        });
        win.doLayout();
        win.enable();
        mask && mask.hide();
    };

	/*
     * Public
     */
    return {

        /*
         * Observable object
         */
        events: observable,

        /**
         * APIMethod: deactivate
         *
         */
        deactivate: function() {
            mask && mask.hide();
            win && win.close();
        },

        /**
         * APIMethod: create
         * Create and open the styler window.
         *
         * Parameters:
         * layerRecord - {GeoExt.data.LayerRecord} The record representing
         * the WMS layer to style.
         * animateFrom - {String} Id or element from which the window
         *  should animate while opening
         */
        create: function(layerRecord, animateFrom) {
            Ext.QuickTips.init();
            tr = OpenLayers.i18n;

            // clear cache:
            symbolType = null;
            mask = null;
            wmsLayerRecord = layerRecord;

            /*
             * win is the styler window, create it and display it.
             */
            win = new Ext.Window({
                title: tr("Styler"),
                layout: "fit",
                width: 900,
                height: 500,
                closeAction: 'close', // window is destroyed when closed
                constrainHeader: true,
                animateTarget: GEOR.config.ANIMATE_WINDOWS && animateFrom,
                modal: false,
                disabled: true,
                buttons: [{
                    text: tr("Close"),
                    handler: function() {
                        win.close();
                    }
                }, {
                    text: tr("Download style"),
                    handler: dlStyle
                },{
                    text: tr("Apply"),
                    handler: function() {
                        // we're done, apply styling
                        // to layer
                        applyStyling(function(ok){
                            return;
                        });
                    }
                }],
                listeners: {
                    "afterrender": function() {
                        mask = new Ext.LoadMask(win.body, {
                            msg: tr("Loading...")
                        });
                        mask.show();
                    }
                }
            });
            win.show();

            var recordType = Ext.data.Record.create([
                {name: "featureNS", type: "string"},
                {name: "WFSVersion", type: "string"},
                {name: "owsURL", type: "string"},
                {name: "typeName", type: "string"}
            ]);
            var data = {
                "owsURL": layerRecord.get("WFS_URL"),
                "typeName": layerRecord.get("WFS_typeName")
            };
            wfsInfo = new recordType(data);

            // store a reference to the store in a
            // private attribute of the instance
            attributes = GEOR.ows.WFSDescribeFeatureType(wfsInfo, {
                success: function(st, recs, opts) {
                    // extract & remove geometry column name
                    var idx = st.find('type', GEOR.ows.matchGeomProperty);
                    if (idx > -1) {
                        // we have a geometry
                        var r = st.getAt(idx);
                        geometryName = r.get('name');
                        st.remove(r);
                    }
                    if (st.getCount() > 0) {
                        // we have at least one attribute that we can style
                        getSymbolType(initStyler);
                    } else {
                        // give up
                        giveup([
                            tr("Impossible to complete the operation:"),
                            tr("no available attribute")
                        ].join(" "));
                    }
                },
                failure: function() {
                    mask && mask.hide();
                    win.close();
                }
            });
        }
    };
})();
