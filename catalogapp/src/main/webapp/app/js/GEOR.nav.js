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
    
    var handleNavigation = function(store) {
        numberOfRecordsMatched = store.getTotalCount();
        var plural = (numberOfRecordsMatched > 1) ? 's' : '';
        
        var max = isMax() ? numberOfRecordsMatched : parseInt(startPosition + RESULTSPERPAGE - 1);
        
        return "Résultat"+plural+" "+startPosition+" à "+max+" sur "+numberOfRecordsMatched;
        
        /*
        if (numberOfRecordsMatched > 0) {
            $("#catalogForm-header-pdf, #cat-navigation, #catalogForm-header-sort").removeClass("display-none");
        } else {
            $("#catalogForm-header-pdf, #cat-navigation, #catalogForm-header-sort").addClass("display-none");
        }
        
        if (isMax()) {
            $("#catalogForm-header-next").addClass("display-none");
        } else if($("#catalogForm-header-next").hasClass("display-none")) {
            $("#catalogForm-header-next").removeClass("display-none");
        }
        
        if (startPosition == 1) {
            $("#catalogForm-header-prev").addClass("display-none");
        } else if($("#catalogForm-header-prev").hasClass("display-none")) {
            $("#catalogForm-header-prev").removeClass("display-none");
        }
        */
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