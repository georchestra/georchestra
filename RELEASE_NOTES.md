The development branch is master. It can be used for testing and reporting errors.

For production systems, you are advised to use the stable branch (currently 13.09).
This branch receives bug fixes as they arrive, during 6 months at least.


Version 13.12 (development version)
====================================

New features:
 * mapfishapp: multi-layer querier tool - see [#435](https://github.com/georchestra/georchestra/pull/435)
 * mapfishapp: Extractor addon - see the [README](mapfishapp/src/main/webapp/app/addons/extractor/README.md)
 * mapfishapp: OpenLS addon - see the [README](mapfishapp/src/main/webapp/app/addons/openls/README.md)
 * mapfishapp editor revamped - read [this](mapfishapp/README.md#feature-editor)
 * GeoFence integration

Enhancements:
 * analytics: translated to ES, thanks to GeoBolivia !
 * doc: improved installation instructions for gdal native libs
 * js minification: test that jsbuild is working, rather than only testing if venv exists
 * ldapadmin: in the mail sent to the moderator, the "from" field is set to the user email - see [#380](https://github.com/georchestra/georchestra/pull/380)
 * ldapadmin: improved description fields - see #400
 * ldapadmin: added ability to configure fields in users list - read the [how-to](ldapadmin/README.md#configure-the-look-of-the-users-list)
 * ldapadmin: in case of duplicated email error, the message is more explicit
 * ldapadmin: add validation for four optional fields, depending on the config
 * mapfishapp: WMS DescribeLayer on each WMS layer - see [#401](https://github.com/georchestra/georchestra/pull/401)
 * mapfishapp: new layer menu item to set layer as baselayer/overlay - see [#445](https://github.com/georchestra/georchestra/pull/445)
 * mapfishapp: preserve the full attribution information on context save/restore - see [#422](https://github.com/georchestra/georchestra/pull/422)
 * mapfishapp: cswquerier: better results count
 * mapfishapp: wms layer tab: red crosses removed, icons centered
 * mapfishapp: backbuffer hidden behind baselayer for non-opaque baselayers - see [#411](https://github.com/georchestra/georchestra/pull/411)
 * mapfishapp: allow to POST the url of a WFS service or layer - see [#392](https://github.com/georchestra/georchestra/pull/392)
 * mapfishapp: baselayers have a different color in the layer manager
 * proxy: new filter to make basic auth challenge if https and matches user-agent - read the [notes](https://github.com/georchestra/georchestra/commit/8828a11ffb0cb716ad0a6bb1f847ce24328ea450)
 * static: module is now called "header"

Bug fixes:
 * analytics: fixed wrong password for jdbc postgresql
 * geoserver: fixed "inspire extension not deployed"
 * header: fixed IE8 compatibility + header frameborder size set to 0
 * ldapadmin: fixed incorrect generation of login - see [#344](https://github.com/georchestra/georchestra/pull/344)
 * ldapadmin: remove user from groups after deleting the user - see [#406](https://github.com/georchestra/georchestra/pull/406)
 * ldapadmin: prevent race condition when opening /#groups/:group directly.
 * ldapadmin: fixed erroneous admin count - see [#405](https://github.com/georchestra/georchestra/pull/405)
 * mapfishapp: fixed incorrect styles ordering
 * mapfishapp: annotation addon: fixed label size
 * mapfishapp: fix for vector features incompletely displayed - see [#367](https://github.com/georchestra/georchestra/pull/367)
 * mapfishapp: fixing buggy legend makes the print fail - see [#362](https://github.com/georchestra/georchestra/pull/362)
 * mapfishapp: window.onbeforeunload should not return null (IE popup)
 * mapfishapp: fixed "too many features" message in referentials search
 * mapfishapp: fixed gfi feature reprojection in IE
 * mapfishapp: always compute data model on getfeatureinfo
 * mapfishapp: Try to find a text/html metadataURL entry
 * mapfishapp: fixed WFS2 capabilities - see [#373](https://github.com/georchestra/georchestra/pull/373)
 * mapfishapp: fixed JPEG layers in WMC loaded as PNG - see [#370](https://github.com/georchestra/georchestra/pull/370)
 * mapfishapp: connection link in toolbar when header height = 0
 * mapfishapp: referential recenter : fix for old fashioned namespace usage
 * mapfishapp: fixed missing dependency to WFSCapabilitiesReader
 * mapfishapp: annotation addon: fixed calling createUrlObject with an object parameter - see [#437](https://github.com/georchestra/georchestra/pull/437)
 * mapfishapp: in layerfinder, fixed incorrect reference to OpenLayers.i18n method
 * mapfishapp: file upload - the limit is 8MB, not 8GB
 * mapfishapp: file upload - better handling of server-side errors
 * mapfishapp: fixed extra comma (IE) in GEOR_print
 * mapfishapp: fixed wrong popup anchor position on edit - see [#456](https://github.com/georchestra/georchestra/pull/456)
 * mapfishapp: annotations: fixed popup anchor - see [#366](https://github.com/georchestra/georchestra/pull/366)
 * mapfishapp: fixed potentially incorrect metadata url - see [#454](https://github.com/georchestra/georchestra/pull/454)
 * mapfishapp: less strict filtering of suitable images for thumbnail display in CSW querier
 * mapfishapp: fixed missing dependency to WKT format - see [#482](https://github.com/georchestra/georchestra/pull/482)
 * mapfishapp: fixed incorrect maxScaleDenominator on WMC restored - see [#431](https://github.com/georchestra/georchestra/pull/431)
 * mapfishapp: attribution logo correctly sized - see [#490](https://github.com/georchestra/georchestra/pull/490)
 * mapfishapp: fixed wrong proxy selected when the webapp name does not contain private - see [#509](https://github.com/georchestra/georchestra/pull/509)
 * mapfishapp: fixed various problems in legend printing
 * mapfishapp: set a white background to the overview map in the printed PDF - see [#372](https://github.com/georchestra/georchestra/pull/372)
 * proxy: fixed charset detection in ArcGIS server responses - see [#498](https://github.com/georchestra/georchestra/pull/498)
 * proxy: removed sec-* headers from client request - see [#154](https://github.com/georchestra/georchestra/pull/154)
 * header: maintains existing URI parameters when adding the "login" param

UPGRADING:
 * mapfishapp:
   * NS_EDIT config option has been removed. All layers served by the platform geoserver are editable, provided the user has the rights to.
 * extractorapp:
   * BUFFER_VALUES has changed. If you had a custom value in your GEOR_custom.js file, you have to modify it according to the new syntax.
 * geoserver: be sure to set the file.encoding tomcat option for geoserver to interpret correctly UTF-8 SLDs.
 * removed the geobretagne_production env variable - see [#97](https://github.com/georchestra/georchestra/pull/97)
 * analytics: the ExtJS submodule path has changed, be sure to run ```git submodule update --init``` when you switch branches.
 * static/header: as "static" module has been renamed "header", your deployment scripts *must* be adapted, as well as your apache2 configuration (or any other reverse proxy).
 * databases: the downloadform, ogcstatistics and ldapadmin databases are now merged into a single one named "georchestra". Each webapp expects to find its tables in a dedicated schema ("downloadform" for the downloadform module, "ogcstatistics" for ogc-server-statistics, and "ldapadmin" for ldapadmin). See https://github.com/georchestra/georchestra/pull/535 for the complete patch. If you currently have one dedicated database for each module, you can keep your setup, provided you customize the ```shared.psql.ogc.statistics.db``` and ```shared.psql.download_form.db``` maven filters in your own config. In any case, you'll have to rename the ```download``` schema (of the previous ```downloadform``` database) into ```downloadform```, and migrate the tables which were in the public schema of the databases ```ogcstatistics``` and ```ldapadmin``` into the newly created schemas.

Version 13.09 (current stable version)
=======================================

This major release was supported by the GeoPicardie, PIGMA, CIGALsace and GeoBretagne projects. 
The CRAIG (Centre Régional Auvergnat de l'Information Géographique) is also to be thanked for some nice patches.

New features:
 * mapfishapp: [annotation addon](https://github.com/georchestra/georchestra/tree/master/mapfishapp/src/main/webapp/app/addons/annotation) with KML export,
 * mapfishapp: geofile upload: support of SHP, MIF/MID, GML, KML by default (uses a geotools implementation). Support of TAB and GPX when OGR is available (read this [how to](https://github.com/georchestra/georchestra/tree/master/mapfishapp#optional-install-gdal-native-library)),
 * mapfishapp: WMS + WFS version autodetection,
 * mapfishapp: WMTS 1.0.0 support,
 * mapfishapp: WFS 2.0.0 support,
 * mapfishapp: WMS 1.3.0 support,
 * mapfishapp: OWSContext 0.3.x READ support (WMS layers),
 * mapfishapp: French IGN's GeoPortail webservices support,
 * mapfishapp: Russian translation (!),
 * geoserver: Opaque WMS Layer property support (see [#158](https://github.com/georchestra/georchestra/issues/158)),
 * ldapadmin: a brand new module is available which allows at the same time to: admin your users in your browser, let your users recover their lost password, and let them register too ! More information in the module [README](ldapadmin/README.md),
 * extractorapp: ship metadata in ZIP (read this [how to](extractorapp/README.md#metadata-extraction)),
 * extractorapp: now supports KML & TIF+(TFW,TAB) output.

Enhancements:
 * mapfishapp: external libs such as ExtJS updated to 3.4.1.1 (for IE 10 support), GeoExt and OpenLayers updated to master (yeah !). This brings greater navigation ease and support for other cool stuff.
 * mapfishapp: added ability to restore contexts with a projection different from the map's (assuming layers will be able to reproject),
 * mapfishapp: print improved (updated to MapFish Print 2.0 for WMTS and GeoPortail support, brand new templates, natural 91 dpi resolution and new "comment" field),
 * mapfishapp: referentials search is no more case sensitive with WFS2,
 * mapfishapp: improved syntax for metadata search (via CSW), see [#325](https://github.com/georchestra/georchestra/pull/325),
 * mapfishapp: true print extent displayed,
 * mapfishapp: querier radius is now dynamically displayed in meters/km,
 * mapfishapp: WFS layers feature selection & attributes viewing, 
 * mapfishapp: layer name and source smartly ellipsed with CSS rather than JS,
 * mapfishapp: do not close window on style applied,
 * mapfishapp: layer style list is now alphabetically sorted,
 * mapfishapp: permalink validity displayed in months,
 * mapfishapp: link to layer metadata which is declared as text/html is now prefered over the other links,
 * mapfishapp: addons can now be loaded by default (read [how](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L64)),
 * mapfishapp: added a [note on data](mapfishapp/README.md#your-data-in-mapfishapp) in the doc,
 * extractorapp: several small fixes for extractorapp reprojection,
 * extractorapp: more visible extract button,
 * extractorapp: auto-deactivate the "Modify the bbox" button,
 * geonetwork: validation report: translated all reported XSD errors in editor,
 * geonetwork: user menu: improved links according to role,
 * geonetwork: RSS: added URL parameter to only return one link for each metadata,
 * geonetwork: widgets: added privileges panel to batch operation,
 * geonetwork: improved icons plus a custom icon for XLS files,
 * SDI Instance name in page titles across the SDI (```shared.instance.name```, defaulting to "geOrchestra"),
 * everywhere: plain text emails, with the ability to switch back to HTML via ```shared.email.html```,
 * everywhere: all outgoing emails are now prefixed with the platform name,
 * everywhere: better translations.
 * documentation: improved [installation](INSTALL.md) instructions

Bug fixes:
 * security-proxy: now only sends one referer headers - fixes consuming arcgis web services - [read more](https://github.com/georchestra/georchestra/issues/266),
 * geoserver: fixed incorrect WMS 1.3.0 scaledenominator values, see [#264](https://github.com/georchestra/georchestra/issues/264),
 * geonetwork: editor: suggestion: changes saved before processing,
 * geonetwork: user menu: IE compatibility issues fixed,
 * static: fixed incorrect login link on CAS pages,
 * cas: fixed IE8 JS error on login page,
 * extractorapp: fixed app loading on IE8,
 * extractorapp: now reports broken or uninstalled GDAL libraries,
 * mapfishapp: WM[T]S GetFeatureInfo geometries on the fly reprojection (at last !) - as a result, it is advised to fill the most widely used SRSes in your country in your [GEOR_custom.js](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L365) config file,
 * mapfishapp: fixed fontFamily not taken into account by styler - also requires ```apt-get install ttf-mscorefonts-installer```,
 * mapfishapp: fixed querier setup issue when WFS service is not available,
 * mapfishapp: more robust layer hydrating from namespaced capabilities,
 * mapfishapp: fixed zooming occuring while drawing features,
 * mapfishapp: mouse position rounding is now correct for ETRS89, RGF93 and other long-lat based projections,
 * mapfishapp: fixed scrolling on a map without any visible layer,
 * mapfishapp: fixed unresolved images for point symbolizer combo,
 * mapfishapp: fixed legend label not appearing when only one class is available,
 * mapfishapp: fixed incorrect describeFeatureType URLs,
 * mapfishapp: fixed undefined addons title and description when lang dict does not exist yet,
 * mapfishapp: fixed broken referentials search in IE8,
 * mapfishapp: fixed broken help url


UPGRADING:
 * mapfishapp:
   * default projection changes from EPSG:2154 to EPSG:3857 (aka Spherical Web Mercator). Your users might need to clear their localStorage, or force loading of the new default context.
   * default MAP_SCALES changes to match the OGC WMTS spec,
 * LDAP: see [georchestra/LDAP#2](https://github.com/georchestra/LDAP/pull/2)
   * one group was renamed: ```STAT_USER``` becomes ```MOD_ANALYTICS``` - grants access to the analytics app,
   * an other one was created: ```MOD_LDAPADMIN``` - grants access to the LDAPadmin private UI (/ldapadmin/privateui/index.html).
 * The default application language is now **English**:
   * ```shared.language``` = en
   * ```geonetwork.language``` = eng
   * default email templates [here](https://github.com/georchestra/georchestra/tree/master/config/defaults/ldapadmin/WEB-INF/templates) and [there](https://github.com/georchestra/georchestra/tree/master/config/defaults/extractorapp/WEB-INF/templates): be sure to override them in your own config !
 * Remember also to fill these new global maven filters: 
   * ```shared.homepage.url``` - for your SDI home page (might be something like http://my.sdi.org/portal/),
   * ```shared.instance.name``` - will be displayed in page titles (eg: GeoMyCompany),
   * ```shared.email.html``` - whether to send emails in plain text (default) or HTML,
   * ```shared.administrator.email``` - this email receives new account requests (eg: me@mycompany.com)
 * shared maven filters renamed:
   * ```shared.smtp.replyTo``` -> ```shared.email.replyTo```
   * ```shared.smtp.from``` -> ```shared.email.from```
 * frontend webserver:
   * add a proxy rule for `/_static/` subdirectory (see https://github.com/georchestra/georchestra/tree/master/INSTALL.md)


Version 13.06
==============

This release was supported by the French GeoPicardie, GeoBretagne and CIGALsace projects, the GIP ATGeRi and individual contributors.

New features:
 * geoserver: updated to 2.3.2,
 * geoserver: patched to include ScaleHint (WMS 1.1.x) and Min/MaxScaleDenominator (WMS 1.3.0) in the capabilities documents, according to the default layer SLD. The patched was accepted in GeoServer master, see http://jira.codehaus.org/browse/GEOS-572

Enhancements:
 * the header is now part of the static module, and all webapps make use of it. As a result, requests to static pass through the security-proxy,
 * the header height can be set across all applications by the way of a unique shared maven filter,
 * mapfishapp: the CSW querier filters results on type = dataset OR series,
 * extractorapp: supported output formats combos made configurable, ECW output format expunged by default,
 * the template config was slimmed down: files where default values are suitable for most deployments were moved to config/defaults,
 * the documentation was improved ([mapfishapp](mapfishapp/README.md#feature-editor), [install](INSTALL.md), [manage configs](README.md#how-to-customize-)),
 * we now have CI on JS files syntax with Travis !

Bug fixes:
 * fixed the ability to turn off the downloadform with shared.downloadform.activated,
 * mapfishapp: referentials search now sends the map srs in the WFS query to allow feature reprojection,
 * mapfishapp: fixed incorrect thumbnail URLs in CSW querier,
 * mapfishapp: fixed misaligned thumbnail in CSW querier,
 * mapfishapp: fixed window + grid problems on service opening, see [issue 109](https://github.com/georchestra/georchestra/issues/109),
 * mapfishapp: fixed error in GEOR.ows.hydrateLayerRecord when falling back to main service,
 * mapfishapp: fixed links to MD sheet, see [issue 110](https://github.com/georchestra/georchestra/issues/110),
 * mapfishapp: fixed broken legend after styling, see [issue 107](https://github.com/georchestra/georchestra/issues/107),
 * mapfishapp: more robust handling of incoming WMS server URLs (eg: those with a mapfile GET parameter),
 * geonetwork: fixed ldap attribute mapping.
 
UPGRADING:
 * mapfishapp's default thesaurus has been set to local.theme.test, which is the only one exported by GeoNetwork by default. Feel free to customize to suit your needs,
 * geonetwork upgrade instructions are available [here](https://github.com/georchestra/geonetwork/blob/georchestra-29/README.md).


Version 13.02
=============

This release was made possible thanks to support from the French GIP ATGeRi (http://cartogip.fr/) and contributors.

New features:
 * geoserver: updated to 2.3.0, see http://blog.geoserver.org/2013/03/18/geoserver-2-3-0-released-first-official-osgeo-release/
 * geoserver: useful extensions added in template profile, see http://applis-bretagne.fr/redmine/issues/4217
 * geonetwork: upgraded geonetwork to geonetwork master (2.9.0-pre)
 * extractorapp: extraction bbox is now part of the data bundle, see https://github.com/georchestra/georchestra/pull/35
 * mapfishapp: lon, lat and radius GET parameters for startup recentering, see https://github.com/georchestra/georchestra/pull/20
 * mapfishapp: switchable pointer coordinates SRS, see https://github.com/georchestra/georchestra/pull/25
 * mapfishapp: layers drag'n drop in layer manager, see http://applis-bretagne.fr/redmine/issues/1959
 * mapfishapp: OGC context switcher, see https://github.com/georchestra/georchestra/pull/26
 * mapfishapp: print layouts ACL, see https://github.com/georchestra/georchestra/pull/30
 * mapfishapp: spatial query based on a circle, see http://applis-bretagne.fr/redmine/issues/1957
 * mapfishapp: support for addons & magnifier addon, see https://github.com/georchestra/georchestra/pull/36
 * mapfishapp: cadastre addon, see https://github.com/georchestra/georchestra/pull/48
 * mapfishapp: support transitionEffect resize (aka "back buffers") on layers coming from a WMC, see https://github.com/georchestra/georchestra/pull/42

Enhancements:
 * mapfishapp: results panel displays URLs as html links, see https://github.com/georchestra/georchestra/pull/21
 * mapfishapp: add layer from thesaurus: metadata title first, see https://github.com/georchestra/georchestra/pull/23
 * mapfishapp: more visible layer names, see https://github.com/georchestra/georchestra/pull/22
 * mapfishapp: add zoomout button in the toolbar, see https://github.com/georchestra/georchestra/pull/24
 * mapfishapp: added ability to print protected geoserver layers, see https://github.com/georchestra/template/commit/bb424bd74f7504af93b5e5c708f807ce0b6fdca4
 * mapfishapp: more robust detection of WMS layers in CSW getRecords responses, see https://github.com/georchestra/georchestra/pull/4
 * mapfishapp: window buttons consistency and default actions, see https://github.com/georchestra/georchestra/pull/33
 * mapfishapp: by default, the map is now restored with its latest known state (context), see https://github.com/georchestra/georchestra/pull/50
 * mapfishapp: missing translations
 * mapfishapp, downloadform, extractorapp, security-proxy, ogc-server-statistics: the java packages now belong to org.georchestra
 * mapfishapp: DocController's maxDocAgeInMinutes was change to manage long integer value, see https://github.com/georchestra/georchestra/pull/81

Bug fixes:
 * security-proxy: Location header was erroneously removed in some cases, see https://github.com/georchestra/georchestra/commit/fef3d77ab4fe0e6045c47add1f84dbd7de3a8c4e
 * mapfishapp: fixed erroneous WMSC2WMS mapping, which prevented printing of the GeoBretagne OSM baselayer, see https://github.com/georchestra/georchestra/commit/159bd4f24ecb21b9c76f76d27c1736ec1040f0ab
 * mapfishapp: use toponymName instead of name in GeoNames results, see https://github.com/georchestra/georchestra/issues/45
 * mapfishapp: WFS layer source server now correctly displayed, see https://github.com/georchestra/georchestra/commit/945349a1935286af2e02bfd21f9d7d9eeb6481e7
 * mapfishapp: Styler 2nd load timing out fixed, see https://github.com/georchestra/georchestra/commit/7b28656a2a81d01c00ebe0ff5a55e571f43aa63c
 * mapfishapp: download style styler link did not always provide the current layer style, see https://github.com/georchestra/georchestra/commit/5c47caa38b8c975982776f2a35c0574217bc2a17
 * mapfishapp: fixed XML documents missing the prolog, see http://applis-bretagne.fr/redmine/issues/4536
 * mapfishapp: WFS layer redraw was throwing an error, see http://applis-bretagne.fr/redmine/issues/4544
 * LDAP: group membership is now declared with memberUid = user uid rather than full dn, see https://github.com/georchestra/georchestra/pull/91

UPGRADING:
 * LDAP tree needs to be migrated as a result of https://github.com/georchestra/georchestra/pull/91 :
    * ldif export: ldapsearch -xLLL -D "cn=admin,dc=georchestra,dc=org" -W > dump.ldif
    * migration: sed 's/\(memberUid: uid=\)\(.*\)\(,ou=users,dc=georchestra,dc=org\)/memberUid: \2/' dump.ldif > new.ldif
    * ldif import
 * mapfishapp config changes:
    * the print config folder should be moved from YOUR_CONFIG/mapfishapp/print to YOUR_CONFIG/mapfishapp/WEB-INF/print for security reasons, see https://github.com/georchestra/georchestra/issues/82
    * client side (see GEOR_config.js or GEOR_custom.js for more information):
        * MAP_POS_SRS1 and MAP_POS_SRS2 options have been replaced with POINTER_POSITION_SRS_LIST
        * DEFAULT_WMC option has been replaced with CONTEXTS
        * PRINT_LAYOUTS_ACL allows to fine-tune available printing layouts based on user roles
        * DEFAULT_PRINT_FORMAT is now replaced by DEFAULT_PRINT_LAYOUT
        * DEACCENTUATE_REFERENTIALS_QUERYSTRING option added (controls whether to deaccentuate the referentials widget query string or not)
    * server side:
        * There is a new maven filter for mapfishapp temporary documents: shared.mapfishapp.docTempDir (defaults to /tmp/mapfishapp)
    * don't forget to edit your WMCs to activate back buffers on base layers, see https://github.com/georchestra/georchestra/pull/42
 * In GeoNetwork, it is now recommended to use OGC:WMS protocol rather than OGC:WMS-1.1.1-http-get-map (or any other WMS tagged with a version) to declare WMS layers, see https://github.com/georchestra/georchestra/pull/4
