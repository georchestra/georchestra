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
            tooltip: "zoom sur l'étendue globale de la carte",
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
            tooltip: "glisser - déplacer la carte",
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
            tooltip: "zoom en avant",
            toggleGroup: "map",
            allowDepress: false
        }));

        items.push("-");

        ctrl = new OpenLayers.Control.NavigationHistory();
        map.addControl(ctrl);
        items.push(new GeoExt.Action({
            control: ctrl.previous,
            iconCls: "back",
            tooltip: "revenir à la précédente emprise",
            disabled: true
        }));
        items.push(new GeoExt.Action({
            control: ctrl.next,
            iconCls: "next",
            tooltip: "aller à l'emprise suivante",
            disabled: true
        }));

        items.push('->');

        // insert a login or logout link in the toolbar
        var login_html = '<div style="margin-right:1em;margin-right:1em;font:11px tahoma,verdana,helvetica;"><a href="' + GEOR.config.LOGIN_URL +
            '" style="text-decoration:none;" onclick="return GEOR.toolbar.confirmLogin()">Connexion</a></div>';
        if(!GEOR.data.anonymous) {
            login_html = '<div style="margin-right:1em;margin-right:1em;font:11px tahoma,verdana,helvetica;">'+GEOR.data.username + '&nbsp;<a href="' + GEOR.config.LOGOUT_URL +
                '" style="text-decoration:none;">déconnexion</a></div>';
        }
        items.push(Ext.DomHelper.append(Ext.getBody(), login_html));

        items.push("-");

        items.push({
            text: "Aide",
            tooltip: "Afficher l'aide",
            handler: function() {
                if(Ext.isIE) {
                    window.open("/doc/html/documentation.html#extractor");
                } else {
                    window.open("/doc/html/documentation.html#extractor", "Aide de l'extrateur", "menubar=no,status=no,scrollbars=yes");
                }
            }
        });

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
            return confirm("Vous allez quitter cette page et perdre le contexte cartographique courant");
            // ou : "Pour vous connecter, nous vous redirigeons vers une autre page web. Vous risquez de perdre le contexte cartographique courant. Vous pouvez le sauvegarder en annulant cette opération, et en cliquant sur Espace de travail > Sauvegarder la carte" ?
        }
    };

})();
