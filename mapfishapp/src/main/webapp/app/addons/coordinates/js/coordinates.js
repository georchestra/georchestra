Ext.namespace("GEOR.Addons");

GEOR.Addons.coordinates = function (map, options) {
    this.map = map;
    this.options = options;
    this.control = null;
    this.item = null;
    this.layer = null;
    this.action = null;
    this.toolbar = null;
    this.infos = [];    
};

GEOR.Addons.coordinates.prototype = (function () {

    /*
     * Private     */

    var _self = null;
    var _map = null;    
    var _config = null;    
    var _coordinatesLayer = null;    
    var _mask_loader = null;
    
    var _style = {
                externalGraphic: "app/addons/coordinates/img/target.png",
                graphicWidth: 16,
                graphicHeight: 16
                };

    var _styleMap= new OpenLayers.StyleMap({'default': _style, 'temporary': _style});   
    
    var _createDrawControl = function () {
            var drawPointCtrl = new OpenLayers.Control.DrawFeature(_coordinatesLayer, OpenLayers.Handler.Point, {
                featureAdded: function (e) {
                    _onClick(e);
                    drawPointCtrl.deactivate();
                }
            });
           drawPointCtrl.deactivate();
           return drawPointCtrl;
        };
        
    var _onClick = function (feature) {
        /*var url = _config.url;
        var pixel = _map.getPixelFromLonLat(new OpenLayers.LonLat(feature.geometry.x,feature.geometry.y));
        var params = {
            SERVICE: "WMS",
            VERSION: "1.1.1",
            REQUEST: "GetFeatureInfo",
            LAYERS: _config.infoslayers,
            QUERY_LAYERS: _config.infoslayers,
            FEATURE_COUNT: "10",
            STYLES:"",
            BBOX:_map.getExtent().toBBOX(),
            HEIGHT: _map.getCurrentSize().h,
            WIDTH: _map.getCurrentSize().w,
            FORMAT: "image/png",
            INFO_FORMAT: "application/vnd.ogc.gml",
            SRS: _map.getProjection(),
            X: pixel.x,
            Y: pixel.y        
        }        
        _self.infos.push(new GEOR.Addons.coordinatesquery(_map,feature,url,params));*/
        _self.infos.push(new GEOR.Addons.coordinatesquery(_map,feature,_config.services));
        _self.control.deactivate();
    };
    
   
    
    var _activateControl = function () {
        _self.control.activate();
       _self.map.setLayerIndex( _coordinatesLayer, _self.map.layers.length-1);
    };
    
    var _showInfos = function (e) {
        console.log("Coordonnées",e.feature.coordinates);
    }
    

    return {
        /*
         * Public
         */
        activateTool: function() {
            this.action = new Ext.Action({handler: _activateControl,scope:this,iconCls: 'coordinates-icon' });
            this.toolbar  = (_config.placement === "bottom") ? Ext.getCmp("mappanel").bottomToolbar : Ext.getCmp("mappanel").topToolbar;         
            this.toolbar.insert(parseInt(this.options.position),'-');
            this.toolbar.insert(parseInt(this.options.position),this.action);
            this.toolbar.doLayout();
        },
         deactivateTool: function() {
            this.toolbar.remove(this.action.items[0]);
            this.toolbar.remove(this.toolbar.items.items[this.options.position]);
         },        
        onCheckchange: function(item, checked) {
            if (checked) {
               this.activateTool();
            } else {
               this.deactivateTool();
            }
        },     
        

        init: function (record) {
            _self = this;
            var lang = OpenLayers.Lang.getCode();
            title = record.get("title")[lang];
            _map = this.map;            
            _coordinatesLayer = new OpenLayers.Layer.Vector("coordinates", {
                displayInLayerSwitcher: false
                ,styleMap: _styleMap
            });
            this.layer = _coordinatesLayer;
            _config = _self.options;
            
            this.map.addLayers([_coordinatesLayer]);
            this.control = _createDrawControl();           
               
            this.map.addControl(this.control);
            
            var item = new Ext.menu.CheckItem({
                text: title,
                hidden:(this.options.showintoolmenu ===true)? false: true,                
                checked: this.options.autoactivate,
                qtip: record.get("description")[lang],
                listeners: {
                    "checkchange": this.onCheckchange,
                    scope: this
                }
               
            });
            if (this.options.autoactivate === true) { this.activateTool();}            
            this.item = item;
            return item;
        },
        destroy: function () {
            this.map = null;
            this.control.deactivate();
            this.control.destroy();
            this.control = null;
            this.item = null;
            this.layer.destroy();
            Ext.each(this.infos, function(w, i) {w.destroy();});
            this.toolbar.remove(this.action.items[0]);
            this.toolbar.remove(this.toolbar.items.items[this.options.position]);
            this.options = null;
        }
    }
})();