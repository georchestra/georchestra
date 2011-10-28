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

GEOR.nav = (function() {

    /*
     * Private
     */
    var ok;
    var startPosition = 1;
    var numberOfRecordsMatched;
    var RESULTSPERPAGE = GEOR.config.RESULTS_PER_PAGE;
    
    var isMax = function() {
        return (startPosition == Math.floor((numberOfRecordsMatched - 1)/RESULTSPERPAGE) * RESULTSPERPAGE + 1);
    };
    
    var handleNavigation = function(store, bar) {
        numberOfRecordsMatched = store.getTotalCount();
        var plural = (numberOfRecordsMatched > 1) ? 's' : '';
        var max = isMax() ? numberOfRecordsMatched : parseInt(startPosition + RESULTSPERPAGE - 1);
        
        if (bar.items.length === 0) {
            bar.add({
                text: '<<',
                ref: 'first',
                disabled: true,
                handler: GEOR.nav.begin,
                tooltip: "aller au début des résultats",
                width: 30
            },{
                text: '<',
                ref: 'previous',
                disabled: true,
                handler: GEOR.nav.previousPage,
                tooltip: "page précédente",
                width: 30
            },{
                text: '>',
                ref: 'next',
                handler: GEOR.nav.nextPage,
                tooltip: "page suivante",
                width: 30
            },{
                text: '>>',
                ref: 'end',
                handler: GEOR.nav.end,
                tooltip: "aller à la fin des résultats",
                width: 30
            }, new Ext.Toolbar.TextItem({
                text: '',
                ref: 'navText'
            }), '->', new Ext.Toolbar.TextItem({
                text: '',
                ref: 'selText'
            }));
            bar.ownerCt.doLayout();
        }
        bar.navText.setText("Résultat"+plural+" "+startPosition+" à "+max+" sur "+numberOfRecordsMatched);
        
        // handle buttons activation/deactivation:
        if (startPosition == 1) {
            bar.first.disable();
            bar.previous.disable();
            bar.next.enable();
            bar.end.enable();
        } else if (isMax()) {
            bar.first.enable();
            bar.previous.enable();
            bar.next.disable();
            bar.end.disable();
        } else {
            bar.first.enable();
            bar.previous.enable();
            bar.next.enable();
            bar.end.enable();
        }
    };
    
    /*
     * Public
     */
    return {
        
        getParameters: function(options) {
            return {
                "startPosition": startPosition,
                "maxResults": startPosition + RESULTSPERPAGE - 1
            };
        },
        
        update: handleNavigation,
        
        reset: function() {
            startPosition = 1;
        },
        
        nextPage: function() {
            if (isMax()) {
                return;
            }
            startPosition += RESULTSPERPAGE;
            GEOR.observable.fireEvent("searchrequest");
        },
        
        previousPage: function() {
            if (startPosition == 1) {
                return;
            }
            startPosition -= RESULTSPERPAGE;
            GEOR.observable.fireEvent("searchrequest");
        },
        
        begin: function() {
            if (startPosition == 1) {
                return;
            }
            startPosition = 1;
            GEOR.observable.fireEvent("searchrequest");            
        },
        
        end: function() {
            if (isMax()) {
                return;
            }
            startPosition = Math.floor((numberOfRecordsMatched - 1)/RESULTSPERPAGE) * RESULTSPERPAGE + 1;
            GEOR.observable.fireEvent("searchrequest");
        }
    };
})();