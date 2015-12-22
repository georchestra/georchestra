/**
 * Sample geOrchestra catlogapp config file
 *
 * Instructions: 
 * uncomment lines you wish to modify and 
 * modify the corresponding values to suit your needs.
 */

Ext.namespace("GEOR");

GEOR.custom = {
    
    /**
     * Constant: HEADER_HEIGHT
     * Integer value representing the header height, as set in the shared maven filters
     * Defaults to 90
     */
    HEADER_HEIGHT: 90,

    /***** Beginning of config options which can be set in this file *****/

    /**
     * Constant: GEONETWORK_URL
     * The URL to the GeoNetwork server.
     * Defaults to "/geonetwork/srv/fre"
     */
    GEONETWORK_URL: "https://georchestra.mydomain.org/geonetwork/srv/fre",

    /**
     * Constant: VIEWER_URL
     * The URL to Mapfishapp
     * Defaults to "/mapfishapp/"
     */
    VIEWER_URL: "https://georchestra.mydomain.org/mapfishapp/",

    /**
     * Constant: EXTRACTOR_URL
     * The URL to Extractorapp
     * Defaults to "/extractorapp/"
     */
    EXTRACTOR_URL: "https://georchestra.mydomain.org/extractorapp/"

    /**
     * Constant: MAP_DOTS_PER_INCH
     * {Float} Sets the resolution used for scale computation.
     * Defaults to GeoServer defaults, which is 25.4 / 0.28
     */
    //,MAP_DOTS_PER_INCH: 25.4 / 0.28

    // No trailing comma for the last line (or IE will complain)
}
