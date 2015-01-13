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
 * @include GEOR_util.js
 * @include GEOR_config.js
 * @include GEOR_waiter.js
 * @include GEOR_localStorage.js
 */

Ext.namespace("GEOR");

GEOR.wmcbrowser = (function() {
    /*
     * Private
     */

    /**
     * Property: popup
     * {Ext.Window}
     */
    var popup;

    /**
     * Property: view
     * {Ext.DataView}
     */
    var view;

    /**
     * Property: keywords
     * {Array} unique context keywords
     */
    var keywords;
    var ALL_CONTEXTS_STRING = "all contexts";

    /**
     * Property: formPanel
     * {Ext.form.FormPanel} the WMC loading form panel
     */
    var formPanel;

    /**
     * Property: _refreshing
     * {Boolean} true while the view is refreshing 
     * and we don't want to call onViewSelectionChange
     */
    var _refreshing;

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: contextselected
         * Fires when a context has been selected
         */
        "contextselected"
    );

    /**
     * Method: onFailure
     * Called when a context cannot be restored
     *
     * Parameters:
     * msg - {String} the message key to display to the end-user
     *
     */
    var onFailure = function(msg) {
        GEOR.util.errorDialog({
            msg: tr(msg)
        });
    };

    /**
     * Method: fetchAndRestoreWMC
     * Fetch the WMC content and restore it.
     *
     * Parameters:
     * wmcURI - {String} the WMC URI
     * noReset - {Boolean} if true, do not reset the map. 
     *           Defaults to false.
     */
    var fetchAndRestoreWMC = function(wmcURI, noReset) {
        GEOR.waiter.show();
        noReset = noReset || false;
        OpenLayers.Request.GET({
            url: wmcURI,
            success: function(response) {
                var status = observable.fireEvent("contextselected", {
                    wmcString: response.responseXML || response.responseText,
                    noReset: noReset
                });
                if (!status) {
                    onFailure("The provided file is not a valid OGC context");
                }
            },
            failure: onFailure.createCallback("Could not find WMC file")
        });
    };

    /**
     * Method: onDblclick
     * Callback on view node double clicked
     *
     * Parameters:
     * view - {Ext.DataView}
     * index - {Integer} not used internally
     * nodes - {Array}
     * e - {Ext.EventObject} unused
     * noReset - {Boolean} defaults to false
     */
    var onDblclick = function(view, index, node, e, noReset) {
        var record = view.getRecord(node);
        if (record) {
            fetchAndRestoreWMC(
                GEOR.util.getValidURI(record.get("wmc")), noReset
            );
        }
    };

    /**
     * Method: loadBtnHandler
     * Handler for the button triggering the WMC loading
     *
     * Parameters:
     * noReset - {Boolean}
     */
    var loadBtnHandler = function(noReset) {
        var form;
        // we need to check whether to load from view or from form
        if (view.getSelectionCount() === 1) {
            onDblclick(view, null, view.getSelectedNodes()[0], null, noReset);
        } else {
            form = formPanel.getForm();
            if (form.isValid()) {
                form.submit({
                    url: GEOR.config.PATHNAME + "/ws/wmc/",
                    // Beware: form submission requires a *success* parameter in json response
                    // As said in http://extjs.com/learn/Manual:RESTful_Web_Services
                    // "Ext.form.BasicForm hopefully becomes HTTP Status Code aware!"
                    success: function(form, action) {
                        var o = Ext.decode(action.response.responseText);
                        fetchAndRestoreWMC(GEOR.config.PATHNAME + "/" + o.filepath, noReset);
                    },
                    failure: onFailure.createCallback("File submission failed or invalid file"),
                    scope: this
                });
            }
        }
    };

    /**
     * Method: silentCheck
     * set checkbox status silently
     *
     * Parameters:
     * cbx - {Ext.form.Checkbox}
     * checked - {Boolean}
     */
    var silentCheck = function(cbx, checked) {
        cbx.suspendEvents();
        cbx.setValue(checked);
        cbx.resumeEvents();
    };

    /**
     * Method: onViewSelectionChange
     * Callback on DataView selectionchange event
     *
     * Parameters:
     * view - {Ext.DataView}
     * selections - {Array} array of selected nodes
     */
    var onViewSelectionChange = function(view, selections) {
        if (_refreshing) {
            return;
        };
        var fbar = popup.getFooterToolbar(),
            btn1 = fbar.getComponent('add'),
            btn2 = fbar.getComponent('load'),
            cbx = fbar.getComponent('cbx'),
            viewHasSelection = selections.length === 1,
            lsAvailable = GEOR.ls.available,
            cbxChecked;

        btn1.setDisabled(!viewHasSelection);
        btn2.setDisabled(!viewHasSelection);
        if (viewHasSelection) {
            cbxChecked = GEOR.util.getValidURI(view.getSelectedRecords()[0].get("wmc")) === 
                GEOR.ls.get("default_context");
            silentCheck(cbx, cbxChecked);
            cbx.setDisabled(!lsAvailable);
        } else {
            silentCheck(cbx, false);
            cbx.disable();
        }
        formPanel.getForm().reset();
    };

    /**
     * Method: onUploadFormValidation
     * Callback on upload form validation event
     *
     * Parameters:
     * form - {Ext.form.FormPanel}
     * valid - {Boolean} validity of form
     */
    var onUploadFormValidation = function(form, valid) {
        if (valid) {
            var fbar = popup.getFooterToolbar(),
                btn1 = fbar.getComponent('add'),
                btn2 = fbar.getComponent('load'),
                cbx = fbar.getComponent('cbx');

            btn1.enable();
            btn2.enable();
            silentCheck(cbx, false);
            cbx.disable();
            // we suppress event to prevent retroaction on this field
            view.clearSelections(true);
        }
    };

    /**
     * Method: onCbxCheckChange
     * Callback on remember checkbox check event
     *
     * Parameters:
     * cbx - {Ext.form.Checkbox}
     * checked - {Boolean}
     */
    var onCbxCheckChange = function(cbx, checked) {
        var record = view.getSelectedRecords()[0];
        if (checked) {
            // set the currently selected context as default one
            GEOR.ls.set("default_context", 
                GEOR.util.getValidURI(record.get("wmc")));
        } else {
            GEOR.ls.remove("default_context");
        }
        _refreshing = true;
        // to apply the "default" CSS class to the correct node:
        view.refresh();
        // keep selection after refresh:
        view.select(record);
        _refreshing = false;
    };

    /**
     * Method: createPopup
     *
     * Parameters:
     * animateFrom - {String} Id or element from which the window
     *  should animate while opening
     *
     * Returns:
     * {Ext.Window} 
     */
    var createPopup = function(animateFrom) {
        var storeData = GEOR.config.CONTEXTS.slice(0); // array cloning
        var store = new Ext.data.JsonStore({
            fields: ['label', 'thumbnail', 'wmc', 'tip', 'keywords'],
            data: storeData
        });
        view = new Ext.DataView({
            store: store,
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap {[this.isDefault(values)]}" ext:qtip="{[this.tr(values)]}">',
                    '<div class="thumb"><img src="{[this.getThumbnailURI(values)]}" ext:qtip="{[this.tr(values)]}"></div>',
                    '<span>{label}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>', 
            {
                compiled: true,
                disableFormats: true,
                tr: function(v) {
                    var d = this.isDefault(v),
                        out = tr(v.tip);
                    if (d !== "") {
                        out += " " + tr("(" + d + ")");
                    }
                    return out;
                },
                getThumbnailURI: function(v) {
                    return GEOR.util.getValidURI(v.thumbnail);
                },
                isDefault: function(v) {
                    return (GEOR.util.getValidURI(v.wmc) === GEOR.ls.get("default_context")) ? 
                        "default" : "";
                }
            }),
            flex: 1,
            autoScroll: true,
            overClass: 'x-view-over',
            itemSelector: 'div.thumb-wrap',
            singleSelect: true,
            cls: 'context-selector',
            listeners: {
                "selectionchange": onViewSelectionChange,
                "dblclick": onDblclick
            }
        });
        formPanel = new Ext.form.FormPanel({
            fileUpload: true,
            bodyStyle: 'padding:10px',
            labelWidth: 200,
            height: 45,
            monitorValid: true,
            buttonAlign: 'right',
            items: [{
                xtype: 'textfield',
                inputType: 'file',
                name: 'wmc',
                labelSeparator: tr("labelSeparator"),
                fieldLabel: tr("... or a local context"),
                allowBlank: false,
                blankText: tr("The file is required.")
            }],
            listeners: {
                "clientvalidation": onUploadFormValidation
            }
        });
        return new Ext.Window({
            title: tr("Context restoring"),
            layout: 'vbox',
            layoutConfig: {
                align: 'stretch'
            },
            border: true,
            defaults: {
                border: false
            },
            modal: false,
            constrainHeader: true,
            animateTarget: animateFrom,
            width: 4 * 130 + 2 * 10 + 15, // 15 for scrollbar
            height: 450,
            closeAction: 'hide',
            plain: true,
            buttonAlign: 'left',
            fbar: [{
                xtype: 'checkbox',
                itemId: 'cbx',
                disabled: !GEOR.ls.available,
                boxLabel: tr("default viewer context"),
                listeners: {
                    "check": onCbxCheckChange
                }
            },'->', {
                text: tr("Close"),
                handler: function() {
                    popup.hide();
                }
            }, {
                text: tr("Add"),
                disabled: true,
                itemId: 'add',
                minWidth: 90,
                iconCls: 'geor-add-map',
                handler: loadBtnHandler.createCallback(true)
            }, {
                text: tr("Load"),
                disabled: true,
                itemId: 'load',
                minWidth: 90,
                iconCls: 'geor-load-map',
                handler: loadBtnHandler.createCallback(false),
                listeners: {
                    "enable": function(btn) {
                        btn.focus();
                    }
                }
            }],
            items: [{
                xtype: 'form',
                bodyStyle: 'padding:5px',
                labelWidth: 350,
                height: 30,
                items: [{
                    xtype: "combo",
                    width: 150,
                    mode: 'local',
                    forceSelection: true,
                    triggerAction: "all",
                    labelSeparator: '',
                    fieldLabel: tr("Load or add the layers from one of these map contexts:"),
                    editable: false,
                    value: tr(ALL_CONTEXTS_STRING),
                    store: keywords,
                    listeners: {
                        "select": function(cb, record) {
                            var selected = record.get("field1");
                            if (selected == tr(ALL_CONTEXTS_STRING)) {
                                store.clearFilter();
                                return;
                            }
                            store.filterBy(function(r) {
                                return r.get("keywords").indexOf(selected) > -1;
                            });
                        }
                    }
                }]
            }, view, formPanel]
        });
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
         * APIMethod: init
         * 
         */
        init: function() {
            tr = OpenLayers.i18n;
            // create array of unique keywords
            keywords = [];
            var K = {};
            Ext.each(GEOR.config.CONTEXTS, function(c) {
                Ext.each(c.keywords, function(k) {
                    K[k] = true;
                });
            });
            Ext.iterate(K, function(k, v) {
                keywords.push(k);
            });
            keywords.sort(GEOR.util.sortFn);
            keywords = [tr(ALL_CONTEXTS_STRING)].concat(keywords);
        },

        /**
         * APIMethod: show
         * Shows the context selector window.
         */
        show: function() {
            var target = (GEOR.config.ANIMATE_WINDOWS) ? 
                this.el : undefined;
            if (!popup) {
                popup = createPopup(target);
            }
            popup.show(target);
        }
    };
})();
