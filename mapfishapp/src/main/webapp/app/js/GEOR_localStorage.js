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

GEOR.ls = (function() {
    /*
     * Private
     */
    var isAvailable = typeof(localStorage) !== "undefined";

    /*
     * Public
     */
    return {

        /**
         * APIMethod: available
         */
        available: isAvailable,

        /**
         * APIMethod: set
         */
        set: function(key, value) {
            if (isAvailable) {
                try {
                    localStorage.setItem(key, value);
                } catch (e) {
                    if (e.name.toUpperCase() === "QUOTA_EXCEEDED_ERR" || 
                        e.name.toUpperCase() === "NS_ERROR_DOM_QUOTA_REACHED") {

                        alert("localStorage quota exceeded !");
                    } else {
                        alert("localStorage failed to store value ("+e.name+")");
                    }
                }
            }
        },

        /**
         * APIMethod: get
         */
        get: function(key) {
            if (isAvailable) {
                return localStorage.getItem(key);
            }
            return null;
        },

        /**
         * APIMethod: remove
         */
        remove: function(key) {
            if (isAvailable) {
                localStorage.removeItem(key);
            }
        }
    };
})();
