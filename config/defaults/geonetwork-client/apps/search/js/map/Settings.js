OpenLayers.DOTS_PER_INCH=90.71;
OpenLayers.ImgPath="../js/OpenLayers/img/";
OpenLayers.IMAGE_RELOAD_ATTEMPTS=3;
OpenLayers.Util.onImageLoadErrorColor="transparent";
OpenLayers.Lang.setCode(GeoNetwork.defaultLocale);
OpenLayers.Util.onImageLoadError=function(){this._attempts=(this._attempts)?(this._attempts+1):1;
if(this._attempts<=OpenLayers.IMAGE_RELOAD_ATTEMPTS){this.src=this.src
}else{this.style.backgroundColor=OpenLayers.Util.onImageLoadErrorColor;
this.style.display="none"
}};
Proj4js.defs["EPSG:2154"]="+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
GeoNetwork.map.printCapabilities="../../pdf";
GeoNetwork.map.PROJECTION="EPSG:4326";
GeoNetwork.map.EXTENT=new OpenLayers.Bounds(-4, 42, 2, 46);
GeoNetwork.map.BACKGROUND_LAYERS=[new OpenLayers.Layer.WMS("Background layer","/geoserver/wms",{layers:"Fond_GIP",format:"image/jpeg"},{isBaseLayer:true})];
GeoNetwork.map.MAP_OPTIONS={projection:GeoNetwork.map.PROJECTION,maxExtent:GeoNetwork.map.EXTENT,restrictedExtent:GeoNetwork.map.EXTENT,resolutions:GeoNetwork.map.RESOLUTIONS,controls:[]};
GeoNetwork.map.MAIN_MAP_OPTIONS={projection:GeoNetwork.map.PROJECTION,maxExtent:GeoNetwork.map.EXTENT,restrictedExtent:GeoNetwork.map.EXTENT,resolutions:GeoNetwork.map.RESOLUTIONS,controls:[]};