/**
 * empty geOrchestra viewer configuration file
 * see GEOR_custom.js.sample for full explanation
 */

Ext.namespace("GEOR");

GEOR.custom = {
    // update these values to match your setup
    DEFAULT_ATTRIBUTION: 'MyCompany',
    PROJ4JS_STRINGS: {
        "EPSG:2154": "+title=RGF-93/Lambert 93, +proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
        "EPSG:900913": "+title=Web Spherical Mercator, +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs"
    },
    CATALOG_NAME: 'Catalogue'
    // No trailing comma for the last line (or IE will complain)
}
