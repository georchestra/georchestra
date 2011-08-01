/*
 * Copyright (C) Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace("GEOB");

GEOB.waiter = (function() {
    /*
     * Private
     */
    
    var waiter = null;
        
    return {
    
        init: function() {
            waiter = Ext.get('waiter');
        },
        
        hide: function() {
            if (waiter && waiter.isVisible()) {
                waiter.hide();
            }
        },
        
        show: function(c) {
            if (waiter && !waiter.isVisible()) {
                waiter.show();
            }
        }
    };
})();
