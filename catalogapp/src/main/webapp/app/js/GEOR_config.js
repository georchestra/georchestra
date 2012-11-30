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

GEOR.config = (function() {


    /**
     * Method: getCustomParameter
     *  If parameter paramName exists in GEOR.custom, returns its value
     *  else defaults to the mandatory defaultValue
     *
     * Parameters:
     * paramName - {String} the parameter name
     * defaultValue - {Mixed} the default value if none is
     *                specified in GEOR.custom
     *
     * Returns:
     * {Mixed} The parameter value
     */
    var getCustomParameter = function(paramName, defaultValue) {
        return (GEOR.custom && GEOR.custom.hasOwnProperty(paramName)) ?
            GEOR.custom[paramName] : defaultValue;
    };

    return {

        /**
         * Constant: GEONETWORK_URL
         * The URL to the GeoNetwork server.
         * Defaults to "/geonetwork/srv/fr"
         */
        GEONETWORK_URL: getCustomParameter('GEONETWORK_URL',
            "/geonetwork/srv/fr"),

        /**
         * Constant: VIEWER_URL
         * The URL to Mapfishapp
         * Defaults to "/mapfishapp/"
         */
        VIEWER_URL: getCustomParameter('VIEWER_URL',
            "/mapfishapp/"),

        /**
         * Constant: EXTRACTOR_URL
         * The URL to Extractorapp
         * Defaults to "/extractorapp/"
         */
        EXTRACTOR_URL: getCustomParameter('EXTRACTOR_URL',
            "/extractorapp/"),

        /**
         * Constant: MAP_DOTS_PER_INCH
         * {Float} Sets the resolution used for scale computation.
         * Defaults to GeoServer defaults, which is 25.4 / 0.28
         */
        MAP_DOTS_PER_INCH: getCustomParameter('MAP_DOTS_PER_INCH',
            25.4 / 0.28),

        RESULTS_PER_PAGE: getCustomParameter('RESULTS_PER_PAGE',
            20)
    };
})();
