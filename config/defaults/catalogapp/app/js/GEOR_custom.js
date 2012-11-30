/**
 * Sample geOrchestra viewer config file
 *
 * Instructions: copy this buffer into GEOR_custom.js,
 * uncomment lines you wish to modify and 
 * modify the corresponding values to suit your needs.
 */

Ext.namespace("GEOR");

GEOR.custom = {

    /**
     * Constant: GEONETWORK_URL
     * The URL to the GeoNetwork server.
     * Defaults to "/geonetwork/srv/fr"
     */
    GEONETWORK_URL: "http://@shared.server.name@:@shared.server.port@/geonetwork/srv/fr",

    /**
     * Constant: VIEWER_URL
     * The URL to Mapfishapp
     * Defaults to "/mapfishapp/"
     */
    VIEWER_URL: "http://@shared.server.name@:@shared.server.port@/mapfishapp/",
        
    /**
     * Constant: EXTRACTOR_URL
     * The URL to Extractorapp
     * Defaults to "/extractorapp/"
     */
    EXTRACTOR_URL: "http://@shared.server.name@:@shared.server.port@/extractorapp/"
    
    /**
     * Constant: MAP_DOTS_PER_INCH
     * {Float} Sets the resolution used for scale computation.
     * Defaults to GeoServer defaults, which is 25.4 / 0.28
     */
    //,MAP_DOTS_PER_INCH: 25.4 / 0.28
    
    // No trailing comma for the last line (or IE will complain)
}
