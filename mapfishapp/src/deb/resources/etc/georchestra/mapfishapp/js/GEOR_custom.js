Ext.namespace("GEOR");

GEOR.custom = {
    HEADER_HEIGHT: 90,
    GEOSERVER_WFS_URL: "http://geobretagne.fr/geoserver/wfs",

    GEONETWORK_BASE_URL: "/geonetwork",

    CATALOGS: [
        ['http://geobretagne.fr/geonetwork/srv/fre/csw', 'le catalogue GeoBretagne'],
        ['/geonetwork/srv/fre/csw', 'le catalogue local']
    ],

    GEONAMES_FILTERS: {
        username: 'georchestra', // please replace this username by yours !
        // You can create a geonames account here: http://www.geonames.org/login
        // It is then required to enable your account to query the free web services
        // by visiting http://www.geonames.org/manageaccount
        country: 'FR',         // France
        //adminCode1: '97',    // Region
        style: 'short',        // verbosity of results
        lang: 'fr',
        featureClass: 'P',     // class category: populated places
        maxRows: 20            // maximal number of results
    },

    ROLES_FOR_STYLER: [],

    EDITABLE_LAYERS: /.*georchestra.mydomain.org.*/i,

    /**
     * Constant: FORCE_LOGIN_IN_TOOLBAR
     * {Boolean} If true, the login link is always shown in the app toolbar.
     * Defaults to false.
     */
    //FORCE_LOGIN_IN_TOOLBAR: false,

    /**
     * Constant: SEND_MAP_TO
     * {Array} List of menu items configs
     *
     * Each menu item config **must** have the following properties:
     *  - name: the link name. Will be localized by OpenLayers.i18n
     *  - url: the template url for the link. Must contain one of
     *   {context_url}, {map_url} or {id} strings, which will be resp.
     *   replaced by the generated WMC link, the map permalink and the map id.
     *
     * Each menu item config **may** have the following properties:
     *  - qtip: the tip appearing on menu item hover. Will be localized by OpenLayers.i18n
     *  - iconCls: the CSS class which will be appended to the menu item
     *
    SEND_MAP_TO: [{
        "name": "Mobile viewer",
        "url": "http://sdi.georchestra.org/sviewer/?wmc={context_url}",
        "qtip": "Mobile compatible viewer on sdi.georchestra.org"
    }, {
        "name": "Desktop viewer",
        "url": "http://sdi.georchestra.org/mapfishapp/?wmc={context_url}",
        "qtip": "Desktop viewer on sdi.georchestra.org"
    }],*/

    /**
     * Constant: OGC_SERVERS_URL
     * {Object} associates OGC interface names with resource file URLs
     *          (relative to viewer or complete) where the servers are enlisted
     */
    OGC_SERVERS_URL: {
        "WMS": "ws/wms.servers.json",
        "WFS": "ws/wfs.servers.json",
        "WMTS": "ws/wmts.servers.json"
    },

    /**
     * Constant: DEFAULT_SERVICE_TYPE
     * {String} The default service type for the "Add layer" window OGC tab.
     * Defaults to "WMS"
     **/
    DEFAULT_SERVICE_TYPE: "WMS"
    // No trailing comma for the last line (or IE will complain)
}
