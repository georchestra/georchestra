/*
 * Call generic wms service for GoogleMaps v2
 * John Deck, UC Berkeley
 * Inspiration & Code from:
 *	Mike Williams http://www.econym.demon.co.uk/googlemaps2/ V2 Reference & custommap code
 *	Brian Flood http://www.spatialdatalogic.com/cs/blogs/brian_flood/archive/2005/07/11/39.aspx V1 WMS code
 *	Kyle Mulka http://blog.kylemulka.com/?p=287  V1 WMS code modifications
 *      http://search.cpan.org/src/RRWO/GPS-Lowrance-0.31/lib/Geo/Coordinates/MercatorMeters.pm
 *
 * Modified by Chris Holmes, TOPP to work by default with GeoServer.
 * Modified by Eduin Yesid Carrillo Vega to work with any map name. 
 *
 * Note this only works with gmaps v2.36 and above.  http://johndeck.blogspot.com 
 * has scripts
 * that do the same for older gmaps versions - just change from 54004 to 41001.
 *
 * About:
 * This script provides an implementation of GTileLayer that works with WMS
 * services that provide epsg 41001 (Mercator).  This provides a reasonable
 * accuracy on overlays at most zoom levels.  It switches between Mercator
 * and Lat/Long at the myMercZoomLevel variable, defaulting to MERC_ZOOM_DEFAULT
 * of 5.  It also performs the calculation from a GPoint to the appropriate
 * BBOX to pass the WMS.  The overlays could be more accurate, and if you 
 * figure out a way to make them so please contribute information back to
 * http://geoserver.org/display/GEOSDOC/Google+Maps.  There is much
 * information at: 
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 * 
 * Use:
 * This script is used by creating a new GTileLayer, setting the required
 * and any desired optional variables, and setting the functions here to 
 * override the appropriate GTileLayer ones.   
 * 
 * At the very least you will need:
 * var myTileLayer= new GTileLayer(new GCopyrightCollection(""),1,17);
 *     myTileLayer.myBaseURL='http://yourserver.org/wms?'
 *     myTileLayer.myLayers='myLayerName';
 *     myTileLayer=CustomGetTileUrl
 *
 * After that you can override the format (myFormat), the level at
 * which the zoom switches (myMercZoomLevel), and the style (myStyles)
 * - be sure to put one style for each layer (both are separated by
 * commas).  You can also override the Opacity:
 *     myTileLayer.myOpacity=0.69
 *     myTileLayer.getOpacity=customOpacity
 *
 * Then you can overlay on google maps with something like:
 * var layer=[G_SATELLITE_MAP.getTileLayers()[0],tileCountry];
 * var custommap = new GMapType(layer, G_SATELLITE_MAP.getProjection(), "WMS");
 * var ma+p = new GMap(document.getElementById("map"));
 *     map.addMapType(custommap);
 */

var MAGIC_NUMBER=6378137.0;
var PI=3.14159265358979323846;

// Enable/disable meta tiling
var META_TILING = true;

//Default image format, used if none is specified
var FORMAT_DEFAULT = "image/png";

//EPSG code with the Google projection definition
var EPSG_GOOGLE_CODE = "EPSG:900913"
function dd2MercMetersLng(p_lng) { 
	return MAGIC_NUMBER * p_lng; 
}

function dd2MercMetersLat(p_lat) {
	if (p_lat >= 85) p_lat=85;
	if (p_lat <= -85) p_lat=-85;
	return MAGIC_NUMBER * Math.log(Math.tan(p_lat / 2 + PI / 4));
}

CustomGetTileUrl=function(a,b,c) {
        if (this.myFormat == undefined) {
	    this.myFormat = FORMAT_DEFAULT;
        }

        if (this.myMapname == undefined) {

	    this.myMapname = "map";
        }

	if (typeof(window['this.myStyles'])=="undefined") this.myStyles=""; 
	var lULP = new GPoint(a.x*256.0,(a.y+1)*256.0);
	var lLRP = new GPoint((a.x+1)*256.0,a.y*256.0);
	var lUL = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lULP,b,c);
	var lLR = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lLRP,b,c);
	// set a fixed position as the tiles origin for the on the fly meta tiler (0,0) should be good
	var lLL = G_NORMAL_MAP.getProjection().fromPixelToLatLng(new GPoint(0,0), b, c);
	
	// switch between Mercator and DD if merczoomlevel is set
	eval("var lwz = "+ this.myMapname + ".getZoom()");
	var lBbox= dd2MercMetersLng(lUL.lngRadians())+","+dd2MercMetersLat(lUL.latRadians())+","+dd2MercMetersLng(lLR.lngRadians())+","+dd2MercMetersLat(lLR.latRadians());
	var lSRS= EPSG_GOOGLE_CODE;
	var lLLx = dd2MercMetersLng(0)
	var lLLy = dd2MercMetersLat(0)

	var lURL=this.myBaseURL;
	lURL+="&REQUEST=GetMap";
	lURL+="&SERVICE=WMS";
	lURL+="&VERSION=1.1.1";
	lURL+="&LAYERS="+this.myLayers;
	if (this.mySLD == null || this.mySLD == '') {
        lURL+="&STYLES="+this.myStyles;
    }
    if (this.mySLD != null && this.mySLD != '') {
        lURL+="&SLD="+this.mySLD;
    }
    if (this.myCQL != null && this.myCQL != '') {
        lURL+="&CQL_FILTER="+this.myCql;
	}
	if (this.myFilter != null && this.myFilter != '') {
        lURL+="&FILTER="+this.myFilter;
	}
	if (this.myFeatureIds != null && this.myFeatureIds != '') {
        lURL+="&FEATUREID="+this.myFeatureIds;
	}
    lURL+="&FORMAT="+this.myFormat;
	lURL+="&BGCOLOR=0xFFFFFF";
	lURL+="&TRANSPARENT=TRUE";
	lURL+="&SRS="+lSRS;
	lURL+="&BBOX="+lBbox;
	lURL+="&WIDTH=256";
	lURL+="&HEIGHT=256";
	lURL+="&reaspect=false";
	if(META_TILING == true) {
	  lURL+="&tiled=true";
	  lURL+="&tilesOrigin=" + lLLx + "," + lLLy;
	}
//document.write(lURL + "<br/>")        
//alert(" url is " + lURL);
	return lURL;
}

function customOpacity() {
   return this.myOpacity;
}
