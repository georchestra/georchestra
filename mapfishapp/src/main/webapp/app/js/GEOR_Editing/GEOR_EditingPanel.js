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
 * @include GEOR_ows.js
 * @include GEOR_Editing/GEOR_LayerEditingPanel.js
 */

Ext.namespace("GEOR.Editing");

/**
 */
GEOR.Editing.EditingPanel = Ext.extend(Ext.Panel, {

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
     * {GEOR.ows.WFSCapabilities}
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
                GEOR.ows.WFSDescribeFeatureType(
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
        GEOR.Editing.EditingPanel.superclass.initComponent.call(this);  

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
            xtype: "geor_layereditingpanel",
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

Ext.reg("geor_editingpanel", GEOR.Editing.EditingPanel);
