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
 * @include GEOR_wmc.js
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
     * Property: formPanel
     * {Ext.form.FormPanel} the WMC loading form panel
     */
    var formPanel;

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
     * wmc - {String} the WMC URL
     */
    var fetchAndRestoreWMC = function(wmc) {
        GEOR.waiter.show();
        OpenLayers.Request.GET({
            url: wmc,
            success: function(response) {
                var status = observable.fireEvent("contextselected", {
                    wmcString: response.responseXML || response.responseText
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
     * Called when a context is to be loaded
     *
     * Parameters:
     * view - {Ext.DataView}
     * nodes - {Array}
     */
    var onDblclick = function(view, nodes) {
        var record = view.getRecords(nodes)[0];
        if (record) {
            fetchAndRestoreWMC(record.get('wmc'));
        }
    };

    /**
     * Method: loadBtnHandler
     * Handler for the button triggering the WMC loading
     */
    var loadBtnHandler = function() {
        var form;
        // we need to check whether to load from view or from form
        if (view.getSelectionCount() === 1) {
            onDblclick(view, view.getSelectedNodes());
        } else {
            form = formPanel.getForm();
            if (form.isValid()) {
                form.submit({
                    url: "ws/wmc/",
                    // Beware: form submission requires a *success* parameter in json response
                    // As said in http://extjs.com/learn/Manual:RESTful_Web_Services
                    // "Ext.form.BasicForm hopefully becomes HTTP Status Code aware!"
                    success: function(form, action) {
                        var o = Ext.decode(action.response.responseText);
                        fetchAndRestoreWMC(o.filepath);
                    },
                    failure: onFailure.createCallback("File submission failed or invalid file"),
                    scope: this
                });
            }
        }
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
        var storeData = [
            [GEOR.config.DEFAULT_CONTEXT_LABEL, GEOR.config.DEFAULT_CONTEXT_THUMBNAIL,
            GEOR.config.DEFAULT_WMC, GEOR.config.DEFAULT_CONTEXT_TOOLTIP]
        ];
        var store = new Ext.data.ArrayStore({
            fields: ['label', 'thumbnail', 'wmc', 'tooltip'],
            data: storeData.concat(GEOR.config.CONTEXT_SELECTOR_CONTEXTS)
        });
        view = new Ext.DataView({
            store: store,
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap" ext:qtip="{[this.tr(values)]}">',
                    '<div class="thumb"><img src="{thumbnail}" ext:qtip="{[this.tr(values)]}"></div>',
                    '<span>{label}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>', 
            {
                compiled: true,
                disableFormats: true,
                tr: function(v){
                    return tr(v.tooltip);
                },
            }),
            flex: 1,
            autoScroll: true,
            overClass: 'x-view-over',
            itemSelector: 'div.thumb-wrap',
            singleSelect: true,
            cls: 'context-selector',
            listeners: {
                "selectionchange": function() {
                    var btn = popup.getFooterToolbar().getComponent('load');
                    btn.setDisabled(view.getSelectionCount() === 0);
                    formPanel.getForm().reset();
                },
                "dblclick": function(view, index, node) {
                    onDblclick(view, [node]);
                }
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
                fieldLabel: tr("... or a custom context"),
                allowBlank: false,
                blankText: tr("The file is required.")
            }],
            listeners: {
                "clientvalidation": function(form, valid) {
                    if (valid) {
                        popup.getFooterToolbar().getComponent('load').enable();
                        // we suppress event to prevent retroaction on this field
                        view.clearSelections(true);
                    }
                }
            }
        });
        return new Ext.Window({
            title: tr("Context restoring"),
            layout: 'vbox',
            layoutConfig: {
                align: 'stretch'
            },
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
            buttons: [{
                text: tr("Close"),
                handler: function() {
                    popup.hide();
                }
            }, {
                text: tr("Load"),
                disabled: true,
                itemId: 'load',
                handler: loadBtnHandler
            }],
            items: [{
                xtype: 'box',
                height: 30,
                autoEl: {
                    tag: 'div',
                    cls: 'box-as-panel',
                    html: tr("Replace current map composition with one of these contexts:"),
                }
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
         * APIMethod: show
         * Shows the context selector window.
         */
        show: function() {
            var target = (GEOR.config.ANIMATE_WINDOWS) ? 
                this.el : undefined;
            if (!popup) {
                tr = OpenLayers.i18n;
                popup = createPopup(target);
            }
            popup.show(target);
        }
    };
})();
