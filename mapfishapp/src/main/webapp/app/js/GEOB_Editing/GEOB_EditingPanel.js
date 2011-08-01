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

/*
 * @include GEOB_ows.js
 * @include GEOB_Editing/GEOB_LayerEditingPanel.js
 */

Ext.namespace("GEOB.Editing");

/**
 */
GEOB.Editing.EditingPanel = Ext.extend(Ext.Panel, {

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    map: null,

    /**
     * Property: layerCb 
     * {Ext.form.ComboBox}
     */
    layerCb: null,

    /**
     * Property: store
     * {GEOB.ows.WFSCapabilities}
     */
    store: null,

    /**
     * Property: nsAlias
     * {String} The editing layers' namespace alias.
     */
    nsAlias: null,
    
    /**
     * Property: mask
     * {Ext.LoadMask}
     */
    mask: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {

        this.layerCb = this.createLayerCb();
        this.autoScroll = true;
        this.layout = 'border';
        

        this.items = [{
            xtype: 'form',
            region: 'north',
            labelWidth: 50,
            height: 50,
            bodyStyle: 'padding: 10px;',
            border: false,
            items: [this.layerCb]
        }, {
            layout: 'card',
            region: 'center',
            border: false,
            activeItem: 0,
            items: [{
                border: false,
                html: '<p> </p>'
            }]
        }];
        
        this.on('afterlayout', function(){
            this.mask = new Ext.LoadMask(this.items.get(1).getEl(), { 
                msg: 'Chargement...'
            }); 
        });

        // manage events
        this.layerCb.on('select', function(combo, record, index){
            this.mask.show();
            if (this.items.get(1).layout.activeItem.tearDown) {
                this.items.get(1).layout.activeItem.tearDown();
            }
            if (!this.items.get(1).findById(record.get('name'))) {
                GEOB.ows.WFSDescribeFeatureType(
                    {
                        owsURL: this.store.url,
                        typeName: this.nsAlias + ":" + record.get('name')
                    },
                    {
                        success: function(attributeStore, rec, opts) {
                            var protocol = record.get('layer').protocol;
                            var matchGeomProperty = 
                                /^gml:(Multi)?(Point|LineString|Polygon|Curve|Surface|Geometry)PropertyType$/;
                            // here, we complement the protocol with a valid geometryName
                            // else, "the_geom" is used as default geometryName and this can lead to pbs
                            attributeStore.each(function(record) {
                                if (record.get('type').match(matchGeomProperty)) {
                                    protocol.geometryName = record.get('name');
                                    protocol.format.geometryName = record.get('name');
                                }
                            });
                            
                            this.createLayerEditingPanel(record.get('name'), attributeStore, protocol);
                            this.mask.hide();
                        },
                        scope: this
                    }
                );
            } else {
                
                this.items.get(1).layout.setActiveItem(record.get('name'));
                this.items.get(1).findById(record.get('name')).setUp();
                this.mask.hide();
            }
        }, this);

        // call parent initComponent
        GEOB.Editing.EditingPanel.superclass.initComponent.call(this);  

    },

    /**
     * Method: createLayerEditingPanel
     *
     * Parameters:
     * name - {String}
     * attributeStore - {GeoExt.data.AttributeStore}
     * protocol - {OpenLayers.Protocol.WFS}
     */
    createLayerEditingPanel: function(name, attributeStore, protocol) {
        var panel = this.items.get(1).add({
            xtype: "geob_layereditingpanel",
            map: this.map,
            id: name,
            border: false,
            attributeStore: attributeStore,
            protocol: protocol
        });
        this.items.get(1).layout.setActiveItem(name);
        panel.setUp();
        this.doLayout();
    },

    /*
     * Method: createLayerCb
     *
     * Returns: {Ext.form.ComboBox}
     */ 
    createLayerCb: function() {
        return new Ext.form.ComboBox({
            emptyText: 'choisissez une couche',
            store: this.store,
            valueField: 'name',
            fieldLabel: 'Couche ',
            displayField: 'title',
            editable: false,
            triggerAction: 'all'
        });
    }

});

Ext.reg("geob_editingpanel", GEOB.Editing.EditingPanel);
