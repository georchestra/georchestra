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
 */

Ext.namespace("GEOR");

GEOR.themeselector = (function() {
    /*
     * Private
     */

    /**
     * Property: tpl
     * {Ext.XTemplate}
     */
    var tpl;

    /**
     * Property: store
     * {Ext.data.ArrayStore}
     */
    var store;

    /**
     * Property: view
     * {Ext.DataView}
     */
    var view;

    /**
     * Property: button
     * {Ext.Button}
     */
    var button;

    /**
     * Property: selectedId
     * {String} 
     */
    var selectedId = "geor-theme-default";

    /**
     * Property: buttonPrefix
     * {String} 
     */
    var buttonPrefix;

    /**
     * Property: observable
     * {Ext.util.Obervable}
     */
    var observable = new Ext.util.Observable();
    observable.addEvents(
        /**
         * Event: themeselected
         * Fires when a theme has been selected
         */
        "themeselected"
    );

    /**
     * Method: onFailure
     * Called when theme cannot be restored
     *
     * Parameters:
     * msg - {String} the message key to display to the end-user
     *
     */
    var onFailure = function(msg) {
        // restore previous button label:
        button.setText(buttonPrefix + tr(store.getById(selectedId).get('label'))); 
        // restore previous selection too:
        view.select(selectedId, false, true);
        // warn user:
        GEOR.util.errorDialog({
            msg: tr(msg)
        });
    };

    /**
     * Method: onThemeSelected
     * Called when a theme has been chosen
     *
     * Parameters:
     * view - {Ext.DataView}
     * nodes - {Array}
     */
    var onThemeSelected = function(view, nodes) {
        button.hideMenu();
        var record = view.getRecords(nodes)[0];
        if (record && record.id !== selectedId) {
            // to give an immediate visual feedback for the end-user:
            button.setText(buttonPrefix + tr(record.get('label')));
            GEOR.waiter.show();
            // fetch WMC file:
            OpenLayers.Request.GET({
                url: record.get('wmc'),
                success: function(response) {
                    var status = observable.fireEvent("themeselected", {
                        record: record,
                        wmcString: response.responseXML || response.responseText
                    });
                    // if a listener returns false, it means something went wrong
                    // we won't switch WMC
                    if (status) {
                        selectedId = record.get('id');
                    } else {
                        onFailure("Impossible to restore selected context");
                    }
                },
                failure: onFailure.createCallback("Could not find WMC file")
            });
        }
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
         * APIMethod: create
         * Returns the theme selector button.
         */
        create: function() {
            tr = OpenLayers.i18n;
            buttonPrefix = tr("Theme: ");

            tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap" id="{id}">',
                    '<div class="thumb"><img src="{thumbnail}"></div>',
                    '<span>{label}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>'
            );

            var storeData = [
                ["geor-theme-default", GEOR.config.DEFAULT_THEME_LABEL, 
                GEOR.config.DEFAULT_THEME_THUMBNAIL, GEOR.config.DEFAULT_WMC]
            ];
            store = new Ext.data.ArrayStore({
                fields: ['id', 'label', 'thumbnail', 'wmc'],
                idIndex: 0,
                data: storeData.concat(GEOR.config.THEME_SELECTOR_THEMES)
            });

            view = new Ext.DataView({
                store: store,
                tpl: tpl,
                overClass: 'x-view-over',
                itemSelector: 'div.thumb-wrap',
                singleSelect: true,
                width: GEOR.config.THEME_SELECTOR_COLUMNS * 140,
                cls: 'theme-selector',
                listeners: {
                    "selectionchange": onThemeSelected,
                    "afterrender": function(view) {
                        view.select(selectedId, false, true);
                    }
                }
            });
 
            button = new Ext.Button({
                text: buttonPrefix + tr(store.getById(selectedId).get('label')),
                cls: "themes",
                iconCls: 'themes',
                scale: 'large',
                width: '100%',
                menu: [{
                    xtype: 'container',
                    items: [view]
                }]
            });
            return button;
        }
    };
})();
