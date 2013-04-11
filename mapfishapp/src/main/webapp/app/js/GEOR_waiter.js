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

GEOR.waiter = (function() {
    /*
     * Private
     */
    
    var waiter = null,
        count = 0;
        
    return {
    
        init: function() {
            waiter = Ext.get('waiter');
        },
        
        hide: function() {
            if (count > 0) {
                count -= 1;
            }
            if (waiter && count == 0) {
                waiter.hide();
            }
        },
        
        show: function(c) {
            if (c === 0) {
                return;
            }
            count += (c || 1);
            if (waiter && count > 0) {
                waiter.show();
            }
        }
    };
})();
