/*
 * Copyright (C) Geobretagne
 *
 * This file is NOT part of geOrchestra (well, not yet)
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

GEOR.addonsmenu = (function () {
    /*
     * Private
     */

    /**
     * Property: map
     * {OpenLayers.Map} The map instance.
     */
    var map = null;

     /**
     * Property: tr
     * {Function} an alias to OpenLayers.i18n
     */
    var tr = null;

    /**
     * Property: addons
     * Array of addon config objects
     */
    var addons = null;

    /**
     * Property: initializes
     * boolean.
     */
    var initialized = false;

    /**
     *Method : getGroupItem
     * this method returns menuItem index corresponding at the label group passed in parameter
     * Parameter: menuaddons : {Ext.Action}, group: string.
     *
    */
    var getGroupItem = function (menuaddons, group) {
        var index = -1;
        var i = 0;
        for (i = 0; i < menuaddons.menu.items.items.length; i += 1) {
            if (menuaddons.menu.items.items[i].text === group) {
                index = i;
                break;
            }
        }
        return index;
    };

    /**
     *Method : loadCssFiles
     * this method loads dynamically the css files passed in parameter
     * this method is used because Ext.Loader does not works with css files
     * Parameter:
     * [filename - css files].
     */
    var loadCssFiles = function (filenames) {
        var i = 0;
        for (i = 0; i < filenames.length; i += 1) {
            var fileref = document.createElement("link");
            fileref.setAttribute("rel", "stylesheet");
            fileref.setAttribute("type", "text/css");
            fileref.setAttribute("href", filenames[i]);
            document.getElementsByTagName("head")[0].appendChild(fileref);
        }

    };

    /**
     *Method : checkRoles
     * this method checks the addon permissions
     * Parameter: okRoles {addonItems.roles}.
     *
     */
    var checkRoles = function (okRoles) {
        // addon is available for everyone if okRoles is empty:
        var ok = (okRoles.length === 0);
        var i = 0;
        // else, check existence of required role to activate addon:
        for (i = 0; i < okRoles.length; i += 1) {
            if (GEOR.config.ROLES.indexOf(okRoles[i]) >= 0) {
                ok = true;
                break;
            }
        }
        return ok;
    };

    /**
     *Method : lazyLoad
     * this method loads dynamically all js and css files registered in
     * addons.js and addons.css using Ext.Loader.
     */
    var lazyLoad = function () {
        if (initialized === false) {
            var libs, i, j;
            libs = [];
            i = 0;
            j = 0;
            for (i = 0; i < addons.length; i += 1) {
                var files = addons[i].js;
                for (j = 0; j < files.length; j += 1) {
                    libs.push(files[j]);
                }
                if (addons[i].css) {
                    loadCssFiles(addons[i].css);
                }

            }
            Ext.Loader.load(libs, function (test) {
                var i = 0;
                var menuaddons = Ext.getCmp('menuaddons'); // éviter le getCmp : lent ! (utiliser une référence interne au présent module)
                for (i = 0; i < addons.length; i += 1) {
                    var addon = addons[i].addon;
                    var addonObject = GEOR[addon];
                    if (addonObject && checkRoles(addons[i].options.roles ? addons[i].options.roles : [])) {
                        if (addons[i].options.group) {
                            var menuGroup = getGroupItem(menuaddons, addons[i].options.group);
                            menuaddons.menu.items.items[menuGroup].menu.addItem(
                                addonObject.create(map, addons[i])
                            );
                        } else {
                            menuaddons.menu.addItem(addonObject.create(map, addons[i]));
                        }
                    }
                }
                menuaddons.menu.remove(menuaddons.menu.items.items[0]);
            }, this, true);
            initialized = true;
        }
    };





    return {
        /*
         * Public
         */

        /**
         * APIMethod: create
         *
         * This API method returns items menu from each addon loaded
         * Parameters:
         * m - {OpenLayers.Map} The map instance.
         */

        create: function (m) {
            if (GEOR.config.ADDONS.length == 0) {
                return null;
            }

            map = m;
            tr = OpenLayers.i18n;
            addons = GEOR.config.ADDONS;

            var groups = {};
            Ext.each(addons, function(addon) {
                if (addon.options && addon.options.group) {
                    groups[addon.options.group] = 1;
                }
            });

            var menuitems = new Ext.Action({
                text: tr("Tools"),
                id: 'menuaddons',
                handler: lazyLoad,
                menu: new Ext.menu.Menu({
                    items: [{
                        text: tr("loading") + "..."
                    }]
                })
            });

            Ext.iterate(groups, function(group) {
                menuitems.initialConfig.menu.addItem({
                    text: group,
                    iconCls: 'geor-save-map',
                    menu: new Ext.menu.Menu({
                        items: []
                    })
                });
            });

            return menuitems;
        }

    };
})();
