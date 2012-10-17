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
 * @include OpenLayers/Filter/Comparison.js
 */

Ext.namespace("GEOR");

GEOR.what = (function() {

    var field;

    return {

        getCmp: function() {
            if (field) {
                return;
            }
            field = new Ext.form.TextField({
                label: ' ',
                width: 210,
                emptyText: 'saisissez un ou plusieurs mots clés',
                name: 'what',
                enableKeyEvents: true,
                listeners: {
                    "specialkey": function(field, e){
                        if (e.getKey() == e.ENTER) {
                            GEOR.observable.fireEvent("searchrequest", {
                                reset: true
                            });
                        }
                    }
                }
            });

            return {
                xtype: 'form',
                labelSeparator: ' ',
                labelWidth: 1,
                bodyStyle: 'padding: 20px;',
                items: [field]
            };
        },

        getFilter: function() {
            var v = field && field.getValue();
            if (v) {
                return new OpenLayers.Filter.Comparison({
                    type: "~",
                    // OL (format/filter/1.1.0) has wildCard: "*", singleChar: ".", escapeChar: "!" hardcoded
                    property: "AnyText",
                    value: '*'+v+'*'
                });
            }
            return null;
        },

        reset: function() {
            field && field.reset();
        }
    };
})();
