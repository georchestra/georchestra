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
 * @include GEOR_data.js
 * @include GEOR_config.js
 * @include OpenLayers/Control/ZoomToMaxExtent.js
 * @include OpenLayers/Control/ZoomBox.js
 * @include OpenLayers/Control/NavigationHistory.js
 * @include GeoExt/widgets/Action.js
 */

Ext.namespace("GEOR");

GEOR.toolbar = (function() {
    /*
     * Private
     */

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

    /**
     * Method: createTbar
     * Create the toolbar.
     *
     * Parameters:
     * map - {OpenLayers.Map} The application map.
     *
     * Returns:
     * {Array} The toolbar items.
     */
    var createTbar = function(map) {
        var tbar = new Ext.Toolbar();
        var ctrl, items = [];

        ctrl = new OpenLayers.Control.ZoomToMaxExtent();
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            tooltip: tr("Zoom on the global extent of the map"),
            iconCls: "zoomfull"
        }));

        items.push("-");

        // default control is a fake, so that Navigation control
        // is used by default to pan.
        ctrl = new OpenLayers.Control();
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "pan",
            tooltip: tr("Pan"),
            toggleGroup: "map",
            allowDepress: false,
            pressed: true
        }));

        ctrl = new OpenLayers.Control.ZoomBox({
            out: false
        });
        items.push(new GeoExt.Action({
            control: ctrl,
            map: map,
            iconCls: "zoomin",
            tooltip: tr("Zoom in"),
            toggleGroup: "map",
            allowDepress: false
        }));

        items.push("-");

        ctrl = new OpenLayers.Control.NavigationHistory();
        map.addControl(ctrl);
        items.push(new GeoExt.Action({
            control: ctrl.previous,
            iconCls: "back",
            tooltip: tr("Back to the previous extent"),
            disabled: true
        }));
        items.push(new GeoExt.Action({
            control: ctrl.next,
            iconCls: "next",
            tooltip: tr("Go to the next extent"),
            disabled: true
        }));

        items.push('->');
        
        items.push({
            text: tr("Help"),
            tooltip: tr("Show help"),
            handler: function() {
                if(Ext.isIE) {
                    window.open(GEOR.config.HELP_URL);
                } else {
                    window.open(GEOR.config.HELP_URL,
                                tr("Extractor help"),
                                "menubar=no,status=no,scrollbars=yes");
                }
            }
        });
            
        if (GEOR.header === false) {
            items.push("-");
            // insert a login or logout link in the toolbar
            items.push(
                Ext.DomHelper.append(Ext.getBody(), 
                    GEOR.data.anonymous ?
                        '<div class="login"><a href="' + GEOR.config.LOGIN_URL +
                        '" class="nounderline" onclick="return GEOR.toolbar.confirmLogin()">' +
                        tr('Login') +
                        '</a></div>' :
                        '<div class="login">'+GEOR.data.username + 
                        '&nbsp;<a href="' + GEOR.config.LOGOUT_URL +
                        '" class="nounderline">' +
                        tr('Logout') +
                        '</a></div>'
                )
            );
        }
        
        return items;
    };



    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Return the toolbar config.
         *
         * Parameters:
         * map - {OpenLayers.Map} The application map.
         *
         * Returns:
         * {Ext.Toolbar} The toolbar.
         */
        create: function(map) {
            Ext.QuickTips.init();
            return createTbar(map);
        },
        
        /**
         * Method: confirmLogin
         * Displays a confirm dialog before leaving the app for CAS login
         */
        confirmLogin: function() {
            return confirm(tr('toolbar.confirm.login'));
        }
    };

})();
