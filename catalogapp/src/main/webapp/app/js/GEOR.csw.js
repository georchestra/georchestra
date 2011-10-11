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

GEOR.csw = (function() {

    var format = null;
    
    var getFilter = function() {
        var f, filters = [], criteria = GEOR.criteria;
        
        for (var i=0, l = criteria.length; i<l; i++) {
            f = GEOR[criteria[i]].getFilter();
            if (f !== null) {
                filters.push(f);
            }
        }
        
        if (filters.length == 1) {
            return filters[0];
        } else if (filters.length >= 1) {
            return new OpenLayers.Filter.Logical({
                type: '&&',
                filters: filters
            });
        }
        return null;
    };

    return {

        getPostData: function(options) {
            format = format || new OpenLayers.Format.CSWGetRecords();
            
            var query = {
                ElementSetName: {
                    value: "full"
                }/*,
                SortBy: // TODO.
                */
            };
            
            var filter = getFilter();
            if (filter) {
                query.Constraint = {
                    version: "1.1.0",
                    Filter: filter
                };
            }
            
            return format.write({
                resultType: "results_with_summary",
                Query: query,
                startPosition: options && options.nav.startPosition || 1,
                maxRecords: options && options.nav.maxResults || 999
                /*,
                outputSchema: options.filter && "http://www.isotc211.org/2005/gmd" // to get ISO19139 XML when a filter is specified (~ getRecordById)
                */
            });
        }

    };
})();
