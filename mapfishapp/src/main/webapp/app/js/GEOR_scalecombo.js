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
 * @include GeoExt/data/ScaleStore.js
 */

Ext.namespace("GEOR");

GEOR.scalecombo = (function() {
     /*
      * Private
      */

    /**
     * Constant: cmp_id
     * The base id
     */
    var cmp_id = "GEOR_scalecombo";

     /*
      * Public
      */
    return {
        /**
         * APIMethod: create
         * Get the scale combo config.
         *
         * Parameters:
         * map - {OpenLayers.Map} 
         */
        create: function(map) {
            var store = new GeoExt.data.ScaleStore({
                map: map    
            });

            // the combo config that we'll return
            var comboCfg = {
                id: cmp_id+'_scale_combo',
                xtype: "combo",
                store: store,
                editable: false,
                tpl: [
                    '<tpl for=".">',
                    '<div class="x-combo-list-item">',
                    '1 : {[OpenLayers.Number.format(values.scale, 0)]}',
                    '</div>',
                    '</tpl>'
                ].join(''),
                triggerAction: 'all',
                mode: 'local',
                listeners: {
                    select: function(combo, record, index) {
                        map.zoomTo(record.data.level);
                    }
                }
            };

            // register a zoomend listener on the map to update
            // the combo value
            map.events.register('zoomend', this, function() {
                var recs = store.queryBy(function(r) {
                    return r.data.level == this.map.getZoom();
                });
                // on the first zoomend the scale store has no
                // records, this is most probably because the
                // map has no baselayer
                if (recs.getCount() > 0) {
                    Ext.getCmp(cmp_id+'_scale_combo').setValue(
                        "1 : " + OpenLayers.Number.format(recs.first().get("scale"), 0)
                    );
                }
            });

            return comboCfg;
        }
    };
})();
