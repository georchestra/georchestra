Ext.namespace("GEOR.Addons");

GEOR.Addons.Backgrounds = Ext.extend(GEOR.Addons.Base, {
	_layersStore: null,
	_layersCombobox: null,
	_triggerButton: null,
	_currentIndex: null,
	_currentLayer: null,
	
	init: function(record) {
		if (!this.target) {
			alert("Backgrounds addon config error: requires a target property !");
			return;
		}
		
		this._layersStore = this._initLayersStore();
		this._layersCombobox = this._createLayersComboBox(record);
		this.components = this.target.insertButton(this.position, this._layersCombobox);
		this.target.doLayout();
		
		this._triggerButton = new Ext.Button({
			enableToggle: false,
			tooltip: this.getTooltip(record),
			iconCls: "addon-backgrounds"
		});
		this._triggerButton.render(document.getElementById("backgrounds-addon-trigger"));
	},
	
	_initLayersStore: function() {
		var layersGroups = this.options.layers_groups;
		var layerStore = new Ext.data.ArrayStore({fields: ['category','name', 'url','layer'], idIndex: 0});
		var LayerRecord = Ext.data.Record.create([
			{name: 'category', mapping: 'category', allowBlank: false},
			{name: 'name', mapping: 'name', allowBlank: false},
			{name: 'url', mapping: 'url', allowBlank: false},
			{name: 'layer', mapping: 'layer', allowBlank: false}
		]);
		
		layerStore.add(new LayerRecord({
			category: 'reset',
			name: OpenLayers.i18n('backgrounds.reset'),
			url: '',
			layer: ''
		}));
		
		for( var i = 0 ; i < layersGroups.length ; i++ ) {
			var groupName = layersGroups[i].title;
			for( var j = 0 ; j < layersGroups[i].layers.length ; j++ ) {
				var layer = new LayerRecord({
					category: groupName,
					name: layersGroups[i].layers[j].name,
					url: layersGroups[i].layers[j].url,
					layer: layersGroups[i].layers[j].layer
				});
				layerStore.add(layer);
			}
		}
		layerStore.commitChanges();
		
		return layerStore;
	},
	
	_createLayersComboBox: function(record) {
		var template = new Ext.XTemplate(
			'<tpl for=".">',
			'<tpl if="this.shouldShowHeader(category)"><div class="group-header">{[this.showHeader(values.category)]}</div></tpl><div class="x-combo-list-item">{name}</div>',
			'</tpl>',
			{
				currentCategory: null,
				shouldShowHeader: function(category) {
					if( category == 'reset' )
						return false;
					return this.currentCategory != category;
				},
				showHeader: function(category){
					this.currentCategory = category;
					return category;
				}
			}
		);
		
		var comboBox = new Ext.form.ComboBox({
			store: this._layersStore,
			mode: 'local',
			lastQuery: '',
			displayField: 'name',
			listClass: 'grouped-list',
			tpl: template,
			editable: false,
			fieldClass: 'backgrounds-field',
			listWidth: 200,
			triggerConfig: {tag: "div", id: "backgrounds-addon-trigger",cls: "x-btn-text " + this.triggerClass},
			width: 22,
			height: 24,
			autoSelect: false,
			listeners: {
				"select": this._onSelect,
				"beforequery": function(queryEvent) { queryEvent.query = ""; },
				scope: this
			}
		});
		
		return comboBox;
	},
	
	_onSelect: function(combo, record, index) {
		if( index == this._currentIndex ) {
			return;
		}
		
		if( this._currentLayer != null ) {
			this.map.removeLayer(this._currentLayer);
			this._currentLayer = null;
		}
		
		if( record.data.category != 'reset' ) {
			var WMTSStore = new GEOR.ows.WMTSCapabilities({
				mapSRS: this.map.getProjection(),
				storeOptions: {
					url: record.data.url,
					sortInfo: {
						field: 'title',
						direction: 'ASC'
					}
				},
				success: function(resultStore) {
					for( var i = 0 ; i < resultStore.data.items.length ; i++ ) {
						var tempLayer = resultStore.data.items[i];
						if( tempLayer.data.name == record.data.layer ) {
							this.map.addLayer(tempLayer.data.layer);
							this.map.setBaseLayer(tempLayer.data.layer);
							this.map.setLayerIndex(tempLayer.data.layer,1);
							this._currentLayer = tempLayer.data.layer;
							break;
						}
					}
					
				},
				scope: this
			});
		}
	}
});