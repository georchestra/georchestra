The development branch is master. It can be used for testing and reporting errors.

For production systems, you are advised to use the branch named "stable".
This branch receives bug fixes as they arrive.

Every release is supported for 6 months.
Each time a new release is out, the stable branch is renamed according to the old release version, and a new "stable" branch is forked off master.


Version 13.09 (in the works)
============================

This major release was supported by the GeoPicardie, PIGMA, CIGALsace and GeoBretagne projects. 
The CRAIG (Centre Régional Auvergnat de l'Information Géographique) is also to be thanked for some nice patches.

New features:
 * mapfishapp: [annotation addon](https://github.com/georchestra/georchestra/tree/master/mapfishapp/src/main/webapp/app/addons/annotation) with KML export,
 * mapfishapp: geofile upload: support of SHP, MIF/MID, GML, KML by default (uses a geotools implementation). Support of TAB and GPX when OGR is available (read this [how to](https://github.com/georchestra/georchestra/tree/master/mapfishapp#optional-install-gdal-native-library)),
 * mapfishapp: querier radius is now dynamically displayed in meters/km,
 * mapfishapp: WMS + WFS version autodetection,
 * mapfishapp: WMTS 1.0.0 support,
 * mapfishapp: WFS 2.0.0 support,
 * mapfishapp: OWSContext 0.3.x READ support (WMS layers),
 * mapfishapp: French IGN's GeoPortail webservices support,
 * mapfishapp: Russian translation (!),
 * geoserver: Opaque WMS Layer property support (see [#158](https://github.com/georchestra/georchestra/issues/158)),
 * ldapadmin: a brand new module is available which allows at the same time to: admin your users in your browser, let your users recover their lost password, and let them register too ! More information in the module [README](https://github.com/georchestra/georchestra/blob/master/ldapadmin/README.md),
 * extractorapp: ship metadata in ZIP (read this [how to](https://github.com/georchestra/georchestra/blob/master/extractorapp/README.md#metadata-extraction)),
 * extractorapp: now supports KML, TIF+TFW/TAB output.

Enhancements:
 * mapfishapp: external libs such as ExtJS updated to 3.4.1.1 (for IE 10 support), GeoExt and OpenLayers updated to master (yeah !). This brings greater navigation ease and support for other cool stuff.
 * mapfishapp: restoring contexts with different projections,
 * mapfishapp: print improved (updated to MapFish Print 2.0 for WMTS and GeoPortail support, brand new templates, natural 91 dpi resolution and new "comment" field),
 * mapfishapp: referentials search is no more case sensitive with WFS2,
 * mapfishapp: improved syntax for metadata search (via CSW), see [#325](https://github.com/georchestra/georchestra/pull/325),
 * mapfishapp: true print extent displayed,
 * mapfishapp: WFS layers feature selection & attributes viewing, 
 * mapfishapp: layer name and source smartly ellipsed with CSS rather than JS,
 * mapfishapp: do not close window on style applied,
 * mapfishapp: layer style list is now alphabetically sorted,
 * mapfishapp: permalink validity displayed in months,
 * mapfishapp: link to layer metadata which is declared as text/html is now prefered over the other links,
 * mapfishapp: addons can now be loaded by default (read [how](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L64)),
 * extractorapp: several small fixes for extractorapp reprojection,
 * extractorapp: more visible extract button,
 * extractorapp: auto-deactivate the "Modify the bbox" button,
 * geonetwork: validation report: translated all reported XSD errors in editor,
 * geonetwork: user menu: improved links according to role,
 * geonetwork: RSS: added URL parameter to only return one link for each metadata,
 * geonetwork: widgets: added privileges panel to batch operation,
 * geonetwork: improved icons plus a custom icon for XLS files,
 * SDI Instance name in page titles across the SDI (shared.instance.name, defaulting to "geOrchestra"),
 * everywhere: plain text emails, with the ability to switch back to HTML via shared.email.html,
 * everywhere: all outgoing emails are now prefixed with the platform name,
 * everywhere: better translations.
 * improved [installation](https://github.com/georchestra/georchestra/blob/master/INSTALL.md) instructions

Bug fixes:
 * security-proxy: now only sends one referer headers - fixes consuming arcgis web services - [read more](https://github.com/georchestra/georchestra/issues/266),
 * static: fixed incorrect login link on CAS pages,
 * geonetwork: editor: suggestion: changes saved before processing,
 * geonetwork: user menu: IE compatibility issues fixed.
 * mapfishapp: WM[T]S GetFeatureInfo geometries on the fly reprojection (at last !) - as a result, it is advised to fill the most widely used SRSes in your country in your [GEOR_custom.js](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L365) config file,
 * mapfishapp: fixed fontFamily not taken into account by styler - also requires apt-get install ttf-mscorefonts-installer,
 * mapfishapp: fixed querier setup issue when WFS service is not available,
 * mapfishapp: more robust layer hydrating from namespaced capabilities,
 * mapfishapp: fixed zooming occuring while drawing features,
 * mapfishapp: mouse position rounding is now correct for ETRS89, RGF93 and other long-lat based projections,
 * mapfishapp: fixed scrolling on a map without any visible layer,
 * mapfishapp: fixed unresolved images for point symbolizer combo,
 * mapfishapp: fixed legend label not appearing when only one class is available,
 * mapfishapp: fixed incorrect describeFeatureType URLs,
 * mapfishapp: fixed broken referentials search in IE8,
 * mapfishapp: fixed broken help url.


UPGRADING:
 * mapfishapp: default projection changes from EPSG:2154 to EPSG:3857 (aka Spherical Web Mercator). Your users might need to clear their localStorage, or force loading of the new default context.
 * LDAP: one group was renamed: STAT_USER became MOD_ANALYTICS and an other one was created: MOD_LDAPADMIN. The latter grants access to the LDAPadmin private UI (/ldapadmin/privateui/index.html), while the former grants access to the analytics app. See [georchestra/LDAP#2](https://github.com/georchestra/LDAP/pull/2).
 * The default application language is now English: shared.language=en + geonetwork.language=eng + default email templates ... be sure to override these in your own config !
 * Remember also to fill these two new global maven filters: shared.homepage.url for your SDI home page (might be something like http://my.sdi.org/portal/) and shared.instance.name (eg: GeoMyCompany)


Version 13.06 (current stable version)
======================================

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
 * the documentation was improved ([mapfishapp](https://github.com/georchestra/georchestra/blob/master/mapfishapp/README.md#feature-editor), [install](https://github.com/georchestra/georchestra/blob/master/INSTALL.md), [manage configs](https://github.com/georchestra/georchestra/blob/master/README.md#how-to-customize-)),
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
