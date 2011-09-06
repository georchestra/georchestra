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
     * Constant: DEFAULT_WMC.
     * The path to the application's default WMC.
     * Defaults to "default.wmc"
     */
    //DEFAULT_WMC: "default.wmc",
    
    /**
     * Constant: GEOSERVER_WFS_URL
     * The URL to GeoServer WFS.
     * This is required if and only if the edit application is used
     * or if the "referentials" module is activated.
     * Defaults to /geoserver/wfs
     */
    //GEOSERVER_WFS_URL: "/geoserver/wfs",

    /**
     * Constant: GEOSERVER_WMS_URL
     * The URL to the GeoServer WMS. 
     * This is required if and only if OSM_AS_OVMAP is set to false.
     * Defaults to /geoserver/wms
     */
    //GEOSERVER_WMS_URL: "/geoserver/wms",

    /**
     * Constant: GEONETWORK_URL
     * The URL to the GeoNetwork server.
     * Defaults to "/geonetwork/srv/fr"
     */
    //GEONETWORK_URL: "/geonetwork/srv/fr",

    /**
     * Constant: CSW_GETDOMAIN_SORTING
     * true to case insensitive sort (client side) the keywords 
     * got from a CSW getDomain request. false to disable 
     * client side sorting 
     * (which is preferable in case of too many keywords).
     * Defaults to false
     */
    //CSW_GETDOMAIN_SORTING: false,
    
    /**
     * Constant: CATALOG_NAME
     * The name to display in the catalog tab under "add layer"
     * (was: GeoCatalogue for the GeoBretagne project).
     * Defaults to 'Catalogue geOrchestra'
     */
    CATALOG_NAME: 'Catalogue PIGMA',
    
    /**
     * Constant: THESAURUS_NAME
     * Thesaurus name to display for the CSW GetDomain request.
     * Defaults to 'mots clés'
     */
    //THESAURUS_NAME: 'mots clés',
    
    /**
     * Constant: DEFAULT_THESAURUS_KEY
     * Key (as the one in the response from /geonetwork/srv/fr/xml.thesaurus.getList) 
     * of the thesaurus to use as the default (selected) one.
     */
    DEFAULT_THESAURUS_KEY: 'local._none_.pigma',
        
    /**
     * Constant: MAX_FEATURES
     * The maximum number of vector features displayed.
     * Defaults to a value estimated by an empirical formula
     */
    //MAX_FEATURES: 500,
    
    /**
     * Constant: MAX_LENGTH
     * The maximum number of chars in a XML response 
     * before triggering an alert.
     * Defaults to a value estimated by an empirical formula
     */
    //MAX_LENGTH: 500000,

    
    /**
     * Constant: DEFAULT_ATTRIBUTION
     * Default attribution for layers which don't have one.
     * Defaults to ''
     */
    DEFAULT_ATTRIBUTION: 'PIGMA',

    /**
     * Constant: OSM_AS_OVMAP
     * Boolean: if true, use OSM mapnik as overview map baselayer 
     * instead of GEOR.config.OVMAP_LAYER_NAME.
     * Defaults to true
     */
    //OSM_AS_OVMAP: true,
    
    /**
     * Constant: OVMAP_LAYER_NAME
     * The name of the base layer which will be displayed in the overview map.
     * This is required if and only if OSM_AS_OVMAP is set to false.
     * This layer must be served by the server GEOSERVER_WMS_URL as image/png
     * Defaults to "geor_loc:DEPARTEMENTS"
     */
    //OVMAP_LAYER_NAME: "geor_loc:DEPARTEMENTS",
    
    /**
     * Constant: WMSC2WMS
     * Hash allowing correspondance between WMS-C server URLs and WMS server URLs for print
     * This assumes that layers share the same name on both servers
     * Eventually, Administrator can setup a mirror WMS server configured to consume WMS-C and serve them as WMS ...
     *
     * Example usage:
     *
     * "wmsc_url": "wms_url",
     *
     * For a WMSC with no WMS counterpart,
     * referencing the wmsc_url here allows the user 
     * to be warned that this layer will not be printed:
     *
     * "wmsc_url": undefined,
     */
    /*WMSC2WMS: {
        "http://osm.geobretagne.fr/service/wms": 
            "http://maps.qualitystreetmap.org/geob_wms",
        "http://geobretagne.fr/geoserver/gwc/service/wms": 
            undefined // no trailing comma
    },*/


    /**
     * Constant: MAP_DOTS_PER_INCH
     * {Float} Sets the resolution used for scale computation.
     * Defaults to GeoServer defaults, which is 25.4 / 0.28
     */
    //MAP_DOTS_PER_INCH: 25.4 / 0.28,


    /**
     * Constant: ADDRESS_URL
     * {String} The URL to the OpenAddresses web service.
     * Defaults to "/addrapp/addresses"
     */
    //ADDRESS_URL: "/addrapp/addresses",


    /**
     * Constant: NS_EDIT
     * {String} The editing layers' namespace alias as defined in
     *    the GeoServer configuration.
     * Defaults to "geor_edit"
     */
    NS_EDIT: "pigma_edit",


    /**
     * Constant: CSW_GETDOMAIN_PROPERTY
     * {String} the property used to query the CSW for keywords.
     * Defaults to "subject"
     */
    //CSW_GETDOMAIN_PROPERTY: "subject",


    /**
     * Constant: MAP_SCALES
     * {Array} The map's scales.
     * Defaults to GeoBretagne GWC compliant scales
     */
    /*MAP_SCALES : [
        266.591197934,
        533.182395867,
        1066.364791734,
        2132.729583468,
        4265.459166936,
        8530.918333871,
        17061.836667742,
        34123.673335484,
        68247.346670968,
        136494.693341936,
        272989.386683873,
        545978.773367746,
        1091957.546735491,
        2183915.093470982,
        4367830.186941965,
        8735660.373883929
    ],*/

    /**
     * Constant: MAP_SRS
     * {String} The default map SRS code.
     * Defaults to EPSG:2154
     */
    //MAP_SRS: "EPSG:2154",

    /**
     * Constant: MAP_XMIN aka "left"
     * {Float} The max extent xmin in MAP_SRS coordinates.
     * Defaults to -357823 (France metropolitaine left)
     */
    //MAP_XMIN: -357823,

    /**
     * Constant: MAP_YMIN aka "bottom"
     * {Float} The max extent ymin in MAP_SRS coordinates.
     * Defaults to 6037008 (France metropolitaine bottom)
     */
    //MAP_YMIN: 6037008,

    /**
     * Constant: MAP_XMAX aka "right"
     * {Float} The max extent xmax in MAP_SRS coordinates.
     * Defaults to 1313632 (France metropolitaine right)
     */
    //MAP_XMAX: 1313632,

    /**
     * Constant: MAP_YSMAX aka "top"
     * {Float} The max extent ymax in MAP_SRS coordinates
     * Defaults to 7230727 (France metropolitaine top)
     */
    //MAP_YMAX: 7230727,
    
    /**
     * Constant: MAP_POS_SRS1
     * {String} The cursor position will be displayed using this SRS.
     * Defaults to "EPSG:2154"
     */
    //MAP_POS_SRS1: "EPSG:2154",
    
    /**
     * Constant: MAP_POS_SRS2
     * {String} The cursor position will be displayed using this SRS.
     * Defaults to "EPSG:3948"
     */
    //MAP_POS_SRS2: "EPSG:3948",
    
    /**
     * Constant: TILE_SINGLE
     * {Boolean} When false, activates WMS tiled requests.
     * Defaults to false
     */
    //TILE_SINGLE: false,
    
    /**
     * Constant: TILE_WIDTH
     * {Integer} Width of the WMS tiles in pixels.
     * Defaults to 512
     */
    //TILE_WIDTH: 512,
    
    /**
     * Constant: TILE_HEIGHT
     * {Integer} Height of the WMS tiles in pixels.
     * Defaults to 512
     */
    //TILE_HEIGHT: 512,

    /**
     * Constant: GEONAMES_FILTERS
     * {Object} Describes the geonames options.
     * Defaults to France/Bretagne/populated places
     */
    GEONAMES_FILTERS: {
        country: 'FR',         // France
        adminCode1: '97',      // Aquitaine
        style: 'short',        // verbosity of results
        lang: 'fr',
        featureClass: 'P',     // class category: populated places
        maxRows: 20            // maximal number of results
    },

    /**
     * Constant: GEONAMES_ZOOMLEVEL
     * {Integer} The number of zoom levels from maximum zoom level
     *           to zoom to, when using GeoNames recentering
     * Should always be >= 1.
     * Defaults to 5
     */
    //GEONAMES_ZOOMLEVEL: 5,
    
    /**
     * Constant: ROLES_FOR_STYLER
     * {Array} roles required for the styler to show up
     * Empty array means the module is available for everyone
     * ROLE_SV_USER means the user needs to be connected.
     * Defaults to ['ROLE_SV_USER']
     */
    //ROLES_FOR_STYLER: ['ROLE_SV_USER'],
    
    /**
     * Constant: ROLES_FOR_QUERIER
     * {Array} roles required for the querier to show up
     * Empty array means the module is available for everyone
     * ROLE_SV_USER means the user needs to be connected.
     * Defaults to []
     */
    //ROLES_FOR_QUERIER: [],
    
    /**
     * Constant: ROLES_FOR_PRINTER
     * {Array} roles required to be able to print
     * Empty array means printing is available for everyone
     * ROLE_SV_USER means the user needs to be connected.
     * Defaults to ['ROLE_SV_USER']
     */
    //ROLES_FOR_PRINTER: ['ROLE_SV_USER'],
    
    /**
     * Constant: HELP_URL
     * {String} URL of the help ressource.
     * Defaults to "/doc/html/documentation.html#viewer"
     */
    //HELP_URL: "/doc/html/documentation.html#viewer",
    
    /**
     * Constant: WMS_SERVERS
     * {Array} List of externals WMS to display in the WMS servers tab.
     */
    WMS_SERVERS: [
        {"name": "PIGMA", "url": "http://ns383241.ovh.net/geoserver/wms"},
        {"name": "Sandre/zonages", "url": "http://services.sandre.eaufrance.fr/geo/zonage"},
        {"name": "Sandre/ouvrages", "url": "http://services.sandre.eaufrance.fr/geo/ouvrage"},
        {"name": "Sandre/stations", "url": "http://services.sandre.eaufrance.fr/geo/stations"},
        {"name": "BRGM/géologie", "url": "http://geoservices.brgm.fr/geologie"},
        {"name": "BRGM/risques", "url": "http://geoservices.brgm.fr/risques"},
        {"name": "Cartorisque33, risques naturels", "url": "http://cartorisque.prim.net/wms/33"},
        {"name": "Cartorisque24, risques naturels", "url": "http://cartorisque.prim.net/wms/24"},
        {"name": "Cartorisque47, risques naturels", "url": "http://cartorisque.prim.net/wms/47"},
        {"name": "Cartorisque40, risques naturels", "url": "http://cartorisque.prim.net/wms/40"},
        {"name": "Cartorisque64, risques naturels", "url": "http://cartorisque.prim.net/wms/64"},
        {"name": "Carmen", "url": "http://ws.carmen.application.developpement-durable.gouv.fr/WFS/10/Nature_Paysage"},
        {"name": "GeoSignal", "url": "http://www.geosignal.org/cgi-bin/wmsmap"},
        {"name": "Corine Land Cover", "url": "http://sd1878-2.sivit.org/geoserver/wms"},
        {"name": "GeoLittoral", "url": "http://geolittoral.application.equipement.gouv.fr/wms/metropole"},
        {"name": "Gest'Eau", "url": "http://gesteau.oieau.fr/service"},
        {"name": "IFREMER/littoral", "url": "http://www.ifremer.fr/services/wms1"},
        {"name": "Cartelie/CETE Ouest", "url": "http://mapserveur.application.developpement-durable.gouv.fr/map/mapserv?map%3D%2Fopt%2Fdata%2Fcarto%2Fcartelie%2Fprod%2FCETE_Ouest%2Fxdtyr36laj.www.map"}
    ],
    
    /**
     * Constant: WFS_SERVERS
     * {Array} List of externals WFS to display in the WFS servers tab.
     */
    WFS_SERVERS: [
        {"name": "PIGMA", "url": "http://ns383241.ovh.net/geoserver/wfs"},
        {"name": "Corine Land Cover", "url": "http://sd1878-2.sivit.org/geoserver/wfs"}
    ]
    
    // No trailing comma for the last line (or IE will complain)
}
